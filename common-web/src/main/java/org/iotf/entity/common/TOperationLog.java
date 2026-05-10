package org.iotf.entity.common;

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
 * @since 2026-05-08
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_operation_log")
public class TOperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "operate_id", type = IdType.ASSIGN_ID)
    private Long operate_id;

    private Long operator;

    private String operation;

    private Long target_id;

    private String target_type;

    private String detail;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    public static final String OPERATE_ID = "operate_id";

    public static final String OPERATOR = "operator";

    public static final String OPERATION = "operation";

    public static final String TARGET_ID = "target_id";

    public static final String TARGET_TYPE = "target_type";

    public static final String DETAIL = "detail";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    @Override
    public String toString() {
        return "TOperationLog{" +
        "operate_id = " + operate_id +
        ", operator = " + operator +
        ", operation = " + operation +
        ", target_id = " + target_id +
        ", target_type = " + target_type +
        ", detail = " + detail +
        ", create_time = " + create_time +
        ", update_time = " + update_time +
        "}";
    }
}
