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
public class AvatarFileRepository
{
    private static final String FILE_DIR = "file";
    private static final String IMG_DIR = "img";
    private static final String TMP_DIR = "tmp";
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final FileStorageProperties fileStorageProperties;
    private Path imgPath;
    private Path tmpPath;

    /**
     * 初始化头像文件目录。
     */
    @PostConstruct
    public void init()
    {
        if (!StringUtils.hasText(fileStorageProperties.getRootPath()))
        {
            throw new BusinessException(500, "文件存储根路径未配置");
        }

        Path rootPath = Path.of(fileStorageProperties.getRootPath()).toAbsolutePath().normalize();
        imgPath = rootPath.resolve(FILE_DIR).resolve(IMG_DIR).normalize();
        tmpPath = rootPath.resolve(FILE_DIR).resolve(TMP_DIR).normalize();

        try
        {
            Files.createDirectories(imgPath);
            Files.createDirectories(tmpPath);
        }
        catch (IOException exception)
        {
            throw new BusinessException(500, "头像目录初始化失败");
        }
    }

    /**
     * 将头像文件保存到 tmp 目录。
     *
     * @param userId 用户 ID
     * @param avatarFile 头像文件
     * @return 头像文件保存计划
     */
    public AvatarFilePlan saveToTmp(Long userId, MultipartFile avatarFile)
    {
        validateAvatarFile(avatarFile);
        String extension = getExtension(avatarFile.getOriginalFilename());
        String fileName = userId + "_" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path tmpFilePath = tmpPath.resolve(fileName).normalize();
        assertPathInDirectory(tmpFilePath, tmpPath);

        try
        {
            Files.copy(avatarFile.getInputStream(), tmpFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException exception)
        {
            throw new BusinessException(3001, "头像上传失败");
        }

        return new AvatarFilePlan(fileName, tmpFilePath, imgPath.resolve(fileName).normalize());
    }

    /**
     * 将头像文件从 tmp 目录移动到 img 目录。
     *
     * @param avatarFilePlan 头像文件保存计划
     */
    public void moveToImg(AvatarFilePlan avatarFilePlan)
    {
        assertPathInDirectory(avatarFilePlan.getImgFilePath(), imgPath);
        try
        {
            Files.move(avatarFilePlan.getTmpFilePath(), avatarFilePlan.getImgFilePath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException exception)
        {
            throw new BusinessException(3002, "头像保存失败");
        }
    }

    /**
     * 从 img 目录读取头像文件。
     *
     * @param fileName 头像文件名
     * @return 头像文件资源
     */
    public Resource loadImg(String fileName)
    {
        String safeFileName = Path.of(fileName).getFileName().toString();
        Path filePath = imgPath.resolve(safeFileName).normalize();
        assertPathInDirectory(filePath, imgPath);
        if (!Files.isRegularFile(filePath))
        {
            throw new BusinessException(3003, "头像不存在");
        }

        try
        {
            return new UrlResource(filePath.toUri());
        }
        catch (MalformedURLException exception)
        {
            throw new BusinessException(3003, "头像不存在");
        }
    }

    /**
     * 删除 tmp 目录中的临时头像文件。
     *
     * @param avatarFilePlan 头像文件保存计划
     */
    public void deleteTmpQuietly(AvatarFilePlan avatarFilePlan)
    {
        deleteQuietly(avatarFilePlan.getTmpFilePath());
    }

    /**
     * 删除 img 目录中的头像文件。
     *
     * @param avatarFilePlan 头像文件保存计划
     */
    public void deleteImgQuietly(AvatarFilePlan avatarFilePlan)
    {
        deleteQuietly(avatarFilePlan.getImgFilePath());
    }

    /**
     * 删除 img 目录中的指定头像文件。
     *
     * @param fileName 头像文件名
     */
    public void deleteImgQuietly(String fileName)
    {
        if (!StringUtils.hasText(fileName))
        {
            return;
        }

        Path filePath = imgPath.resolve(Path.of(fileName).getFileName().toString()).normalize();
        assertPathInDirectory(filePath, imgPath);
        deleteQuietly(filePath);
    }

    /**
     * 根据文件扩展名判断下载时的 Content-Type。
     *
     * @param fileName 头像文件名
     * @return Content-Type
     */
    public String getContentType(String fileName)
    {
        return switch (getExtension(fileName))
        {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    /**
     * 校验上传的头像文件。
     *
     * @param avatarFile 头像文件
     */
    private void validateAvatarFile(MultipartFile avatarFile)
    {
        if (avatarFile == null || avatarFile.isEmpty())
        {
            throw new BusinessException(3004, "头像文件不能为空");
        }

        String extension = getExtension(avatarFile.getOriginalFilename());
        if (!IMAGE_EXTENSIONS.contains(extension))
        {
            throw new BusinessException(3005, "头像格式不支持");
        }
    }

    /**
     * 获取文件扩展名。
     *
     * @param fileName 文件名
     * @return 小写扩展名
     */
    private String getExtension(String fileName)
    {
        if (!StringUtils.hasText(fileName) || !fileName.contains("."))
        {
            throw new BusinessException(3005, "头像格式不支持");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 校验目标路径必须位于指定目录内。
     *
     * @param path 目标路径
     * @param directory 允许访问的目录
     */
    private void assertPathInDirectory(Path path, Path directory)
    {
        if (!path.normalize().startsWith(directory.normalize()))
        {
            throw new BusinessException(400, "文件路径不合法");
        }
    }

    /**
     * 静默删除文件。
     *
     * @param path 文件路径
     */
    private void deleteQuietly(Path path)
    {
        try
        {
            Files.deleteIfExists(path);
        }
        catch (IOException ignored)
        {
            // 清理失败不影响主业务流程。
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class AvatarFilePlan
    {
        private final String fileName;
        private final Path tmpFilePath;
        private final Path imgFilePath;
    }
}
