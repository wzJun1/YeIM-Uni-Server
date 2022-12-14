package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.ConversationV0;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.MessageMapper;
import cn.wzjun1.yeimServer.socket.WebSocket;
import cn.wzjun1.yeimServer.socket.cons.SocketStatusCode;
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

    @Override
    public ConversationV0 getConversation(String conversationId, String userId) {
        return conversationMapper.getConversationV0(conversationId, userId);
    }

    @Override
    public void clearConversationUnread(String conversationId) throws Exception {
        boolean exist = conversationMapper.exists(new QueryWrapper<Conversation>().eq("conversation_id", conversationId).eq("user_id",LoginUserContext.getUser().getUserId()));
        if (!exist){
            throw new Exception("会话不存在");
        }
        Conversation update = new Conversation();
        update.setUnread(0);
        //更新会话未读数
        conversationMapper.update(update, new QueryWrapper<Conversation>().eq("conversation_id", conversationId).eq("user_id",LoginUserContext.getUser().getUserId()));
        //更新消息已读状态
        Message updateMessage = new Message();
        updateMessage.setIsRead(1);
        //更新当前会话对方的发件箱消息已读状态
        messageMapper.update(updateMessage, new QueryWrapper<Message>().eq("user_id", conversationId).eq("conversation_id", LoginUserContext.getUser().getUserId()).eq("direction", "out"));
        //如果会话接收方在线，发送PRIVATE_READ_RECEIPT事件
        WebSocket.sendMessage(conversationId, Result.info(SocketStatusCode.PRIVATE_READ_RECEIPT.getCode(), SocketStatusCode.PRIVATE_READ_RECEIPT.getDesc(), new HashMap<String, Object>() {{
            put("conversationId", LoginUserContext.getUser().getUserId());
        }}).toJSONString());
    }
}




