# 08 · Phase 3 麻雀虽小五脏俱全

目标：顾客路径 + 客服路径可点可跑：

```text
看商品 → 下单 → 履约/库存感知 → 申请售后 → 审批 → 退款中间态 → PSP 回调终态
```

仍不做：Keycloak / Kafka / 服务网格 / 真实 Payment BC。

---

## 1. 服务与端口

| 服务 | 端口 | 角色 |
|------|------|------|
| `demo-web` | 8080 | Thymeleaf：目录 / 下单 / 订单履约感知 / 客服退货退款 |
| `order-return` | 8081 | 退货审批 |
| `payment-refund` | 8082 | 退款 + ExternalExecution |
| `commerce` | 8083 | 目录 + SalesOrder；outbox `OrderPlaced` → `novashop.order.placed` |
| `fulfillment` | 8084 | 库存预留 + `FulfillmentOrder`（消费 `OrderPlaced`） |
| Postgres | `${POSTGRES_HOST_PORT:-5432}` | DB：`order_return` / `payment_refund` / `commerce` / `fulfillment` |
| RabbitMQ | 5672 / 15672 | 事件总线 |

`PSP_MODE=CALLBACK_LATER`（Compose 默认）以便看到 `PENDING_EXTERNAL_EXECUTION`。

---

## 2. 起栈

```bash
# 在案例根目录执行（含 docker-compose.yml；独立发布后即为仓库根）

# 若本机 5432 被占用（推荐默认写 5433）：
POSTGRES_HOST_PORT=5433 docker compose up --build -d

# 从旧 volume 升级时 init 不重跑；一次性建库（可整段复制，见 05 §3）：
# docker compose exec postgres psql -U novashop -d postgres -c 'CREATE DATABASE commerce;'
# docker compose exec postgres psql -U novashop -d postgres -c 'CREATE DATABASE fulfillment;'
```

浏览器：http://localhost:8080/

| 路径 | 视角 | 做什么 |
|------|------|--------|
| `/` | 顾客 | 目录 |
| `/orders/new` | 顾客 | 下单 |
| `/orders/{id}` | 顾客 | 订单 + **履约状态** + SKU 库存；申请退货 |
| `/agent/returns` | 客服 | 待审 / 审批 |
| `/agent/refunds/{id}` | 客服 | 退款 + **Simulate PSP callback success (demo only)** |

---

## 3. Smoke

```bash
chmod +x scripts/smoke-phase3.sh
./scripts/smoke-phase3.sh
# 期望末行：SMOKE OK
```

路径：catalog → order → assert fulfillment `ALLOCATED` + inventory reserved → return → approve → PENDING → callback → SUCCEEDED。

回归：

```bash
./scripts/smoke-phase2.sh
./scripts/smoke-return-refund.sh
```

---

## 4. 设计要点（勿破坏）

- `OrderPlaced` 由 commerce outbox **真实发布**；fulfillment listener → `ReserveStockFromOrderPlacedHandler`（不在 listener 里写规则）。
- 退货仍经 demo-web **种子字段**；Payment BC deferred（commerce mint `paymentId`）。
- `ExternalExecution` / edit-set 叙事与 Phase 1 一致。

---

## 5. 本地单测

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew test
```
