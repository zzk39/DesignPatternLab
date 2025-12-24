package fu.dan.admin.service;

import fu.dan.admin.domain.exception.ExceptionRecord;
import fu.dan.admin.domain.exception.ExceptionRecordRepository;
import fu.dan.admin.domain.pkg.packageentity.Package;
import fu.dan.admin.domain.pkg.packageentity.PackageRepository;
import fu.dan.admin.domain.enums.ExceptionType;
import fu.dan.admin.domain.enums.HandlingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 异常处理服务 - 实现异常处理扩展流程
 */
@Service
@RequiredArgsConstructor
public class ExceptionService {
    
    private final ExceptionRecordRepository exceptionRecordRepository;
    private final PackageRepository packageRepository;

    /**
     * 扩展流程1: 包裹分拣异常
     */
    @Transactional
    public ExceptionRecord createSortingException(String packageId, String reportPerson,
                                                 String reportPersonRole, String reason) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在"));
        
        // 标记包裹为异常
        packageEntity.markAsException(reason);
        packageRepository.save(packageEntity);
        
        // 创建异常记录
        ExceptionRecord exception = new ExceptionRecord();
        exception.setExceptionId(UUID.randomUUID().toString());
        exception.setPackageId(packageId);
        exception.setExceptionType(ExceptionType.SORTING_EXCEPTION);
        exception.setExceptionReason(reason);
        exception.setReportPerson(reportPerson);
        exception.setReportPersonRole(reportPersonRole);
        exception.createException();
        
        return exceptionRecordRepository.save(exception);
    }

    /**
     * 扩展流程3: 派送异常
     */
    @Transactional
    public ExceptionRecord createDeliveryException(String packageId, String reportPerson,
                                                   String reportPersonRole, String reason) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在"));
        
        // 标记包裹为异常
        packageEntity.markAsException(reason);
        packageRepository.save(packageEntity);
        
        // 创建异常记录
        ExceptionRecord exception = new ExceptionRecord();
        exception.setExceptionId(UUID.randomUUID().toString());
        exception.setPackageId(packageId);
        exception.setExceptionType(ExceptionType.DELIVERY_EXCEPTION);
        exception.setExceptionReason(reason);
        exception.setReportPerson(reportPerson);
        exception.setReportPersonRole(reportPersonRole);
        exception.createException();
        
        return exceptionRecordRepository.save(exception);
    }

    /**
     * 分配异常处理人
     */
    @Transactional
    public void assignHandler(String exceptionId, String handlerId) {
        ExceptionRecord exception = exceptionRecordRepository.findById(exceptionId)
                .orElseThrow(() -> new IllegalArgumentException("异常记录不存在"));
        
        exception.assignHandler(handlerId);
        exceptionRecordRepository.save(exception);
    }

    /**
     * 解决异常
     */
    @Transactional
    public void resolveException(String exceptionId, String solution) {
        ExceptionRecord exception = exceptionRecordRepository.findById(exceptionId)
                .orElseThrow(() -> new IllegalArgumentException("异常记录不存在"));
        
        exception.resolveException(solution);
        exceptionRecordRepository.save(exception);
    }

    /**
     * 查询包裹的异常记录
     */
    public List<ExceptionRecord> getPackageExceptions(String packageId) {
        return exceptionRecordRepository.findByPackageId(packageId);
    }
}

