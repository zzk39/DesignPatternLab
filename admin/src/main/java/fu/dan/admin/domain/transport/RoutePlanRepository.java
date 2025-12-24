package fu.dan.admin.domain.transport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 路径规划Repository
 */
@Repository
public interface RoutePlanRepository extends JpaRepository<RoutePlan, String> {
}

