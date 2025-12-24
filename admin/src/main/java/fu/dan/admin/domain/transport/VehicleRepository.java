package fu.dan.admin.domain.transport;

import fu.dan.admin.domain.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 车辆Repository
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    List<Vehicle> findByCurrentStatus(VehicleStatus status);
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}

