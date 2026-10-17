# NovaShop 案例研究：本体 → DDD 分层 → 微服务可运行标本

**日期：** 2026-10-17  
**状态：** Phase 1–3 已实现；本文为教学发布用设计说明  
**方法：** 教学切片优先（售后闭环 → 整店演示）  
**说明：** 与可运行代码同仓于案例根；研究单仓内另有写作原稿。

---

## 1. 目标与成功标准

### 1.1 一句话目标

用 NovaShop **售后闭环** 作为可运行标本，让初级学员走完「本体概念 → DDD 战略/战术 → Evans 四层代码 → 微服务集成 → 测试金字塔验证」，并保留演进到 **麻雀虽小五脏俱全的整店演示**（含前端）的路线图。

### 1.2 受众

- 初级程序员：希望深度入门本体建模、DDD 与微服务落地
- 能在本机 **Docker Desktop** 上复现并动手改规则/测试

### 1.3 Phase 1 成功标准

- `docker compose up --build` 可起
- smoke 跑通且**可观察两阶段**：审批后 `Refund` 为 `PENDING_EXTERNAL_EXECUTION` → PSP 回写后终态
- 关键业务规则有领域层（L1）测试；应用/集成/冒烟按金字塔配备
- 案例手册可读：概念精要 + 映射表 + 代码/测试锚点 + 复盘报告
- 编码符合本文「工程约束」；领域包无 Spring/JPA 泄漏

### 1.4 终局愿景（Phase 2–3）

可运行、可演示的小型电商：演示路径「看商品 → 下单 → 售后 → 退款」可点可跑（含前端），文档与测试不退化。

---

## 2. 已确认决策摘要

| 维度 | 决策 |
|------|------|
| 推进策略 | 方案 1：教学切片优先，再扩展整店 |
| Phase 1 业务 | 售后闭环（ApproveReturn → Refund 诞生 → ExecuteRefund → RecordRefundResult） |
| 技术栈 | Java 21 + Spring Boot 3.x；Gradle 多模块 |
| 服务 | `order-return` + `payment-refund` |
| 集成 | RabbitMQ + Transactional Outbox |
| 演示入口（Phase 1） | API + OpenAPI + 脚本；无前端 |
| 鉴权 | `X-Actor-Id` / `X-Actor-Role` + 领域/应用准入（模拟 submission 身份，≠ Dynamic Security） |
| 测试 | 测试金字塔 + TDD 风格；手写 Fake 优先 |
| 手册组织 | 新手册为主路径；三份既有理论文作前置深读 |
| 编码 | 工程约束见 §6 |
| 前端 | Phase 1 不做；Phase 2+ 纳入整店演示 |
| Refund 诞生 | **推荐默认：** `payment-refund` 消费 `ReturnApproved` 后、调 PSP **前** 创建 `Refund(PENDING_EXTERNAL_EXECUTION)`（Ontology Approve 创建 Refund 的分布式近似） |
| `refundId` | 由 `order-return` 在审批事务中 **预分配** 写入 outbox；`payment-refund` 用同一 id 创建聚合（PSP 幂等键） |

### 2.1 既有文档关系

| 文档 | 角色 |
|------|------|
| `docs/theory/palantir-foundry-ontology-theory.md` | 本体理论深读（含 NovaShop 案例 §11） |
| `docs/theory/ddd-application-architectures.md` | Evans 等应用架构深读 |
| `docs/theory/modeling-paradigms-relationship.md` | 范式关系正本清源 |
| `docs/handbook/` | 带代码的学习主路径与案例报告 |
| `docs/design/case-design.md` | 本规格（教学发布副本） |

---

## 3. 阶段范围

### 3.1 Phase 1（本规格实现范围）

| 产物 | 说明 |
|------|------|
| 案例研究手册 | `docs/handbook/` |
| 可运行代码 | 本仓库根：多服务、Compose、脚本、OpenAPI |
| 测试金字塔 | 每层均有真实用例（非空目录） |
| 映射工件 | Ontology ↔ DDD ↔ 代码对照表（含翻译说明） |
| 全店路线图 | 附录：Phase 2/3，含前端与五脏验收 |

### 3.2 Phase 1 非目标

- 完整前台商城 / SPA
- Keycloak 等完整 IAM / Foundry Dynamic Security 全栈
- Kafka、真实 Foundry/OSDK
- 促销 Scenario、独立风控模型服务、完整 `DecisionRecord` 对象族
- 服务网格、重型可观测平台

### 3.3 Phase 2–3（路线图，非本规格一次实现）

- Phase 2：演示所需支撑上下文（如订单只读、简单目录）+ **极简 Web 控制台**
- Phase 3：履约/库存等 + 更完整演示店面；可选薄「操作层投影」以对照 Foundry 定位

---

## 4. 本体 → DDD → 服务边界映射

### 4.0 定位免责（必读 · 防概念混淆）

本仓库在 **权威微服务内模拟** Foundry Ontology 的动能概念（Action、submission criteria、ExternalExecution 等），便于教学与 Docker 复现。

| 生产中的 Foundry Ontology | 本教学标本 |
|---------------------------|------------|
| 坐落在多系统之上的**操作层** | **不**实现真实 Ontology 引擎 |
| 投影企业对象并受治理写回 | 各 BC **自己就是写权威** |
| Action edit-set 可跨多 Object 原子提交 | 跨服务用 Outbox **近似**；手册必须标明差异 |

勿把 Spring 服务当成 Ontology，也勿把学术分类本体论当成 Foundry。权威叙述见 `modeling-paradigms-relationship.md`。

### 4.1 映射总原则

| 本体（Foundry） | DDD / 微服务落点 | 脚注 |
|-----------------|------------------|------|
| Object type | 实体 / 值对象 / 聚合内概念 | 非「一表一服务」 |
| Link type | 聚合内引用，或跨上下文 ID | Phase 1 不做 object-backed link 深挖 |
| Action + submission criteria | 应用用例 + 领域不变式/策略 | criteria ≠ 读 ACL / Dynamic Security |
| Function（纯计算） | 领域服务或纯函数 | ≠ 任意 `@Service`；≠ Action |
| Automation | 消费者 **再进入应用用例**（重检不变式） | ≠「监听器里直接改库/调 PSP」 |
| External side effect | ACL 端口（如 `PspRefundClient`）+ **`ExternalExecution` 实体** | Outbox 行 ≠ ExternalExecution |
| 审批审计（本期） | `ReturnRequest` 上 actor/reason/resolution + 事件载荷 | **不**把 ApproveReturn 称作 `DecisionRecord` |
| `DecisionRecord` | Phase 2+ / 风控等上下文再演示 | 见本体 §11.10 |
| Object set | 仓储查询 / 只读用例 | `PendingReturnReviews` ↔ `GET /returns/pending` |

### 4.2 Phase 1 限界上下文

```text
┌─────────────────────────────┐     ReturnApproved      ┌──────────────────────────────┐
│  Return Management          │ ────(outbox)──────────► │  Payment / Refund            │
│  (order-return)             │                         │  (payment-refund)            │
│  ReturnRequest 聚合         │ ◄── RefundSucceeded/    │  Refund 聚合                 │
│  ApproveReturn              │     RefundFailed        │   + ExternalExecution 实体   │
│  预分配 refundId            │                         │  ExecuteRefund               │
│                             │                         │  RecordRefundResult          │
│                             │                         │  RetryExternalExecution(简)  │
└─────────────────────────────┘                         └──────────────────────────────┘
```

| 上下文 | 服务 | 聚合与要点 | 本体对应 |
|--------|------|------------|----------|
| Return Management | `order-return` | `ReturnRequest` + `ReturnLine`；审批策略；预分配 `refundId` | `ReturnRequest`；`ApproveReturn` |
| Payment / Refund | `payment-refund` | `Refund` + **`ExternalExecution`**；PSP ACL | `Refund` / `ExternalExecution` / `ExternallyExecutable`；`ExecuteRefund`、`RecordRefundResult`、`RetryExternalExecution` |

**SalesOrder / Payment 权威服务：** Phase 1 不建；使用**版本化种子快照**（`salesOrderId`、可退额度、`paymentId`、行可退数量）。手册标明全店阶段再拆分，并提示种子不是长期真相源。

### 4.3 Ontology edit-set → 两服务：翻译说明（Blocker 修复）

**Ontology（理论 §11.5.2）中一次 `ApproveReturn`：**

- 修改 `ReturnRequest`
- **创建** `Refund`（状态 `PENDING_EXTERNAL_EXECUTION`）及与 Return/Payment 的链接
- **不**包含 PSP 实际出款（那是 `ExecuteRefund` / `RecordRefundResult`）

**本标本的分布式翻译（刻意近似，非 1:1）：**

| 步骤 | 服务 | 本地一致提交的内容 |
|------|------|-------------------|
| 1 | `order-return` | 退货审批通过 + 审计字段 + outbox `ReturnApproved`（含预分配 `refundId`、金额、行、paymentId） |
| 2 | `payment-refund` | **创建** `Refund(PENDING_EXTERNAL_EXECUTION)` + 初始 `ExternalExecution` 意图；**尚未**视 PSP 成功 |
| 3 | `payment-refund` | 应用用例 `ExecuteRefund` 经 `PspRefundClient` 发起外部调用 |
| 4 | `payment-refund` | `RecordRefundResult` 根据回调/对账更新 Refund 与 ExternalExecution |
| 5 | `order-return` | 可选消费终态事件，投影到退货查询视图 |

教学句：`ReturnApproved` 扮演的是「触发后续受治理执行」的侧向信号（近似 Automation 触发），**不是** Ontology `ApproveReturn` Action 本身。

两一致性边界仍成立：

1. 步骤 1 成功 ≠ PSP 已退款  
2. 步骤 2 成功（Refund 已存在且 PENDING）≠ PSP 已退款  
3. 只有步骤 4 成功才是资金侧终态  

### 4.4 动能序列与 `refundId`

**规范序列：**

`ApproveReturn` → `ReturnApproved` → **创建 Refund(PENDING_…)** → `ExecuteRefund` → ExternalExecution 尝试 → 回调 → `RecordRefundResult` → `RefundSucceeded|Failed` →（可选）退货侧投影

**`refundId`：** `order-return` 在审批成功时生成 UUID，写入领域审计/outbox；`payment-refund` **必须**用该 id 创建聚合；PSP 幂等键 = `refundId`。禁止监听器内另造冲突 id。

**适配器约束：** Rabbit 消费者只反序列化并调用应用服务（如 `StartRefundFromReturnApproved` → 内部再调创建 + `ExecuteRefund`）；**禁止**在 listener 内直接调 PSP 或改领域规则。

### 4.5 跨服务事件（最小集）

| 事件 | 发布方 | 消费方 |
|------|--------|--------|
| `ReturnApproved` | order-return | payment-refund |
| `RefundSucceeded` / `RefundFailed` | payment-refund | order-return |

`ReturnApproved` 载荷：`returnRequestId`、`refundId`、`paymentId`、`amount`、`currency`、`approvedLineIds`、`actorId`、`actorRole`、`reason`、`resolutionCode`、幂等键（可用 `refundId`）、可选 `schemaVersion`。

### 4.6 Evans 四层（每服务内）

```text
interfaces/api     → User Interface
application        → Application（用例、事务、outbox 写入）
domain             → Domain（聚合、不变式、领域事件）
infrastructure     → Infrastructure（JPA、Rabbit、假 PSP、时钟）
```

采用实用分层 + 端口倒置。手册注明与 Evans 2003 原文差异，并链回 `ddd-application-architectures.md`。

**ApproveReturn 准入（L1 锁住）：**

- 状态须 `PENDING_REVIEW`
- 客服角色金额 ≤ ¥500；超额需 `SUPERVISOR`
- 批准金额 ≤ 可退额度（种子 Payment/订单快照）
- **同一 `orderLineId` 不得重复批准退款 / 不可超过行剩余可退数量**
- 请求体含：批准行集合、`refundAmount`、`resolutionCode`、`reason`

---

## 5. 仓库结构、API 与运行时

### 5.1 布局

```text
.                                 # 案例根（独立 GitHub 发布时即为仓库根）
  README.md                       # 公开入口
  docs/
    handbook/                     # 学习主路径（见 §8）
    theory/                       # 理论深读 + figures
    design/case-design.md         # 本规格
  docker-compose.yml
  .env.example
  scripts/smoke-*.sh
  order-return/  payment-refund/  commerce/  fulfillment/  demo-web/
  platform/                       # 事件契约；无领域模型
```

### 5.2 核心 API 与用例

**应用用例（命名进入代码）：**

| 用例 | 服务 | 说明 |
|------|------|------|
| `ApproveReturn` | order-return | 审批 + outbox |
| `StartRefundFromReturnApproved` | payment-refund | 创建 Refund(PENDING) + ExternalExecution 草稿 |
| `ExecuteRefund` | payment-refund | 经 PSP ACL 发起；更新执行尝试 |
| `RecordRefundResult` | payment-refund | 可信回调/对账写终态 |
| `RetryExternalExecution` | payment-refund | Phase 1 可提供 API；smoke 可不走，但 L1/L2 覆盖受控重试 |

**order-return HTTP**

| 方法 | 路径 | 作用 |
|------|------|------|
| `POST` | `/returns` | 创建待审退货（含行与可退快照） |
| `GET` | `/returns/pending` | Object set：`PendingReturnReviews` |
| `POST` | `/returns/{id}/approve` | body：`lineIds`、`refundAmount`、`resolutionCode`、`reason`；头：`X-Actor-Id`、`X-Actor-Role` |
| `GET` | `/returns/{id}` | 详情 + 可选退款投影状态 |

**payment-refund HTTP**

| 方法 | 路径 | 作用 |
|------|------|------|
| `GET` | `/refunds/{id}` | Refund + **最新 ExternalExecution**（attemptCount、status、lastError） |
| `POST` | `/refunds/{id}/retry` | `RetryExternalExecution`（失败后受控重试） |
| `POST` | `/psp/callbacks/refunds` | 仅绑定 `RecordRefundResult` |

### 5.3 Docker Desktop

| 容器 | 角色 |
|------|------|
| `order-return` | 服务 |
| `payment-refund` | 服务 |
| `postgres` | DB：`order_return`、`payment_refund` |
| `rabbitmq` | 消息总线 |

假 PSP **内嵌**于 `payment-refund`（`PspRefundClient` 适配器）。可配置同步成功 / 异步回调 / 失败，便于测试。

### 5.4 配置

十二因子；角色金额阈值可配置；SpringDoc OpenAPI。

---

## 6. 工程约束（编码硬约束）

| 原则 | 落地 |
|------|------|
| 意图优先于框架 | 富领域模型；注解停在 interfaces / infrastructure |
| 小而尖的类型 | 金额、状态、角色等值对象；非法状态不可构造 |
| 命名即统一语言 | 与 Ontology/DDD 术语对齐；禁止 Manager/Util 垃圾桶 |
| 依赖方向干净 | Domain 零 Spring/JPA；端口向内 |
| 简单胜过巧妙 | Outbox/幂等写清楚；无超前平台抽象 |
| 错误是领域的一部分 | 领域结果/异常表达业务拒绝 |
| 测试即规格 | 可读测试名；先红后绿 |
| 可删性 | 一句话职责；错误共享不如局部重复 |
| 共享极薄 | `platform` 仅事件契约与测试夹具 |

默认：Java 21、不可变值对象、构造即校验。手册含好/坏示例一小节。

---

## 7. 测试金字塔与 TDD

### 7.1 形状

```text
        /\
       /E2\          极少：Compose smoke（含中间态断言）
      /----\
     / 集成 \        少：Outbox→Rabbit、仓储、PSP
    /--------\
   /  应用层  \      中：用例 + Fake 端口
  /------------\
 /   领域单测    \    多：聚合、策略、状态机、角色/金额/重复行
/----------------\
```

### 7.2 Phase 1 必备分层用例

**L1 领域**

- 角色/金额/状态机（同前）
- **同行重复退 / 超行可退数量 → 拒绝**
- `ExternalExecution`：同 `refundId` 幂等；失败记 FAILED；`Retry` 增加 attemptCount
- Refund 不得在「PSP 成功前」进入 SUCCEEDED

**L2 应用**

- `ApproveReturn`：状态 + outbox 含预分配 `refundId`
- `StartRefundFromReturnApproved`：创建 PENDING Refund（先于 PSP）
- `ExecuteRefund` / `RecordRefundResult` 分离
- 消费者路径只调应用服务（可用 Fake 断言）
- 缺角色头拒绝

**L3 集成**

- 持久化；outbox→消费幂等；PSP 成功/失败/重复回调

**L4 Smoke**

1. 创建退货 → 审批  
2. **轮询** `GET /refunds/{id}` 直至 `PENDING_EXTERNAL_EXECUTION`（或等价）  
3. 等待/触发回调 → 断言终态 SUCCEEDED  
4. `GET /returns/{id}` 投影一致  

### 7.3 TDD 节奏

L1 → L2 → L3/L4；禁止先堆 Controller 再补测。

### 7.4 工具

JUnit 5 + AssertJ；Fake 优先；L3 Testcontainers。

---

## 8. 手册与案例报告

### 8.1 章节

| 章 | 文件 | 内容 |
|----|------|------|
| 0 | `README.md` | 目标、前置、环境、学习顺序、整店愿景 |
| 1 | `01-concepts-bridge.md` | 概念精要 + §4.0 定位 + 链回旧文 |
| 2 | `02-ontology-to-ddd-map.md` | 映射表 + **§4.3 翻译说明** |
| 3 | `03-layered-code-tour.md` | 四层导读、工程约束、关键类 |
| 4 | `04-test-pyramid-and-tdd.md` | 分层用例索引与 TDD |
| 5 | `05-runbook-docker.md` | 起停、smoke（含中间态）、排障 |
| 6 | `06-retro-case-report.md` | 完整案例复盘报告 |
| 附录 | `roadmap-full-shop.md` | 整店 + 前端 + 五脏验收 |

### 8.2 案例报告必含块

业务问题；本体摘要；BC 切分与 **edit-set 翻译**；分层与不变式；Outbox 与 ExternalExecution；测试金字塔；运行证据；整店缺口。

### 8.3 终局「五脏俱全」验收（roadmap）

演示路径可运行；前端可操作；学员可复现；金字塔不退化。

---

## 9. 风险与对策

| 风险 | 对策 |
|------|------|
| 把分布式近似当成 Ontology 原子语义 | §4.0 / §4.3 强制写入手册与注释 |
| ExternalExecution 沦为状态枚举 | 一等实体 + GET 暴露 + 重试用例 |
| 种子额度与真实订单脱节 | 标明快照；L1 仍测不变式；Phase 2 接权威 |
| 监听器变成上帝 | 适配器只调应用服务 |
| 范围膨胀 / 前端拖垮 | Phase 门禁 |
| 用词：Automation / Dynamic Security | 映射表脚注 |

---

## 10. Phase 1 验收清单

- [ ] Compose 在 Docker Desktop 可起
- [ ] smoke：审批 → **可见 PENDING_EXTERNAL_EXECUTION** → 终态
- [ ] 角色/金额/**重复行** 有 L1 测试
- [ ] ExternalExecution 可查询；Execute / Record / Retry 用例存在
- [ ] 手册含 §4.0 定位与 §4.3 翻译说明
- [ ] 工程约束与依赖方向符合 §6
- [ ] OpenAPI 与演示脚本可用

---

## 11. 实现顺序（供 writing-plans 展开）

1. 手册骨架 + 映射/翻译说明初稿  
2. TDD：`order-return` 领域 → 应用 → 基础设施 → API  
3. TDD：`payment-refund`（Refund + ExternalExecution + PSP）  
4. Outbox + Rabbit；消费者只调应用服务  
5. Compose + 含中间态的 smoke  
6. 手册走读、Runbook、复盘定稿  

---

## 12. 开放项（实现计划钉死）

- Spring Boot / 依赖精确版本号  
- Outbox 表结构与轮询间隔  
- Money 值对象 API  
- CI（建议有，非教学阻塞）  
- 内嵌 PSP：默认同步成功 vs 显式回调模式（计划中二选一，测试两种都覆盖）

---

## 13. 修订记录

| 日期 | 变更 |
|------|------|
| 2026-10-17 | 初稿（brainstorming） |
| 2026-10-17 | 吸收 Critic/Architect Must-fix：edit-set 翻译、操作层定位、ExternalExecution、重复行、refundId、DecisionRecord 降级、API/用例/smoke 中间态 |
