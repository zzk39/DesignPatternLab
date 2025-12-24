package fu.dan.admin.domain.enums;

/**
 * 路径规划类型枚举
 */
public enum PlanType {
    SHORTEST_PATH("最短路径"),
    FASTEST_PATH("最快路径"),
    COST_OPTIMAL("成本最优");

    private final String description;

    PlanType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

