package fu.dan.admin.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址值对象
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Column(name = "province")
    private String province;      // 省
    @Column(name = "city")
    private String city;          // 市
    @Column(name = "district")
    private String district;      // 区
    @Column(name = "street")
    private String street;        // 街道
    @Column(name = "detail_address")
    private String detailAddress; // 详细地址
    @Column(name = "postal_code")
    private String postalCode;    // 邮编

    /**
     * 验证地址有效性
     */
    public boolean validateAddress() {
        return province != null && !province.isEmpty() &&
               city != null && !city.isEmpty() &&
               district != null && !district.isEmpty() &&
               detailAddress != null && !detailAddress.isEmpty();
    }

    /**
     * 获取完整地址字符串
     */
    public String getFullAddress() {
        return String.format("%s%s%s%s%s",
                province != null ? province : "",
                city != null ? city : "",
                district != null ? district : "",
                street != null ? street : "",
                detailAddress != null ? detailAddress : "");
    }

    /**
     * 判断是否同城
     */
    public boolean isSameCity(Address other) {
        if (other == null) {
            return false;
        }
        return this.province != null && this.city != null &&
               this.province.equals(other.province) &&
               this.city.equals(other.city);
    }
}

