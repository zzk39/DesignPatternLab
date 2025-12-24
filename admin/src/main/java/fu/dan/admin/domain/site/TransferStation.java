package fu.dan.admin.domain.site;

import fu.dan.admin.domain.pkg.packageentity.Package;
import fu.dan.admin.domain.transport.TransportTask;
import fu.dan.admin.domain.valueobject.SortingRule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 中转站实体
 */
@Entity
@Table(name = "transfer_stations")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransferStation extends Station {
    @Column(name = "sorting_capacity")
    private Integer sortingCapacity;

    @Embedded
    private SortingRule sortingRules;

    @ElementCollection
    @CollectionTable(name = "station_connections", joinColumns = @JoinColumn(name = "station_id"))
    @Column(name = "connected_station_id")
    private List<String> connectedStationIds = new ArrayList<>();

    /**
     * 批量分拣包裹
     */
    public Map<String, List<Package>> sortPackages(List<Package> packages) {
        // 业务逻辑：根据分拣规则批量分拣包裹
        // 返回按目标站点ID分组的包裹列表
        return null;
    }

    /**
     * 生成运输任务
     */
    public List<fu.dan.admin.domain.transport.TransportTask> generateTransportTasks() {
        // 业务逻辑：根据分拣结果生成运输任务
        return new ArrayList<>();
    }

    /**
     * 应用分拣规则确定下一站ID
     */
    public String applySortingRule(Package packageEntity) {
        // 业务逻辑：根据分拣规则确定包裹的下一站ID
        if (sortingRules != null && sortingRules.validate()) {
            // 应用规则逻辑
        }
        return null;
    }
}

