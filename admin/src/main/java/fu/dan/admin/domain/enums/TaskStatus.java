package fu.dan.admin.domain.enums;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    PENDING("待分配"),
    ASSIGNED("已分配"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

