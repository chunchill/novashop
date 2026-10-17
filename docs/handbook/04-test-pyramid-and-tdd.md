# 04 · 测试金字塔与 TDD

> 一句话（尽早记住）：**`src/main` = DDD 分层；`src/test` = `unit/` → `integration/`；`scripts/smoke-*.sh` = e2e。**  
> 改业务规则：先在 `unit/domain` 补失败用例，再改聚合。

本案例刻意拆开两类关注点：

| 树 | 组织逻辑 | 学员学什么 |
|----|----------|------------|
| **`src/main`** | **DDD**：`domain` / `application` / `infrastructure` / `interfaces` | 限界上下文内如何分层、护不变式 |
| **`src/test`** | **测试金字塔**：`unit` / `integration` | 测什么、测多深、怎么跑 |
| **`e2e/` + `scripts/`** | **e2e**（Compose 全栈） | 两一致性边界可观察 |

> 测试包名会带 `unit` / `integration` 段（与 main 包不完全镜像）。这是教学取舍：金字塔优先可见；DDD 理念留在 main。

工具：JUnit 5 + AssertJ；应用层 Fake 优先；持久化 IT 当前用 H2 slice。

---

## 1. 目录形状

```text
<service>/
  src/main/java/.../domain|application|infrastructure|interfaces/   ← DDD
  src/test/java/.../
    unit/
      domain/...
      application/...
      infrastructure/...          ← 纯逻辑 / Fake / mock，无 Spring 容器
    integration/
      infrastructure/persistence/...IT
      interfaces/api/...IT        ← Spring 切片 / JPA
e2e/README.md
scripts/smoke-return-refund.sh   # Phase 1 售后闭环
scripts/smoke-phase2.sh          # Phase 2：catalog → order → return → refund
scripts/smoke-phase3.sh          # Phase 3：+ fulfillment / inventory
```

```text
        /\
       /e2e\         e2e/ + scripts/smoke-*.sh
      /------\
     /integ.  \      src/test/.../integration/**  *IT
    /----------\
   /   unit     \    src/test/.../unit/**  *Test
  /--------------\
```

| 层 | 包名片段 | 类名 | Spring？ |
|----|----------|------|----------|
| unit | `…unit.domain…` / `…unit.application…` / `…unit.infrastructure…` | `*Test` | 否 |
| integration | `…integration…` | `*IT` | 是（切片） |
| e2e | （脚本） | smoke | 真实进程 |

带 Fake 的应用测试仍属 **unit**。

---

## 2. 用例索引

### unit

| 模块 | 包 / 类 | 要点 |
|------|---------|------|
| order-return | `…unit.domain.returnrequest.ReturnRequestTest` | 角色/金额/状态机/重复行/qty/`refundId` |
| order-return | `…unit.application.ApproveReturnHandlerTest` | outbox 与 `refundId` |
| order-return | `…unit.application.ApplyRefundTerminalHandlerTest` | 终态投影 |
| order-return | `…unit.infrastructure.*` | Listener / OutboxJob（mock） |
| payment-refund | `…unit.domain.refund.RefundTest` | PENDING、幂等、retry |
| payment-refund | `…unit.application.RefundApplicationHandlersTest` | Start 不调 PSP；Execute/Record/Retry |
| payment-refund | `…unit.infrastructure.*` | Listener、PSP stub、OutboxJob |
| platform | `…unit.events.EventContractTest` | 事件契约 |
| commerce | `…unit.salesorder.domain…` / `CreateOrderHandlerTest` / `OutboxPublisherJobTest` | 下单 + OrderPlaced outbox |
| fulfillment | `…unit.domain.inventory.InventoryPositionTest` / `ReserveStockFromOrderPlacedHandlerTest` | 预留 / 不足拒绝 / 幂等 |
| demo-web | `…unit.web.ReturnSeedMapperTest` | 订单 → 退货种子映射 |
### integration

| 模块 | 类 | 要点 |
|------|-----|------|
| order-return | `JpaReturnRequestRepositoryIT` | 持久化往返 |
| order-return | `ReturnControllerIT` | 缺角色头 → 400 |
| payment-refund | `JpaRefundRepositoryIT` | Refund + ExternalExecution |
| payment-refund | `RefundControllerIT` | GET execution；回调 |
| commerce | `OrderControllerIT` | 创建/查询订单 HTTP |
| commerce | `JpaSalesOrderRepositoryIT` | SalesOrder + outbox 行 |

### e2e

- `scripts/smoke-return-refund.sh`（Phase 1）：审批 → **PENDING_EXTERNAL_EXECUTION** → 回调 → 终态 → 投影。
- `scripts/smoke-phase2.sh`（Phase 2）：catalog → create order → return → approve → PENDING → callback → SUCCEEDED。
- `scripts/smoke-phase3.sh`（Phase 3）：同上 + assert fulfillment ALLOCATED / inventory reserved（见 `e2e/README.md`）。

---

## 3. 命令

```bash
# 在案例根目录执行（含 docker-compose.yml；独立发布后即为仓库根）

./gradlew test                 # unit + integration（src/test 全量）
./gradlew unitTest             # 仅 *.unit.*
./gradlew integrationTest      # 仅 *.integration.*

./gradlew :order-return:unitTest --tests '*.ReturnRequestTest'

# e2e（Compose 已起）
./scripts/smoke-return-refund.sh
./scripts/smoke-phase2.sh
./scripts/smoke-phase3.sh
```

---

## 4. TDD 节奏

1. 红/绿先打在 **unit/domain**  
2. 再补 **unit/application**（Fake 端口）  
3. 稳定后补 **integration**  
4. 最后 **e2e** smoke  

禁止先堆 Controller 再补领域测。

---

## 5. 与规格 §7

| 规格 | 落点 |
|------|------|
| 角色/金额/重复行 | unit · `ReturnRequestTest` |
| ExternalExecution / retry | unit · `RefundTest` |
| Approve + outbox | unit · `ApproveReturnHandlerTest` |
| Start 先于 PSP | unit · `RefundApplicationHandlersTest` |
| 持久化 / HTTP | integration · `*IT` |
| Smoke 中间态 | e2e |
