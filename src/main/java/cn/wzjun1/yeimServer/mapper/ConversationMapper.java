package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.Conversation;
import cn.wzjun1.yeimServer.domain.ConversationV0;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author wzjun1
 * @description 针对表【conversation】的数据库操作Mapper
 * @createDate 2022-11-17 16:51:12
 * @Entity cn.wzjun1.yeimServer.domain.Conversation
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    ConversationV0 getConversationV0(@Param("conversationId") String conversationId, @Param("userId") String userId);

    IPage<ConversationV0> listConversationV0(IPage<ConversationV0> page, @Param("userId") String userId);


    @Select("update `conversation` set `unread` = `unread` + 1, `last_message_id` = #{messageId},`updated_at` = #{time} where `conversation_id` = #{groupId} and user_id = #{userId} and type = 'group'")
    void updateGroupConversation(@Param("messageId") String messageId, @Param("time") long time, @Param("groupId") String groupId, @Param("userId") String userId);

}




