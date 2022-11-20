package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.Conversation;
import cn.wzjun1.yeimServer.domain.ConversationV0;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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


}




