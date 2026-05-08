package org.iotf.authservice.service;

import jakarta.servlet.http.HttpServletRequest;
import org.iotf.entity.auth.dao.TUser;
import com.baomidou.mybatisplus.extension.service.IService;
import org.iotf.requestFormation.auth.forgetPasswordRequest;
import org.iotf.requestFormation.auth.loginRequest;
import org.iotf.requestFormation.auth.registerRequest;

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

    boolean userExists(registerRequest request);
    boolean registerService(registerRequest request);
    boolean resetPassword(forgetPasswordRequest request, TUser user);

    String resolveClientType(HttpServletRequest httpRequest);

    String resolveDeviceId(HttpServletRequest httpRequest);
}
