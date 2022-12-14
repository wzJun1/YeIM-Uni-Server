package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.domain.Group;
import cn.wzjun1.yeimServer.domain.GroupUser;
import cn.wzjun1.yeimServer.dto.group.GroupUserAddDTO;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.GroupApplyMapper;
import cn.wzjun1.yeimServer.result.vo.AddUserToGroupResultVO;
import cn.wzjun1.yeimServer.service.GroupService;
import cn.wzjun1.yeimServer.service.GroupUserService;
import cn.wzjun1.yeimServer.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Validated
@RestController
public class GroupUserController {

    @Autowired
    GroupService groupService;

    @Autowired
    GroupUserService groupUserService;

    @Autowired
    GroupApplyMapper groupApplyMapper;

    /**
     * 添加群成员
     *
     * @param params
     * @return AddUserToGroupResultVO
     */
    @PostMapping(path = "/group/user/add")
    @UserAuthorization
    public Result<AddUserToGroupResultVO> add(@RequestBody @Validated GroupUserAddDTO params) {
        try {
            AddUserToGroupResultVO resultVO = groupUserService.addUserToGroup(params);
            return Result.success(resultVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 移除群成员
     *
     * @param params
     * @return
     */
    @PostMapping(path = "/group/user/delete")
    @UserAuthorization
    public Result delete(@RequestBody @Validated GroupUserAddDTO params) {
        try {
            groupUserService.deleteUserFromGroup(params);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退出群組
     * @param groupId
     * @return
     */
    @GetMapping(path = "/group/user/leave")
    @UserAuthorization
    public Result leaveGroup(@RequestParam @NotEmpty String groupId) {
        try {
            groupUserService.leaveGroup(groupId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取入群申请列表
     *
     * @return
     */
    @GetMapping(path = "/group/user/apply/list")
    @UserAuthorization
    public Result applyList() {
        try {
            List<String> groupIds = new ArrayList<>();
            List<GroupUser> userManageGroups = groupUserService.list(new QueryWrapper<GroupUser>().eq("user_id", LoginUserContext.getUser().getUserId()).eq("is_admin", 1));
            List<Group> userOwnGroups = groupService.list(new QueryWrapper<Group>().eq("leader_user_id", LoginUserContext.getUser().getUserId()));
            userManageGroups.forEach(groupUser -> {
                if (!groupIds.contains(groupUser.getGroupId())) {
                    groupIds.add(groupUser.getGroupId());
                }
            });
            userOwnGroups.forEach(group -> {
                if (!groupIds.contains(group.getGroupId())) {
                    groupIds.add(group.getGroupId());
                }
            });
            return Result.success(groupApplyMapper.getApplyList(groupIds));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 处理用户入群申请
     *
     * @return
     */
    @GetMapping(path = "/group/user/apply/change")
    @UserAuthorization
    public Result applyHandle(@RequestParam @NotNull Integer id, @NotNull Integer status) {
        try {
            if (status > 3 || status < 1) {
                throw new Exception("处理结果 status 填写错误");
            }
            return Result.success(groupUserService.applyHandle(id, status));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    /**
     * 设置群成员为管理员，或取消设置为管理员
     *
     * @param groupId
     * @param userId
     * @param isAdmin 0 = 非管理员 1 = 管理员
     * @return
     */
    @GetMapping(path = "/group/user/set/adminstrator")
    @UserAuthorization
    public Result setAdminstrator(@RequestParam @NotEmpty String groupId, @NotEmpty String userId, @NotNull Integer isAdmin) {
        try {
            groupUserService.setAdminstrator(groupId, userId, isAdmin);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置用户禁言
     *
     * 每次设置均从操作时间开始计算禁言的具体到期时间
     * @param groupId
     * @param userId
     * @param time 分钟数，0表示取消禁言
     * @return
     */
    @GetMapping(path = "/group/user/set/mute")
    @UserAuthorization
    public Result setMute(@RequestParam @NotEmpty String groupId, @NotEmpty String userId, @NotNull Integer time) {
        try {
            groupUserService.setMute(groupId, userId, time);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取群成员列表
     *
     * @param groupId
     * @return
     */
    @GetMapping(path = "/group/user/list")
    @UserAuthorization
    public Result list(@RequestParam @NotEmpty String groupId) {
        try {
            return Result.success(groupUserService.getGroupUserList(groupId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
