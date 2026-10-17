# 02 · Ontology → DDD → 代码映射

本章是手册核心：**映射总表** + **edit-set 分布式翻译**（规格 §4.3）。读完应能解释「Ontology 一次 ApproveReturn」在本标本里为什么变成多步。

单概念定义与测试锚点 → [00-concept-playbook.md](./00-concept-playbook.md)。

---

## 1. 映射总原则（规格 §4.1）

| 本体（Foundry） | DDD / 微服务落点 | 本标本锚点 |
|-----------------|------------------|------------|
| Object type | 实体 / 值对象 / 聚合内概念 | `Product`/`Sku`、`SalesOrder`、`ReturnRequest`、`Refund`、`ExternalExecution`、`Money` |
| Link type | 聚合内引用，或跨上下文 ID | `orderId`、`skuId`、`refundId`、`returnRequestId`、`paymentId`（mint，非 Payment BC） |
| Action + submission criteria | 应用用例 + 领域不变式/策略 | `CreateOrderHandler`；`ApproveReturnHandler` + `ReturnRequest.approve` |
| Function（纯计算） | 领域服务或纯函数 | Phase 1–2 几乎不用；≠ `@Service` |
| Automation | 消费者再进入应用用例 | `OrderPlacedListener` → `ReserveStock…`；`ReturnApprovedListener` → `StartRefund…` / `ExecuteRefund…` || External side effect | ACL 端口 + `ExternalExecution` | `PspRefundClient` / `StubPspRefundClient`；**Outbox 行 ≠ ExternalExecution** |
| 审批审计（本期） | 聚合字段 + 事件载荷 | `actorId` / `reason` / `resolutionCode`；**不叫** `DecisionRecord` |
| Object set | 仓储查询 / 只读用例 | `ListProductsHandler`；`ListPendingReturnsHandler` ↔ `GET /returns/pending` |
---

## 2. 限界上下文与服务

```text
┌─────────────────────────────┐     OrderPlaced         ┌──────────────────────────────┐
│  Catalog + SalesOrder       │ ────(outbox)──────────► │  Fulfillment / Inventory     │
│  (commerce :8083)           │     novashop.order.placed│  (fulfillment :8084)         │
│  Product / Sku；SalesOrder  │                         │  InventoryPosition；          │
│  mint paymentId（非 Payment BC）                       │  FulfillmentOrder ALLOCATED  │
└──────────────┬──────────────┘                         └──────────────────────────────┘
               │ demo-web 种子 CreateReturn（教学 ACL）
               ▼
┌─────────────────────────────┐     ReturnApproved      ┌──────────────────────────────┐
│  Return Management          │ ────(outbox)──────────► │  Payment / Refund            │
│  (order-return :8081)       │                         │  (payment-refund :8082)      │
│  ReturnRequest 聚合         │ ◄── RefundTerminal      │  Refund 聚合                 │
│  ApproveReturn              │     (SUCCEEDED/FAILED)  │   + ExternalExecution        │
│  预分配 refundId            │                         │  Start / Execute / Record    │
└─────────────────────────────┘                         └──────────────────────────────┘
```

| 上下文 | 服务 | 聚合与要点 | 本体对应 |
|--------|------|------------|----------|
| Catalog | `commerce`（包 `catalog`） | `Product` + `Sku`；只读目录 | Product / SKU object types |
| SalesOrder | `commerce`（包 `salesorder`） | `SalesOrder` + `OrderLine`；下单 mint `paymentId`；outbox `OrderPlaced` | SalesOrder；PlaceOrder |
| Fulfillment / Inventory | `fulfillment` | `InventoryPosition`；`FulfillmentOrder`；消费 `OrderPlaced` 预留库存 | Inventory / FulfillmentOrder |
| Return Management | `order-return` | `ReturnRequest` + `ReturnLine`；审批策略；预分配 `refundId`；创建仍靠种子 | `ReturnRequest`；`ApproveReturn` |
| Payment / Refund | `payment-refund` | `Refund` + `ExternalExecution`；PSP ACL | `Refund` / `ExternalExecution`；`ExecuteRefund`… |
| Payment（捕获权威） | — | **Deferred**：无独立 Payment BC | — |
事件契约（薄共享，无领域模型）：

- `com.novashop.platform.events.OrderPlaced`（exchange `novashop.order.placed`）
- `com.novashop.platform.events.ReturnApproved`
- `com.novashop.platform.events.RefundTerminal`
---

## 3. Ontology edit-set → 两服务：翻译说明（§4.3 · 必读）

### 3.1 Ontology 理论中一次 `ApproveReturn`

见本体文 [§11.5.2](../theory/palantir-foundry-ontology-theory.md)：

- 修改 `ReturnRequest`
- **创建** `Refund`（状态 `PENDING_EXTERNAL_EXECUTION`）及与 Return/Payment 的链接
- **不**包含 PSP 实际出款（那是 `ExecuteRefund` / `RecordRefundResult`）

### 3.2 本标本的分布式翻译（刻意近似，非 1:1）

| 步骤 | 服务 | 本地一致提交的内容 | 代码锚点 |
|------|------|-------------------|----------|
| 1 | `order-return` | 退货审批通过 + 审计字段 + outbox `ReturnApproved`（含预分配 `refundId`、金额、行、paymentId） | `ReturnRequest.approve` → `ApproveReturnHandler` → `JpaOutboxAdapter` / `OutboxPublisherJob` |
| 2 | `payment-refund` | **创建** `Refund(PENDING_EXTERNAL_EXECUTION)` + 初始 `ExternalExecution` 意图；**尚未**视 PSP 成功 | `StartRefundFromReturnApprovedHandler` → `Refund.createPending` |
| 3 | `payment-refund` | 应用用例 `ExecuteRefund` 经 `PspRefundClient` 发起外部调用 | `ExecuteRefundHandler` → `StubPspRefundClient` |
| 4 | `payment-refund` | `RecordRefundResult` 根据回调/对账更新 Refund 与 ExternalExecution | `RecordRefundResultHandler`；回调入口 `PspCallbackController` |
| 5 | `order-return` | 可选消费终态事件，投影到退货查询视图 | `RefundTerminalListener` → `ApplyRefundTerminalHandler` |

教学句：

> `ReturnApproved` 扮演的是「触发后续受治理执行」的侧向信号（近似 Automation 触发），**不是** Ontology `ApproveReturn` Action 本身。

对应概念卡：Action / Automation / 最终一致 / Outbox → [00](./00-concept-playbook.md) B7、B10、A13、A14。

### 3.3 两一致性边界（仍成立）

1. 步骤 1 成功 ≠ PSP 已退款  
2. 步骤 2 成功（Refund 已存在且 PENDING）≠ PSP 已退款  
3. 只有步骤 4 成功才是资金侧终态  

### 3.4 `refundId` 规则

- `order-return` 在审批成功时生成 UUID，写入领域 + outbox  
- `payment-refund` **必须**用该 id 创建聚合（`Refund.createPending(event.refundId(), …)`）  
- PSP 幂等键 = `refundId`（见 `ExecuteRefundHandler` / `StubPspRefundClient`）  
- **禁止**监听器内另造冲突 id  

---

## 4. 种子与权威（Phase 2–3 诚实说明）

| 事实 | 谁说了算 | 常见误读 |
|------|----------|----------|
| 商品 / 销售订单 | **commerce**（`SalesOrder` 聚合） | 「退货服务里也有订单权威」 |
| `paymentId` | commerce 在 `SalesOrder.place` **mint**（`pay-` + UUID） | 「已有完整 Payment BC」——**没有**；捕获/对账权威仍 deferred |
| 退货单上的可退额度 / 行 | **创建退货那一刻的快照** | 「order-return 实时查 commerce」——**默认没有** |
| 谁把订单快照塞进退货 | **demo-web**（`ReturnSeedMapper`）：读 `GET /orders/{id}` → `POST /returns` | 「这就是正式跨 BC ACL」——这是**教学过渡**：BFF 种子，不是 order-return 内嵌防腐层 |

演进方向（未实现）：order-return 增加只读 `OrderSnapshotClient`（ACL），创建退货时自行拉取；Payment BC 独立捕获后再由订单引用真实 `paymentId`。

代码锚点：

- mint：`commerce/.../salesorder/domain/order/SalesOrder.java`（`place`）  
- 种子映射：`demo-web/.../web/ReturnSeedMapper.java` + unit `ReturnSeedMapperTest`  
- 退货创建仍只认 POST body 快照：`order-return` `CreateReturnHandler`

---

## 5. 应用用例 ↔ HTTP

| 用例 | 服务 | HTTP |
|------|------|------|
| `ListProducts` / `GetProduct` | commerce | `GET /products`、`GET /products/{id}` |
| `CreateOrder` / `GetOrder` | commerce | `POST /orders`、`GET /orders/{id}` |
| `ReserveStockFromOrderPlaced` | fulfillment | （消息驱动） |
| 查询库存 / 履约 | fulfillment | `GET /inventory/{skuId}`、`GET /fulfillments/by-order/{orderId}` |
| `CreateReturn` / `ListPending` / `GetReturn` | order-return | `POST /returns`、`GET /returns/pending`、`GET /returns/{id}` |
| `ApproveReturn` | order-return | `POST /returns/{id}/approve` |
| `StartRefundFromReturnApproved` | payment-refund | （消息驱动，无对外 REST） |
| `ExecuteRefund` | payment-refund | （消息驱动后由 listener 串联） |
| `RecordRefundResult` | payment-refund | `POST /psp/callbacks/refunds` |
| `RetryExternalExecution` | payment-refund | `POST /refunds/{id}/retry` |
| 查询 Refund + 最新 ExternalExecution | payment-refund | `GET /refunds/{id}` |

审批请求头：`X-Actor-Id`、`X-Actor-Role`。Body：`lineIds`、`refundAmount`、`resolutionCode`、`reason`。

---

## 6. ApproveReturn 准入（L1 锁住）

- 状态须 `PENDING_REVIEW`
- 客服 `AGENT` 金额 ≤ ¥500；超额需 `SUPERVISOR`
- 批准金额 ≤ 可退额度（种子快照）
- 同一 `orderLineId` 不得重复批准 / 不可超过行剩余可退数量
- 测试锚点：`ReturnRequestTest`（`agent_may_not_approve_over_500`、`cannot_approve_same_line_twice_…` 等）

---

## 7. 适配器约束

Rabbit 消费者只反序列化并调用应用服务：

- `ReturnApprovedListener` → `StartRefundService` + `ExecuteRefundService`（**不**注入 `PspRefundClient`）
- `RefundTerminalListener` → `ApplyRefundTerminalService`

隔离测试：`ReturnApprovedListenerIsolationTest.listener_constructors_do_not_take_psp_client`。
