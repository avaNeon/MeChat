package com.neon.mechat.repository;

import com.neon.mechat.common.BusinessException;
import com.neon.mechat.config.properties.FileStorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MessageFileRepository
{
    private static final String FILE_DIR = "file";
    private static final String MSG_DIR = "msg";
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final FileStorageProperties fileStorageProperties;
    private Path msgPath;

    @PostConstruct
    public void init()
    {
        if (!StringUtils.hasText(fileStorageProperties.getRootPath()))
        {
            throw new BusinessException(500, "文件存储根路径未配置");
        }

        Path rootPath = Path.of(fileStorageProperties.getRootPath()).toAbsolutePath().normalize();
        msgPath = rootPath.resolve(FILE_DIR).resolve(MSG_DIR).normalize();

        try
        {
            Files.createDirectories(msgPath);
        }
        catch (IOException exception)
        {
            throw new BusinessException(500, "消息图片目录初始化失败");
        }
    }

    public String save(Long userId, MultipartFile imageFile)
    {
        validateImageFile(imageFile);
        String extension = getExtension(imageFile.getOriginalFilename());
        String fileName = userId + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path filePath = msgPath.resolve(fileName).normalize();
        assertPathInDirectory(filePath, msgPath);

        try
        {
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException exception)
        {
            throw new BusinessException(3101, "图片上传失败");
        }

        return FILE_DIR + "/" + MSG_DIR + "/" + fileName;
    }

    public Resource load(String relativePath)
    {
        String fileName = Path.of(relativePath).getFileName().toString();
        Path filePath = msgPath.resolve(fileName).normalize();
        assertPathInDirectory(filePath, msgPath);
        if (!Files.isRegularFile(filePath))
        {
            throw new BusinessException(3102, "图片不存在");
        }

        try
        {
            return new UrlResource(filePath.toUri());
        }
        catch (MalformedURLException exception)
        {
            throw new BusinessException(3102, "图片不存在");
        }
    }

    public String getContentType(String relativePath)
    {
        String fileName = Path.of(relativePath).getFileName().toString();
        return switch (getExtension(fileName))
        {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    private void validateImageFile(MultipartFile imageFile)
    {
        if (imageFile == null || imageFile.isEmpty())
        {
            throw new BusinessException(3103, "图片文件不能为空");
        }

        String extension = getExtension(imageFile.getOriginalFilename());
        if (!IMAGE_EXTENSIONS.contains(extension))
        {
            throw new BusinessException(3104, "图片格式不支持");
        }
    }

    private String getExtension(String fileName)
    {
        if (!StringUtils.hasText(fileName) || !fileName.contains("."))
        {
            throw new BusinessException(3104, "图片格式不支持");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private void assertPathInDirectory(Path path, Path directory)
    {
        if (!path.normalize().startsWith(directory.normalize()))
        {
            throw new BusinessException(400, "文件路径不合法");
        }
    }
}
