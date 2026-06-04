package com.neon.mechat.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException
{
    private final int code;

    /**
     * 创建业务异常，配合全局异常处理器向前端返回明确的业务错误码和错误信息。
     *
     * @param code    业务错误码
     * @param message 业务错误信息
     */
    public BusinessException(int code, String message)
    {
        super(message);
        this.code = code;
    }
}
