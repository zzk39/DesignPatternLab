package fu.dan.admin.domain.pkg.packageentity;

import fu.dan.admin.domain.customer.Customer;
import fu.dan.admin.domain.site.Site;
import fu.dan.admin.domain.valueobject.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运单实体
 */
@Entity
@Table(name = "waybills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Waybill {
    @Id
    @Column(name = "waybill_number", length = 50)
    private String waybillNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Customer senderInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Customer recipientInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_site_id")
    private Site originSite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_site_id")
    private Site destinationSite;

    @Column(name = "declared_value", precision = 10, scale = 2)
    private BigDecimal declaredValue;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "package_id", length = 50)
    private String packageId;

    /**
     * 验证运单信息完整性
     */
    public boolean validate() {
        return waybillNumber != null && !waybillNumber.isEmpty() &&
               senderInfo != null &&
               recipientInfo != null &&
               originSite != null &&
               destinationSite != null &&
               createdTime != null;
    }

    /**
     * 打印运单
     */
    public void print() {
        // 业务逻辑：打印运单
        System.out.println("打印运单：" + waybillNumber);
    }

    /**
     * 获取寄件人地址
     */
    public Address getSenderAddress() {
        return senderInfo != null ? senderInfo.getAddress() : null;
    }

    /**
     * 获取收件人地址
     */
    public Address getRecipientAddress() {
        return recipientInfo != null ? recipientInfo.getAddress() : null;
    }
}

