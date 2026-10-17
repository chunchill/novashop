# ADR 003 · demo-web 退货种子

**状态：** Accepted（教学过渡）  
**日期：** 2026-10

## 背景

创建退货需要订单行、可退金额、`paymentId` 等快照。正式做法是 order-return 经 ACL 调 SalesOrder / Payment。Phase 2–3 优先打通整店演示，避免先做完整跨 BC HTTP 防腐。

## 决策

`demo-web` 读 `commerce` 订单 JSON，经 **`ReturnSeedMapper`** 映射后 `POST /returns` 写入种子字段。  
`order-return` **不**在运行时依赖 commerce。

## 后果

- **正向：** commerce 挂了仍可演示已种子的售后路径；边界诚实、好讲  
- **代价：** 易被误认为「正式 ACL」——手册 §4 与代码注释必须标明过渡性质  
- **演进：** 后续可将种子逻辑下沉为 order-return 内嵌 ACL  
