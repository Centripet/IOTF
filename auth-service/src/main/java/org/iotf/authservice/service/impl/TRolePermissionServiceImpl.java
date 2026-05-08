package org.iotf.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.iotf.authservice.mapper.TRolePermissionMapper;
import org.iotf.authservice.service.ITRolePermissionService;
import org.iotf.entity.auth.dao.TRolePermission;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色权限表 服务实现类
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Service
@RequiredArgsConstructor
public class TRolePermissionServiceImpl extends ServiceImpl<TRolePermissionMapper, TRolePermission> implements ITRolePermissionService {

}
