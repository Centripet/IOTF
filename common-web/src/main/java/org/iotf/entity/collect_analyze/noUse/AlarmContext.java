package org.iotf.entity.collect_analyze.noUse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class AlarmContext {

    private Long alarm_id;           // 对应 t_alarm.id
    private String status;          // 当前状态：TRIGGERED / ACKNOWLEDGED / RESOLVED
    private Instant triggered_time;  // 首次触发时间
    private boolean upgraded;       // 是否已升级
}
