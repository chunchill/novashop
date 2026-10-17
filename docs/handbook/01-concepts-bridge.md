# 01 · 概念桥：操作层 vs 本标本

读完本章，你应能回答：本仓库在模拟什么、**不是**什么、以及该去哪份理论文深读。

术语卡（统一语言、聚合、Action、ExternalExecution 等）见 [00-concept-playbook.md](./00-concept-playbook.md)。

---

## 1. 一句话定位（规格 §4.0）

本仓库在 **权威微服务内模拟** Foundry Ontology 的动能概念（Action、submission criteria、ExternalExecution 等），便于教学与 Docker 复现。

| 生产中的 Foundry Ontology | 本教学标本 |
|---------------------------|------------|
| 坐落在多系统之上的**操作层** | **不**实现真实 Ontology 引擎 |
| 投影企业对象并受治理写回 | 各 BC **自己就是写权威** |
| Action edit-set 可跨多 Object 原子提交 | 跨服务用 Outbox **近似**；见 [02-ontology-to-ddd-map.md](./02-ontology-to-ddd-map.md) |

**勿**把 Spring 服务当成 Ontology，也**勿**把学术分类本体论当成 Foundry。权威叙述见 [modeling-paradigms-relationship.md](../theory/modeling-paradigms-relationship.md)。

---

## 2. 五者各管哪一层（精要）

| 方法 | 一句话 | 本标本里怎么用 |
|------|--------|----------------|
| Ontology（Foundry） | 跨系统「看见同一世界」并受治理行动 | 概念来源与映射目标；**无**引擎 |
| DDD | 统一语言 + 限界上下文 + 聚合护不变式 | 多 BC、聚合、用例命名 |
| 微服务 | 独立部署 / 故障域 | `commerce` + `fulfillment` + `order-return` + `payment-refund`（+ `demo-web`） |
| OO | 行为与数据同处 | 富领域模型（非贫血 CRUD） |
| E-R / 持久化 | 结构清楚的事实存储 | JPA 表在 infrastructure |

详细论证 → [modeling-paradigms-relationship.md](../theory/modeling-paradigms-relationship.md) §1–§4。

---

## 3. 业务切片（Phase 1–3 诚实边界）

**Phase 1 售后闭环**（仍是教学核心）：

```text
ApproveReturn → ReturnApproved → 创建 Refund(PENDING_…)
  → ExecuteRefund → ExternalExecution 尝试
  → RecordRefundResult → RefundSucceeded|Failed
  →（可选）退货侧投影
```

**Phase 2**（目录 + 销售订单 + demo-web）：

```text
ListProducts / PlaceOrder → SalesOrder 持久化 + outbox OrderPlaced（Rabbit: novashop.order.placed）
  → demo-web 读订单 JSON，把 paymentId / refundableTotal / lines 种子到 order-return
```

**Phase 3**（履约 / 库存）：

```text
OrderPlaced → fulfillment ReserveStock → FulfillmentOrder(ALLOCATED|…) + InventoryPosition 预留
  → 订单页可看见履约状态与库存；售后路径同 Phase 1–2
```

对应服务：

| 限界上下文 | 服务 | 核心聚合 / 要点 |
|------------|------|-----------------|
| Catalog + SalesOrder | `commerce` (:8083) | `Product`/`Sku`；`SalesOrder`（下单时 **mint `paymentId`**，`refundableTotal = total`） |
| Fulfillment / Inventory | `fulfillment` (:8084) | `InventoryPosition`；`FulfillmentOrder`（消费 `OrderPlaced`） |
| Return Management | `order-return` | `ReturnRequest`；创建退货仍靠 demo-web **种子字段**（非跨 BC HTTP ACL） |
| Payment / Refund | `payment-refund` | `Refund` + `ExternalExecution` |

诚实边界：

- **Payment BC 权威服务仍 deferred**：没有独立支付捕获服务；`paymentId` 由 commerce 在下单时铸造，仅作退款关联键（**mint ≠ Payment BC**）。
- **退货路径仍经 demo-web 种子**：order-return 不依赖 commerce 运行时；跨服务 HTTP ACL 未做。对照表 → [02 §4 种子与权威](./02-ontology-to-ddd-map.md)。
- **Spring 服务 ≠ Ontology**：无真实操作层引擎；见上文 §1。
- Phase 1 手工种子脚本（`smoke-return-refund.sh`）仍可用；Phase 2/3 用 `smoke-phase2.sh` / `smoke-phase3.sh`。

---

## 4. 易混术语（脚注）

| 词 | 本标本含义 | 常见误读 |
|----|------------|----------|
| Action | 应用用例 + 领域不变式（如 `ApproveReturnHandler`） | 任意 `@Service` |
| submission criteria | 角色/金额/状态等准入（领域锁住） | 完整 Dynamic Security / IAM |
| Automation | 消费者**再进入应用用例**（如 `ReturnApprovedListener` → handlers） | 监听器里直接改库/调 PSP |
| ExternalExecution | 领域一等实体（尝试次数、状态、错误） | Outbox 表行 |
| Function | 纯计算（本期极少） | 任意 Spring bean |
| DecisionRecord | Phase 1 **不做**；审批审计落在 `ReturnRequest` 字段 + 事件载荷 | 把 ApproveReturn 叫 DecisionRecord |

展开定义与代码锚点 → [00-concept-playbook.md](./00-concept-playbook.md) §B。Ontology 动力要素深读 → [palantir-foundry-ontology-theory.md](../theory/palantir-foundry-ontology-theory.md) §7、§11.5。

---

## 5. Evans 四层（每服务内）

```text
interfaces/api     → User Interface
application        → Application（用例、事务、outbox 写入）
domain             → Domain（聚合、不变式、领域事件意图）
infrastructure     → Infrastructure（JPA、Rabbit、假 PSP、时钟）
```

实用分层 + 端口倒置。卡片 → [00](./00-concept-playbook.md) A15–A16。与 Evans 2003 原文差异、谱系对照 → [ddd-application-architectures.md](../theory/ddd-application-architectures.md) §2、§7。

代码导读 → [03-layered-code-tour.md](./03-layered-code-tour.md)。

---

## 6. 深读清单（按需）

1. [00-concept-playbook.md](./00-concept-playbook.md) — 概念卡 + Phase1 coverage  
2. [modeling-paradigms-relationship.md](../theory/modeling-paradigms-relationship.md) — 先建立边界直觉  
3. [palantir-foundry-ontology-theory.md](../theory/palantir-foundry-ontology-theory.md) §11 — NovaShop 本体演练（尤其 `ApproveReturn` edit-set §11.5.2）  
4. [ddd-application-architectures.md](../theory/ddd-application-architectures.md) §2 — Evans 分层  
5. 回到本手册 [02-ontology-to-ddd-map.md](./02-ontology-to-ddd-map.md) — 看分布式翻译
