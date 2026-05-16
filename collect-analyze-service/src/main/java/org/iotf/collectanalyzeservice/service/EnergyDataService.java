package org.iotf.collectanalyzeservice.service;

import org.iotf.entity.collect_analyze.EnergyDataDTO;
import org.iotf.collectanalyzeservice.model.EnergyDataPoint;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 能耗数据服务接口
 * 负责设备数据上报、数据处理和存储
 */
public interface EnergyDataService {

    /**
     * 处理设备上报的数据
     * @param data 设备上报的能耗数据
     */
    void processReportedData(EnergyDataDTO data);

    /**
     * 将EnergyDataDTO转换为InfluxDB数据点
     * @param dto 设备上报数据DTO
     * @return InfluxDB数据点
     */
    EnergyDataPoint convertToDataPoint(EnergyDataDTO dto);

    /**
     * 数据滤波处理（简单滑动平均）
     * @param newVal 新值
     * @param lastVal 旧值
     * @return 滤波后的值
     */
    double filterData(double newVal, double lastVal);

    /**
     * 数据清洗：去除异常值
     * @param data 待清洗的数据
     * @return 清洗后的数据
     */
    EnergyDataDTO cleanData(EnergyDataDTO data);

    /**
     * 存储数据到InfluxDB
     * @param dataPoint InfluxDB数据点
     */
    void storeToInfluxDB(EnergyDataPoint dataPoint);

    /**
     * 查询设备历史数据
     * @param deviceUUID 设备UUID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据列表
     */
    List<EnergyDataPoint> queryHistoryData(String deviceUUID, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计设备日能耗
     * @param deviceUUID 设备UUID
     * @param date 日期
     * @return 日能耗(Wh)
     */
    Double calculateDailyEnergy(String deviceUUID, LocalDateTime date);

    /**
     * 统计设备周能耗
     * @param deviceUUID 设备UUID
     * @param date 日期（所在周）
     * @return 周能耗(Wh)
     */
    Double calculateWeeklyEnergy(String deviceUUID, LocalDateTime date);

    /**
     * 获取设备实时数据
     * @param deviceUUID 设备UUID
     * @return 最新数据点
     */
    EnergyDataPoint getRealtimeData(String deviceUUID);
}