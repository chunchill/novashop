# Architecture Decision Records

只记录**非显然**的架构/技术决策。格式精简（背景 → 决策 → 后果）。  
技术清单见 [../05-tech-stack.md](../05-tech-stack.md)。

| ADR | 标题 |
|-----|------|
| [001](./001-transactional-outbox.md) | 跨服务用 Transactional Outbox，不用分布式事务 |
| [002](./002-stub-psp-and-callback-later.md) | PSP 用进程内 Stub + CALLBACK_LATER |
| [003](./003-demo-web-return-seed.md) | 退货创建经 demo-web 种子，暂不做跨 BC HTTP ACL |
| [004](./004-test-pyramid-layout.md) | `src/main` 按 DDD 分层；`src/test` 按金字塔目录 |
