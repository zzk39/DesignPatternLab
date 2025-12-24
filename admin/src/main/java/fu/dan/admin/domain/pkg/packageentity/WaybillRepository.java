package fu.dan.admin.domain.pkg.packageentity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 运单Repository
 */
@Repository
public interface WaybillRepository extends JpaRepository<Waybill, String> {
}
