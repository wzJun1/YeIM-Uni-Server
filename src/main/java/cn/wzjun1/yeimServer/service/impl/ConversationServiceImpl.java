package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.ConversationV0;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.wzjun1.yeimServer.domain.Conversation;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.mapper.ConversationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public ConversationV0 getConversation(String conversationId, String userId) {
        return conversationMapper.getConversationV0(conversationId, userId);
    }
}




