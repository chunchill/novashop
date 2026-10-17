# NovaShop 案例手册（Phase 1 → 3）

用可运行的小店走通：

**本体概念 → DDD 战略/战术 → Evans 四层代码 → 微服务集成 → 测试金字塔**

演示路径（终局）：

```text
看商品 → 下单 → 履约/库存感知 → 申请售后 → 审批 → 退款中间态 → PSP 回调终态
```

本手册是学习主路径。三份理论文只做深读链接，正文不重复长文。文档地图见 [../README.md](../README.md)；仓库入口见 [../../README.md](../../README.md)。

**从这里开始读概念：** [00-concept-playbook.md](./00-concept-playbook.md)（每张卡挂业务 + 可打开的代码锚点）。

---

## 30 分钟 Quickstart（Docker Desktop）

本机 5432 常被占用——**直接用 5433**（下列命令已写好）。

```bash
# 在案例根目录执行（含 docker-compose.yml；独立发布后即为仓库根）

# 1) 起全栈（含 commerce + fulfillment + demo-web）
POSTGRES_HOST_PORT=5433 docker compose up --build -d

# 2) 等健康（首次构建可能 3–8 分钟；HEALTHCHECK start-period≈40s）
docker compose ps
# 全部 healthy / running 后再继续；也可：
# until curl -sf http://localhost:8080/ >/dev/null; do sleep 3; done

# 3) Phase 3 smoke（catalog → order → fulfillment → return → refund）
chmod +x scripts/smoke-phase3.sh
./scripts/smoke-phase3.sh
# 期望末行：SMOKE OK

# 4) 浏览器点一遍
open http://localhost:8080/   # 或手动打开
```

| 若失败 | 先看 |
|--------|------|
| Postgres 报 port in use | 确认用了 `POSTGRES_HOST_PORT=5433` |
| `commerce` / `fulfillment` 起不来、日志说 database does not exist | 旧 volume 不会重跑 init → 见 [05-runbook-docker.md](./05-runbook-docker.md) §3 建库一句话 |
| Smoke 超时 | `docker compose logs -f commerce fulfillment payment-refund`；再等一轮健康 |

停栈：`docker compose down`（加 `-v` 会删库数据，慎用）。

---

## 你在旅程的哪一站？

| Phase | 你能跑什么 | 入口 |
|-------|------------|------|
| **1** | 售后闭环 API：审批 → 退款中间态 → 回调 | [05](./05-runbook-docker.md) + `smoke-return-refund.sh` |
| **2** | + 目录 / 下单 / Thymeleaf 控制台 | [07](./07-phase2-demo-path.md) + `smoke-phase2.sh` |
| **3**（当前终局） | + 履约/库存 + 全路径 UI | [08](./08-phase3-full-demo.md) + `smoke-phase3.sh` |

路线图勾选状态 → [roadmap-full-shop.md](./roadmap-full-shop.md)。

---

## 受众与前置

- 初级工程师：想把 Ontology / DDD / 微服务落到可改、可测的代码上
- 本机有 **Docker Desktop**、JDK 21、能跑 Gradle；smoke 需要 `curl` + `jq`
- **Ontology ≠ Spring 服务**：仓库在 BC 内*模拟*动能概念，不实现真实 Foundry 引擎——见 [01](./01-concepts-bridge.md) §1

---

## 学习路径（建议顺序）

| 步 | 读什么 | 做什么 |
|----|--------|--------|
| — | [../architecture/README.md](../architecture/README.md) | （可选）C4 Context → Container，建立系统地图 |
| 0 | [00-concept-playbook.md](./00-concept-playbook.md) | 扫 DDD + Ontology 概念卡；打开锚点类 |
| 1 | [01-concepts-bridge.md](./01-concepts-bridge.md) | 分清「Foundry 操作层」与本标本 |
| 2 | [02-ontology-to-ddd-map.md](./02-ontology-to-ddd-map.md) | edit-set 翻译表；`refundId` 与两一致性边界 |
| 3 | 代码：`order-return` + `payment-refund`（再扫 `commerce` / `fulfillment`） | 对照 [03-layered-code-tour.md](./03-layered-code-tour.md) |
| 4 | [04-test-pyramid-and-tdd.md](./04-test-pyramid-and-tdd.md) | **`src/main` = DDD 分层；`src/test` = `unit/` → `integration/`；`scripts/` = e2e smoke** |
| 5 | [05-runbook-docker.md](./05-runbook-docker.md) | Compose 细节、建库、排障 |
| 6 | [06-retro-case-report.md](./06-retro-case-report.md) | Phase 1–3 复盘（核心仍是两一致性边界） |
| 7 | [07-phase2-demo-path.md](./07-phase2-demo-path.md) | UI + `smoke-phase2.sh` |
| 8 | [08-phase3-full-demo.md](./08-phase3-full-demo.md) | 全路径 + `smoke-phase3.sh` |
| 9 | [09-lab-2h.md](./09-lab-2h.md) | **学员 2 小时实验单**（跑 → 点 → 改测） |
| 附录 | [roadmap-full-shop.md](./roadmap-full-shop.md) | 勾选状态与演进边界 |

赶时间：Quickstart → 打开 http://localhost:8080 → 再回头读 00 / 01 / 02。  
按实验单自学：直接打开 [09-lab-2h.md](./09-lab-2h.md)。

---

## 仓库锚点

| 路径 | 角色 |
|------|------|
| 本仓库根 | 可运行代码（Gradle 多模块） |
| `order-return/` | Return Management BC（:8081） |
| `payment-refund/` | Payment / Refund BC（:8082） |
| `commerce/` | Catalog + SalesOrder（:8083） |
| `fulfillment/` | 库存 + 履约；消费 `OrderPlaced`（:8084） |
| `demo-web/` | Thymeleaf 演示控制台（:8080） |
| `platform/` | 薄事件契约（`OrderPlaced` / `ReturnApproved` / `RefundTerminal`） |
| `scripts/` | `smoke-return-refund.sh` / `smoke-phase2.sh` / `smoke-phase3.sh` |
| 设计规格 | [../design/case-design.md](../design/case-design.md) |

---

## 理论深读（勿与手册混淆）

| 文档 | 用途 |
|------|------|
| [../theory/palantir-foundry-ontology-theory.md](../theory/palantir-foundry-ontology-theory.md) | Foundry Ontology 理论；NovaShop 案例见 §11 |
| [../theory/ddd-application-architectures.md](../theory/ddd-application-architectures.md) | Evans 分层等应用架构 |
| [../theory/modeling-paradigms-relationship.md](../theory/modeling-paradigms-relationship.md) | 本体 / DDD / OO / E-R / 微服务正本清源 |

---

## 成功标准（摘要）

**Phase 1（售后核心）**

- smoke：审批后可见 `Refund` = `PENDING_EXTERNAL_EXECUTION`，PSP 回写后终态
- 关键业务规则有领域层（L1）测试；领域包无 Spring / JPA 泄漏

**Phase 2–3（整店演示）**

- `POSTGRES_HOST_PORT=5433 docker compose up --build -d` 可起（见 Quickstart / Runbook）
- UI：目录 → 下单 → 订单页见履约/库存 → 客服审批 → 模拟 PSP
- `smoke-phase3.sh` 通过；映射表 / 操作层免责声明仍有效（Payment BC 仍 deferred）

---

## 环境速查（端口一览）

| 项 | 值 |
|----|-----|
| Java | 21（`JAVA_HOME` 指向 JDK 21） |
| Spring Boot | 3.4.x |
| `demo-web` | http://localhost:8080 |
| `order-return` | http://localhost:8081 |
| `payment-refund` | http://localhost:8082 |
| `commerce` | http://localhost:8083 |
| `fulfillment` | http://localhost:8084 |
| Postgres（宿主机） | `${POSTGRES_HOST_PORT:-5432}` → 容器内仍 5432；忙则用 **5433** |
| RabbitMQ | 5672；管理台 http://localhost:15672（guest/guest） |
| 鉴权头（模拟） | `X-Actor-Id` / `X-Actor-Role`（`AGENT` \| `SUPERVISOR`） |
| AGENT 金额上限 | ¥500（`novashop.agent-approve-limit`） |
| 假 PSP | Compose 默认 `PSP_MODE=CALLBACK_LATER`（便于看见中间态） |
