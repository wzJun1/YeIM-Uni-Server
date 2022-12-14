package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.*;
import cn.wzjun1.yeimServer.mapper.*;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.service.GroupMessageService;
import cn.wzjun1.yeimServer.service.MessageService;
import cn.wzjun1.yeimServer.socket.WebSocket;
import cn.wzjun1.yeimServer.socket.cons.MessageStatus;
import cn.wzjun1.yeimServer.socket.cons.SocketStatusCode;
import cn.wzjun1.yeimServer.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yitter.idgen.YitIdHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wzjun1
 * @description 针对表【message】的数据库操作Service实现
 * @createDate 2022-11-16 23:29:12
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

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

    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    @Override
    public Message insertMessage(User user, MessageSaveDTO message) throws Exception {
        try {

            //判断接收者是否存在
            User toUser = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", message.getTo()));
            if (toUser == null || toUser.getUserId() == null) {
                throw new Exception("接收者ID错误");
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

            //2.1 更新发送者会话
            Conversation conversation1 = conversationService.getOne(new QueryWrapper<Conversation>().eq("user_id", user.getUserId()).eq("conversation_id", message.getTo()));
            if (conversation1 == null) {
                conversation1 = new Conversation();
                conversation1.setUnread(0);
            }
            conversation1.setConversationId(message.getTo());
            conversation1.setType(message.getConversationType());
            conversation1.setUserId(user.getUserId());
            conversation1.setUnread(conversation1.getUnread() + 1);
            conversation1.setLastMessageId(outMessageId);
            if (conversation1.getCreatedAt() == null || conversation1.getCreatedAt() == 0) {
                conversation1.setCreatedAt(System.currentTimeMillis());
            } else {
                conversation1.setUpdatedAt(System.currentTimeMillis());
            }

            boolean conversation1Result = conversationService.saveOrUpdate(conversation1);
            // 2.1.1 推送给发送者会话更新消息
            this.emitJSSDKConversationUpdated(conversation1);

            //2.2 更新接收者会话
            Conversation conversation2 = conversationService.getOne(new QueryWrapper<Conversation>().eq("user_id", message.getTo()).eq("conversation_id", message.getFrom()));
            if (conversation2 == null) {
                conversation2 = new Conversation();
                conversation2.setUnread(0);
            }
            conversation2.setConversationId(message.getFrom());
            conversation2.setType(message.getConversationType());
            conversation2.setUserId(message.getTo());
            conversation2.setUnread(conversation2.getUnread() + 1);
            conversation2.setLastMessageId(inMessageId);
            if (conversation2.getCreatedAt() == null || conversation2.getCreatedAt() == 0) {
                conversation2.setCreatedAt(System.currentTimeMillis());
            } else {
                conversation2.setUpdatedAt(System.currentTimeMillis());
            }
            boolean conversation2Result = conversationService.saveOrUpdate(conversation2);
            // 2.2.1 推送给接收者会话更新消息
            this.emitJSSDKConversationUpdated(conversation2);
            // 2.2.2 推送给接收者socket消息，如果在线的话
            this.emitJSSDKMessageReceive(inResultMessage);

            if (messageResult && conversation1Result && conversation2Result) {
                return outResultMessage;
            } else {
                throw new Exception("insertMessage error");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }





    /**
     * 私聊消息更新应更新两条，发送消息 、接收消息（发件箱、收件箱）
     * 私聊消息ID组成：分布式唯一ID-毫秒级时间戳-发送端1（接收端2）
     *
     * @param update
     * @param messageId
     * @return
     */
    @Override
    public void updatePrivateMessageById(Message update, String userId, String messageId) throws Exception {

        Message exist = messageMapper.getMessageById(messageId, userId);

        if (exist == null) {
            throw new Exception("messageId 错误");
        }
        String prefix = messageId.substring(0, messageId.length() - 1);
        //更新接收者消息
        this.update(update, new QueryWrapper<Message>().eq("message_id", prefix + "1"));
        //更新发送者消息
        this.update(update, new QueryWrapper<Message>().eq("message_id", prefix + "2"));
    }


    @Override
    public IPage<Message> listMessage(IPage<Message> page, String userId, String conversationId) {
        return messageMapper.listMessage(page, userId, conversationId);
    }


    /**
     * 会话更新,推送给双方
     *
     * @param conversation
     */
    private void emitJSSDKConversationUpdated(Conversation conversation) {
        WebSocket.sendMessage(conversation.getUserId(), Result.info(SocketStatusCode.CONVERSATION_CHANGED.getCode(), SocketStatusCode.CONVERSATION_CHANGED.getDesc(), conversationService.getConversation(conversation.getConversationId(), conversation.getUserId())).toJSONString());
    }

    /**
     * 给接收方推送socket message
     *
     * @param message
     */
    private void emitJSSDKMessageReceive(Message message) {
        //消息是发送方的参照，会话ID是接收方，这里切换成发送方的。
        message.setConversationId(message.getFrom());
        WebSocket.sendMessage(message.getTo(), Result.info(SocketStatusCode.MESSAGE_RECEIVE.getCode(), "", message).toJSONString());
    }

}




