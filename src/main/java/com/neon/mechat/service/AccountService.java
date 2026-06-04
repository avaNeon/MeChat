package com.neon.mechat.service;

import com.neon.mechat.common.BusinessException;
import com.neon.mechat.config.properties.AccountProperties;
import com.neon.mechat.dto.AccountLoginDTO;
import com.neon.mechat.dto.AccountRegisterDTO;
import com.neon.mechat.entity.Account;
import com.neon.mechat.mapper.AccountMapper;
import com.neon.mechat.repository.AccountRepository;
import com.neon.mechat.support.SnowflakeIdGenerator;
import com.neon.mechat.vo.AccountLoginVO;
import com.neon.mechat.vo.AccountUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    /**
     * 注册账号。
     *
     * @param accountRegisterDTO 注册参数
     * @return 注册后的用户信息，包含服务端生成的 userId
     */
    @Transactional
    public AccountUserVO register(AccountRegisterDTO accountRegisterDTO)
    {
        // userId 由服务端生成，避免客户端手动指定账号 ID。
        Long userId = snowflakeIdGenerator.nextId();

        Account account = new Account()
                .setUserId(userId)
                .setNickname(accountRegisterDTO.getNickname())
                // 密码只保存 BCrypt 哈希值，不保存明文。
                .setPassword(passwordEncoder.encode(accountRegisterDTO.getPassword()))
                .setAvatar(accountRegisterDTO.getAvatar());

        accountMapper.insert(account);
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
        Account account = accountMapper.selectByUserId(accountLoginDTO.getUserId());
        if (account == null || !passwordEncoder.matches(accountLoginDTO.getPassword(), account.getPassword()))
        {
            throw new BusinessException(1002, "用户ID或密码错误");
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
