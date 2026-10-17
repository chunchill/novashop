# 09 · 学员 2 小时实验单

目标：不依赖讲师口述，独立走通「跑起来 → 看懂概念 → 改一条规则 → 用测试锁住」。

**前置：** Docker Desktop、JDK 21、`curl`/`jq`；已读 [00-concept-playbook.md](./00-concept-playbook.md) 前半（DDD A1–A6 + Ontology B1–B3 即可）。

---

## 0–20 分钟 · 起栈与冒烟

```bash
# 在案例根目录执行（含 docker-compose.yml；独立发布后即为仓库根）
POSTGRES_HOST_PORT=5433 docker compose up --build -d
docker compose ps   # 全部 healthy
./scripts/smoke-phase3.sh   # 末行 SMOKE OK
```

失败排障 → [05-runbook-docker.md](./05-runbook-docker.md)。

**打卡：** 写出本机访问的店面 URL：`http://localhost:____/`

---

## 20–45 分钟 · 浏览器走通路径

打开 http://localhost:8080/

| # | 操作 | 你应看到 |
|---|------|----------|
| 1 | Catalog 选 SKU → Place order | 订单创建；有 `paymentId`（`pay-…`） |
| 2 | 订单详情 | 履约/库存信息（fulfillment 已消费 `OrderPlaced`） |
| 3 | Agent → 从订单申请退货 | 待审退货出现 |
| 4 | 用 SUPERVISOR（或超额规则下的角色）审批 | 进入退款流程 |
| 5 | 打开退款页 | 状态可为 `PENDING_EXTERNAL_EXECUTION`；有 ExternalExecution |
| 6 | 点 **Simulate PSP callback**（demo） | 终态 `SUCCEEDED`；退货侧投影更新 |

**打卡：** 抄下一个 `refundId`：`________________`

**思考题（写一句）：** 为什么审批成功 ≠ 钱已退？  
提示：[02-ontology-to-ddd-map.md](./02-ontology-to-ddd-map.md) §3。

---

## 45–75 分钟 · 概念 → 代码锚点

打开 [00-concept-playbook.md](./00-concept-playbook.md)，至少完成：

| 概念 | 打开的文件（路径抄下来） |
|------|--------------------------|
| 聚合 / 聚合根 | |
| Action ≈ 用例 + 不变式 | |
| ExternalExecution | |
| Outbox ≠ ExternalExecution | |

确认你**不会**把 Spring 服务叫做「Ontology」。定位见 [01-concepts-bridge.md](./01-concepts-bridge.md) §1。

---

## 75–110 分钟 · 改规则 + 测试（TDD 小练习）

在 `order-return` 领域测里加一条规格（任选其一，或自拟更严规则）：

**练习 A（推荐）：** AGENT 上限从可配置的 500 改成你本地测试里写死的更低值（例如 300），先改测试再改领域/配置，使：

```bash
./gradlew :order-return:unitTest --tests '*.ReturnRequestTest'
```

红 → 绿。

**练习 B：** 给 `resolutionCode` 增加非空校验（若尚未有）：先写 unit 红测，再实现。

记住测试包在 `src/test/.../unit/...`（金字塔），生产代码在 `src/main/.../domain/...`（DDD）。见 [04-test-pyramid-and-tdd.md](./04-test-pyramid-and-tdd.md)。

**打卡：** 粘贴你新增的测试方法名：`________________`

---

## 110–120 分钟 · 复盘三句话

用自己的话写：

1. Ontology edit-set 与本标本两服务拆分差在哪？  
2. `demo-web` 创建退货时，订单数据从哪来？是不是 order-return 直接调 commerce？（诚实答案见下一步手册「种子过渡」）  
3. `paymentId` 是谁 mint 的？算不算完整 Payment BC？

对照 [06-retro-case-report.md](./06-retro-case-report.md)。

---

## 交卷清单（自学勾选）

- [ ] smoke-phase3 通过  
- [ ] UI 走到 PSP 终态  
- [ ] 打开过至少 4 个概念锚点类  
- [ ] 至少一条 unit 测试按 TDD 改过并变绿  
- [ ] 能口头解释「两一致性边界」
