# 快递物流管理系统领域模型说明与验证

> 说明：本文件基于“小组第一阶段”需求，对快递物流系统的领域模型进行文字化说明，并从“主流程 + 扩展流程 + 三大核心能力”三个角度进行模型验证。

---

## 一、领域模型说明

### 1. 基础参与者与单据

#### 1.1 Customer（寄件人 / 客户）

- **含义**：发起寄件业务的用户（个人或企业）。
- **关键属性**：
  - `customerId`
  - `name`
  - `phone`
  - `address`
- **关系**：
  - `Customer 1 —— * Waybill`  
    一个客户可以创建多个运单。

---

#### 1.2 Consignee（收件人）

- **含义**：接收包裹的用户。
- **关键属性**：
  - `consigneeId`
  - `name`
  - `phone`
  - `address`
- **关系**：
  - `Waybill 1 —— 1 Consignee`  
    一个运单对应一个收件人。

---

#### 1.3 Waybill（运单）

- **含义**：一次寄件业务的核心单据，记录寄件人和收件人信息以及寄递需求。
- **关键属性**：
  - `waybillNo`：运单号（唯一）
  - `createdTime`：创建时间
  - `status`：整体状态（如：已揽收 / 运输中 / 派送中 / 已签收 / 异常 / 已退回 等）
  - `originAddress`：寄件地址
  - `destinationAddress`：收件地址（也可由 Consignee.address 承担）
  - `totalWeight`：总重量
  - `totalVolume`：总体积
- **关系**：
  - `Customer 1 —— * Waybill`
  - `Waybill 1 —— 1 Consignee`
  - `Waybill 1 —— * Package`  
    一个运单可包含一个或多个包裹。

---

### 2. 包裹与状态追踪

#### 2.1 Package（包裹）

- **含义**：实际被快递网络运输的物理包裹，是系统追踪的核心对象。
- **关键属性**：
  - `packageId`：内部包裹编号（可与运单号关联）
  - `weight`：重量
  - `volume`：体积
  - `currentStatus`：当前状态（如：已揽收 / 分拣中 / 运输中 / 中转中 / 派送中 / 已签收 / 分拣异常 / 运输异常 / 派送异常 / 退回中 / 已退回 等）
  - `currentStation`：当前所在站点引用（Station）
- **关系**：
  - `Waybill 1 —— * Package`
  - `Package 1 —— * TrackingEvent`
  - `Package 1 —— * ExceptionRecord`
  - `Package * —— * TransportTask`（包裹可能参与多次运输任务）
  - `Package * —— * DeliveryTask`（包裹可能经历多次派送任务，如二次派送）

---

#### 2.2 TrackingEvent（轨迹记录）

- **含义**：记录包裹在某时间、地点发生的一次业务事件，用于支持轨迹追踪。
- **关键属性**：
  - `eventId`
  - `packageId`
  - `eventTime`
  - `eventType`：揽收 / 到达站点 / 离开站点 / 分拣完成 / 交付派送员 / 派送开始 / 派送失败 / 签收 / 退回等
  - `description`：描述信息
- **关系**：
  - `Package 1 —— * TrackingEvent`
  - `Station 1 —— * TrackingEvent`
  - `TransportTask 1 —— * TrackingEvent`（如“装车发车”、“到站卸车”）
  - `DeliveryTask 1 —— * TrackingEvent`（如“开始派送”、“签收完成”）

---

#### 2.3 ExceptionRecord（异常记录）

- **含义**：记录包裹在分拣、运输、派送过程中出现的异常及处理结果。
- **关键属性**：
  - `exceptionId`
  - `packageId`
  - `exceptionType`：分拣异常 / 运输异常 / 派送异常
  - `reason`：如地址不清晰、标签损坏、天气原因、交通管制、收件人不在家、包裹破损等
  - `recordTime`：记录时间
  - `status`：待处理 / 处理中 / 已处理
  - `resolution`：处理措施（更正地址、二次派送、退回寄件人等）
- **关系**：
  - `Package 1 —— * ExceptionRecord`
  - `Station 1 —— * ExceptionRecord`（发生异常的站点）
  - `TransportTask 1 —— * ExceptionRecord`（运输过程中产生的异常）
  - `DeliveryTask 1 —— * ExceptionRecord`（派送过程中产生的异常）

---

### 3. 站点与分拣 / 路线规划

#### 3.1 Station（站点 / 网点 / 中转站）

- **含义**：快递公司线下的物理节点（揽收网点、中转中心、派送网点等）。
- **关键属性**：
  - `stationId`
  - `name`
  - `type`：BRANCH（揽收网点）/ TRANSFER_CENTER（中转中心）/ HUB / DELIVERY_STATION（派送网点）等
  - `address`
- **关系**：
  - `Station 1 —— * SortingRule`
  - `Station 1 —— * TransportTask`（作为起点或终点）
  - `Station 1 —— * DeliveryTask`（隶属的派送任务）
  - `Station 1 —— * TrackingEvent`
  - `Station 1 —— * ExceptionRecord`

---

#### 3.2 SortingRule（分拣规则）

- **含义**：站点对包裹进行分拣时所使用的业务规则。
- **关键属性**：
  - `ruleId`
  - `description`：规则描述，如“发往北京的包裹统一走华北中转中心”
  - `criteria`：匹配条件（目的地区域、重量区间、优先级等，可简单保存为字符串）
- **关系**：
  - `Station 1 —— * SortingRule`  
    某站点可以拥有多条不同的分拣规则。

---

#### 3.3 Route（运输路线模板）

- **含义**：从起点站到终点站的一条预规划路线，可能由多段组成。
- **关键属性**：
  - `routeId`
  - `name`：如“沪-京标准路线”
  - `originStation`
  - `destinationStation`
- **关系**：
  - `Route 1 —— * RouteSegment`
  - `Route 1 —— * TransportTask`（运输任务可引用所使用的路线）

---

#### 3.4 RouteSegment（路线段）

- **含义**：Route 中的一段，如“上海分拣中心 → 南京中转站”。
- **关键属性**：
  - `segmentId`
  - `sequenceNo`：在整条路线中的顺序
- **关系**：
  - `Route 1 —— * RouteSegment`
  - `RouteSegment * —— 1 Station（fromStation）`
  - `RouteSegment * —— 1 Station（toStation）`

---

### 4. 运输调度与执行

#### 4.1 Vehicle（车辆）

- **含义**：执行运输任务的车辆。
- **关键属性**：
  - `vehicleId`
  - `plateNumber`
  - `capacityWeight`
  - `capacityVolume`
  - `status`：可用 / 运输中 / 维护中
- **关系**：
  - `Vehicle 1 —— * TransportTask`  
    一辆车可以执行多次运输任务。

---

#### 4.2 Driver（司机）

- **含义**：负责驾驶车辆完成运输任务的人员。
- **关键属性**：
  - `driverId`
  - `name`
  - `phone`
  - `licenseNo`
- **关系**：
  - `Driver 1 —— * TransportTask`

---

#### 4.3 Dispatcher（调度员）

- **含义**：负责制定和调整运输计划与任务的人员。
- **关键属性**：
  - `dispatcherId`
  - `name`
- **关系**：
  - `Dispatcher 1 —— * TransportTask`
  - `Dispatcher 1 —— * RouteChangeRecord`

---

#### 4.4 TransportTask（运输任务）

- **含义**：一次从某站点到另一站点的中长距离运输计划及执行记录，是司机和车辆工作的核心任务单元。
- **关键属性**：
  - `transportTaskId`
  - `plannedDepartureTime`
  - `plannedArrivalTime`
  - `actualDepartureTime`
  - `actualArrivalTime`
  - `status`：待装车 / 运输中 / 已到达 / 已取消等
- **关系**：
  - `Station 1 —— * TransportTask（fromStation）`
  - `Station 1 —— * TransportTask（toStation）`
  - `Vehicle 1 —— * TransportTask`
  - `Driver 1 —— * TransportTask`
  - `Dispatcher 1 —— * TransportTask`
  - `TransportTask * —— * Package`（某次运输任务可以装载多个包裹）
  - `Route 1 —— * TransportTask`（可选：任务基于哪条规划路线）
  - `TransportTask 1 —— * RouteChangeRecord`
  - `TransportTask 1 —— * TrackingEvent`
  - `TransportTask 1 —— * ExceptionRecord`

---

#### 4.5 RouteChangeRecord（路线变更记录）

- **含义**：记录运输任务执行过程中，由于天气、交通管制等导致路线调整的事件。
- **关键属性**：
  - `changeId`
  - `changeTime`
  - `reason`
  - `oldRouteInfo`
  - `newRouteInfo`
- **关系**：
  - `TransportTask 1 —— * RouteChangeRecord`
  - `Dispatcher 1 —— * RouteChangeRecord`

---

### 5. 末端派送与派送员

#### 5.1 Courier（派送员 / 快递员）

- **含义**：负责末端“最后一公里”派送的人员。
- **关键属性**：
  - `courierId`
  - `name`
  - `phone`
- **关系**：
  - `Station 1 —— * Courier`（派送员归属某个派送网点）
  - `Courier 1 —— * DeliveryTask`（一个派送员可承担多个派送任务）

---

#### 5.2 DeliveryTask（派送任务）

- **含义**：末端派送环节中，分配给派送员的一批派送工作。一次 DeliveryTask 可以包含一个或多个包裹，可能是一次派送尝试，也可能是二次派送。
- **关键属性**：
  - `deliveryTaskId`
  - `plannedTimeWindow`：计划派送时间窗口
  - `status`：待派送 / 派送中 / 派送失败 / 已签收 / 退回中 / 已退回 等
  - `routeHint`：系统计算的派送路线建议（可为文本描述）
  - （可选加强）`attemptNo`：对同一包裹的第几次派送
  - （可选加强）`actualFinishTime`：本次派送任务结束时间
- **关系**：
  - `Station 1 —— * DeliveryTask`（所属派送网点）
  - `Courier 1 —— * DeliveryTask`
  - `DeliveryTask * —— * Package`
  - `DeliveryTask 1 —— * TrackingEvent`
  - `DeliveryTask 1 —— * ExceptionRecord`

---

## 二、模型验证说明

本节从三个维度验证领域模型是否支持题目要求：

1. 正常业务主流程（1–8 步）
2. 三类扩展 / 异常流程
3. 系统需提供的三类核心能力

---

### 1. 对照正常流程（用例 1–8 步）

#### 步骤 1：客户通过网点寄送包裹

- **需求描述**：客户发起寄件，在网点提交包裹。
- **相关实体**：
  - `Customer`
  - `Station`（作为揽收网点）
- **模型支撑说明**：
  - Customer 作为发件人参与业务；
  - Station 表示受理寄件的网点，为后续揽收事件的发生地点。

---

#### 步骤 2：填写运单信息并打印运单号

- **需求描述**：录入收件人、地址、重量尺寸，生成运单号。
- **相关实体**：
  - `Customer`
  - `Consignee`
  - `Waybill`
  - `Package`
- **模型支撑说明**：
  - Waybill 记录寄件人与收件人信息、起止地址和总体特征；
  - Consignee 记录收件人详细信息；
  - Package 记录具体包裹的重量、体积等；
  - Customer 1 —— * Waybill，Waybill 1 —— * Package 映射“一个客户可多次寄件；一次寄件可有多个包裹”。

---

#### 步骤 3：系统根据目的地进行路径规划

- **需求描述**：依据目的地址规划运输路由。
- **相关实体**：
  - `Waybill`
  - `Route`
  - `RouteSegment`
  - `Station`
- **模型支撑说明**：
  - 根据 Waybill.destinationAddress，选择合适的 Route；
  - Route 由多个 RouteSegment 构成，指定经过的 Station；
  - 后续 TransportTask 将据此创建具体运输任务。

---

#### 步骤 4：根据路径和当前站点进行分拣，生成运输任务

- **需求描述**：在当前站点分拣，并为包裹生成下一步运输任务。
- **相关实体**：
  - `Station`
  - `SortingRule`
  - `Package`
  - `Route`
  - `TransportTask`
- **模型支撑说明**：
  - Station 拥有多条 SortingRule，按目的地 / 路线等决定包裹下一站；
  - 一旦确定下一站及路径，创建 TransportTask（fromStation → toStation），并把对应 Package 加入该任务；
  - 包裹状态可由 `currentStatus` 变更为“分拣中 / 待装车 / 运输中”，对应 TrackingEvent 记录事件。

---

#### 步骤 5：调度员安排车辆和司机

- **需求描述**：根据包裹量、车辆容量、路线等，分配合适的车和司机。
- **相关实体**：
  - `Dispatcher`
  - `TransportTask`
  - `Vehicle`
  - `Driver`
- **模型支撑说明**：
  - Dispatcher 负责创建和调整 TransportTask；
  - 每个 TransportTask 关联一个 Vehicle 和一个 Driver；
  - Vehicle 提供载重和容积能力，用于控制装载量；
  - Driver 执行实际驾驶工作。

---

#### 步骤 6：司机按路线运输包裹

- **需求描述**：司机沿着既定路线从当前站点运输到下一中转站或目的网点。
- **相关实体**：
  - `TransportTask`
  - `Vehicle`
  - `Driver`
  - `Route` / `RouteSegment`
  - `Package`
  - `TrackingEvent`
- **模型支撑说明**：
  - TransportTask.status 从“待装车”变为“运输中”，直到“已到达”；
  - 不同阶段产生多个 TrackingEvent（装车出发、到站卸车等），更新 Package.currentStation；
  - Route / RouteSegment 指示理论路线，TrackingEvent 记录实际经过的站点。

---

#### 步骤 7.1：中转站分拣后继续运输

- **需求描述**：到达中转站后，再次按照规则分拣，继续循环第 4 步。
- **相关实体**：
  - `Station`（中转站）
  - `SortingRule`
  - `TransportTask`
  - `Package`
- **模型支撑说明**：
  - 包裹到达中转站后，通过 TrackingEvent 记录“到达中转站”事件；
  - 在中转站 Station 上应用 SortingRule，判断下一步发往哪里；
  - 创建新的 TransportTask，重复“分拣→生成任务→运输”的闭环。

---

#### 步骤 7.2：到达目的网点，生成派送任务

- **需求描述**：到达目的网点后，系统为包裹分配派送员并生成派送任务。
- **相关实体**：
  - `Station`（目的网点 / 派送网点）
  - `Courier`
  - `DeliveryTask`
  - `Package`
  - `Waybill` / `Consignee`
- **模型支撑说明**：
  - 在 DeliveryStation 上创建 DeliveryTask，并分配给某个 Courier；
  - 每个 DeliveryTask 包含多个 Package；
  - 派送员通过 DeliveryTask 能获取包裹对应的 Waybill / Consignee 信息（收件人姓名、地址、电话）；
  - TrackingEvent 记录“交付派送员”、“开始派送”等事件。

---

#### 步骤 8：收件人签收

- **需求描述**：收件人签收确认后流程结束。
- **相关实体**：
  - `DeliveryTask`
  - `Package`
  - `TrackingEvent`
  - `Waybill`
- **模型支撑说明**：
  - 收件人签收时产生 TrackingEvent(eventType=签收)；
  - Package.currentStatus 变为“已签收”，Waybill.status 也可更新为“已签收”；
  - DeliveryTask.status 更新为“已签收 / 已完成”。

---

### 2. 对照扩展 / 异常流程

#### 扩展流程 1：包裹分拣异常

- **需求描述**：地址不清晰、标签损坏、目的地不明确时，包裹需要标记为异常并等待处理。
- **相关实体**：
  - `Package`
  - `Station`
  - `ExceptionRecord`
  - `TrackingEvent`
- **模型支撑说明**：
  - 在分拣过程中，如果发现问题，创建 ExceptionRecord：
    - `exceptionType = Sorting`
    - `reason = 地址不清晰 / 标签损坏 等`
    - `status = 待处理`
  - Package.currentStatus 变为“分拣异常”；
  - TrackingEvent 记录“分拣异常”事件；
  - 处理完成后，更新 ExceptionRecord.status 与 resolution，并将 Package.currentStatus 改回“分拣中 / 待发运”等继续流程。

---

#### 扩展流程 2：运输路线变更

- **需求描述**：因天气、交通等原因临时调整运输路线，需更新任务并通知司机和网点。
- **相关实体**：
  - `TransportTask`
  - `Dispatcher`
  - `RouteChangeRecord`
  - `Route` / `RouteSegment`
  - `Package`
- **模型支撑说明**：
  - 当 TransportTask 执行中需要改道时，调度员 Dispatcher 创建 RouteChangeRecord：
    - 记录 `changeTime`、`reason`、`oldRouteInfo`、`newRouteInfo`；
  - 运输任务可更新引用的 Route / 目标 Station；
  - 通过 TransportTask 与 Package 的关联，所有受影响包裹的后续 TrackingEvent 会基于新路线产生，支持追溯和成本分析。

---

#### 扩展流程 3：派送异常处理

- **需求描述**：收件人不在家、地址错误、包裹破损等，需要记录异常并可能进行二次派送或退回。
- **相关实体**：
  - `DeliveryTask`
  - `Courier`
  - `Package`
  - `ExceptionRecord`
  - `TrackingEvent`
- **模型支撑说明**：
  - 派送员通过系统在对应 DeliveryTask 下创建 ExceptionRecord：
    - `exceptionType = Delivery`
    - `reason = 收件人不在家 / 地址错误 / 包裹破损 等`
    - `status = 待处理`
  - Package.currentStatus 更新为“派送异常”；
  - 后续可创建新的 DeliveryTask（attemptNo+1），安排二次派送，或建立退回流程（状态=退回中）；
  - TrackingEvent 记录派送失败及后续动作（再次派送、退回等）。

---

### 3. 对照系统三大核心能力

#### 能力 1：包裹状态管理与追溯

- **需求描述**：根据运单号查询包裹当前状态、历史轨迹、当前位置、下一站、预计到达时间等。
- **相关实体**：
  - `Waybill`
  - `Package`
  - `TrackingEvent`
  - `Station`
  - `TransportTask`
  - `Route` / `RouteSegment`
- **模型支撑说明**：
  1. 用户输入 `waybillNo`，通过 `Waybill` 找到关联的 `Package`；
  2. 通过 Package.currentStatus 获取当前状态（如“运输中”）；
  3. 通过 `Package 1 —— * TrackingEvent` 查询该包裹的历史轨迹：
     - 按 eventTime 排序即可显示“揽收 → 分拣 → 中转 → 派送中”等路径；
  4. TrackingEvent 中最近一条到达或装车事件 + Station 信息表示“当前位置”（如“上海分拣中心”）；
  5. 根据当前 TransportTask（若状态为“运输中”）和其目标 Station，可推断“下一站”；
  6. 利用 TransportTask.plannedArrivalTime / actualDepartureTime 等，可估算或显示 ETA。

---

#### 能力 2：运输调度与任务管理

- **需求描述**：司机可查看当日运输任务列表、路线详情、车辆装载清单。
- **相关实体**：
  - `Driver`
  - `TransportTask`
  - `Vehicle`
  - `Station`
  - `Route` / `RouteSegment`
  - `Package`
- **模型支撑说明**：
  1. 司机登录后，经 `Driver 1 —— * TransportTask` 获取当天关联的所有 TransportTask；
  2. 每个 TransportTask 提供：
     - 起点 / 终点 Station 信息
     - 计划 / 实际出发到达时间
     - 关联 Vehicle（车牌、容量等）
     - 关联 Route（整体路线）和 RouteSegment（详细经停站）；
  3. 通过 `TransportTask * —— * Package`，司机可以查看本次运输任务的装载包裹清单及数量；
  4. TransportTask.status 与 TrackingEvent 共同记录执行进度，用于调度和监控。

---

#### 能力 3：派送任务执行支持

- **需求描述**：派送员可查看个人派送任务队列、收件人信息、地址、联系方式和派送路线建议。
- **相关实体**：
  - `Courier`
  - `DeliveryTask`
  - `Package`
  - `Waybill`
  - `Consignee`
- **模型支撑说明**：
  1. 通过 `Courier 1 —— * DeliveryTask`，派送员获取个人派送任务队列；
  2. 每个 DeliveryTask 包含多个 Package，系统可显示任务内所有包裹；
  3. 对于每个 Package，通过 `Waybill` 和 `Consignee` 可以访问：
     - 收件人姓名、联系电话、详细地址；
  4. DeliveryTask.routeHint 字段可保存系统计算的派送顺序或路线建议（例如按地理位置聚合后排序）。
  5. 若使用 `attemptNo` 字段，还可以区分首派和二次派送等不同尝试。

---

### 4. 示例问题：如何支持“获取派送员多次派送的记录”？

- **需求背景**：老师在 Lab3 可能会问：“如何通过模型获取某个派送员的多次派送记录？”
- **相关实体**：
  - `Courier`
  - `DeliveryTask`
  - `Package`
  - `ExceptionRecord`
  - `TrackingEvent`
- **模型支撑说明**：
  1. 通过 `Courier 1 —— * DeliveryTask`，找到某个派送员的所有派送任务；
  2. 每个 DeliveryTask 可以包含多个 Package，表示一次批量派送；
  3. 若某次派送失败（如收件人不在家），在对应 DeliveryTask 下创建 ExceptionRecord，记录失败原因；
  4. 为同一个包裹创建新的 DeliveryTask（可使用 `attemptNo` 记录是第几次派送），表示二次派送；
  5. 结合 DeliveryTask.status、actualFinishTime、TrackingEvent，可以完整回放某个派送员所有派送任务及每次派送结果；
  6. 因此，模型能够支持老师提出的“获取派送员多次派送记录”的查询与统计需求。

---

## 三、小结

- 本领域模型通过 `Customer / Consignee / Waybill / Package` 等实体，完整描述了寄件与包裹的基础信息；
- 通过 `Station / SortingRule / Route / RouteSegment / TransportTask / Vehicle / Driver / Dispatcher / RouteChangeRecord`，覆盖了从分拣、路径规划到干线运输与路线变更的全过程；
- 通过 `Courier / DeliveryTask`，建模了末端派送和多次派送场景；
- 通过 `TrackingEvent / ExceptionRecord`，支持包裹全生命周期的状态追踪与异常处理。

从主流程、扩展流程及三大核心能力三个层面验证可见，该模型**能够支撑题目中所有给定的业务场景与查询需求**，满足“快递物流管理”Lab 第一阶段的领域建模要求。
