package fu.dan.admin.domain.pkg.packageentity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 包裹Repository
 */
@Repository
public interface PackageRepository extends JpaRepository<Package, String> {
    Optional<Package> findByWaybillNumber(String waybillNumber);
}

