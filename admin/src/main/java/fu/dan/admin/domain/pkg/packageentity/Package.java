package fu.dan.admin.domain.pkg.packageentity;

import fu.dan.admin.domain.enums.PackageStatus;
import fu.dan.admin.domain.valueobject.Location;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 包裹实体（聚合根）
 */
@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    @Id
    @Column(name = "package_id", length = 50)
    private String packageId;

    @Column(name = "waybill_number", nullable = false, unique = true, length = 50)
    private String waybillNumber;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "length", precision = 10, scale = 2)
    private BigDecimal length;

    @Column(name = "width", precision = 10, scale = 2)
    private BigDecimal width;

    @Column(name = "height", precision = 10, scale = 2)
    private BigDecimal height;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    private PackageStatus currentStatus;

    @Embedded
    private Location currentLocation;

    @Column(name = "estimated_arrival_time")
    private LocalDateTime estimatedArrivalTime;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "waybill_number", referencedColumnName = "waybill_number", insertable = false, updatable = false)
    private Waybill waybill;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private List<PackageTrace> traceHistory = new ArrayList<>();

    /**
     * 更新包裹状态
     */
    public void updateStatus(PackageStatus status) {
        this.currentStatus = status;
    }

    /**
     * 添加轨迹记录
     */
    public void addTrace(PackageTrace trace) {
        if (trace != null) {
            trace.setPackageId(this.packageId);
            this.traceHistory.add(trace);
        }
    }

    /**
     * 获取历史轨迹
     */
    public List<PackageTrace> getTraceHistory() {
        return new ArrayList<>(traceHistory);
    }

    /**
     * 标记为异常
     */
    public void markAsException(String reason) {
        this.currentStatus = PackageStatus.EXCEPTION;
        // 创建异常轨迹记录
        PackageTrace exceptionTrace = new PackageTrace();
        exceptionTrace.setPackageId(this.packageId);
        exceptionTrace.setStatus(PackageStatus.EXCEPTION);
        exceptionTrace.setDescription("异常：" + reason);
        exceptionTrace.setTimestamp(LocalDateTime.now());
        addTrace(exceptionTrace);
    }

    /**
     * 获取当前位置
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * 获取下一站（业务方法，需要从RoutePlan获取）
     */
    public fu.dan.admin.domain.site.Station getNextStation() {
        // 业务逻辑：从RoutePlan获取下一站
        // 实际实现中会调用RoutePlanService
        return null;
    }
}

