package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.constant.ConversationType;
import cn.wzjun1.yeimServer.constant.MessageType;
import cn.wzjun1.yeimServer.constant.StatusCode;
import cn.wzjun1.yeimServer.domain.*;
import cn.wzjun1.yeimServer.mapper.ConversationMapper;
import cn.wzjun1.yeimServer.mapper.GroupMapper;
import cn.wzjun1.yeimServer.mapper.GroupUserMapper;
import cn.wzjun1.yeimServer.mapper.UserMapper;
import cn.wzjun1.yeimServer.pojo.YeIMPushConfig;
import cn.wzjun1.yeimServer.result.Result;
import cn.wzjun1.yeimServer.service.AsyncService;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.service.PushService;
import cn.wzjun1.yeimServer.socket.WebSocket;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wzjun1
 */
@Service
@Slf4j
public class AsyncServiceImpl implements AsyncService {

    @Autowired
    YeIMPushConfig yeIMPushConfig;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    ConversationService conversationService;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    GroupUserMapper groupUserMapper;

    @Autowired
    PushService pushService;

    /**
     * 单聊
     * 给接收方推送socket message
     *
     * @param message
     */
    @Async
    public void emitJSSDKMessageReceive(Message message) {
        //消息是发送方的参照，会话ID是接收方，这里切换成发送方的。
        message.setConversationId(message.getFrom());
        int result = WebSocket.sendMessage(message.getTo(), Result.info(StatusCode.MESSAGE_RECEIVE.getCode(), "", message).toJSONString());

        //第三方通知消息推送。在线透传，离线通知
        try{
            if (yeIMPushConfig.isEnable()) {
                String pushToId = message.getTo();
                User user = userMapper.findByUserId(pushToId);
                if (user != null && user.getMobileDeviceId() != null) {
                    String pushTitle = message.getFromUserInfo().getNickname();
                    String pushContent = message.getBody().toString();
                    JSONObject body = JSONObject.parseObject(message.getBody().toString());
                    if (message.getType().equals(MessageType.TEXT)){
                        pushContent = body.getString("text");
                    }else{
                        pushContent = "[其他类型消息]";
                    }
                    pushService.pushSingleByDeviceId(user.getMobileDeviceId(), pushTitle, pushContent);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 异步
     * 发送群组会话更新，群新消息通知事件
     *
     * @param groupId 群ID
     * @param message 群消息
     */
    @Async
    public void updateGroupConversationSendEvent(String groupId, GroupMessage message) {

        //TODO 后面看情况用队列改一下

        long time = System.currentTimeMillis();

        //查出群组全部用户的会话
        List<Conversation> conversations = conversationMapper.selectList(new QueryWrapper<Conversation>().eq("conversation_id ", groupId).eq("type", "group"));

        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));

        //查出群组内所有的用户，并更新群成员的会话。
//        List<GroupUser> groupUsers = groupUserMapper.selectList(new QueryWrapper<GroupUser>().eq("group_id", groupId));
        List<GroupUser> groupUsers = groupUserMapper.getGroupUserList(groupId);
        groupUsers.forEach(groupUser -> {
            try {
                int exist = -1;
                for (int i = 0; i < conversations.size(); i++) {
                    if (groupUser.getUserId().equals(conversations.get(i).getUserId())) {
                        //此用户已有会话
                        exist = i;
                    }
                }
                //会话存在则更新
                if (exist != -1) {
                    Conversation conversation = conversations.get(exist);
                    conversation.setLastMessageId(message.getMessageId());
                    conversation.setUpdatedAt(time);
                    if (message.getFrom().equals(groupUser.getUserId())) {
                        conversation.setUnread(conversation.getUnread());
                    } else {
                        conversation.setUnread(conversation.getUnread() + 1);
                    }
                    conversationMapper.updateGroupConversation(message.getMessageId(), time, groupId, groupUser.getUserId());
                } else {
                    //会话不存在就新增
                    Conversation conversation = new Conversation();
                    conversation.setConversationId(groupId);
                    conversation.setType(ConversationType.GROUP);
                    conversation.setUserId(groupUser.getUserId());
                    conversation.setUnread(1);
                    if (message.getFrom().equals(groupUser.getUserId())) {
                        conversation.setUnread(0);
                    } else {
                        conversation.setUnread(1);
                    }
                    conversation.setLastMessageId(message.getMessageId());
                    if (conversation.getCreatedAt() == null || conversation.getCreatedAt() == 0) {
                        conversation.setCreatedAt(System.currentTimeMillis());
                    } else {
                        conversation.setUpdatedAt(System.currentTimeMillis());
                    }
                    conversationMapper.insert(conversation);
                }
                //socket转发会话更新事件，非群成员不发送（已删除的成员，会话仍在，消息不更新）
                WebSocket.sendMessage(groupUser.getUserId(), Result.info(StatusCode.CONVERSATION_CHANGED.getCode(), StatusCode.CONVERSATION_CHANGED.getDesc(), conversationService.getConversation(groupUser.getGroupId(), groupUser.getUserId())).toJSONString());

                //给群成员发送在线消息（发送者除外）
                if (!message.getFrom().equals(groupUser.getUserId())) {
                    WebSocket.sendMessage(groupUser.getUserId(), Result.info(StatusCode.MESSAGE_RECEIVE.getCode(), "", message).toJSONString());
                    //第三方通知消息推送。在线透传，离线通知
                    try {
                        if (yeIMPushConfig.isEnable()) {
                            if (groupUser.getUserInfo().getMobileDeviceId() != null) {
                                String pushTitle = group.getName();
                                String pushContent = "";
                                JSONObject body = JSONObject.parseObject(message.getBody().toString());
                                if (message.getType().equals(MessageType.TEXT)) {
                                    pushContent = message.getFromUserInfo().getNickname() + ": " + body.getString("text");
                                } else {
                                    pushContent = message.getFromUserInfo().getNickname() + ": [其他类型消息]";
                                }
                                pushService.pushSingleByDeviceId(groupUser.getUserInfo().getMobileDeviceId(), pushTitle, pushContent);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        });
    }

    /**
     * 异步
     * 发送私聊消息撤回事件
     *
     * @param message 群消息
     */
    @Async
    public void messageRevokedSendEvent(Message message) {
        //消息撤回事件
        WebSocket.sendMessage(message.getTo(), Result.info(StatusCode.MESSAGE_REVOKED.getCode(), StatusCode.MESSAGE_REVOKED.getDesc(), message).toJSONString());
        //如果会话最新消息是撤回的消息，则会话更新
        ConversationV0 conversation = conversationService.getConversation(message.getFrom(), message.getTo());
        if (conversation != null && conversation.getLastMessage().getMessageId().equals(message.getMessageId())) {
            WebSocket.sendMessage(message.getTo(), Result.info(StatusCode.CONVERSATION_CHANGED.getCode(), StatusCode.CONVERSATION_CHANGED.getDesc(), conversation).toJSONString());
        }
        //第三方通知消息推送。在线透传，离线通知
        try {
            if (yeIMPushConfig.isEnable()) {
                User user = userMapper.findByUserId(message.getTo());
                if (user.getMobileDeviceId() != null) {
                    String pushTitle = user.getNickname();
                    String pushContent = "[对方撤回了一条消息]";
                    pushService.pushSingleByDeviceId(user.getMobileDeviceId(), pushTitle, pushContent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步
     * 发送群组消息撤回事件
     *
     * @param groupId 群ID
     * @param message 群消息
     */
    @Async
    public void groupMessageRevokedSendEvent(String groupId, GroupMessage message) {

        //TODO 后面看情况用队列改一下

        long time = System.currentTimeMillis();

        //查出群组全部用户的会话
        List<Conversation> conversations = conversationMapper.selectList(new QueryWrapper<Conversation>().eq("conversation_id ", groupId).eq("type", "group"));

        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));

        //查出群组内所有的用户，并更新群成员的会话。
        List<GroupUser> groupUsers = groupUserMapper.getGroupUserList(groupId);
        groupUsers.forEach(groupUser -> {
            try {
                int exist = -1;
                for (int i = 0; i < conversations.size(); i++) {
                    if (groupUser.getUserId().equals(conversations.get(i).getUserId())) {
                        //此用户已有会话
                        exist = i;
                    }
                }

                //如果会话存在，并且会话最新消息是当前撤回的消息，则更新会话
                if (exist != -1) {
                    Conversation conversation = conversations.get(exist);
                    if (conversation.getLastMessageId().equals(message.getMessageId())){
                        conversation.setLastMessageId(message.getMessageId());
                        conversation.setUpdatedAt(time);
                        if (message.getFrom().equals(groupUser.getUserId())) {
                            conversation.setUnread(conversation.getUnread());
                        } else {
                            conversation.setUnread(conversation.getUnread() + 1);
                        }
                        conversationMapper.updateGroupConversation(message.getMessageId(), time, groupId, groupUser.getUserId());
                        //socket转发会话更新事件，非群成员不发送（已删除的成员，会话仍在，消息不更新）
                        WebSocket.sendMessage(groupUser.getUserId(), Result.info(StatusCode.CONVERSATION_CHANGED.getCode(), StatusCode.CONVERSATION_CHANGED.getDesc(), conversationService.getConversation(groupUser.getGroupId(), groupUser.getUserId())).toJSONString());
                    }
                }
                //给群成员发送撤回事件
                if (!message.getFrom().equals(groupUser.getUserId())) {
                    WebSocket.sendMessage(groupUser.getUserId(), Result.info(StatusCode.MESSAGE_REVOKED.getCode(), StatusCode.MESSAGE_REVOKED.getDesc(), message).toJSONString());
                    //第三方通知消息推送。在线透传，离线通知
                    try {
                        if (yeIMPushConfig.isEnable()) {
                            if (groupUser.getUserInfo().getMobileDeviceId() != null) {
                                String pushTitle = group.getName();
                                String pushContent = "";
                                pushContent = message.getFromUserInfo().getNickname() + ": [撤回了一条消息]";
                                pushService.pushSingleByDeviceId(groupUser.getUserInfo().getMobileDeviceId(), pushTitle, pushContent);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        });
    }


}




