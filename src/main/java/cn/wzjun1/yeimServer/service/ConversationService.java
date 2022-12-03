package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.Conversation;
import cn.wzjun1.yeimServer.domain.ConversationV0;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author wzjun1
* @description 针对表【conversation】的数据库操作Service
* @createDate 2022-11-17 16:51:12
*/
public interface ConversationService extends IService<Conversation> {
    ConversationV0 getConversation(String conversationId, String userId);

    void clearConversationUnread(String conversationId) throws Exception;
}
