# C4 Level 3 · Component（售后切片）

两个服务**分图竖向排列**（interfaces → application → domain ← infrastructure），避免左右对打交叉线。  
类级走读 → [handbook/03-layered-code-tour.md](../handbook/03-layered-code-tour.md)。

---

## 3.1 order-return

```mermaid
C4Component
  title Components — order-return
  UpdateLayoutConfig($c4ShapeInRow="1", $c4BoundaryInRow="1")

  Container_Boundary(or, "order-return :8081") {
    Component(api, "ReturnController", "interfaces", "HTTP + X-Actor-*")
    Component(app, "ApproveReturnHandler", "application", "Txn + outbox enqueue")
    Component(dom, "ReturnRequest", "domain", "Aggregate; mint refundId")
    Component(repo, "JpaReturnRequestRepository", "infrastructure", "Persistence")
    Component(outbox, "OutboxPublisherJob", "infrastructure", "Publish ReturnApproved")
    Component(listen, "RefundTerminalListener", "infrastructure", "Project terminal state")
  }

  ContainerQueue(rmq, "RabbitMQ", "AMQP", "Bus")

  Rel(api, app, "ApproveReturn")
  Rel(app, dom, "approve()")
  Rel(app, repo, "save")
  Rel(app, outbox, "enqueue")
  Rel(outbox, rmq, "ReturnApproved")
  Rel(rmq, listen, "RefundTerminal")
  Rel(listen, dom, "applyRefundTerminal")
```

---

## 3.2 payment-refund

```mermaid
C4Component
  title Components — payment-refund
  UpdateLayoutConfig($c4ShapeInRow="1", $c4BoundaryInRow="1")

  Container_Boundary(pr, "payment-refund :8082") {
    Component(listen, "ReturnApprovedListener", "infrastructure", "Inbound event")
    Component(start, "StartRefund…Handler", "application", "Create PENDING refund")
    Component(exec, "ExecuteRefundHandler", "application", "Call PSP port")
    Component(dom, "Refund + ExternalExecution", "domain", "Lifecycle")
    Component(psp, "StubPspRefundClient", "infrastructure", "PSP ACL")
    Component(cb, "PspCallbackController", "interfaces", "Record result")
    Component(outbox, "OutboxPublisherJob", "infrastructure", "Publish RefundTerminal")
    Component(repo, "JpaRefundRepository", "infrastructure", "Persistence")
  }

  ContainerQueue(rmq, "RabbitMQ", "AMQP", "Bus")

  Rel(rmq, listen, "ReturnApproved")
  Rel(listen, start, "handle")
  Rel(start, dom, "createPending")
  Rel(start, repo, "save")
  Rel(start, exec, "then execute")
  Rel(exec, psp, "refund()")
  Rel(cb, dom, "recordResult")
  Rel(cb, outbox, "enqueue")
  Rel(outbox, rmq, "RefundTerminal")
```

---

## 依赖方向（每个服务内）

```text
interfaces / infrastructure  →  application  →  domain
```

| Ontology（模拟） | 落点 |
|------------------|------|
| Action `ApproveReturn` | `ApproveReturnHandler` + `ReturnRequest.approve` |
| 跨对象 edit-set | 本地事务 + `ReturnApproved`（非单库原子） |
| ExternalExecution | 领域实体 + `StubPspRefundClient` |
| 预分配 `refundId` | 审批铸 ID → 同 ID 建 `Refund` |

时序 → [04-dynamic-approve-refund.md](./04-dynamic-approve-refund.md)
