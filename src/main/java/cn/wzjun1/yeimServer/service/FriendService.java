package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.Friend;
import cn.wzjun1.yeimServer.dto.friend.DeleteFriendDTO;
import cn.wzjun1.yeimServer.dto.friend.UpdateFriendDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author wzjun1
* @description 针对表【friend(好友表)】的数据库操作Service
* @createDate 2023-04-04 10:48:49
*/
public interface FriendService extends IService<Friend> {
    void deleteFriend(DeleteFriendDTO deleteFriendDTO);
    void updateFriend(UpdateFriendDTO updateFriendDTO);
}
