package org.iotf.collectanalyzeservice.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.model.EnergyDataPoint;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfluxDBService {

    private final InfluxDBClient influxDBClient;

    // ========== 写入 ==========

    /**
     * 写入单个数据点
     */
    public void writeData(EnergyDataPoint point) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurement(WritePrecision.MS, point);
        log.debug("写入能耗数据: deviceId={}, power={}", point.getDeviceId(), point.getPower());
    }

    /**
     * 批量写入
     */
    public void writeBatchData(List<EnergyDataPoint> points) {
        if (points == null || points.isEmpty()) return;
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writeMeasurements(WritePrecision.MS, points);
        log.info("批量写入能耗数据: {} 条", points.size());
    }

    // ========== 查询 ==========

    /**
     * 查询设备在指定时间范围内的所有数据
     */
    public List<FluxTable> queryDeviceData(String deviceId, Instant start, Instant end) {
        String flux = String.format(
                "from(bucket: \"energy\") " +
                        "|> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._measurement == \"device_energy_raw\") " +
                        "|> filter(fn: (r) => r.deviceId == \"%s\") " +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                start.toString(), end.toString(), deviceId
        );

        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi.query(flux);
    }

    /**
     * 计算设备日总能耗
     */
    public Double queryDailyEnergy(String deviceId) {
        String flux = String.format(
                "from(bucket: \"energy\") " +
                        "|> range(start: -1d) " +
                        "|> filter(fn: (r) => r._measurement == \"device_energy_raw\") " +
                        "|> filter(fn: (r) => r.deviceId == \"%s\") " +
                        "|> filter(fn: (r) => r._field == \"energy\") " +
                        "|> sum()",
                deviceId
        );

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux);

        if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
            return (Double) tables.get(0).getRecords().get(0).getValue();
        }
        return 0.0;
    }

    /**
     * 计算各设备能耗占比
     */
    public List<FluxTable> queryEnergyRatio() {
        String flux =
                "from(bucket: \"energy\") " +
                        "|> range(start: -1d) " +
                        "|> filter(fn: (r) => r._measurement == \"device_energy_raw\") " +
                        "|> filter(fn: (r) => r._field == \"energy\") " +
                        "|> group(columns: [\"deviceId\"]) " +
                        "|> sum()";

        return influxDBClient.getQueryApi().query(flux);
    }

    /**
     * 查询设备最近一次数据
     */
    public List<FluxTable> queryLatestData(String deviceId) {
        String flux = String.format(
                "from(bucket: \"energy\") " +
                        "|> range(start: -10m) " +
                        "|> filter(fn: (r) => r._measurement == \"device_energy_raw\") " +
                        "|> filter(fn: (r) => r.deviceId == \"%s\") " +
                        "|> last() " +
                        "|> pivot(rowKey: [\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\")",
                deviceId
        );

        return influxDBClient.getQueryApi().query(flux);
    }

    /**
     * 删除设备历史数据（保留策略相关）
     */
    public void deleteDeviceData(String deviceId, OffsetDateTime start, OffsetDateTime stop) {
        String predicate = String.format(
                "_measurement=\"device_energy_raw\" AND deviceId=\"%s\"", deviceId
        );

        influxDBClient.getDeleteApi().delete(
                start,          // OffsetDateTime
                stop,           // OffsetDateTime
                predicate,      // 删除条件
                "energy",       // bucket
                "iotf"          // org
        );
    }

}
