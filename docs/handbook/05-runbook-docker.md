# 05 · Runbook：Docker 起停与 Smoke

目标：在 Docker Desktop 上复现全栈，并**看见**中间态 `PENDING_EXTERNAL_EXECUTION`。

赶时间：手册 README 的 **30 分钟 Quickstart**（已写好 `POSTGRES_HOST_PORT=5433`）。本页是细节与排障。

规格要求：`docker compose up --build` + smoke 两阶段断言（§1.3 / §7.2）。

---

## 1. 前置

| 项 | 说明 |
|----|------|
| Docker Desktop | 已启动，可访问守护进程 |
| 工具 | 本机 `curl` + `jq`（smoke 脚本需要） |
| 工作目录 | 本仓库根 |

### 端口一览

| 端口 | 服务 |
|------|------|
| 8080 | `demo-web`（Thymeleaf） |
| 8081 | `order-return` |
| 8082 | `payment-refund` |
| 8083 | `commerce` |
| 8084 | `fulfillment` |
| `${POSTGRES_HOST_PORT:-5432}` | Postgres（宿主机映射；**忙则用 5433**） |
| 5672 / 15672 | RabbitMQ / 管理台 |

---

## 2. 容器拓扑

| 容器 / 服务 | 角色 |
|-------------|------|
| `demo-web` | Thymeleaf 控制台，端口 8080 |
| `order-return` | 退货 BC，端口 8081 |
| `payment-refund` | 退款 BC，端口 8082 |
| `commerce` | 目录 + 销售订单，端口 8083 |
| `fulfillment` | 库存 + 履约（消费 `OrderPlaced`），端口 8084 |
| `postgres` | DB：`order_return`、`payment_refund`、`commerce`、`fulfillment` |
| `rabbitmq` | 总线：`novashop.order.placed` / `novashop.return.approved` / `novashop.refund.terminal` |

假 PSP **内嵌**于 `payment-refund`（`StubPspRefundClient`）。教学 Compose 宜设 `PSP_MODE=CALLBACK_LATER`，以便稳定断言中间态。

---

## 3. 起停与健康等待

```bash
# 在案例根目录执行（含 docker-compose.yml；独立发布后即为仓库根）

# 推荐：宿主机 Postgres 用 5433（避开本机已占用的 5432）
POSTGRES_HOST_PORT=5433 docker compose up --build -d

# 看状态（应用镜像内 HEALTHCHECK，start-period≈40s；首次 build 更久）
docker compose ps

# 等 demo-web 可访问（可选）
until curl -sf http://localhost:8080/ >/dev/null; do sleep 3; echo waiting…; done

# 看日志
docker compose logs -f commerce fulfillment order-return payment-refund

# 停止（保留 volume）
docker compose down
```

环境样例：`.env.example`（复制为 `.env` 按需改）。也可在 `.env` 里写死 `POSTGRES_HOST_PORT=5433`。

### 旧 volume：一次性建库（init 不会重跑）

`docker/postgres/init-databases.sql` **只在 volume 首次创建时**执行。若你从 Phase 1 / Phase 2 旧数据升上来，容器已有 volume，但缺新库——应用会报 `database "commerce" does not exist` 或 `fulfillment`。

在 **postgres 已 healthy** 后执行（可整段复制）：

```bash
docker compose exec postgres psql -U novashop -d postgres -c 'CREATE DATABASE commerce;'
docker compose exec postgres psql -U novashop -d postgres -c 'CREATE DATABASE fulfillment;'
```

若库已存在，psql 会报错，可忽略。然后：

```bash
docker compose restart commerce fulfillment
```

全新环境（愿意清空库）：`docker compose down -v` 后再 `up --build`，init 会自动建四个库。

本地无 Compose、只跑单测：

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home   # 按本机路径改
./gradlew test
```

---

## 4. Smoke（含中间态）

| 脚本 | 覆盖 |
|------|------|
| `scripts/smoke-return-refund.sh` | Phase 1：手工种子退货 → 审批 → PENDING → 回调 |
| `scripts/smoke-phase2.sh` | Phase 2：catalog → order → return → refund |
| `scripts/smoke-phase3.sh` | Phase 3：+ fulfillment `ALLOCATED` / 库存预留 |

```bash
chmod +x scripts/smoke-phase3.sh scripts/smoke-phase2.sh scripts/smoke-return-refund.sh

# 推荐先跑终局
./scripts/smoke-phase3.sh
# 期望末行：SMOKE OK
```

环境变量（可选）：

| 变量 | 默认 |
|------|------|
| `COMMERCE_URL` | `http://localhost:8083` |
| `ORDER_RETURN_URL` | `http://localhost:8081` |
| `PAYMENT_REFUND_URL` | `http://localhost:8082` |
| `FULFILLMENT_URL` | `http://localhost:8084` |

### Phase 1 断言顺序（规格强制；phase2/3 同样含中间态）

1. `POST /returns` 创建待审（phase2/3 则先经 commerce 下单再种子）  
2. `POST /returns/{id}/approve`（头：`X-Actor-Id`、`X-Actor-Role`）→ 拿到 `refundId`  
3. **轮询** `GET /refunds/{refundId}` 直至 `status=PENDING_EXTERNAL_EXECUTION`（超时约 30s）  
4. 若 `PSP_MODE=CALLBACK_LATER`：`POST /psp/callbacks/refunds` 成功体  
5. 轮询至 `SUCCEEDED`  
6. `GET /returns/{id}` 上投影的退款状态一致  

OpenAPI：各服务 SpringDoc（通常 `/swagger-ui.html` 或 `/v3/api-docs`）。

---

## 5. 手工 curl 示例（未测命令可改端口）

创建退货：

```bash
curl -s -X POST http://localhost:8081/returns \
  -H 'Content-Type: application/json' \
  -d '{
    "id": "ret-demo-1",
    "paymentId": "pay-1",
    "refundableTotal": "200.00",
    "lines": [{"orderLineId": "L1", "remainingRefundableQty": 1}],
    "alreadyApprovedLineIds": []
  }'
```

审批（SUPERVISOR）：

```bash
curl -s -X POST http://localhost:8081/returns/ret-demo-1/approve \
  -H 'Content-Type: application/json' \
  -H 'X-Actor-Id: u1' \
  -H 'X-Actor-Role: SUPERVISOR' \
  -d '{
    "lineIds": ["L1"],
    "refundAmount": "200.00",
    "resolutionCode": "FULL",
    "reason": "damaged"
  }'
```

查退款（观察中间态 / ExternalExecution）：

```bash
curl -s http://localhost:8082/refunds/<refundId>
```

回调（`CALLBACK_LATER`）：

```bash
curl -s -X POST http://localhost:8082/psp/callbacks/refunds \
  -H 'Content-Type: application/json' \
  -d '{"refundId":"<refundId>","success":true,"failureReason":null}'
```

> JSON 字段对齐 `CreateReturnRequest` / `ApproveReturnRequest` / `PspRefundCallbackRequest`。日常优先跑 smoke 脚本。

---

## 6. 排障

| 现象 | 排查 |
|------|------|
| `Bind for 0.0.0.0:5432 failed` | 用 `POSTGRES_HOST_PORT=5433`；或改 `.env` |
| `database "commerce"/"fulfillment" does not exist` | §3 建库一句话；或 `down -v` 重建 |
| demo-web 一直 starting | 依赖服务 HEALTHCHECK 未过；`docker compose ps` + `logs` |
| 审批后永远无 Refund | Outbox：`OutboxPublisherJob` 日志；队列 `payment-refund.return-approved` |
| 一直停在 PENDING | `PSP_MODE=CALLBACK_LATER` 且未回调；或 `FAIL` 后 `POST /refunds/{id}/retry` |
| 直接变 SUCCEEDED、看不到中间态 | 本地默认 `SUCCESS_SYNC` 太快；Compose 用 `CALLBACK_LATER` |
| 400 on approve | 缺 `X-Actor-Id` / `X-Actor-Role` |
| 订单页履约一直 n/a | 等 commerce outbox（~5s）+ fulfillment 消费；刷新订单页 |
| Listener 上帝类嫌疑 | 确认未注入 `PspRefundClient`（见 Isolation 测试） |

消息名（`application.yml`）：

- `novashop.order.placed` / `order.placed`
- `novashop.return.approved` / `return.approved`
- `novashop.refund.terminal` / `refund.terminal`

---

## 7. 依赖工具

Smoke 需要 `curl` 与 `jq`。Compose 默认 `PSP_MODE=CALLBACK_LATER`（见 `.env.example`）。
