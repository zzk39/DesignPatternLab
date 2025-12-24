package fu.dan.admin.domain.enums;

/**
 * 异常类型枚举
 */
public enum ExceptionType {
    SORTING_EXCEPTION("分拣异常"),
    TRANSPORT_EXCEPTION("运输异常"),
    DELIVERY_EXCEPTION("派送异常"),
    PACKAGE_DAMAGE("包裹破损"),
    ADDRESS_ERROR("地址错误"),
    RECIPIENT_UNAVAILABLE("收件人不在"),
    WEATHER_DELAY("天气延误"),
    TRAFFIC_DELAY("交通延误"),
    OTHER("其他");

    private final String description;

    ExceptionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

