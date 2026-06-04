package com.neon.mechat.common;

import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{

    /**
     * 处理业务层主动抛出的异常，只返回 code 和 message，避免 RuntimeException 内部字段暴露给前端。
     *
     * @param exception 业务异常
     * @return 标准失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse <Void> handleBusinessException(BusinessException exception)
    {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理请求参数校验失败，统一返回参数错误而不是暴露框架异常结构。
     *
     * @param exception Spring 参数绑定或校验异常
     * @return 标准失败响应
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse <Void> handleValidationException(Exception exception)
    {
        return ApiResponse.fail(400, "请求参数不合法");
    }

    /**
     * 兜底处理未预期异常，避免默认错误响应泄露堆栈、类名等服务端细节。
     *
     * @param exception 未捕获异常
     * @return 标准失败响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse <Void> handleException(Exception exception)
    {
        return ApiResponse.fail(500, "服务器内部错误");
    }
}
