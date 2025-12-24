package fu.dan.admin.domain.enums;

/**
 * 车辆类型枚举
 */
public enum VehicleType {
    SMALL_VAN("小型货车"),
    MEDIUM_TRUCK("中型卡车"),
    LARGE_TRUCK("大型卡车"),
    ELECTRIC_VEHICLE("电动车");

    private final String description;

    VehicleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

