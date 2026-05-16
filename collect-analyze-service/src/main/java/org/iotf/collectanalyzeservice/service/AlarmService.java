//package org.iotf.collectanalyzeservice.service;
//
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.iotf.collectanalyzeservice.mapper.TAlarmLogMapper;
//import org.iotf.collectanalyzeservice.mapper.TAlarmMapper;
//import org.iotf.entity.collect_analyze.AlarmContext;
//import org.iotf.entity.collect_analyze.AlarmPush;
//import org.iotf.entity.collect_analyze.TAlarm;
//import org.iotf.entity.collect_analyze.TAlarmLog;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AlarmService {
//
//    private final TAlarmMapper alarmMapper;
//    private final TAlarmLogMapper alarmLogMapper;
//    private final MqttPublisher mqttPublisher;
//
//    // 内存中维护当前告警状态
//    private final Map<Long, AlarmContext> activeAlarms = new ConcurrentHashMap<>();
//
//    private static final int UPGRADE_MINUTES = 30;  // 30分钟升级
//
//    /**
//     * 检测并处理告警
//     */
//    public void checkAndHandle(
//            Long device_id,
//            String device_name,
//            double currentPower,
//            double threshold
//    ) {
//        if (currentPower > threshold) {
//            handleOverload(device_id, device_name, currentPower, threshold);
//        } else {
//            handleRecovery(device_id, device_name);
//        }
//    }
//
//    /**
//     * 处理过载告警
//     */
//    private void handleOverload(
//            Long device_id,
//            String device_name,
//            double power,
//            double threshold
//    ) {
//        AlarmContext ctx = activeAlarms.get(device_id);
//
//        if (ctx == null || ctx.getStatus().equals("RESOLVED")) {
//            // 首次触发 或 之前已恢复 → 新建告警
//            TAlarm alarm = createAlarm(device_id, device_name, "OVERLOAD", power, threshold);
//            pushAlarm(device_id, device_name, alarm.getAlarm_id(), "OVERLOAD",
//                    String.format("%s 过载，当前功率 %.1fW，超过阈值 %.1fW", device_name, power, threshold));
//
//            activeAlarms.put(
//                    device_id,
//                    AlarmContext.builder()
//                            .alarm_id(alarm.getAlarm_id())
//                            .status("TRIGGERED")
//                            .triggered_time(Instant.now())
//                            .build()
//            );
//
//        } else if (ctx.getStatus().equals("TRIGGERED")) {
//            // 持续告警中 → 检查是否需要升级
//            long minutes = Duration.between(ctx.getTriggered_time(), Instant.now()).toMinutes();
//
//            if (minutes >= UPGRADE_MINUTES && !ctx.isUpgraded()) {
//                ctx.setUpgraded(true);
//
//                alarmMapper.update(
//                        new UpdateWrapper<TAlarm>()
//                                .eq(TAlarm.ALARM_ID, ctx.getAlarm_id())
//                                .set(TAlarm.ALARM_LEVEL, "CRITICAL")
//                );
//
//                pushAlarm(device_id, device_name, ctx.getAlarm_id(), "OVERLOAD",
//                        String.format("【升级】%s 持续过载 %d 分钟，请尽快处理！", device_name, minutes));
//            }
//            // 不新建记录，不重复推送
//        }
//        // ACKNOWLEDGED 状态：等待用户处理，不操作
//    }
//
//    /**
//     * 处理恢复
//     */
//    private void handleRecovery(Long device_id, String device_name) {
//        AlarmContext ctx = activeAlarms.get(device_id);
//
//        if (ctx != null && !ctx.getStatus().equals("RESOLVED")) {
//            // 更新告警状态为已恢复
//            alarmMapper.update(
//                    new UpdateWrapper<TAlarm>()
//                            .eq(TAlarm.ALARM_ID, ctx.getAlarm_id())
//                            .set(TAlarm.STATUS, "RESOLVED")
//            );
//
//            alarmLogMapper.insert(
//                    TAlarmLog.builder()
//                            .alarm_id(ctx.getAlarm_id())
//                            .to_status(ctx.getStatus())
//                            .from_status("RESOLVED")
//                            .change_reason("数据自动恢复")
//                            .build()
//            );
//
//            pushAlarm(device_id, device_name, ctx.getAlarm_id(), "RECOVERY",
//                    String.format("%s 已恢复正常", device_name));
//
//            ctx.setStatus("RESOLVED");
//        }
//    }
//
//    /**
//     * 用户确认告警
//     */
//    public void acknowledge(Long alarm_id, Long user_id) {
//        alarmMapper.update(
//                new UpdateWrapper<TAlarm>()
//                        .eq(TAlarm.ALARM_ID, alarm_id)
//                        .set(TAlarm.STATUS, "ACKNOWLEDGED")
//        );
//        alarmLogMapper.insert(
//                TAlarmLog.builder()
//                        .alarm_id(alarm_id)
//                        .to_status("TRIGGERED")
//                        .from_status("ACKNOWLEDGED")
//                        .change_reason("用户 " + user_id + " 确认")
//                        .build()
//        );
//
//    }
//
//    /**
//     * 创建告警记录
//     */
//    private TAlarm createAlarm(Long device_id, String device_name,
//                             String alarm_type, double trigger_value, double threshold) {
//        TAlarm alarm = TAlarm.builder()
//                .device_id(device_id)
//                .device_name(device_name)
//                .alarm_type(alarm_type)
//                .alarm_level("NORMAL")
//                .status("TRIGGERED")
//                .trigger_value(trigger_value)
//                .threshold(threshold)
//                .description(String.format("%s 触发%s告警", device_name, alarm_type))
//                .triggered_time(LocalDateTime.from(Instant.now()))
//                .build();
//        alarmMapper.insert(alarm);
//
//        alarmLogMapper.insert(
//                TAlarmLog.builder()
//                        .alarm_id(alarm.getAlarm_id())
//                        .from_status(null)
//                        .to_status("TRIGGERED")
//                        .change_reason("首次触发")
//                        .build()
//        );
//        return alarm;
//    }
//
//    /**
//     * 推送告警到 EMQX
//     */
//    private void pushAlarm(Long device_id, String device_name, Long alarm_id,
//                           String alarm_type, String message) {
//        AlarmPush push = AlarmPush.builder()
//                .alarm_id(alarm_id)
//                .device_id(device_id)
//                .device_name(device_name)
//                .alarm_type(alarm_type)
//                .message(message)
//                .timestamp(Instant.now().toEpochMilli())
//                .build();
//
//        mqttPublisher.alarmPush(push);
//    }
//}