package fu.dan.admin.domain.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 签收记录Repository
 */
@Repository
public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, String> {
    Optional<SignatureRecord> findByPackageId(String packageId);
    List<SignatureRecord> findByCourierId(String courierId);
}

