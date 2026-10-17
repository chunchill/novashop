# C4 Level 2 · Containers

每个 **Container** = 可独立部署的进程（或托管基础设施）。`platform/` 只是事件 DTO 的共享模块，不画成容器。

```mermaid
C4Container
  title Container Diagram - NovaShop

  Person(shopper, "Shopper", "Catalog and checkout")
  Person(agent, "Support agent", "Return approval")

  System_Boundary(novashop, "NovaShop") {
    Container(demo, "demo-web", "Spring + Thymeleaf", "BFF UI :8080; seeds CreateReturn from order JSON")
    Container(commerce, "commerce", "Spring Boot", "Catalog + SalesOrder; mints paymentId :8083")
    Container(fulfillment, "fulfillment", "Spring Boot", "Reserve stock on OrderPlaced :8084")
    Container(orderReturn, "order-return", "Spring Boot", "ReturnRequest aggregate; ApproveReturn :8081")
    Container(payment, "payment-refund", "Spring Boot", "Refund + ExternalExecution; stub PSP :8082")
    ContainerDb(pg, "PostgreSQL", "Postgres 16", "DBs: commerce, fulfillment, order_return, payment_refund")
    ContainerQueue(rmq, "RabbitMQ", "AMQP", "order.placed / return.approved / refund.terminal")
  }

  System_Ext(psp, "PSP (conceptual)", "Refund side effect")

  Rel(shopper, demo, "Uses UI", "HTTPS")
  Rel(agent, demo, "Uses agent console", "HTTPS")

  Rel(demo, commerce, "List products / create & get orders", "HTTP JSON")
  Rel(demo, fulfillment, "Read fulfillment & inventory", "HTTP JSON")
  Rel(demo, orderReturn, "Create return (seed) / approve / get", "HTTP JSON")
  Rel(demo, payment, "Get refund / simulate callback", "HTTP JSON")

  Rel(commerce, pg, "Reads/writes", "JDBC")
  Rel(fulfillment, pg, "Reads/writes", "JDBC")
  Rel(orderReturn, pg, "Reads/writes", "JDBC")
  Rel(payment, pg, "Reads/writes", "JDBC")

  Rel(commerce, rmq, "Publishes OrderPlaced via outbox", "AMQP")
  Rel(fulfillment, rmq, "Consumes OrderPlaced", "AMQP")
  Rel(orderReturn, rmq, "Publishes ReturnApproved; consumes RefundTerminal", "AMQP")
  Rel(payment, rmq, "Consumes ReturnApproved; publishes RefundTerminal", "AMQP")

  Rel(payment, psp, "ExecuteRefund (stub adapter)", "in-process StubPspRefundClient")
```

## 容器 ↔ 限界上下文

| Container | 端口 | BC / 职责 |
|-----------|------|-----------|
| `demo-web` | 8080 | 演示 BFF（非领域权威） |
| `commerce` | 8083 | Catalog + SalesOrder |
| `fulfillment` | 8084 | Inventory / Fulfillment |
| `order-return` | 8081 | Return Management |
| `payment-refund` | 8082 | Payment / Refund（捕获仍 deferred） |
| PostgreSQL | 5433→5432 | 每服务一库 |
| RabbitMQ | 5672 / 15672 | 集成事件总线 |

## 两条集成路径（对照手册）

1. **下单 → 履约**：`commerce` outbox `OrderPlaced` → `fulfillment` 预留库存  
2. **售后闭环**：审批 outbox `ReturnApproved` → `payment-refund` 建 `Refund(PENDING_…)` → Stub/回调 → `RefundTerminal` → 退货侧投影  

退货**创建**不经 Rabbit：demo-web 读订单 JSON 后 `POST /returns` 种子字段（教学过渡，非正式跨 BC ACL）→ [handbook/02 §4](../handbook/02-ontology-to-ddd-map.md)。

## 事件名（契约在 `platform/`）

| 路由键 / 逻辑名 | 发布方 | 消费方 |
|-----------------|--------|--------|
| `OrderPlaced` | commerce | fulfillment |
| `ReturnApproved` | order-return | payment-refund |
| `RefundTerminal` | payment-refund | order-return |

下一层 → [03-component-return-refund.md](./03-component-return-refund.md)
