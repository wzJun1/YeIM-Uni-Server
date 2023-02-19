package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.UserBlackList;
import cn.wzjun1.yeimServer.domain.UserBlackListV0;
import cn.wzjun1.yeimServer.dto.user.UserBlackListAddDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author wzjun1
* @description 针对表【user_black_list(用户黑名单列表)】的数据库操作Service
* @createDate 2023-02-19 11:19:57
*/
public interface UserBlackListService extends IService<UserBlackList> {

    List<UserBlackListV0> getBlackUserList();
    void addToBlackUserList(UserBlackListAddDTO userBlackListAddDTO) throws Exception;
    void removeFromBlacklist(UserBlackListAddDTO userBlackListAddDTO) throws Exception;


}
