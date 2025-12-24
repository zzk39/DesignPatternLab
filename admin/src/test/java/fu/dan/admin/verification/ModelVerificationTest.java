package fu.dan.admin.verification;

import fu.dan.admin.domain.customer.Customer;
import fu.dan.admin.domain.customer.CustomerRepository;
import fu.dan.admin.domain.delivery.Courier;
import fu.dan.admin.domain.delivery.CourierRepository;
import fu.dan.admin.domain.delivery.DeliveryTask;
import fu.dan.admin.domain.enums.*;
import fu.dan.admin.domain.exception.ExceptionRecord;

import fu.dan.admin.domain.pkg.packageentity.Package;
import fu.dan.admin.domain.pkg.packageentity.PackageTrace;
import fu.dan.admin.domain.site.Site;
import fu.dan.admin.domain.site.SiteRepository;
import fu.dan.admin.domain.transport.*;
import fu.dan.admin.domain.valueobject.Address;
import fu.dan.admin.domain.valueobject.Location;
import fu.dan.admin.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 领域模型验证测试类
 * 验证模型是否支持主流程、扩展流程和基本能力
 */
@SpringBootTest
@Transactional
@DisplayName("领域模型验证测试")
@TestPropertySource(locations = "classpath:application-test.properties")
public class ModelVerificationTest {

    @Autowired
    private PackageService packageService;
    
    @Autowired
    private TransportService transportService;
    
    @Autowired
    private DeliveryService deliveryService;
    
    @Autowired
    private ExceptionService exceptionService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private SiteRepository siteRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private CourierRepository courierRepository;

    private Customer sender;
    private Customer recipient;
    private Site originSite;
    private Site destinationSite;
    private Vehicle vehicle;
    private Driver driver;
    private Courier courier;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        createTestData();
    }

    /**
     * 验证主流程：正常流程8个步骤
     */
    @Test
    @DisplayName("验证主流程：客户寄送包裹到签收的完整流程")
    void testMainProcess() {
        System.out.println("\n========== 验证主流程 ==========");
        
        // 流程1-2: 客户寄送包裹，录入运单信息并打印运单号
        System.out.println("步骤1-2: 客户寄送包裹，录入运单信息");
        Address senderAddress = createAddress("上海市", "黄浦区", "南京东路", "100号");
        Address recipientAddress = createAddress("北京市", "朝阳区", "建国路", "88号");
        
        Package packageEntity = packageService.createPackageAndWaybill(
                sender, recipient, originSite, destinationSite,
                senderAddress, recipientAddress, 2.5, 30, 20, 15);
        
        assertNotNull(packageEntity);
        assertNotNull(packageEntity.getWaybillNumber());
        assertEquals(PackageStatus.COLLECTED, packageEntity.getCurrentStatus());
        System.out.println("✓ 运单号: " + packageEntity.getWaybillNumber());
        System.out.println("✓ 包裹状态: " + packageEntity.getCurrentStatus().getDescription());
        
        // 流程3: 路径规划
        System.out.println("\n步骤3: 路径规划");
        Location origin = createLocation(originSite.getStationId(), originSite.getStationName());
        Location destination = createLocation(destinationSite.getStationId(), destinationSite.getStationName());
        List<String> intermediateStationIds = new ArrayList<>();
        
        RoutePlan routePlan = transportService.createRoutePlan(origin, destination, intermediateStationIds);
        assertNotNull(routePlan);
        assertNotNull(routePlan.getRouteId());
        System.out.println("✓ 路径规划ID: " + routePlan.getRouteId());
        
        // 流程4: 分拣并生成运输任务
        System.out.println("\n步骤4: 分拣并生成运输任务");
        List<String> packageIds = new ArrayList<>();
        packageIds.add(packageEntity.getPackageId());
        
        TransportTask transportTask = transportService.createTransportTask(
                originSite.getStationId(), destinationSite.getStationId(), routePlan, packageIds);
        assertNotNull(transportTask);
        assertEquals(TaskStatus.PENDING, transportTask.getTaskStatus());
        System.out.println("✓ 运输任务ID: " + transportTask.getTaskId());
        System.out.println("✓ 任务状态: " + transportTask.getTaskStatus().getDescription());
        
        // 更新包裹状态为分拣中
        packageService.updateStatus(packageEntity.getPackageId(), PackageStatus.SORTING,
                originSite.getStationId(), originSite.getStationName(), "包裹已分拣");
        
        // 流程5: 运输调度 - 分配车辆和司机
        System.out.println("\n步骤5: 运输调度 - 分配车辆和司机");
        transportService.assignVehicleAndDriver(transportTask.getTaskId(),
                vehicle.getVehicleId(), driver.getDriverId());
        
        TransportTask updatedTask = transportService.getTaskDetails(transportTask.getTaskId());
        assertEquals(TaskStatus.ASSIGNED, updatedTask.getTaskStatus());
        assertNotNull(updatedTask.getAssignedVehicle());
        assertNotNull(updatedTask.getAssignedDriver());
        System.out.println("✓ 车辆: " + updatedTask.getAssignedVehicle().getVehicleNumber());
        System.out.println("✓ 司机: " + updatedTask.getAssignedDriver().getName());
        
        // 流程6: 司机运输包裹
        System.out.println("\n步骤6: 司机运输包裹");
        transportService.startTransport(transportTask.getTaskId());
        
        updatedTask = transportService.getTaskDetails(transportTask.getTaskId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getTaskStatus());
        assertNotNull(updatedTask.getActualDepartureTime());
        System.out.println("✓ 运输开始时间: " + updatedTask.getActualDepartureTime());
        
        // 更新包裹状态为运输中
        packageService.updateStatus(packageEntity.getPackageId(), PackageStatus.IN_TRANSIT,
                vehicle.getVehicleId(), vehicle.getVehicleNumber(), "包裹运输中");
        
        // 完成运输
        transportService.completeTransport(transportTask.getTaskId());
        updatedTask = transportService.getTaskDetails(transportTask.getTaskId());
        assertEquals(TaskStatus.COMPLETED, updatedTask.getTaskStatus());
        System.out.println("✓ 运输完成");
        
        // 流程7.2: 到达目的网点，分配给派送员
        System.out.println("\n步骤7.2: 到达目的网点，分配给派送员");
        packageService.updateStatus(packageEntity.getPackageId(), PackageStatus.OUT_FOR_DELIVERY,
                destinationSite.getStationId(), destinationSite.getStationName(), "到达目的网点");
        
        DeliveryTask deliveryTask = deliveryService.createDeliveryTask(
                courier.getCourierId(), packageIds, "朝阳区");
        assertNotNull(deliveryTask);
        assertEquals(TaskStatus.ASSIGNED, deliveryTask.getTaskStatus());
        assertNotNull(deliveryTask.getAssignedCourier());
        System.out.println("✓ 派送任务ID: " + deliveryTask.getTaskId());
        System.out.println("✓ 派送员: " + deliveryTask.getAssignedCourier().getName());
        
        // 流程8: 收件人签收确认
        System.out.println("\n步骤8: 收件人签收确认");
        deliveryService.confirmSignature(packageEntity.getPackageId(), "张三",
                "本人", courier.getCourierId(), deliveryTask.getTaskId());
        
        Package finalPackage = packageService.getPackageStatus(packageEntity.getWaybillNumber()) != null ?
                packageService.getPackageStatus(packageEntity.getWaybillNumber()) != null ? packageEntity : null : null;
        
        // 验证签收
        List<PackageTrace> traces = packageService.getTraceHistory(packageEntity.getWaybillNumber());
        assertTrue(traces.stream().anyMatch(t -> t.getStatus() == PackageStatus.DELIVERED));
        System.out.println("✓ 签收完成");
        
        System.out.println("\n========== 主流程验证通过 ==========\n");
    }

    /**
     * 验证扩展流程1: 包裹分拣异常
     */
    @Test
    @DisplayName("验证扩展流程1: 包裹分拣异常处理")
    void testSortingException() {
        System.out.println("\n========== 验证扩展流程1: 分拣异常 ==========");
        
        // 创建包裹
        Address senderAddress = createAddress("上海市", "黄浦区", "南京东路", "100号");
        Address recipientAddress = createAddress("北京市", "朝阳区", "建国路", "88号");
        
        Package packageEntity = packageService.createPackageAndWaybill(
                sender, recipient, originSite, destinationSite,
                senderAddress, recipientAddress, 2.5, 30, 20, 15);
        
        // 报告分拣异常
        ExceptionRecord exception = exceptionService.createSortingException(
                packageEntity.getPackageId(), "分拣员001", "分拣员", "地址不清晰");
        
        assertNotNull(exception);
        assertEquals(ExceptionType.SORTING_EXCEPTION, exception.getExceptionType());
        assertEquals(HandlingStatus.REPORTED, exception.getHandlingStatus());
        
        // 验证包裹状态
        PackageStatus status = packageService.getPackageStatus(packageEntity.getWaybillNumber());
        assertEquals(PackageStatus.EXCEPTION, status);
        
        System.out.println("✓ 异常类型: " + exception.getExceptionType().getDescription());
        System.out.println("✓ 异常原因: " + exception.getExceptionReason());
        System.out.println("✓ 包裹状态: " + status.getDescription());
        
        // 分配处理人
        exceptionService.assignHandler(exception.getExceptionId(), "处理员001");
        ExceptionRecord updatedException = exceptionService.getPackageExceptions(
                packageEntity.getPackageId()).get(0);
        assertEquals(HandlingStatus.ASSIGNED, updatedException.getHandlingStatus());
        System.out.println("✓ 已分配处理人");
        
        // 解决异常
        exceptionService.resolveException(exception.getExceptionId(), "已联系发件人确认地址");
        updatedException = exceptionService.getPackageExceptions(packageEntity.getPackageId()).get(0);
        assertEquals(HandlingStatus.RESOLVED, updatedException.getHandlingStatus());
        System.out.println("✓ 异常已解决");
        
        System.out.println("\n========== 扩展流程1验证通过 ==========\n");
    }

    /**
     * 验证扩展流程2: 运输路线变更
     */
    @Test
    @DisplayName("验证扩展流程2: 运输路线变更")
    void testRouteChange() {
        System.out.println("\n========== 验证扩展流程2: 运输路线变更 ==========");
        
        // 创建路径规划
        Location origin = createLocation(originSite.getStationId(), originSite.getStationName());
        Location destination = createLocation(destinationSite.getStationId(), destinationSite.getStationName());
        RoutePlan originalRoute = transportService.createRoutePlan(origin, destination, new ArrayList<>());
        
        // 创建运输任务
        List<String> packageIds = new ArrayList<>();
        TransportTask task = transportService.createTransportTask(
                originSite.getStationId(), destinationSite.getStationId(), originalRoute, packageIds);
        
        // 分配车辆和司机
        transportService.assignVehicleAndDriver(task.getTaskId(),
                vehicle.getVehicleId(), driver.getDriverId());
        
        // 开始运输
        transportService.startTransport(task.getTaskId());
        
        // 报告路线变更
        System.out.println("✓ 原路线: " + originalRoute.getRouteId());
        
        // 创建新路线（包含中转站）
        List<String> newStationIds = new ArrayList<>();
        
        // 更新路线
        transportService.changeRoute(task.getTaskId(), newStationIds);
        
        TransportTask updatedTask = transportService.getTaskDetails(task.getTaskId());
        assertNotNull(updatedTask.getPlannedRoute());
        System.out.println("✓ 路线已更新");
        
        System.out.println("\n========== 扩展流程2验证通过 ==========\n");
    }

    /**
     * 验证扩展流程3: 派送异常处理
     */
    @Test
    @DisplayName("验证扩展流程3: 派送异常处理")
    void testDeliveryException() {
        System.out.println("\n========== 验证扩展流程3: 派送异常 ==========");
        
        // 创建包裹
        Address senderAddress = createAddress("上海市", "黄浦区", "南京东路", "100号");
        Address recipientAddress = createAddress("北京市", "朝阳区", "建国路", "88号");
        
        Package packageEntity = packageService.createPackageAndWaybill(
                sender, recipient, originSite, destinationSite,
                senderAddress, recipientAddress, 2.5, 30, 20, 15);
        
        // 创建派送任务
        List<String> packageIds = new ArrayList<>();
        packageIds.add(packageEntity.getPackageId());
        DeliveryTask deliveryTask = deliveryService.createDeliveryTask(
                courier.getCourierId(), packageIds, "朝阳区");
        
        // 报告派送异常（使用ExceptionService创建异常记录）
        ExceptionRecord exception = exceptionService.createDeliveryException(
                packageEntity.getPackageId(), courier.getCourierId(), "派送员", "收件人不在家");
        
        // 验证异常记录
        assertNotNull(exception);
        assertEquals(ExceptionType.DELIVERY_EXCEPTION, exception.getExceptionType());
        
        List<ExceptionRecord> exceptions = exceptionService.getPackageExceptions(packageEntity.getPackageId());
        assertFalse(exceptions.isEmpty());
        
        System.out.println("✓ 异常类型: " + exception.getExceptionType().getDescription());
        System.out.println("✓ 异常原因: " + exception.getExceptionReason());
        
        System.out.println("\n========== 扩展流程3验证通过 ==========\n");
    }

    /**
     * 验证基本能力1: 包裹状态管理与追溯
     */
    @Test
    @DisplayName("验证基本能力1: 包裹状态管理与追溯")
    void testPackageStatusAndTrace() {
        System.out.println("\n========== 验证基本能力1: 包裹状态管理与追溯 ==========");
        
        // 创建包裹
        Address senderAddress = createAddress("上海市", "黄浦区", "南京东路", "100号");
        Address recipientAddress = createAddress("北京市", "朝阳区", "建国路", "88号");
        
        Package packageEntity = packageService.createPackageAndWaybill(
                sender, recipient, originSite, destinationSite,
                senderAddress, recipientAddress, 2.5, 30, 20, 15);
        
        String waybillNumber = packageEntity.getWaybillNumber();
        
        // 查询包裹状态
        PackageStatus status = packageService.getPackageStatus(waybillNumber);
        assertEquals(PackageStatus.COLLECTED, status);
        System.out.println("✓ 当前状态: " + status.getDescription());
        
        // 查询历史轨迹
        List<PackageTrace> traces = packageService.getTraceHistory(waybillNumber);
        assertFalse(traces.isEmpty());
        System.out.println("✓ 历史轨迹数量: " + traces.size());
        traces.forEach(trace -> {
            System.out.println("  - " + trace.getTimestamp() + " " + 
                    trace.getStatus().getDescription() + ": " + trace.getDescription());
        });
        
        // 查询当前位置
        Location location = packageService.getCurrentLocation(waybillNumber);
        assertNotNull(location);
        System.out.println("✓ 当前位置: " + location.getLocationName());
        
        // 更新状态并验证轨迹
        packageService.updateStatus(packageEntity.getPackageId(), PackageStatus.SORTING,
                originSite.getStationId(), originSite.getStationName(), "包裹已分拣");
        
        traces = packageService.getTraceHistory(waybillNumber);
        assertTrue(traces.size() >= 2);
        System.out.println("✓ 状态更新后轨迹数量: " + traces.size());
        
        System.out.println("\n========== 基本能力1验证通过 ==========\n");
    }

    /**
     * 验证基本能力2: 运输调度与任务管理
     */
    @Test
    @DisplayName("验证基本能力2: 运输调度与任务管理")
    void testTransportTaskManagement() {
        System.out.println("\n========== 验证基本能力2: 运输调度与任务管理 ==========");
        
        // 创建路径规划
        Location origin = createLocation(originSite.getStationId(), originSite.getStationName());
        Location destination = createLocation(destinationSite.getStationId(), destinationSite.getStationName());
        RoutePlan routePlan = transportService.createRoutePlan(origin, destination, new ArrayList<>());
        
        // 创建运输任务
        List<String> packageIds = new ArrayList<>();
        TransportTask task = transportService.createTransportTask(
                originSite.getStationId(), destinationSite.getStationId(), routePlan, packageIds);
        
        // 分配车辆和司机
        transportService.assignVehicleAndDriver(task.getTaskId(),
                vehicle.getVehicleId(), driver.getDriverId());
        
        // 司机查看运输任务列表
        List<TransportTask> driverTasks = transportService.getDriverTasks(driver.getDriverId());
        assertFalse(driverTasks.isEmpty());
        System.out.println("✓ 司机任务数量: " + driverTasks.size());
        
        // 查看任务详情
        TransportTask taskDetails = transportService.getTaskDetails(task.getTaskId());
        assertNotNull(taskDetails);
        assertNotNull(taskDetails.getPlannedRoute());
        assertNotNull(taskDetails.getAssignedVehicle());
        assertNotNull(taskDetails.getAssignedDriver());
        
        System.out.println("✓ 任务ID: " + taskDetails.getTaskId());
        System.out.println("✓ 路线: " + taskDetails.getPlannedRoute().getRouteId());
        System.out.println("✓ 车辆: " + taskDetails.getAssignedVehicle().getVehicleNumber());
        System.out.println("✓ 司机: " + taskDetails.getAssignedDriver().getName());
        System.out.println("✓ 包裹数量: " + taskDetails.getPackageCount());
        
        System.out.println("\n========== 基本能力2验证通过 ==========\n");
    }

    /**
     * 验证基本能力3: 派送任务执行支持
     */
    @Test
    @DisplayName("验证基本能力3: 派送任务执行支持")
    void testDeliveryTaskSupport() {
        System.out.println("\n========== 验证基本能力3: 派送任务执行支持 ==========");
        
        // 创建包裹
        Address senderAddress = createAddress("上海市", "黄浦区", "南京东路", "100号");
        Address recipientAddress = createAddress("北京市", "朝阳区", "建国路", "88号");
        
        Package packageEntity = packageService.createPackageAndWaybill(
                sender, recipient, originSite, destinationSite,
                senderAddress, recipientAddress, 2.5, 30, 20, 15);
        
        // 创建派送任务
        List<String> packageIds = new ArrayList<>();
        packageIds.add(packageEntity.getPackageId());
        DeliveryTask deliveryTask = deliveryService.createDeliveryTask(
                courier.getCourierId(), packageIds, "朝阳区");
        
        // 派送员查看派送任务列表
        List<DeliveryTask> courierTasks = deliveryService.getCourierTasks(courier.getCourierId());
        assertFalse(courierTasks.isEmpty());
        System.out.println("✓ 派送员任务数量: " + courierTasks.size());
        
        // 查看任务详情
        DeliveryTask taskDetails = deliveryService.getDeliveryTaskDetails(deliveryTask.getTaskId());
        assertNotNull(taskDetails);
        assertNotNull(taskDetails.getAssignedCourier());
        assertFalse(taskDetails.getPackageList().isEmpty());
        
        System.out.println("✓ 任务ID: " + taskDetails.getTaskId());
        System.out.println("✓ 派送区域: " + taskDetails.getDeliveryArea());
        System.out.println("✓ 派送员: " + taskDetails.getAssignedCourier().getName());
        System.out.println("✓ 包裹数量: " + taskDetails.getTotalCount());
        System.out.println("✓ 完成进度: " + taskDetails.getProgress() + "%");
        
        System.out.println("\n========== 基本能力3验证通过 ==========\n");
    }

    // ========== 辅助方法 ==========
    
    private void createTestData() {
        // 创建寄件人
        sender = new Customer();
        sender.setCustomerId("CUST001");
        sender.setName("李四");
        sender.setPhoneNumber("13800138001");
        sender.setAddress(createAddress("上海市", "黄浦区", "南京东路", "100号"));
        sender.setCustomerType(CustomerType.SENDER);
        sender = customerRepository.save(sender);
        
        // 创建收件人
        recipient = new Customer();
        recipient.setCustomerId("CUST002");
        recipient.setName("张三");
        recipient.setPhoneNumber("13800138002");
        recipient.setAddress(createAddress("北京市", "朝阳区", "建国路", "88号"));
        recipient.setCustomerType(CustomerType.RECIPIENT);
        recipient = customerRepository.save(recipient);
        
        // 创建寄件网点
        originSite = new Site();
        originSite.setStationId("SITE001");
        originSite.setStationName("上海黄浦网点");
        originSite.setAddress(createAddress("上海市", "黄浦区", "南京东路", "100号"));
        originSite.setSiteType(SiteType.SERVICE_SITE);
        originSite.setCapacity(1000);
        originSite = siteRepository.save(originSite);
        
        // 创建目的网点
        destinationSite = new Site();
        destinationSite.setStationId("SITE002");
        destinationSite.setStationName("北京朝阳网点");
        destinationSite.setAddress(createAddress("北京市", "朝阳区", "建国路", "88号"));
        destinationSite.setSiteType(SiteType.SERVICE_SITE);
        destinationSite.setCapacity(1000);
        destinationSite = siteRepository.save(destinationSite);
        
        // 创建车辆
        vehicle = new Vehicle();
        vehicle.setVehicleId("VEH001");
        vehicle.setVehicleNumber("沪A12345");
        vehicle.setVehicleType(VehicleType.MEDIUM_TRUCK);
        vehicle.setCapacity(BigDecimal.valueOf(5000));
        vehicle.setMaxVolume(BigDecimal.valueOf(20));
        vehicle.setCurrentStatus(VehicleStatus.AVAILABLE);
        vehicle = vehicleRepository.save(vehicle);
        
        // 创建司机
        driver = new Driver();
        driver.setDriverId("DRV001");
        driver.setName("王五");
        driver.setPhoneNumber("13800138003");
        driver.setLicenseNumber("LIC001");
        driver.setCurrentStatus(DriverStatus.AVAILABLE);
        driver = driverRepository.save(driver);
        
        // 创建派送员
        courier = new Courier();
        courier.setCourierId("COU001");
        courier.setName("赵六");
        courier.setPhoneNumber("13800138004");
        courier.setAssignedArea("朝阳区");
        courier.setCurrentStatus(CourierStatus.AVAILABLE);
        courier = courierRepository.save(courier);
    }
    
    private Address createAddress(String province, String city, String district, String detail) {
        Address address = new Address();
        address.setProvince(province);
        address.setCity(city);
        address.setDistrict(district);
        address.setDetailAddress(detail);
        return address;
    }
    
    private Location createLocation(String locationId, String locationName) {
        Location location = new Location();
        location.setLocationId(locationId);
        location.setLocationName(locationName);
        return location;
    }
}

