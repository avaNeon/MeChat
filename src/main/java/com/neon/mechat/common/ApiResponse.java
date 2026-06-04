package com.neon.mechat.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T>
{
    private int code;
    private String message;
    private T data;

    /**
     * 构造统一成功响应，避免 Controller 直接暴露业务对象的包装细节。
     *
     * @param data 返回给客户端的业务数据
     * @param <T>  业务数据类型
     * @return 统一成功响应
     */
    public static <T> ApiResponse<T> success(T data)
    {
        return new ApiResponse<>(0, "success", data);
    }

    /**
     * 构造统一失败响应，用于业务异常、参数异常和兜底异常的标准输出。
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     业务数据类型
     * @return 统一失败响应
     */
    public static <T> ApiResponse<T> fail(int code, String message)
    {
        return new ApiResponse<>(code, message, null);
    }
}
