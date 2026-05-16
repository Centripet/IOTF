package org.iotf.entity.collect_analyze;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警信息DTO
 * 对应C代码中的AlarmInfo结构体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmInfoDTO {

    /**
     * 报警类型: OVERLOAD-过载, HIGH_ENERGY-高能耗, ELECTRIC_LEAK-漏电, DEVICE_FAULT-设备故障
     */
    private String alarmType;

    /**
     * 关联设备ID
     */
    private String deviceId;

    /**
     * 设备UUID
     */
    private String deviceUUID;

    /**
     * 报警时间
     */
    private LocalDateTime timestamp;

    /**
     * 报警描述
     */
    private String description;

    /**
     * 处理状态
     */
    private Boolean isHandled;

    /**
     * 触发时的值
     */
    private Double triggerValue;

    /**
     * 告警阈值
     */
    private Double threshold;

    /**
     * 告警级别: NORMAL-普通, WARNING-警告, CRITICAL-严重
     */
    private String alarmLevel;
}