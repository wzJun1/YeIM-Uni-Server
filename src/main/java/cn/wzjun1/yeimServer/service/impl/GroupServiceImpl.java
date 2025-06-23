package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.Conversation;
import cn.wzjun1.yeimServer.domain.GroupUser;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.dto.group.GroupCreateDTO;
import cn.wzjun1.yeimServer.dto.group.GroupEditDTO;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import cn.wzjun1.yeimServer.exception.group.GroupDuplicateException;
import cn.wzjun1.yeimServer.exception.group.GroupNotFoundException;
import cn.wzjun1.yeimServer.exception.group.GroupPermissionDeniedException;
import cn.wzjun1.yeimServer.exception.group.NoGroupUserException;
import cn.wzjun1.yeimServer.exception.user.UserNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.GroupUserMapper;
import cn.wzjun1.yeimServer.mapper.UserMapper;
import cn.wzjun1.yeimServer.service.*;
import cn.wzjun1.yeimServer.constant.ConversationType;
import cn.wzjun1.yeimServer.constant.JoinGroupMode;
import cn.wzjun1.yeimServer.constant.MessageType;
import cn.wzjun1.yeimServer.utils.Common;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.wzjun1.yeimServer.domain.Group;
import cn.wzjun1.yeimServer.mapper.GroupMapper;
import com.github.yitter.idgen.YitIdHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【group】的数据库操作Service实现
 * @createDate 2022-12-07 20:00:11
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group>
        implements GroupService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    MessageService messageService;

    @Autowired
    GroupMessageService groupMessageService;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    GroupUserService groupUserService;

    @Autowired
    GroupUserMapper groupUserMapper;

    @Autowired
    ConversationService conversationService;

    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    @Override
    public void createGroup(GroupCreateDTO params) throws Exception {
        try {

            String groupId = "";
            if (params.getGroupId() == null || params.getGroupId().isEmpty()) {
                groupId = "group_" + YitIdHelper.nextId();
            } else {
                groupId = params.getGroupId();
            }

            boolean exist = groupMapper.exists(new QueryWrapper<Group>().eq("group_id", groupId));
            if (exist) {
                throw new GroupDuplicateException("群ID重复");
            }

            int joinMode = JoinGroupMode.FREE;
            if (params.getJoinMode() == null) {

            } else if (params.getJoinMode().equals(JoinGroupMode.CHECK)) {
                joinMode = JoinGroupMode.CHECK;
            } else if (params.getJoinMode().equals(JoinGroupMode.FORBIDDEN)) {
                joinMode = JoinGroupMode.FORBIDDEN;
            }

            //创建群组，保存到数据库
            Group group = new Group();
            group.setGroupId(groupId);
            group.setName(params.getName());
            group.setAvatarUrl(params.getAvatarUrl());
            group.setLeaderUserId(LoginUserContext.getUser().getUserId());
            group.setJoinMode(joinMode);
            group.setIntroduction(params.getIntroduction());
            group.setNotification(params.getNotification());
            group.setCreatedAt(System.currentTimeMillis());
            this.save(group);

            //传递的初始用户列表，为空则只有自己
            List<String> members = params.getMembers();
            if (members == null) {
                members = new ArrayList<>();
                members.add(LoginUserContext.getUser().getUserId());
            } else {
                if (!members.contains(LoginUserContext.getUser().getUserId())) {
                    members.add(LoginUserContext.getUser().getUserId());
                }
            }

            List<GroupUser> groupUsers = new ArrayList<>();
            List<Conversation> conversations = new ArrayList<>();

            //传递的初始用户列表，加入群组，保存到数据库
            members.forEach(userId -> {
                //群成员关联群组
                GroupUser groupUser = new GroupUser();
                groupUser.setGroupId(group.getGroupId());
                groupUser.setUserId(userId);
                groupUser.setJoinAt(System.currentTimeMillis());
                groupUser.setCreatedAt(System.currentTimeMillis());
                groupUsers.add(groupUser);

                //每个群成员均有一个此群的会话
                Conversation conversation = new Conversation();
                conversation.setUnread(0);
                conversation.setConversationId(group.getGroupId());
                conversation.setType(ConversationType.GROUP);
                conversation.setUserId(userId);
                conversation.setLastMessageId("");
                conversation.setUpdatedAt(System.currentTimeMillis());
                conversation.setCreatedAt(System.currentTimeMillis());
                conversations.add(conversation);

            });
            //群成员批量关联到群组
            groupUserService.saveBatch(groupUsers);
            //批量创建群成员的此群会话
            conversationService.saveOrUpdateBatch(conversations);

            //发送通知
            MessageSaveDTO message = new MessageSaveDTO();
            message.setType(MessageType.GROUP_SYS_NOTICE);
            message.setConversationType(ConversationType.GROUP);
            message.setConversationId(group.getGroupId());
            message.setFrom("0");
            message.setTo(group.getGroupId());
            message.setBody(new JSONObject(new HashMap<String, Object>() {{
                put("tips", LoginUserContext.getUser().getNickname() + "创建了群组：" + params.getName());
            }}));
            message.setExtra("");
            message.setTime(System.currentTimeMillis());
            groupMessageService.insertGroupMessage(LoginUserContext.getUser(), message);

        } catch (Exception e) {
            if (e instanceof GroupDuplicateException){
                throw new GroupDuplicateException(e.getMessage());
            }{
                throw new Exception(e.getMessage());
            }
        }
    }

    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    @Override
    public void dissolveGroup(String groupId) throws Exception {

        //判断群组是否存在
        Group isExistGroup = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));
        if (isExistGroup == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }

        if (!isExistGroup.getLeaderUserId().equals(LoginUserContext.getUser().getUserId())) {
            throw new GroupPermissionDeniedException("仅群主可解散该群");
        }

        MessageSaveDTO message = new MessageSaveDTO();
        message.setType(MessageType.GROUP_SYS_NOTICE);
        message.setConversationType(ConversationType.GROUP);
        message.setConversationId(groupId);
        message.setFrom("0");
        message.setTo(groupId);
        message.setBody(new JSONObject(new HashMap<String, Object>() {{
            put("tips", "群主解散了该群");
        }}));
        message.setExtra("");
        message.setTime(System.currentTimeMillis());
        groupMessageService.insertGroupMessage(LoginUserContext.getUser(), message);

        Group update = new Group();
        update.setIsDissolve(1);
        groupMapper.update(update, new QueryWrapper<Group>().eq("group_id", groupId));

    }

    @Override
    public void updateGroup(GroupEditDTO params) throws Exception {

        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", params.getGroupId()).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }

        GroupUser operatorGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", params.getGroupId()).eq("user_id", LoginUserContext.getUser().getUserId()));
        if (operatorGroupUser == null || operatorGroupUser.getUserId() == null) {
            throw new NoGroupUserException("非群成员无法执行此操作");
        }

        boolean isAllowHandle = false;
        if (group.getLeaderUserId().equals(LoginUserContext.getUser().getUserId())) {
            //群主直接操作
            isAllowHandle = true;
        } else {
            //判断是不是管理员
            if (operatorGroupUser.getIsAdmin() == 1) {
                isAllowHandle = true;
            }
        }

        if (!isAllowHandle) {
            throw new GroupPermissionDeniedException("无权限操作");
        }
        Group update = new Group();
        if (params.getName() != null) {
            update.setName(params.getName());
        }
        if (params.getAvatarUrl() != null) {
            update.setAvatarUrl(params.getAvatarUrl());
        }
        if (params.getJoinMode() != null) {
            update.setJoinMode(params.getJoinMode());
        }
        if (params.getIntroduction() != null) {
            update.setIntroduction(params.getIntroduction());
        }
        if (params.getNotification() != null) {
            update.setNotification(params.getNotification());
        }
        if (params.getIsMute() != null) {
            if (params.getIsMute().equals(1)) {
                update.setIsMute(1);
            } else {
                update.setIsMute(0);
            }
            if ((group.getIsMute().equals(1) && params.getIsMute().equals(0)) || (group.getIsMute().equals(0) && params.getIsMute().equals(1))){
                //关闭全体禁言和开启全体禁言
                String tips = "";
                if (params.getIsMute().equals(0)){
                    tips = LoginUserContext.getUser().getNickname() + "关闭了全体禁言";
                }else{
                    tips = LoginUserContext.getUser().getNickname() + "开启了全体禁言";
                }
                MessageSaveDTO message = new MessageSaveDTO();
                message.setType(MessageType.GROUP_SYS_NOTICE);
                message.setConversationType(ConversationType.GROUP);
                message.setConversationId(group.getGroupId());
                message.setFrom("0");
                message.setTo(group.getGroupId());
                String finalTips = tips;
                message.setBody(new JSONObject(new HashMap<String, Object>() {{
                    put("tips", finalTips);
                }}));
                message.setExtra("");
                message.setTime(System.currentTimeMillis());
                groupMessageService.insertGroupMessage(LoginUserContext.getUser(), message);
            }
        }
        if (Common.isNotEmptyBean(update)) {
            groupMapper.update(update, new QueryWrapper<Group>().eq("group_id", params.getGroupId()));
        }
    }

    @Override
    public void transferLeader(String groupId, String userId) throws Exception {

        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }
        //仅群主可操作转让
        if (!group.getLeaderUserId().equals(LoginUserContext.getUser().getUserId())) {
            throw new GroupPermissionDeniedException("无权限操作");
        }

        //查询用户是否存在
        User isUserExist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userId));
        if (isUserExist == null || isUserExist.getUserId() == null) {
            throw new UserNotFoundException("此用户不存在");
        }

        GroupUser isGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", groupId).eq("user_id", userId));
        if (isGroupUser == null || isGroupUser.getUserId() == null) {
            throw new NoGroupUserException("转让的用户不在当前群组，无法转让");
        }

        Group update = new Group();
        update.setLeaderUserId(userId);
        groupMapper.update(update, new QueryWrapper<Group>().eq("id", group.getId()));

        MessageSaveDTO message = new MessageSaveDTO();
        message.setType(MessageType.GROUP_SYS_NOTICE);
        message.setConversationType(ConversationType.GROUP);
        message.setConversationId(groupId);
        message.setFrom("0");
        message.setTo(groupId);
        message.setBody(new JSONObject(new HashMap<String, Object>() {{
            put("tips", LoginUserContext.getUser().getNickname() + "已转让群主给" + isUserExist.getNickname());
        }}));
        message.setExtra("");
        message.setTime(System.currentTimeMillis());
        groupMessageService.insertGroupMessage(LoginUserContext.getUser(), message);

    }

}




