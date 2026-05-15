package org.iotf.collectanalyzeservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.mapper.TDeviceMapper;
import org.iotf.entity.collect_analyze.TDevice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmScheduler {

    private final AlarmService alarmService;
    private final EnergyDataService influxDBService;
    private final TDeviceMapper deviceMapper;

    /**
     * 每分钟检测一次所有设备
     */
    @Scheduled(fixedDelay = 60000)
    public void checkAllDevices() {
        List<TDevice> devices = deviceMapper.selectList(new LambdaQueryWrapper<TDevice>()
                .select(TDevice::getDevice_id, TDevice::getDevice_uuid));
        
        for (TDevice device : devices) {
            // 查询 InfluxDB 最近一次数据
            Double latestPower = 0D;
                    influxDBService.queryLatestPower(device.getDevice_uuid());

            if (latestPower != null) {
                double threshold = getThreshold(device.getDevice_id());  // 从配置获取
                String deviceName = deviceMapper.selectById(device.getDevice_id()).getDevice_name();
                alarmService.checkAndHandle(device.getDevice_id(), deviceName, latestPower, threshold);
            }
        }
    }

    private double getThreshold(Long device_id) {
        // 可从 MySQL 配置表读取，或使用默认值
        return 2000;
    }
}
