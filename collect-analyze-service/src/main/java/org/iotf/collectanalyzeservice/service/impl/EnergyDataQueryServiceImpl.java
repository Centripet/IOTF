package org.iotf.collectanalyzeservice.service.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.model.EnergyDataPoint;
import org.iotf.collectanalyzeservice.service.EnergyDataQueryService;
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
}
