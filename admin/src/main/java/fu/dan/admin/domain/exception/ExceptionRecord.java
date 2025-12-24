package fu.dan.admin.domain.exception;

import fu.dan.admin.domain.enums.ExceptionType;
import fu.dan.admin.domain.enums.HandlingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 异常记录实体
 */
@Entity
@Table(name = "exception_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionRecord {
    @Id
    @Column(name = "exception_id", length = 50)
    private String exceptionId;

    @Column(name = "package_id", nullable = false, length = 50)
    private String packageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exception_type")
    private ExceptionType exceptionType;

    @Column(name = "exception_reason", length = 500)
    private String exceptionReason;

    @Column(name = "report_time")
    private LocalDateTime reportTime;

    @Column(name = "report_person", length = 100)
    private String reportPerson;

    @Column(name = "report_person_role", length = 50)
    private String reportPersonRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "handling_status")
    private HandlingStatus handlingStatus;

    @Column(name = "assigned_handler", length = 100)
    private String assignedHandler;

    @Column(name = "resolution_plan", length = 1000)
    private String resolutionPlan;

    @Column(name = "resolved_time")
    private LocalDateTime resolvedTime;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    /**
     * 创建异常记录
     */
    public void createException() {
        this.reportTime = LocalDateTime.now();
        this.handlingStatus = HandlingStatus.REPORTED;
    }

    /**
     * 分配处理人
     */
    public void assignHandler(String handlerId) {
        this.assignedHandler = handlerId;
        this.handlingStatus = HandlingStatus.ASSIGNED;
    }

    /**
     * 解决异常
     */
    public void resolveException(String solution) {
        this.resolutionPlan = solution;
        this.resolvedTime = LocalDateTime.now();
        this.handlingStatus = HandlingStatus.RESOLVED;
    }

    /**
     * 通知客户
     */
    public void notifyCustomer(String message) {
        // 业务逻辑：通知客户异常情况
        // 实际实现中会调用消息服务或通知服务
    }

    /**
     * 升级异常
     */
    public void escalate() {
        this.handlingStatus = HandlingStatus.ESCALATED;
    }
}

