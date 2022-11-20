package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.mapper.UserMapper;
import cn.wzjun1.yeimServer.pojo.user.UserRegisterPojo;
import cn.wzjun1.yeimServer.pojo.user.UserUpdatePojo;
import cn.wzjun1.yeimServer.service.UserService;
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
    implements UserService{

    @Autowired
    UserMapper userMapper;

    @Override
    public User getUserById(String userId) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("user_id",userId));
    }

    @Override
    public void register(UserRegisterPojo user) throws Exception {
        User exist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id",user.getUserId()));
        if (!Objects.isNull(exist)){
            throw new Exception("用户已存在，请勿重复注册");
        }
        User entity = new User();
        entity.setUserId(user.getUserId());
        entity.setNickname(user.getNickname());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setCreatedAt(System.currentTimeMillis());
        int result = userMapper.insert(entity);
        if (result <= 0){
            throw new Exception("注册用户异常，请稍后重试");
        }
    }

    @Override
    public void updateUser(String userId, UserUpdatePojo user) throws Exception {
        User entity = new User();
        entity.setNickname(user.getNickname());
        entity.setAvatarUrl(user.getAvatarUrl());
        int result = userMapper.update(entity,new QueryWrapper<User>().eq("user_id",userId));
        if (result <= 0){
            throw new Exception("更新用户资料异常，请稍后重试");
        }
    }
}




