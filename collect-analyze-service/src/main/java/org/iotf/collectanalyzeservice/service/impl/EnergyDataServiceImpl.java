package org.iotf.collectanalyzeservice.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.model.EnergyDataPoint;
import org.iotf.collectanalyzeservice.service.EnergyDataService;
import org.iotf.collectanalyzeservice.service.ITAlarmService;
import org.iotf.collectanalyzeservice.service.ITDeviceService;
import org.iotf.entity.collect_analyze.EnergyDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 能耗数据服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyDataServiceImpl implements EnergyDataService {

    private final InfluxDBClient influxDBClient;
    private final ITAlarmService alarmService;
    private final ITDeviceService deviceService;

    @Value("${influx.bucket}")
    private String bucket;
    @Value("${influx.org}")
    private String influxOrg;

    // 用于存储设备上次上报的数据（用于滤波）
    private final ConcurrentHashMap<String, EnergyDataDTO> lastDataMap = new ConcurrentHashMap<>();

    // 默认阈值常量（与C代码对应）
    private static final double VOLTAGE_NORMAL_MIN = 200;
    private static final double VOLTAGE_NORMAL_MAX = 240;
    private static final double CURRENT_MAX = 30;
    private static final double POWER_MAX = 5000;

    @Override
    public void processReportedData(EnergyDataDTO data) {
        log.info("接收到设备数据上报: deviceId={}", data.getDeviceId());

        // 1. 数据清洗
        EnergyDataDTO cleanedData = cleanData(data);

        // 2. 获取上次数据用于滤波
        EnergyDataDTO lastData = lastDataMap.get(data.getDeviceId());

        // 3. 数据滤波处理
        if (lastData != null && cleanedData.getIsComplete()) {
            cleanedData.setCurrent(filterData(cleanedData.getCurrent(), lastData.getCurrent()));
            cleanedData.setVoltage(filterData(cleanedData.getVoltage(), lastData.getVoltage()));
            cleanedData.setPower(filterData(cleanedData.getPower(), lastData.getPower()));
        }

        // 4. 更新上次数据缓存
        lastDataMap.put(data.getDeviceId(), cleanedData);

        // 5. 转换为InfluxDB数据点并存储
        EnergyDataPoint dataPoint = convertToDataPoint(cleanedData);
        storeToInfluxDB(dataPoint);

        // 6. 触发告警检测
        alarmService.checkAlarms(cleanedData);

        log.info("设备数据处理完成: deviceId={}, power={}W, energy={}Wh",
                data.getDeviceId(), cleanedData.getPower(), cleanedData.getEnergy());
    }

    @Override
    public EnergyDataPoint convertToDataPoint(EnergyDataDTO dto) {
        return EnergyDataPoint.builder()
                .deviceUUID(dto.getDeviceUUID() != null ? dto.getDeviceUUID() : dto.getDeviceId())
                .deviceType(dto.getDeviceType())
                .location(dto.getLocation())
                .commType(dto.getCommType())
                .current(dto.getCurrent())
                .voltage(dto.getVoltage())
                .power(dto.getPower())
                .energy(dto.getEnergy())
                .totalEnergy(dto.getTotalEnergy())
                .isOn(dto.getIsOn())
                .isFault(dto.getIsFault())
                .isComplete(dto.getIsComplete())
                .timestamp(dto.getTimestamp() != null ?
                        dto.getTimestamp().atZone(ZoneId.systemDefault()).toInstant() : Instant.now())
                .receivedTime(Instant.now())
                .lastUpdateTime(dto.getTimestamp() != null ?
                        dto.getTimestamp().atZone(ZoneId.systemDefault()).toInstant() : Instant.now())

                .user_id(dto.getUser_id())
                .device_id(dto.getDevice_id())
                .device_type(dto.getDevice_type())
                .device_name(dto.getDevice_name())
                .location(dto.getLocation_pg())
                .build();
    }

    @Override
    public double filterData(double newVal, double lastVal) {
        // 简单滑动平均：新值权重70%，旧值权重30%
        return 0.7 * newVal + 0.3 * lastVal;
    }

    @Override
    public EnergyDataDTO cleanData(EnergyDataDTO data) {
        boolean isComplete = true;

        // 电流异常值处理
        if (data.getCurrent() == null || data.getCurrent() < 0 || data.getCurrent() > CURRENT_MAX) {
            log.warn("发现异常电流值 {}A，已修正", data.getCurrent());
            data.setCurrent(0.0);
            isComplete = false;
        }

        // 电压异常值处理
        if (data.getVoltage() == null || data.getVoltage() < VOLTAGE_NORMAL_MIN || data.getVoltage() > VOLTAGE_NORMAL_MAX) {
            log.warn("发现异常电压值 {}V，已修正为220V", data.getVoltage());
            data.setVoltage(220.0);
        }

        // 功率异常值处理
        if (data.getPower() == null || data.getPower() < 0 || data.getPower() > POWER_MAX) {
            log.warn("发现异常功率值 {}W，已修正", data.getPower());
            data.setPower(0.0);
            isComplete = false;
        }

        data.setIsComplete(isComplete);
        return data;
    }

    @Override
    public void storeToInfluxDB(EnergyDataPoint dataPoint) {
        try {
            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writeMeasurement(bucket, influxOrg, WritePrecision.NS, dataPoint);
            log.debug("数据已写入InfluxDB: deviceUUID={}", dataPoint.getDeviceUUID());
        } catch (Exception e) {
            log.error("写入InfluxDB失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<EnergyDataPoint> queryHistoryData(String deviceUUID, LocalDateTime startTime, LocalDateTime endTime) {
        List<EnergyDataPoint> result = new ArrayList<>();

        String query = String.format(
                "from(bucket: \"energy\") " +
                        "|> range(start: %ds, stop: %ds) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"device_energy_raw\") " +
                        "|> filter(fn: (r) => r[\"deviceUUID\"] == \"%s\")",
                startTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                endTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),
                deviceUUID
        );

        try {
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(query);
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Map<String, Object> values = record.getValues();
                    EnergyDataPoint point = EnergyDataPoint.builder()
                            .deviceUUID(values.get("deviceUUID") != null ? values.get("deviceUUID").toString() : null)
                            .current(getDoubleValue(values.get("current")))
                            .voltage(getDoubleValue(values.get("voltage")))
                            .power(getDoubleValue(values.get("power")))
                            .energy(getDoubleValue(values.get("energy")))
                            .timestamp(record.getTime())
                            .build();
                    result.add(point);
                }
            }
        } catch (Exception e) {
            log.error("查询InfluxDB失败: {}", e.getMessage(), e);
        }

        return result;
    }

    private Double getDoubleValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Double calculateDailyEnergy(String deviceUUID, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().plusDays(1).atStartOfDay();

        List<EnergyDataPoint> dataPoints = queryHistoryData(deviceUUID, startOfDay, endOfDay);
        return dataPoints.stream()
                .mapToDouble(EnergyDataPoint::getEnergy)
                .sum();
    }

    @Override
    public Double calculateWeeklyEnergy(String deviceUUID, LocalDateTime date) {
        LocalDate localDate = date.toLocalDate();
        LocalDate startOfWeek = localDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(7);

        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = endOfWeek.atStartOfDay();

        List<EnergyDataPoint> dataPoints = queryHistoryData(deviceUUID, start, end);
        return dataPoints.stream()
                .mapToDouble(EnergyDataPoint::getEnergy)
                .sum();
    }

    @Override
    public EnergyDataPoint getRealtimeData(String deviceUUID) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(5);

        List<EnergyDataPoint> dataPoints = queryHistoryData(deviceUUID, startTime, endTime);
        if (dataPoints.isEmpty()) {
            return null;
        }

        // 返回最新的数据点
        return dataPoints.stream()
                .max(Comparator.comparing(EnergyDataPoint::getTimestamp))
                .orElse(null);
    }
}