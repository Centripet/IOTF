//package org.iotf.collectanalyzeservice.service;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.iotf.collectanalyzeservice.mapper.TDeviceMapper;
//import org.iotf.entity.collect_analyze.TDevice;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class AlarmScheduler {
//
//    private final AlarmService alarmService;
//    private final EnergyDataService influxDBService;
//    private final TDeviceMapper deviceMapper;
//
//    /**
//     * 每30秒检测一次所有设备
//     */
//    @Scheduled(fixedDelay = 30000)
//    public void checkAllDevices() {
//        List<TDevice> devices = deviceMapper.selectList(
//                new LambdaQueryWrapper<TDevice>()
//                        .eq(TDevice::getDeleted, 0)
//                        .eq(TDevice::getReport_status, true)
//                        .select(TDevice::getDevice_id, TDevice::getDevice_uuid));
//
//        for (TDevice device : devices) {
//
//            Double latestPower = influxDBService.queryLatestPower(device.getDevice_uuid());
//
//            if (latestPower != null) {
//                String deviceName = deviceMapper.selectById(device.getDevice_id()).getDevice_name();
//
//                alarmService.checkAndHandle(device.getDevice_id(), deviceName, latestPower, device.getThreshold());
//
//            }
//
//        }
//    }
//
//}
