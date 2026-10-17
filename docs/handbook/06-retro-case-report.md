# 06 · 案例复盘报告（Phase 1–3）

**日期：** 2026-10-17（Phase 3 后修订）  
**标本：** 本仓库根（`cases/novashop/` 若在 DDD Arch 单仓内）  
**规格：** [设计规格](../design/case-design.md)

> 教学核心仍是**售后两一致性边界**；Phase 2–3 把「如何进到售后」补成可演示整店路径。下面先复盘核心切片，再补整店叙事。

---

## 1. 业务与决策问题

| 决策 | 风险若做错 |
|------|------------|
| 是否批准退货 | 错批 → 资损 / 客诉 |
| 退款是否真正打出 | 显示成功但 PSP 失败 → 对账灾难 |

成功标准：人能按业务概念操作；变更过同一套准入；Ontology/领域 edits 与外部副作用边界清晰；能回答谁在何时基于什么做了决定。

Phase 1 只切退货→退款闭环。Phase 2–3 加上：看商品 → 下单 → 库存预留感知 → 再进售后。

---

## 2. 本体 → 标本：摘要

理论（[本体文 §11](../theory/palantir-foundry-ontology-theory.md)）里：

- Object：`ReturnRequest`、`Refund`、`ExternalExecution`、`SalesOrder`、`Sku`、`InventoryPosition`…  
- Action：`ApproveReturn`（edit-set 含创建 PENDING Refund）、`ExecuteRefund`、`RecordRefundResult`  
- submission criteria：角色 / 金额 / 状态  
- Automation：审批后触发后续执行  

本仓库**不**实现 Ontology 引擎；在各 BC **权威服务内**用 DDD 模拟动能语义。定位 → [01](./01-concepts-bridge.md) §1；概念卡 → [00](./00-concept-playbook.md)。

---

## 3. 限界上下文地图（终局）

```text
demo-web (:8080)  ─BFF/演示─►  commerce (:8083)     catalog + salesorder
                              fulfillment (:8084)  库存预留 ← OrderPlaced
                              order-return (:8081) 退货审批（退货种子来自 demo-web）
                              payment-refund (:8082) Refund + ExternalExecution + PSP
```

| BC / 模块 | 权威什么 | 刻意不权威什么 |
|-----------|----------|----------------|
| commerce | 商品、销售订单；mint `paymentId` | 真实支付捕获、退货规则 |
| fulfillment | 库存预留、简易履约单 | 复杂 WMS/改仓 |
| order-return | 退货审批、`refundId` 预分配 | 订单真相（靠种子快照） |
| payment-refund | 退款生命周期、PSP 尝试 | 退货审批 |

**种子过渡（必读）：** demo-web 读 commerce 订单 JSON，再 POST 到 order-return 创建退货——这是**教学用种子/ACL 前身**，不是 order-return 内嵌的正式跨服务防腐层。详见 [02](./02-ontology-to-ddd-map.md)「种子与权威」一节。

---

## 4. edit-set 翻译（教学心脏）

Ontology 一次 `ApproveReturn` 原子 edit-set（改 Return + **创建** PENDING Refund）在标本中拆成：

| 步 | 服务 | 本地一致 | ≠ PSP 成功 |
|----|------|----------|------------|
| 1 | order-return | 审批 + outbox `ReturnApproved`（含 `refundId`） | ✓ |
| 2 | payment-refund | 创建 `Refund(PENDING_…)` + ExternalExecution 意图 | ✓ |
| 3 | payment-refund | `ExecuteRefund` → PSP | 进行中 |
| 4 | payment-refund | `RecordRefundResult` | 仅此可称资金终态 |

完整表 → [02](./02-ontology-to-ddd-map.md) §3。

---

## 5. 分层、不变式、测试

- 每服务：`interfaces` / `application` / `domain` / `infrastructure`；领域无 Spring。  
- `src/main` = DDD；`src/test` = `unit/` + `integration/`；`scripts/` = e2e。  
- 关键 L1：角色金额、重复行、qty、Refund 不得未经 recordResult 变 SUCCEEDED、ExternalExecution retry。  

证据 → [04](./04-test-pyramid-and-tdd.md)；动手 → [09-lab-2h.md](./09-lab-2h.md)。

---

## 6. Outbox vs ExternalExecution

| | Outbox | ExternalExecution |
|--|--------|-------------------|
| 管 | 可靠投递领域意图事件 | 外部系统尝试语义 |
| Phase 1–2 例 | `ReturnApproved`、`OrderPlaced` | PSP 尝试次数/错误/幂等键 |

监听器只进应用用例（隔离测试锁住）。

---

## 7. 运行证据（整店）

| 证据 | 命令 / 入口 |
|------|-------------|
| 单测+IT | `./gradlew test` |
| 全路径 smoke | `./scripts/smoke-phase3.sh` |
| 售后中间态 | smoke 或 UI 退款页 `PENDING_EXTERNAL_EXECUTION` |
| UI | http://localhost:8080/ |

Runbook → [05](./05-runbook-docker.md)；Phase 3 → [08](./08-phase3-full-demo.md)。

---

## 8. 仍不做（诚实边界）

- 生产级 SPA / 完整 IAM / Foundry·OSDK / Kafka / 服务网格  
- **独立 Payment BC**（捕获、对账权威）——当前 `pay-` UUID 由 `SalesOrder.place` mint  
- order-return **正式** HTTP ACL 拉订单（仍用 demo-web 种子）  
- `DecisionRecord` 全族、促销 Scenario、完整风控  

路线图 → [roadmap-full-shop.md](./roadmap-full-shop.md)。

---

## 9. 教学收获（给学员带走的五句）

1. 操作层 ≠ 你仓库里的微服务。  
2. edit-set 翻译必讲，否则 Outbox 会被误当成 Ontology 原子提交。  
3. 中间态是一等公民：`PENDING_EXTERNAL_EXECUTION` + `ExternalExecution`。  
4. Adapter 只调应用服务；PSP 停在 ACL。  
5. 测试包跟金字塔；生产包跟 DDD——别混。
