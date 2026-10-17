# Dynamic · 审批到退款终态

Phase 1 主线。此处用 **sequenceDiagram**（C4 Dynamic 视图的常用画法）：编号步骤、少交叉，比 `C4Dynamic` 蜘蛛网更易读。

```mermaid
sequenceDiagram
  autonumber
  actor Agent as Support agent
  participant Demo as demo-web
  participant OR as order-return
  participant RMQ as RabbitMQ
  participant PR as payment-refund
  participant PSP as PSP stub

  Agent->>Demo: Approve return (UI)
  Demo->>OR: POST /returns/{id}/approve<br/>X-Actor-*
  Note over OR: Same TX: approve + mint refundId + outbox
  OR-->>Demo: 200 (approved)

  OR->>RMQ: Publish ReturnApproved (outbox job)
  RMQ->>PR: Deliver ReturnApproved
  Note over PR: Create Refund PENDING_EXTERNAL_EXECUTION
  PR->>PSP: ExecuteRefund attempt
  Note over PSP,PR: CALLBACK_LATER → stays PENDING

  Agent->>Demo: Simulate PSP callback
  Demo->>PR: POST callback
  Note over PR: RecordRefundResult → SUCCEEDED + outbox
  PR->>RMQ: Publish RefundTerminal
  RMQ->>OR: Deliver RefundTerminal
  Note over OR: Project terminal onto ReturnRequest
```

## 两处一致性边界

1. **审批本地事务**：`ReturnRequest` + outbox 同库提交  
2. **退款本地事务**：`Refund` / `ExternalExecution` + outbox  

跨服务无分布式事务：Ontology edit-set →「本地聚合 + 集成事件」。  
→ [handbook/02](../handbook/02-ontology-to-ddd-map.md) · [handbook/06](../handbook/06-retro-case-report.md)

## 中间态

`PSP_MODE=CALLBACK_LATER`（Compose 默认）：步骤 6–7 之间可断言 `PENDING_EXTERNAL_EXECUTION`。

## 代码锚点

| 步 | 锚点 |
|----|------|
| 审批 | `ApproveReturnHandler`、`ReturnRequest.approve` |
| 出站 | order-return `OutboxPublisherJob` |
| 入站退款 | `ReturnApprovedListener`、`StartRefund…`、`ExecuteRefundHandler` |
| 回调 | `PspCallbackController`、`RecordRefundResultHandler` |
| 投影 | `RefundTerminalListener` |
