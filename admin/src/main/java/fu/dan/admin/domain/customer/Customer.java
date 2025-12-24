package fu.dan.admin.domain.customer;

import fu.dan.admin.domain.enums.CustomerType;
import fu.dan.admin.domain.valueobject.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户实体
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @Column(name = "customer_id", length = 50)
    private String customerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;

    /**
     * 创建运单（业务方法，实际创建由Waybill实体处理）
     */
    public void createWaybill() {
        // 业务逻辑：客户创建运单
        // 实际实现中会调用WaybillService
    }

    /**
     * 查询包裹状态（业务方法）
     */
    public void queryPackageStatus(String waybillNumber) {
        // 业务逻辑：查询包裹状态
        // 实际实现中会调用PackageService
    }

    /**
     * 更新联系信息
     */
    public void updateContactInfo(String phone, Address newAddress) {
        if (phone != null && !phone.isEmpty()) {
            this.phoneNumber = phone;
        }
        if (newAddress != null) {
            this.address = newAddress;
        }
    }
}

