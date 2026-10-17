# NovaShop

教学用迷你电商：用可运行代码走通

**Foundry 风格本体概念 → DDD 限界上下文 / 聚合 → Evans 分层 → 微服务集成 → 测试金字塔**

```text
看商品 → 下单 → 履约/库存 → 申请售后 → 审批 → 退款中间态 → PSP 回调终态
```

Java 21 · Spring Boot 3.4 · PostgreSQL · RabbitMQ · Docker Desktop

---

## 30 分钟跑通

前置：Docker Desktop、`curl`、`jq`。本机 Postgres 5432 常被占用时用 **5433**。

```bash
# 在本目录（含 docker-compose.yml）
cp -n .env.example .env   # 可选；示例已默认 POSTGRES_HOST_PORT=5433
POSTGRES_HOST_PORT=5433 docker compose up --build -d
docker compose ps         # 全部 healthy 后再继续

chmod +x scripts/*.sh
./scripts/smoke-phase3.sh # 期望末行：SMOKE OK
```

浏览器打开：**http://localhost:8080/**

| 若失败 | 先看 |
|--------|------|
| 端口占用 | 确认 `POSTGRES_HOST_PORT=5433` |
| `database "commerce" does not exist` | 旧 volume 不会重跑 init → [docs/handbook/05-runbook-docker.md](docs/handbook/05-runbook-docker.md) |
| Smoke 超时 | `docker compose logs -f commerce fulfillment payment-refund` |

停栈：`docker compose down`（加 `-v` 会删数据，慎用）。

---

## 文档怎么读

| 路径 | 给谁 | 内容 |
|------|------|------|
| [docs/README.md](docs/README.md) | 所有人 | 文档地图 |
| [docs/architecture/](docs/architecture/) | 先看结构 | **C4**：Context → Container → Component → Dynamic |
| [docs/handbook/](docs/handbook/) | 学员主路径 | 概念卡 → 映射 → 分层走读 → 测试 → Runbook → 实验单 |
| [docs/handbook/09-lab-2h.md](docs/handbook/09-lab-2h.md) | 自学 2 小时 | 跑 → 点 → 改测 |
| [docs/theory/](docs/theory/) | 深读 | Ontology / DDD 架构 / 范式关系 |
| [docs/design/case-design.md](docs/design/case-design.md) | 想看设计意图 | Phase 范围、API、成功标准 |

赶时间：**Quickstart → 打开店面 → 再读** [00-concept-playbook](docs/handbook/00-concept-playbook.md)。

---

## 仓库结构

```text
.
├── README.md                 ← 你在这里
├── docs/
│   ├── architecture/         # C4 架构图（Context / Container / Component）
│   ├── handbook/             # 学习主路径（00–09 + roadmap）
│   ├── theory/               # 理论深读 + figures
│   └── design/               # 案例设计规格
├── demo-web/                 # :8080 店面 + 客服台（Thymeleaf）
├── order-return/             # :8081 Return Management BC
├── payment-refund/           # :8082 Payment / Refund BC
├── commerce/                 # :8083 Catalog + SalesOrder
├── fulfillment/              # :8084 库存预留 / 履约
├── platform/                 # 薄事件契约（无领域模型）
├── scripts/                  # smoke / e2e
├── docker-compose.yml
└── .env.example
```

**诚实边界（读代码前先记住）：**

- Spring 服务 **模拟** Ontology 动能概念，**不是**真实 Foundry 引擎
- `paymentId` 由 commerce **mint**，Payment 捕获 BC 仍 deferred
- 退货创建经 demo-web **种子快照**，不是跨 BC 正式 HTTP ACL

细节见 [handbook/02 §4](docs/handbook/02-ontology-to-ddd-map.md)。

---

## 端口

| 服务 | 端口 |
|------|------|
| demo-web | 8080 |
| order-return | 8081 |
| payment-refund | 8082 |
| commerce | 8083 |
| fulfillment | 8084 |
| Postgres（宿主机） | `${POSTGRES_HOST_PORT:-5432}`，推荐 **5433** |
| RabbitMQ / 管理台 | 5672 / 15672（guest/guest） |

鉴权头（模拟）：`X-Actor-Id`、`X-Actor-Role`（`AGENT` \| `SUPERVISOR`）。AGENT 批准上限默认 ¥500。

---

## 本地测试

```bash
./gradlew test                          # unit + integration
./scripts/smoke-phase3.sh               # 需 Compose 已起
```

测试组织：`src/main` = DDD 分层；`src/test` = `unit/` → `integration/`；`scripts/` = e2e。说明见 [docs/handbook/04-test-pyramid-and-tdd.md](docs/handbook/04-test-pyramid-and-tdd.md)。

---

## 许可与来源

本案例用于教学与自学。理论文中的架构原图版权归原作者；见各 theory 文末引用。若你从更大的研究单仓拆出本目录独立发布，请保持 `docs/` 与可运行代码一并公开，避免「只有代码没有地图」。
