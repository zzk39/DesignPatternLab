package fu.dan.admin.domain.delivery;

import fu.dan.admin.domain.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 派送员Repository
 */
@Repository
public interface CourierRepository extends JpaRepository<Courier, String> {
    List<Courier> findByCurrentStatus(CourierStatus status);
    List<Courier> findByAssignedArea(String assignedArea);
}

