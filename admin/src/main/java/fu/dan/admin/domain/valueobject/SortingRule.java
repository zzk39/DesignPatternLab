package fu.dan.admin.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分拣规则值对象
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortingRule {
    private String ruleId;      // 规则ID
    private String ruleName;    // 规则名称
    private Integer priority;   // 优先级

    /**
     * 验证规则有效性
     */
    public boolean validate() {
        return ruleId != null && !ruleId.isEmpty() &&
               ruleName != null && !ruleName.isEmpty() &&
               priority != null && priority >= 0;
    }
}

