package com.neon.mechat.service;

import com.neon.mechat.common.BusinessException;
import com.neon.mechat.dto.AddFriendDTO;
import com.neon.mechat.entity.Account;
import com.neon.mechat.entity.FriendRequest;
import com.neon.mechat.entity.FriendRelation;
import com.neon.mechat.mapper.AccountMapper;
import com.neon.mechat.mapper.FriendRequestMapper;
import com.neon.mechat.mapper.FriendRelationMapper;
import com.neon.mechat.repository.AccountRepository;
import com.neon.mechat.support.SnowflakeIdGenerator;
import com.neon.mechat.vo.AccountUserVO;
import com.neon.mechat.vo.FriendRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendRelationService
{
    private static final int REQUEST_STATUS_PENDING = 0;
    private static final int REQUEST_STATUS_ACCEPTED = 1;
    private static final int REQUEST_STATUS_REJECTED = 2;

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final FriendRelationMapper friendRelationMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 发起好友申请。真正的好友关系只在对方同意后创建。
     *
     * @param token        登录 token
     * @param addFriendDTO 添加好友参数
     * @return 好友申请信息
     */
    @Transactional
    public FriendRequestVO requestFriend(String token, AddFriendDTO addFriendDTO)
    {
        Long currentUserId = authenticate(token);
        Account requester = getCurrentAccount(currentUserId);
        Account addressee = findFriendAccount(addFriendDTO);
        if (currentUserId.equals(addressee.getUserId()))
        {
            throw new BusinessException(4001, "不能添加自己为好友");
        }

        Long userLowId = Math.min(currentUserId, addressee.getUserId());
        Long userHighId = Math.max(currentUserId, addressee.getUserId());
        if (friendRelationMapper.selectByUsers(userLowId, userHighId) != null)
        {
            throw new BusinessException(4003, "已经是好友");
        }
        FriendRequest existedRequest = friendRequestMapper.selectByUsers(userLowId, userHighId);
        if (existedRequest != null
                && Integer.valueOf(REQUEST_STATUS_PENDING).equals(existedRequest.getStatus())
                && !currentUserId.equals(existedRequest.getRequesterId()))
        {
            throw new BusinessException(4007, "对方已申请添加你为好友，请先处理申请");
        }

        FriendRequest friendRequest = new FriendRequest()
                .setId(snowflakeIdGenerator.nextId())
                .setRequesterId(currentUserId)
                .setAddresseeId(addressee.getUserId())
                .setUserLowId(userLowId)
                .setUserHighId(userHighId)
                .setRequestMessage(normalizeRequestMessage(addFriendDTO.getMessage()))
                .setStatus(REQUEST_STATUS_PENDING);
        friendRequestMapper.insertOrResetPending(friendRequest);

        FriendRequest savedRequest = friendRequestMapper.selectByUsers(userLowId, userHighId);
        return toFriendRequestVO(savedRequest, requester, addressee);
    }

    /**
     * 查询当前用户收到的待处理好友申请。
     *
     * @param token 登录 token
     * @return 待处理好友申请列表
     */
    public List<FriendRequestVO> listPendingRequests(String token)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        return friendRequestMapper.selectByAddresseeAndStatus(currentUserId, REQUEST_STATUS_PENDING).stream()
                .map(this::toFriendRequestVO)
                .toList();
    }

    /**
     * 查询当前用户的好友列表。
     *
     * @param token 登录 token
     * @return 好友列表
     */
    public List<AccountUserVO> listFriends(String token)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        return friendRelationMapper.selectFriendAccounts(currentUserId).stream()
                .map(this::toUserVO)
                .toList();
    }

    /**
     * 同意好友申请，并创建好友关系。
     *
     * @param token     登录 token
     * @param requestId 好友申请 ID
     * @return 申请发起人的基础信息
     */
    @Transactional
    public AccountUserVO acceptRequest(String token, Long requestId)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        FriendRequest friendRequest = getPendingRequestForCurrentUser(requestId, currentUserId);
        Account requester = accountMapper.selectByUserId(friendRequest.getRequesterId());
        if (requester == null)
        {
            throw new BusinessException(4002, "好友不存在");
        }

        int updatedRows = friendRequestMapper.updateStatus(requestId, REQUEST_STATUS_ACCEPTED, REQUEST_STATUS_PENDING);
        if (updatedRows == 0)
        {
            throw new BusinessException(4006, "好友申请已处理");
        }

        FriendRelation friendRelation = new FriendRelation()
                .setId(snowflakeIdGenerator.nextId())
                .setUserLowId(friendRequest.getUserLowId())
                .setUserHighId(friendRequest.getUserHighId())
                .setInitiatorId(friendRequest.getRequesterId());
        friendRelationMapper.insertIgnore(friendRelation);
        return toUserVO(requester);
    }

    /**
     * 拒绝好友申请。
     *
     * @param token     登录 token
     * @param requestId 好友申请 ID
     */
    @Transactional
    public void rejectRequest(String token, Long requestId)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        getPendingRequestForCurrentUser(requestId, currentUserId);

        int updatedRows = friendRequestMapper.updateStatus(requestId, REQUEST_STATUS_REJECTED, REQUEST_STATUS_PENDING);
        if (updatedRows == 0)
        {
            throw new BusinessException(4006, "好友申请已处理");
        }
    }

    /**
     * 判断两名用户是否已经是好友。
     *
     * @param firstUserId  第一名用户 ID
     * @param secondUserId 第二名用户 ID
     * @return true 表示已经是好友
     */
    public boolean areFriends(Long firstUserId, Long secondUserId)
    {
        if (firstUserId == null || secondUserId == null)
        {
            return false;
        }
        Long userLowId = Math.min(firstUserId, secondUserId);
        Long userHighId = Math.max(firstUserId, secondUserId);
        return friendRelationMapper.selectByUsers(userLowId, userHighId) != null;
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
     * 根据用户 ID 或昵称定位好友账号。
     *
     * @param addFriendDTO 添加好友参数
     * @return 好友账号
     */
    private Account findFriendAccount(AddFriendDTO addFriendDTO)
    {
        if (addFriendDTO == null)
        {
            throw new BusinessException(400, "好友用户ID和昵称必须二选一");
        }

        boolean hasFriendUserId = addFriendDTO.getFriendUserId() != null;
        boolean hasFriendNickname = StringUtils.hasText(addFriendDTO.getFriendNickname());
        if (hasFriendUserId == hasFriendNickname)
        {
            throw new BusinessException(400, "好友用户ID和昵称必须二选一");
        }

        Account friend = hasFriendUserId
                ? accountMapper.selectByUserId(addFriendDTO.getFriendUserId())
                : accountMapper.selectByNickname(addFriendDTO.getFriendNickname());
        if (friend == null)
        {
            throw new BusinessException(4002, "好友不存在");
        }
        return friend;
    }

    /**
     * 查询当前账号，避免 token 中的用户 ID 指向已删除账号。
     *
     * @param currentUserId 当前用户 ID
     * @return 当前账号
     */
    private Account getCurrentAccount(Long currentUserId)
    {
        Account currentAccount = accountMapper.selectByUserId(currentUserId);
        if (currentAccount == null)
        {
            throw new BusinessException(401, "登录已失效");
        }
        return currentAccount;
    }

    /**
     * 查询当前用户可以处理的待处理申请。
     *
     * @param requestId     申请 ID
     * @param currentUserId 当前用户 ID
     * @return 待处理申请
     */
    private FriendRequest getPendingRequestForCurrentUser(Long requestId, Long currentUserId)
    {
        FriendRequest friendRequest = friendRequestMapper.selectByIdForUpdate(requestId);
        if (friendRequest == null)
        {
            throw new BusinessException(4004, "好友申请不存在");
        }
        if (!currentUserId.equals(friendRequest.getAddresseeId()))
        {
            throw new BusinessException(4005, "不能处理别人的好友申请");
        }
        if (!Integer.valueOf(REQUEST_STATUS_PENDING).equals(friendRequest.getStatus()))
        {
            throw new BusinessException(4006, "好友申请已处理");
        }
        return friendRequest;
    }

    /**
     * 标准化好友申请留言。
     *
     * @param message 客户端传入留言
     * @return 标准化后的留言
     */
    private String normalizeRequestMessage(String message)
    {
        if (!StringUtils.hasText(message))
        {
            return null;
        }
        return message.trim();
    }

    /**
     * 将账号实体转换成用户信息 VO。
     *
     * @param account 账号实体
     * @return 用户信息 VO
     */
    private AccountUserVO toUserVO(Account account)
    {
        return new AccountUserVO(account.getUserId(), account.getNickname(), account.getAvatar());
    }

    /**
     * 将好友申请实体转换成 VO。
     *
     * @param friendRequest 好友申请
     * @return 好友申请 VO
     */
    private FriendRequestVO toFriendRequestVO(FriendRequest friendRequest)
    {
        Account requester = accountMapper.selectByUserId(friendRequest.getRequesterId());
        Account addressee = accountMapper.selectByUserId(friendRequest.getAddresseeId());
        return toFriendRequestVO(friendRequest, requester, addressee);
    }

    /**
     * 将好友申请实体转换成 VO。
     *
     * @param friendRequest 好友申请
     * @param requester     申请发起人
     * @param addressee     申请接收人
     * @return 好友申请 VO
     */
    private FriendRequestVO toFriendRequestVO(FriendRequest friendRequest, Account requester, Account addressee)
    {
        return new FriendRequestVO(
                friendRequest.getId(),
                toUserVO(requester),
                toUserVO(addressee),
                friendRequest.getRequestMessage(),
                friendRequest.getStatus(),
                friendRequest.getCreateTime()
        );
    }
}
