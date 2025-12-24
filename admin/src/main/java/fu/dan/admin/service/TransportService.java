package fu.dan.admin.service;

import fu.dan.admin.domain.transport.*;
import fu.dan.admin.domain.enums.TaskStatus;
import fu.dan.admin.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 运输服务 - 实现运输调度核心业务流程
 */
@Service
@RequiredArgsConstructor
public class TransportService {
    
    private final TransportTaskRepository transportTaskRepository;
    private final RoutePlanRepository routePlanRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    /**
     * 流程3: 路径规划
     */
    @Transactional
    public RoutePlan createRoutePlan(Location origin, Location destination, 
                                     List<String> intermediateStationIds) {
        RoutePlan routePlan = new RoutePlan();
        routePlan.setRouteId(UUID.randomUUID().toString());
        routePlan.setOrigin(origin);
        routePlan.setDestination(destination);
        routePlan.setIntermediateStationIds(intermediateStationIds != null ? intermediateStationIds : new ArrayList<>());
        
        // 计算路径（简化实现）
        routePlan.calculateRoute();
        
        return routePlanRepository.save(routePlan);
    }

    /**
     * 流程4: 生成运输任务
     */
    @Transactional
    public TransportTask createTransportTask(String originStationId, String destinationStationId,
                                            RoutePlan routePlan, List<String> packageIds) {
        TransportTask task = new TransportTask();
        task.setTaskId(UUID.randomUUID().toString());
        task.setOriginStationId(originStationId);
        task.setDestinationStationId(destinationStationId);
        task.setPlannedRoute(routePlan);
        task.setTaskStatus(TaskStatus.PENDING);
        task.setScheduledTime(LocalDateTime.now());
        
        // 添加包裹
        for (String packageId : packageIds) {
            task.addPackage(packageId);
        }
        
        return transportTaskRepository.save(task);
    }

    /**
     * 流程5: 运输调度 - 分配车辆和司机
     */
    @Transactional
    public void assignVehicleAndDriver(String taskId, String vehicleId, String driverId) {
        TransportTask task = transportTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("运输任务不存在"));
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("车辆不存在"));
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("司机不存在"));
        
        // 检查车辆和司机可用性
        if (!vehicle.checkAvailability()) {
            throw new IllegalStateException("车辆不可用");
        }
        
        if (driver.getCurrentStatus() != fu.dan.admin.domain.enums.DriverStatus.AVAILABLE) {
            throw new IllegalStateException("司机不可用");
        }
        
        task.assignVehicleAndDriver(vehicle, driver);
        transportTaskRepository.save(task);
    }

    /**
     * 流程6: 司机开始运输
     */
    @Transactional
    public void startTransport(String taskId) {
        TransportTask task = transportTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("运输任务不存在"));
        
        task.startTransport();
        transportTaskRepository.save(task);
    }

    /**
     * 流程6: 完成运输
     */
    @Transactional
    public void completeTransport(String taskId) {
        TransportTask task = transportTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("运输任务不存在"));
        
        task.completeTransport();
        transportTaskRepository.save(task);
    }

    /**
     * 扩展流程2: 运输路线变更
     */
    @Transactional
    public void changeRoute(String taskId, List<String> newStationIds) {
        TransportTask task = transportTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("运输任务不存在"));
        
        if (task.getPlannedRoute() != null) {
            task.getPlannedRoute().updateRoute(newStationIds);
            routePlanRepository.save(task.getPlannedRoute());
        }
        transportTaskRepository.save(task);
    }

    /**
     * 能力2: 司机查看运输任务列表
     */
    public List<TransportTask> getDriverTasks(String driverId) {
        return transportTaskRepository.findByAssignedDriver_DriverId(driverId);
    }

    /**
     * 能力2: 获取运输任务详情
     */
    public TransportTask getTaskDetails(String taskId) {
        return transportTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("运输任务不存在"));
    }
}

