package fu.dan.admin.domain.delivery;

import fu.dan.admin.domain.enums.TaskStatus;
import fu.dan.admin.domain.pkg.packageentity.Package;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 派送任务实体（聚合根）
 */
@Entity
@Table(name = "delivery_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTask {
    @Id
    @Column(name = "task_id", length = 50)
    private String taskId;

    @ElementCollection
    @CollectionTable(name = "delivery_task_packages", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "package_id")
    private List<String> packageList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_courier_id")
    private Courier assignedCourier;

    @Column(name = "delivery_area", length = 200)
    private String deliveryArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TaskStatus taskStatus;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completed_count")
    private Integer completedCount = 0;

    @Column(name = "total_count")
    private Integer totalCount = 0;

    /**
     * 分配派送员
     */
    public void assignCourier(Courier courier) {
        this.assignedCourier = courier;
        this.taskStatus = TaskStatus.ASSIGNED;
        if (courier != null) {
            courier.updateStatus(fu.dan.admin.domain.enums.CourierStatus.DELIVERING);
        }
    }

    /**
     * 添加包裹
     */
    public void addPackage(String packageId) {
        if (packageId != null && !packageList.contains(packageId)) {
            packageList.add(packageId);
            totalCount = packageList.size();
        }
    }

    /**
     * 优化派送路线
     */
    public List<Package> optimizeDeliveryRoute() {
        // 业务逻辑：优化派送路线
        // 实际实现中会根据地址信息进行路线优化
        return new ArrayList<>();
    }

    /**
     * 完成派送
     */
    public void completeDelivery(String packageId) {
        if (packageList.contains(packageId)) {
            completedCount++;
            if (completedCount >= totalCount) {
                this.taskStatus = TaskStatus.COMPLETED;
                if (assignedCourier != null) {
                    assignedCourier.updateStatus(fu.dan.admin.domain.enums.CourierStatus.AVAILABLE);
                }
            }
        }
    }

    /**
     * 获取进度
     */
    public double getProgress() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) completedCount / totalCount * 100;
    }
}

