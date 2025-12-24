package fu.dan.admin.domain.transport;

import fu.dan.admin.domain.enums.PlanType;
import fu.dan.admin.domain.valueobject.Location;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 路径规划实体
 */
@Entity
@Table(name = "route_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlan {
    @Id
    @Column(name = "route_id", length = 50)
    private String routeId;

    @Column(name = "origin_location_id", length = 50)
    private String originLocationId;

    @Column(name = "destination_location_id", length = 50)
    private String destinationLocationId;
    
    // 业务方法：获取origin Location（需要从Repository查询）
    public Location getOrigin() {
        // 实际实现中会从LocationRepository查询
        return null;
    }
    
    // 业务方法：获取destination Location（需要从Repository查询）
    public Location getDestination() {
        // 实际实现中会从LocationRepository查询
        return null;
    }
    
    public void setOrigin(Location origin) {
        this.originLocationId = origin != null ? origin.getLocationId() : null;
    }
    
    public void setDestination(Location destination) {
        this.destinationLocationId = destination != null ? destination.getLocationId() : null;
    }

    @ElementCollection
    @CollectionTable(name = "route_intermediate_stations", joinColumns = @JoinColumn(name = "route_id"))
    @Column(name = "station_id")
    private List<String> intermediateStationIds = new ArrayList<>();

    @Column(name = "estimated_time")
    private Duration estimatedTime;

    @Column(name = "distance", precision = 10, scale = 2)
    private BigDecimal distance;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type")
    private PlanType planType;

    /**
     * 计算路径
     */
    public void calculateRoute() {
        // 业务逻辑：根据planType计算最优路径
        // 设置intermediateStations、estimatedTime、distance等
    }

    /**
     * 更新路径
     */
    public void updateRoute(List<String> newStationIds) {
        this.intermediateStationIds = newStationIds != null ? new ArrayList<>(newStationIds) : new ArrayList<>();
        // 重新计算时间和距离
        calculateRoute();
    }

    /**
     * 获取下一站ID
     */
    public String getNextStationId(String currentStationId) {
        if (intermediateStationIds == null || intermediateStationIds.isEmpty()) {
            return null;
        }
        int index = intermediateStationIds.indexOf(currentStationId);
        if (index >= 0 && index < intermediateStationIds.size() - 1) {
            return intermediateStationIds.get(index + 1);
        }
        return null;
    }

    /**
     * 估算到达时间
     */
    public LocalDateTime estimateArrivalTime(String currentStationId) {
        if (estimatedTime == null) {
            return null;
        }
        // 业务逻辑：根据当前位置和剩余路径估算到达时间
        return LocalDateTime.now().plus(estimatedTime);
    }
}

