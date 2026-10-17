# 架构总览（C4）

用 [C4 Model](https://c4model.com/) 说明 NovaShop。图做了**拆分与减线**：同一层用多张窄图代替一张蜘蛛网，GitHub Mermaid 更易读。

| 层级 | 文档 | 看什么 |
|------|------|--------|
| 1 Context | [01-context.md](./01-context.md) | 人、系统边界、概念上的 PSP |
| 2 Container | [02-container.md](./02-container.md) | ① HTTP 同步面 · ② 异步事件面（分两张） |
| 3 Component | [03-component-return-refund.md](./03-component-return-refund.md) | order-return / payment-refund 各一张竖向分层 |
| Dynamic | [04-dynamic-approve-refund.md](./04-dynamic-approve-refund.md) | 审批→退款→回调（sequence，比 C4Dynamic 更整洁） |

**不做 Code 级 C4** → [handbook/03-layered-code-tour.md](../handbook/03-layered-code-tour.md)  
**不做生产 Deployment** → [handbook/05-runbook-docker.md](../handbook/05-runbook-docker.md)

`platform/` 是事件契约共享模块，**不是**可部署容器。

诚实边界：

- 图上的 PSP 在实现里是 `payment-refund` 内嵌 Stub（对齐 Ontology ExternalExecution）
- 退货创建经 **demo-web 种子**，非正式跨 BC HTTP ACL
- Spring 服务模拟 Ontology 动能，不是真实 Foundry 引擎
