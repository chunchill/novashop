# 架构总览（C4）

用 [C4 Model](https://c4model.com/) 四层里的前三层说明 NovaShop：**Context → Container → Component**，并加一张售后主路径的 **Dynamic**（时序化协作）。

| 层级 | 文档 | 读者 |
|------|------|------|
| 1 Context | [01-context.md](./01-context.md) | 所有人：谁在用、系统边界、外部依赖 |
| 2 Container | [02-container.md](./02-container.md) | 技术学员：可部署单元与通信 |
| 3 Component | [03-component-return-refund.md](./03-component-return-refund.md) | 开发：售后切片上的 Evans 分层 |
| Dynamic | [04-dynamic-approve-refund.md](./04-dynamic-approve-refund.md) | 开发：审批 → 退款 → 回调怎么走 |

**不做 Code 级 C4**：类级锚点见 [handbook/03-layered-code-tour.md](../handbook/03-layered-code-tour.md)。  
**不做生产 Deployment**：本标本目标是 Docker Desktop 可复现，见 [handbook/05-runbook-docker.md](../handbook/05-runbook-docker.md)。

图用 Mermaid `C4*` 语法，GitHub 可直接渲染。元素保持少而准；共享库 `platform/`（事件契约）**不是**可部署容器，只在文字里说明。

诚实边界（与手册一致）：

- 图上的「假 PSP」在实现里是 `payment-refund` 内嵌 Stub（`PSP_MODE`），画成外部是为对齐 Ontology **ExternalExecution**
- 退货创建经 **demo-web 种子**，不是 order-return ← commerce 的正式 HTTP ACL
- Spring 服务模拟 Ontology 动能，**不是**真实 Foundry 引擎
