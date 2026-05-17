package org.iotf.collectanalyzeservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.collect_analyze.AlarmInfoDTO;
import org.iotf.entity.collect_analyze.EnergyDataDTO;
import org.iotf.entity.collect_analyze.TAlarm;
import com.baomidou.mybatisplus.extension.service.IService;
import org.iotf.requestFormation.collect_analyze.acknowledgeRequest;
import org.iotf.requestFormation.collect_analyze.alarmListRequest;

import java.util.List;

/**
 * <p>
 * 告警记录表 服务类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
public interface ITAlarmService extends IService<TAlarm> {

    /**
     * 检查所有告警类型
     * @param data 设备上报数据
     */
    void checkAlarms(EnergyDataDTO data);

    /**
     * 检查过载告警
     * @param data 设备上报数据
     * @param threshold 过载阈值(W)
     * @return 告警信息，如果没有告警则返回null
     */
    AlarmInfoDTO checkOverload(EnergyDataDTO data, double threshold);

    /**
     * 检查高能耗告警
     * @param data 设备上报数据
     * @param threshold 高能耗阈值(Wh)
     * @return 告警信息，如果没有告警则返回null
     */
    AlarmInfoDTO checkHighEnergy(EnergyDataDTO data, double threshold);

    /**
     * 检查漏电告警
     * @param data 设备上报数据
     * @param currentThreshold 电流阈值(A)
     * @return 告警信息，如果没有告警则返回null
     */
    AlarmInfoDTO checkElectricLeak(EnergyDataDTO data, double currentThreshold);

    /**
     * 检查设备故障告警
     * @param data 设备上报数据
     * @return 告警信息，如果没有告警则返回null
     */
    AlarmInfoDTO checkDeviceFault(EnergyDataDTO data);

    /**
     * 创建并保存告警记录
     * @param alarmInfo 告警信息
     */
    void createAlarm(AlarmInfoDTO alarmInfo);

    /**
     * 处理告警（标记为已确认）
     */
    Boolean acknowledgeAlarm(acknowledgeRequest request, JwtPayload payload);

    /**
     * 恢复告警（标记为已恢复）
     * @param alarmId 告警ID
     */
    void resolveAlarm(Long alarmId, JwtPayload payload);

//    /**
//     * 根据设备UUID查询告警列表
//     * @param deviceUUID 设备UUID
//     * @return 告警列表
//     */
//    List<TAlarm> getAlarmsByDeviceUUID(String deviceUUID);
//
//    /**
//     * 查询未处理的告警列表
//     * @return 未处理告警列表
//     */
//    List<TAlarm> getUnHandledAlarms();

    /**
     * 通知用户告警（多渠道通知）
     * @param alarm 告警信息
     */
    void notifyUser(TAlarm alarm);

    IPage<TAlarm> alarmList(alarmListRequest request, JwtPayload payload);

}