package fu.dan.admin.domain.transport;

import fu.dan.admin.domain.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 司机Repository
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    List<Driver> findByCurrentStatus(DriverStatus status);
    Optional<Driver> findByLicenseNumber(String licenseNumber);
}

