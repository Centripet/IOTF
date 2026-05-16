package org.iotf.collectanalyzeservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.mapper.TAlarmLogMapper;
import org.iotf.collectanalyzeservice.mapper.TAlarmMapper;
import org.iotf.collectanalyzeservice.service.EnergyDataService;
import org.iotf.collectanalyzeservice.service.ITAlarmService;
import org.iotf.collectanalyzeservice.service.ITDeviceService;
import org.iotf.entity.collect_analyze.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 告警记录表 服务实现类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TAlarmServiceImpl extends ServiceImpl<TAlarmMapper, TAlarm> implements ITAlarmService {

    private final TAlarmLogMapper alarmLogMapper;
    private final ITDeviceService deviceService;
    private final EnergyDataService energyDataService;

    // 默认阈值常量（与C代码对应）
    private static final double DEFAULT_OVERLOAD_THRESHOLD = 2000;  // 默认过载阈值2000W
    private static final double DEFAULT_HIGH_ENERGY_THRESHOLD = 500; // 默认高能耗阈值500Wh
    private static final double DEFAULT_CURRENT_THRESHOLD = 10;      // 默认电流过载阈值10A

    @Override
    public void checkAlarms(EnergyDataDTO data) {
        log.debug("开始检查告警: deviceId={}", data.getDeviceId());

        // 获取设备配置的阈值，若无则使用默认值
        TDevice device = deviceService.getDeviceByUUID(data.getDeviceUUID());
        double overloadThreshold = device != null && device.getThreshold() != null ?
                device.getThreshold() : DEFAULT_OVERLOAD_THRESHOLD;

        // 检查各类告警
        AlarmInfoDTO overloadAlarm = checkOverload(data, overloadThreshold);
        AlarmInfoDTO highEnergyAlarm = checkHighEnergy(data, DEFAULT_HIGH_ENERGY_THRESHOLD);
        AlarmInfoDTO leakAlarm = checkElectricLeak(data, DEFAULT_CURRENT_THRESHOLD);
        AlarmInfoDTO faultAlarm = checkDeviceFault(data);

        // 如果有告警则创建告警记录
        if (overloadAlarm != null) {
            createAlarm(overloadAlarm);
        }
        if (highEnergyAlarm != null) {
            createAlarm(highEnergyAlarm);
        }
        if (leakAlarm != null) {
            createAlarm(leakAlarm);
        }
        if (faultAlarm != null) {
            createAlarm(faultAlarm);
        }
    }

    @Override
    public AlarmInfoDTO checkOverload(EnergyDataDTO data, double threshold) {
        if (data.getIsOn() != null && data.getIsOn() &&
                data.getPower() != null && data.getPower() > threshold) {
            return AlarmInfoDTO.builder()
                    .alarmType("OVERLOAD")
                    .alarmLevel("CRITICAL")
                    .deviceId(data.getDeviceId())
                    .deviceUUID(data.getDeviceUUID())
                    .timestamp(LocalDateTime.now())
                    .triggerValue(data.getPower())
                    .threshold(threshold)
                    .description(String.format("设备功率超过阈值(%.2fW > %.2fW)", data.getPower(), threshold))
                    .isHandled(false)
                    .build();
        }
        return null;
    }

    @Override
    public AlarmInfoDTO checkHighEnergy(EnergyDataDTO data, double threshold) {
        if (data.getDeviceUUID() == null) {
            return null;
        }

        Double dailyEnergy = energyDataService.calculateDailyEnergy(data.getDeviceUUID(), LocalDateTime.now());
        if (dailyEnergy != null && dailyEnergy > threshold) {
            return AlarmInfoDTO.builder()
                    .alarmType("HIGH_ENERGY")
                    .alarmLevel("WARNING")
                    .deviceId(data.getDeviceId())
                    .deviceUUID(data.getDeviceUUID())
                    .timestamp(LocalDateTime.now())
                    .triggerValue(dailyEnergy)
                    .threshold(threshold)
                    .description(String.format("设备日能耗过高(%.2fWh > %.2fWh)", dailyEnergy, threshold))
                    .isHandled(false)
                    .build();
        }
        return null;
    }

    @Override
    public AlarmInfoDTO checkElectricLeak(EnergyDataDTO data, double currentThreshold) {
        // 漏电判断: 电流异常且功率与电流不匹配
        if (data.getCurrent() != null && data.getVoltage() != null && data.getPower() != null &&
                data.getCurrent() > currentThreshold &&
                data.getPower() < data.getCurrent() * data.getVoltage() * 0.5) {
            return AlarmInfoDTO.builder()
                    .alarmType("ELECTRIC_LEAK")
                    .alarmLevel("CRITICAL")
                    .deviceId(data.getDeviceId())
                    .deviceUUID(data.getDeviceUUID())
                    .timestamp(LocalDateTime.now())
                    .triggerValue(data.getCurrent())
                    .threshold(currentThreshold)
                    .description(String.format("设备可能存在漏电(电流异常: %.2fA)", data.getCurrent()))
                    .isHandled(false)
                    .build();
        }
        return null;
    }

    @Override
    public AlarmInfoDTO checkDeviceFault(EnergyDataDTO data) {
        if (data.getIsFault() != null && data.getIsFault()) {
            return AlarmInfoDTO.builder()
                    .alarmType("DEVICE_FAULT")
                    .alarmLevel("WARNING")
                    .deviceId(data.getDeviceId())
                    .deviceUUID(data.getDeviceUUID())
                    .timestamp(LocalDateTime.now())
                    .description("设备传感器故障，请检查设备")
                    .isHandled(false)
                    .build();
        }
        return null;
    }

    @Override
//    @Transactional
    public void createAlarm(AlarmInfoDTO alarmInfo) {
        // 检查是否已存在相同类型的未恢复告警
        LambdaQueryWrapper<TAlarm> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TAlarm::getDevice_uuid, alarmInfo.getDeviceUUID())
                .eq(TAlarm::getAlarm_type, alarmInfo.getAlarmType())
                .eq(TAlarm::getStatus, "TRIGGERED");

        List<TAlarm> existingAlarms = baseMapper.selectList(queryWrapper);
        if (!existingAlarms.isEmpty()) {
            log.debug("已存在相同类型的未恢复告警，跳过创建: deviceUUID={}, alarmType={}",
                    alarmInfo.getDeviceUUID(), alarmInfo.getAlarmType());
            return;
        }

        // 获取设备信息
        TDevice device = deviceService.getDeviceByUUID(alarmInfo.getDeviceUUID());
        String deviceName = device != null ? device.getDevice_name() : alarmInfo.getDeviceId();

        // 创建告警记录
        TAlarm alarm = TAlarm.builder()
                .device_id(device != null ? device.getDevice_id() : null)
                .device_name(deviceName)
                .device_uuid(alarmInfo.getDeviceUUID())
                .alarm_type(alarmInfo.getAlarmType())
                .alarm_level(alarmInfo.getAlarmLevel())
                .status("TRIGGERED")
                .trigger_value(alarmInfo.getTriggerValue())
                .threshold(alarmInfo.getThreshold())
                .description(alarmInfo.getDescription())
                .triggered_time(alarmInfo.getTimestamp())
                .created_time(LocalDateTime.now())
                .updated_time(LocalDateTime.now())
                .build();

        baseMapper.insert(alarm);

        // 创建告警日志
        TAlarmLog alarmLog = TAlarmLog.builder()
                .alarm_id(alarm.getAlarm_id())
                .from_status(null)
                .to_status("TRIGGERED")
                .change_reason("告警触发")
                .changed_time(LocalDateTime.now())
                .changed_time(LocalDateTime.now())
                .update_time(LocalDateTime.now())
                .build();

        alarmLogMapper.insert(alarmLog);

        log.info("创建告警: alarmId={}, type={}, device={}",
                alarm.getAlarm_id(), alarm.getAlarm_type(), alarm.getDevice_uuid());

        // 通知用户
        notifyUser(alarm);
    }

    @Override
//    @Transactional
    public void acknowledgeAlarm(Long alarmId, String acknowledgedBy) {
        TAlarm alarm = baseMapper.selectById(alarmId);
        if (alarm == null) {
            log.warn("告警不存在: alarmId={}", alarmId);
            return;
        }

        String fromStatus = alarm.getStatus();
        alarm.setStatus("ACKNOWLEDGED");
        alarm.setAcknowledged_time(LocalDateTime.now());
        alarm.setAcknowledged_by(acknowledgedBy);
        alarm.setUpdated_time(LocalDateTime.now());

        baseMapper.updateById(alarm);

        // 创建告警日志
        TAlarmLog alarmLog = TAlarmLog.builder()
                .alarm_id(alarmId)
                .from_status(fromStatus)
                .to_status("ACKNOWLEDGED")
                .change_reason("用户确认")
                .changed_time(LocalDateTime.now())
                .create_time(LocalDateTime.now())
                .update_time(LocalDateTime.now())
                .build();

        alarmLogMapper.insert(alarmLog);

        log.info("告警已确认: alarmId={}, acknowledgedBy={}", alarmId, acknowledgedBy);
    }

    @Override
//    @Transactional
    public void resolveAlarm(Long alarmId) {
        TAlarm alarm = baseMapper.selectById(alarmId);
        if (alarm == null) {
            log.warn("告警不存在: alarmId={}", alarmId);
            return;
        }

        String fromStatus = alarm.getStatus();
        alarm.setStatus("RESOLVED");
        alarm.setResolved_time(LocalDateTime.now());
        alarm.setUpdated_time(LocalDateTime.now());

        baseMapper.updateById(alarm);

        // 创建告警日志
        TAlarmLog alarmLog = TAlarmLog.builder()
                .alarm_id(alarmId)
                .from_status(fromStatus)
                .to_status("RESOLVED")
                .change_reason("告警恢复")
                .changed_time(LocalDateTime.now())
                .create_time(LocalDateTime.now())
                .update_time(LocalDateTime.now())
                .build();

        alarmLogMapper.insert(alarmLog);

        log.info("告警已恢复: alarmId={}", alarmId);
    }

    @Override
    public List<TAlarm> getAlarmsByDeviceUUID(String deviceUUID) {
        LambdaQueryWrapper<TAlarm> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TAlarm::getDevice_uuid, deviceUUID)
                .orderByDesc(TAlarm::getTriggered_time);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<TAlarm> getUnHandledAlarms() {
        LambdaQueryWrapper<TAlarm> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TAlarm::getStatus, "TRIGGERED")
                .orderByDesc(TAlarm::getTriggered_time);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void notifyUser(TAlarm alarm) {
        // 这里可以实现多渠道通知：APP推送、短信、邮件等
        String alarmTypeStr = getAlarmTypeString(alarm.getAlarm_type());

        log.info("[告警通知] 类型: {}, 设备: {}, 描述: {}",
                alarmTypeStr, alarm.getDevice_name(), alarm.getDescription());

        // TODO: 集成APP推送服务
        // TODO: 集成短信通知服务
        // TODO: 集成邮件通知服务
    }

    /**
     * 获取告警类型描述字符串
     */
    private String getAlarmTypeString(String alarmType) {
        if (alarmType == null) {
            return "未知报警";
        }
        return switch (alarmType) {
            case "OVERLOAD" -> "设备过载";
            case "HIGH_ENERGY" -> "异常高能耗";
            case "ELECTRIC_LEAK" -> "漏电警告";
            case "DEVICE_FAULT" -> "设备故障";
            default -> "未知报警";
        };
    }
}