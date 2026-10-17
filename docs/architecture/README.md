# 架构总览（C4 Model）

文档遵循 [C4 Model](https://c4model.com/) 的抽象层级（Context → Container → Component → Dynamic），但**绘图语法不用 Mermaid 原生 `C4*`**。

原因：`C4Context` / `C4Container` 等在 Mermaid 里仍是实验特性，GitHub 渲染时自动布线容易交叉缠绕，几乎无法精细控布局（见 [Mermaid C4 说明](https://mermaid.js.org/syntax/c4.html)）。  
本仓做法（与常见工程实践一致）：

| 视图 | 画法 | 为何 |
|------|------|------|
| Context / Container / Component | `flowchart` + `subgraph` 分层 | 声明顺序 ≈ 视觉层次，线少、方向单一 |
| Dynamic（协作） | `sequenceDiagram` | 天生适合编号步骤，无蜘蛛网 |

| 文档 | 内容 | 表达模型 |
|------|------|----------|
| [01-context.md](./01-context.md) | 系统上下文 | C4 L1（flowchart） |
| [02-container.md](./02-container.md) | 容器与通信 | C4 L2（flowchart） |
| [03-component-return-refund.md](./03-component-return-refund.md) | 售后分层组件 | C4 L3（flowchart） |
| [04-dynamic-approve-refund.md](./04-dynamic-approve-refund.md) | 审批→退款协作 | C4 Dynamic（sequence） |
| [05-tech-stack.md](./05-tech-stack.md) | 技术栈与版本 | **Stack inventory**（非 C4） |
| [adr/](./adr/) | 非显然选型 | **ADR** |

**不做 Code 级图** → [handbook/03](../handbook/03-layered-code-tour.md)  
**不做生产 Deployment** → [handbook/05](../handbook/05-runbook-docker.md)

诚实边界：PSP 课内为 Stub；退货创建经 demo-web 种子；非真实 Foundry 引擎。
