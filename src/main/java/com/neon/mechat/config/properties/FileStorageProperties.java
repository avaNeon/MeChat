package com.neon.mechat.config.properties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mechat.file")
@Schema(description = "文件存储配置")
public class FileStorageProperties
{
    @Schema(description = "文件存储根路径，Windows 下建议使用绝对路径", example = "E:/MeChatStorage")
    private String rootPath;
}
