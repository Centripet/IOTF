package org.iotf.entity.auth.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.iotf.util.JsonTypeHandler;
import org.postgresql.util.PGobject;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 角色权限表
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
@TableName("t_role_permission")
public class TRolePermission implements Serializable {

    @TableField(exist = false)
    ObjectMapper objectMapper = new ObjectMapper();

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "role_id", type = IdType.ASSIGN_ID)
    private Long role_id;

    private String role;

    @TableField(value = "permission",typeHandler = JsonTypeHandler.class)
    private Object permission;

    @TableField(exist = false)
    private List<String> permissionList;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    public static final String ROLE_ID = "role_id";

    public static final String ROLE = "role";

    public static final String PERMISSION = "permission";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    @Override
    public String toString() {
        return "TRolePermission{" +
        "role_id = " + role_id +
        ", role = " + role +
        ", permission = " + permission +
        ", create_time = " + create_time +
        ", update_time = " + update_time +
        "}";
    }

    public List<String> jsonPermissionList() throws JsonProcessingException {

        String json = ((PGobject)permission).getValue();
        permissionList = objectMapper.readValue(json, new TypeReference<List<String>>() {});

        return permissionList;
    }

}
