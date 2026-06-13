package com.neon.mechat.service;

import com.neon.mechat.common.BusinessException;
import com.neon.mechat.dto.CreateGroupDTO;
import com.neon.mechat.entity.Account;
import com.neon.mechat.entity.Conversation;
import com.neon.mechat.entity.GroupChat;
import com.neon.mechat.entity.GroupChatMember;
import com.neon.mechat.mapper.AccountMapper;
import com.neon.mechat.mapper.ConversationMapper;
import com.neon.mechat.mapper.GroupChatMapper;
import com.neon.mechat.repository.AccountRepository;
import com.neon.mechat.support.SnowflakeIdGenerator;
import com.neon.mechat.vo.AccountUserVO;
import com.neon.mechat.vo.GroupChatVO;
import com.neon.mechat.vo.GroupMemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatService
{
    private static final int ROLE_MEMBER = 0;
    private static final int ROLE_OWNER = 1;

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final GroupChatMapper groupChatMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final FriendRelationService friendRelationService;
    private final ConversationMapper conversationMapper;

    @Transactional
    public GroupChatVO createGroup(String token, CreateGroupDTO createGroupDTO)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);

        List<Long> memberIds = new ArrayList<>(createGroupDTO.getMemberUserIds());
        memberIds.remove(currentUserId);
        for (Long memberId : memberIds)
        {
            if (!friendRelationService.areFriends(currentUserId, memberId))
            {
                Account account = accountMapper.selectByUserId(memberId);
                String nickname = account != null ? account.getNickname() : String.valueOf(memberId);
                throw new BusinessException(4101, "用户 " + nickname + " 不是你的好友，无法拉入群聊");
            }
        }

        GroupChat groupChat = new GroupChat()
                .setId(snowflakeIdGenerator.nextId())
                .setGroupName(createGroupDTO.getGroupName().trim())
                .setOwnerId(currentUserId);
        groupChatMapper.insert(groupChat);

        List<GroupChatMember> members = new ArrayList<>();
        members.add(new GroupChatMember()
                .setId(snowflakeIdGenerator.nextId())
                .setGroupId(groupChat.getId())
                .setUserId(currentUserId)
                .setRole(ROLE_OWNER));
        for (Long memberId : memberIds)
        {
            members.add(new GroupChatMember()
                    .setId(snowflakeIdGenerator.nextId())
                    .setGroupId(groupChat.getId())
                    .setUserId(memberId)
                    .setRole(ROLE_MEMBER));
        }
        groupChatMapper.insertMembers(members);

        Conversation conversation = new Conversation()
                .setId(snowflakeIdGenerator.nextId())
                .setType(1)
                .setGroupId(groupChat.getId());
        conversationMapper.insertGroup(conversation);

        return toGroupChatVO(groupChat);
    }

    @Transactional
    public void addMembers(String token, Long groupId, List<Long> userIds)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);

        GroupChat groupChat = getGroupChat(groupId);
        if (groupChatMapper.selectMember(groupId, currentUserId) == null)
        {
            throw new BusinessException(4103, "你不是群成员，无权拉人");
        }

        Set<Long> newMemberIds = userIds.stream()
                .filter(id -> !id.equals(currentUserId))
                .collect(Collectors.toSet());
        if (newMemberIds.isEmpty())
        {
            return;
        }

        for (Long memberId : newMemberIds)
        {
            if (!friendRelationService.areFriends(currentUserId, memberId))
            {
                Account account = accountMapper.selectByUserId(memberId);
                String nickname = account != null ? account.getNickname() : String.valueOf(memberId);
                throw new BusinessException(4101, "用户 " + nickname + " 不是你的好友，无法拉入群聊");
            }
        }

        List<GroupChatMember> members = newMemberIds.stream()
                .map(userId -> new GroupChatMember()
                        .setId(snowflakeIdGenerator.nextId())
                        .setGroupId(groupId)
                        .setUserId(userId)
                        .setRole(ROLE_MEMBER))
                .toList();
        groupChatMapper.insertMembers(members);
    }

    @Transactional
    public void kickMember(String token, Long groupId, Long targetUserId)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);

        GroupChat groupChat = getGroupChat(groupId);
        if (!groupChat.getOwnerId().equals(currentUserId))
        {
            throw new BusinessException(4104, "只有群主才能踢人");
        }
        if (targetUserId.equals(currentUserId))
        {
            throw new BusinessException(4105, "群主不能踢自己");
        }
        if (targetUserId.equals(groupChat.getOwnerId()))
        {
            throw new BusinessException(4105, "不能踢群主");
        }

        int deleted = groupChatMapper.deleteMember(groupId, targetUserId);
        if (deleted == 0)
        {
            throw new BusinessException(4106, "该用户不在群中");
        }

        int remainingCount = groupChatMapper.selectMemberCount(groupId);
        if (remainingCount == 0)
        {
            conversationMapper.deleteByGroupId(groupId);
            groupChatMapper.deleteGroup(groupId);
        }
    }

    public List<GroupMemberVO> listMembers(String token, Long groupId)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);

        GroupChat groupChat = getGroupChat(groupId);
        if (groupChatMapper.selectMember(groupId, currentUserId) == null)
        {
            throw new BusinessException(4103, "你不是群成员，无权查看");
        }

        return groupChatMapper.selectMembersByGroupId(groupId).stream()
                .map(this::toGroupMemberVO)
                .toList();
    }

    public List<GroupChatVO> listMyGroups(String token)
    {
        Long currentUserId = authenticate(token);
        getCurrentAccount(currentUserId);

        return groupChatMapper.selectByUserId(currentUserId).stream()
                .map(this::toGroupChatVO)
                .toList();
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
        Account currentAccount = accountMapper.selectByUserId(currentUserId);
        if (currentAccount == null)
        {
            throw new BusinessException(401, "登录已失效");
        }
        return currentAccount;
    }

    private GroupChat getGroupChat(Long groupId)
    {
        GroupChat groupChat = groupChatMapper.selectById(groupId);
        if (groupChat == null)
        {
            throw new BusinessException(4102, "群聊不存在");
        }
        return groupChat;
    }

    private GroupChatVO toGroupChatVO(GroupChat groupChat)
    {
        Account owner = accountMapper.selectByUserId(groupChat.getOwnerId());
        AccountUserVO ownerVO = new AccountUserVO(
                owner != null ? owner.getUserId() : groupChat.getOwnerId(),
                owner != null ? owner.getNickname() : "未知",
                owner != null ? owner.getAvatar() : null);
        int memberCount = groupChatMapper.selectMemberCount(groupChat.getId());
        return new GroupChatVO(
                groupChat.getId(),
                groupChat.getGroupName(),
                ownerVO,
                groupChat.getAvatar(),
                memberCount,
                groupChat.getCreateTime());
    }

    private GroupMemberVO toGroupMemberVO(GroupChatMember member)
    {
        Account account = accountMapper.selectByUserId(member.getUserId());
        AccountUserVO userVO = new AccountUserVO(
                account != null ? account.getUserId() : member.getUserId(),
                account != null ? account.getNickname() : "未知",
                account != null ? account.getAvatar() : null);
        return new GroupMemberVO(userVO, member.getRole(), member.getJoinTime());
    }
}
