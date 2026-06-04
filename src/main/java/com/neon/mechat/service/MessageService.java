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
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * 发送单聊消息。
     *
     * @param token 登录 token，用于识别发送者
     * @param sendMessageDTO 发送消息参数
     * @return 已持久化的消息信息
     */
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

        // 客户端弱网重试时，使用 senderId + clientMessageId 保证消息不会重复入库。
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
        return toVO(messageMapper.selectById(message.getId()));
    }

    /**
     * 获取或创建两名用户之间的单聊会话。
     *
     * @param senderId 发送者用户 ID
     * @param receiverId 接收者用户 ID
     * @return 单聊会话
     */
    private Conversation getOrCreateConversation(Long senderId, Long receiverId)
    {
        // 固定较小 ID 在前，避免 A-B 和 B-A 被创建成两条会话。
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

    /**
     * 根据 token 校验登录态并返回当前用户 ID。
     *
     * @param token 登录 token
     * @return 当前登录用户 ID
     */
    private Long authenticate(String token)
    {
        // 发送者只能从服务端登录态解析，不能相信客户端传入的 senderId。
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
     * 将消息实体转换成消息 VO。
     *
     * @param message 消息实体
     * @return 消息 VO
     */
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

}
