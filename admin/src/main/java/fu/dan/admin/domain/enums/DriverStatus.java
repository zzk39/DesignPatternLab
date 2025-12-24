package fu.dan.admin.domain.enums;

/**
 * 司机状态枚举
 */
public enum DriverStatus {
    AVAILABLE("空闲"),
    ON_DUTY("在岗"),
    OFF_DUTY("下班"),
    ON_LEAVE("请假");

    private final String description;

    DriverStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

