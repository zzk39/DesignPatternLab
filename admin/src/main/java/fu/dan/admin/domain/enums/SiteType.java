package fu.dan.admin.domain.enums;

/**
 * 网点类型枚举
 */
public enum SiteType {
    SERVICE_SITE("营业网点"),
    SORTING_CENTER("分拣中心"),
    WAREHOUSE("仓库");

    private final String description;

    SiteType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

