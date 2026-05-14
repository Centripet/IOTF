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
 * 告警状态变更日志表
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
@TableName("t_alarm_log")
public class TAlarmLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "alarm_log_id", type = IdType.ASSIGN_ID)
    private Long alarm_log_id;

    /**
     * 关联告警ID
     */
    private Long alarm_id;

    /**
     * 变更前状态
     */
    private String from_status;

    /**
     * 变更后状态
     */
    private String to_status;

    /**
     * 变更原因
     */
    private String change_reason;

    /**
     * 变更时间
     */
    private LocalDateTime changed_time;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    public static final String ALARM_LOG_ID = "alarm_log_id";

    public static final String ALARM_ID = "alarm_id";

    public static final String FROM_STATUS = "from_status";

    public static final String TO_STATUS = "to_status";

    public static final String CHANGE_REASON = "change_reason";

    public static final String CHANGED_TIME = "changed_time";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    @Override
    public String toString() {
        return "TAlarmLog{" +
        "alarm_log_id = " + alarm_log_id +
        ", alarm_id = " + alarm_id +
        ", from_status = " + from_status +
        ", to_status = " + to_status +
        ", change_reason = " + change_reason +
        ", changed_time = " + changed_time +
        ", create_time = " + create_time +
        ", update_time = " + update_time +
        "}";
    }
}
