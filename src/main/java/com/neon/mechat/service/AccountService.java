package com.neon.mechat.service;

import com.neon.mechat.common.BusinessException;
import com.neon.mechat.config.properties.AccountProperties;
import com.neon.mechat.dto.AccountLoginDTO;
import com.neon.mechat.dto.AccountRegisterDTO;
import com.neon.mechat.entity.Account;
import com.neon.mechat.mapper.AccountMapper;
import com.neon.mechat.repository.AccountRepository;
import com.neon.mechat.repository.AvatarFileRepository;
import com.neon.mechat.repository.AvatarFileRepository.AvatarFilePlan;
import com.neon.mechat.support.SnowflakeIdGenerator;
import com.neon.mechat.vo.AccountLoginVO;
import com.neon.mechat.vo.AccountUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService
{
    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final AccountProperties accountProperties;
    private final AvatarFileRepository avatarFileRepository;

    /**
     * 注册账号。
     *
     * @param accountRegisterDTO 注册参数
     * @return 注册后的用户信息，包含服务端生成的 userId
     */
    @Transactional
    public AccountUserVO register(AccountRegisterDTO accountRegisterDTO)
    {
        if (accountMapper.existsByNickname(accountRegisterDTO.getNickname()))
        {
            throw new BusinessException(1004, "昵称已存在");
        }

        // userId 由服务端生成，避免客户端手动指定账号 ID。
        Long userId = snowflakeIdGenerator.nextId();

        Account account = new Account()
                .setUserId(userId)
                .setNickname(accountRegisterDTO.getNickname())
                // 密码只保存 BCrypt 哈希值，不保存明文。
                .setPassword(passwordEncoder.encode(accountRegisterDTO.getPassword()));

        try
        {
            accountMapper.insert(account);
        }
        catch (DuplicateKeyException exception)
        {
            throw new BusinessException(1004, "昵称已存在");
        }
        return toUserVO(account);
    }

    /**
     * 登录账号。
     *
     * @param accountLoginDTO 登录参数
     * @return token 和用户信息
     */
    public AccountLoginVO login(AccountLoginDTO accountLoginDTO)
    {
        Account account = accountMapper.selectByNickname(accountLoginDTO.getNickname());
        if (account == null || !passwordEncoder.matches(accountLoginDTO.getPassword(), account.getPassword()))
        {
            throw new BusinessException(1002, "昵称或密码错误");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        accountRepository.saveToken(token, account.getUserId(), accountProperties.getLoginTokenTtl());
        return new AccountLoginVO(token, toUserVO(account));
    }

    /**
     * 自动登录。
     *
     * @param token 客户端保存的 token
     * @return token 和当前用户信息
     */
    public AccountLoginVO autoLogin(String token)
    {
        if (!StringUtils.hasText(token))
        {
            throw new BusinessException(401, "未登录");
        }

        Long userId = accountRepository.findUserIdByToken(token);
        if (userId == null)
        {
            throw new BusinessException(401, "登录已失效");
        }

        Account account = accountMapper.selectByUserId(userId);
        if (account == null)
        {
            throw new BusinessException(401, "登录已失效");
        }

        if (accountRepository.getTokenTtl(token).compareTo(accountProperties.getLoginTokenRenewThreshold()) <= 0)
        {
            // token 临近过期时续期，减少用户频繁重新登录。
            accountRepository.refreshToken(token, accountProperties.getLoginTokenTtl());
        }

        return new AccountLoginVO(token, toUserVO(account));
    }

    /**
     * 获取当前登录用户信息。
     *
     * @param token 登录 token
     * @return 当前用户信息
     */
    public AccountUserVO getCurrentUser(String token)
    {
        Long userId = authenticate(token);
        Account account = accountMapper.selectByUserId(userId);
        if (account == null)
        {
            throw new BusinessException(401, "登录已失效");
        }
        return toUserVO(account);
    }

    /**
     * 上传当前用户头像。
     *
     * @param token 登录 token
     * @param avatarFile 头像文件
     */
    @Transactional
    public void uploadAvatar(String token, MultipartFile avatarFile)
    {
        Long userId = authenticate(token);
        Account account = accountMapper.selectByUserId(userId);
        if (account == null)
        {
            throw new BusinessException(401, "登录已失效");
        }

        AvatarFilePlan avatarFilePlan = avatarFileRepository.saveToTmp(userId, avatarFile);
        accountMapper.updateAvatar(userId, avatarFilePlan.getFileName());
        registerAvatarFileTransactionSynchronization(avatarFilePlan, account.getAvatar());
    }

    /**
     * 下载头像文件。
     *
     * @param userId 用户 ID
     * @return 头像文件资源
     */
    public Resource downloadAvatar(Long userId)
    {
        Account account = getAccountForAvatar(userId);
        return avatarFileRepository.loadImg(account.getAvatar());
    }

    /**
     * 获取头像文件的 Content-Type。
     *
     * @param userId 用户 ID
     * @return Content-Type
     */
    public String getAvatarContentType(Long userId)
    {
        Account account = getAccountForAvatar(userId);
        return avatarFileRepository.getContentType(account.getAvatar());
    }

    /**
     * 根据 token 校验登录态并返回当前用户 ID。
     *
     * @param token 登录 token
     * @return 当前登录用户 ID
     */
    private Long authenticate(String token)
    {
        if (!StringUtils.hasText(token))
        {
            throw new BusinessException(401, "未登录");
        }

        Long userId = accountRepository.findUserIdByToken(token);
        if (userId == null)
        {
            throw new BusinessException(401, "登录已失效");
        }
        return userId;
    }

    /**
     * 查询拥有头像的账号。
     *
     * @param userId 用户 ID
     * @return 账号实体
     */
    private Account getAccountForAvatar(Long userId)
    {
        Account account = accountMapper.selectByUserId(userId);
        if (account == null)
        {
            throw new BusinessException(1003, "用户不存在");
        }
        if (!StringUtils.hasText(account.getAvatar()))
        {
            throw new BusinessException(3003, "头像不存在");
        }
        return account;
    }

    /**
     * 注册头像文件事务同步动作。
     *
     * @param avatarFilePlan 新头像文件保存计划
     * @param oldAvatar 旧头像文件名
     */
    private void registerAvatarFileTransactionSynchronization(AvatarFilePlan avatarFilePlan, String oldAvatar)
    {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void beforeCommit(boolean readOnly)
            {
                // 数据库更新即将提交时才把头像从 tmp 移动到 img。
                avatarFileRepository.moveToImg(avatarFilePlan);
            }

            @Override
            public void afterCompletion(int status)
            {
                // 业务失败时清理 tmp；提交失败时也清理已经移动到 img 的文件。
                avatarFileRepository.deleteTmpQuietly(avatarFilePlan);
                if (status != STATUS_COMMITTED)
                {
                    avatarFileRepository.deleteImgQuietly(avatarFilePlan);
                    return;
                }
                // 新头像完全生效后再删除旧头像，避免失败时用户头像丢失。
                avatarFileRepository.deleteImgQuietly(oldAvatar);
            }
        });
    }

    /**
     * 将账号实体转换成用户信息 VO。
     *
     * @param account 账号实体
     * @return 用户信息 VO
     */
    private AccountUserVO toUserVO(Account account)
    {
        // VO 不包含密码等敏感字段。
        return new AccountUserVO(account.getUserId(), account.getNickname(), account.getAvatar());
    }
}
