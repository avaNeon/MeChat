package com.neon.mechat.service;

import com.neon.mechat.common.BusinessException;
import com.neon.mechat.config.properties.AccountProperties;
import com.neon.mechat.dto.SendGroupMessageDTO;
import com.neon.mechat.dto.SendMessageDTO;
import com.neon.mechat.entity.Account;
import com.neon.mechat.entity.Conversation;
import com.neon.mechat.entity.GroupChat;
import com.neon.mechat.entity.Message;
import com.neon.mechat.mapper.AccountMapper;
import com.neon.mechat.mapper.ConversationMapper;
import com.neon.mechat.mapper.GroupChatMapper;
import com.neon.mechat.mapper.MessageMapper;
import com.neon.mechat.repository.AccountRepository;
import com.neon.mechat.repository.MessageFileRepository;
import com.neon.mechat.support.SnowflakeIdGenerator;
import com.neon.mechat.vo.MessageVO;
import com.neon.mechat.websocket.MessagePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
    private final MessageFileRepository messageFileRepository;
    private final AccountProperties accountProperties;
    private final GroupChatMapper groupChatMapper;

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
                .setContent(sendMessageDTO.getContent())
                .setImgPath(sendMessageDTO.getImgPath());

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

    @Transactional
    public MessageVO sendGroupMessage(String token, Long groupId, SendGroupMessageDTO sendGroupMessageDTO)
    {
        Long senderId = authenticate(token);
        getCurrentAccount(senderId);

        GroupChat groupChat = groupChatMapper.selectById(groupId);
        if (groupChat == null)
        {
            throw new BusinessException(4102, "群聊不存在");
        }
        if (groupChatMapper.selectMember(groupId, senderId) == null)
        {
            throw new BusinessException(4103, "你不是群成员，无权发送消息");
        }

        if (StringUtils.hasText(sendGroupMessageDTO.getClientMessageId()))
        {
            Message existed = messageMapper.selectBySenderAndClientMessageId(senderId, sendGroupMessageDTO.getClientMessageId());
            if (existed != null)
            {
                return toVO(existed);
            }
        }
        else
        {
            sendGroupMessageDTO.setClientMessageId(java.util.UUID.randomUUID().toString().replace("-", ""));
        }

        Conversation conversation = conversationMapper.selectByGroupId(groupId);
        if (conversation == null)
        {
            throw new BusinessException(4102, "群聊会话不存在");
        }

        Message message = new Message()
                .setConversationId(conversation.getId())
                .setGroupId(groupId)
                .setSenderId(senderId)
                .setReceiverId(0L)
                .setClientMessageId(sendGroupMessageDTO.getClientMessageId())
                .setMessageType(sendGroupMessageDTO.getMessageType())
                .setContent(sendGroupMessageDTO.getContent())
                .setImgPath(sendGroupMessageDTO.getImgPath());

        try
        {
            messageMapper.insert(message);
        }
        catch (DuplicateKeyException exception)
        {
            return toVO(messageMapper.selectBySenderAndClientMessageId(senderId, sendGroupMessageDTO.getClientMessageId()));
        }

        conversationMapper.updateLastMessage(conversation.getId(), message.getId());
        MessageVO messageVO = toVO(messageMapper.selectById(message.getId()));
        pushGroupAfterCommit(messageVO);
        return messageVO;
    }

    @Transactional
    public String uploadImage(String token, MultipartFile imageFile)
    {
        Long userId = authenticate(token);
        Account account = accountMapper.selectByUserId(userId);
        if (account == null)
        {
            throw new BusinessException(401, "登录已失效");
        }

        long fileSize = imageFile.getSize();
        long dailyUsed = accountRepository.getDailyUploadBytes(userId);
        if (dailyUsed + fileSize > accountProperties.getUploadDailyQuotaBytes())
        {
            throw new BusinessException(3004, "上传超过每日限额");
        }

        String relativePath = messageFileRepository.save(userId, imageFile);
        accountRepository.addDailyUploadBytes(userId, fileSize);
        return relativePath;
    }

    public Resource downloadImage(String token, String relativePath)
    {
        authenticate(token);
        return messageFileRepository.load(relativePath);
    }

    public String getImageContentType(String relativePath)
    {
        return messageFileRepository.getContentType(relativePath);
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

    private Account getCurrentAccount(Long userId)
    {
        Account account = accountMapper.selectByUserId(userId);
        if (account == null)
        {
            throw new BusinessException(401, "登录已失效");
        }
        return account;
    }

    private MessageVO toVO(Message message)
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

    private void pushGroupAfterCommit(MessageVO messageVO)
    {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                messagePushService.pushGroupMessage(messageVO);
            }
        });
    }
}
