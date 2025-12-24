package fu.dan.admin.domain.exception;

import fu.dan.admin.domain.enums.ExceptionType;
import fu.dan.admin.domain.enums.HandlingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 异常记录Repository
 */
@Repository
public interface ExceptionRecordRepository extends JpaRepository<ExceptionRecord, String> {
    List<ExceptionRecord> findByPackageId(String packageId);
    List<ExceptionRecord> findByExceptionType(ExceptionType type);
    List<ExceptionRecord> findByHandlingStatus(HandlingStatus status);
}

