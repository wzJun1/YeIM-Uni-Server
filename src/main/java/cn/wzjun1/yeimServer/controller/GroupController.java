package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.dto.group.GroupCreateDTO;
import cn.wzjun1.yeimServer.dto.group.GroupEditDTO;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.GroupMapper;
import cn.wzjun1.yeimServer.mapper.GroupUserMapper;
import cn.wzjun1.yeimServer.service.GroupService;
import cn.wzjun1.yeimServer.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Slf4j
@Validated
@RestController
public class GroupController {

    @Autowired
    GroupService groupService;

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    GroupUserMapper groupUserMapper;

    /**
     * 创建群组
     *
     * @param params
     * @return
     */
    @PostMapping(path = "/group/create")
    @UserAuthorization
    public Result create(@RequestBody @Validated GroupCreateDTO params) {
        try {
            groupService.createGroup(params);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解散群组，仅群主可解散
     *
     * @param groupId
     * @return
     */
    @GetMapping(path = "/group/dissolve")
    @UserAuthorization
    public Result dissolve(@RequestParam @NotEmpty String groupId) {
        try {
            groupService.dissolveGroup(groupId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据群ID获取群信息
     *
     * @return
     */
    @GetMapping(path = "/group/get")
    @UserAuthorization
    public Result get(@RequestParam @NotEmpty String groupId) {
        try {
            return Result.success(groupMapper.findByGroupId(groupId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改群资料
     *
     * @return
     */
    @PostMapping(path = "/group/edit")
    @UserAuthorization
    public Result edit(@RequestBody @Validated GroupEditDTO params) {
        try {
            groupService.updateGroup(params);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 转让群主
     *
     * @param groupId
     * @param userId
     * @return
     */
    @GetMapping(path = "/group/transferLeader")
    @UserAuthorization
    public Result transferLeader(@RequestParam @NotEmpty String groupId, @NotEmpty String userId) {
        try {
            groupService.transferLeader(groupId, userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我加入的群组
     *
     * @return
     */
    @GetMapping(path = "/group/list")
    @UserAuthorization
    public Result list() {
        try {
            return Result.success(groupMapper.selectGroupByUserId(LoginUserContext.getUser().getUserId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


}
