# 附录 · 整店路线图（Phase 2–3）

Phase 1 交付售后切片 + 手册核心叙事；**Phase 2 / 3 已落地**（见下方勾选）。本附录记录演进边界，避免文档 / 测试退化。

---

## 1. 终局演示路径

```text
看商品 → 下单 →（可选履约感知）→ 申请售后 → 审批 → 退款终态
```

前端可点可跑；API / OpenAPI / smoke / 金字塔仍在。

---

## 2. Phase 2（支撑上下文 + 极简控制台）

| 产物 | 说明 | 状态 |
|------|------|------|
| 订单只读 / 简单目录 | `commerce` 模块：`catalog` + `salesorder` 包级 BC；端口 8083 | [x] |
| 简单支付权威或适配 | 下单时 mint `paymentId`，`refundableTotal = total` | [x] |
| **极简 Web 控制台** | `demo-web` Thymeleaf（8080）：目录 / 下单 / 待审 / 退款 | [x] |
| 演示 smoke | `scripts/smoke-phase2.sh`（含中间态） | [x] |
| 可选薄「操作层投影」 | UI 上对照 Foundry「看见对象再行动」的体验，仍非真实 Ontology 引擎 | [ ] |
| `DecisionRecord`（可选试点） | 风控 / 审批审计对象族，见本体 §11.10；与 Phase 1 退货字段并存时要划清边界 | [ ] |

验收侧重：演示路径前半段可跑；Phase 1 售后闭环回归不破。运行说明见 [07-phase2-demo-path.md](./07-phase2-demo-path.md)。

---

## 3. Phase 3（履约 / 库存 + 更完整店面）

| 产物 | 说明 | 状态 |
|------|------|------|
| 履约 / 库存上下文 | `fulfillment` (:8084)：`InventoryPosition` + `FulfillmentOrder`；消费 `OrderPlaced` | [x] |
| 更完整演示店面 | demo-web 订单页显示履约/库存；客服路径含 PSP simulate | [x] |
| 演示 smoke | `scripts/smoke-phase3.sh` | [x] |
| 手册 | [08-phase3-full-demo.md](./08-phase3-full-demo.md) | [x] |
| 观测性增强 | 日志足够讲故事；无重型平台 | [x] 轻量 |

仍不做：完整 IAM 全栈、Kafka 替换（除非教学需要）、服务网格、真实 Payment BC。

---

## 4. 「五脏俱全」验收（规格 §8.3）

- [x] 演示路径可运行（含前端）— Phase 3：catalog → order → fulfillment → return → refund（见 08）  
- [x] 学员可按手册复现 — [08-phase3-full-demo.md](./08-phase3-full-demo.md)  
- [x] 测试金字塔不退化（L1 关键规则仍在；smoke 仍含退款中间态或等价可观察点）  
- [x] 映射表 / 操作层免责声明仍有效；catalog/salesorder/fulfillment 已补进手册；Payment BC 仍 deferred  

---

## 5. 演进时勿破坏的 Phase 1 资产

| 资产 | 原因 |
|------|------|
| edit-set 翻译叙事 | 防止把分布式近似说成 Ontology 原子提交 |
| `ExternalExecution` 一等实体 | 防止退款「状态枚举化」 |
| Listener → 应用用例纪律 | 防止集成层变上帝 |
| `refundId` 预分配 | PSP 幂等与跨服务关联 |
| 种子 → 权威替换策略 | 快照退场时 L1 不变式仍可测 |

---

## 6. 建议阅读顺序（整店阶段）

1. 本手册 [00](./00-concept-playbook.md) → 01–06（Phase 1 售后打底）  
2. [07-phase2-demo-path.md](./07-phase2-demo-path.md) → [08-phase3-full-demo.md](./08-phase3-full-demo.md)（可点可跑）  
3. 本体 §11 全链路对象（目录、订单、库存）  
4. 范式文中「组合使用」章节  
5. 再开新规格前先核对本表未勾选项，避免范围膨胀拖垮教学切片  

