package fu.dan.admin.domain.site;

import fu.dan.admin.domain.enums.SiteType;
import fu.dan.admin.domain.pkg.packageentity.Package;
import fu.dan.admin.domain.pkg.packageentity.Waybill;
import fu.dan.admin.domain.delivery.DeliveryTask;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 网点实体
 */
@Entity
@Table(name = "sites")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Site extends Station {
    @Enumerated(EnumType.STRING)
    @Column(name = "site_type")
    private SiteType siteType;

    @Column(name = "operating_hours", length = 50)
    private String operatingHours;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * 接收包裹
     */
    @Override
    public void acceptPackage(Package packageEntity) {
        super.acceptPackage(packageEntity);
        // 业务逻辑：网点接收包裹后的处理
    }

    /**
     * 分拣包裹
     */
    public void sortPackage(Package packageEntity) {
        // 业务逻辑：分拣包裹
        packageEntity.updateStatus(fu.dan.admin.domain.enums.PackageStatus.SORTING);
    }

    /**
     * 分配派送任务
     */
    public DeliveryTask assignDeliveryTask(List<Package> packages) {
        // 业务逻辑：分配派送任务
        // 实际实现中会创建DeliveryTask并分配给Courier
        return null;
    }

    /**
     * 打印运单
     */
    public void printWaybill(Waybill waybill) {
        if (waybill != null) {
            waybill.print();
        }
    }
}

