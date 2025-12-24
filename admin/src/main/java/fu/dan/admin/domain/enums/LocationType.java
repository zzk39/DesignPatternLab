package fu.dan.admin.domain.enums;

/**
 * 位置类型枚举
 */
public enum LocationType {
    SITE("网点"),
    TRANSFER_STATION("中转站"),
    VEHICLE("车辆"),
    OTHER("其他");

    private final String description;

    LocationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

