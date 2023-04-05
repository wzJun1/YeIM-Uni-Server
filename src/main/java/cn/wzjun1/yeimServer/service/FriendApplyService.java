package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.FriendApply;
import cn.wzjun1.yeimServer.dto.friend.AddFriendDTO;
import cn.wzjun1.yeimServer.dto.friend.DeleteFriendDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author wzjun1
* @description 针对表【friend_apply(好友申请添加表)】的数据库操作Service
* @createDate 2023-04-03 19:09:36
*/
public interface FriendApplyService extends IService<FriendApply> {
    void addFriend(AddFriendDTO addFriendDTO);
    void acceptApply(Integer id, String remark);
    void refuseApply(Integer id);
}
