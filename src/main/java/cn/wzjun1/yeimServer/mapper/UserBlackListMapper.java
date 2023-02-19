package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.UserBlackList;
import cn.wzjun1.yeimServer.domain.UserBlackListV0;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author wzjun1
* @description 针对表【user_black_list(用户黑名单列表)】的数据库操作Mapper
* @createDate 2023-02-19 11:19:57
* @Entity cn.wzjun1.yeimServer.domain.UserBlackList
*/
@Mapper
public interface UserBlackListMapper extends BaseMapper<UserBlackList> {
    List<UserBlackListV0> getBlackUserList(@Param("userId") String userId);
}




