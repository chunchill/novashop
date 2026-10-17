# NovaShop 文档

本目录与可运行代码同仓，便于独立推送到 GitHub 供学员学习。

## 地图

```text
docs/
├── architecture/ # C4 架构总览（结构入口）
├── handbook/     # 学习主路径（先读这里）
├── theory/       # 理论深读（手册卡片上的「深读」链接）
└── design/       # 设计规格（范围、API、成功标准）
```

| 想… | 打开 |
|-----|------|
| 先看系统长什么样（C4） | [architecture/README.md](architecture/README.md) |
| 按课表学 | [handbook/README.md](handbook/README.md) |
| 2 小时自学实验 | [handbook/09-lab-2h.md](handbook/09-lab-2h.md) |
| 概念卡（DDD + Ontology） | [handbook/00-concept-playbook.md](handbook/00-concept-playbook.md) |
| 种子 vs 权威、ACL 诚实边界 | [handbook/02-ontology-to-ddd-map.md](handbook/02-ontology-to-ddd-map.md) |
| Docker / 排障 | [handbook/05-runbook-docker.md](handbook/05-runbook-docker.md) |
| Phase 1–3 复盘 | [handbook/06-retro-case-report.md](handbook/06-retro-case-report.md) |
| Foundry Ontology 理论 | [theory/palantir-foundry-ontology-theory.md](theory/palantir-foundry-ontology-theory.md) |
| Evans / 六边形 / 洋葱 / 整洁 | [theory/ddd-application-architectures.md](theory/ddd-application-architectures.md) |
| 本体 · DDD · OO · E-R · 微服务关系 | [theory/modeling-paradigms-relationship.md](theory/modeling-paradigms-relationship.md) |
| 设计规格 | [design/case-design.md](design/case-design.md) |

仓库根 [README.md](../README.md) 含 Quickstart 与端口表。

## 给维护者

- **学员可见、应随代码发布：** `architecture/`、`handbook/`、`theory/`、`design/`
- **研究单仓内的写作草稿 / 执行计划：** 仍可留在上级仓库的 `docs/superpowers/`，不必随 NovaShop 公开仓发布
- 改手册内链时保持相对路径：`../theory/…`、`../design/…`、`../architecture/…`
