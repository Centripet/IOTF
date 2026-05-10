package org.iotf.wrapper.operationLogHandle;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.iotf.entity.auth.JwtPayload;
import org.iotf.entity.common.TOperationLog;
import org.iotf.service.common.ITOperationLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 *  日志记录 切面
 * </p>
 *
 * @author Centripet
 * @since 2025-11-10
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ITOperationLogService operationLogService;

    @Around("@annotation(operationLog)")
    public Object logOperation(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        Object result;
        Throwable throwable = null;

        long start = System.currentTimeMillis();

        try {
            result = joinPoint.proceed();  // 执行目标方法
            return result;
        } catch (Throwable t) {
            throwable = t;
            throw t;
        } finally {
            long cost = System.currentTimeMillis() - start;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            JwtPayload payload = (JwtPayload) auth.getPrincipal();

            // 构造日志对象
            TOperationLog log = TOperationLog.builder()
                    .operator(payload.getUser_id())
                    .operation(operationLog.operation())
                    .target_type(operationLog.targetType())
                    .create_time(LocalDateTime.now())
                    .build();

            Object requestArg = null;
            Long targetId = null;
            String detail = null;

            // 找到第一个 request 参数对象
            for (Object arg : joinPoint.getArgs()) {
                if (arg == null) continue;
                if (arg.getClass().getSimpleName().endsWith("Request")) {
                    requestArg = arg;
                    break;
                }
            }

            if (requestArg != null && !operationLog.targetIdField().isEmpty()) {
                targetId = Long.valueOf(Objects.requireNonNull(getNestedFieldValue(requestArg, operationLog.targetIdField())));
            }

            // 将请求对象序列化为 detail
            if (requestArg != null) {
                try {
                    detail = new ObjectMapper().writeValueAsString(requestArg);
                } catch (Exception ignored) {}
            }

            log.setTarget_id(targetId);
            log.setDetail(detail);

            if (throwable != null) {
                log.setDetail("操作失败：" + throwable.getMessage());
            }

            operationLogService.save(log);
            System.out.println("[AOP日志] 操作: " + log.getOperation() + " 用时: " + cost + "ms");
        }
    }

    // 递归获取嵌套字段值，支持 user.userId 这种路径
    private String getNestedFieldValue(Object obj, String fieldPath) {
        try {
            String[] parts = fieldPath.split("\\.");
            Object current = obj;
            for (String part : parts) {
                if (current == null) return null;
                Field field = current.getClass().getDeclaredField(part);
                field.setAccessible(true);
                current = field.get(current);
            }
            return (String) current;
        } catch (Exception e) {
            return null;
        }
    }

}
