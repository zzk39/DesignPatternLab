package fu.dan.admin.domain.pkg.packageentity;

import fu.dan.admin.domain.enums.PackageStatus;
import fu.dan.admin.domain.valueobject.Location;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 包裹轨迹实体
 */
@Entity
@Table(name = "package_traces")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageTrace {
    @Id
    @Column(name = "trace_id", length = 50)
    private String traceId;
    
    @PrePersist
    protected void onCreate() {
        if (traceId == null) {
            traceId = java.util.UUID.randomUUID().toString();
        }
    }

    @Column(name = "package_id", nullable = false, length = 50)
    private String packageId;

    @Embedded
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PackageStatus status;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "operator", length = 100)
    private String operator;

    @Column(name = "operator_type", length = 50)
    private String operatorType;

    @Column(name = "description", length = 500)
    private String description;

    /**
     * 获取位置名称
     */
    public String getLocationName() {
        return location != null ? location.getFullName() : "";
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        return status != null ? status.getDescription() : "";
    }
}

