# 07 · Phase 2 演示路径（UI + smoke）

目标：从「看商品 → 下单 → 申请售后 → 审批 → 退款中间态 → 回调终态」可点可跑。

Phase 1 售后闭环（`smoke-return-refund.sh`）保持可用；本页补前半段。

---

## 1. 服务与端口

| 服务 | 端口 | 角色 |
|------|------|------|
| `demo-web` | 8080 | Thymeleaf 控制台（目录 / 下单 / 客服） |
| `order-return` | 8081 | 退货审批 BC（Phase 1） |
| `payment-refund` | 8082 | 退款 + ExternalExecution（Phase 1） |
| `commerce` | 8083 | 目录 + 销售订单（Phase 2 模块化单体） |
| Postgres | `${POSTGRES_HOST_PORT:-5432}` | DB：`order_return` / `payment_refund` / `commerce` |
| RabbitMQ | 5672 / 15672 | 事件总线（含 `novashop.order.placed` / return / refund） |

`PSP_MODE=CALLBACK_LATER`（Compose 默认）以便稳定看到 `PENDING_EXTERNAL_EXECUTION`。

---

## 2. 起栈

```bash
# 在案例根目录执行（含 docker-compose.yml；独立发布后即为仓库根）

# 若本机 5432 被占用（推荐默认写 5433）：
POSTGRES_HOST_PORT=5433 docker compose up --build -d

# 从 Phase 1 旧 volume 升级时，init 脚本不会重跑；一次性建库（见 05 §3）：
# docker compose exec postgres psql -U novashop -d postgres -c 'CREATE DATABASE commerce;'
# docker compose exec postgres psql -U novashop -d postgres -c 'CREATE DATABASE fulfillment;'
```

浏览器打开：http://localhost:8080/

| 路径 | 做什么 |
|------|--------|
| `/` | 目录列表（调 commerce） |
| `/orders/new` | 选 SKU/数量下单 |
| `/orders/{id}` | 订单详情；**Request return for order** |
| `/agent/returns` | 待审列表 + 审批（表单填 Actor Id/Role） |
| `/agent/refunds/{id}` | 退款状态 + ExternalExecution；**Simulate PSP callback success (demo only)** |

ACL 说明：demo-web 读 commerce 订单 JSON，再把 `paymentId` / `refundableTotal` / lines **种子字段** POST 到 order-return。order-return 不依赖 commerce；跨服务 HTTP ACL 留待后续。Payment BC 权威服务仍 deferred（commerce mint `paymentId`）。`OrderPlaced` 已真实发布到 Rabbit（Phase 3 履约将消费）。

---

## 3. Smoke Phase 2

```bash
chmod +x scripts/smoke-phase2.sh
./scripts/smoke-phase2.sh
# 期望末行：SMOKE OK
```

路径：catalog → create order → create return → approve → `PENDING_EXTERNAL_EXECUTION` → callback → `SUCCEEDED`。

Phase 1 回归：

```bash
./scripts/smoke-return-refund.sh
```

---

## 4. 本地单测

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew test
```
