# ADR 004 · 测试金字塔目录约定

**状态：** Accepted  
**日期：** 2026-10

## 背景

若把「unit / integration」混进 `src/main` 包名，会污染 DDD 分层。若测试扁平堆在一起，学员分不清金字塔层次。

## 决策

| 位置 | 含义 |
|------|------|
| `src/main/.../{domain,application,infrastructure,interfaces}` | Evans 分层（生产代码） |
| `src/test/.../unit/` | 快、无 IO（或极少） |
| `src/test/.../integration/` | Spring / JPA / Testcontainers |
| `scripts/smoke-*.sh` | e2e 冒烟 |

Gradle 侧用约定包路径 + `failOnNoMatchingTests` 等，避免空层假装有测试。

## 后果

- **正向：** 主代码讲领域，测试目录讲策略；与手册 04 一致  
- **代价：** 新建测试必须放对目录；跨层引用要有意识  
