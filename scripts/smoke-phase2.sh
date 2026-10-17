#!/usr/bin/env bash
# Phase 2 smoke: catalog → create order → create return → approve → PENDING → callback → SUCCEEDED
set -euo pipefail

BASE_CM=${COMMERCE_URL:-http://localhost:8083}
BASE_OR=${ORDER_RETURN_URL:-http://localhost:8081}
BASE_PR=${PAYMENT_REFUND_URL:-http://localhost:8082}

log() { printf '[smoke-phase2] %s\n' "$*"; }

wait_http() {
  local url=$1
  local timeout_s=${2:-90}
  local elapsed=0
  log "waiting for $url (timeout ${timeout_s}s)"
  until curl -sf "$url" >/dev/null 2>&1; do
    sleep 2
    elapsed=$((elapsed + 2))
    if (( elapsed >= timeout_s )); then
      echo "ERROR: timed out waiting for $url" >&2
      exit 1
    fi
  done
  log "ready: $url"
}

poll_json() {
  local url=$1
  local jq_expr=$2
  local expected=$3
  local timeout_s=${4:-30}
  local elapsed=0
  local actual=""
  while true; do
    if resp=$(curl -sf "$url" 2>/dev/null); then
      actual=$(echo "$resp" | jq -r "$jq_expr")
      if [[ "$actual" == "$expected" ]]; then
        echo "$resp"
        return 0
      fi
    fi
    sleep 1
    elapsed=$((elapsed + 1))
    if (( elapsed >= timeout_s )); then
      echo "ERROR: timed out after ${timeout_s}s waiting for $jq_expr == $expected at $url (last=$actual)" >&2
      exit 1
    fi
  done
}

# --- readiness ---
wait_http "$BASE_CM/products" 120
wait_http "$BASE_OR/returns/pending" 120
wait_http "$BASE_PR/v3/api-docs" 120

SUFFIX=$(date +%s)-$$
ORDER_ID="ord-p2-${SUFFIX}"
RETURN_ID="ret-p2-${SUFFIX}"

log "1) catalog has seeded SKUs"
PRODUCTS=$(curl -sf "$BASE_CM/products")
SKU_ID=$(echo "$PRODUCTS" | jq -r '.[0].skus[0].skuId')
if [[ -z "$SKU_ID" || "$SKU_ID" == "null" ]]; then
  echo "ERROR: no SKU in catalog: $PRODUCTS" >&2
  exit 1
fi
log "using sku=$SKU_ID"

log "2) create order $ORDER_ID"
ORDER_RESP=$(curl -sf -X POST "$BASE_CM/orders" \
  -H 'Content-Type: application/json' \
  -d "{
    \"orderId\": \"${ORDER_ID}\",
    \"lines\": [{\"skuId\": \"${SKU_ID}\", \"quantity\": 1}]
  }")
PAYMENT_ID=$(echo "$ORDER_RESP" | jq -r '.paymentId')
REFUNDABLE=$(echo "$ORDER_RESP" | jq -r '.refundableTotal')
LINE_ID=$(echo "$ORDER_RESP" | jq -r '.lines[0].lineId')
if [[ -z "$PAYMENT_ID" || "$PAYMENT_ID" == "null" || -z "$LINE_ID" || "$LINE_ID" == "null" ]]; then
  echo "ERROR: bad order response: $ORDER_RESP" >&2
  exit 1
fi
log "order created paymentId=$PAYMENT_ID refundable=$REFUNDABLE line=$LINE_ID"

log "3) create return from order snapshot (demo ACL: seed fields, order-return stays independent)"
CREATE_RESP=$(curl -sf -X POST "$BASE_OR/returns" \
  -H 'Content-Type: application/json' \
  -d "{
    \"id\": \"${RETURN_ID}\",
    \"paymentId\": \"${PAYMENT_ID}\",
    \"refundableTotal\": \"${REFUNDABLE}\",
    \"lines\": [{\"orderLineId\": \"${LINE_ID}\", \"remainingRefundableQty\": 1}],
    \"alreadyApprovedLineIds\": []
  }")
echo "$CREATE_RESP" | jq -e --arg id "$RETURN_ID" '.id == $id' >/dev/null
log "return created: $CREATE_RESP"

log "4) approve as SUPERVISOR"
APPROVE_RESP=$(curl -sf -X POST "$BASE_OR/returns/${RETURN_ID}/approve" \
  -H 'Content-Type: application/json' \
  -H 'X-Actor-Id: smoke-supervisor' \
  -H 'X-Actor-Role: SUPERVISOR' \
  -d "{
    \"lineIds\": [\"${LINE_ID}\"],
    \"refundAmount\": \"${REFUNDABLE}\",
    \"resolutionCode\": \"FULL\",
    \"reason\": \"smoke-phase2\"
  }")
REFUND_ID=$(echo "$APPROVE_RESP" | jq -r '.refundId')
if [[ -z "$REFUND_ID" || "$REFUND_ID" == "null" ]]; then
  echo "ERROR: approve did not return refundId: $APPROVE_RESP" >&2
  exit 1
fi
log "approved refundId=$REFUND_ID"

log "5) poll until refund status=PENDING_EXTERNAL_EXECUTION"
PENDING_RESP=$(poll_json "$BASE_PR/refunds/${REFUND_ID}" '.status' 'PENDING_EXTERNAL_EXECUTION' 45)
log "middle state: $(echo "$PENDING_RESP" | jq -c '{id,status,latestExecution}')"

log "6) PSP callback success"
CB_CODE=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE_PR/psp/callbacks/refunds" \
  -H 'Content-Type: application/json' \
  -d "{\"refundId\": \"${REFUND_ID}\", \"success\": true, \"failureReason\": null}")
if [[ "$CB_CODE" != "202" && "$CB_CODE" != "200" ]]; then
  echo "ERROR: PSP callback returned HTTP $CB_CODE" >&2
  exit 1
fi
log "callback accepted HTTP $CB_CODE"

log "7) poll until refund status=SUCCEEDED"
SUCCEEDED_RESP=$(poll_json "$BASE_PR/refunds/${REFUND_ID}" '.status' 'SUCCEEDED' 45)
log "terminal refund: $(echo "$SUCCEEDED_RESP" | jq -c '{id,status}')"

log "8) poll return projection refundStatus=SUCCEEDED"
RETURN_RESP=$(poll_json "$BASE_OR/returns/${RETURN_ID}" '.refundStatus' 'SUCCEEDED' 60)
log "return projection: $(echo "$RETURN_RESP" | jq -c '{id,status,refundId,refundStatus}')"

echo "SMOKE OK"
