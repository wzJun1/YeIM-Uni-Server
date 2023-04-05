package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.ConversationV0;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.exception.conversation.ConversationNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.MessageMapper;
import cn.wzjun1.yeimServer.service.OnlineChannel;
import cn.wzjun1.yeimServer.socket.WebSocket;
import cn.wzjun1.yeimServer.constant.ConversationType;
import cn.wzjun1.yeimServer.constant.StatusCode;
import cn.wzjun1.yeimServer.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.wzjun1.yeimServer.domain.Conversation;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.mapper.ConversationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author wzjun1
 * @description 针对表【conversation】的数据库操作Service实现
 * @createDate 2022-11-17 16:51:12
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
        implements ConversationService {

    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    MessageMapper messageMapper;

    @Autowired
    OnlineChannel onlineChannel;

    @Override
    public ConversationV0 getConversation(String conversationId, String userId) {
        return conversationMapper.getConversationV0(conversationId, userId);
    }

    /**
     * 新增或更新会话
     *
     * @param userId           会话所属用户
     * @param conversationId   会话ID
     * @param conversationType 会话类型
     * @param lastMessageId    最新消息ID
     * @param unread           未读数
     * @param emit             是否给会话所属用户发送会话更新事件
     */
    @Override
    public boolean updateConversation(String userId, String conversationId, String conversationType, String lastMessageId, Integer unread, boolean emit) {
        Conversation conversation = this.getOne(new QueryWrapper<Conversation>().eq("user_id", userId).eq("conversation_id", conversationId));
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setUnread(unread);
        } else {
            //TODO 存在并发问题
            conversation.setUnread(conversation.getUnread() + unread);
        }
        conversation.setConversationId(conversationId);
        conversation.setType(conversationType);
        conversation.setUserId(userId);
        if (lastMessageId != null && !lastMessageId.equals("")) {
            conversation.setLastMessageId(lastMessageId);
        }
        if (conversation.getCreatedAt() == null || conversation.getCreatedAt() == 0) {
            conversation.setCreatedAt(System.currentTimeMillis());
        } else {
            conversation.setUpdatedAt(System.currentTimeMillis());
        }
        //新增或更新
        boolean conversation1Result = this.saveOrUpdate(conversation);
        if (conversation1Result) {
            //推送给发送者会话更新消息
            this.emitJSSDKConversationUpdated(conversation);
        }
        return conversation1Result;
    }

    @Override
    public void clearConversationUnread(String conversationId) throws Exception {
        Conversation exist = conversationMapper.selectOne(new QueryWrapper<Conversation>().eq("conversation_id", conversationId).eq("user_id", LoginUserContext.getUser().getUserId()));
        if (exist == null || exist.getConversationId() == null) {
            throw new ConversationNotFoundException("会话不存在");
        }
        Conversation update = new Conversation();
        update.setUnread(0);
        //更新会话未读数
        conversationMapper.update(update, new QueryWrapper<Conversation>().eq("conversation_id", conversationId).eq("user_id", LoginUserContext.getUser().getUserId()));
        //私聊会话更新消息已读状态并发送已读消息事件
        if (exist.getType().equals(ConversationType.PRIVATE)) {
            //更新消息已读状态
            Message updateMessage = new Message();
            updateMessage.setIsRead(1);
            //更新当前会话对方的发件箱消息已读状态
            messageMapper.update(updateMessage, new QueryWrapper<Message>().eq("user_id", conversationId).eq("conversation_id", LoginUserContext.getUser().getUserId()).eq("direction", "out"));
            //如果会话接收方在线，发送PRIVATE_READ_RECEIPT事件
            onlineChannel.send(conversationId, Result.info(StatusCode.PRIVATE_READ_RECEIPT.getCode(), StatusCode.PRIVATE_READ_RECEIPT.getDesc(), new HashMap<String, Object>() {{
                put("conversationId", LoginUserContext.getUser().getUserId());
            }}).toJSONString());
        }
    }

    /**
     * 会话更新事件
     *
     * @param conversation
     */
    private void emitJSSDKConversationUpdated(Conversation conversation) {
        onlineChannel.send(conversation.getUserId(), Result.info(StatusCode.CONVERSATION_CHANGED.getCode(), StatusCode.CONVERSATION_CHANGED.getDesc(), this.getConversation(conversation.getConversationId(), conversation.getUserId())).toJSONString());
    }
}




