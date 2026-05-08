package org.iotf.authservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.iotf.authservice.mapper.SUserMapper;
import org.iotf.authservice.mapper.TFileMapper;
import org.iotf.authservice.service.AliOssService;
import org.iotf.authservice.service.ITFileService;
import org.iotf.entity.auth.dao.TUser;
import org.iotf.authservice.mapper.TUserMapper;
import org.iotf.authservice.service.ITUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.iotf.requestFormation.auth.forgetPasswordRequest;
import org.iotf.requestFormation.auth.loginRequest;
import org.iotf.requestFormation.auth.registerRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.iotf.util.CommonGenerator.generateHexSalt;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Service
@RequiredArgsConstructor
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser> implements ITUserService {

    private final TUserMapper tUserMapper;
    private final SUserMapper sUserMapper;
    private final ITFileService fileService;
    private final TFileMapper fileMapper;
    private final AliOssService ossService;
    private final List<String> clientTypeList = List.of("pc", "mobile");

    @Override
    public TUser loginVerification(loginRequest request) {
        Optional<TUser> OVUser = Optional.ofNullable(tUserMapper.selectOne(
                new QueryWrapper<TUser>().eq("user_name", request.user_name())
        ));

        if (OVUser.isPresent()) {
            TUser user = OVUser.get();
            if (
                    DigestUtils.sha256Hex(user.getSalt() + request.passwordHash())
                            .equals(user.getPassword_hash())
            ) {
                return user;
            } else
                return null;
        }

        return null;
    }

    @Override
    public List<TUser> findUsersByUserPhone(String phone) {
        return this.lambdaQuery()
                .eq(TUser::getPhone, phone)
                .list();
    }

    @Override
    public List<TUser> findUsersByUserName(String user_name) {
        return this.lambdaQuery()
                .eq(TUser::getUser_name, user_name)
                .list();
    }

    @Override
    public List<TUser> findUsersByUserId(String userId) {
        return this.lambdaQuery()
                .eq(TUser::getUser_id, userId)
                .list();
    }

    @Override
    public boolean userExists(registerRequest request) {
        String userName = request.user_name();
        String phone = request.phone();
        // 构建查询条件
        LambdaQueryWrapper<TUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userName != null, TUser::getUser_name, userName)
                .or()
                .eq(phone != null, TUser::getPhone, phone);
        // 查询是否存在记录
        long count = tUserMapper.selectCount(queryWrapper);
        return count > 0;
    }

    @Override
    public boolean registerService(registerRequest request) {
        String salt = generateHexSalt(16);
        String passwordHash = DigestUtils.sha256Hex(salt + request.passwordHash());

        TUser user = TUser.builder()
                .user_name(request.user_name())
                .phone(request.phone())
                .user_status(1)
                .salt(salt)
                .password_hash(passwordHash)
                .role_id(1L)
                .create_time(LocalDateTime.now())
                .build();

        return tUserMapper.insert(user) >= 1;
    }

    @Override
    public boolean resetPassword(forgetPasswordRequest request, TUser user) {
        return tUserMapper.update(
                new UpdateWrapper<TUser>()
                        .eq("user_id", user.getUser_id())
                        .set("passwordHash", DigestUtils.sha256Hex(user.getSalt() + request.passwordHash()))
                        .set("update_time", LocalDateTime.now())
        )>= 1;
    }

    @Override
    public String resolveClientType(HttpServletRequest httpRequest) {
        return "unknown";
    }

    @Override
    public String resolveDeviceId(HttpServletRequest httpRequest) {
        return "unknown";
    }

}
