package org.iotf.entity.collect_analyze.noUse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmPush {

    private Long alarm_id;         // 告警ID，App 可用来跳转详情
    private Long device_id;      // 设备ID
    private String device_name;    // 设备名称（客厅空调）
    private String alarm_type;     // 告警类型：OVERLOAD / RECOVERY
    private String message;       // 推送文案："客厅空调 过载，当前功率 2150W"
    private Long timestamp;       // 推送时间戳
}
