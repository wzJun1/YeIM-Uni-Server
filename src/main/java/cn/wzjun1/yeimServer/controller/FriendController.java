package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.constant.StatusCode;
import cn.wzjun1.yeimServer.domain.*;
import cn.wzjun1.yeimServer.dto.friend.AddFriendDTO;
import cn.wzjun1.yeimServer.dto.friend.DeleteFriendDTO;
import cn.wzjun1.yeimServer.dto.friend.UpdateFriendDTO;
import cn.wzjun1.yeimServer.exception.FrequencyLimitException;
import cn.wzjun1.yeimServer.exception.ParamsException;
import cn.wzjun1.yeimServer.exception.friend.ApplyNeedException;
import cn.wzjun1.yeimServer.exception.friend.DuplicateException;
import cn.wzjun1.yeimServer.exception.friend.FriendApplyNotFoundException;
import cn.wzjun1.yeimServer.exception.friend.FriendNotFoundException;
import cn.wzjun1.yeimServer.exception.user.UserFriendDenyException;
import cn.wzjun1.yeimServer.exception.user.UserFriendDuplicateException;
import cn.wzjun1.yeimServer.exception.user.UserNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.FriendApplyMapper;
import cn.wzjun1.yeimServer.mapper.FriendMapper;
import cn.wzjun1.yeimServer.result.Result;
import cn.wzjun1.yeimServer.service.FriendApplyService;
import cn.wzjun1.yeimServer.service.FriendService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@Validated
@RestController
public class FriendController {

    @Autowired
    FriendMapper friendMapper;

    @Autowired
    FriendService friendService;

    @Autowired
    FriendApplyService friendApplyService;

    @Autowired
    FriendApplyMapper friendApplyMapper;

    /**
     * 获取用户好友列表
     *
     * @param profile 资料类型，0=简略资料，1=详细资料
     * @param page    页码
     * @param limit   每页数量
     * @return IPage<FriendApplyV0>
     */
    @UserAuthorization
    @GetMapping(path = "/friend/list")
    public Result friendList(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer limit, Integer profile) {
        IPage<FriendV0> friendV0IPage = null;
        if (profile == null || profile == 0) {
            friendV0IPage = friendMapper.getFriendList(Page.of(page, limit), LoginUserContext.getUser().getUserId());
        } else {
            friendV0IPage = friendMapper.getFriendProfileList(Page.of(page, limit), LoginUserContext.getUser().getUserId());
        }
        return Result.success(friendV0IPage);
    }

    /**
     * 添加好友
     */
    @UserAuthorization
    @PostMapping(path = "/friend/add")
    public Result addFriend(@RequestBody @Validated AddFriendDTO addFriendDTO) {
        try {
            friendApplyService.addFriend(addFriendDTO);
        } catch (Exception e) {
            if (e instanceof UserNotFoundException) {
                return Result.error(StatusCode.USER_NOT_FOUND.getCode(), e.getMessage());
            } else if (e instanceof UserFriendDenyException) {
                return Result.error(StatusCode.USER_FRIEND_DENY_FOUND.getCode(), e.getMessage());
            } else if (e instanceof UserFriendDuplicateException) {
                return Result.error(StatusCode.USER_FRIEND_DUPLICATE.getCode(), e.getMessage());
            } else if (e instanceof FrequencyLimitException) {
                return Result.error(StatusCode.FREQUENCY_LIMIT.getCode(), e.getMessage());
            } else if (e instanceof ApplyNeedException) {
                return Result.error(StatusCode.APPLY_NEED.getCode(), e.getMessage());
            }
            return Result.error(e.getMessage());
        }
        return Result.success("好友添加成功");
    }

    @UserAuthorization
    @PostMapping(path = "/friend/delete")
    public Result deleteFriend(@RequestBody @Validated DeleteFriendDTO deleteFriendDTO) {
        try {
            friendService.deleteFriend(deleteFriendDTO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    @UserAuthorization
    @PostMapping(path = "/friend/update")
    public Result updateFriend(@RequestBody @Validated UpdateFriendDTO updateFriendDTO) {
        try {
            friendService.updateFriend(updateFriendDTO);
        } catch (Exception e) {
            if (e instanceof FriendNotFoundException) {
                return Result.error(StatusCode.FRIEND_NOT_FOUND.getCode(), e.getMessage());
            } else if (e instanceof ParamsException) {
                return Result.error(StatusCode.PARAMS_ERROR.getCode(), e.getMessage());
            }
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 获取用户好友申请列表
     *
     * @param type  类型，0=发给我申请，1=我发出去的申请
     * @param page  页码
     * @param limit 每页数量
     * @return IPage<FriendApplyV0>
     */
    @UserAuthorization
    @GetMapping(path = "/friend/apply/list")
    public Result applyList(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer limit, Integer type) {
        IPage<FriendApplyV0> friendApplyV0IPage = null;
        if (type == null || type == 0) {
            friendApplyV0IPage = friendApplyMapper.getApplyList(Page.of(page, limit), LoginUserContext.getUser().getUserId());
        } else {
            friendApplyV0IPage = friendApplyMapper.getRequestList(Page.of(page, limit), LoginUserContext.getUser().getUserId());
        }
        IPage<FriendApplyV0> finalFriendApplyV0IPage = friendApplyV0IPage;
        return Result.success(new HashMap<String, Object>() {{
            put("apply", finalFriendApplyV0IPage);
            put("unread", friendApplyMapper.selectCount(new QueryWrapper<FriendApply>().eq("user_id", LoginUserContext.getUser().getUserId()).eq("is_read", 0)));
        }});
    }

    /**
     * 将全部好友申请设置为已读状态
     */
    @UserAuthorization
    @GetMapping(path = "/friend/apply/set/read")
    public Result setApplyRead() {
        FriendApply friendApply = new FriendApply();
        friendApply.setIsRead(1);
        friendApplyService.update(friendApply, new QueryWrapper<FriendApply>().eq("user_id", LoginUserContext.getUser().getUserId()));
        return Result.success();
    }

    /**
     * 同意好友申请
     *
     * @param id     申请ID
     * @param remark 备注
     * @return
     */
    @UserAuthorization
    @GetMapping(path = "/friend/apply/accept")
    public Result acceptApply(@RequestParam @NonNull Integer id, String remark) {
        try {
            friendApplyService.acceptApply(id, remark);
        } catch (Exception e) {
            if (e instanceof FriendApplyNotFoundException) {
                return Result.error(StatusCode.FRIEND_APPLY_NOT_FOUND.getCode(), e.getMessage());
            } else if (e instanceof DuplicateException) {
                return Result.error(StatusCode.DUPLICATE_ERROR.getCode(), e.getMessage());
            } else if (e instanceof UserNotFoundException) {
                return Result.error(StatusCode.USER_NOT_FOUND.getCode(), e.getMessage());
            }
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 拒绝好友申请
     *
     * @param id 申请ID
     * @return
     */
    @UserAuthorization
    @GetMapping(path = "/friend/apply/refuse")
    public Result refuseApply(@RequestParam @NonNull Integer id) {
        try {
            friendApplyService.refuseApply(id);
        } catch (Exception e) {
            if (e instanceof FriendApplyNotFoundException) {
                return Result.error(StatusCode.FRIEND_APPLY_NOT_FOUND.getCode(), e.getMessage());
            } else if (e instanceof DuplicateException) {
                return Result.error(StatusCode.DUPLICATE_ERROR.getCode(), e.getMessage());
            }
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

}
