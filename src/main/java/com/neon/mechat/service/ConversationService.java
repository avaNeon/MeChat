package com.neon.mechat.service;

import com.neon.mechat.common.BusinessException;
import com.neon.mechat.entity.Account;
import com.neon.mechat.entity.Conversation;
import com.neon.mechat.entity.GroupChat;
import com.neon.mechat.entity.GroupChatMember;
import com.neon.mechat.entity.Message;
import com.neon.mechat.mapper.AccountMapper;
import com.neon.mechat.mapper.ConversationMapper;
import com.neon.mechat.mapper.GroupChatMapper;
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
    private static final int TYPE_SINGLE = 0;
    private static final int TYPE_GROUP = 1;

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final GroupChatMapper groupChatMapper;

    public List<ConversationVO> listConversations(String token, Integer limit)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        int actualLimit = normalizeLimit(limit, DEFAULT_CONVERSATION_LIMIT, MAX_CONVERSATION_LIMIT);
        return conversationMapper.selectByUserId(currentUserId, actualLimit).stream()
                .map(conversation -> toConversationVO(conversation, currentUserId))
                .toList();
    }

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

    @Transactional
    public void markConversationRead(String token, Long conversationId)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        Conversation conversation = getConversationForCurrentUser(conversationId, currentUserId);
        if (Integer.valueOf(TYPE_GROUP).equals(conversation.getType()))
        {
            if (conversation.getLastMessageId() != null)
            {
                groupChatMapper.updateLastReadMessageId(conversation.getGroupId(), currentUserId, conversation.getLastMessageId());
            }
        }
        else
        {
            if (conversation.getLastMessageId() != null)
            {
                conversationMapper.updateUserLastSyncMessage(conversation.getId(), currentUserId, conversation.getLastMessageId());
            }
        }
    }

    public ConversationMessagePageVO listGroupMessages(String token, Long groupId, Long beforeMessageId, Integer limit)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        GroupChat groupChat = groupChatMapper.selectById(groupId);
        if (groupChat == null)
        {
            throw new BusinessException(4102, "群聊不存在");
        }
        if (groupChatMapper.selectMember(groupId, currentUserId) == null)
        {
            throw new BusinessException(4103, "你不是群成员，无权查看");
        }
        int actualLimit = normalizeLimit(limit, DEFAULT_MESSAGE_LIMIT, MAX_MESSAGE_LIMIT);
        List<Message> queriedMessages = messageMapper.selectGroupMessages(groupId, beforeMessageId, actualLimit + 1);
        boolean hasMore = queriedMessages.size() > actualLimit;
        List<Message> returnedMessages = hasMore ? queriedMessages.subList(0, actualLimit) : queriedMessages;
        List<MessageVO> messageVOList = new ArrayList<>(returnedMessages.stream()
                .map(this::toMessageVO)
                .toList());
        Collections.reverse(messageVOList);
        return new ConversationMessagePageVO(messageVOList, hasMore);
    }

    @Transactional
    public void markGroupRead(String token, Long groupId)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);
        GroupChat groupChat = groupChatMapper.selectById(groupId);
        if (groupChat == null)
        {
            throw new BusinessException(4102, "群聊不存在");
        }
        if (groupChatMapper.selectMember(groupId, currentUserId) == null)
        {
            throw new BusinessException(4103, "你不是群成员");
        }
        Conversation conversation = conversationMapper.selectByGroupId(groupId);
        if (conversation != null && conversation.getLastMessageId() != null)
        {
            groupChatMapper.updateLastReadMessageId(groupId, currentUserId, conversation.getLastMessageId());
        }
    }

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

    private Account getCurrentAccount(Long currentUserId)
    {
        Account account = accountMapper.selectByUserId(currentUserId);
        if (account == null)
        {
            throw new BusinessException(401, "登录已失效");
        }
        return account;
    }

    private Conversation getConversationForCurrentUser(Long conversationId, Long currentUserId)
    {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !isParticipant(conversation, currentUserId))
        {
            throw new BusinessException(2004, "会话不存在");
        }
        return conversation;
    }

    private ConversationVO toConversationVO(Conversation conversation, Long currentUserId)
    {
        Message lastMessage = conversation.getLastMessageId() == null
                ? null
                : messageMapper.selectById(conversation.getLastMessageId());

        if (Integer.valueOf(TYPE_GROUP).equals(conversation.getType()))
        {
            GroupChat groupChat = groupChatMapper.selectById(conversation.getGroupId());
            GroupChatMember member = groupChatMapper.selectMember(conversation.getGroupId(), currentUserId);
            long lastReadId = member != null ? (member.getLastReadMessageId() != null ? member.getLastReadMessageId() : 0L) : 0L;
            int unread = messageMapper.countGroupUnreadMessages(conversation.getGroupId(), currentUserId, lastReadId);

            return new ConversationVO(
                    conversation.getId(),
                    TYPE_GROUP,
                    conversation.getGroupId(),
                    groupChat != null ? groupChat.getGroupName() : null,
                    groupChat != null ? groupChat.getAvatar() : null,
                    null,
                    lastMessage == null ? null : toMessageVO(lastMessage),
                    conversation.getLastMessageTime(),
                    unread
            );
        }

        Long peerUserId = currentUserId.equals(conversation.getUserLowId())
                ? conversation.getUserHighId()
                : conversation.getUserLowId();
        Account peer = accountMapper.selectByUserId(peerUserId);
        Long lastSyncMessageId = currentUserId.equals(conversation.getUserLowId())
                ? conversation.getUserLowLastSyncMessageId()
                : conversation.getUserHighLastSyncMessageId();
        int unread = messageMapper.countUnreadMessages(conversation.getId(), currentUserId, lastSyncMessageId);

        return new ConversationVO(
                conversation.getId(),
                TYPE_SINGLE,
                null,
                null,
                null,
                toAccountUserVO(peer),
                lastMessage == null ? null : toMessageVO(lastMessage),
                conversation.getLastMessageTime(),
                unread
        );
    }

    private boolean isParticipant(Conversation conversation, Long userId)
    {
        if (Integer.valueOf(TYPE_GROUP).equals(conversation.getType()))
        {
            return groupChatMapper.selectMember(conversation.getGroupId(), userId) != null;
        }
        return userId.equals(conversation.getUserLowId()) || userId.equals(conversation.getUserHighId());
    }

    private int normalizeLimit(Integer limit, int defaultLimit, int maxLimit)
    {
        if (limit == null || limit <= 0)
        {
            return defaultLimit;
        }
        return Math.min(limit, maxLimit);
    }

    private AccountUserVO toAccountUserVO(Account account)
    {
        return new AccountUserVO(account.getUserId(), account.getNickname(), account.getAvatar());
    }

    private MessageVO toMessageVO(Message message)
    {
        return new MessageVO(
                message.getId(),
                message.getConversationId(),
                message.getGroupId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getClientMessageId(),
                message.getMessageType(),
                message.getContent(),
                message.getImgPath(),
                message.getSendTime()
        );
    }
}
