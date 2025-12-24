package fu.dan.admin.domain.site;

import fu.dan.admin.domain.pkg.packageentity.Package;
import fu.dan.admin.domain.valueobject.Address;
import fu.dan.admin.domain.valueobject.Location;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 站点抽象类
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Station {
    @Id
    @Column(name = "station_id", length = 50)
    private String stationId;

    @Column(name = "station_name", nullable = false, length = 100)
    private String stationName;

    @Embedded
    private Location location;
    
    /**
     * 获取地址（从location中获取）
     */
    public Address getAddress() {
        return location != null ? location.getAddress() : null;
    }
    
    /**
     * 设置地址（设置到location中）
     */
    public void setAddress(Address address) {
        if (location == null) {
            location = new Location();
        }
        location.setAddress(address);
    }

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "current_load")
    private Integer currentLoad = 0;

    /**
     * 接收包裹
     */
    public void acceptPackage(Package packageEntity) {
        if (checkCapacity()) {
            this.currentLoad++;
            // 业务逻辑：更新包裹位置
        }
    }

    /**
     * 检查容量
     */
    public boolean checkCapacity() {
        return capacity == null || currentLoad < capacity;
    }
}

