package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.ConversationV0;
import cn.wzjun1.yeimServer.domain.Friend;
import cn.wzjun1.yeimServer.domain.FriendV0;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author wzjun1
* @description 针对表【friend(好友表)】的数据库操作Mapper
* @createDate 2023-04-04 10:48:49
* @Entity generator.domain.Friend
*/
@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
    IPage<FriendV0> getFriendList(IPage<ConversationV0> page, @Param("userId") String userId);
    IPage<FriendV0> getFriendProfileList(IPage<ConversationV0> page, @Param("userId") String userId);
}




