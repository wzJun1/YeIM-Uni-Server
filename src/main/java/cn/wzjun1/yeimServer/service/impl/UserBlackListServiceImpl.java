package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.UserBlackListV0;
import cn.wzjun1.yeimServer.dto.user.UserBlackListAddDTO;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.interceptor.UserAuthorizationInterceptor;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.wzjun1.yeimServer.domain.UserBlackList;
import cn.wzjun1.yeimServer.service.UserBlackListService;
import cn.wzjun1.yeimServer.mapper.UserBlackListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author wzjun1
 * @description 针对表【user_black_list(用户黑名单列表)】的数据库操作Service实现
 * @createDate 2023-02-19 11:19:57
 */
@Service
public class UserBlackListServiceImpl extends ServiceImpl<UserBlackListMapper, UserBlackList>
        implements UserBlackListService {

    @Autowired
    UserBlackListMapper userBlackListMapper;

    /**
     * 获取黑名单列表
     *
     * @return
     */
    @Override
    public List<UserBlackListV0> getBlackUserList() {
        return userBlackListMapper.getBlackUserList(LoginUserContext.getUser().getUserId());
    }

    /**
     * 黑名单入库
     *
     * @param userBlackListAddDTO
     * @throws Exception
     */
    @Override
    public void addToBlackUserList(UserBlackListAddDTO userBlackListAddDTO) throws Exception {
        long createdAt = System.currentTimeMillis();
        List<UserBlackList> userBlackList = new ArrayList<>();
        userBlackListAddDTO.getMembers().forEach(id -> {
            UserBlackList userBlack = new UserBlackList();
            userBlack.setUserId(LoginUserContext.getUser().getUserId());
            userBlack.setCoverUserId(id);
            userBlack.setCreatedAt(createdAt);
            userBlackList.add(userBlack);
        });
        this.saveBatch(userBlackList);
    }

    /**
     * 黑名单移除
     *
     * @param userBlackListAddDTO
     * @throws Exception
     */
    @Override
    public void removeFromBlacklist(UserBlackListAddDTO userBlackListAddDTO) throws Exception {
        userBlackListAddDTO.getMembers().forEach(id -> {
            this.remove(new QueryWrapper<UserBlackList>().eq("cover_user_id", id));
        });
    }


}




