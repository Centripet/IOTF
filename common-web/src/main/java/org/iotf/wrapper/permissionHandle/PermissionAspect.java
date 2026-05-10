package org.iotf.wrapper.permissionHandle;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.wrapper.responseHandle.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  权限操作 切面
 * </p>
 *
 * @author Centripet
 * @since 2025-11-10
 */
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        JwtPayload payload = (JwtPayload) auth.getPrincipal();

        String requiredPermission = requirePermission.permission().code();

        if (!payload.getPermission().contains(requiredPermission)) {
            return ApiResponse.fail(403, "您没有此操作的权限");
        }

        return joinPoint.proceed();
    }
}