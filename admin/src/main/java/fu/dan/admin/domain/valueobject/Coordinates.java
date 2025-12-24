package fu.dan.admin.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 坐标值对象
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {
    @Column(name = "longitude")
    private Double longitude; // 经度
    @Column(name = "latitude")
    private Double latitude;  // 纬度
}

