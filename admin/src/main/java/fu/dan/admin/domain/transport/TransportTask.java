package fu.dan.admin.domain.transport;

import fu.dan.admin.domain.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 运输任务实体（聚合根）
 */
@Entity
@Table(name = "transport_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportTask {
    @Id
    @Column(name = "task_id", length = 50)
    private String taskId;

    @ElementCollection
    @CollectionTable(name = "transport_task_packages", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "package_id")
    private List<String> packageList = new ArrayList<>();

    @Column(name = "package_count")
    private Integer packageCount = 0;

    @Column(name = "origin_station_id", length = 50)
    private String originStationId;

    @Column(name = "destination_station_id", length = 50)
    private String destinationStationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_vehicle_id")
    private Vehicle assignedVehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    private Driver assignedDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planned_route_id")
    private RoutePlan plannedRoute;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TaskStatus taskStatus;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    /**
     * 分配车辆和司机
     */
    public void assignVehicleAndDriver(Vehicle vehicle, Driver driver) {
        this.assignedVehicle = vehicle;
        this.assignedDriver = driver;
        this.taskStatus = TaskStatus.ASSIGNED;
    }

    /**
     * 添加包裹
     */
    public void addPackage(String packageId) {
        if (packageId != null && !packageList.contains(packageId)) {
            packageList.add(packageId);
            packageCount = packageList.size();
        }
    }

    /**
     * 开始运输
     */
    public void startTransport() {
        this.taskStatus = TaskStatus.IN_PROGRESS;
        this.actualDepartureTime = LocalDateTime.now();
        if (assignedDriver != null) {
            assignedDriver.startTask(this.taskId);
        }
        if (assignedVehicle != null) {
            assignedVehicle.updateStatus(fu.dan.admin.domain.enums.VehicleStatus.IN_USE);
        }
    }

    /**
     * 完成运输
     */
    public void completeTransport() {
        this.taskStatus = TaskStatus.COMPLETED;
        this.actualArrivalTime = LocalDateTime.now();
        if (assignedDriver != null) {
            assignedDriver.completeTask(this.taskId);
        }
        if (assignedVehicle != null) {
            assignedVehicle.updateStatus(fu.dan.admin.domain.enums.VehicleStatus.AVAILABLE);
        }
    }

    /**
     * 处理路线变更
     */
    public void handleRouteChange(RoutePlan newRoute) {
        if (newRoute != null && plannedRoute != null) {
            plannedRoute.updateRoute(newRoute.getIntermediateStationIds());
        }
    }

    /**
     * 获取装载率
     */
    public double getLoadRate() {
        if (assignedVehicle == null || packageCount == 0) {
            return 0.0;
        }
        // 业务逻辑：计算装载率
        // 实际实现中需要获取包裹列表并计算
        return 0.0;
    }
}

