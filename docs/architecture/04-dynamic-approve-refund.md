# Dynamic · 审批到退款终态

用 `sequenceDiagram`（C4 Dynamic 视图的推荐画法）：参与者从左到右固定，步骤自上而下编号。

```mermaid
sequenceDiagram
  autonumber
  actor Agent as Support agent
  participant Demo as demo-web
  participant OR as order-return
  participant RMQ as RabbitMQ
  participant PR as payment-refund
  participant PSP as PSP stub

  Agent->>Demo: Approve return
  Demo->>OR: POST /approve + X-Actor-*
  Note over OR: TX: approve, mint refundId, outbox
  OR-->>Demo: 200

  OR->>RMQ: ReturnApproved
  RMQ->>PR: deliver
  Note over PR: Refund PENDING_EXTERNAL_EXECUTION
  PR->>PSP: ExecuteRefund
  Note over PR,PSP: CALLBACK_LATER → still PENDING

  Agent->>Demo: Simulate PSP callback
  Demo->>PR: POST callback
  Note over PR: SUCCEEDED + outbox
  PR->>RMQ: RefundTerminal
  RMQ->>OR: deliver
  Note over OR: project onto ReturnRequest
```

## 两处一致性边界

1. 审批：`ReturnRequest` + outbox 同库  
2. 退款：`Refund` / `ExternalExecution` + outbox  

无跨服务分布式事务 → [handbook/02](../handbook/02-ontology-to-ddd-map.md) · [06](../handbook/06-retro-case-report.md)

## 锚点

| 步 | 代码 |
|----|------|
| 审批 | `ApproveReturnHandler`、`ReturnRequest.approve` |
| 出站 | order-return `OutboxPublisherJob` |
| 入站退款 | `ReturnApprovedListener` → `StartRefund…` → `ExecuteRefundHandler` |
| 回调 | `PspCallbackController`、`RecordRefundResultHandler` |
| 投影 | `RefundTerminalListener` |
