package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author wzjun1
* @description 针对表【message】的数据库操作Mapper
* @createDate 2022-11-16 23:29:12
* @Entity cn.wzjun1.yeimServer.domain.Message
*/
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    Message getMessageById(String messageId);
    IPage<Message> listMessage(IPage<Message> page, @Param("userId") String userId,@Param("conversationId") String conversationId);
}




