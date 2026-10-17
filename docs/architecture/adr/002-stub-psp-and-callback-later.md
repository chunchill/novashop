# ADR 002 · Stub PSP + CALLBACK_LATER

**状态：** Accepted  
**日期：** 2026-10

## 背景

ExternalExecution 应对接真实支付侧效应。引入沙箱 PSP 会增加账号、网络与 flaky 测试成本，干扰「看见中间态」这一教学目标。

## 决策

- `PspRefundClient` 端口 + **`StubPspRefundClient`** 适配器（进程内）  
- Compose 默认 **`PSP_MODE=CALLBACK_LATER`**：执行后保持 `PENDING_EXTERNAL_EXECUTION`，由 demo-web / smoke 再触发回调  

## 后果

- **正向：** 稳定断言中间态；零外部依赖；对齐 Ontology「外部执行」叙事  
- **代价：** Context 图上的 PSP 是概念外部，实现并非独立进程（文档已声明）  
- **演进：** 将来可换真实 HTTP PSP 适配器而不改领域模型  
