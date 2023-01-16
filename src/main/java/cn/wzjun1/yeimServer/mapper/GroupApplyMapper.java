package cn.wzjun1.yeimServer.mapper;

import cn.wzjun1.yeimServer.domain.GroupApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
* @description 针对表【group_apply(入群申请表)】的数据库操作Mapper
* @createDate 2022-12-11 10:52:00
* @Entity cn.wzjun1.yeimServer.domain.GroupApply
*/
@Mapper
public interface GroupApplyMapper extends BaseMapper<GroupApply> {

    GroupApply getApply(@Param("id") Integer id);

    List<GroupApply> getApplyList(@Param("groupIds") List<String> groupIds);

}




