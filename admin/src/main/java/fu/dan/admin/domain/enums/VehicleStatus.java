package fu.dan.admin.domain.enums;

/**
 * 车辆状态枚举
 */
public enum VehicleStatus {
    AVAILABLE("空闲"),
    IN_USE("使用中"),
    MAINTENANCE("维修中"),
    BREAKDOWN("故障");

    private final String description;

    VehicleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

