package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.ConversationV0;
import cn.wzjun1.yeimServer.domain.FriendApply;
import cn.wzjun1.yeimServer.domain.FriendApplyV0;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author wzjun1
 * @description 针对表【friend_apply(好友申请添加表)】的数据库操作Mapper
 * @createDate 2023-04-03 19:09:36
 * @Entity generator.domain.FriendApply
 */

@Mapper
public interface FriendApplyMapper extends BaseMapper<FriendApply> {
    IPage<FriendApplyV0> getApplyList(IPage<ConversationV0> page, @Param("userId") String userId);
    IPage<FriendApplyV0> getRequestList(IPage<ConversationV0> page, @Param("userId") String userId);
    FriendApplyV0 fetchApplyById(@Param("id") Long id);
}




