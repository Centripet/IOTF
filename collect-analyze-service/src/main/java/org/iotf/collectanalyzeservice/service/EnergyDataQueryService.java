package org.iotf.collectanalyzeservice.service;

import org.iotf.collectanalyzeservice.model.EnergyDataPoint;
import org.iotf.entity.collect_analyze.EnergyAggDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Energy data query and statistics service.
 */
public interface EnergyDataQueryService {

    List<EnergyDataPoint> queryHistoryData(String deviceUUID, LocalDateTime startTime, LocalDateTime endTime);

    Double calculateDailyEnergy(String deviceUUID, LocalDateTime date);

    Double calculateWeeklyEnergy(String deviceUUID, LocalDateTime date);

    EnergyDataPoint getRealtimeData(String deviceUUID);

    EnergyAggDTO queryLatestDeviceAgg(Long userId, Long deviceId, String period);

    EnergyAggDTO queryLatestUserAgg(Long userId, String period);

    List<EnergyAggDTO> queryDeviceAggPage(Long userId, Long deviceId, String period, Integer page, Integer size);

    List<EnergyAggDTO> queryUserAggPage(Long userId, String period, Integer page, Integer size);

    Long queryDeviceAggCount(Long userId, Long deviceId, String period);

    Long queryUserAggCount(Long userId, String period);

    /**
     * 查询设备的原始能耗数据，用于折线图展示趋势变化
     * @param deviceId 设备ID
     * @param page 页码
     * @param size 每页数量
     * @return 按时间排序的原始数据列表，每条记录包含_time和所有字段值
     */
    List<Map<String, Object>> queryDeviceRawData(Long deviceId, Integer page, Integer size);
}
