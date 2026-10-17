# C4 Level 3 · Component（售后切片）

只展开 **order-return** 与 **payment-refund** 的内部组件，对应 Evans 实用四层 + 端口倒置。其它服务同构，不重复画。

类级走读 → [handbook/03-layered-code-tour.md](../handbook/03-layered-code-tour.md)。

```mermaid
C4Component
  title Component Diagram - Return and Refund path

  Container_Boundary(orderReturn, "order-return :8081") {
    Component(orApi, "ReturnController", "interfaces", "HTTP + X-Actor headers")
    Component(orApp, "ApproveReturnHandler", "application", "Txn: load, approve, outbox")
    Component(orDom, "ReturnRequest", "domain", "Aggregate; mints refundId")
    Component(orOutbox, "Outbox + Publisher", "infrastructure", "ReturnApproved to Rabbit")
    Component(orRepo, "JpaReturnRequestRepository", "infrastructure", "Persistence adapter")
    Component(orListen, "RefundTerminalListener", "infrastructure", "Projects terminal status")
  }

  Container_Boundary(payment, "payment-refund :8082") {
    Component(prListen, "ReturnApprovedListener", "infrastructure", "Starts pending Refund")
    Component(prStart, "StartRefundFromReturnApproved", "application", "Create Refund PENDING")
    Component(prExec, "ExecuteRefundHandler", "application", "Calls PSP port")
    Component(prDom, "Refund + ExternalExecution", "domain", "Pending then terminal")
    Component(prPsp, "StubPspRefundClient", "infrastructure", "PspRefundClient ACL")
    Component(prCb, "PspCallbackController", "interfaces", "RecordRefundResult")
    Component(prOutbox, "Outbox + Publisher", "infrastructure", "RefundTerminal to Rabbit")
    Component(prRepo, "JpaRefundRepository", "infrastructure", "Persistence adapter")
  }

  ContainerQueue(rmq, "RabbitMQ", "AMQP", "Integration events")

  Rel(orApi, orApp, "ApproveReturn")
  Rel(orApp, orDom, "approve()")
  Rel(orApp, orRepo, "save")
  Rel(orApp, orOutbox, "enqueue ReturnApproved")
  Rel(orOutbox, rmq, "publish")
  Rel(orListen, rmq, "consume RefundTerminal")
  Rel(orListen, orDom, "applyRefundTerminal")

  Rel(prListen, rmq, "consume ReturnApproved")
  Rel(prListen, prStart, "handle")
  Rel(prStart, prDom, "createPending")
  Rel(prStart, prRepo, "save")
  Rel(prStart, prExec, "execute after create")
  Rel(prExec, prPsp, "refund()")
  Rel(prCb, prDom, "recordResult")
  Rel(prCb, prOutbox, "enqueue RefundTerminal")
  Rel(prOutbox, rmq, "publish")
```

## 依赖方向（每个服务内）

```text
interfaces / infrastructure  →  application  →  domain
```

- **domain**：无 Spring / JPA  
- **application**：用例编排；只依赖端口  
- **infrastructure**：JPA、Rabbit、Stub PSP  
- **interfaces**：HTTP 适配

## 与 Ontology 概念的对应

| Ontology（模拟） | 组件落点 |
|------------------|----------|
| Action `ApproveReturn` | `ApproveReturnHandler` + `ReturnRequest.approve` |
| edit-set 跨对象 | 拆成本地事务 + `ReturnApproved` 消息（非单库原子） |
| ExternalExecution | `ExternalExecution` 实体 + `StubPspRefundClient` |
| 预分配 `refundId` | `ReturnRequest.approve` 铸 ID → outbox → 同 ID 建 `Refund` |

时序化协作 → [04-dynamic-approve-refund.md](./04-dynamic-approve-refund.md)
