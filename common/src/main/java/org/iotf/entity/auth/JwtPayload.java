package org.iotf.entity.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtPayload {
    private Long user_id;
//    private String department;
    private String role;
    private List<String> permission;

    private Long jti;
    private String clientType;
    private String deviceId;
}