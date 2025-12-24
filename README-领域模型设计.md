# 快递物流管理系统 - 领域模型设计文档

## 📋 项目概述

本项目是针对**Lab3-1-1 快递物流管理**的领域模型设计，目标是构建一个完整的快递物流管理系统，实现包裹全生命周期的跟踪管理，支持揽收分拣、运输配送和末端派送等核心业务流程。

## 📁 文档结构

```
DesignPatternLab/
├── lab3-1-1.md                    # Lab需求文档（原始需求）
├── domain-model-design.md         # 领域模型设计文档（核心文档）
├── domain-model.puml              # 领域模型类图（PlantUML代码）
├── sequence-diagrams.puml         # 业务流程时序图（PlantUML代码）
├── model-verification.md          # 模型验证报告
└── README-领域模型设计.md         # 本文档
```

## 📖 文档说明

### 1. domain-model-design.md（核心设计文档）

**内容**:
- 领域模型核心实体详细定义（18个实体）
- 实体的职责、属性和行为
- 核心业务流程支持说明
- 扩展流程支持说明
- 领域模型关系说明
- 关键设计决策
- 技术实现建议

**适用场景**:
- 理解整个系统的领域模型设计
- 查看每个实体的详细定义
- 了解如何支持各种业务流程

### 2. domain-model.puml（类图）

**内容**:
- 完整的领域模型类图
- 包含18个核心实体
- 10个枚举类型
- 实体之间的关系（聚合、组合、关联）
- 按领域划分为6个包：
  - 包裹管理领域
  - 客户管理领域
  - 网点与站点领域
  - 运输调度领域
  - 派送领域
  - 异常处理领域

**如何查看**:
1. 使用PlantUML插件（推荐）
2. 在线预览：http://www.plantuml.com/plantuml/
3. VS Code插件：安装"PlantUML"扩展
4. IDEA插件：安装"PlantUML Integration"

**使用方法**:
```bash
# 如果安装了PlantUML命令行工具
java -jar plantuml.jar domain-model.puml
# 会生成 domain-model.png
```

### 3. sequence-diagrams.puml（时序图）

**内容**:
- 7个核心业务流程的时序图
- 包裹从揽收到签收的完整流程
- 3个扩展流程（分拣异常、路线变更、派送异常）
- 3个查询功能（包裹追溯、运输任务查询、派送任务查询）

**包含的时序图**:
1. 流程1：包裹揽收到派送全流程
2. 流程2：包裹分拣异常处理
3. 流程3：运输路线变更
4. 流程4：派送异常处理
5. 流程5：包裹状态查询与追溯
6. 流程6：司机查看运输任务
7. 流程7：派送员查看派送任务

**如何查看**:
- 与类图相同，使用PlantUML工具查看

### 4. model-verification.md（验证报告）

**内容**:
- 对照Lab需求进行逐项验证
- 验证系统目标（1项）
- 验证正常流程（8个步骤）
- 验证扩展流程（3个场景）
- 验证系统基本能力（3个功能模块）
- 验证总结和结论

**验证结果**:
✅ **100%通过** - 所有需求都有对应的实体和方法支持

## 🎯 核心领域实体

### 包裹管理（3个实体）
- **Package**: 包裹（核心实体）
- **Waybill**: 运单
- **PackageTrace**: 包裹轨迹

### 客户管理（2个实体）
- **Customer**: 客户
- **Address**: 地址（值对象）

### 网点与站点（4个实体）
- **Station**: 站点（抽象类）
- **Site**: 网点
- **TransferStation**: 中转站
- **SortingRule**: 分拣规则（值对象）

### 运输调度（4个实体）
- **RoutePlan**: 路径规划
- **TransportTask**: 运输任务（聚合根）
- **Vehicle**: 车辆
- **Driver**: 司机

### 派送管理（3个实体）
- **DeliveryTask**: 派送任务（聚合根）
- **Courier**: 派送员
- **SignatureRecord**: 签收记录

### 异常处理（1个实体）
- **ExceptionRecord**: 异常记录

### 支持实体（1个实体）
- **Location**: 位置信息（值对象）

**总计**: 18个核心实体 + 10个枚举类型

## 🔄 核心业务流程

### 正常流程（8步）
1. ✅ 客户寄送包裹
2. ✅ 录入运单信息并打印
3. ✅ 路径规划
4. ✅ 分拣并生成运输任务
5. ✅ 运输调度（分配车辆和司机）
6. ✅ 司机运输包裹
7. ✅ 中转站分拣或派送
8. ✅ 收件人签收

### 扩展流程（3个场景）
1. ✅ 包裹分拣异常处理
2. ✅ 运输路线变更
3. ✅ 派送异常处理（收件人不在、地址错误、包裹破损）

### 系统基本能力（3个功能）
1. ✅ 包裹状态管理与追溯
2. ✅ 运输调度与任务管理
3. ✅ 派送任务执行支持

## 🛠️ 如何使用PlantUML图

### 方法1: VS Code（推荐）

1. 安装VS Code扩展"PlantUML"
2. 打开`.puml`文件
3. 按`Alt+D`预览图表
4. 或右键选择"Preview Current Diagram"

### 方法2: IntelliJ IDEA

1. 安装插件"PlantUML Integration"
2. 打开`.puml`文件
3. 会自动在右侧显示图表

### 方法3: 在线预览

1. 访问 http://www.plantuml.com/plantuml/
2. 复制`.puml`文件内容
3. 粘贴到在线编辑器
4. 即可查看生成的图表

### 方法4: 命令行生成图片

```bash
# 安装PlantUML（需要Java环境）
# 下载 plantuml.jar

# 生成PNG图片
java -jar plantuml.jar domain-model.puml
java -jar plantuml.jar sequence-diagrams.puml

# 生成SVG（矢量图，推荐）
java -jar plantuml.jar -tsvg domain-model.puml
java -jar plantuml.jar -tsvg sequence-diagrams.puml
```

## 💡 设计亮点

### 1. 领域驱动设计（DDD）
- 使用聚合根（TransportTask、DeliveryTask）管理生命周期
- 值对象（Address、Location、SortingRule）保证不可变性
- 明确的边界上下文划分

### 2. 单一职责原则
- 每个实体职责清晰
- 运输和派送分离
- 网点和中转站分离

### 3. 开闭原则
- 使用策略模式支持多种路径规划算法
- 枚举类型便于扩展新的状态和类型

### 4. 状态模式
- PackageStatus定义清晰的状态流转
- TaskStatus管理任务生命周期

### 5. 异常处理机制
- 统一的ExceptionRecord处理所有异常
- 支持完整的异常处理生命周期
- 可追溯、可恢复

## 📊 模型统计

| 类型 | 数量 | 说明 |
|------|------|------|
| 核心实体 | 18 | 业务领域对象 |
| 枚举类型 | 10 | 状态和类型定义 |
| 聚合根 | 2 | TransportTask, DeliveryTask |
| 值对象 | 4 | Address, Location, SortingRule等 |
| 领域包 | 6 | 按业务领域划分 |

## 🎓 学习建议

### 阶段1: 理解需求（30分钟）
1. 阅读`lab3-1-1.md`（原始需求）
2. 理解系统目标和业务流程

### 阶段2: 学习设计（1-2小时）
1. 阅读`domain-model-design.md`（核心设计文档）
2. 理解每个实体的职责和关系
3. 查看`domain-model.puml`类图，建立整体认识

### 阶段3: 理解流程（1小时）
1. 查看`sequence-diagrams.puml`时序图
2. 理解各个业务流程如何通过实体协作完成
3. 重点关注正常流程和异常处理

### 阶段4: 验证理解（30分钟）
1. 阅读`model-verification.md`验证报告
2. 对照需求检查每个功能点的实现
3. 确认理解正确

### 阶段5: 准备实现（可选）
1. 思考如何用Java代码实现这些实体
2. 考虑使用Spring Boot + JPA的技术栈
3. 规划数据库表结构

## 🚀 后续实现计划

### 第一阶段：核心实体实现
- [ ] 定义Java实体类（使用JPA注解）
- [ ] 实现枚举类型
- [ ] 配置实体关系映射

### 第二阶段：基础服务实现
- [ ] PackageService（包裹管理）
- [ ] WaybillService（运单管理）
- [ ] TransportTaskService（运输任务）
- [ ] DeliveryTaskService（派送任务）

### 第三阶段：业务流程实现
- [ ] 包裹揽收流程
- [ ] 路径规划算法
- [ ] 分拣和调度流程
- [ ] 派送和签收流程

### 第四阶段：异常处理实现
- [ ] 分拣异常处理
- [ ] 运输路线变更
- [ ] 派送异常处理

### 第五阶段：查询功能实现
- [ ] 包裹状态查询API
- [ ] 轨迹追溯API
- [ ] 任务管理API

## 📝 技术栈建议

### 后端
- **框架**: Spring Boot 3.x
- **持久化**: Spring Data JPA + Hibernate
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RabbitMQ或Kafka
- **API文档**: Swagger/OpenAPI

### 前端（可选）
- **框架**: Vue 3 或 React
- **UI组件**: Element Plus 或 Ant Design
- **地图**: 高德地图或百度地图

### 工具
- **构建**: Maven或Gradle
- **版本控制**: Git
- **代码质量**: SonarQube
- **测试**: JUnit 5 + Mockito

## ❓ 常见问题

### Q1: 为什么分离TransportTask和DeliveryTask？
A: 运输和派送是两个不同的业务阶段，有不同的参与者（司机 vs 派送员）、不同的管理方式（车辆调度 vs 末端派送）、不同的优化目标（长途运输 vs 最后一公里）。分离后便于独立管理和优化。

### Q2: 为什么需要TransferStation和Site两个类？
A: TransferStation（中转站）专注于大规模分拣和中转，Site（网点）专注于揽收和派送。虽然都继承自Station，但业务重点不同。中转站处理批量包裹的分拣和转运，网点处理与客户的直接交互。

### Q3: ExceptionRecord如何处理不同类型的异常？
A: 使用exceptionType枚举区分异常类型（分拣异常、运输异常、派送异常等），使用handlingStatus跟踪处理进度。不同类型的异常有不同的resolutionPlan（解决方案），但都遵循统一的异常处理生命周期。

### Q4: 路径规划支持哪些算法？
A: 设计中定义了PlanType枚举（SHORTEST_PATH、FASTEST_PATH、COST_OPTIMAL），支持策略模式扩展。实现时可以使用Dijkstra算法（最短路径）、A*算法（最快路径）或自定义的成本优化算法。

### Q5: 如何保证包裹状态的一致性？
A: 通过PackageTrace记录每次状态变更，形成不可变的历史记录。Package.updateStatus()方法在更新状态时自动添加轨迹记录。使用数据库事务保证状态更新和轨迹记录的原子性。

## 📧 联系方式

如有问题，请联系：
- 小组成员
- 课程助教

## 📄 版本信息

- **文档版本**: 1.0
- **创建日期**: 2025-12-22
- **Lab**: Lab3-1-1 快递物流管理
- **课程**: 高级软件工程

---

**注意**: 本文档是Lab第一阶段的领域模型设计，第二阶段会有需求迭代，届时需要更新模型。


