package fu.dan.admin.domain.enums;

/**
 * 客户类型枚举
 */
public enum CustomerType {
    SENDER("寄件人"),
    RECIPIENT("收件人"),
    BOTH("寄件人和收件人");

    private final String description;

    CustomerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

