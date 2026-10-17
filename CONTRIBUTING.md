# 贡献指南

感谢关注 NovaShop。本仓优先服务**教学清晰度**与**可复现运行**，而不是功能堆叠。

## 改文档

1. 学员主路径只放在 `docs/handbook/`
2. 长理论放在 `docs/theory/`；手册用「深读」链接，不复制长文
3. 改完后自检相对链接：`../theory/…`、`../design/…`
4. 诚实边界（Payment mint、demo-web 种子 ≠ 正式 ACL、非真实 Ontology 引擎）不要弱化

## 改代码

1. 领域包保持零 Spring / JPA
2. 新测试放入 `src/test/java/.../unit/` 或 `.../integration/`，与金字塔一致
3. 本地：`./gradlew test`；有行为变更时更新对应 smoke（`scripts/smoke-*.sh`）
4. 跨服务契约变更同步改 `platform/` 与手册映射表

## 提 PR 前

- [ ] Compose 仍能 `POSTGRES_HOST_PORT=5433 docker compose up --build -d`
- [ ] 相关 smoke 通过
- [ ] 手册 / README 端口与路径未过期
