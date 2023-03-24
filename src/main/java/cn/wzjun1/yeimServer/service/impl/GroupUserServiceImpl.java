package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.constant.*;
import cn.wzjun1.yeimServer.domain.*;
import cn.wzjun1.yeimServer.dto.group.GroupUserAddDTO;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import cn.wzjun1.yeimServer.exception.group.*;
import cn.wzjun1.yeimServer.exception.message.IdException;
import cn.wzjun1.yeimServer.exception.user.UserNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.GroupApplyMapper;
import cn.wzjun1.yeimServer.mapper.GroupMapper;
import cn.wzjun1.yeimServer.mapper.UserMapper;
import cn.wzjun1.yeimServer.result.vo.AddUserToGroupResultVO;
import cn.wzjun1.yeimServer.service.*;
import cn.wzjun1.yeimServer.socket.WebSocket;
import cn.wzjun1.yeimServer.result.Result;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.wzjun1.yeimServer.mapper.GroupUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【group_user】的数据库操作Service实现
 * @createDate 2022-12-07 20:55:41
 */
@Service
public class GroupUserServiceImpl extends ServiceImpl<GroupUserMapper, GroupUser>
        implements GroupUserService {


    @Autowired
    UserMapper userMapper;

    @Autowired
    ConversationService conversationService;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    GroupUserMapper groupUserMapper;

    @Autowired
    GroupMessageService groupMessageService;

    @Autowired
    GroupApplyMapper groupApplyMapper;

    @Autowired
    GroupApplyService groupApplyService;

    @Autowired
    OnlineChannel onlineChannel;

    /**
     * 添加群成员
     *
     * @param groupUserAddDTO
     * @return
     * @throws Exception
     */
    @Override
    public AddUserToGroupResultVO addUserToGroup(GroupUserAddDTO groupUserAddDTO) throws Exception {

        //返回值
        AddUserToGroupResultVO addUserToGroupResultVO = new AddUserToGroupResultVO();
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();
        List<String> ignoreList = new ArrayList<>();

        if (groupUserAddDTO.getMembers().size() > 500) {
            throw new GroupUserInsertLimitException("单次最多添加500个成员");
        }

        //判断群组是否存在
        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupUserAddDTO.getGroupId()).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }
        addUserToGroupResultVO.setGroup(group);
        //判断加群方式
        //禁止加群直接返回
        if (group.getJoinMode().equals(JoinGroupMode.FORBIDDEN)) {
            throw new GroupNoEntryException("当前群组设置禁止加群");
        }
        //是否需要申请才能入群
        boolean needApply = true;
        //提交的用户列表需验证后才能加入群
        if (group.getJoinMode().equals(JoinGroupMode.CHECK)) {
            //判断提交者是不是群主或管理员，是的话不需要验证，否则转为申请
            if (group.getLeaderUserId().equals(LoginUserContext.getUser().getUserId())) {
                needApply = false;
            } else {
                GroupUser operatorGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", groupUserAddDTO.getGroupId()).eq("user_id", LoginUserContext.getUser().getUserId()));
                //如果操作者已是群成员且是管理员，也不需要申请，可以直接拉人入群
                if (operatorGroupUser != null && operatorGroupUser.getIsAdmin() == 1) {
                    needApply = false;
                }
            }
        }
        if (group.getJoinMode().equals(JoinGroupMode.FREE)) {
            needApply = false;
        }

        if (needApply) {
            //需要申请，保存申请记录，发送给群主、管理员申请事件
            List<GroupApply> groupApplies = new ArrayList<>();
            groupUserAddDTO.getMembers().forEach(userId -> {

                //查询用户是否存在，不存在则执行下一个用户
                User isUserExist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userId));
                if (isUserExist == null || isUserExist.getUserId() == null) {
                    failList.add(userId);
                    return;
                }

                //查询用户是否已经是群成员，是则执行下一个用户
                boolean isGroupUser = groupUserMapper.exists(new QueryWrapper<GroupUser>().eq("group_id", groupUserAddDTO.getGroupId()).eq("user_id", userId));
                if (isGroupUser) {
                    ignoreList.add(userId);
                    return;
                }
                try {
                    GroupApply groupApply = new GroupApply();
                    groupApply.setGroupId(group.getGroupId());
                    groupApply.setUserId(userId);
                    groupApply.setInviterId(LoginUserContext.getUser().getUserId());
                    groupApply.setStatus(GroupApplyStatus.PENDING);
                    groupApply.setCreatedAt(System.currentTimeMillis());
                    groupApply.setUserInfo(isUserExist);
                    groupApply.setGroupInfo(group);
                    //保存入群申请
                    groupApplyService.save(groupApply);
                    groupApplies.add(groupApply);
                    successList.add(userId);
                } catch (Exception e) {
                    failList.add(userId);
                }

            });
            //通知群管理入群申请事件
            emitJSSDKGroupUserApply(group.getGroupId(), groupApplies);
        } else {
            //不需要申请，直接拉人入群，关联群组、保存会话、发送入群事件。
            List<String> addUserName = new ArrayList<>();
            //遍历前端传入的用户列表
            groupUserAddDTO.getMembers().forEach(userId -> {

                //查询用户是否存在，不存在则执行下一个用户
                User isUserExist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userId));
                if (isUserExist == null || isUserExist.getUserId() == null) {
                    failList.add(userId);
                    return;
                }

                //查询用户是否已经是群成员，是则执行下一个用户
                boolean isGroupUser = groupUserMapper.exists(new QueryWrapper<GroupUser>().eq("group_id", groupUserAddDTO.getGroupId()).eq("user_id", userId));
                if (isGroupUser) {
                    ignoreList.add(userId);
                    return;
                }

                try {
                    //群成员关联群组
                    GroupUser groupUser = new GroupUser();
                    groupUser.setGroupId(group.getGroupId());
                    groupUser.setUserId(userId);
                    groupUser.setJoinAt(System.currentTimeMillis());
                    groupUser.setCreatedAt(System.currentTimeMillis());
                    //群成员关联到群组
                    this.save(groupUser);
                    //只要关联到群组，则表示成功
                    successList.add(userId);
                    addUserName.add(isUserExist.getNickname());
                } catch (Exception e) {
                    failList.add(userId);
                }

                try {
                    if (failList.contains(userId)) {
                        return;
                    }
                    //每个群成员均有一个此群的会话
                    Conversation conversation = new Conversation();
                    conversation.setUnread(0);
                    conversation.setConversationId(group.getGroupId());
                    conversation.setType(ConversationType.GROUP);
                    conversation.setUserId(userId);
                    conversation.setLastMessageId("");
                    conversation.setUpdatedAt(System.currentTimeMillis());
                    conversation.setCreatedAt(System.currentTimeMillis());
                    //创建群成员的此群会话
                    conversationService.save(conversation);
                } catch (Exception e) {
                    //try 如果此会话已存在或保存出错则忽略，发送消息时也会自动创建会话，此处可不作处理。
                }

            });

            //暂时无用
            //if (addUserName.size() == 0) {
            //  throw new Exception("传递的用户ID列表没有可以添加的");
            //}

            //邀请入群消息
            StringBuilder tips = new StringBuilder();
            tips.append("新用户");
            if (addUserName.size() < 3) {
                for (int i = 0; i < addUserName.size(); i++) {
                    tips.append(addUserName.get(i));
                    if (i < addUserName.size() - 1) {
                        tips.append("、");
                    }
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    tips.append(addUserName.get(i));
                    if (i < addUserName.size() - 1) {
                        tips.append("、");
                    } else {
                        tips.append("...等人");
                    }
                }
            }
            tips.append("加入了群组");

            //发送入群通知
            MessageSaveDTO message = new MessageSaveDTO();
            message.setType(MessageType.GROUP_SYS_NOTICE);
            message.setConversationType(ConversationType.GROUP);
            message.setConversationId(group.getGroupId());
            message.setFrom("0");
            message.setTo(group.getGroupId());
            message.setBody(new JSONObject(new HashMap<String, Object>() {{
                put("tips", tips);
            }}));
            message.setExtra("");
            message.setTime(System.currentTimeMillis());
            groupMessageService.insertGroupMessage(LoginUserContext.getUser(), message);
        }
        addUserToGroupResultVO.setSuccessList(successList);
        addUserToGroupResultVO.setFailList(failList);
        addUserToGroupResultVO.setIgnoreList(ignoreList);
        return addUserToGroupResultVO;
    }

    /**
     * 移除群成员
     *
     * @param groupUserAddDTO
     * @throws Exception
     */
    @Override
    public void deleteUserFromGroup(GroupUserAddDTO groupUserAddDTO) throws Exception {

        //判断群组是否存在
        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupUserAddDTO.getGroupId()).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }

        //校验权限
        if (!group.getLeaderUserId().equals(LoginUserContext.getUser().getUserId())) {
            throw new GroupPermissionDeniedException("仅群主可移除群成员");
        }

        if (groupUserAddDTO.getMembers() != null) {

            //遍历需要移出的群成员
            groupUserAddDTO.getMembers().forEach(userId -> {
                //群主不能移除
                if (userId.equals(group.getLeaderUserId())) {
                    return;
                }
                //查询用户是否存在
                User isUserExist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userId));
                if (isUserExist == null || isUserExist.getUserId() == null) {
                    return;
                }
                //查询用户是否是群成员
                GroupUser isGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", groupUserAddDTO.getGroupId()).eq("user_id", userId));
                if (isGroupUser == null) {
                    return;
                }

                MessageSaveDTO message = new MessageSaveDTO();
                message.setType(MessageType.GROUP_SYS_NOTICE);
                message.setConversationType(ConversationType.GROUP);
                message.setConversationId(group.getGroupId());
                message.setFrom("0");
                message.setTo(group.getGroupId());
                message.setBody(new JSONObject(new HashMap<String, Object>() {{
                    put("tips", "你已被踢出了群聊");
                }}));
                message.setExtra("");
                message.setTime(System.currentTimeMillis());
                try {
                    //给指定群成员发送系统消息
                    groupMessageService.insertGroupMessageToOne(LoginUserContext.getUser(), message, isGroupUser.getUserId());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //移除群成员与群的关联
                groupUserMapper.deleteById(isGroupUser.getId());
            });

        }
    }

    @Override
    public void leaveGroup(String groupId) throws Exception {

        //判断群组是否存在
        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }

        //判断是否是群主
        if (group.getLeaderUserId().equals(LoginUserContext.getUser().getUserId())) {
            throw new GroupOnlyDissolveException("群主无法退出自己的群");
        }

        boolean isGroupUser = groupUserMapper.exists(new QueryWrapper<GroupUser>().eq("group_id", groupId).eq("user_id", LoginUserContext.getUser().getUserId()));
        if (!isGroupUser) {
            throw new NoGroupUserException("非群成员，无法执行退出操作");
        }

        MessageSaveDTO message = new MessageSaveDTO();
        message.setType(MessageType.GROUP_SYS_NOTICE);
        message.setConversationType(ConversationType.GROUP);
        message.setConversationId(groupId);
        message.setFrom("0");
        message.setTo(groupId);
        message.setBody(new JSONObject(new HashMap<String, Object>() {{
            put("tips", LoginUserContext.getUser().getNickname() + "退出了群组");
        }}));
        message.setExtra("");
        message.setTime(System.currentTimeMillis());
        groupMessageService.insertGroupMessage(LoginUserContext.getUser(), message);

        groupUserMapper.delete(new QueryWrapper<GroupUser>().eq("group_id", groupId).eq("user_id", LoginUserContext.getUser().getUserId()));

    }

    @Override
    public void setAdminstrator(String groupId, String userId, Integer isAdmin) throws Exception {
        //判断群组是否存在
        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }
        //判断是否是群主
        if (!group.getLeaderUserId().equals(LoginUserContext.getUser().getUserId())) {
            throw new GroupPermissionDeniedException("无权限操作");
        }
        //判断此用户是否在群内，不在群内无法设置为管理员
        //查询用户是否存在，不存在则执行下一个用户
        User isUserExist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userId));
        if (isUserExist == null || isUserExist.getUserId() == null) {
            throw new UserNotFoundException("用户不存在");
        }

        GroupUser groupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", groupId).eq("user_id", userId));
        if (groupUser == null || groupUser.getId() == 0) {
            throw new NoGroupUserException("此用户不在当前群组，无法设置为管理员");
        }

        //相同结果不再设置
        if (groupUser.getIsAdmin().equals(isAdmin)){
            return;
        }

        String tips = "";
        GroupUser update = new GroupUser();
        if (isAdmin == 1) {
            update.setIsAdmin(1);
            tips = "群主已设置" + isUserExist.getNickname() + "为当前群组管理员";
        } else {
            update.setIsAdmin(0);
            tips = "群主已撤销" + isUserExist.getNickname() + "的管理员权限";
        }
        groupUserMapper.update(update, new QueryWrapper<GroupUser>().eq("group_id", groupId).eq("user_id", userId));

        //发送设置管理员提示消息
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

    @Override
    public void setMute(String groupId, String userId, Integer time) throws Exception {
        //判断群组是否存在
        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }

        //查询用户是否存在
        User isUserExist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userId));
        if (isUserExist == null || isUserExist.getUserId() == null) {
            throw new UserNotFoundException("当前禁言用户不存在");
        }

        GroupUser setGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", groupId).eq("user_id", userId));
        if (setGroupUser == null) {
            throw new NoGroupUserException("当前禁言用户不在群内，无法操作");
        }

        boolean isAllowHandle = false;
        if (group.getLeaderUserId().equals(LoginUserContext.getUser().getUserId())) {
            //群主直接操作
            isAllowHandle = true;
        } else {
            GroupUser operatorGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", groupId).eq("user_id", LoginUserContext.getUser().getUserId()));
            //判断是不是管理员
            if (operatorGroupUser != null && operatorGroupUser.getIsAdmin() == 1) {
                isAllowHandle = true;
            }
        }

        if (!isAllowHandle) {
            throw new GroupPermissionDeniedException("无权限操作");
        }
        long muteEndTime = 0L;
        long nowTime = System.currentTimeMillis();
        //禁言
        if (time > 0) {
            muteEndTime = nowTime + time * 60 * 1000;
        }
        GroupUser update = new GroupUser();
        update.setMuteEndTime(muteEndTime);
        groupUserMapper.update(update, new QueryWrapper<GroupUser>().eq("id", setGroupUser.getId()));
        MessageSaveDTO message = new MessageSaveDTO();
        message.setType(MessageType.GROUP_SYS_NOTICE);
        message.setConversationType(ConversationType.GROUP);
        message.setConversationId(group.getGroupId());
        message.setFrom("0");
        message.setTo(group.getGroupId());
        message.setExtra("");
        message.setTime(System.currentTimeMillis());

        if (muteEndTime > 0) {
            message.setBody(new JSONObject(new HashMap<String, Object>() {{
                put("tips", LoginUserContext.getUser().getNickname() + "禁言" + isUserExist.getNickname() + time + "分钟");
            }}));
            groupMessageService.insertGroupMessage(LoginUserContext.getUser(), message);
        } else if (muteEndTime == 0 && setGroupUser.getMuteEndTime() > nowTime) {
            message.setBody(new JSONObject(new HashMap<String, Object>() {{
                put("tips", LoginUserContext.getUser().getNickname() + "解除了" + isUserExist.getNickname() + "的禁言");
            }}));
            groupMessageService.insertGroupMessage(LoginUserContext.getUser(), message);
        }

    }

    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    @Override
    public GroupApply applyHandle(Integer ApplyId, Integer status) throws Exception {

        GroupApply groupApply = groupApplyMapper.selectOne(new QueryWrapper<GroupApply>().eq("id", ApplyId));
        if (groupApply == null) {
            throw new IdException("申请ID错误");
        }

        if (!groupApply.getStatus().equals(GroupApplyStatus.PENDING)) {
            //说明已被其他管理员处理了
            return groupApplyMapper.getApply(ApplyId);
        }

        if (status.equals(GroupApplyStatus.PENDING)) {
            throw new Exception("请设置处理状态");
        }

        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupApply.getGroupId()).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }

        GroupUser operatorGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", group.getGroupId()).eq("user_id", LoginUserContext.getUser().getUserId()));
        if (operatorGroupUser == null || operatorGroupUser.getUserId() == null) {
            throw new NoGroupUserException("非群成员无法执行此操作");
        }

        //查询用户是否存在，不存在则执行下一个用户
        User isUserExist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", groupApply.getUserId()));
        if (isUserExist == null || isUserExist.getUserId() == null) {
            throw new UserNotFoundException("申请人用户不存在");
        }

        GroupUser isGroupUserExist = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", group.getGroupId()).eq("user_id", groupApply.getUserId()));
        GroupApply result = new GroupApply();
        if (isGroupUserExist != null && isGroupUserExist.getUserId() != null) {
            //已在群内，只更新申请，其他不做处理
            GroupApply apply = new GroupApply();
            apply.setStatus(GroupApplyStatus.AGREE);
            apply.setAdminId(LoginUserContext.getUser().getUserId());
            apply.setTransformTime(System.currentTimeMillis());
            groupApplyMapper.update(apply, new QueryWrapper<GroupApply>().eq("id", ApplyId));
            result = groupApplyMapper.getApply(ApplyId);
        } else {
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

            //开始处理
            GroupApply apply = new GroupApply();
            apply.setStatus(status);
            apply.setAdminId(LoginUserContext.getUser().getUserId());
            apply.setTransformTime(System.currentTimeMillis());
            groupApplyMapper.update(apply, new QueryWrapper<GroupApply>().eq("id", ApplyId));

            if (status.equals(GroupApplyStatus.AGREE)) {
                //拉人入群
                //群成员关联群组
                GroupUser groupUser = new GroupUser();
                groupUser.setGroupId(group.getGroupId());
                groupUser.setUserId(groupApply.getUserId());
                groupUser.setJoinAt(System.currentTimeMillis());
                groupUser.setCreatedAt(System.currentTimeMillis());
                groupUserMapper.insert(groupUser);
                //会话
                Conversation conversation = conversationService.getOne(new QueryWrapper<Conversation>().eq("user_id", groupApply.getUserId()).eq("conversation_id", group.getGroupId()));
                if(conversation == null){
                    conversation = new Conversation();
                    conversation.setUnread(0);
                    conversation.setConversationId(group.getGroupId());
                    conversation.setType(ConversationType.GROUP);
                    conversation.setUserId(groupApply.getUserId());
                    conversation.setLastMessageId("");
                    conversation.setUpdatedAt(System.currentTimeMillis());
                    conversation.setCreatedAt(System.currentTimeMillis());
                }else{
                    conversation.setUpdatedAt(System.currentTimeMillis());
                }
                conversationService.saveOrUpdate(conversation);

                result = groupApplyMapper.getApply(ApplyId);
                //发送邀请入群消息
                String tips = isUserExist.getNickname() + "加入了群组";
                MessageSaveDTO message = new MessageSaveDTO();
                message.setType(MessageType.GROUP_SYS_NOTICE);
                message.setConversationType(ConversationType.GROUP);
                message.setConversationId(group.getGroupId());
                message.setFrom("0");
                message.setTo(group.getGroupId());
                message.setBody(new JSONObject(new HashMap<String, Object>() {{
                    put("tips", tips);
                }}));
                message.setExtra("");
                message.setTime(System.currentTimeMillis());
                groupMessageService.insertGroupMessage(LoginUserContext.getUser(), message);
            }
        }
        return result;
    }

    @Override
    public List<GroupUser> getGroupUserList(String groupId) throws Exception {

        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }

        GroupUser operatorGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", group.getGroupId()).eq("user_id", LoginUserContext.getUser().getUserId()));
        if (operatorGroupUser == null || operatorGroupUser.getUserId() == null) {
            throw new NoGroupUserException("非群成员无法执行此操作");
        }

        return groupUserMapper.getGroupUserList(groupId);
    }

    /**
     * 给在线群管理员推送群组申请
     *
     * @param groupId
     */
    private void emitJSSDKGroupUserApply(String groupId, List<GroupApply> groupApplies) {
        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));
        if (group != null) {
            //通知群主
            onlineChannel.send(group.getLeaderUserId(), Result.info(StatusCode.GROUP_APPLY_RECEIVE.getCode(), StatusCode.GROUP_APPLY_RECEIVE.getDesc(), groupApplies).toJSONString());
            List<GroupUser> groupAdminList = groupUserMapper.selectList(new QueryWrapper<GroupUser>().eq("group_id", groupId).eq("is_admin", 1));
            groupAdminList.forEach(groupUser -> {
                onlineChannel.send(groupUser.getUserId(), Result.info(StatusCode.GROUP_APPLY_RECEIVE.getCode(), StatusCode.GROUP_APPLY_RECEIVE.getDesc(), groupApplies).toJSONString());
            });
        }
    }

}




