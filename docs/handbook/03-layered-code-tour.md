# 03 · 分层代码导读

按 Evans 四层（实用版 + 端口倒置）走读本仓库根。依赖方向：**interfaces / infrastructure → application → domain**；domain **零** Spring / JPA。

架构深读 → [ddd-application-architectures.md](../theory/ddd-application-architectures.md) §2。

---

## 1. 模块布局

```text

  platform/          # 事件契约 only
  order-return/      # Return Management
  payment-refund/    # Payment / Refund
```

包根：

- `com.novashop.orderreturn.{domain,application,infrastructure,interfaces}`
- `com.novashop.paymentrefund.{domain,application,infrastructure,interfaces}`
- `com.novashop.platform.events`

---

## 2. Domain（领域）

### 2.1 order-return

| 类型 | 类 | 职责 |
|------|-----|------|
| 聚合 | `ReturnRequest` | 待审 → 审批；铸 `refundId`；投影终态 |
| 实体/行 | `ReturnLine` | 行可退数量 |
| 命令 | `ApproveCommand` | 批准行、金额、角色、AGENT 限额 |
| 结果 | `ApprovalDecision` | 新状态 + `refundId` |
| 值对象 | `Money`、`ActorRole`、`ReturnStatus` | 金额 scale 2 / 角色 / 状态机 |
| 异常 | `ReturnRequestException` | 业务拒绝 |

关键行为：`ReturnRequest.approve(ApproveCommand)`、`applyRefundTerminal(status)`。

### 2.2 payment-refund

| 类型 | 类 | 职责 |
|------|-----|------|
| 聚合 | `Refund` | `createPending` → 执行尝试 → `recordResult` / `retry` |
| 实体 | `ExternalExecution` | `attemptCount`、status、`lastError`、幂等键 |
| 枚举 | `RefundStatus`、`ExternalExecutionStatus` | 含 `PENDING_EXTERNAL_EXECUTION` |
| 异常 | `RefundException` | 非法状态转换 |

不变式要点：

- `createPending` 即 `PENDING_EXTERNAL_EXECUTION`，**先于** PSP 成功
- `SUCCEEDED` **只能**经 `recordResult(true, …)`，不能经 `createPending` / `recordExecutionAttempt` 直接到达

---

## 3. Application（应用）

### 3.1 order-return

| Handler | 作用 |
|---------|------|
| `CreateReturnHandler` | 创建待审退货 |
| `ListPendingReturnsHandler` | Object set：待审列表 |
| `ApproveReturnHandler` | 审批 + `OutboxPort.append(ReturnApproved)` |
| `GetReturnHandler` | 详情（含 `refundStatus` 投影） |
| `ApplyRefundTerminalHandler` | 消费终态事件，写投影 |

端口：`ReturnRequestRepository`、`OutboxPort`。

### 3.2 payment-refund

| Handler | 作用 |
|---------|------|
| `StartRefundFromReturnApprovedHandler` | 用事件中的 `refundId` 创建 PENDING Refund（**不**调 PSP） |
| `ExecuteRefundHandler` | 经 `PspRefundClient` 发起；更新执行尝试 |
| `RecordRefundResultHandler` | 写终态 + `RefundTerminalOutboxPort` |
| `RetryExternalExecutionHandler` | 失败后受控重试 |

端口：`RefundRepository`、`PspRefundClient`、`RefundTerminalOutboxPort`。

类注释明示：`StartRefundFromReturnApprovedHandler` 是 Ontology ApproveReturn **edit-set 中「创建 Refund」**的分布式近似。

---

## 4. Infrastructure（基础设施）

### 4.1 持久化

| 服务 | 适配器 / 实体 |
|------|----------------|
| order-return | `JpaReturnRequestRepository`、`ReturnRequestEntity`、`ReturnLineEntity` |
| payment-refund | `JpaRefundRepository`、`RefundEntity`、`ExternalExecutionEntity` |

### 4.2 Outbox

| 服务 | 写入 | 发布 |
|------|------|------|
| order-return | `JpaOutboxAdapter` implements `OutboxPort` | `OutboxPublisherJob` → exchange `novashop.return.approved` |
| payment-refund | `JpaRefundTerminalOutboxAdapter` | `OutboxPublisherJob` → exchange `novashop.refund.terminal` |

### 4.3 消息消费者（只调应用）

| Listener | 注入 | 调用 |
|----------|------|------|
| `ReturnApprovedListener` | `StartRefundService`、`ExecuteRefundService` | `start` 再 `execute` |
| `RefundTerminalListener` | `ApplyRefundTerminalService` | 投影退货 |

### 4.4 假 PSP

`StubPspRefundClient` + `PspMode`：

| 模式 | 行为 |
|------|------|
| `SUCCESS_SYNC` | 同步成功（默认本地配置） |
| `FAIL` | 同步失败 |
| `CALLBACK_LATER` | 返回 pending；需 `POST /psp/callbacks/refunds`（Compose 教学推荐） |

配置：`novashop.psp.mode` / 环境变量 `PSP_MODE`。

---

## 5. Interfaces（用户接口）

| Controller | 路径 |
|------------|------|
| `ReturnController` | `POST/GET /returns…`、`POST /returns/{id}/approve` |
| `RefundController` | `GET /refunds/{id}`、`POST /refunds/{id}/retry` |
| `PspCallbackController` | `POST /psp/callbacks/refunds` |

头解析：`ActorHeaders`（`X-Actor-Id` / `X-Actor-Role`）。缺头 → `MissingActorHeaderException` → 400（见 `ReturnControllerWebMvcTest`）。

Spring 装配：各服务 `ApplicationHandlersConfig` 把无注解的 Handler 包成带 `@Transactional` 的 Service facade，避免 domain/application 沾 Spring。

---

## 6. 工程约束：好 / 坏对照

| 原则 | 好 | 坏 |
|------|----|----|
| 意图优先 | `ApproveReturnHandler` + `ReturnRequest.approve` | `ReturnManager.doApprove()` 塞满 if |
| 依赖方向 | domain 无 `import org.springframework` | 聚合上 `@Entity` / `@Autowired` |
| 命名即语言 | `ExternalExecution`、`RecordRefundResult` | `RefundUtil`、`Helper2` |
| 监听器薄 | Listener 只反序列化 + 调 Handler | Listener 里调 PSP、改规则 |
| Outbox ≠ 外部执行 | `OutboxEntity` 管可靠投递；`ExternalExecution` 管 PSP 尝试 | 用 outbox 行当「退款执行状态」 |
| 错误属领域 | `ReturnRequestException` / `RefundException` | 一律 `RuntimeException("error")` |

---

## 7. 建议走读顺序（30 分钟）

1. `ReturnRequest.approve` → `ApproveReturnHandler` → `ReturnApproved` record  
2. `ReturnApprovedListener` → `StartRefundFromReturnApprovedHandler` → `Refund.createPending`  
3. `ExecuteRefundHandler` → `StubPspRefundClient` → `RecordRefundResultHandler`  
4. `RefundTerminalListener` → `ApplyRefundTerminalHandler` → `GET /returns/{id}` 投影字段  

映射表回顾 → [02-ontology-to-ddd-map.md](./02-ontology-to-ddd-map.md)。
