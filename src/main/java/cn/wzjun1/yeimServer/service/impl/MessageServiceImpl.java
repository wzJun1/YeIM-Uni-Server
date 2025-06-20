package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.*;
import cn.wzjun1.yeimServer.exception.friend.FriendNotFoundException;
import cn.wzjun1.yeimServer.exception.message.IdException;
import cn.wzjun1.yeimServer.exception.message.MessageRejectedException;
import cn.wzjun1.yeimServer.exception.message.ToUserIdNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.*;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import cn.wzjun1.yeimServer.pojo.YeIMPushConfig;
import cn.wzjun1.yeimServer.service.*;
import cn.wzjun1.yeimServer.socket.WebSocket;
import cn.wzjun1.yeimServer.constant.MessageStatus;
import cn.wzjun1.yeimServer.constant.StatusCode;
import cn.wzjun1.yeimServer.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yitter.idgen.YitIdHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wzjun1
 * @description 针对表【message】的数据库操作Service实现
 * @createDate 2022-11-16 23:29:12
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    @Value("${yeim.friend.enable}")
    boolean friendCheck;

    @Autowired
    UserMapper userMapper;

    @Autowired
    MessageMapper messageMapper;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    GroupUserMapper groupUserMapper;

    @Autowired
    GroupMessageMapper groupMessageMapper;

    @Autowired
    GroupMessageService groupMessageService;

    @Autowired
    ConversationService conversationService;

    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    UserBlackListMapper userBlackListMapper;

    @Autowired
    FriendMapper friendMapper;

    @Autowired
    YeIMPushConfig yeIMPushConfig;

    @Autowired
    PushService pushService;

    @Autowired
    AsyncService asyncService;

    @Autowired
    OnlineChannel onlineChannel;

    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    @Override
    public Message insertMessage(User user, MessageSaveDTO message) throws Exception {
        try {

            //判断接收者是否存在
            User toUser = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", message.getTo()));
            if (toUser == null || toUser.getUserId() == null) {
                throw new ToUserIdNotFoundException("接收者ID错误");
            }

            //判断发送方是否在接收方的黑名单中
            boolean isBlack = userBlackListMapper.exists(new QueryWrapper<UserBlackList>().eq("cover_user_id", message.getFrom()).eq("user_id", message.getTo()));
            if (isBlack) {
                throw new MessageRejectedException("您已被当前用户拉黑，无法向他发送消息");
            }

            //检查好友关系
            if (friendCheck) {
                boolean rela1 = friendMapper.exists(new QueryWrapper<Friend>().eq("user_id", message.getFrom()).eq("friend_user_id", message.getTo()));
                boolean rela2 = friendMapper.exists(new QueryWrapper<Friend>().eq("friend_user_id", message.getFrom()).eq("user_id", message.getTo()));
                if (!rela1 || !rela2) {
                    throw new FriendNotFoundException("非好友关系，无法发送消息");
                }
            }

            //1.插入消息到数据库

            //消息入库数据
            long time = System.currentTimeMillis();
            //统一消息ID
            String messageId = YitIdHelper.nextId() + "-" + System.currentTimeMillis();
            //outMessageId = messageId + "-1"; 消息输出（发送）
            //inMessageId = messageId + "-2"; 消息流入（接收）
            String outMessageId = messageId + "-1";
            String inMessageId = messageId + "-2";

            //私聊消息将会给发送方和接收方都存一条消息。
            //代表着发送方的会话和接收方的会话里都有一条消息。
            //1.1 消息插入，属发送者会话
            Message out = new Message();
            out.setMessageId(outMessageId);
            out.setUserId(message.getFrom());
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
            Message outResultMessage = messageMapper.getMessageById(outMessageId, user.getUserId());

            //1.2 消息插入，属接收者会话
            Message in = new Message();
            in.setMessageId(inMessageId);
            in.setUserId(message.getTo());
            in.setConversationId(message.getFrom());
            in.setDirection("in");
            in.setType(message.getType());
            in.setFrom(message.getFrom());
            in.setTo(message.getTo());
            in.setIsRead(0);
            in.setIsRevoke(0);
            in.setStatus(MessageStatus.SUCCESS);
            in.setTime(time);
            in.setBody(message.getBody());
            in.setExtra(message.getExtra());
            this.save(in);
            Message inResultMessage = messageMapper.getMessageById(inMessageId, message.getTo());
            //2.更新多端会话

            //2.1 更新发送者会话,并推送给发送者会话更新时间
            boolean conversation1Result = conversationService.updateConversation(user.getUserId(), message.getTo(), message.getConversationType(), outMessageId, 0, true);

            //2.2 更新接收者会话
            boolean conversation2Result = conversationService.updateConversation(message.getTo(), message.getFrom(), message.getConversationType(), inMessageId, 1, true);

            if (messageResult && conversation1Result && conversation2Result) {
                //给接收方推送在线消息[Socket Message]
                asyncService.emitJSSDKMessageReceive(inResultMessage);
                //返回消息
                return outResultMessage;
            } else {
                throw new Exception("insertMessage error");
            }
        } catch (Exception e) {
            if (e instanceof ToUserIdNotFoundException) {
                throw new ToUserIdNotFoundException(e.getMessage());
            } else if (e instanceof MessageRejectedException) {
                throw new MessageRejectedException(e.getMessage());
            } else if (e instanceof FriendNotFoundException){
                throw new FriendNotFoundException(e.getMessage());
            }
            else {
                throw new Exception(e.getMessage());
            }
        }
    }


    /**
     * 私聊消息更新应更新两条，发送消息 、接收消息（发件箱、收件箱）
     * 私聊消息ID组成：分布式唯一ID-毫秒级时间戳-发送端1（接收端2）
     *
     * @param update
     * @param messageId
     * @return
     * @Deprecated
     */
    @Deprecated
    @Override
    public void updatePrivateMessageById(Message update, String userId, String messageId) throws Exception {
        Message exist = messageMapper.getMessageById(messageId, userId);
        if (exist == null) {
            throw new Exception("messageId 错误");
        }
        String prefix = messageId.substring(0, messageId.length() - 1);
        //更新发送者消息
        this.update(update, new QueryWrapper<Message>().eq("message_id", prefix + "1"));
        //更新接收者消息
        this.update(update, new QueryWrapper<Message>().eq("message_id", prefix + "2"));
    }

    /**
     * 根据消息ID删除消息
     *
     * @param userId
     * @param messageId
     * @return
     */
    @Override
    public void deleteMessage(String userId, String messageId) throws Exception {
        Message privateMessage = messageMapper.getMessageById(messageId, userId);
        if (privateMessage != null) {
            Message update = new Message();
            update.setIsDeleted(1);
            String prefix = messageId.substring(0, messageId.length() - 1);
            if (privateMessage.getFrom().equals(userId)) {
                //被删除的消息发送者是当前登录用户
                //更新发送者消息
                this.update(update, new QueryWrapper<Message>().eq("message_id", prefix + "1"));
            } else if (privateMessage.getTo().equals(userId)) {
                //被删除的消息接收者是当前登录用户
                //更新接收者消息
                this.update(update, new QueryWrapper<Message>().eq("message_id", prefix + "2"));
            }
        } else {
            GroupMessage groupMessage = groupMessageMapper.selectOne(new QueryWrapper<GroupMessage>().eq("message_id", messageId).eq("`from`", userId));
            if (groupMessage != null) {
                groupMessageMapper.deleteGroupMessage(messageId, groupMessage.getConversationId(), userId, System.currentTimeMillis());
            }
        }
    }

    /**
     * 撤回消息
     * <p>
     * 私聊消息更新应更新两条，发送消息 、接收消息（发件箱、收件箱）
     * 私聊消息ID组成：分布式唯一ID-毫秒级时间戳-发送端1（接收端2）
     * <p>
     * 群里消息更新仅需更新一条即可
     *
     * @param userId
     * @param messageId
     * @return
     */
    @Override
    public void revokeMessage(String userId, String messageId) throws Exception {
        boolean isPrivateMessage = messageMapper.exists(new QueryWrapper<Message>().eq("message_id", messageId));
        if (isPrivateMessage) {
            Message update = new Message();
            update.setIsRevoke(1);
            String prefix = messageId.substring(0, messageId.length() - 1);
            //更新接收者消息
            this.update(update, new QueryWrapper<Message>().eq("message_id", prefix + "1"));
            //更新发送者消息
            this.update(update, new QueryWrapper<Message>().eq("message_id", prefix + "2"));
            //发送事件
            emitJSSDKPrivateMessageRevoked(messageMapper.getMessageById(messageId, userId));
        } else {
            boolean isGroupMessage = groupMessageMapper.exists(new QueryWrapper<GroupMessage>().eq("message_id", messageId).eq("`from`", userId));
            if (isGroupMessage) {
                GroupMessage update = new GroupMessage();
                update.setIsRevoke(1);
                //更新群消息
                groupMessageMapper.update(update, new QueryWrapper<GroupMessage>().eq("message_id", messageId).eq("`from`", userId));
                emitJSSDKGroupMessageRevoked(groupMessageMapper.selectOne(new QueryWrapper<GroupMessage>().eq("message_id", messageId).eq("`from`", userId)));
            } else {
                throw new IdException("messageId 错误");
            }
        }
    }

    /**
     * 分页获取历史消息记录
     *
     * @param page           页码
     * @param userId         当前用户ID
     * @param conversationId 会话ID
     * @return IPage<Message>
     */
    @Override
    public IPage<Message> listMessage(IPage<Message> page, String userId, String conversationId) {
        return messageMapper.listMessage(page, userId, conversationId);
    }

    /**
     * 根据nextMesssageId获取下一批历史消息记录
     *
     * @param conversationId 会话ID
     * @param nextMessageId  最后一条消息的ID，用以获取下一批消息
     * @param limit          获取数量
     * @return List<Message>
     */
    @Override
    public List<Message> listMessage(String conversationId, String nextMessageId, Integer limit) {
        return messageMapper.listMessageByNextMessageId(LoginUserContext.getUser().getUserId(), conversationId, nextMessageId, limit);
    }

    /**
     * 私聊消息撤回事件
     *
     * @param message
     */
    private void emitJSSDKPrivateMessageRevoked(Message message) {
        asyncService.messageRevokedSendEvent(message);
    }

    /**
     * 群聊消息撤回事件，推送给对方
     *
     * @param message
     */
    private void emitJSSDKGroupMessageRevoked(GroupMessage message) {
        asyncService.groupMessageRevokedSendEvent(message.getConversationId(), message);
    }

}




