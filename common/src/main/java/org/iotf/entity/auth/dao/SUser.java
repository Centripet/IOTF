package org.iotf.entity.auth.dao;

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
@TableName("t_user")
public class SUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long user_id;

    private String user_name;

    private String phone;

    private String email;

//    private String salt;

//    private String password_hash;

    private Long role_id;

    private Integer user_status;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    private String nick_name;

    private Short sex;

    private String icon;

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String PHONE = "phone";

    public static final String EMAIL = "email";

//    public static final String SALT = "salt";

//    public static final String PASSWORD_HASH = "password_hash";

    public static final String ROLE_ID = "role_id";

    public static final String USER_STATUS = "user_status";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String NICK_NAME = "nick_name";

    public static final String SEX = "sex";

    public static final String ICON = "icon";

    @Override
    public String toString() {
        return "TUser{" +
        "user_id = " + user_id +
        ", user_name = " + user_name +
        ", phone = " + phone +
        ", email = " + email +
//        ", salt = " + salt +
//        ", password_hash = " + password_hash +
        ", role_id = " + role_id +
        ", user_status = " + user_status +
        ", create_time = " + create_time +
        ", update_time = " + update_time +
        ", nick_name = " + nick_name +
        ", sex = " + sex +
        ", icon = " + icon +
        "}";
    }
}
