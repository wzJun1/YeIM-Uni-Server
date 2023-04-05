package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.constant.ConversationType;
import cn.wzjun1.yeimServer.constant.StatusCode;
import cn.wzjun1.yeimServer.domain.*;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import cn.wzjun1.yeimServer.exception.group.GroupAllMuteException;
import cn.wzjun1.yeimServer.exception.group.GroupMuteException;
import cn.wzjun1.yeimServer.exception.group.GroupNotFoundException;
import cn.wzjun1.yeimServer.exception.group.NoGroupUserException;
import cn.wzjun1.yeimServer.exception.message.MessageRejectedException;
import cn.wzjun1.yeimServer.exception.message.ToUserIdNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.ConversationMapper;
import cn.wzjun1.yeimServer.mapper.GroupMapper;
import cn.wzjun1.yeimServer.mapper.GroupUserMapper;
import cn.wzjun1.yeimServer.result.Result;
import cn.wzjun1.yeimServer.service.AsyncService;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.constant.MessageStatus;
import cn.wzjun1.yeimServer.service.OnlineChannel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.wzjun1.yeimServer.service.GroupMessageService;
import cn.wzjun1.yeimServer.mapper.GroupMessageMapper;
import com.github.yitter.idgen.YitIdHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【group_message】的数据库操作Service实现
 * @createDate 2022-12-08 21:18:05
 */
@Service
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageMapper, GroupMessage>
        implements GroupMessageService {

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    GroupUserMapper groupUserMapper;

    @Autowired
    GroupMessageMapper groupMessageMapper;

    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    ConversationService conversationService;

    @Autowired
    OnlineChannel onlineChannel;

    @Autowired
    AsyncService asyncService;

    /**
     * 发送群消息
     *
     * @param user    当前操作者
     * @param message 消息
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    @Override
    public GroupMessage insertGroupMessage(User user, MessageSaveDTO message) throws Exception {
        try {

            //判断群组是否存在
            Group isExistGroup = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", message.getTo()).eq("is_dissolve", 0));
            if (isExistGroup == null || isExistGroup.getGroupId() == null) {
                throw new GroupNotFoundException("当前群组不存在或已解散");
            }

            boolean isAdmin = false;
            if (isExistGroup.getLeaderUserId().equals(user.getUserId())) {
                isAdmin = true;
            } else {
                GroupUser operatorGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", isExistGroup.getGroupId()).eq("user_id", user.getUserId()));
                if (operatorGroupUser != null && operatorGroupUser.getIsAdmin() == 1) {
                    isAdmin = true;
                }
            }

            //判断是否有发言权限(群主、管理员禁言状态下也可以发言)
            if (isExistGroup.getIsMute().equals(1) && !isAdmin) {
                throw new GroupAllMuteException("当前群组全体禁言");
            }

            GroupUser isGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", message.getTo()).eq("user_id", user.getUserId()));
            if (isGroupUser == null || isGroupUser.getUserId() == null) {
                throw new NoGroupUserException("非群成员无法给当前群组发送消息");
            }

            if (isGroupUser.getMuteEndTime() > System.currentTimeMillis() && !isAdmin) {
                throw new GroupMuteException("您在当前群组内已被禁言");
            }

            //1.插入消息到数据库

            //消息入库数据
            long time = System.currentTimeMillis();
            //统一消息ID
            String messageId = YitIdHelper.nextId() + "-" + time;
            //群消息的direction只有out

            //1.1 保存消息到数据库
            GroupMessage out = new GroupMessage();
            out.setMessageId(messageId);
            out.setUserId("");
            out.setConversationId(message.getTo());
            out.setDirection("out");
            out.setType(message.getType());
            out.setFrom(message.getFrom());
            out.setTo(message.getTo());
            out.setIsRead(0);
            out.setIsRevoke(0);
            out.setStatus(MessageStatus.SUCCESS);
            out.setTime(time);
            out.setBody(message.getBody());
            out.setExtra(message.getExtra());
            boolean messageResult = this.save(out);
            GroupMessage outResultMessage = groupMessageMapper.getMessageById(messageId, message.getTo());

            if (messageResult) {
                //2.更新多端会话
                ///异步批量更新群用户会话 + 转发事件
                asyncService.updateGroupConversationSendEvent(message.getTo(), outResultMessage);
                return outResultMessage;
            } else {
                throw new Exception("insertMessage error");
            }
        } catch (Exception e) {
            if (e instanceof GroupAllMuteException) {
                throw new GroupAllMuteException(e.getMessage());
            } else if (e instanceof GroupMuteException) {
                throw new GroupMuteException(e.getMessage());
            } else if (e instanceof NoGroupUserException) {
                throw new NoGroupUserException(e.getMessage());
            } else {
                throw new Exception(e.getMessage());
            }
        }
    }

    /**
     * 系统消息
     * <p>
     * 发送群消息给指定接收者
     *
     * @param user          当前操作者
     * @param message       消息
     * @param receiveUserId 指定接收者用户ID
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    @Override
    public GroupMessage insertGroupMessageToOne(User user, MessageSaveDTO message, String receiveUserId) throws Exception {
        try {

            //判断群组是否存在
            Group isExistGroup = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", message.getTo()).eq("is_dissolve", 0));
            if (isExistGroup == null || isExistGroup.getGroupId() == null) {
                throw new GroupNotFoundException("当前群组不存在或已解散");
            }

            GroupUser isGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", message.getTo()).eq("user_id", receiveUserId));
            if (isGroupUser == null || isGroupUser.getUserId() == null) {
                throw new NoGroupUserException("指定接收者非群成员");
            }

            //1.插入消息到数据库

            //消息入库数据
            long time = System.currentTimeMillis();
            //统一消息ID
            String messageId = YitIdHelper.nextId() + "-" + time;
            //群消息的direction只有out

            //1.1 保存消息到数据库
            GroupMessage out = new GroupMessage();
            out.setMessageId(messageId);
            out.setUserId("");
            out.setConversationId(message.getTo());
            out.setDirection("out");
            out.setType(message.getType());
            out.setFrom(message.getFrom());
            out.setTo(receiveUserId);
            out.setIsRead(0);
            out.setIsRevoke(0);
            out.setStatus(MessageStatus.SUCCESS);
            out.setTime(time);
            out.setBody(message.getBody());
            out.setExtra(message.getExtra());
            boolean messageResult = this.save(out);
            GroupMessage outResultMessage = groupMessageMapper.getMessageById(messageId, message.getTo());

            if (messageResult) {
                //更新接收者会话
                conversationService.updateConversation(receiveUserId, message.getTo(), ConversationType.GROUP, outResultMessage.getMessageId(), 0, true);

                //发送消息事件
                onlineChannel.send(receiveUserId, Result.info(StatusCode.MESSAGE_RECEIVE.getCode(), "", outResultMessage).toJSONString());

                return outResultMessage;
            } else {
                throw new Exception("insertMessage error");
            }
        } catch (Exception e) {
            if (e instanceof GroupAllMuteException) {
                throw new GroupAllMuteException(e.getMessage());
            } else if (e instanceof GroupMuteException) {
                throw new GroupMuteException(e.getMessage());
            } else if (e instanceof NoGroupUserException) {
                throw new NoGroupUserException(e.getMessage());
            } else {
                throw new Exception(e.getMessage());
            }
        }
    }


    /**
     * @param page
     * @param conversationId
     * @return
     * @throws Exception
     * @deprecated
     */
    @Override
    public IPage<GroupMessage> listMessage(IPage<GroupMessage> page, String conversationId) throws Exception {

        //判断群组是否存在
        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", conversationId).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }

        //判断是否有权限
        boolean isGroupUser = groupUserMapper.exists(new QueryWrapper<GroupUser>().eq("group_id", conversationId).eq("user_id", LoginUserContext.getUser().getUserId()));
        if (!isGroupUser) {
            throw new NoGroupUserException("非群成员无法获取群聊天记录");
        }

        return groupMessageMapper.listMessage(page, LoginUserContext.getUser().getUserId(), conversationId);
    }

    @Override
    public List<GroupMessage> listMessage(String conversationId, String nextMessageId, Integer limit) {

        //判断群组是否存在
        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", conversationId).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new GroupNotFoundException("当前群组不存在或已解散");
        }

        //判断是否有权限
        boolean isGroupUser = groupUserMapper.exists(new QueryWrapper<GroupUser>().eq("group_id", conversationId).eq("user_id", LoginUserContext.getUser().getUserId()));
        //判断当前用户是否存在群聊的会话，如果存在，则从会话的last_message_id开始取历史记录
        ConversationV0 conversation = conversationService.getConversation(conversationId, LoginUserContext.getUser().getUserId());
        //不是群成员，且没有当前群聊的会话（曾经是群成员）
        if (!isGroupUser && conversation == null) {
            throw new NoGroupUserException("非群成员无法获取群聊天记录");
        } else if (!isGroupUser && nextMessageId.equals("")) {
            //不是群成员，但有当前群聊的会话，仍然允许获取退群之前的聊天记录
            nextMessageId = conversation.getLastMessage().getMessageId();
        }

        return groupMessageMapper.listMessageByNextMessageId(LoginUserContext.getUser().getUserId(), conversationId, nextMessageId, limit);
    }


}




