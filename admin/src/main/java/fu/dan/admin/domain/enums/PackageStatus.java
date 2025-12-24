package fu.dan.admin.domain.enums;

/**
 * 包裹状态枚举
 */
public enum PackageStatus {
    COLLECTED("已揽收"),
    SORTING("分拣中"),
    IN_TRANSIT("运输中"),
    ARRIVED_AT_TRANSFER("到达中转站"),
    OUT_FOR_DELIVERY("派送中"),
    DELIVERED("已签收"),
    EXCEPTION("异常"),
    RETURNED("已退回");

    private final String description;

    PackageStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

