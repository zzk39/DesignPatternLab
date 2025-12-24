package fu.dan.admin.domain.delivery;

import fu.dan.admin.domain.enums.CourierStatus;
import fu.dan.admin.domain.exception.ExceptionRecord;
import fu.dan.admin.domain.pkg.packageentity.Package;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 派送员实体
 */
@Entity
@Table(name = "couriers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Courier {
    @Id
    @Column(name = "courier_id", length = 50)
    private String courierId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "assigned_area", length = 200)
    private String assignedArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    private CourierStatus currentStatus;

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;

    @Column(name = "current_task", length = 50)
    private String currentTask;

    /**
     * 查看派送任务列表
     */
    public List<DeliveryTask> viewDeliveryTasks() {
        // 业务逻辑：查询该派送员的派送任务
        // 实际实现中会调用DeliveryTaskRepository
        return new ArrayList<>();
    }

    /**
     * 派送包裹
     */
    public void deliverPackage(String packageId) {
        // 业务逻辑：派送包裹
        // 实际实现中会更新Package状态和DeliveryTask进度
    }

    /**
     * 确认签收
     */
    public void confirmSignature(String packageId, SignatureRecord signature) {
        // 业务逻辑：确认签收
        // 实际实现中会创建SignatureRecord并更新Package状态
    }

    /**
     * 报告异常
     */
    public ExceptionRecord reportException(String packageId, String reason) {
        // 业务逻辑：报告派送异常
        // 实际实现中会创建ExceptionRecord
        return null;
    }

    /**
     * 更新状态
     */
    public void updateStatus(CourierStatus status) {
        this.currentStatus = status;
    }
}

