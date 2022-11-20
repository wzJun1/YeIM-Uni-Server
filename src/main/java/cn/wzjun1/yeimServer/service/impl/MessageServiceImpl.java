package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.Conversation;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.entity.SocketMessage;
import cn.wzjun1.yeimServer.mapper.MessageMapper;
import cn.wzjun1.yeimServer.pojo.message.MessageSavePojo;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.service.MessageService;
import cn.wzjun1.yeimServer.socket.WebSocket;
import cn.wzjun1.yeimServer.socket.cons.MessageStatus;
import cn.wzjun1.yeimServer.socket.cons.SocketStatusCode;
import cn.wzjun1.yeimServer.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    MessageMapper messageMapper;

    @Autowired
    ConversationService conversationService;

    @Override
    public Message formatMessage(SocketMessage message) {
        Message entity = new Message();
        entity.setMessageId(message.getMessageId());
        entity.setConversationId(message.getTo());
        entity.setType(message.getType());
        entity.setFrom(message.getFrom());
        entity.setTo(message.getTo());
        entity.setIsRead(0);
        entity.setIsRecall(0);
        entity.setStatus(MessageStatus.SUCCESS);
        entity.setTime(System.currentTimeMillis());
        //entity.setBody(message.getBody());
        return entity;
    }

    @Override
    public int insertMessage(SocketMessage message) {
        Message entity = formatMessage(message);
        int result = messageMapper.insert(entity);
        return result;
    }

    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    @Override
    public Message insertMessage(User user, MessageSavePojo message) {
        try {

            //1.插入消息到数据库
            Message entity = new Message();
            entity.setMessageId(message.getMessageId());
            entity.setConversationId(message.getTo());
            entity.setType(message.getType());
            entity.setFrom(message.getFrom());
            entity.setTo(message.getTo());
            entity.setIsRead(0);
            entity.setIsRecall(0);
            entity.setStatus(MessageStatus.SUCCESS);
            entity.setTime(System.currentTimeMillis());
            entity.setBody(message.getBody());
            boolean messageResult = this.save(entity);
            Message resultMessage = messageMapper.getMessageById(message.getMessageId());

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
            conversation1.setLastMessageId(message.getMessageId());
            if (conversation1.getCreatedAt() == null || conversation1.getCreatedAt() == 0){
                conversation1.setCreatedAt(System.currentTimeMillis());
            }else{
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
            conversation2.setLastMessageId(message.getMessageId());
            if (conversation2.getCreatedAt() == null || conversation2.getCreatedAt() == 0){
                conversation2.setCreatedAt(System.currentTimeMillis());
            }else{
                conversation2.setUpdatedAt(System.currentTimeMillis());
            }
            boolean conversation2Result = conversationService.saveOrUpdate(conversation2);
            // 2.2.1 推送给接收者会话更新消息
            this.emitJSSDKConversationUpdated(conversation2);
            // 2.2.2 推送给接收者socket消息，如果在线的话
            this.emitJSSDKMessageReceive(resultMessage);

            if (messageResult && conversation1Result && conversation2Result) {
                return resultMessage;
            } else {
                throw new Exception("insertMessage error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public IPage<Message> listMessage(IPage<Message> page, String userId, String conversationId) {
        return messageMapper.listMessage(page, userId ,conversationId);
    }

    /**
     * 会话更新,推送给双方
     * @param conversation
     */
    private void emitJSSDKConversationUpdated(Conversation conversation){
        WebSocket.sendMessage(conversation.getUserId(), Result.info(SocketStatusCode.CONVERSATION_CHANGED.getCode(), SocketStatusCode.CONVERSATION_CHANGED.getDesc(),conversationService.getConversation(conversation.getConversationId(),conversation.getUserId())).toJSONString());
    }

    /**
     * 给接收方推送socket message
     * @param message
     */
    private void emitJSSDKMessageReceive(Message message){
        //消息是发送方的参照，会话ID是接收方，这里切换成发送方的。
        message.setConversationId(message.getFrom());
        WebSocket.sendMessage(message.getTo(), Result.info(SocketStatusCode.MESSAGE_RECEIVE.getCode(), "",message).toJSONString());
    }

}




