package com.neon.mechat.service;

import com.neon.mechat.common.BusinessException;
import com.neon.mechat.entity.Account;
import com.neon.mechat.entity.Conversation;
import com.neon.mechat.entity.Message;
import com.neon.mechat.mapper.AccountMapper;
import com.neon.mechat.mapper.ConversationMapper;
import com.neon.mechat.mapper.MessageMapper;
import com.neon.mechat.repository.AccountRepository;
import com.neon.mechat.vo.AccountUserVO;
import com.neon.mechat.vo.ConversationMessagePageVO;
import com.neon.mechat.vo.ConversationVO;
import com.neon.mechat.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService
{
    private static final int DEFAULT_CONVERSATION_LIMIT = 50;
    private static final int MAX_CONVERSATION_LIMIT = 100;
    private static final int DEFAULT_MESSAGE_LIMIT = 30;
    private static final int MAX_MESSAGE_LIMIT = 100;

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    /**
     * 查询当前用户的首页会话列表。
     *
     * @param token 登录 token
     * @param limit 查询数量
     * @return 会话列表
     */
    public List<ConversationVO> listConversations(String token, Integer limit)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        int actualLimit = normalizeLimit(limit, DEFAULT_CONVERSATION_LIMIT, MAX_CONVERSATION_LIMIT);
        return conversationMapper.selectByUserId(currentUserId, actualLimit).stream()
                .map(conversation -> toConversationVO(conversation, currentUserId))
                .toList();
    }

    /**
     * 查询指定会话的历史消息。
     *
     * @param token           登录 token
     * @param conversationId  会话 ID
     * @param beforeMessageId 只查询该消息 ID 之前的消息
     * @param limit           查询数量
     * @return 历史消息分页结果
     */
    public ConversationMessagePageVO listMessages(String token, Long conversationId, Long beforeMessageId, Integer limit)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        Conversation conversation = getConversationForCurrentUser(conversationId, currentUserId);
        int actualLimit = normalizeLimit(limit, DEFAULT_MESSAGE_LIMIT, MAX_MESSAGE_LIMIT);
        List<Message> queriedMessages = messageMapper.selectConversationMessages(conversation.getId(), beforeMessageId, actualLimit + 1);
        boolean hasMore = queriedMessages.size() > actualLimit;
        List<Message> returnedMessages = hasMore ? queriedMessages.subList(0, actualLimit) : queriedMessages;

        List<MessageVO> messageVOList = new ArrayList<>(returnedMessages.stream()
                .map(this::toMessageVO)
                .toList());
        Collections.reverse(messageVOList);
        return new ConversationMessagePageVO(messageVOList, hasMore);
    }

    /**
     * 标记会话已读。
     *
     * @param token          登录 token
     * @param conversationId 会话 ID
     */
    @Transactional
    public void markConversationRead(String token, Long conversationId)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        Conversation conversation = getConversationForCurrentUser(conversationId, currentUserId);
        if (conversation.getLastMessageId() != null)
        {
            conversationMapper.updateUserLastSyncMessage(conversation.getId(), currentUserId, conversation.getLastMessageId());
        }
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
     * 查询当前账号。
     *
     * @param currentUserId 当前用户 ID
     * @return 当前账号
     */
    private Account getCurrentAccount(Long currentUserId)
    {
        Account account = accountMapper.selectByUserId(currentUserId);
        if (account == null)
        {
            throw new BusinessException(401, "登录已失效");
        }
        return account;
    }

    /**
     * 查询并校验当前用户参与的会话。
     *
     * @param conversationId 会话 ID
     * @param currentUserId  当前用户 ID
     * @return 会话实体
     */
    private Conversation getConversationForCurrentUser(Long conversationId, Long currentUserId)
    {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !isParticipant(conversation, currentUserId))
        {
            throw new BusinessException(2004, "会话不存在");
        }
        return conversation;
    }

    /**
     * 转换会话实体。
     *
     * @param conversation  会话实体
     * @param currentUserId 当前用户 ID
     * @return 会话 VO
     */
    private ConversationVO toConversationVO(Conversation conversation, Long currentUserId)
    {
        Long peerUserId = currentUserId.equals(conversation.getUserLowId())
                ? conversation.getUserHighId()
                : conversation.getUserLowId();
        Account peer = accountMapper.selectByUserId(peerUserId);
        Long lastSyncMessageId = currentUserId.equals(conversation.getUserLowId())
                ? conversation.getUserLowLastSyncMessageId()
                : conversation.getUserHighLastSyncMessageId();
        int unreadCount = messageMapper.countUnreadMessages(conversation.getId(), currentUserId, lastSyncMessageId);
        Message lastMessage = conversation.getLastMessageId() == null
                ? null
                : messageMapper.selectById(conversation.getLastMessageId());

        return new ConversationVO(
                conversation.getId(),
                toAccountUserVO(peer),
                lastMessage == null ? null : toMessageVO(lastMessage),
                conversation.getLastMessageTime(),
                unreadCount
        );
    }

    /**
     * 判断用户是否参与会话。
     *
     * @param conversation 会话实体
     * @param userId       用户 ID
     * @return true 表示用户参与该会话
     */
    private boolean isParticipant(Conversation conversation, Long userId)
    {
        return userId.equals(conversation.getUserLowId()) || userId.equals(conversation.getUserHighId());
    }

    /**
     * 标准化分页数量。
     *
     * @param limit        客户端传入数量
     * @param defaultLimit 默认数量
     * @param maxLimit     最大数量
     * @return 实际数量
     */
    private int normalizeLimit(Integer limit, int defaultLimit, int maxLimit)
    {
        if (limit == null || limit <= 0)
        {
            return defaultLimit;
        }
        return Math.min(limit, maxLimit);
    }

    /**
     * 转换账号实体。
     *
     * @param account 账号实体
     * @return 用户信息 VO
     */
    private AccountUserVO toAccountUserVO(Account account)
    {
        return new AccountUserVO(account.getUserId(), account.getNickname(), account.getAvatar());
    }

    /**
     * 转换消息实体。
     *
     * @param message 消息实体
     * @return 消息 VO
     */
    private MessageVO toMessageVO(Message message)
    {
        return new MessageVO(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getClientMessageId(),
                message.getMessageType(),
                message.getContent(),
                message.getSendTime()
        );
    }
}
