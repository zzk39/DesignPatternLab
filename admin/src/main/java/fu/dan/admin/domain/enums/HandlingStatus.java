package fu.dan.admin.domain.enums;

/**
 * 异常处理状态枚举
 */
public enum HandlingStatus {
    REPORTED("已报告"),
    ASSIGNED("已分配"),
    IN_PROGRESS("处理中"),
    RESOLVED("已解决"),
    ESCALATED("已升级");

    private final String description;

    HandlingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

