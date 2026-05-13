package org.iotf.entity.collect_analyze;

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

    @TableId("device_id")
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

    public static final String USER_ID = "user_id";

    public static final String DEVICE_ID = "device_id";

    public static final String DEVICE_UUID = "device_uuid";

    public static final String DEVICE_TYPE = "device_type";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String DEVICE_NAME = "device_name";

    public static final String DELETED = "deleted";

    public static final String FREQUENCY = "frequency";

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
        "}";
    }
}
