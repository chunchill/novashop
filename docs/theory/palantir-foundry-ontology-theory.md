# Palantir Foundry 本体建模理论研究

> 面向对本体论（Ontology）感兴趣的初学者与架构实践者。
> 本文基于 Palantir 官方文档体系（Ontology Overview、Core Concepts、Why Ontology、Architecture Center、Object/Link/Action Types、Interfaces、Functions、Object Backend、Permissioning、Ontology Design Best Practices / Anti-patterns / Structural Guidance / Validation 等）整理与阐释，力求概念准确、方法论可落地。

---

## 目录

1. [引言：为何需要「操作层」而非仅数据层](#1-引言为何需要操作层而非仅数据层)
2. [Foundry Ontology 的定义与定位](#2-foundry-ontology-的定义与定位)
3. [决策中心论：Data · Logic · Action · Security](#3-决策中心论data--logic--action--security)
4. [核心概念体系](#4-核心概念体系)
5. [本体架构：Language · Engine · Toolchain](#5-本体架构language--engine--toolchain)
6. [语义要素：Object、Property、Link、Interface](#6-语义要素objectpropertylinkinterface)
7. [动力要素：Action、Function、Automation、Dynamic Security 与 Scenario](#7-动力要素actionfunctionautomationdynamic-security-与-scenario)
8. [安全与治理](#8-安全与治理)
9. [本体驱动的应用生态](#9-本体驱动的应用生态)
10. [建模方法论：原则、结构、反模式与验证](#10-建模方法论原则结构反模式与验证)
11. [电商案例：端到端建模演练](#11-电商案例端到端建模演练)
12. [附录：术语对照与延伸阅读](#12-附录术语对照与延伸阅读)

---

## 1. 引言：为何需要「操作层」而非仅数据层

传统企业数据架构擅长回答「发生了什么」：报表、数仓、指标看板。但组织真正的竞争力往往在于能否在变化的条件下**做出正确决策并可靠地执行**——例如：

- 库存告急时，优先满足哪一批订单？
- 供应商中断时，如何重排履约路径？
- 客服收到退款申请时，谁可以批准、系统应如何联动改状态？

这类问题不只依赖数据，还依赖**业务逻辑、可执行动作与安全边界**。Palantir 将 Foundry Ontology 定位为组织的**操作层（operational layer）**：它坐落在已集成到平台中的数字资产（数据集、虚拟表、模型）之上，把这些资产与真实世界中的对应物连接起来——从工厂、设备、商品等物理资产，到客户订单、金融交易等概念实体。

在许多场景中，Ontology 充当组织的**数字孪生（digital twin）**，同时包含：

| 维度                       | 要素                                 | 作用                               |
| -------------------------- | ------------------------------------ | ---------------------------------- |
| **语义（Semantic）** | Objects、Properties、Links           | 描述「世界里有什么、它们如何关联」 |
| **动力（Kinetic）**  | Actions、Functions、Dynamic Security | 描述「如何在治理约束下改变世界」   |

这与学术本体论（对概念体系做形式化分类）既有渊源，又有关键差异：**Foundry Ontology 不是抽象分类学，而是映射到真实数据、可被应用查询、可被动作修改、可被安全策略约束的操作语义层。**

---

## 2. Foundry Ontology 的定义与定位

### 2.1 官方定义（概念层）

根据 Palantir 文档：

> **Ontology is a categorization of the world.**
> 在 Foundry 中，Ontology 是组织的数字孪生，把组织的数字资产（数据集与模型）整合为连贯整体。Foundry Ontology 通过将数据集与模型映射到 object types、properties、link types 与 action types，形成组织世界的完整图景。

四类基本映射关系：

- **Object type**：定义组织中的实体或事件
- **Property**：定义 object type 的特征
- **Link type**：定义两个 object type 之间的关系
- **Action type**：定义 object type 如何被修改

### 2.2 与数据集结构的类比（帮助建立直觉）

初学者最容易上手的入口，是把 Ontology 概念与数据集结构对照理解：

| 数据集概念   | Ontology 概念  |
| ------------ | -------------- |
| Dataset      | Object type    |
| Row          | Object（实例） |
| Column       | Property       |
| Field / Cell | Property value |
| Join         | Link type      |

**重要提醒：** 类比用于理解，不等于「把每张表原样搬成一个 Object type」。官方最佳实践反复强调：**要建模真实世界，而不是源系统表结构。**

### 2.3 Ontology 与「语义层」的区别

Architecture Center 明确指出：Ontology **不是**一层薄薄的 semantic layer，也不是单体设计。它是由数十个底层组件构成的多模态系统，在概念上可分为：

1. **Language（语言）** —— 如何描述语义、动作与逻辑
2. **Engine（引擎）** —— 如何大规模读、写、索引与物化
3. **Toolchain（工具链）** —— 如何把 Ontology 当作后端来构建应用与治理生产用例

这也是理解「本体架构」时最关键的框架（见第 5 章）。

---

## 3. 决策中心论：Data · Logic · Action · Security

Palantir 对 Ontology 价值主张的核心是：**Ontology 表征的是企业中的决策，而不仅仅是数据。**

每一个操作决策被建模为四个组成部分的整合：

### 3.1 Data（数据）

Ontology 既纳入企业多源数据（结构化、流式/边缘、非结构化、影像、地理空间等），也纳入**决策过程中产生的数据**——决策上下文、评估过的选项、已提交选择的下游影响。后者对 AI 持续学习尤为关键，因为它构成「决策血缘（decision lineage）」：何时、基于哪一版企业数据、通过哪个应用做出了决策。

数据在 Ontology 中呈现为**实时演化的 objects、properties、links**，并直接嵌入决策工作流，而非仅服务于离线报表。

### 3.2 Logic（逻辑）

逻辑是决定「何时、如何做决策」的推理资产，例如：

- CRM/ERP 中的业务规则
- 数据科学工作台中的预测模型
- 优化/仿真算法
- LLM 与多模态模型的非确定性推理

Ontology 通过灵活的 **logic binding**，为异构逻辑资产提供一致接口，使人类与 Agent 能以同样方式调用这些「工具」。

### 3.3 Action（动作）

若语义对象是企业的「名词」，动作就是「动词」。关闭决策闭环——把决策同步回操作系统——是操作系统区别于分析系统的关键。

Ontology 原生建模 actions：可在 Scenario 中安全暂存，受与数据/逻辑同等粒度的访问控制约束，并能写回交易系统、边缘设备与自定义应用。

### 3.4 Security（安全）

操作场景中的人机协作需要超越「对数据桶做角色授权」的治理能力。Palantir 安全架构强调：

- Marking / Purpose / Role 等策略组合
- 跨 data、logic、action、application 的动态血缘
- 变更与发布管理工具链
- 运行时动态计算细粒度权限（行级、列级、提交准则等）

**一句话总结：** Ontology = 决策中心的企业模型 = Data + Logic + Action + Security 的可运行整合。

---

## 4. 核心概念体系

### 4.1 Object type / Object / Object set

| 术语                  | 含义                                   |
| --------------------- | -------------------------------------- |
| **Object type** | 真实世界实体或事件的模式定义（schema） |
| **Object**      | 某 object type 的单个实例              |
| **Object set**  | 多个 object 实例的集合                 |

示例：

- Object type：`Employee`（全体员工的模式）
- Object：`Melissa Chang`
- Object set：`All tenured employees`（所有资深员工）

Object 的产生方式通常有两类：

1. 为 object type **绑定 backing datasource**（从现有数据映射而来）
2. 通过 **Actions 创建**对象（可无 backing datasource）

### 4.2 Property / Property value / Shared property

- **Property**：实体/事件特征的模式定义
- **Property value**：某 object 上该特征的具体值
- **Shared property**：可在多个 object type 上复用的属性定义，用于统一建模与集中管理元数据；**元数据共享，底层数据不共享**

常见属性基类型包括：`String`、`Integer`、`Date`/`Timestamp`、`Boolean`、数值类型、`Array`、`Struct`、`Geopoint`/`Geoshape`、`Media Reference`、`Time Series`、`Attachment`、`Marking`、`Cipher`、`Vector` 等。主键（primary key）与标题键（title key）对类型有约束（例如浮点不宜作主键，时间类型作主键通常不推荐）。

### 4.3 Link type / Link

- **Link type**：两个 object type 之间关系的模式定义
- **Link**：该关系在两个具体 object 之间的一次实例

要点：

- Link type **双向**：一个关系有两侧，每侧可独立命名与遍历，无需再定义「反向 link type」
- 同一对 object type 之间可定义**多个不同** link type（代表不同真实关系）
- 可定义**自关联**（如 `Employee` 的 Direct Report ↔ Manager）
- **跨 Ontology 的链接不受支持**；需要共享同一 Ontology
- 多对多关系通常由 join 表（link 自身的 datasource）支撑

### 4.4 Action type / Action

- **Action**：一次事务性变更，按用户定义逻辑修改一个或多个 object 的属性/链接
- **Action type**：这组变更及其副作用行为的定义

Action 让用户以「业务目标」而非「逐字段改库」的方式管理数据。同一套 action 逻辑可在 Workshop、Object Views 等应用中复用，保证编辑一致性；用户编辑后的最新状态体现在 writeback / materialization 机制中（OSv1 与 OSv2 实现不同，见第 5 章）。

### 4.5 Function

Function 是服务端隔离执行的代码逻辑（TypeScript / Python），与 Ontology 一等集成：可读取 objects / object sets、遍历 links、并在 function-backed action 中产生 Ontology edits。

典型用途：Workshop 中的派生列与聚合、复杂多对象编辑、Quiver 自定义指标、外部系统富化、LLM 流式结果等。

### 4.6 Interface

Interface 描述 object type 的「形状」与能力，提供**多态**：工作流可面向接口编程，而不必硬编码每个具体类型。

- Interface **抽象**：无 backing dataset，不可直接实例化
- Object type **具体**：可实例化
- 一个 object type 可实现多个 interfaces
- Interface 可继承/扩展其他 interfaces

### 4.7 权限与 Object Views

- **Project-based permissions**：当前推荐的 Ontology 资源权限模型；资源元数据权限与对象/链接实例的数据权限需要分别配置
- **Ontology roles**：遗留权限模式；新设计不应再把它视为中心权限模型
- **Object Views**：围绕某个 object 的信息与工作流中枢（传记式信息、关联对象、指标、分析与应用嵌入）

### 4.8 Models 与 Ontology

把模型映射进 Ontology，使预测/分类/估计等结果以真实世界概念（某 object 的属性）呈现。收益包括：可解释性、规模化复用、以及让 Ontology 不仅成为数据的单一事实来源，也成为**逻辑预期**的单一事实来源——从而支撑仿真与数字孪生式推演。

---

## 5. 本体架构：Language · Engine · Toolchain

### 5.1 三层概念架构

#### Language（本体语言）

描述：

- 语义：objects、links、properties
- 动力：actions、automations
- 逻辑：决定动作如何运行、如何与外部系统交互的规则与函数

#### Engine（本体引擎）

兑现 Language 的运行时能力：

- **读架构**：高规模 SQL 查询、状态变更实时订阅、各类 materialization
- **写架构**：原子且持久的事务更新、高规模批变更、流式写入、CDC 等低延迟镜像

#### Toolchain（工具链）

以 Ontology 为后端构建生产级应用：Ontology SDK（OSDK）、DevOps 与治理工具等。

### 5.2 后端功能组件（实现视角）

Object Backend 文档将 Ontology 后端职责概括为三类：

1. **Datasource 管理** —— 喂养 Ontology，管理 schema
2. **查询 / 搜索 / 聚合** —— 支持过滤与权限
3. **写入编排** —— 索引 datasource，并应用用户 edits

主要服务：

| 组件                                       | 职责                                                          |
| ------------------------------------------ | ------------------------------------------------------------- |
| **OMS（Ontology Metadata Service）** | 定义存在哪些本体实体及其元数据（object/link/action types 等） |
| **Object databases**                 | 存储索引后的对象数据；负责索引、查询、编排用户编辑            |
| **OSS（Object Set Service）**        | 对外提供读能力：搜索、过滤、聚合、加载                        |
| **Actions**                          | 应用用户 edits；支持复杂权限与条件；可产生 action log         |
| **Object Data Funnel**               | OSv2 中编排写入：从 datasource 与 Actions 读入并索引          |
| **Functions on Objects**             | 操作上下文中的快速逻辑执行                                    |

### 5.3 Object Storage v1 vs v2（架构演进）

|          | OSv1（Phonograph）                       | OSv2                                                                                                 |
| -------- | ---------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| 定位     | 遗留对象库                               | 下一代规范数据存储                                                                                   |
| 状态     | 计划弃用（文档标注 2026-06-30 后不可用） | 当前推荐                                                                                             |
| 编辑启用 | 通常依赖 writeback dataset               | 可在 Datasources 页开启 Edits；materialization 可选                                                  |
| 能力亮点 | 统一但耦合度高                           | 索引与查询解耦、增量索引、更高吞吐、多 datasource 细粒度权限、流式索引、单次 Action 可编辑更多对象等 |

对建模者的实践含义：

- **优先按 OSv2 思维设计**（Edits、materializations、多 datasource 权限）
- Writeback dataset 在 OSv1 中是「编辑后真相」的关键出口；在 OSv2 中更常讨论 materialization 与 Action 写路径

### 5.4 Object sets 的几种形态

| 维度 | 类型      | 说明                               |
| ---- | --------- | ---------------------------------- |
| 定义 | Static    | 保存主键列表，不随数据变化         |
| 定义 | Dynamic   | 保存过滤条件，匹配新数据时集合更新 |
| 状态 | Temporary | 跨应用传递用，通常 24 小时过期     |
| 状态 | Permanent | 持久资源，可共享复用               |

---

## 6. 语义要素：Object、Property、Link、Interface

本章对应建模时的「名词系统」设计。

### 6.1 Object type 设计要点

1. **一个 object type 对应一个清晰的真实世界实体或事件**
2. **先理解领域，再映射数据**（而非「ontologize a dataset」式逐列映射）
3. **主键稳定且跨源可对齐**（跨系统合并的前提）
4. 纯技术辅助类型若不可避免，应标记 **hidden**，避免污染默认导航视图

### 6.2 Property 设计要点

- 每个属性应有明确业务或技术价值（有意策展，而非 Kitchen Sink）
- 用业务语言命名：`lastInspectionDate` 优于 `dtLastInspMod`
- 多字段语义一体时使用 **Struct**（如地址、带置信度的 AI 输出）
- 跨类型一致的属性用 **Shared property**
- 对需约束与复用语义的值可使用 **Value types**（文档中的 value types 体系）

#### 规范化与派生属性

官方结构指引：**每个事实只存一次；用 derived properties 获得便利。**

| 类型                | 特征                             | 推荐手段           | 示例                                 |
| ------------------- | -------------------------------- | ------------------ | ------------------------------------ |
| Pre-computed        | 同对象稳定输入、随管道更新       | Pipeline transform | `fullName = firstName + lastName`  |
| Dynamically derived | 依赖链接对象或 Action 引起的变化 | Derived property   | `directReportCount` 统计链接员工数 |

高规模查询（约 >10k objects/query）时，派生属性可能引入延迟；此时选择性反规范化应是**有文档记录的显式权衡**，而非默认。

### 6.3 Link type 设计要点

链接应回答清晰的领域问题，例如「这个订单属于哪个客户？」。

| 链接形态           | 何时使用                                 | 示例                                       |
| ------------------ | ---------------------------------------- | ------------------------------------------ |
| Direct link        | 关系本身无额外元数据                     | `Order → Customer`                      |
| Object-backed link | 关系自带元数据（角色、起止、分配比例等） | `Employee → VentureStaffing → Venture` |

基数（cardinality）与 backing：

- 一对一 / 一对多：通常通过外键属性表达；创建/删除链接常体现为修改外键
- 多对多：link type 自身常有 join datasource

命名建议：从两侧各自「自然可读」——如 `department` / `employees`，避免 `relatedItems`、`link1`。

### 6.4 Interface 设计要点

适用于：

- 多类型共享属性/动作（`Inspectable`）
- 共享工作流（面向 `SchedulableResource` 调度）
- 分类学聚合（`MilitaryAsset` 由 Aircraft/Vessel/... 实现）

原则：**组合优于深继承**——用多个 focused interfaces 组合能力，而不是造 `SchedulableBuilding` 这类扭曲中间类型。

即使平台某些应用对 interface 支持尚未完全，也应尽早定义接口，为后续收敛预留路径（官方建议：scaffold now, consolidate later）。

---

## 7. 动力要素：Action、Function、Automation、Dynamic Security 与 Scenario

本章对应建模时的「动词系统」、事件反应、安全约束与推演系统。官方概览所称的 canonical kinetic elements 是 **actions、functions、dynamic security**；Automations 与 Scenarios 是组织和试演这些动力能力的重要运行机制。

### 7.1 Action type 的结构

一个完整的 Action type 通常包含：

1. **Parameters**：用户/应用传入的输入（对象引用、字符串、数值等）
2. **Rules**：把参数转化为 Ontology edits 或副作用
3. **Submission criteria**：是否允许提交的业务条件（原 validations）
4. **Side effects**：通知、Webhook、触发 schedule 等

#### Ontology rules（节选）

- Create / Modify / Create-or-modify / Delete object(s)
- Create / Delete many-to-many link(s)
- Function rule（引用 Ontology edit function；出现时通常独占）
- Interface 上的 create/modify/delete/link 规则（对实现某接口的对象操作）

规则值可来自：参数、对象参数的属性、静态值、当前用户/当前时间等。

#### Submission criteria

把业务逻辑编码进「能否提交」：

- 基于 **Current user**（组、组织、属性）
- 基于 **Parameter**（如飞机是否仍在运营）
- 基于 **Execution context**（是否在 Scenario 中执行）

可组合 AND/OR/嵌套逻辑，并为失败条件配置面向终端用户的失败消息。

### 7.2 Actions 与 Pipelines / Functions 的分工（避免 Golden Hammer）

| 能力                         | 适合解决的问题                           |
| ---------------------------- | ---------------------------------------- |
| **Pipelines（批/流）** | 自动化数据加工、跨系统合并、稳定派生字段 |
| **Actions**            | 人类或 Agent 的决策性变更、受治理的写回  |
| **Functions**          | 复杂实时逻辑、多对象编辑编排、模型包装   |
| **Automations**        | 事件驱动反应                             |

错误做法：用 Action 做本该在管道中完成的大规模 ETL；或用管道绕过本该受提交准则约束的人工决策。

### 7.3 Function-backed Actions

当变更逻辑复杂到规则配置器难以表达时，使用 Ontology edit functions：

- 声明编辑的 object types
- 在函数中 create/update/delete objects 与 links
- 由 Action 提交时统一应用 edits（并受权限与提交准则约束）

### 7.4 Automations 与 Dynamic Security

- **Automations**：按持续条件或计划触发 Action、Function、通知等 effect；提交 Action 时仍需通过 submission criteria，并以 automation owner 身份执行
- **Dynamic Security**：根据用户、组织、purpose、对象属性、marking/classification 与执行上下文动态约束数据、逻辑和动作

Function 仅仅返回 edits 不代表已经写入 Ontology；需要持久化时，仍应由 function-backed Action 应用。

### 7.5 Scenarios（What-if 沙箱）

Ontology scenarios（文档标注为 beta）用于：

- What-if 分析
- 业务逻辑仿真
- Agent 评估与预测对比
- 多方案权衡

关键特性：

- 由 Actions（以及可选 Models）形成 Ontology 数据的不可变 fork，不直接破坏线上主数据
- Scenario 中可比较多个方案；正式执行称为 **Apply scenario**，不是 merge
- Apply 时，关联 Actions 的 edits 被事务性应用到主 Ontology；Scenario 中的模型结果不会随之写回
- 提交准则可区分「Scenario 内」与「主 Ontology」执行上下文
- **不是**历史版本快照工具

与 Global Branching 的区别（简要）：

- **Global Branching**：构建者端到端开发/测试工作流，避免冲击生产
- **Scenario**：工作流使用者（人与 Agent）在沙箱中试动作、比较方案，再按治理规则 apply

---

## 8. 安全与治理

### 8.1 两层授权结构

1. **Ontology resources**（object/link/action types 等元数据/schema）
2. **Objects and links**（实际主键与属性值数据）

二者权限需分别理解与配置。

### 8.2 语义化安全设计

结构指引强调：安全应按领域语义表达，遵循最小权限。

| 层           | 控制内容             | 示例                             |
| ------------ | -------------------- | -------------------------------- |
| Row-level    | 可见哪些 objects     | VIP 客户仅高级客服可见           |
| Column-level | 可见对象上的哪些属性 | 支付信息仅财务团队可见           |
| Cell-level   | 行×列交集           | VIP 客户的支付信息仅高级财务可见 |

**反模式：** 为安全拆出 `PublicCustomer` / `RestrictedCustomer` 两套重复类型——易导致 schema 漂移。应优先「单类型 + 策略」。

### 8.3 动态安全与工具调用

在 Agent 场景中：

- 工具调用至少依赖对底层 objects/properties/links 的访问权
- Actions 可附加运行时校验（submission criteria）
- 日志与遥测同样受 markings 等安全原语约束

### 8.4 编辑路径上的治理

允许用户编辑前，需正确配置 edits（OSv2 toggle / OSv1 writeback 等）。编辑不会直接覆盖原始 backing dataset；在 OSv2 中由对象写路径索引最新状态，materialization 是可选的下游输出。这样可同时保留源数据、用户决策和最新操作状态的可追溯关系。

---

## 9. 本体驱动的应用生态

Ontology 的投资目标是**规模化地改善组织决策**。Foundry 提供一组原生理解 Ontology 的应用：

| 应用                      | 主要用途               | 工作流风格            | 配置模型                   |
| ------------------------- | ---------------------- | --------------------- | -------------------------- |
| **Object Views**    | 发现 / 360° 对象中枢  | 工作流特定            | 开箱可用                   |
| **Object Explorer** | 发现与探索分析         | 探索式                | 开箱可用                   |
| **Quiver**          | 高级分析与仪表盘       | 探索 / 可发布为仪表盘 | 分析模式开箱；仪表盘可定制 |
| **Workshop**        | 无代码应用与交互工作流 | 工作流特定            | 可定制                     |
| **Slate**           | 更灵活/偏代码的应用    | 工作流特定            | 可定制（可碰数据集）       |
| **Map**             | 地理空间分析           | 探索或工作流          | 开箱可用                   |
| **Carbon**          | 组合多资源成操作工作区 | 工作流特定            | 可定制                     |

用途光谱：

- **Discovery** —— 找到正确信息与工作流
- **Analysis** —— 探索式回答开放问题
- **Dashboards** —— 预配置只读消费
- **Applications** —— 面向特定角色的交互式操作界面（常含 writeback）

对建模者的启示：**Ontology 质量会直接决定探索式应用是否「开箱可用」**；若模型不可导航，团队往往会用 Workshop「硬编码路径」掩盖建模缺陷——这在验证阶段应被刻意排除（见 10.4）。

---

## 10. 建模方法论：原则、结构、反模式与验证

本章是「如何做」的核心。建议把建模视为**持续演进的生产级软件设计**，而非一次性导入。

### 10.1 四条优先级原则

官方按优先级给出四条核心原则（冲突时高优先级优先）：

| 优先级 | 原则                                                  | 核心思想                           |
| ------ | ----------------------------------------------------- | ---------------------------------- |
| 1      | **Domain-driven design**                        | 建模真实世界，而非源数据           |
| 2      | **Do not repeat yourself（Rule of three）**     | 同样的东西建了三次就重构           |
| 3      | **Open for extension, closed for modification** | 保护核心模型，允许扩展             |
| 4      | **Composition over deep hierarchies**           | 用 interfaces 组合能力，保持可插拔 |

#### 原则 1：领域驱动

- Object 应是 `Patient`、`WorkOrder`、`Order` 这类语义概念，而不是 `api_v3_order_resp`
- 一条源数据行可能对应多个实体（订单 CSV 中同时含客户、商品、数量 → 至少三类）
- 命名面向人类与 Agent 可导航性

#### 原则 2：不要重复自己

- 多团队各自造 `SalesCustomer` / `SupportCustomer` 是典型失败
- 优先单一规范类型，或共享 Interface

#### 原则 3：对扩展开放、对修改封闭

- 生产核心类型应稳定
- 新能力优先通过**链接扩展类型 / 新 Interface 实现**加入，而非不断往核心类型塞可空字段

#### 原则 4：组合优于深层次

- `Arena implements Building + SchedulableResource`
- 避免为每种能力组合制造中间继承节点

### 10.2 实用设计清单（Design guidelines）

1. Model reality, not systems
2. Curate intentionally（每个属性有价值）
3. Collaborate across teams（跨部门共建，防孤岛）
4. Keep object types focused
5. Choose the right tool（Action / Pipeline / Function / Automation）
6. Use interfaces for abstraction
7. Document decisions in Ontology Manager
8. Validate against real business questions

### 10.3 八大反模式（务必避免）

| 反模式                     | 症状                                            | 解法                              |
| -------------------------- | ----------------------------------------------- | --------------------------------- |
| **System Silos**     | 按源系统拆 `HR Employee` / `Badge Employee` | 管道合并 → 单一 `Employee`     |
| **Kitchen Sink**     | 把 ETL 元数据列全暴露为属性                     | 有意策展；技术列留在数据集        |
| **Department Silos** | 销售/客服/财务各造一套 Customer                 | 共享 `Customer` + 部门属性/链接 |
| **God Object**       | 一个类型装下多种实体，大量空字段                | 拆分类型；共性用 Interface        |
| **Golden Hammer**    | 凡事只用 Action 或凡事只用管道                  | 按问题选工具                      |
| **Action Sprawl**    | 大量单字段 Action                               | 按业务操作打包相关变更            |
| **Time Machine**     | 历史版本拆成多个对象/类型                       | 单实体 + 历史/修订对象或时序属性  |
| **Misnomer**         | `Item`、`value`、`link1` 等含糊命名       | 具体、可区分、按关系命名          |

### 10.4 任务型验证（比 schema review 更重要）

结构正确 ≠ 可运营。Palantir 推荐用**真实业务问题**做验收：

1. 从运营节奏提炼问题（领导反复追问什么？跨团队决策需要什么？）
2. 问题序列建议：**建立态势 → 追溯因素 → 评估影响**
3. 演练时**禁用**为任务特制的 Workshop「拐杖应用」，使用通用分析工具
4. 记录正确性、耗时、犹豫点、搜索词、是否人工导出/手搓 join
5. 对人与 Agent（如 AIP Analyst）分别跑同一问题集，比较暴露的弱点

观察 → 可能原因（节选）：

| 观察                 | 优先检查                    |
| -------------------- | --------------------------- |
| 找不到起始类型       | 命名/别名/描述/可见类型过多 |
| 同一概念选不同类型   | Department Silos / 概念重叠 |
| 无法在相关概念间跳转 | 缺失或错误的 Link           |
| 只能靠导出手工 join  | 领域关系未本体化            |
| 只有构建者能答对     | System Silos / 源系统术语   |

验证应成为**持续实践**：问题集演进为 Ontology 的回归测试套件。

### 10.5 建议的建模过程（方法步骤）

可按以下循环推进：

```text
① 界定决策与角色（哪些决策要闭环？谁做？）
        ↓
② 抽取领域名词与动词（Objects / Links / Actions）
        ↓
③ 设计规范模型（interfaces、shared properties、安全边界）
        ↓
④ 映射数据与逻辑资产（pipelines、models、functions）
        ↓
⑤ 配置 Actions + Submission criteria + Side effects
        ↓
⑥ 用 Object Views / Explorer / Workshop 交付工作流
        ↓
⑦ 任务型验证 → 重构（DRY / 扩展点 / 反模式清理）
        ↓
⑧ Scenario / Agent 工具化 → 持续学习与演进
```

---

## 11. 电商案例：覆盖语义、动力、安全与运行时的端到端建模

下面以多品类电商 **NovaShop** 为例。公司同时经营自营与第三方卖家，OMS、PIM、WMS、PSP、CRM 和 3PL 数据割裂。本案例不追求罗列最多类型，而是证明一条完整链路：

> 业务决策 → 语义模型 → 逻辑 → 受治理动作 → 外部执行 → 决策血缘 → 反馈与演进

### 11.1 Step 0：定义决策、结果与责任边界

先定义需要闭环的决策，而不是先扫描数据表：

| 决策 | 责任角色 | Data | Logic | Action | Security / Outcome |
| --- | --- | --- | --- | --- | --- |
| 缺货时是否改仓 | 履约调度员 | 订单行、库存、仓、运力 | 分配优化、ETA | 改仓、调整预留 | 仅授权调度员；降低缺货与延误 |
| 高风险订单是否放行 | 风控专员 | 客户、订单、支付、历史 | 风险模型、政策规则 | 放行、拒绝、转人工 | 支付属性受限；控制拒付损失 |
| 退货是否批准并退款 | 售后与主管 | 订单行、物流、质检 | 退货政策、欺诈规则 | 批准退货、请求退款 | 金额分级；跟踪退款结果 |
| 促销库存如何预留 | 商品运营 | 活动、SKU、仓库库存 | 需求预测、分配优化 | 建立或释放预留 | 先在 Scenario 比较方案 |

成功标准不仅是“能查到数据”，还包括：

1. 人与 Agent 能从业务概念导航到决策所需上下文；
2. 每次变更都经过相同的 Action、权限与提交准则；
3. Ontology edits 与外部系统副作用的边界明确；
4. 能回答谁在何时、基于什么数据和模型、为何选择某方案，以及结果如何。

### 11.2 Step 1：建立规范语义模型

#### 11.2.1 Object types、Properties 与标识

对象类型对应真实实体、事件或需要独立生命周期的关系，不对应源系统表。

| 领域 | Object type | 主键 / 标题键 | 关键 Properties |
| --- | --- | --- | --- |
| 客户与商品 | `Customer` | customerId / displayName | email、segment、riskTier、defaultAddress |
| 客户与商品 | `Seller` | sellerId / sellerName | sellerType、rating |
| 客户与商品 | `Product` | productId / title | brand、category |
| 客户与商品 | `Sku` | skuId / displayName | attributes、listPrice、currencyCode |
| 交易 | `SalesOrder` | orderId / orderNumber | placedAt、orderStatus、channel、totalAmount |
| 交易 | `OrderLine` | orderLineId / displayName | quantity、unitPrice、lineStatus |
| 交易 | `Payment` | paymentId / paymentReference | amount、paymentStatus、paidAt、instrumentMasked |
| 履约 | `Warehouse` | warehouseId / warehouseName | warehouseStatus、regionCode、location、capacity |
| 履约 | `InventoryPosition` | inventoryPositionId / displayName | onHand、reserved、availableToSell、asOf |
| 履约 | `FulfillmentOrder` | fulfillmentId / fulfillmentNumber | fulfillmentStatus、promisedShipBy、version |
| 履约 | `FulfillmentAllocation` | allocationId / displayName | allocatedQuantity、allocationStatus |
| 履约 | `Shipment` | shipmentId / trackingNumber | carrier、shipmentStatus、estimatedDeliveryAt、shippedAt |
| 履约 | `ShipmentLeg` | shipmentLegId / displayName | sequence、eta、ata、origin、destination |
| 售后 | `ReturnRequest` | returnRequestId / returnNumber | returnStatus、requestedAt、reasonCode |
| 售后 | `ReturnLine` | returnLineId / displayName | quantity、inspectionResult、responsibility |
| 售后 | `Refund` | refundId / refundReference | amount、refundStatus、requestedAt、completedAt |
| 运营 | `Promotion` | promotionId / promotionName | promotionStatus、startsAt、endsAt |
| 运营 | `InventoryReservation` | reservationId / displayName | quantity、reservationStatus、expiresAt |
| 决策 | `RiskReview` | reviewId / displayName | reviewStatus、score、confidence、explanation、featureVersion、evaluatedAt、decision、reason |
| 决策 | `FulfillmentDecision` | decisionId / displayName | candidateSummary、selectedOption、reason |
| 决策 | `PromotionDecision` | decisionId / displayName | scenarioId、selectedOption、reason |
| 集成 | `ExternalExecution` | executionId / idempotencyKey | targetSystem、operation、executionStatus、attemptCount、lastError |

关键设计说明：

- `Address` 可建为 Struct，`Warehouse.location` 使用 Geopoint；质检图片可用 Media Reference / Attachment。属性类型服务于业务语义，而非炫技。
- 外键可保留在 backing datasource 中用于构建 Link，但用户导航应依赖 Link，避免同时暴露两份语义事实。
- `InventoryReservation`、`RiskReview`、`FulfillmentDecision` 与 `ExternalExecution` 不是技术日志：它们分别拥有业务生命周期、权限和审计价值，因此值得成为对象。
- 购物车、发票、卖家结算等不服务于本轮四个决策，先留在扩展范围，避免 Kitchen Sink。

#### 11.2.2 Value types 与 Shared properties

Value type 用于为基础类型增加统一语义、格式和验证，例如：

- `OrderId`、`SkuId`、`WarehouseId`：受约束的标识符；
- `CurrencyCode`、`RegionCode`、`ReasonCode`：受控代码；
- `EmailAddress`：格式验证；
- `OrderStatus`、`FulfillmentStatus`、`ReturnStatus`、`RefundStatus`：各自独立的状态值域。

不要创建一个通用 `Status` value type：这些状态机的含义和合法值不同。Value types 受所在 space 与平台支持范围约束，使用前应核对 enrollment。

Shared property 只在**名称、类型、描述和治理语义都一致**时复用。本例共享 `createdAt`、`updatedAt`、`createdBy`；不共享各领域的 `status`。Shared property 共享元数据定义，不共享底层数据值。

#### 11.2.3 Links：让决策路径闭合

| Link type | 基数 | 两侧可读名称 |
| --- | --- | --- |
| `SalesOrder → Customer` | 多对一 | order.customer / customer.orders |
| `OrderLine → SalesOrder` | 多对一 | line.order / order.lines |
| `OrderLine → Sku` | 多对一 | line.sku / sku.orderLines |
| `Payment → SalesOrder` | 多对一 | payment.order / order.payments |
| `Sku → Product` | 多对一 | sku.product / product.skus |
| `InventoryPosition → Sku` | 多对一 | position.sku / sku.inventoryPositions |
| `InventoryPosition → Warehouse` | 多对一 | position.warehouse / warehouse.inventoryPositions |
| `FulfillmentOrder → SalesOrder` | 多对一 | fulfillment.salesOrder / order.fulfillments |
| `FulfillmentOrder → Warehouse` | 多对一 | fulfillment.warehouse / warehouse.fulfillments |
| `FulfillmentAllocation → FulfillmentOrder` | 多对一 | allocation.fulfillment / fulfillment.allocations |
| `FulfillmentAllocation → OrderLine` | 多对一 | allocation.orderLine / line.allocations |
| `Shipment → FulfillmentOrder` | 多对一 | shipment.fulfillment / fulfillment.shipments |
| `ShipmentLeg → Shipment` | 多对一 | leg.shipment / shipment.legs |
| `ReturnRequest → SalesOrder` | 多对一 | return.order / order.returnRequests |
| `ReturnLine → ReturnRequest` | 多对一 | returnLine.request / request.lines |
| `ReturnLine → OrderLine` | 多对一 | returnLine.originalLine / line.returnLines |
| `Refund → ReturnRequest` | 多对一 | refund.returnRequest / request.refunds |
| `Refund → Payment` | 多对一 | refund.payment / payment.refunds |
| `InventoryReservation → Promotion` | 多对一、可选 | reservation.promotion / promotion.reservations |
| `InventoryReservation → Sku` | 多对一 | reservation.sku / sku.reservations |
| `InventoryReservation → Warehouse` | 多对一 | reservation.warehouse / warehouse.reservations |
| `InventoryReservation → FulfillmentOrder` | 多对一、可选 | reservation.fulfillment / fulfillment.reservations |
| `RiskReview → SalesOrder` | 多对一 | review.order / order.riskReviews |
| `RiskReview → Customer` | 多对一、可选 | review.customer / customer.riskReviews |
| `RiskReview → Payment` | 多对一、可选 | review.payment / payment.riskReviews |
| `FulfillmentDecision → FulfillmentOrder` | 多对一 | decision.fulfillment / fulfillment.decisions |
| `PromotionDecision → Promotion` | 多对一 | decision.promotion / promotion.decisions |
| `ExternalExecution → Refund` | 多对一 | execution.refund / refund.executions |
| `ExternalExecution → FulfillmentDecision` | 多对一 | execution.fulfillmentDecision / decision.executions |

本例使用 `FulfillmentAllocation` 作为 object-backed link，以两个多对一链接承载订单行在某履约单中的 `allocatedQuantity`。这就是 object-backed link 的判断标准：**关系是否拥有自己的事实和生命周期**。`InventoryReservation` 可由 Promotion 或 FulfillmentOrder 拥有，并通过 submission criteria / Function 保证两种 owner 不会同时为空或同时存在。

自关联 `Sku ↔ Sku`（`substitutes` / `substitutedBy`）表达缺货替代。Link type 本身双向，不要再造一个重复的反向类型。

#### 11.2.4 Interfaces：定义可执行契约

Interface 不是标签，而是共享 shape/capability 的契约：

| Interface | 契约 | 实现者与映射 | 面向接口的工作流 |
| --- | --- | --- | --- |
| `RiskAssessable` | riskState、latestRiskScore、lastAssessedAt；`RequestRiskAssessment` action constraint | `Customer`、`SalesOrder`、`Payment` 映射各自属性和 Action | 风险队列、批量评估函数 |
| `FulfillmentTrackable` | fulfillmentState、promisedAt；shipment link constraint | `FulfillmentOrder`，后续可由门店自提单实现 | 延误分析与告警 |
| `ExternallyExecutable` | externalStatus、lastAttemptAt；`RetryExternalExecution` action constraint | `Refund`、`ExternalExecution` | 失败重试与对账 |
| `DecisionRecord` | decidedAt、decidedBy、reason、sourceApplication、actionId、inputObjectSetRid、logicAssetRid、functionVersion、modelDeploymentVersion、outcome | `RiskReview`、`FulfillmentDecision`、`PromotionDecision` | 审计、回放与效果评估 |

只有一个实现者且无近期扩展计划时，不为了“抽象”强造 Interface。面向 Interface 的 Action、Function 或应用还需核对具体语言和应用的 feature support。

#### 11.2.5 Object sets：把“队列”建成可复用查询资产

- `AtRiskOrders`：动态过滤 `SalesOrder`，条件为风险分超过阈值且未完成人工复核；
- `DelayedShipments`：动态过滤 `Shipment`，条件为当前时间超过 ETA 且未签收；
- `PendingReturnReviews`：动态过滤 `ReturnRequest`，供客服工作台消费；
- `PromotionReservationCandidates`：动态过滤 `InventoryPosition`，作为优化 Function 输入；
- 某次促销评审选择的 500 个 SKU：可保存为 permanent static object set，以复现评审范围；
- 从 Object Explorer 临时传给 Workshop 的结果：temporary object set，适合短期跨应用传递，不作为长期业务记录。

Object set 是某一 object type 的无序对象集合。需要跨类型多态时，应通过 Interface 及受支持的 API，而不是把不同类型硬塞进同一个集合。

### 11.3 Step 2：映射数据，并设计读写状态生命周期

#### 11.3.1 Datasources 与规范来源

- OMS → `SalesOrder`、`OrderLine`、`FulfillmentOrder`、`FulfillmentAllocation`
- PIM → `Product`、`Sku`
- WMS → `Warehouse`、`InventoryPosition`
- PSP → `Payment` 与退款执行回执
- CRM / 退货管理系统 → `Customer`、`ReturnRequest`、`ReturnLine`
- 活动管理系统 → `Promotion`
- 3PL → `Shipment`、`ShipmentLeg`

Pipeline 负责标识解析、去重和跨源合并。例如客户姓名以 CRM 为权威来源，订单状态以 OMS 为准，库存实物数以 WMS 为准。技术摄取列留在数据集，不默认暴露到 Ontology。

#### 11.3.2 OSv2 Edits、冲突策略与 Materialization

按 Action 的完整 edit set 开启 Edits：包括会被创建的 `InventoryReservation`、`RiskReview`、`FulfillmentDecision`、`PromotionDecision`、`Refund`、`ExternalExecution`，会被修改的 `SalesOrder`、`FulfillmentOrder`、`ReturnRequest`，以及 Action 会创建、删除或改绑的相关 Links。`FulfillmentAllocation` 默认由 OMS 分配事件提供；若允许人工拆分履约，则另定义 `SplitFulfillment` Action，并为该对象及链接开启 Edits。对源系统主导的 `InventoryPosition`，明确每个属性的冲突策略：

- `onHand`：WMS 权威，不允许人工 Action 直接修改；
- `reserved`：若 WMS 和 Actions 都会更新，按业务选择 **Apply user edits** 或基于 UTC 时间戳的 **Apply most recent value**，并测试乱序到达；
- `availableToSell`：由一致的派生逻辑计算，不让多个写入者各自维护。

OSv2 中开启 Edits 不依赖 materialization。Action edits 进入对象写路径并由 Object Data Funnel 编排索引；materialization 是 OSv1 writeback dataset 的后继/对应概念，但在 OSv2 中是可选的、且可配置多个，用于生成包含源数据与用户 edits 的最新状态数据集。对象上的 discretionary 行列策略不会自动变成 materialized dataset 的同等保护；下游还需 markings / classification controls 与数据集权限。

### 11.4 Step 3：Logic——派生属性、Functions 与 Models

| 需求 | 实现 | 边界 |
| --- | --- | --- |
| 客户累计订单数 | Derived property：聚合 customer.orders | 依赖链接实时状态；高规模查询需压测 |
| 可售库存 | Derived property 或 Pipeline 预计算 | 只保留一个事实来源；反规范化要记录同步策略 |
| 风险评分 | 已部署 Model + wrapper Function | 在线结果不会自动持久化 |
| 改仓候选方案 | Function 读取订单行、库存 object set、仓与运力 | 返回排序方案，不直接产生持久编辑 |
| 多对象原子改仓 | Ontology edit Function + function-backed Action | edits 只有经 Action 提交才落地 |

风险模型契约至少记录：model/deployment version、输入特征版本、score、置信度、解释、evaluatedAt 与失败策略。三种使用方式要区分：

1. **在线建议**：Workshop 调 Function 即时展示，不写属性；
2. **持久复核**：`RequestRiskAssessment` 创建 `RiskReview`，再由 `DecideRiskDisposition` 保存人工结论；
3. **Scenario 预测**：比较方案的模型结果只存在 Scenario 评估中，Apply scenario 时不会自动写回主 Ontology。

### 11.5 Step 4：Actions——完整建模动力要素

Action 围绕业务操作设计。参数收集意图，rules 产生 edits，submission criteria 守住不变式与权限，side effects 连接后续流程。

#### 11.5.1 `ReallocateFulfillmentWarehouse`

**Parameters**

- `fulfillment: FulfillmentOrder`
- `targetWarehouse: Warehouse`
- `reason: String`
- `expectedVersion: String`（用于检测用户评审后状态已变化）

**Rule**

采用一个 **Function rule**：Ontology edit Function 重新读取所需订单行和库存，创建 `FulfillmentDecision`，更新履约单的仓库链接，释放原仓预留并建立目标仓预留。Function rule 出现时不再混用其他 Ontology rules，所有 edits 由该函数统一返回并由 Action 原子提交。

**Submission criteria**

```text
ALL:
  current user is in "Fulfillment Controllers"
  fulfillment.fulfillmentStatus is ALLOCATED
  fulfillment.version equals expectedVersion
  target warehouse is active
```

跨多个订单行、SKU 与库存对象的区域、危险品和可售库存约束由 edit Function 在生成 edits 前再次校验；不要假设所有复杂图约束都能由声明式 submission criteria 表达。Scenario 中可使用独立条件允许 `Planners` 评估候选方案，但 Apply 时仍重新校验生产状态，不能假设 Scenario 创建时的库存仍有效。

**外部副作用**

由于 Function rule 不能与其他 rules（包括通知或 Webhook rule）组合，该 Action 只原子创建 Ontology edits 以及状态为 `PENDING` 的 `ExternalExecution`。后续 Automation 发现该对象后通知仓经理并调用 WMS，使用 `decisionId` 作为幂等键，再由独立 Action 记录成功、失败与重试状态。Ontology edits 的原子性不包含通知、Webhook 或外部 WMS 写入。

#### 11.5.2 `ApproveReturn`

**Parameters**：`returnRequest`、批准的 `ReturnLine` 集合、`refundAmount`、`resolutionCode`、`reason`。

**Rules**：简单情形可用声明式 rules 修改 `ReturnRequest`，创建 `Refund`（状态 `PENDING_EXTERNAL_EXECUTION`），并创建 `Refund → ReturnRequest` 与 `Refund → Payment` 链接；需要跨多个 `ReturnLine` 校验逐行可退数量时，改用单一 Function rule 生成全部 edits，不与任何其他 rules 混用。`Payment` 由参数显式选择或经 `ReturnRequest → SalesOrder ← Payment` 路径唯一解析。

**Submission criteria**

- 售后组可批准不超过 ¥500；
- 更高金额要求主管权限；
- 申请必须为 `PENDING_REVIEW`；
- 退款金额不得超过可退金额，且同一订单行不得重复退款。

批准退货与 PSP 实际退款是两个一致性边界，不能写成一个原子事务。

#### 11.5.3 `ExecuteRefund` 与 `RecordRefundResult`

`ExecuteRefund` 受治理地发起 PSP 请求，使用 `refundId` 作为幂等键；`RecordRefundResult` 根据可信回调或对账结果把 `Refund` 更新为 `SUCCEEDED` / `FAILED`。失败进入重试或人工补偿队列。这样不会出现“Ontology 已显示退款成功，但支付机构实际失败”的假象。

#### 11.5.4 `PlaceInventoryReservation`

以 `Promotion` 和 `PromotionReservationCandidates` object set 为输入，由优化 Function 计算分配，并通过 function-backed Action 批量创建 `InventoryReservation`。Submission criteria 只检查操作者和活动状态；object-set 聚合、总预留量与逐仓可用量由 edit Function 校验并在违反约束时阻止提交，因为 object-set 参数不能直接用于 submission criteria。

#### 11.5.5 `RequestRiskAssessment` 与 `DecideRiskDisposition`

`RequestRiskAssessment` 面向 `RiskAssessable` 契约，调用已部署模型并创建 `RiskReview`，记录目标对象链接、score、模型部署版本与 `PENDING_REVIEW` 状态。`DecideRiskDisposition` 由风控人员提交，原子记录 `APPROVE` / `REJECT` / `MANUAL_REVIEW`、reason 与订单处置状态。这样风险模型建议与最终人工决策不会混为一谈。

### 11.6 Step 5：Automations——事件驱动而非人工点击

Automation 用于无需逐次人工输入的事件反应：

1. 新订单进入 `AtRiskOrders` 时，提交 `RequestRiskAssessment` Action，并通知风控；
2. `Refund` 保持 `PENDING_EXTERNAL_EXECUTION` 超过 SLA 时，提交受治理的重试 Action 或通知财务；
3. 促销到期时，提交 `ReleaseExpiredReservations` Action。

Automation 提交 Action 时仍必须通过 submission criteria，并以 automation owner 的身份运行。普通 Function effect 返回 edits 并不自动写入，持久化应经过 function-backed Action；AIP Logic effect 可返回并执行 Ontology edits，应按其独立权限与审核语义配置，不能与普通 Function effect 混为一谈。

### 11.7 Step 6：Scenarios——安全试演促销预留

运营人员从 `PromotionReservationCandidates` 创建方案，分别执行“保守 / 均衡 / 激进”预留 Actions，并调用缺货率和收入模型比较结果。

正确生命周期是：

1. 创建或复制 Scenario，形成由 Actions 和可选 Models 构成的不可变 fork；
2. 比较 KPI、库存影响与约束违反项；
3. 由运营主管选择方案；
4. 执行 **Apply scenario**：关联 Actions 的 edits 被事务性应用到主 Ontology，并重新接受权限与有效性检查；
5. 模型预测结果不会随 Apply 自动写回；若需留档，显式创建 `PromotionDecision` 或执行记录 Action。

可配置 apply Action 做附加校验或成功后处理，但它不是“merge action”。Global Branching 的 merge 属于构建者发布 schema/configuration 的流程，不等同于业务 Scenario apply。

### 11.8 Step 7：Dynamic Security 与治理边界

安全设计分层实施，而不是只写“行列权限”：

| 层 | NovaShop 策略 |
| --- | --- |
| Ontology resource metadata | 通过 project-based permissions 控制谁能发现、编辑、发布 object/link/action/interface 等定义 |
| Object / row | 区域团队仅看授权区域订单；VIP 客户仅高级客服与指定风控可见 |
| Property / column-cell | 客服看 `instrumentMasked`；支付敏感属性仅财务与风控可见 |
| Dynamic policy | 根据用户组、组织、purpose、marking/classification 与对象属性运行时计算访问 |
| Action submission | 退款金额、订单状态、库存约束、Scenario execution context 与操作者身份共同判定 |
| Agent / Automation | 最小权限 service identity；高影响 Action 默认要求人类批准或只能先写入 Scenario |
| Downstream | 导出、Webhook、日志和 OSDK 返回值离开对象查询边界后，使用 markings / classification controls 与外部系统权限继续保护 |

必须分别验证“能否读取对象”与“能否提交编辑该对象的 Action”。Submission criteria 是业务准入，不替代底层读写权限。`Cipher` 是数据表示/加密类型，不等价于列级授权。

### 11.9 Step 8：应用与 OSDK 复用同一 Ontology

| 使用者 | 入口 | 消费的 Ontology 能力 |
| --- | --- | --- |
| 客服 | `SalesOrder` Object View | 客户、支付摘要、履约、退货链接；`ApproveReturn` |
| 调度员 | Workshop 履约控制塔 | `AtRiskOrders`、候选方案 Function、`ReallocateFulfillmentWarehouse` |
| 分析师 | Object Explorer / Quiver | 区域 → 仓 → 履约单 → 运单 → 运段的语义钻取 |
| 运营 | Workshop + Scenario | 预留 Actions、模型 KPI、Apply scenario |
| 外部客服门户 | OSDK | 查询被授权订单、遍历售后链接、提交同一 Action、处理提交失败 |

OSDK 应用在 Developer Console 中只选择必要的 Ontology SDK resources，并配置 resource access restrictions；OAuth operation scopes（如 Ontology read/write）另行控制可执行的 API 操作。实际结果还受用户或 application identity 的数据权限约束。应用必须显示 submission failure message，记录调用身份和 correlation ID，不能用前端按钮隐藏来代替授权。

### 11.10 Step 9：Decision lineage 与外部一致性

Action log 提供执行记录，但不会自动保存所有候选方案、模型上下文和业务结果。本例让 `RiskReview`、`FulfillmentDecision`、`PromotionDecision` 实现 `DecisionRecord`，并使用 `ExternalExecution` 保存：

- 决策者、时间、reason 与来源应用；
- 输入对象链接，或 permanent object set 的 RID；需要严格历史重放时还要保留输入快照/版本；
- 候选方案摘要与最终选择；
- Function / Model / deployment 版本；
- Action / Scenario 标识；
- 外部执行幂等键、尝试次数和最终结果；
- 延误率、退款时长、拒付损失等事后 outcome。

这些对象让“可审计”从口号变为可查询的决策数据；在输入快照、代码/模型版本和 outcome 都可用时，才具备严格回放与学习条件。

### 11.11 Step 10：任务型验证与发布治理

#### Semantic 验证

1. 从某客户订单能否导航到订单行、SKU、履约单、仓、运单、运段、退货与退款？
2. 哪个区域延误最多，能否继续钻取到造成延误的仓和运输段？
3. 是否需要导出 Excel 或手工 join？若需要，优先检查缺失 Link、错误基数与命名。

#### Kinetic 与安全验证

1. 改仓成功路径是否原子更新全部 Ontology edits？
2. 库存不足、状态过期、版本冲突、未授权用户是否分别得到正确拒绝消息？
3. Webhook 超时、重复回调、PSP 失败时是否幂等并进入可恢复状态？
4. Automation 是否以预期身份运行并通过相同 submission criteria？
5. Scenario Apply 前后差异是否正确，模型结果是否没有被误写入生产？
6. OSDK 是否同时遵守 scope 与调用身份权限？

#### 数据、模型与治理验证

- 验证 datasource 与 user edits 的冲突策略、乱序更新、materialization 完整性；
- 固定模型版本回放风险决策，检查特征、置信度与解释；
- 人与 Agent 用同一问题集验收，比较搜索词、耗时、错误导航与工具调用；
- 在 Global Branch 中修改 Ontology，通过 proposal、检查、审批、rebase、merge 发布；
- 为 object/action/interface 指定 owner，检查依赖与 usage，建立弃用和回归测试流程。

### 11.12 Step 11：扩展而不污染核心

财务新增结算对账时，不在 `SalesOrder` 上堆 20 个可空字段；新增 `SettlementBill`、`SellerPayout`，链接 `Seller` 与相关订单。若多个类型确有共同对账契约，再定义 `Reconcilable` Interface。核心订单模型保持稳定，体现 open/closed principle。

### 11.13 覆盖检查：本案例落实了哪些关键概念

| 类别 | 关键概念 | NovaShop 落点 |
| --- | --- | --- |
| Semantic | Object type / Object / Property / Property value | 客户、订单、库存、决策与执行对象及其实例值 |
| Semantic | Link type / Link / 基数 / 双向命名 | 订单—履约—运单—售后完整导航图 |
| Semantic | Object-backed link | `FulfillmentAllocation` 承载分配数量 |
| Semantic | Shared property / Value type / Struct / 地理属性 | 审计属性、领域状态值、地址、仓库坐标 |
| Semantic | Interface | 风险评估、履约跟踪、外部执行契约 |
| Semantic | Object set | 动态队列、永久静态评审范围、临时跨应用集合 |
| Kinetic | Action parameters / rules / submission criteria / side effects | 改仓、退货、退款、预留 |
| Kinetic | Function / Ontology edit Function | 候选计算与 function-backed Action 分离 |
| Kinetic | Automation | 风险入队、退款 SLA、预留释放 |
| Kinetic | Scenario | 促销 what-if 与 Apply scenario |
| Kinetic | Dynamic security | 资源、对象、属性、Action、Agent 与下游分层控制 |
| Logic | Models / derived properties | 风险评分、需求预测、实时聚合 |
| Runtime | Datasource / OSv2 edits / conflict / materialization | WMS 权威数据、用户预留编辑、下游最新状态 |
| Toolchain | Object View / Explorer / Quiver / Workshop / OSDK | 同一模型服务人、Agent 与外部应用 |
| Governance | Metadata / branching / validation / lineage | owner、proposal、任务测试、决策对象与执行对象 |

至此，电商案例不再只是 ER 图或术语清单，而是一个覆盖 **Semantic elements + Kinetic elements + Logic + Security + Runtime + Toolchain + Governance** 的可运行决策系统参考模型。

---

## 12. 附录：术语对照与延伸阅读

### 12.1 中英术语对照

| 中文（本文用法）    | 英文（官方）                |
| ------------------- | --------------------------- |
| 本体 / Ontology     | Ontology                    |
| 对象类型            | Object type                 |
| 对象（实例）        | Object / Object instance    |
| 对象集              | Object set                  |
| 属性                | Property                    |
| 共享属性            | Shared property             |
| 链接类型 / 链接     | Link type / Link            |
| 操作类型 / 操作     | Action type / Action        |
| 提交准则            | Submission criteria         |
| 函数                | Function                    |
| 接口                | Interface                   |
| 写回 / 物化         | Writeback / Materialization |
| 场景 / 沙箱推演     | Ontology scenario           |
| 数字孪生            | Digital twin                |
| 语义要素 / 动力要素 | Semantic / Kinetic elements |
| 本体元数据服务      | OMS                         |
| 对象集服务          | OSS                         |

### 12.2 推荐阅读路径（官方文档）

1. [Ontology overview](https://www.palantir.com/docs/foundry/ontology/overview/)
2. [Why create an Ontology?](https://www.palantir.com/docs/foundry/ontology/why-ontology/)
3. [Core concepts](https://www.palantir.com/docs/foundry/ontology/core-concepts/)
4. [The Ontology system（Architecture Center）](https://www.palantir.com/docs/foundry/architecture-center/ontology-system/)
5. [Ontology architecture / Object backend](https://www.palantir.com/docs/foundry/object-backend/overview/)
6. Object & Link types、Properties、Shared properties、Interfaces
7. Action types（Rules / Parameters / Submission criteria / Side effects）
8. Functions 与 Automations
9. Dynamic security、Object permissioning 与 Classification controls
10. Ontology scenarios 与 Object edits / Materializations
11. Ontology-aware applications 与 OSDK
12. Ontology design：Best practices / Anti-patterns / Structural guidance / Validation
13. Models in the Ontology

中文入口可从 [Ontology 搭建概述](https://www.palantir.com/docs/zh/foundry/ontology/overview/) 进入；部分页面为机器翻译，**概念细节建议以英文原文为准**。

### 12.3 与领域驱动设计（DDD）的关系（简注）

Palantir 官方原则明确写入 **Domain-driven design**：Ontology 应反映领域语言与真实实体。这与 DDD 的 Ubiquitous Language、Bounded Context 精神一致。实践中仍需注意：

- Foundry 常推动**跨部门共享规范对象**（避免 Department Silos），这与「严格按团队拆上下文」的 DDD 策略需要审慎平衡——共享的是企业级规范概念，差异通过属性、链接、视图与安全表达，而不是复制实体。
- Action types 近似领域中的**命令 / 领域操作**；Submission criteria 近似不变式与权限策略。

若你同时研究 DDD，可将本文第 10–11 章作为「DDD 战略设计 → Foundry 可运行本体」的桥接材料。

---

## 结语

Foundry Ontology 的理论内核可以压缩为三句话：

1. **它是决策中心的操作层**：整合 Data、Logic、Action、Security，而不只是语义标注。
2. **它用可映射的语言描述世界**：Object / Property / Link 做名词，Action / Function / Scenario 做动词与推演。
3. **它的质量由可导航、可治理、可验证定义**：反模式清单与任务型演练，比「对象类型数量」更能衡量成功。

对初学者：先学会用业务决策驱动名词与动词，再谈管道映射与应用搭建；对进阶者：把 Interface、安全策略、Scenario 与 Agent 工具化纳入同一演进闭环——Ontology 就会从「数据目录的升级版」，成长为组织真正的数字孪生操作系统。

---

*文档版本：基于 2026 年公开 Palantir Foundry 文档整理。平台能力持续演进（如 OSv1 弃用时间线、Scenarios beta、Interfaces 各应用支持度），落地前请核对你所在 enrollment 的最新官方文档与 Upgrade Assistant。*
