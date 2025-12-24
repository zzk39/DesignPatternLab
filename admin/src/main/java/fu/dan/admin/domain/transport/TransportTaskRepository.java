package fu.dan.admin.domain.transport;

import fu.dan.admin.domain.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 运输任务Repository
 */
@Repository
public interface TransportTaskRepository extends JpaRepository<TransportTask, String> {
    List<TransportTask> findByAssignedDriver_DriverId(String driverId);
    List<TransportTask> findByTaskStatus(TaskStatus status);
}

