package fu.dan.admin.domain.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 中转站Repository
 */
@Repository
public interface TransferStationRepository extends JpaRepository<TransferStation, String> {
}

