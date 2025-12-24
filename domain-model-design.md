# 快递物流管理系统 - 领域模型设计文档

## 1. 概述

本文档描述快递物流管理系统的领域模型设计，旨在实现包裹全生命周期的跟踪管理，支持揽收分拣、运输配送和末端派送等核心业务流程。

## 2. 领域模型核心实体

### 2.1 包裹管理领域

#### Package (包裹)
- **职责**: 系统的核心业务对象，代表一个需要运输的包裹
- **主要属性**:
  - packageId: 包裹唯一标识
  - waybillNumber: 运单号
  - weight: 重量
  - dimensions: 尺寸（长宽高）
  - currentStatus: 当前状态
  - currentLocation: 当前位置
  - estimatedArrivalTime: 预计到达时间
- **核心行为**:
  - updateStatus(): 更新包裹状态
  - addTrace(): 添加轨迹记录
  - getTraceHistory(): 获取历史轨迹
  - markAsException(): 标记为异常

#### Waybill (运单)
- **职责**: 包裹的详细信息载体，记录寄件和收件信息
- **主要属性**:
  - waybillNumber: 运单号
  - senderInfo: 寄件人信息
  - recipientInfo: 收件人信息
  - originSite: 寄件网点
  - destinationSite: 目的网点
  - createdTime: 创建时间
  - packageInfo: 关联的包裹信息
- **核心行为**:
  - validate(): 验证运单信息完整性
  - print(): 打印运单

#### PackageTrace (包裹轨迹)
- **职责**: 记录包裹在运输过程中的每个节点信息
- **主要属性**:
  - traceId: 轨迹ID
  - packageId: 包裹ID
  - location: 位置
  - status: 状态
  - timestamp: 时间戳
  - operator: 操作人
  - description: 描述信息

### 2.2 客户管理领域

#### Customer (客户)
- **职责**: 代表寄件人或收件人
- **主要属性**:
  - customerId: 客户ID
  - name: 姓名
  - phoneNumber: 联系电话
  - address: 地址
- **核心行为**:
  - createWaybill(): 创建运单
  - queryPackageStatus(): 查询包裹状态

#### Address (地址)
- **职责**: 标准化的地址信息
- **主要属性**:
  - province: 省
  - city: 市
  - district: 区
  - street: 街道
  - detailAddress: 详细地址
  - postalCode: 邮编
- **核心行为**:
  - validateAddress(): 验证地址有效性
  - getFullAddress(): 获取完整地址字符串

### 2.3 网点与站点领域

#### Site (网点)
- **职责**: 揽收和派送的服务站点
- **主要属性**:
  - siteId: 网点ID
  - siteName: 网点名称
  - address: 地址
  - siteType: 网点类型（营业网点/分拣中心）
  - capacity: 容量
- **核心行为**:
  - acceptPackage(): 接收包裹
  - sortPackage(): 分拣包裹
  - assignDeliveryTask(): 分配派送任务

#### TransferStation (中转站)
- **职责**: 中转分拣站点，处理跨区域包裹的中转
- **主要属性**:
  - stationId: 中转站ID
  - stationName: 中转站名称
  - location: 位置
  - sortingRules: 分拣规则
  - capacity: 容量
- **核心行为**:
  - sortPackages(): 批量分拣包裹
  - generateTransportTasks(): 生成运输任务

### 2.4 运输调度领域

#### RoutePlan (路径规划)
- **职责**: 根据目的地规划包裹的运输路径
- **主要属性**:
  - routeId: 路径ID
  - origin: 起点
  - destination: 终点
  - intermediateStations: 中转站列表
  - estimatedTime: 预计时间
  - distance: 距离
- **核心行为**:
  - calculateRoute(): 计算最优路径
  - updateRoute(): 更新路径（处理路线变更）
  - getNextStation(): 获取下一站

#### TransportTask (运输任务)
- **职责**: 运输调度的基本单元，记录一次运输任务的详细信息
- **主要属性**:
  - taskId: 任务ID
  - packageList: 包裹列表
  - originStation: 出发站点
  - destinationStation: 目的站点
  - assignedVehicle: 分配的车辆
  - assignedDriver: 分配的司机
  - plannedRoute: 规划路线
  - taskStatus: 任务状态
  - scheduledTime: 计划发车时间
  - actualDepartureTime: 实际发车时间
  - actualArrivalTime: 实际到达时间
- **核心行为**:
  - assignVehicleAndDriver(): 分配车辆和司机
  - startTransport(): 开始运输
  - completeTransport(): 完成运输
  - handleRouteChange(): 处理路线变更

#### Vehicle (车辆)
- **职责**: 运输工具
- **主要属性**:
  - vehicleId: 车辆ID
  - vehicleNumber: 车牌号
  - vehicleType: 车辆类型
  - capacity: 载重量
  - maxVolume: 最大容积
  - currentStatus: 当前状态（空闲/使用中/维修中）
- **核心行为**:
  - checkAvailability(): 检查可用性
  - calculateLoadRate(): 计算装载率

#### Driver (司机)
- **职责**: 执行运输任务的人员
- **主要属性**:
  - driverId: 司机ID
  - name: 姓名
  - phoneNumber: 联系电话
  - licenseNumber: 驾照号
  - currentStatus: 当前状态（空闲/运输中/休息中）
- **核心行为**:
  - viewTransportTask(): 查看运输任务
  - startTask(): 开始任务
  - completeTask(): 完成任务
  - reportRouteChange(): 报告路线变更

### 2.5 派送领域

#### DeliveryTask (派送任务)
- **职责**: 派送员的派送任务
- **主要属性**:
  - taskId: 任务ID
  - packageList: 包裹列表
  - assignedCourier: 分配的派送员
  - deliveryArea: 派送区域
  - taskStatus: 任务状态
  - scheduledDate: 计划派送日期
- **核心行为**:
  - assignCourier(): 分配派送员
  - optimizeDeliveryRoute(): 优化派送路线
  - completeDelivery(): 完成派送

#### Courier (派送员)
- **职责**: 执行末端派送的人员
- **主要属性**:
  - courierId: 派送员ID
  - name: 姓名
  - phoneNumber: 联系电话
  - assignedArea: 负责区域
  - currentStatus: 当前状态
- **核心行为**:
  - viewDeliveryTasks(): 查看派送任务列表
  - deliverPackage(): 派送包裹
  - confirmSignature(): 确认签收
  - reportException(): 报告派送异常

#### SignatureRecord (签收记录)
- **职责**: 记录包裹签收信息
- **主要属性**:
  - recordId: 记录ID
  - packageId: 包裹ID
  - signatureTime: 签收时间
  - signaturePerson: 签收人
  - signatureImage: 签收照片
  - courierId: 派送员ID

### 2.6 异常处理领域

#### ExceptionRecord (异常记录)
- **职责**: 记录和处理各类异常情况
- **主要属性**:
  - exceptionId: 异常ID
  - packageId: 包裹ID
  - exceptionType: 异常类型（分拣异常/运输异常/派送异常）
  - exceptionReason: 异常原因
  - reportTime: 报告时间
  - reportPerson: 报告人
  - handlingStatus: 处理状态
  - resolutionPlan: 解决方案
  - resolvedTime: 解决时间
- **核心行为**:
  - createException(): 创建异常记录
  - assignHandler(): 分配处理人
  - resolveException(): 解决异常
  - notifyCustomer(): 通知客户

## 3. 核心业务流程支持

### 3.1 正常流程支持

**流程1-2: 客户寄送包裹，录入运单**
- Customer创建Waybill，包含收件人Address、包裹重量尺寸等
- Site打印运单号，创建Package对象

**流程3: 路径规划**
- RoutePlan根据Waybill的destination进行路径规划
- 确定intermediateStations（中转站列表）

**流程4: 分拣并生成运输任务**
- Site根据RoutePlan确定Package的下一站
- 进行分拣操作，更新Package状态为"分拣中"
- 为该批次包裹生成TransportTask

**流程5: 运输调度**
- 调度员根据包裹量、Vehicle容量、RoutePlan等因素
- 为TransportTask分配Vehicle和Driver

**流程6: 运输执行**
- Driver按照plannedRoute运输包裹
- 到达下一站或目的网点，更新PackageTrace

**流程7.1: 中转站分拣**
- Package到达TransferStation
- TransferStation根据sortingRules进行分拣
- 重复流程4

**流程7.2: 到达目的网点派送**
- Package到达destination Site
- Site创建DeliveryTask并分配给Courier
- Courier根据Waybill信息进行派送

**流程8: 签收**
- Courier确认收件人签收
- 创建SignatureRecord
- Package状态更新为"已签收"

### 3.2 扩展流程支持

**扩展1: 包裹分拣异常**
- Site在分拣时发现异常（地址不清晰、标签损坏等）
- 创建ExceptionRecord，类型为"分拣异常"
- Package状态标记为"异常"
- ExceptionRecord.assignHandler()分配处理人
- 处理人联系发件人确认或重新核对信息
- ExceptionRecord.resolveException()解决后恢复正常流程

**扩展2: 运输路线变更**
- Driver或调度员发现路况异常（天气、交通管制等）
- Driver.reportRouteChange()报告需要变更
- RoutePlan.updateRoute()重新规划路线
- TransportTask.handleRouteChange()更新任务信息
- 通知相关Driver和下游Site

**扩展3: 派送异常处理**
- Courier在派送时遇到问题（收件人不在、地址错误、包裹破损等）
- Courier.reportException()创建ExceptionRecord，类型为"派送异常"
- Package状态标记为"派送异常"
- 系统通知客户，根据情况安排：
  - 二次派送：重新创建DeliveryTask
  - 联系客户：更新Address信息
  - 退回包裹：创建返程TransportTask

### 3.3 系统基本能力支持

**能力1: 包裹状态管理与追溯**
- Package.currentStatus属性提供当前状态
- Package.getTraceHistory()返回PackageTrace列表
- PackageTrace记录每个节点的location、status、timestamp
- Package.currentLocation显示当前位置
- RoutePlan.getNextStation()显示下一站
- Package.estimatedArrivalTime显示预计到达时间

**能力2: 运输调度与任务管理**
- Driver.viewTransportTask()查看TransportTask列表
- TransportTask包含：
  - plannedRoute: 路线规划详情
  - packageList: 车辆装载清单（包裹数量）
  - originStation和destinationStation
  - scheduledTime: 发车时间

**能力3: 派送任务执行支持**
- Courier.viewDeliveryTasks()查看DeliveryTask列表
- DeliveryTask包含packageList
- 每个Package关联Waybill，包含：
  - recipientInfo.name: 收件人姓名
  - recipientInfo.address: 详细地址
  - recipientInfo.phoneNumber: 联系电话
  - currentStatus: 包裹状态
- DeliveryTask.optimizeDeliveryRoute()提供派送路线建议

## 4. 领域模型关系说明

### 4.1 核心关联关系

1. **Package ↔ Waybill**: 一对一关系，每个包裹对应一个运单
2. **Package ↔ PackageTrace**: 一对多关系，一个包裹有多条轨迹记录
3. **Package ↔ ExceptionRecord**: 一对多关系，一个包裹可能有多个异常记录
4. **TransportTask ↔ Package**: 一对多关系，一个运输任务包含多个包裹
5. **TransportTask ↔ Vehicle**: 多对一关系，一个车辆可以执行多个任务
6. **TransportTask ↔ Driver**: 多对一关系，一个司机可以执行多个任务
7. **TransportTask ↔ RoutePlan**: 多对一关系，一个路径规划可以被多个任务使用
8. **DeliveryTask ↔ Package**: 一对多关系，一个派送任务包含多个包裹
9. **DeliveryTask ↔ Courier**: 多对一关系，一个派送员可以执行多个任务
10. **Waybill ↔ Customer**: 多对一关系（寄件人、收件人）

### 4.2 聚合关系

- **包裹管理聚合**: Package作为聚合根，聚合Waybill、PackageTrace
- **运输任务聚合**: TransportTask作为聚合根，聚合RoutePlan、相关Package引用
- **派送任务聚合**: DeliveryTask作为聚合根，聚合相关Package引用

## 5. 关键设计决策

### 5.1 状态管理
Package的状态通过枚举PackageStatus管理，包括：
- COLLECTED（已揽收）
- SORTING（分拣中）
- IN_TRANSIT（运输中）
- OUT_FOR_DELIVERY（派送中）
- DELIVERED（已签收）
- EXCEPTION（异常）
- RETURNED（已退回）

### 5.2 路径规划策略
RoutePlan支持动态路径规划和路线变更，采用策略模式可以支持不同的路径规划算法（最短路径、最快路径、成本最优等）。

### 5.3 异常处理机制
统一的ExceptionRecord实体处理所有类型的异常，通过exceptionType区分，支持完整的异常处理生命周期。

### 5.4 任务调度
TransportTask和DeliveryTask分离，体现运输和派送两个不同的业务阶段，便于独立管理和优化。

## 6. 模型验证

### 6.1 主流程验证
✓ 支持客户寄送包裹流程（Customer → Waybill → Package）  
✓ 支持路径规划（RoutePlan）  
✓ 支持分拣流程（Site.sortPackage）  
✓ 支持运输调度（TransportTask + Vehicle + Driver）  
✓ 支持中转站处理（TransferStation）  
✓ 支持派送流程（DeliveryTask + Courier）  
✓ 支持签收确认（SignatureRecord）

### 6.2 扩展流程验证
✓ 支持分拣异常处理（ExceptionRecord + Package.markAsException）  
✓ 支持运输路线变更（RoutePlan.updateRoute + TransportTask.handleRouteChange）  
✓ 支持派送异常处理（ExceptionRecord + 二次派送/退回机制）

### 6.3 基本能力验证
✓ 包裹状态管理与追溯（Package + PackageTrace + currentStatus）  
✓ 运输调度与任务管理（TransportTask + Driver.viewTransportTask）  
✓ 派送任务执行支持（DeliveryTask + Courier.viewDeliveryTasks）

## 7. 后续迭代考虑

- 支持计费系统（根据重量、距离计算运费）
- 支持客户服务系统（投诉、咨询）
- 支持数据分析和报表（运输效率、异常率统计）
- 支持库存管理（包裹在各站点的存储管理）
- 支持智能调度算法（机器学习优化路径和任务分配）

## 8. 技术实现建议

### 8.1 分层架构
- **领域层**: 实现上述所有领域实体和业务逻辑
- **应用层**: 实现用例编排和事务管理
- **基础设施层**: 实现持久化、消息队列、外部服务集成
- **接口层**: 提供REST API、Web界面等

### 8.2 设计模式建议
- **策略模式**: 路径规划算法、分拣规则
- **状态模式**: 包裹状态流转
- **工厂模式**: 创建不同类型的Task
- **观察者模式**: 状态变更通知
- **责任链模式**: 异常处理流程

### 8.3 技术栈建议
- **框架**: Spring Boot
- **持久化**: Spring Data JPA + MySQL
- **缓存**: Redis（热点包裹查询）
- **消息队列**: RabbitMQ/Kafka（异步处理、状态通知）
- **搜索**: Elasticsearch（包裹轨迹查询）

---

**文档版本**: 1.0  
**最后更新**: 2025-12-22  
**作者**: Lab3小组


