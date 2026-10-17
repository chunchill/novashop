# 00 · 概念 Playbook（DDD + Ontology）

用 NovaShop **售后闭环**（审批退货 → 创建退款 → PSP → 终态）把术语钉死。每张卡片：定义 → 为何要 → 业务落点 → 代码/测试 → 深读。

读完再进 [01-concepts-bridge.md](./01-concepts-bridge.md) / [02-ontology-to-ddd-map.md](./02-ontology-to-ddd-map.md)。规格总图见 [设计规格 §4](../design/case-design.md)。

---

## 怎么用

1. 先扫本页卡片，建立「词 → 售后故事」联想  
2. 对着本仓库根打开锚点类  
3. 跑对应 unit 测试验证不变式  
4. 需要理论时再点「深读」——**勿**把长文当入门正文  

---

## A. DDD 概念卡

### A1. 统一语言（Ubiquitous Language）

- **一句话定义**：团队与代码共用的业务词汇表；同一词在代码、API、事件里含义一致。
- **为何需要**：避免「refund 到底指申请还是出款」这类口语漂移。
- **NovaShop 落点**：`ApproveReturn`、`ReturnRequest`、`Refund`、`PENDING_EXTERNAL_EXECUTION`、`ReturnApproved`、`RefundTerminal` 贯穿规格、HTTP、领域与事件。
- **代码/测试锚点**：
  - 事件契约：`platform/.../ReturnApproved.java`、`RefundTerminal.java`
  - 状态枚举：`ReturnStatus`、`RefundStatus`、`ExternalExecutionStatus`
  - 契约测试：`platform/.../EventContractTest.java`
- **深读**：[modeling-paradigms-relationship.md](../theory/modeling-paradigms-relationship.md) §2.3、§4.3

### A2. 限界上下文（Bounded Context）

- **一句话定义**：模型生效的边界；边界外同一现实可有不同模型与权威。
- **为何需要**：售后审批规则与资金出款规则不该挤进同一个聚合。
- **NovaShop 落点**：
  - **Catalog + SalesOrder**（`commerce`）：目录与销售订单；下单 mint `paymentId`（**Payment BC deferred**）
  - **Fulfillment / Inventory**（`fulfillment`）：`InventoryPosition` + `FulfillmentOrder`；消费 `OrderPlaced` 预留库存（Phase 3）
  - **Return Management**（`order-return`）：退货审批、预分配 `refundId`；创建仍靠 demo-web 种子
  - **Payment / Refund**（`payment-refund`）：Refund 生命周期与 PSP
- **代码/测试锚点**：
  - 模块：`commerce/`、`fulfillment/`、`order-return/`、`payment-refund/`、`demo-web/`
  - 履约领域：`fulfillment/.../domain/inventory/InventoryPosition.java`、`domain/fulfillment/FulfillmentOrder.java`
  - 跨边界只传事件 ID/金额，不共享领域类
- **深读**：规格 [§4.2](../design/case-design.md)；[modeling-paradigms-relationship.md](../theory/modeling-paradigms-relationship.md) §5.2；[07-phase2-demo-path.md](./07-phase2-demo-path.md)、[08-phase3-full-demo.md](./08-phase3-full-demo.md)
### A3. 实体（Entity）

- **一句话定义**：有连续身份的对象；属性可变，靠 ID 识别。
- **为何需要**：同一笔退货/退款要跨步骤、跨事务被追踪。
- **NovaShop 落点**：`ReturnRequest`（退货单）、`ReturnLine`（行）、`Refund`、`ExternalExecution`（一次外部尝试记录）。
- **代码/测试锚点**：
  - `order-return/.../domain/returnrequest/ReturnRequest.java`、`ReturnLine.java`
  - `payment-refund/.../domain/refund/Refund.java`、`ExternalExecution.java`
  - unit：`.../unit/domain/returnrequest/ReturnRequestTest.java`、`.../unit/domain/refund/RefundTest.java`
- **深读**：规格 [§4.1](../design/case-design.md) Object type 行

### A4. 值对象（Value Object）

- **一句话定义**：无独立身份、由属性整体定义；通常不可变，可替换不可「改一半」。
- **为何需要**：金额、角色这类概念用错身份会污染模型（「两个 ¥100」不应各有 UUID）。
- **NovaShop 落点**：`Money`（scale 2）、`ActorRole`、状态枚举；审批命令里的金额/角色当作值传入。
- **代码/测试锚点**：
  - `order-return/.../domain/money/Money.java`、`domain/actor/ActorRole.java`
  - `payment-refund/.../domain/money/Money.java`
  - `ApproveCommand` / `ApproveReturnCommand` 承载值语义
- **深读**：手册 [03-layered-code-tour.md](./03-layered-code-tour.md) §2

### A5. 聚合（Aggregate）

- **一句话定义**：一组实体/值对象的一致性边界；对外只通过根修改，事务一次只改一个聚合。
- **为何需要**：行可退数量、审批状态、已批行集合必须一起成立。
- **NovaShop 落点**：
  - `ReturnRequest` 聚合（含 `ReturnLine`）
  - `Refund` 聚合（含 `ExternalExecution`）
- **代码/测试锚点**：同上实体类；跨服务**不**在一次本地事务里改两个聚合（见 Outbox / 最终一致）。
- **深读**：规格 [§4.2](../design/case-design.md)

### A6. 聚合根（Aggregate Root）

- **一句话定义**：聚合唯一对外入口；外部只持有根 ID。
- **为何需要**：禁止外面直接改 `ReturnLine` 绕过审批规则。
- **NovaShop 落点**：根是 `ReturnRequest`、`Refund`；仓储按根 ID 加载/保存。
- **代码/测试锚点**：
  - `ReturnRequest.approve(...)`、`Refund.createPending` / `recordResult` / `retry`
  - 端口：`ReturnRequestRepository`、`RefundRepository`
- **深读**：[03-layered-code-tour.md](./03-layered-code-tour.md) §2–§3

### A7. 不变式（Invariant）

- **一句话定义**：聚合必须始终为真的业务规则；违规则拒绝状态变更。
- **为何需要**：把「AGENT 不能批超 ¥500」锁在领域，而不是散落在 Controller。
- **NovaShop 落点**（节选）：
  - 状态须 `PENDING_REVIEW` 才能批
  - AGENT ≤ ¥500；超额需 `SUPERVISOR`
  - 批准额 ≤ 可退额度；同行不可重复批 / 超剩余数量
  - `SUCCEEDED` 只能经 `recordResult(true, …)`，不能经 `createPending` 直达
- **代码/测试锚点**：
  - `ReturnRequest.approve`；`ReturnRequestTest`（如 `agent_may_not_approve_over_500`）
  - `Refund.recordResult` / `recordExecutionAttempt`；`RefundTest`
- **深读**：手册 [02-ontology-to-ddd-map.md](./02-ontology-to-ddd-map.md) §5；规格 §4.6 准入列表

### A8. 领域事件（Domain Event）

- **一句话定义**：领域内「已发生的事实」；用于解耦后续反应。教学标本里跨服务事实以 **集成事件** 形式落在 `platform`。
- **为何需要**：审批完成后要通知退款侧，但不能让两个 BC 共享一个数据库事务。
- **NovaShop 落点**：`ReturnApproved`（含预分配 `refundId`）；`RefundTerminal`（SUCCEEDED/FAILED）。
- **代码/测试锚点**：
  - `platform/.../ReturnApproved.java`、`RefundTerminal.java`
  - 写出：`ApproveReturnHandler` → `OutboxPort.append`
  - 消费：`ReturnApprovedListener`、`RefundTerminalListener`
  - unit：`EventContractTest`、`ReturnApprovedListenerMappingTest`
- **深读**：规格 [§4.4–§4.5](../design/case-design.md)；手册 [02](./02-ontology-to-ddd-map.md) §3

### A9. 领域服务（Domain Service）

- **一句话定义**：不属于单一实体、但仍是领域规则的无状态（或薄状态）操作。
- **为何需要**：跨多个实体的规则不宜硬塞进某一个实体时使用。
- **NovaShop 落点**：Phase 1 **几乎不用**独立领域服务类；审批策略写在 `ReturnRequest.approve`，退款状态机写在 `Refund`。
- **代码/测试锚点**：无 `*DomainService` 类；对照 Ontology「Function」——本期也极少。
- **深读**：规格 [§4.1](../design/case-design.md) Function 行；[02](./02-ontology-to-ddd-map.md) §1

### A10. 应用服务（Application Service）

- **一句话定义**：用例编排：加载聚合 → 调用领域 → 持久化 → 发消息；**不含**核心业务规则。
- **为何需要**：把事务边界、端口调用与领域规则分开测。
- **NovaShop 落点**：`ApproveReturnHandler`、`StartRefundFromReturnApprovedHandler`、`ExecuteRefundHandler`、`RecordRefundResultHandler` 等。
- **代码/测试锚点**：
  - `order-return/.../application/ApproveReturnHandler.java`
  - `payment-refund/.../application/ExecuteRefundHandler.java` 等
  - unit：`ApproveReturnHandlerTest`、`RefundApplicationHandlersTest`
- **深读**：[ddd-application-architectures.md](../theory/ddd-application-architectures.md) §2（Application 层）

### A11. 仓储（Repository）

- **一句话定义**：按集合接口存取聚合；领域只依赖端口，不依赖 SQL/JPA。
- **为何需要**：领域单测可用内存 Fake，无需启库。
- **NovaShop 落点**：`ReturnRequestRepository` / `RefundRepository`（端口）；`Jpa*Repository`（适配器）。
- **代码/测试锚点**：
  - 端口：`application/port/ReturnRequestRepository.java`、`RefundRepository.java`
  - 适配：`infrastructure/persistence/JpaReturnRequestRepository.java`、`JpaRefundRepository.java`
  - IT：`.../integration/infrastructure/persistence/JpaReturnRequestRepositoryIT.java`、`JpaRefundRepositoryIT.java`
- **深读**：[ddd-application-architectures.md](../theory/ddd-application-architectures.md) §2.1 仓储注；[03](./03-layered-code-tour.md)

### A12. 防腐层（ACL）

- **一句话定义**：把外部系统模型翻译成本上下文语言，隔离外部协议变化。
- **为何需要**：PSP 回调格式 ≠ `Refund` 领域模型。
- **NovaShop 落点**：端口 `PspRefundClient`；实现 `StubPspRefundClient`；回调经 `PspCallbackController` → `RecordRefundResultHandler`。
- **代码/测试锚点**：
  - `application/port/PspRefundClient.java`
  - `infrastructure/psp/StubPspRefundClient.java`
  - unit：`StubPspRefundClientTest`；隔离：`ReturnApprovedListenerIsolationTest`（listener **不**直接持有 PSP）
- **深读**：规格 [§4.1](../design/case-design.md) External side effect 行

### A13. 最终一致（Eventual Consistency）

- **一句话定义**：跨聚合/跨服务不要求同一时刻一致，而要求在可接受延迟后收敛到正确状态。
- **为何需要**：Ontology 一次 Action 的跨对象原子 edit-set，在两微服务里被翻译成多步本地事务。
- **NovaShop 落点**：审批成功 ≠ Refund 已创建 ≠ PSP 已出款；三步各自本地一致，靠事件推进。
- **代码/测试锚点**：手册 [02](./02-ontology-to-ddd-map.md) §3.2–§3.3；smoke 可见中间态 `PENDING_EXTERNAL_EXECUTION`（见 [05-runbook-docker.md](./05-runbook-docker.md)）
- **深读**：规格 [§4.3](../design/case-design.md)

### A14. Outbox

- **一句话定义**：与业务写入同一本地事务写入「待发消息」表，再异步投递，避免「库已提交但消息丢」。
- **为何需要**：保证 `ReturnApproved` / `RefundTerminal` 至少与聚合状态同步发出意图。
- **NovaShop 落点**：`OutboxPort` / `RefundTerminalOutboxPort` + `JpaOutboxAdapter` + `OutboxPublisherJob`。
- **代码/测试锚点**：
  - `ApproveReturnHandler` 调 `outbox.append`
  - `infrastructure/outbox/OutboxPublisherJob.java`
  - unit：`OutboxPublisherJobTest`（两服务各有一份）
- **深读**：规格 §4.3；**Outbox 行 ≠ `ExternalExecution`**

### A15. Evans 四层（UI / Application / Domain / Infrastructure）

- **一句话定义**：把界面、用例编排、领域模型、技术实现分开的经典分层。
- **为何需要**：改 PSP 适配器不应迫使重写审批规则。
- **NovaShop 落点**（每服务内）：

```text
interfaces/api     → User Interface
application        → Application
domain             → Domain
infrastructure     → Infrastructure
```

- **代码/测试锚点**：包结构见 [03-layered-code-tour.md](./03-layered-code-tour.md)；领域包无 Spring/JPA import。
- **深读**：[ddd-application-architectures.md](../theory/ddd-application-architectures.md) §2、§7

### A16. 端口倒置（实用版）

- **一句话定义**：领域/应用定义端口（接口）；基础设施实现端口；依赖指向内（应用/领域），而非领域依赖 JPA。
- **为何需要**：Evans 2003 原文依赖可向下；本标本采用后继架构常见的「核心不依赖具体技术」写法。
- **NovaShop 落点**：`ReturnRequestRepository`、`OutboxPort`、`PspRefundClient` 定义在 `application.port`；`Jpa*` / `Stub*` 在 `infrastructure`。
- **代码/测试锚点**：Handler 构造注入端口；Spring 装配在 `ApplicationHandlersConfig`。
- **深读**：[ddd-application-architectures.md](../theory/ddd-application-architectures.md) §2.1 末段、§3（六边形）对照；手册 [01](./01-concepts-bridge.md) §5

---

## B. Ontology（Foundry）概念卡

> 提醒：本仓库在权威微服务内**模拟**动能概念，**不**实现真实 Ontology 引擎。见规格 [§4.0](../design/case-design.md)。

### B1. 操作层（Operational layer）

- **一句话定义**：Foundry Ontology 坐落在多系统之上，提供统一语义对象与受治理写回/行动面。
- **为何需要**：系统多时「看得见、动得了、可审计」比再建一张报表更关键。
- **NovaShop 落点**：教学标本用两 BC **自己当写权威** 来演示 Action/ExternalExecution 等概念；**不是**生产 Ontology。
- **代码/测试锚点**：无引擎；对照 [01-concepts-bridge.md](./01-concepts-bridge.md) §1 对照表。
- **深读**：[palantir-foundry-ontology-theory.md](../theory/palantir-foundry-ontology-theory.md) §1–§2；[modeling-paradigms-relationship.md](../theory/modeling-paradigms-relationship.md) §2.5、§4.5、§5.4

### B2. Object type

- **一句话定义**：真实世界实体/事件的模式（schema）；实例叫 Object。
- **为何需要**：先定「退货单是什么」再谈怎么批、怎么退。
- **NovaShop 落点**：`ReturnRequest`、`Refund`、`ExternalExecution`（以及种子侧的订单/支付快照概念）。
- **代码/测试锚点**：领域类同上；JPA `*Entity` 是持久化形状，**≠** Object type 本身。
- **深读**：本体文 [§4.1](../theory/palantir-foundry-ontology-theory.md)、案例 [§11.2.1](../theory/palantir-foundry-ontology-theory.md)

### B3. Property

- **一句话定义**：Object type 上的特征定义；实例上的具体值是 Property value。
- **为何需要**：状态、金额、审计字段要有稳定语义，才能做 submission criteria。
- **NovaShop 落点**：`status`、`refundAmount`、`actorId`/`reason`/`resolutionCode`、`attemptCount` 等。
- **代码/测试锚点**：`ReturnRequest` 字段；`ExternalExecution.attemptCount` / `lastError`；API `ReturnResponse` / `RefundResponse`。
- **深读**：本体文 [§4.2](../theory/palantir-foundry-ontology-theory.md)、[§6.2](../theory/palantir-foundry-ontology-theory.md)

### B4. Link type

- **一句话定义**：两 Object type 之间关系的模式；实例是 Link。
- **为何需要**：从退货导航到退款、支付，决策路径才闭合。
- **NovaShop 落点**：跨上下文用 ID 关联：`returnRequestId`、`refundId`、`paymentId`；聚合内 `ReturnRequest`→`ReturnLine`。
- **代码/测试锚点**：事件载荷字段；`Refund` 持有 `returnRequestId`/`paymentId`；Phase 1 **不做** object-backed link 深挖。
- **深读**：本体文 [§4.3](../theory/palantir-foundry-ontology-theory.md)、[§11.2.3](../theory/palantir-foundry-ontology-theory.md)；规格 §4.1

### B5. Interface（Ontology）

- **一句话定义**：描述 Object type 的形状与能力，支持多态（面向契约而非具体类型）。
- **为何需要**：多种「可外部执行」对象可共用同一套执行/重试语义。
- **NovaShop 落点**：理论侧 `ExternallyExecutable`；代码用 `Refund` + `ExternalExecution` **体现能力**，无独立 Ontology Interface 运行时。
- **代码/测试锚点**：`ExternalExecution` 状态机；`RetryExternalExecutionHandler`
- **深读**：本体文 [§4.6](../theory/palantir-foundry-ontology-theory.md)、[§11.2.4](../theory/palantir-foundry-ontology-theory.md)

### B6. Object set

- **一句话定义**：满足条件的 Object 集合；可复用的查询资产。
- **为何需要**：「待审退货队列」应是稳定语义，而非一次性 SQL。
- **NovaShop 落点**：待审列表 ↔ `ListPendingReturnsHandler` ↔ `GET /returns/pending`。
- **代码/测试锚点**：`ListPendingReturnsHandler`；`ReturnController` pending 端点；IT：`ReturnControllerIT`
- **深读**：本体文 [§4.1](../theory/palantir-foundry-ontology-theory.md)、[§11.2.5](../theory/palantir-foundry-ontology-theory.md)

### B7. Action

- **一句话定义**：按业务目标事务性变更一个或多个 Object（属性/链接）的定义与执行。
- **为何需要**：用「批准退货」而不是「改三张表」思考写路径。
- **NovaShop 落点**：`ApproveReturn`、`ExecuteRefund`、`RecordRefundResult`、`RetryExternalExecution` 等用例。
- **代码/测试锚点**：对应 `*Handler`；HTTP 见 [02](./02-ontology-to-ddd-map.md) §4。分布式下一次 Ontology Approve 被拆成多步——见 §3。
- **深读**：本体文 [§7.1](../theory/palantir-foundry-ontology-theory.md)、[§11.5.2–11.5.3](../theory/palantir-foundry-ontology-theory.md)

### B8. Submission criteria

- **一句话定义**：Action 提交前必须满足的准入条件（身份、状态、金额等）。
- **为何需要**：未授权/非法状态不应进入 edit-set。
- **NovaShop 落点**：角色头 + 领域锁住的金额/状态/行规则；**模拟** submission 身份，≠ 完整 Dynamic Security / IAM。
- **代码/测试锚点**：`X-Actor-Id` / `X-Actor-Role`；`ReturnRequest.approve`；`ReturnRequestTest`
- **深读**：本体文 [§7.1 Submission criteria](../theory/palantir-foundry-ontology-theory.md)；规格 §4.6

### B9. Function

- **一句话定义**：服务端纯计算/派生逻辑（可读 Object，可 backing Action）；≠ 任意应用 `@Service`。
- **为何需要**：复杂派生与 Action 编辑职责分离。
- **NovaShop 落点**：Phase 1 **几乎不用**独立 Function。
- **代码/测试锚点**：无对应模块；勿把 `ApproveReturnHandler` 叫 Function。
- **深读**：本体文 [§4.5](../theory/palantir-foundry-ontology-theory.md)、[§7.2–§7.3](../theory/palantir-foundry-ontology-theory.md)

### B10. Automation

- **一句话定义**：事件/条件触发后自动执行 Action（仍应过同一套 criteria）。
- **为何需要**：审批后自动开退款，避免人工点第二步。
- **NovaShop 落点**：`ReturnApprovedListener` → 再进入 `StartRefund…` / `ExecuteRefund…`；**禁止** listener 内直接改库规则或调 PSP。
- **代码/测试锚点**：`ReturnApprovedListener`；`ReturnApprovedListenerIsolationTest`
- **深读**：本体文 [§7.4](../theory/palantir-foundry-ontology-theory.md)、[§11.6](../theory/palantir-foundry-ontology-theory.md)；规格 §4.4 适配器约束

### B11. ExternalExecution / ExternallyExecutable

- **一句话定义**：外部副作用的一等执行记录（尝试次数、状态、错误、幂等键）；可执行对象实现 `ExternallyExecutable` 契约（理论）。
- **为何需要**：PSP 超时/重试/对账必须可查询、可恢复，不能只靠日志。
- **NovaShop 落点**：领域实体 `ExternalExecution` 挂在 `Refund` 下；状态含 `PENDING` → `IN_PROGRESS` → 终态。
- **代码/测试锚点**：
  - `ExternalExecution.java`、`ExternalExecutionStatus.java`
  - `ExecuteRefundHandler`、`RecordRefundResultHandler`、`RetryExternalExecutionHandler`
  - **勿**与 `OutboxEntity` 混淆
- **深读**：本体文 [§11.5.3](../theory/palantir-foundry-ontology-theory.md)、[§11.10](../theory/palantir-foundry-ontology-theory.md)；规格 §4.1

### B12. Decision lineage（本期：审计字段，非完整 DecisionRecord）

- **一句话定义**：决策可追溯链：谁、为何、输入、候选、模型版本、结果与事后 outcome。完整形态常落为 `DecisionRecord` 对象族。
- **为何需要**：审批/风控事后要能回答「为何批了这笔」。
- **NovaShop 落点**：**Phase 1 不做** `DecisionRecord`；审批审计落在 `ReturnRequest` 的 `actorId` / `actorRole` / `reason` / `resolutionCode` + `ReturnApproved` 载荷。
- **代码/测试锚点**：`ReturnRequest` 审批字段；事件 `ReturnApproved`；手册 [01](./01-concepts-bridge.md) §4 术语表
- **深读**：本体文 [§11.10](../theory/palantir-foundry-ontology-theory.md)；规格 §3.2 非目标、§4.1

### B13. Scenario（Phase 2+ / 未实现）

- **一句话定义**：What-if 沙箱：在隔离分支试演 Action/模型，确认后再 Apply。
- **为何需要**：促销预留、库存试算等不宜直接打生产 Ontology。
- **NovaShop 落点**：**未实现**；整店/促销路线见 [roadmap-full-shop.md](./roadmap-full-shop.md)。
- **代码/测试锚点**：无
- **深读**：本体文 [§7.5](../theory/palantir-foundry-ontology-theory.md)、[§11.7](../theory/palantir-foundry-ontology-theory.md)

---

## C. Coverage checklist

| Concept | Covered in Phase1–2 code? | Where | Deferred? |
|---------|---------------------------|-------|-----------|
| 统一语言 | 是 | 领域名/API/事件一致；`platform` 契约 | — |
| 限界上下文 | 是 | `commerce` + `fulfillment` + `order-return` + `payment-refund` | **Payment BC**（捕获权威） |
| 实体 | 是 | `Product`/`Sku`、`SalesOrder`、`ReturnRequest`、`ReturnLine`、`Refund`、`ExternalExecution` | — |
| 值对象 | 是 | `Money`、`ActorRole`、状态枚举 | — |
| 聚合 / 聚合根 | 是 | `SalesOrder`、`ReturnRequest`、`Refund` 为根 | — |
| 不变式 | 是 | `SalesOrder.place`、`ReturnRequest.approve`、`Refund` 状态机 + L1 tests | — |
| 领域事件 | 部分（集成事件） | `OrderPlaced`、`ReturnApproved`、`RefundTerminal` + Outbox | 进程内领域事件总线未单独建模 |
| 领域服务 | 几乎无 | 规则在聚合方法内 | 跨实体规则若变复杂再抽 |
| 应用服务 | 是 | `*Handler` | — |
| 仓储 | 是 | `*Repository` 端口 + JPA 适配 | — |
| 防腐层 (ACL) | 是 | `PspRefundClient` / Stub + 回调；demo-web→order-return **种子映射**（教学 ACL） | 真实 PSP；order-return HTTP ACL |
| 最终一致 | 是 | 下单→（履约待消费）→审批→创建 Refund→PSP 多步 | Ontology 级原子 edit-set |
| Outbox | 是 | commerce / order-return / payment-refund 均有 `OutboxPublisherJob` | — |
| Evans 四层 | 是 | 每服务 `interfaces/application/domain/infrastructure` | — |
| 端口倒置 | 是 | `application.port` ← infrastructure | — |
| 操作层 | 概念模拟 | 无真实 Ontology 引擎；`demo-web` 只是薄控制台 | 真实 Foundry/OSDK |
| Object type | 是（领域映射） | 聚合/实体类（含 Catalog/SalesOrder） | 完整企业对象图 |
| Property | 是 | 领域字段 + API DTO | Shared property 体系 |
| Link type | 部分 | 跨 BC ID + 聚合内行 | object-backed link |
| Interface | 能力体现 | `ExternalExecution` 语义 | 具名 `ExternallyExecutable` 运行时 |
| Object set | 是 | `ListProductsHandler`；`ListPendingReturnsHandler` / pending API | 更多可复用集合 |
| Action | 是 | 各 `*Handler` 用例（含 PlaceOrder） | 跨对象原子 edit-set |
| submission criteria | 是（简化） | 角色头 + 领域准入 | Dynamic Security / IAM |
| Function | 否 | — | 非目标 |
| Automation | 是（近似） | Rabbit listener → 应用用例（含 `OrderPlaced` → 库存预留） | 完整 Automation 身份/治理 |
| ExternalExecution | 是 | 领域实体 + handlers | — |
| 库存/履约聚合 | 是（Phase 3） | `InventoryPosition`、`FulfillmentOrder` | 复杂 WMS/改仓 |
| Decision lineage | 部分 | 审计字段 + 事件载荷 | 完整 `DecisionRecord` |
| Scenario | 否 | — | Phase 3+ / 促销沙箱 |
---

## 下一步

1. [01-concepts-bridge.md](./01-concepts-bridge.md) — 操作层 vs 本标本  
2. [02-ontology-to-ddd-map.md](./02-ontology-to-ddd-map.md) — edit-set 分布式翻译（必读）  
3. [03-layered-code-tour.md](./03-layered-code-tour.md) — 按层点名类  
