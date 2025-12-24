package fu.dan.admin.service;

import fu.dan.admin.domain.delivery.*;
import fu.dan.admin.domain.exception.ExceptionRecord;
import fu.dan.admin.domain.pkg.packageentity.Package;
import fu.dan.admin.domain.pkg.packageentity.PackageRepository;
import fu.dan.admin.domain.pkg.packageentity.PackageTrace;
import fu.dan.admin.domain.pkg.packageentity.PackageTraceRepository;
import fu.dan.admin.domain.enums.PackageStatus;
import fu.dan.admin.domain.enums.TaskStatus;
import fu.dan.admin.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 派送服务 - 实现派送核心业务流程
 */
@Service
@RequiredArgsConstructor
public class DeliveryService {
    
    private final DeliveryTaskRepository deliveryTaskRepository;
    private final CourierRepository courierRepository;
    private final SignatureRecordRepository signatureRecordRepository;
    private final PackageRepository packageRepository;
    private final PackageTraceRepository packageTraceRepository;

    /**
     * 流程7.2: 创建派送任务并分配给派送员
     */
    @Transactional
    public DeliveryTask createDeliveryTask(String courierId, List<String> packageIds, String deliveryArea) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new IllegalArgumentException("派送员不存在"));
        
        DeliveryTask task = new DeliveryTask();
        task.setTaskId(UUID.randomUUID().toString());
        task.setDeliveryArea(deliveryArea);
        task.setTaskStatus(TaskStatus.PENDING);
        task.setScheduledDate(LocalDate.now());
        
        // 添加包裹
        for (String packageId : packageIds) {
            task.addPackage(packageId);
        }
        
        // 分配派送员
        task.assignCourier(courier);
        
        return deliveryTaskRepository.save(task);
    }

    /**
     * 流程8: 确认签收
     */
    @Transactional
    public SignatureRecord confirmSignature(String packageId, String signaturePerson,
                                           String relationToRecipient, String courierId,
                                           String deliveryTaskId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在"));
        
        SignatureRecord signature = new SignatureRecord();
        signature.setRecordId(UUID.randomUUID().toString());
        signature.setPackageId(packageId);
        signature.setWaybillNumber(packageEntity.getWaybillNumber());
        signature.setSignatureTime(java.time.LocalDateTime.now());
        signature.setSignaturePerson(signaturePerson);
        signature.setRelationToRecipient(relationToRecipient);
        signature.setCourierId(courierId);
        signature.setDeliveryTaskId(deliveryTaskId);
        
        if (!signature.validate()) {
            throw new IllegalArgumentException("签收信息不完整");
        }
        
        signature = signatureRecordRepository.save(signature);
        
        // 更新包裹状态为已签收
        packageEntity.updateStatus(PackageStatus.DELIVERED);
        
        // 添加签收轨迹
        if (packageEntity.getCurrentLocation() != null) {
            PackageTrace trace = new PackageTrace();
            trace.setTraceId(java.util.UUID.randomUUID().toString());
            trace.setPackageId(packageId);
            trace.setStatus(PackageStatus.DELIVERED);
            trace.setTimestamp(java.time.LocalDateTime.now());
            trace.setDescription("包裹已签收，签收人：" + signaturePerson);
            trace.setLocation(packageEntity.getCurrentLocation());
            packageTraceRepository.save(trace);
        }
        
        packageRepository.save(packageEntity);
        
        // 更新派送任务进度
        DeliveryTask task = deliveryTaskRepository.findById(deliveryTaskId)
                .orElseThrow(() -> new IllegalArgumentException("派送任务不存在"));
        task.completeDelivery(packageId);
        deliveryTaskRepository.save(task);
        
        return signature;
    }

    /**
     * 扩展流程3: 报告派送异常
     */
    @Transactional
    public ExceptionRecord reportDeliveryException(String packageId, String courierId, String reason) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在"));
        
        packageEntity.markAsException(reason);
        packageRepository.save(packageEntity);
        
        // 创建异常记录（实际应该调用ExceptionService，这里简化处理）
        // 注意：这里应该注入ExceptionService，但为了避免循环依赖，暂时返回null
        // 实际实现中应该调用 exceptionService.createDeliveryException()
        return null;
    }

    /**
     * 能力3: 派送员查看派送任务列表
     */
    public List<DeliveryTask> getCourierTasks(String courierId) {
        return deliveryTaskRepository.findByAssignedCourier_CourierId(courierId);
    }

    /**
     * 能力3: 获取派送任务详情（包含包裹和收件人信息）
     */
    public DeliveryTask getDeliveryTaskDetails(String taskId) {
        return deliveryTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("派送任务不存在"));
    }
}

