package fu.dan.admin.domain.transport;

import fu.dan.admin.domain.enums.VehicleStatus;
import fu.dan.admin.domain.enums.VehicleType;
import fu.dan.admin.domain.pkg.packageentity.Package;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 车辆实体
 */
@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    @Id
    @Column(name = "vehicle_id", length = 50)
    private String vehicleId;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 20)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type")
    private VehicleType vehicleType;

    @Column(name = "capacity", precision = 10, scale = 2)
    private BigDecimal capacity;

    @Column(name = "max_volume", precision = 10, scale = 2)
    private BigDecimal maxVolume;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    private VehicleStatus currentStatus;

    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;

    /**
     * 检查可用性
     */
    public boolean checkAvailability() {
        return currentStatus == VehicleStatus.AVAILABLE;
    }

    /**
     * 计算装载率
     */
    public double calculateLoadRate(List<Package> packages) {
        if (capacity == null || capacity.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal totalWeight = packages.stream()
                .map(pkg -> pkg.getWeight())
                .filter(w -> w != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalWeight.divide(capacity, 2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 更新状态
     */
    public void updateStatus(VehicleStatus status) {
        this.currentStatus = status;
    }
}

