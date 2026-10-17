# 技术栈速览

**表达方式：** 技术栈清单（Stack inventory）+ 容器归属。  
结构关系仍看 [02-container.md](./02-container.md)；**为何选这些**看 [adr/](./adr/)。  
这不是又一张 C4 图——C4 只在容器上挂技术标签，清单负责版本与前置。

---

## 一页总表

| 层次 | 选型 | 版本 / 镜像 | 主要用在 |
|------|------|-------------|----------|
| 语言 | Java（Gradle toolchain） | **21** | 全部模块 |
| 框架 | Spring Boot | **3.4.1** | 各可部署服务 |
| Web | spring-boot-starter-web | BOM | REST API |
| UI | Thymeleaf | BOM | `demo-web` |
| 持久化 | Spring Data JPA + PostgreSQL 驱动 | BOM + runtime | 各 BC 服务 |
| 数据库 | PostgreSQL | **16-alpine** | Compose `postgres`；每服务一库 |
| 消息 | Spring AMQP + RabbitMQ | BOM + **3-management** | Outbox 发布 / 集成事件 |
| API 文档 | springdoc-openapi | **2.7.0** | 各 Spring Web 服务 |
| 构建 | Gradle Wrapper | 仓库自带 | `./gradlew` |
| 运行 | Docker Compose | Desktop | 全栈编排 |
| 单测 | JUnit 5 + AssertJ + H2 | BOM | `src/test/.../unit`、部分 IT |
| 集成测 | Testcontainers（Postgres / RabbitMQ） | BOM | `src/test/.../integration` |
| 冒烟 | bash + curl + jq | 本机 | `scripts/smoke-*.sh` |

依赖以根 `build.gradle.kts` 的 Boot BOM 为准；上表版本为教学锁定值，升级时改代码与本页。

---

## 按容器看技术（对照 C4 Container）

| Container | 技术要点 |
|-----------|----------|
| `demo-web` | Spring Web + Thymeleaf；HTTP 客户端调其余服务 |
| `commerce` | Web + JPA + AMQP；出站 `OrderPlaced` |
| `fulfillment` | Web + JPA + AMQP；消费 `OrderPlaced` |
| `order-return` | Web + JPA + AMQP；审批 Outbox / 消费 `RefundTerminal` |
| `payment-refund` | Web + JPA + AMQP；Stub PSP；消费/发布退款事件 |
| `postgres` | 16；库：`commerce` / `fulfillment` / `order_return` / `payment_refund` |
| `rabbitmq` | 3-management；管理台 :15672 |
| `platform`（库，非容器） | 纯 Java 事件契约；无 Spring |

---

## 学员本机前置

| 需要 | 说明 |
|------|------|
| Docker Desktop | 跑 Compose |
| JDK 21 | 本地 `./gradlew test`；镜像内自带运行时 |
| curl、jq | smoke 脚本 |
| 可选：浏览器 | http://localhost:8080/ |

端口与排障 → [handbook/05-runbook-docker.md](../handbook/05-runbook-docker.md)。

---

## 刻意不用的（教学边界）

| 常见选项 | 本标本 |
|----------|--------|
| Kafka | 用 RabbitMQ，降低本地负担 |
| 真实 PSP / 支付捕获 BC | Stub + mint `paymentId` |
| Keycloak 等完整 IAM | 仅 `X-Actor-*` 头 |
| Kubernetes / 服务网格 | Docker Compose 即可 |
| 真实 Foundry / OSDK | 概念模拟，无引擎 |

选型理由摘要 → [adr/](./adr/)。
