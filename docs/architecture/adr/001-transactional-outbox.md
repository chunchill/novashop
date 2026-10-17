# ADR 001 · Transactional Outbox

**状态：** Accepted  
**日期：** 2026-10

## 背景

Ontology 中一次 Action 的 edit-set 可跨多对象原子提交。微服务拆开后，`ReturnRequest` 与 `Refund` 不在同一库，无法用单库事务表达「批准 + 创建退款」。

## 决策

各权威服务使用 **Transactional Outbox**：

1. 业务聚合与 outbox 行同库提交  
2. 后台 job 发布到 RabbitMQ  
3. 下游消费后更新自己的聚合  

`refundId` 在审批侧预分配，保证退款侧可用同一幂等键。

## 后果

- **正向：** 教学上能讲清「两处一致性边界」；本地易复现；无 2PC  
- **代价：** 最终一致；学员必须接受中间态与投影延迟  
- **不做：** Kafka 事务、Saga 编排引擎、共享库强一致  
