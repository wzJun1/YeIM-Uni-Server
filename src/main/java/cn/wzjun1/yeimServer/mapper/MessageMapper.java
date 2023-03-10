package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wzjun1
 * @description 针对表【message】的数据库操作Mapper
 * @createDate 2022-11-16 23:29:12
 * @Entity cn.wzjun1.yeimServer.domain.Message
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    Message getMessageById(@Param("messageId") String messageId, @Param("userId") String userId);
    IPage<Message> listMessage(IPage<Message> page, @Param("userId") String userId, @Param("conversationId") String conversationId);
    List<Message> listMessageByNextMessageId(@Param("userId") String userId, @Param("conversationId") String conversationId, @Param("nextMessageId") String nextMessageId, @Param("limit") Integer limit);

}




