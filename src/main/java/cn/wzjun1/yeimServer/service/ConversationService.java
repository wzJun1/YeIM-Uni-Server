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
    boolean updateConversation(String userId, String conversationId, String conversationType, String lastMessageId, Integer unread, boolean emit);

    void clearConversationUnread(String conversationId) throws Exception;
}
