package fu.dan.admin.domain.transport;

import fu.dan.admin.domain.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 司机实体
 */
@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {
    @Id
    @Column(name = "driver_id", length = 50)
    private String driverId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(name = "license_type", length = 20)
    private String licenseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    private DriverStatus currentStatus;

    @Column(name = "current_task", length = 50)
    private String currentTask;

    /**
     * 查看运输任务列表
     */
    public List<TransportTask> viewTransportTask() {
        // 业务逻辑：查询该司机的运输任务
        // 实际实现中会调用TransportTaskRepository
        return new ArrayList<>();
    }

    /**
     * 开始任务
     */
    public void startTask(String taskId) {
        this.currentTask = taskId;
        this.currentStatus = DriverStatus.ON_DUTY;
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskId) {
        if (taskId.equals(this.currentTask)) {
            this.currentTask = null;
            this.currentStatus = DriverStatus.AVAILABLE;
        }
    }

    /**
     * 报告路线变更
     */
    public void reportRouteChange(String reason) {
        // 业务逻辑：报告路线变更
        // 实际实现中会创建ExceptionRecord或更新TransportTask
    }

    /**
     * 更新状态
     */
    public void updateStatus(DriverStatus status) {
        this.currentStatus = status;
    }
}

