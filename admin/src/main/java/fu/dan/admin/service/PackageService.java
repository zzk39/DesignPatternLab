package fu.dan.admin.service;

import fu.dan.admin.domain.customer.Customer;
import fu.dan.admin.domain.pkg.packageentity.Package;
import fu.dan.admin.domain.pkg.packageentity.PackageTrace;
import fu.dan.admin.domain.pkg.packageentity.Waybill;
import fu.dan.admin.domain.pkg.packageentity.PackageRepository;
import fu.dan.admin.domain.pkg.packageentity.PackageTraceRepository;
import fu.dan.admin.domain.pkg.packageentity.WaybillRepository;
import fu.dan.admin.domain.site.Site;
import fu.dan.admin.domain.site.SiteRepository;
import fu.dan.admin.domain.enums.PackageStatus;
import fu.dan.admin.domain.valueobject.Address;
import fu.dan.admin.domain.valueobject.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 包裹服务 - 实现包裹管理核心业务流程
 */
@Service
@RequiredArgsConstructor
public class PackageService {
    
    private final PackageRepository packageRepository;
    private final WaybillRepository waybillRepository;
    private final PackageTraceRepository packageTraceRepository;
    private final SiteRepository siteRepository;

    /**
     * 流程1-2: 客户寄送包裹，录入运单信息并打印运单号
     */
    @Transactional
    public Package createPackageAndWaybill(
            Customer sender,
            Customer recipient,
            Site originSite,
            Site destinationSite,
            Address senderAddress,
            Address recipientAddress,
            double weight,
            double length,
            double width,
            double height) {
        
        // 生成运单号
        String waybillNumber = "WB" + System.currentTimeMillis();
        
        // 创建运单
        Waybill waybill = new Waybill();
        waybill.setWaybillNumber(waybillNumber);
        waybill.setSenderInfo(sender);
        waybill.setRecipientInfo(recipient);
        waybill.setOriginSite(originSite);
        waybill.setDestinationSite(destinationSite);
        waybill.setCreatedTime(LocalDateTime.now());
        
        // 验证运单
        if (!waybill.validate()) {
            throw new IllegalArgumentException("运单信息不完整");
        }
        
        // 保存运单
        waybill = waybillRepository.save(waybill);
        
        // 创建包裹
        Package packageEntity = new Package();
        packageEntity.setPackageId(UUID.randomUUID().toString());
        packageEntity.setWaybillNumber(waybillNumber);
        packageEntity.setWeight(java.math.BigDecimal.valueOf(weight));
        packageEntity.setLength(java.math.BigDecimal.valueOf(length));
        packageEntity.setWidth(java.math.BigDecimal.valueOf(width));
        packageEntity.setHeight(java.math.BigDecimal.valueOf(height));
        packageEntity.setCurrentStatus(PackageStatus.COLLECTED);
        
        // 设置当前位置为寄件网点
        Location currentLocation = new Location();
        currentLocation.setLocationId(originSite.getStationId());
        currentLocation.setLocationName(originSite.getStationName());
        currentLocation.setAddress(originSite.getAddress());
        packageEntity.setCurrentLocation(currentLocation);
        
        // 保存包裹
        packageEntity = packageRepository.save(packageEntity);
        
        // 创建初始轨迹记录
        addTrace(packageEntity.getPackageId(), originSite.getStationId(), 
                originSite.getStationName(), PackageStatus.COLLECTED, "包裹已揽收");
        
        return packageEntity;
    }

    /**
     * 流程8: 收件人签收确认（已由DeliveryService.confirmSignature处理）
     */
    @Transactional
    public void confirmDelivery(String packageId, String signaturePerson, String courierId) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在"));
        
        packageEntity.updateStatus(PackageStatus.DELIVERED);
        
        // 添加签收轨迹
        if (packageEntity.getCurrentLocation() != null) {
            addTrace(packageId, packageEntity.getCurrentLocation().getLocationId(),
                    packageEntity.getCurrentLocation().getLocationName(),
                    PackageStatus.DELIVERED, "包裹已签收，签收人：" + signaturePerson);
        }
        
        packageRepository.save(packageEntity);
    }

    /**
     * 能力1: 包裹状态管理与追溯
     */
    public PackageStatus getPackageStatus(String waybillNumber) {
        Package packageEntity = packageRepository.findByWaybillNumber(waybillNumber)
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在"));
        return packageEntity.getCurrentStatus();
    }

    /**
     * 能力1: 获取包裹历史轨迹
     */
    public List<PackageTrace> getTraceHistory(String waybillNumber) {
        Package packageEntity = packageRepository.findByWaybillNumber(waybillNumber)
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在"));
        return packageTraceRepository.findByPackageIdOrderByTimestampDesc(packageEntity.getPackageId());
    }

    /**
     * 能力1: 获取包裹当前位置
     */
    public Location getCurrentLocation(String waybillNumber) {
        Package packageEntity = packageRepository.findByWaybillNumber(waybillNumber)
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在"));
        return packageEntity.getCurrentLocation();
    }

    /**
     * 添加轨迹记录
     */
    private void addTrace(String packageId, String locationId, String locationName, 
                         PackageStatus status, String description) {
        PackageTrace trace = new PackageTrace();
        trace.setTraceId(UUID.randomUUID().toString());
        trace.setPackageId(packageId);
        trace.setStatus(status);
        trace.setTimestamp(LocalDateTime.now());
        trace.setDescription(description);
        
        Location location = new Location();
        location.setLocationId(locationId);
        location.setLocationName(locationName);
        trace.setLocation(location);
        
        packageTraceRepository.save(trace);
    }

    /**
     * 更新包裹状态
     */
    @Transactional
    public void updateStatus(String packageId, PackageStatus status, String locationId, 
                            String locationName, String description) {
        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在"));
        
        packageEntity.updateStatus(status);
        
        // 更新当前位置
        Location location = new Location();
        location.setLocationId(locationId);
        location.setLocationName(locationName);
        packageEntity.setCurrentLocation(location);
        
        // 添加轨迹
        addTrace(packageId, locationId, locationName, status, description);
        
        packageRepository.save(packageEntity);
    }
}

