package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.Group;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.exception.ParamsException;
import cn.wzjun1.yeimServer.exception.user.UserDuplicateException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.UserMapper;
import cn.wzjun1.yeimServer.dto.user.UserRegisterDTO;
import cn.wzjun1.yeimServer.dto.user.UserUpdateDTO;
import cn.wzjun1.yeimServer.service.UserService;
import cn.wzjun1.yeimServer.utils.Common;
import cn.wzjun1.yeimServer.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author wzjun1
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2022-11-15 20:23:25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public User getUserById(String userId) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userId));
    }

    @Override
    public void register(UserRegisterDTO user) throws Exception {
        User exist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", user.getUserId()));
        if (!Objects.isNull(exist)) {
            throw new UserDuplicateException("用户已存在，请勿重复注册");
        }
        User entity = new User();
        entity.setUserId(user.getUserId());
        entity.setNickname(user.getNickname());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setCreatedAt(System.currentTimeMillis());
        if (user.getGender() != null) {
            entity.setGender(user.getGender());
        }
        if (user.getMobile() != null && user.getMobile() != 0) {
            entity.setMobile(user.getMobile());
        }
        if (user.getEmail() != null) {
            entity.setEmail(user.getEmail());
        }
        if (user.getBirthday() != null) {
            entity.setBirthday(user.getBirthday());
        }
        if (user.getMotto() != null) {
            entity.setMotto(user.getMotto());
        }
        if (user.getExtend() != null) {
            entity.setExtend(user.getExtend());
        }
        if (user.getAddFriendType() != null && user.getAddFriendType() != 0) {
            entity.setAddFriendType(user.getAddFriendType());
        }
        int result = userMapper.insert(entity);
        if (result <= 0) {
            throw new Exception("注册用户异常，请稍后重试");
        }
    }

    @Override
    public void updateUser(String userId, UserUpdateDTO user) throws Exception {

        User entity = new User();
        if (user.getNickname() != null) {
            if (user.getNickname().length() > 32) {
                throw new ParamsException("用户昵称的不能超过32位");
            }
            entity.setNickname(user.getNickname());
        }
        if (user.getAvatarUrl() != null) {
            entity.setAvatarUrl(user.getAvatarUrl());
        }
        if (user.getGender() != null) {
            entity.setGender(user.getGender());
        }
        if (user.getMobile() != null && user.getMobile() != 0) {
            entity.setMobile(user.getMobile());
        }
        if (user.getEmail() != null) {
            entity.setEmail(user.getEmail());
        }
        if (user.getBirthday() != null) {
            entity.setBirthday(user.getBirthday());
        }
        if (user.getMotto() != null) {
            entity.setMotto(user.getMotto());
        }
        if (user.getExtend() != null) {
            entity.setExtend(user.getExtend());
        }
        if (user.getAddFriendType() != null && user.getAddFriendType() != 0) {
            entity.setAddFriendType(user.getAddFriendType());
        }
        if (!Common.isNotEmptyBean(entity)) {
            throw new ParamsException("请至少选择一个属性进行更新");
        }

        //更新数据库
        int result = userMapper.update(entity, new QueryWrapper<User>().eq("user_id", userId));
        if (result <= 0) {
            throw new Exception("更新用户资料异常，请稍后重试");
        }

        //更新redis缓存中用户信息
        String tokenKey = "token:" + LoginUserContext.getToken();
        if (redisUtil.hasKey(tokenKey)) {
            long expire = redisUtil.getExpire(tokenKey);
            User update = this.getUserById(userId);
            redisUtil.setWithExpire(tokenKey, update, expire);
        }

    }
}




