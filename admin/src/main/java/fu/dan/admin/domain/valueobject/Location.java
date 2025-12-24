package fu.dan.admin.domain.valueobject;

import fu.dan.admin.domain.enums.LocationType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 位置值对象
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private String locationId;      // 位置ID
    @Enumerated(EnumType.STRING)
    private LocationType locationType; // 位置类型
    private String locationName;    // 位置名称
    private Address address;        // 地址
    private Coordinates coordinates; // 坐标

    /**
     * 获取完整位置名称
     */
    public String getFullName() {
        if (locationName != null && address != null) {
            return locationName + " - " + address.getFullAddress();
        }
        return locationName != null ? locationName : "";
    }

    /**
     * 计算到另一个位置的距离（简化实现，实际应使用地理坐标计算）
     */
    public double distanceTo(Location other) {
        if (this.coordinates == null || other == null || other.getCoordinates() == null) {
            return 0.0;
        }
        // 简化的距离计算，实际应使用Haversine公式
        double latDiff = this.coordinates.getLatitude() - other.getCoordinates().getLatitude();
        double lonDiff = this.coordinates.getLongitude() - other.getCoordinates().getLongitude();
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }
}

