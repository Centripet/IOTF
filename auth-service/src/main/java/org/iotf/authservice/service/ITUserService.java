package org.iotf.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import org.iotf.entity.auth.dao.SUser;
import org.iotf.entity.auth.dao.TUser;
import com.baomidou.mybatisplus.extension.service.IService;
import org.iotf.requestFormation.auth.forgetPasswordRequest;
import org.iotf.requestFormation.auth.loginRequest;
import org.iotf.requestFormation.auth.registerRequest;
import org.iotf.requestFormation.auth.userSelfModifyRequest;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
public interface ITUserService extends IService<TUser> {
    //AUTH
    TUser loginVerification(loginRequest request);

    List<TUser> findUsersByUserPhone(String phone);
    List<TUser> findUsersByUserName(String account);
    List<TUser> findUsersByUserId(String userId);

    Boolean userExists(registerRequest request);
    Boolean registerService(registerRequest request);
    Boolean forgetAndResetPassword(forgetPasswordRequest request, TUser user);

    String resolveClientType(HttpServletRequest httpRequest);

    String resolveDeviceId(HttpServletRequest httpRequest);

    SUser userSelfDetail(Long userId);

    Boolean verifyPassword(Long userId, String passwordHashOld);

    Boolean resetPassword(Long userId, String passwordHash);

    Boolean userSelfModify(Long userId, userSelfModifyRequest request);

}
