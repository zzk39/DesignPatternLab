package fu.dan.admin.domain.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 网点Repository
 */
@Repository
public interface SiteRepository extends JpaRepository<Site, String> {
    List<Site> findBySiteType(fu.dan.admin.domain.enums.SiteType siteType);
}

