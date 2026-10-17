#!/usr/bin/env bash
# End-to-end smoke: create → approve → PENDING_EXTERNAL_EXECUTION → callback → SUCCEEDED → return projection
set -euo pipefail

BASE_OR=${ORDER_RETURN_URL:-http://localhost:8081}
BASE_PR=${PAYMENT_REFUND_URL:-http://localhost:8082}

log() { printf '[smoke] %s\n' "$*"; }

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
  # poll_json <url> <jq_expr> <expected> <timeout_s>
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
wait_http "$BASE_OR/returns/pending" 120
wait_http "$BASE_PR/v3/api-docs" 120

SUFFIX=$(date +%s)-$$
RETURN_ID="ret-smoke-${SUFFIX}"
PAYMENT_ID="pay-smoke-${SUFFIX}"
LINE_ID="L1"

log "1) create return $RETURN_ID"
CREATE_RESP=$(curl -sf -X POST "$BASE_OR/returns" \
  -H 'Content-Type: application/json' \
  -d "{
    \"id\": \"${RETURN_ID}\",
    \"paymentId\": \"${PAYMENT_ID}\",
    \"refundableTotal\": \"100.00\",
    \"lines\": [{\"orderLineId\": \"${LINE_ID}\", \"remainingRefundableQty\": 1}],
    \"alreadyApprovedLineIds\": []
  }")
echo "$CREATE_RESP" | jq -e --arg id "$RETURN_ID" '.id == $id' >/dev/null
log "created: $CREATE_RESP"

log "2) approve as SUPERVISOR"
APPROVE_RESP=$(curl -sf -X POST "$BASE_OR/returns/${RETURN_ID}/approve" \
  -H 'Content-Type: application/json' \
  -H 'X-Actor-Id: smoke-supervisor' \
  -H 'X-Actor-Role: SUPERVISOR' \
  -d "{
    \"lineIds\": [\"${LINE_ID}\"],
    \"refundAmount\": \"100.00\",
    \"resolutionCode\": \"FULL\",
    \"reason\": \"smoke-test\"
  }")
REFUND_ID=$(echo "$APPROVE_RESP" | jq -r '.refundId')
if [[ -z "$REFUND_ID" || "$REFUND_ID" == "null" ]]; then
  echo "ERROR: approve did not return refundId: $APPROVE_RESP" >&2
  exit 1
fi
log "approved refundId=$REFUND_ID"

log "3) poll until refund status=PENDING_EXTERNAL_EXECUTION (middle state)"
PENDING_RESP=$(poll_json "$BASE_PR/refunds/${REFUND_ID}" '.status' 'PENDING_EXTERNAL_EXECUTION' 45)
log "middle state observed: $(echo "$PENDING_RESP" | jq -c '{id,status,latestExecution}')"

log "4) PSP callback success (CALLBACK_LATER)"
CB_CODE=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE_PR/psp/callbacks/refunds" \
  -H 'Content-Type: application/json' \
  -d "{\"refundId\": \"${REFUND_ID}\", \"success\": true, \"failureReason\": null}")
if [[ "$CB_CODE" != "202" && "$CB_CODE" != "200" ]]; then
  echo "ERROR: PSP callback returned HTTP $CB_CODE" >&2
  exit 1
fi
log "callback accepted HTTP $CB_CODE"

log "5) poll until refund status=SUCCEEDED"
SUCCEEDED_RESP=$(poll_json "$BASE_PR/refunds/${REFUND_ID}" '.status' 'SUCCEEDED' 45)
log "terminal refund: $(echo "$SUCCEEDED_RESP" | jq -c '{id,status}')"

log "6) poll return projection refundStatus=SUCCEEDED"
RETURN_RESP=$(poll_json "$BASE_OR/returns/${RETURN_ID}" '.refundStatus' 'SUCCEEDED' 60)
log "return projection: $(echo "$RETURN_RESP" | jq -c '{id,status,refundId,refundStatus}')"

echo "SMOKE OK"
