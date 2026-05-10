package org.iotf.wrapper.exceptionHandle;

import org.iotf.wrapper.responseHandle.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理请求体缺失或格式错误
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析异常: {}", e.getMessage());

        String message = "请求参数错误";

        if (e.getMessage() != null) {
            if (e.getMessage().contains("Required request body is missing")) {
                message = "请求体不能为空";
            } else if (e.getMessage().contains("JSON parse error")) {
                if (e.getMessage().contains("Cannot deserialize")) {
                    message = "参数类型不匹配";
                } else if (e.getMessage().contains("Unexpected character")) {
                    message = "JSON格式错误";
                } else {
                    message = "JSON解析失败";
                }
            }
        }

        return ApiResponse.fail(400, message);
    }

    /**
     * 处理 @Valid 参数校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleValidationException(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> String.format("字段[%s] %s", fe.getField(), fe.getDefaultMessage()))
                .toList();

        log.warn("参数校验失败: {}", errors);
        return ApiResponse.fail(400, "参数校验失败: " + String.join(", ", errors));
    }

    /**
     * 处理参数类型不匹配（如：String 转 Integer 失败）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {}", e.getMessage());
        String message = String.format("参数 '%s' 类型错误，期望类型: %s",
                e.getName(), e.getRequiredType().getSimpleName());
        return ApiResponse.fail(400, message);
    }

    /**
     * 处理缺少请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getMessage());
        return ApiResponse.fail(400, "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 处理 @RequestParam 绑定失败
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleBindException(BindException e) {
        var errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> String.format("参数[%s] %s", fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("参数绑定失败: {}", errors);
        return ApiResponse.fail(400, "参数绑定失败: " + String.join(", ", errors));
    }

    /**
     * 处理自定义业务异常
     */
//    @ExceptionHandler(BusinessException.class)
//    public ApiResponse<?> handleBusinessException(BusinessException e) {
//        log.warn("业务异常: {}", e.getMessage());
//        return ApiResponse.fail(e.getCode(), e.getMessage());
//    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleException(Exception e) {
        e.printStackTrace();
        log.error("未处理异常", e);
        return ApiResponse.fail(500, "服务器内部异常，请联系管理员");
    }
}