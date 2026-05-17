package org.iotf.collectanalyzeservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iotf.collectanalyzeservice.mapper.TAlarmLogMapper;
import org.iotf.collectanalyzeservice.mapper.TAlarmMapper;
import org.iotf.collectanalyzeservice.service.EnergyDataQueryService;
import org.iotf.collectanalyzeservice.service.ITAlarmService;
import org.iotf.collectanalyzeservice.service.ITDeviceService;
import org.iotf.collectanalyzeservice.service.MqttPublisher;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.collect_analyze.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.iotf.requestFormation.collect_analyze.acknowledgeRequest;
import org.iotf.requestFormation.collect_analyze.alarmListRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private final EnergyDataQueryService energyDataQueryService;
    private final MqttPublisher mqttPublisher;

    // 默认阈值常量（与C代码对应）
    private static final double DEFAULT_OVERLOAD_THRESHOLD = 2000;  // 默认过载阈值2000W
    private static final double DEFAULT_HIGH_ENERGY_THRESHOLD = 500; // 默认高能耗阈值500Wh
    private static final double DEFAULT_CURRENT_THRESHOLD = 10;      // 默认电流过载阈值10A

    @Override
    public void checkAlarms(EnergyDataDTO data) {
        log.debug("开始检查告警: deviceId={}", data.getDeviceId());

        // 获取设备配置的阈值，若无则使用默认值
        TDevice device = deviceService.getDeviceByUUID(data.getDeviceUUID());
        double overloadThreshold = device != null && device.getOverload_threshold() != null ?
                device.getOverload_threshold() : DEFAULT_OVERLOAD_THRESHOLD;
        double high_energy_threshold = device != null && device.getHigh_energy_threshold() != null ?
                device.getHigh_energy_threshold() : DEFAULT_HIGH_ENERGY_THRESHOLD;
        double current_threshold = device != null && device.getCurrent_threshold() != null ?
                device.getCurrent_threshold() : DEFAULT_CURRENT_THRESHOLD;


        // 检查各类告警
        AlarmInfoDTO overloadAlarm = checkOverload(data, overloadThreshold);
        AlarmInfoDTO highEnergyAlarm = checkHighEnergy(data, high_energy_threshold);
        AlarmInfoDTO leakAlarm = checkElectricLeak(data, current_threshold);
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

        Double dailyEnergy = energyDataQueryService.calculateDailyEnergy(data.getDeviceUUID(), LocalDateTime.now());
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

        if (device == null) return;

        // 创建告警记录
        TAlarm alarm = TAlarm.builder()
                .device_id(device.getDevice_id())
                .device_name(deviceName)
                .device_uuid(alarmInfo.getDeviceUUID())
                .alarm_type(alarmInfo.getAlarmType())
                .alarm_level(alarmInfo.getAlarmLevel())
                .status("TRIGGERED")
                .trigger_value(alarmInfo.getTriggerValue())
                .threshold(alarmInfo.getThreshold())
                .description(alarmInfo.getDescription())
                .triggered_time(alarmInfo.getTimestamp())
                .create_time(LocalDateTime.now())
                .update_time(LocalDateTime.now())
                .user_id(device.getUser_id())
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
                .user_id(device.getUser_id())
                .build();

        alarmLogMapper.insert(alarmLog);

        log.info("创建告警: alarmId={}, type={}, device={}",
                alarm.getAlarm_id(), alarm.getAlarm_type(), alarm.getDevice_uuid());

        // 通知用户
        notifyUser(alarm);
    }

    @Override
//    @Transactional
    public Boolean acknowledgeAlarm(acknowledgeRequest request, JwtPayload payload) {
        TAlarm alarm = baseMapper.selectById(request.alarm_id());

        String fromStatus = alarm.getStatus();
        alarm.setStatus("ACKNOWLEDGED");
        alarm.setAcknowledged_time(LocalDateTime.now());
        alarm.setAcknowledged_by(payload.getUser_id());
        alarm.setUpdate_time(LocalDateTime.now());



        // 创建告警日志
        TAlarmLog alarmLog = TAlarmLog.builder()
                .alarm_id(request.alarm_id())
                .from_status(fromStatus)
                .to_status("ACKNOWLEDGED")
                .change_reason("用户确认")
                .changed_time(LocalDateTime.now())
                .create_time(LocalDateTime.now())
                .update_time(LocalDateTime.now())
                .user_id(payload.getUser_id())
                .build();

        alarmLogMapper.insert(alarmLog);

        log.info("告警已确认: alarmId={}, acknowledgedBy={}", request.alarm_id(), payload.getUser_id());

        return baseMapper.updateById(alarm)  >= 1;

    }

    @Override
//    @Transactional
    public void resolveAlarm(Long alarmId, JwtPayload payload) {
        TAlarm alarm = baseMapper.selectById(alarmId);
        if (alarm == null) {
            log.warn("告警不存在: alarmId={}", alarmId);
            return;
        }

        String fromStatus = alarm.getStatus();
        alarm.setStatus("RESOLVED");
        alarm.setResolved_time(LocalDateTime.now());
        alarm.setUpdate_time(LocalDateTime.now());

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
                .user_id(payload.getUser_id())
                .build();

        alarmLogMapper.insert(alarmLog);

        log.info("告警已恢复: alarmId={}", alarmId);
    }

//    @Override
//    public List<TAlarm> getAlarmsByDeviceUUID(String deviceUUID) {
//        LambdaQueryWrapper<TAlarm> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(TAlarm::getDevice_uuid, deviceUUID)
//                .orderByDesc(TAlarm::getTriggered_time);
//        return baseMapper.selectList(queryWrapper);
//    }
//
//    @Override
//    public List<TAlarm> getUnHandledAlarms() {
//        LambdaQueryWrapper<TAlarm> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(TAlarm::getStatus, "TRIGGERED")
//                .orderByDesc(TAlarm::getTriggered_time);
//        return baseMapper.selectList(queryWrapper);
//    }

    @Override
    public void notifyUser(TAlarm alarm) {

        String alarmTypeStr = getAlarmTypeString(alarm.getAlarm_type());

        log.info("[告警通知] 类型: {}, 设备: {}, 描述: {}",
                alarmTypeStr, alarm.getDevice_name(), alarm.getDescription());

        mqttPublisher.alarmPush(alarm);

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

    @Override
    public IPage<TAlarm> alarmList(alarmListRequest request, JwtPayload payload) {


        Page<TAlarm> page = new Page<>(request.page(), request.size());
        LambdaQueryWrapper<TAlarm> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(TAlarm::getUser_id, payload.getUser_id());
        wrapper.eq(request.device_id() != null, TAlarm::getDevice_id, request.device_id());
        wrapper.eq(StringUtils.hasText(request.status()), TAlarm::getStatus, request.status());
        wrapper.like(StringUtils.hasText(request.alarm_type()), TAlarm::getAlarm_type, request.alarm_type());
        wrapper.like(StringUtils.hasText(request.device_name()), TAlarm::getDevice_name, request.device_name());

        wrapper.orderByDesc(TAlarm::getCreate_time);

        return baseMapper.selectPage(page, wrapper);
    }






}
