package org.iotf.collectanalyzeservice.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.model.EnergyDataPoint;
import org.iotf.collectanalyzeservice.service.EnergyDataQueryService;
import org.iotf.entity.collect_analyze.EnergyAggDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Query-only access to energy data stored in InfluxDB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyDataQueryServiceImpl implements EnergyDataQueryService {

    private final InfluxDBClient influxDBClient;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.agg-bucket:energy_agg}")
    private String aggBucket;

    @Override
    public List<EnergyDataPoint> queryHistoryData(String deviceUUID, LocalDateTime startTime, LocalDateTime endTime) {
        List<EnergyDataPoint> result = new ArrayList<>();

        String query = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: %ds, stop: %ds) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"device_energy_raw\") " +
                        "|> filter(fn: (r) => r[\"deviceUUID\"] == \"%s\")",
                bucket,
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
            log.error("Query InfluxDB failed: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Double calculateDailyEnergy(String deviceUUID, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().plusDays(1).atStartOfDay();

        return queryHistoryData(deviceUUID, startOfDay, endOfDay).stream()
                .map(EnergyDataPoint::getEnergy)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    @Override
    public Double calculateWeeklyEnergy(String deviceUUID, LocalDateTime date) {
        LocalDate localDate = date.toLocalDate();
        LocalDate startOfWeek = localDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(7);

        return queryHistoryData(deviceUUID, startOfWeek.atStartOfDay(), endOfWeek.atStartOfDay()).stream()
                .map(EnergyDataPoint::getEnergy)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    @Override
    public EnergyDataPoint getRealtimeData(String deviceUUID) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(5);

        return queryHistoryData(deviceUUID, startTime, endTime).stream()
                .max(Comparator.comparing(EnergyDataPoint::getTimestamp))
                .orElse(null);
    }

    @Override
    public EnergyAggDTO queryLatestDeviceAgg(Long userId, Long deviceId, String period) {
        return queryAgg(userId, deviceId, period, true, 1, 1, true).stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public EnergyAggDTO queryLatestUserAgg(Long userId, String period) {
        return queryAgg(userId, null, period, false, 1, 1, true).stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<EnergyAggDTO> queryDeviceAggPage(Long userId, Long deviceId, String period, Integer page, Integer size) {
        return queryAgg(userId, deviceId, period, true, page, size, false);
    }

    @Override
    public List<EnergyAggDTO> queryUserAggPage(Long userId, String period, Integer page, Integer size) {
        return queryAgg(userId, null, period, false, page, size, false);
    }

    @Override
    public Long queryDeviceAggCount(Long userId, Long deviceId, String period) {
        return queryAggCount(userId, deviceId, period, true);
    }

    @Override
    public Long queryUserAggCount(Long userId, String period) {
        return queryAggCount(userId, null, period, false);
    }

    private List<EnergyAggDTO> queryAgg(Long userId, Long deviceId, String period, boolean byDevice,
                                        Integer page, Integer size, boolean latestOnly) {
        String measurement = resolveAggMeasurement(period, byDevice);
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : size;
        int offset = latestOnly ? 0 : (safePage - 1) * safeSize;

        StringBuilder query = new StringBuilder();
        query.append(String.format("""
                from(bucket: "%s")
                  |> range(start: 0)
                  |> filter(fn: (r) => r["_measurement"] == "%s")
                """, aggBucket, measurement, userId));

        if (byDevice) {
            query.append(String.format("  |> filter(fn: (r) => r[\"device_id\"] == \"%s\")%n", deviceId));
        } else {
            query.append(String.format("  |> filter(fn: (r) => r[\"user_id\"] == \"%s\")%n", userId));
        }

        // 按设备聚合保留device_id/user_id，按用户聚合只保留user_id，防止tag不一致导致pivot拆行
        String keepColumns = byDevice
                ? "[\"_time\", \"_field\", \"_value\", \"device_id\", \"user_id\"]"
                : "[\"_time\", \"_field\", \"_value\", \"user_id\"]";
        query.append(String.format("""
                  |> keep(columns: %s)
                  |> pivot(rowKey: ["_time"], columnKey: ["_field"], valueColumn: "_value")
                """, keepColumns));

        if (latestOnly) {
            query.append("  |> sort(columns: [\"_time\"], desc: true)\n");
        } else {
            query.append("  |> sort(columns: [\"_time\"], desc: false)\n");
        }

        query.append(String.format("  |> limit(n: %d, offset: %d)%n", latestOnly ? 1 : safeSize, offset));

        List<EnergyAggDTO> result = new ArrayList<>();
        try {
            List<FluxTable> tables = influxDBClient.getQueryApi().query(query.toString());
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    result.add(toEnergyAggDTO(record, period));
                }
            }
        } catch (Exception e) {
            log.error("Query energy aggregate failed: {}", e.getMessage(), e);
        }

        return result;
    }

    private Long queryAggCount(Long userId, Long deviceId, String period, boolean byDevice) {
        String measurement = resolveAggMeasurement(period, byDevice);

        StringBuilder query = new StringBuilder();
        query.append(String.format("""
                from(bucket: "%s")
                  |> range(start: 0)
                  |> filter(fn: (r) => r["_measurement"] == "%s")
                """, aggBucket, measurement));

        if (byDevice) {
            query.append(String.format("  |> filter(fn: (r) => r[\"device_id\"] == \"%s\")%n", deviceId));
        } else {
            query.append(String.format("  |> filter(fn: (r) => r[\"user_id\"] == \"%s\")%n", userId));
        }

        query.append("""
                  |> pivot(rowKey: ["_time"], columnKey: ["_field"], valueColumn: "_value")
                  |> count()
                """);

        try {
            List<FluxTable> tables = influxDBClient.getQueryApi().query(query.toString());
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object countVal = record.getValueByKey("energy_sum_wh");
                    if (countVal instanceof Number) {
                        return ((Number) countVal).longValue();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Query energy aggregate count failed: {}", e.getMessage(), e);
        }

        return 0L;
    }

    private EnergyAggDTO toEnergyAggDTO(FluxRecord record, String period) {
        Map<String, Object> values = record.getValues();
        return EnergyAggDTO.builder()
                .time(record.getTime())
                .period(period)
                .user_id(getLongValue(values.get("user_id")))
                .device_id(getLongValue(values.get("device_id")))
                .energy_sum_wh(getDoubleValue(values.get("energy_sum_wh")))
                .power_avg_w(getDoubleValue(values.get("power_avg_w")))
                .power_max_w(getDoubleValue(values.get("power_max_w")))
                .sample_count(getLongValue(values.get("sample_count")))
                .build();
    }

    private String resolveAggMeasurement(String period, boolean byDevice) {
        String suffix = period == null ? "" : period.trim().toLowerCase(Locale.ROOT);
        return (byDevice ? "device_energy_" : "user_energy_") + suffix;
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

    private Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Double queryUserEnergySum(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        long startEpoch = startTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long endEpoch = endTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

        String query = String.format("""
                from(bucket: "%s")
                  |> range(start: %ds, stop: %ds)
                  |> filter(fn: (r) => r["_measurement"] == "user_energy_hourly")
                  |> filter(fn: (r) => r["user_id"] == "%s")
                  |> filter(fn: (r) => r["_field"] == "energy_sum_wh")
                  |> sum()
                """, aggBucket, startEpoch, endEpoch, userId);

        try {
            List<FluxTable> tables = influxDBClient.getQueryApi().query(query);
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object value = record.getValue();
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Query user energy sum failed: userId={}, start={}, end={}, error={}",
                    userId, startTime, endTime, e.getMessage(), e);
        }

        return 0.0;
    }

    @Override
    public List<Map<String, Object>> queryDeviceRawData(Long deviceId, Integer page, Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 50 : Math.min(size, 100);
        int offset = (safePage - 1) * safeSize;

        String query = String.format("""
                from(bucket: "%s")
                  |> range(start: -7d)
                  |> filter(fn: (r) => r["_measurement"] == "device_energy_raw")
                  |> filter(fn: (r) => r["device_id"] == "%s")
                  |> keep(columns: ["_time", "_field", "_value"])
                  |> pivot(rowKey: ["_time"], columnKey: ["_field"], valueColumn: "_value")
                  |> sort(columns: ["_time"], desc: true)
                  |> limit(n: %d, offset: %d)
                """, bucket, deviceId, safeSize, offset);

        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<FluxTable> tables = influxDBClient.getQueryApi().query(query);
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("time", record.getTime());
                    // 提取所有字段值
                    record.getValues().forEach((key, value) -> {
                        if (!key.startsWith("_") && value != null) {
                            row.put(key, value);
                        }
                    });
                    if (row.size() > 1) { // 至少有时间+一个字段
                        result.add(row);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Query device raw data failed: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }

        return result;
    }
}
