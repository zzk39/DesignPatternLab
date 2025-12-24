package fu.dan.admin.domain.delivery;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 签收记录实体
 */
@Entity
@Table(name = "signature_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureRecord {
    @Id
    @Column(name = "record_id", length = 50)
    private String recordId;

    @Column(name = "package_id", nullable = false, length = 50)
    private String packageId;

    @Column(name = "waybill_number", length = 50)
    private String waybillNumber;

    @Column(name = "signature_time")
    private LocalDateTime signatureTime;

    @Column(name = "signature_person", length = 100)
    private String signaturePerson;

    @Column(name = "relation_to_recipient", length = 50)
    private String relationToRecipient;

    @Column(name = "signature_image", length = 500)
    private String signatureImage;

    @Column(name = "courier_id", length = 50)
    private String courierId;

    @Column(name = "delivery_task_id", length = 50)
    private String deliveryTaskId;

    /**
     * 验证签收记录
     */
    public boolean validate() {
        return packageId != null && !packageId.isEmpty() &&
               signatureTime != null &&
               signaturePerson != null && !signaturePerson.isEmpty() &&
               courierId != null && !courierId.isEmpty();
    }

    /**
     * 获取签收图片URL
     */
    public String getImageUrl() {
        return signatureImage;
    }
}

