package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.GroupApply;
import cn.wzjun1.yeimServer.domain.GroupUser;
import cn.wzjun1.yeimServer.dto.group.GroupUserAddDTO;
import cn.wzjun1.yeimServer.result.vo.AddUserToGroupResultVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【group_user】的数据库操作Service
* @createDate 2022-12-07 20:55:41
*/
public interface GroupUserService extends IService<GroupUser> {
    AddUserToGroupResultVO addUserToGroup(GroupUserAddDTO groupUserAddDTO) throws Exception;

    void deleteUserFromGroup(GroupUserAddDTO groupUserAddDTO) throws Exception;

    void leaveGroup(String groupId) throws Exception;

    void setAdminstrator(String groupId, String userId, Integer isAdmin) throws Exception;

    void setMute(String groupId, String userId, Integer time) throws Exception;

    GroupApply applyHandle(Integer ApplyId, Integer status) throws Exception;

    List<GroupUser> getGroupUserList(String groupId) throws Exception;
}
