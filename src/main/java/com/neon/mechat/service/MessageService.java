package com.neon.mechat.service;

import com.neon.mechat.common.BusinessException;
import com.neon.mechat.dto.SendMessageDTO;
import com.neon.mechat.entity.Account;
import com.neon.mechat.entity.Conversation;
import com.neon.mechat.entity.Message;
import com.neon.mechat.mapper.AccountMapper;
import com.neon.mechat.mapper.ConversationMapper;
import com.neon.mechat.mapper.MessageMapper;
import com.neon.mechat.repository.AccountRepository;
import com.neon.mechat.support.SnowflakeIdGenerator;
import com.neon.mechat.vo.MessageVO;
import com.neon.mechat.websocket.MessagePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MessageService
{
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final MessagePushService messagePushService;
    private final FriendRelationService friendRelationService;

    @Transactional
    public MessageVO sendMessage(String token, SendMessageDTO sendMessageDTO)
    {
        Long senderId = authenticate(token);
        if (senderId.equals(sendMessageDTO.getReceiverId()))
        {
            throw new BusinessException(2001, "不能给自己发送消息");
        }

        Account receiver = accountMapper.selectByUserId(sendMessageDTO.getReceiverId());
        if (receiver == null)
        {
            throw new BusinessException(2002, "接收者不存在");
        }
        if (!friendRelationService.areFriends(senderId, sendMessageDTO.getReceiverId()))
        {
            throw new BusinessException(2003, "只能给好友发送消息");
        }

        Message existedMessage = messageMapper.selectBySenderAndClientMessageId(senderId, sendMessageDTO.getClientMessageId());
        if (existedMessage != null)
        {
            return toVO(existedMessage);
        }

        Conversation conversation = getOrCreateConversation(senderId, sendMessageDTO.getReceiverId());
        Message message = new Message()
                .setConversationId(conversation.getId())
                .setSenderId(senderId)
                .setReceiverId(sendMessageDTO.getReceiverId())
                .setClientMessageId(sendMessageDTO.getClientMessageId())
                .setMessageType(sendMessageDTO.getMessageType())
                .setContent(sendMessageDTO.getContent());

        try
        {
            messageMapper.insert(message);
        }
        catch (DuplicateKeyException exception)
        {
            return toVO(messageMapper.selectBySenderAndClientMessageId(senderId, sendMessageDTO.getClientMessageId()));
        }

        conversationMapper.updateLastMessage(conversation.getId(), message.getId());
        MessageVO messageVO = toVO(messageMapper.selectById(message.getId()));
        pushAfterCommit(messageVO);
        return messageVO;
    }

    private Conversation getOrCreateConversation(Long senderId, Long receiverId)
    {
        Long userLowId = Math.min(senderId, receiverId);
        Long userHighId = Math.max(senderId, receiverId);

        Conversation conversation = conversationMapper.selectByUsers(userLowId, userHighId);
        if (conversation != null)
        {
            return conversation;
        }

        Conversation newConversation = new Conversation()
                .setId(snowflakeIdGenerator.nextId())
                .setUserLowId(userLowId)
                .setUserHighId(userHighId);
        conversationMapper.insertIgnore(newConversation);
        return conversationMapper.selectByUsers(userLowId, userHighId);
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

    private MessageVO toVO(Message message)
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

    private void pushAfterCommit(MessageVO messageVO)
    {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                messagePushService.pushMessage(messageVO);
            }
        });
    }
}
