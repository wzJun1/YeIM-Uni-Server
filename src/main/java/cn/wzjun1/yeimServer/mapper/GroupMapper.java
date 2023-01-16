package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.Group;
import cn.wzjun1.yeimServer.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Administrator
* @description 针对表【group】的数据库操作Mapper
* @createDate 2022-12-07 20:00:11
* @Entity cn.wzjun1.yeimServer.domain.Group
*/
@Mapper
public interface GroupMapper extends BaseMapper<Group> {
    @Select("select * from `group` where group_id = #{groupId}")
    Group findByGroupId(@Param("groupId") String groupId);

    @Select("SELECT * FROM `group` WHERE `group_id` in ( SELECT `group_id` from group_user WHERE user_id = #{userId} )")
    List<Group> selectGroupByUserId(@Param("userId") String userId);
}




