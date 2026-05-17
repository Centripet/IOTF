package org.iotf.entity.collect_analyze;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 告警记录表
 * </p>
 *
 * @author Centripet
 * @since 2026-05-14
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_alarm")
public class TAlarm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "alarm_id", type = IdType.ASSIGN_ID)
    private Long alarm_id;

    /**
     * 设备ID
     */
    private Long device_id;

    /**
     * 设备名称
     */
    private String device_name;

    /**
     * 告警类型: OVERLOAD-过载, HIGH_ENERGY-高能耗, LEAK-漏电, FAULT-设备故障
     */
    private String alarm_type;

    /**
     * 告警级别: NORMAL-普通, WARNING-警告, CRITICAL-严重
     */
    private String alarm_level;

    /**
     * 告警状态: TRIGGERED-已触发, ACKNOWLEDGED-已确认, RESOLVED-已恢复
     */
    private String status;

    /**
     * 触发时的值
     */
    private Double trigger_value;

    /**
     * 告警阈值
     */
    private Double threshold;

    /**
     * 告警描述
     */
    private String description;

    /**
     * 首次触发时间
     */
    private LocalDateTime triggered_time;

    /**
     * 用户确认时间
     */
    private LocalDateTime acknowledged_time;

    /**
     * 恢复时间
     */
    private LocalDateTime resolved_time;

    /**
     * 确认人
     */
    private Long acknowledged_by;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    private String device_uuid;

    public Long user_id;

    public static final String ALARM_ID = "alarm_id";

    public static final String DEVICE_ID = "device_id";

    public static final String DEVICE_NAME = "device_name";

    public static final String ALARM_TYPE = "alarm_type";

    public static final String ALARM_LEVEL = "alarm_level";

    public static final String STATUS = "status";

    public static final String TRIGGER_VALUE = "trigger_value";

    public static final String THRESHOLD = "threshold";

    public static final String DESCRIPTION = "description";

    public static final String TRIGGERED_TIME = "triggered_time";

    public static final String ACKNOWLEDGED_TIME = "acknowledged_time";

    public static final String RESOLVED_TIME = "resolved_time";

    public static final String ACKNOWLEDGED_BY = "acknowledged_by";

    public static final String CREATE_TIME = "created_time";

    public static final String UPDATE_TIME = "updated_time";

    public static final String DEVICE_UUID = "device_uuid";

    public static final String USER_ID = "user_id";

    @Override
    public String toString() {
        return "TAlarm{" +
        "alarm_id = " + alarm_id +
        ", device_id = " + device_id +
        ", device_name = " + device_name +
        ", alarm_type = " + alarm_type +
        ", alarm_level = " + alarm_level +
        ", status = " + status +
        ", trigger_value = " + trigger_value +
        ", threshold = " + threshold +
        ", description = " + description +
        ", triggered_time = " + triggered_time +
        ", acknowledged_time = " + acknowledged_time +
        ", resolved_time = " + resolved_time +
        ", acknowledged_by = " + acknowledged_by +
        ", created_time = " + create_time +
        ", updated_time = " + update_time +
        ", device_uuid = " + device_uuid +
                ", user_id = " + user_id +
        "}";
    }
}
