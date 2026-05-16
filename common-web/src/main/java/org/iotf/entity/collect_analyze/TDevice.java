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
 * 
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
@TableName("t_device")
public class TDevice implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long user_id;

    @TableId(value = "device_id", type = IdType.ASSIGN_ID)
    private Long device_id;

    private String device_uuid;

    private String device_type;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    private String device_name;

    /**
     * 1删除0存在
     */
    private Integer deleted;

    /**
     * 前上报频率 默认60秒 最小10 秒
     */
    private Integer frequency;

    private Boolean report_status;

//    private String alarm_status;

    private Float threshold;

    public static final String USER_ID = "user_id";

    public static final String DEVICE_ID = "device_id";

    public static final String DEVICE_UUID = "device_uuid";

    public static final String DEVICE_TYPE = "device_type";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String DEVICE_NAME = "device_name";

    public static final String DELETED = "deleted";

    public static final String FREQUENCY = "frequency";

    public static final String REPORT_STATUS = "report_status";

//    public static final String ALARM_STATUS = "alarm_status";

    public static final String THRESHOLD = "threshold";

    @Override
    public String toString() {
        return "TDevice{" +
        "user_id = " + user_id +
        ", device_id = " + device_id +
        ", device_uuid = " + device_uuid +
        ", device_type = " + device_type +
        ", create_time = " + create_time +
        ", update_time = " + update_time +
        ", device_name = " + device_name +
                ", deleted = " + deleted +
                ", frequency = " + frequency +
                ", report_status = " + report_status +
//                ", alarm_status = " + alarm_status +
                ", threshold = " + threshold +
        "}";
    }
}
