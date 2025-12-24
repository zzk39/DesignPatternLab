package fu.dan.admin.domain.delivery;

import fu.dan.admin.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 派送任务Repository
 */
@Repository
public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, String> {
    List<DeliveryTask> findByAssignedCourier_CourierId(String courierId);
    List<DeliveryTask> findByTaskStatus(TaskStatus status);
}

