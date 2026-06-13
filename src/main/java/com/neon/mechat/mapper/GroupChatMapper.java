package com.neon.mechat.mapper;

import com.neon.mechat.entity.Account;
import com.neon.mechat.entity.GroupChat;
import com.neon.mechat.entity.GroupChatMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupChatMapper
{
    int insert(GroupChat groupChat);

    int insertMember(GroupChatMember member);

    int insertMembers(@Param("members") List<GroupChatMember> members);

    GroupChat selectById(@Param("id") Long id);

    GroupChatMember selectMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    List<GroupChatMember> selectMembersByGroupId(@Param("groupId") Long groupId);

    int selectMemberCount(@Param("groupId") Long groupId);

    List<GroupChat> selectByUserId(@Param("userId") Long userId);

    List<Long> selectMemberIds(@Param("groupId") Long groupId);

    int updateLastReadMessageId(@Param("groupId") Long groupId,
                                @Param("userId") Long userId,
                                @Param("messageId") Long messageId);

    int deleteMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    int deleteGroup(@Param("id") Long id);

    int deleteAllMembers(@Param("groupId") Long groupId);
}
