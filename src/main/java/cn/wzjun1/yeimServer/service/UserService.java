package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.dto.user.UserRegisterDTO;
import cn.wzjun1.yeimServer.dto.user.UserUpdateDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author wzjun1
* @description 针对表【user】的数据库操作Service
* @createDate 2022-11-15 20:23:25
*/
public interface UserService extends IService<User> {

    User getUserById(String userId);
    void register(UserRegisterDTO user) throws Exception;

    void updateUser(String userId, UserUpdateDTO user) throws Exception;
}
