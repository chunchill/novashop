# e2e（测试金字塔顶层）

`src/main` 体现 DDD；`src/test` 体现 unit / integration；**本目录 + `scripts/` 体现 e2e**。

在**案例根目录**运行（需 Compose 已起）：

```bash
# Phase 1 — 售后闭环（手工种子 CreateReturn）
./scripts/smoke-return-refund.sh

# Phase 2 — catalog → order → return → approve → PENDING → callback → SUCCEEDED
./scripts/smoke-phase2.sh

# Phase 3 — + fulfillment ALLOCATED / inventory reserved
./scripts/smoke-phase3.sh
```

脚本都会断言：审批后出现 `PENDING_EXTERNAL_EXECUTION`，再回调至终态，并检查退货侧投影。`smoke-phase2`/`phase3` 走 commerce 真实下单；`phase3` 额外断言履约与库存预留。

说明见 [docs/handbook/04-test-pyramid-and-tdd.md](../docs/handbook/04-test-pyramid-and-tdd.md)。
