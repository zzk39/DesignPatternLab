package fu.dan.admin.domain.enums;

/**
 * 派送员状态枚举
 */
public enum CourierStatus {
    AVAILABLE("空闲"),
    DELIVERING("派送中"),
    RETURNING("返回中"),
    OFF_DUTY("下班");

    private final String description;

    CourierStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

