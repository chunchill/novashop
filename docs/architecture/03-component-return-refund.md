# Level 3 · Component（售后切片）

只展开售后两服务。阅读方向：**上 → 下**（接口 → 应用 → 领域；基础设施挂在侧枝）。  
类级走读 → [handbook/03](../handbook/03-layered-code-tour.md)。

---

## 3.1 order-return

```mermaid
flowchart TB
  api["ReturnController<br/><i>interfaces</i>"]
  app["ApproveReturnHandler<br/><i>application</i>"]
  dom["ReturnRequest<br/><i>domain</i>"]

  repo["JpaReturnRequestRepository"]
  outbox["OutboxPublisherJob"]
  listen["RefundTerminalListener"]
  rmq[("RabbitMQ")]

  api --> app
  app --> dom
  app -.-> repo
  app -.-> outbox
  outbox -->|"ReturnApproved"| rmq
  rmq -->|"RefundTerminal"| listen
  listen --> dom

  classDef layer fill:#1168bd,stroke:#0b4884,color:#fff
  classDef infra fill:#85bbf0,stroke:#5a8fbf,color:#003
  classDef bus fill:#438dd5,stroke:#2e6295,color:#fff
  class api,app,dom layer
  class repo,outbox,listen infra
  class rmq bus
```

主链实线（用例）；虚线 = 端口适配（持久化 / 出站）。

---

## 3.2 payment-refund

```mermaid
flowchart TB
  rmqIn[("RabbitMQ")]
  listen["ReturnApprovedListener"]
  start["StartRefundFromReturnApproved"]
  exec["ExecuteRefundHandler"]
  dom["Refund + ExternalExecution<br/><i>domain</i>"]
  psp["StubPspRefundClient"]
  cb["PspCallbackController"]
  outbox["OutboxPublisherJob"]
  repo["JpaRefundRepository"]
  rmqOut[("RabbitMQ")]

  rmqIn -->|"ReturnApproved"| listen
  listen --> start
  start --> dom
  start -.-> repo
  start --> exec
  exec --> psp
  cb --> dom
  cb -.-> outbox
  outbox -->|"RefundTerminal"| rmqOut

  classDef layer fill:#1168bd,stroke:#0b4884,color:#fff
  classDef infra fill:#85bbf0,stroke:#5a8fbf,color:#003
  classDef bus fill:#438dd5,stroke:#2e6295,color:#fff
  class start,exec,dom,cb layer
  class listen,psp,outbox,repo infra
  class rmqIn,rmqOut bus
```

入站事件在上，回调与出站在下；仍是单向下流，无左右对打。

---

## 依赖方向

```text
interfaces / infrastructure  →  application  →  domain
```

| Ontology（模拟） | 落点 |
|------------------|------|
| `ApproveReturn` | Handler + `ReturnRequest.approve` |
| edit-set | 本地 TX + `ReturnApproved` |
| ExternalExecution | 领域实体 + Stub PSP |
| 预分配 `refundId` | 审批铸 ID → 同 ID 建 Refund |

时序 → [04-dynamic-approve-refund.md](./04-dynamic-approve-refund.md)
