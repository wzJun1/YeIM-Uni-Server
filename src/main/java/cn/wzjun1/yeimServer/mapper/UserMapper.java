package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author wzjun1
* @description 针对表【user】的数据库操作Mapper
* @createDate 2022-11-15 20:23:25
* @Entity cn.wzjun1.yeimServer.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where user_id = #{userId}")
    User findByUserId(@Param("userId") String userId);
}




