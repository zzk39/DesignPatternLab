package fu.dan.admin.domain.pkg.packageentity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 包裹轨迹Repository
 */
@Repository
public interface PackageTraceRepository extends JpaRepository<PackageTrace, String> {
    List<PackageTrace> findByPackageIdOrderByTimestampDesc(String packageId);
}

