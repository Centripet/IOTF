package org.iotf.collectanalyzeservice.service;

import org.iotf.collectanalyzeservice.model.EnergyDataPoint;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Energy data query and statistics service.
 */
public interface EnergyDataQueryService {

    List<EnergyDataPoint> queryHistoryData(String deviceUUID, LocalDateTime startTime, LocalDateTime endTime);

    Double calculateDailyEnergy(String deviceUUID, LocalDateTime date);

    Double calculateWeeklyEnergy(String deviceUUID, LocalDateTime date);

    EnergyDataPoint getRealtimeData(String deviceUUID);
}
