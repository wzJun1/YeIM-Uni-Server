package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.GroupMessage;
import cn.wzjun1.yeimServer.domain.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Administrator
 * @description 针对表【group_message】的数据库操作Mapper
 * @createDate 2022-12-08 21:18:05
 * @Entity cn.wzjun1.yeimServer.domain.GroupMessage
 */
@Mapper
public interface GroupMessageMapper extends BaseMapper<GroupMessage> {
    GroupMessage getMessageById(@Param("messageId") String messageId, @Param("conversationId") String conversationId);

    IPage<GroupMessage> listMessage(IPage<GroupMessage> page, @Param("userId") String userId, @Param("conversationId") String conversationId);

    void deleteGroupMessage(@Param("messageId") String messageId, @Param("groupId") String groupId, @Param("userId") String userId, @Param("createdAt") Long createdAt);

}




