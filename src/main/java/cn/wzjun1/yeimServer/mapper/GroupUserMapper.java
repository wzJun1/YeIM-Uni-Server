package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.GroupUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
* @description 针对表【group_user】的数据库操作Mapper
* @createDate 2022-12-07 20:55:41
* @Entity cn.wzjun1.yeimServer.domain.GroupUser
*/
@Mapper
public interface GroupUserMapper extends BaseMapper<GroupUser> {
    List<GroupUser> getGroupUserList(@Param("groupId") String groupId);
}




