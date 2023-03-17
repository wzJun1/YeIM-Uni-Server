package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.constant.StatusCode;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.dto.group.GroupUserAddDTO;
import cn.wzjun1.yeimServer.dto.user.UserBlackListAddDTO;
import cn.wzjun1.yeimServer.exception.message.MessageRejectedException;
import cn.wzjun1.yeimServer.exception.message.ToUserIdNotFoundException;
import cn.wzjun1.yeimServer.exception.user.ExpireException;
import cn.wzjun1.yeimServer.exception.user.SignException;
import cn.wzjun1.yeimServer.exception.user.UserDuplicateException;
import cn.wzjun1.yeimServer.exception.user.UserNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.dto.user.UserRegisterDTO;
import cn.wzjun1.yeimServer.dto.user.UserTokenDTO;
import cn.wzjun1.yeimServer.dto.user.UserUpdateDTO;
import cn.wzjun1.yeimServer.service.UserBlackListService;
import cn.wzjun1.yeimServer.service.UserService;
import cn.wzjun1.yeimServer.utils.MD5Util;
import cn.wzjun1.yeimServer.utils.RedisUtil;
import cn.wzjun1.yeimServer.result.Result;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Validated
@RestController
public class UserController {

    @Value("${yeim.secret.key}")
    private String secretKey;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserService userService;

    @Autowired
    UserBlackListService userBlackListService;

    /**
     * 用户注册
     *
     * @param user
     * @description 使用YeIMUniSDK的用户必须注册才能使用（请使用自己的服务端请求此接口或者根据自身项目自行实现相关代码）
     */
    @PostMapping(path = "/user/register")
    public Result register(@RequestBody @Validated UserRegisterDTO user) {
        try {
            userService.register(user);
        } catch (Exception e) {
            if (e instanceof UserDuplicateException) {
                return Result.error(StatusCode.USER_DUPLICATE);
            }
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 换取token
     *
     * @param params
     * @return
     * @description 用户使用YeIMUniSDK登陆YeImUniServer使用的token，在此换取。或者开发者可自行打通系统，推荐开发者自行打通生成token，避免安全问题。
     */
    @PostMapping(path = "/user/token/get")
    public Result<Map<String, String>> getToken(@RequestBody @Validated UserTokenDTO params) {

        try {
            if (params.getTimestamp() < System.currentTimeMillis()) {
                throw new ExpireException("过期时间设置错误，必须大于当前时间");
            }
            //sign = md5(userId+timestamp+secretKey)
            //secretKey(yeim.secret.key)在application.properties 可自行替换想要的字符串
            String str = params.getUserId() + params.getTimestamp() + secretKey;
            String secret = MD5Util.encode(str);
            if (!secret.equals(params.getSign())) {
                throw new SignException("签名校验错误");
            }

            User user = userService.getUserById(params.getUserId());
            if (Objects.isNull(user)) {
                throw new UserNotFoundException("用户不存在，请先注册");
            }

            //过期时间
            int expire = (int) ((params.getTimestamp() - System.currentTimeMillis()) / 1000);
            //token
            String token = MD5Util.encode(params.getUserId() + System.currentTimeMillis() + secretKey);
            //用户数据放入缓存
            redisUtil.setWithExpire("token:" + token, user, expire);

            return Result.success(new HashMap<String, String>() {{
                put("token", token);
            }});

        } catch (Exception e) {
            if (e instanceof ExpireException) {
                return Result.error(StatusCode.EXPIRE_ERROR.getCode(), e.getMessage());
            } else if (e instanceof SignException) {
                return Result.error(StatusCode.SIGN_ERROR);
            } else if (e instanceof UserNotFoundException) {
                return Result.error(StatusCode.USER_NOT_FOUND.getCode(), e.getMessage());
            }
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户更新资料
     *
     * @param user
     * @description 更新用户昵称和头像地址
     */
    @UserAuthorization
    @PostMapping(path = "/user/update")
    public Result update(@RequestBody @Validated UserUpdateDTO user) {
        try {
            userService.updateUser(LoginUserContext.getUser().getUserId(), user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 根据userId获取用户资料
     *
     * @param userId
     * @return
     */
    @UserAuthorization
    @GetMapping(path = "/user/info")
    public Result getUserInfo(@RequestParam @NotEmpty String userId) {
        return Result.success(userService.getUserById(userId));
    }

    /**
     * 绑定个推移动端推送clientId
     *
     * @param clientId 推送标识符
     * @return
     */
    @UserAuthorization
    @GetMapping(path = "/user/bind/push/id")
    public Result bindClientId(@RequestParam @NotEmpty String clientId) {
        try {
            User update = new User();
            update.setMobileDeviceId(clientId);
            userService.update(update, new UpdateWrapper<User>().eq("user_id", LoginUserContext.getUser().getUserId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 获取黑名单列表
     *
     * @return
     */
    @UserAuthorization
    @GetMapping(path = "/user/black/list")
    public Result getBlackUserList() {
        try {
            return Result.success(userBlackListService.getBlackUserList());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 将用户加入黑名单
     *
     * @param params UserBlackListAddDTO
     * @return
     */
    @UserAuthorization
    @PostMapping(path = "/user/black/add")
    public Result addToBlackUserList(@RequestBody @Validated UserBlackListAddDTO params) {
        try {
            userBlackListService.addToBlackUserList(params);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 将用户移除黑名单
     *
     * @param params UserBlackListAddDTO
     * @return
     */
    @UserAuthorization
    @PostMapping(path = "/user/black/remove")
    public Result removeFromBlacklist(@RequestBody @Validated UserBlackListAddDTO params) {
        try {
            userBlackListService.removeFromBlacklist(params);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }

}
