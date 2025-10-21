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
import cn.wzjun1.yeimServer.service.OnlineChannel;
import cn.wzjun1.yeimServer.service.PushService;
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

    @Autowired
    OnlineChannel onlineChannel;

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
        onlineChannel.send(message.getTo(), Result.info(StatusCode.MESSAGE_RECEIVE.getCode(), "", message).toJSONString());

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

        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));

        //查出群组内所有的用户，并更新群成员的会话。
        List<GroupUser> groupUsers = groupUserMapper.getGroupUserList(groupId);
        groupUsers.forEach(groupUser -> {
            try {
                //更新群成员会话，并发送会话更新事件（已删除的成会话仍在，但非群成员则会话和消息均不更新）
                int unread = 0;
                if (!message.getFrom().equals(groupUser.getUserId())) {
                    unread = 1;
                }
                conversationService.updateConversation(groupUser.getUserId(), groupId, ConversationType.GROUP, message.getMessageId(), unread, true);

                //给群成员发送在线消息（发送者除外）
                if (!message.getFrom().equals(groupUser.getUserId())) {
                    onlineChannel.send(groupUser.getUserId(), Result.info(StatusCode.MESSAGE_RECEIVE.getCode(), "", message).toJSONString());
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
     * @param outMessage 发送者端
     * @param inMessage 接收者端
     */
    @Async
    public void messageRevokedSendEvent(Message outMessage, Message inMessage) {

        //给自己发送会话更新事件
        ConversationV0 conversationV1 = conversationService.getConversation(outMessage.getTo(), outMessage.getFrom());
        if (conversationV1 != null && conversationV1.getLastMessage().getMessageId().equals(outMessage.getMessageId())) {
            onlineChannel.send(outMessage.getFrom(), Result.info(StatusCode.CONVERSATION_CHANGED.getCode(), StatusCode.CONVERSATION_CHANGED.getDesc(), conversationV1).toJSONString());
        }

        //给对方发送消息撤回事件
        onlineChannel.send(inMessage.getTo(), Result.info(StatusCode.MESSAGE_REVOKED.getCode(), StatusCode.MESSAGE_REVOKED.getDesc(), inMessage).toJSONString());
        //如果会话最新消息是撤回的消息，则会话更新
        ConversationV0 conversationV2 = conversationService.getConversation(inMessage.getFrom(), inMessage.getTo());
        if (conversationV2 != null && conversationV2.getLastMessage().getMessageId().equals(inMessage.getMessageId())) {
            onlineChannel.send(inMessage.getTo(), Result.info(StatusCode.CONVERSATION_CHANGED.getCode(), StatusCode.CONVERSATION_CHANGED.getDesc(), conversationV2).toJSONString());
        }

        //第三方通知消息推送。在线透传，离线通知
        try {
            if (yeIMPushConfig.isEnable()) {
                User user = userMapper.findByUserId(inMessage.getTo());
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

        long time = System.currentTimeMillis();

        //查出群组全部用户的会话
        List<Conversation> conversations = conversationMapper.selectList(new QueryWrapper<Conversation>().eq("conversation_id ", groupId).eq("type", "group"));

        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", groupId).eq("is_dissolve", 0));

        //查出群组内所有的用户，并更新群成员的会话。
        List<GroupUser> groupUsers = groupUserMapper.getGroupUserList(groupId);
        groupUsers.forEach(groupUser -> {
            try {
                //更新群成员会话，并发送会话更新事件（已删除的成会话仍在，但非群成员则会话和消息均不更新）
                conversationService.updateConversation(groupUser.getUserId(), groupId, ConversationType.GROUP, message.getMessageId(), 0, true);

                //给群成员发送撤回事件
                if (!message.getFrom().equals(groupUser.getUserId())) {
                    onlineChannel.send(groupUser.getUserId(), Result.info(StatusCode.MESSAGE_REVOKED.getCode(), StatusCode.MESSAGE_REVOKED.getDesc(), message).toJSONString());
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




