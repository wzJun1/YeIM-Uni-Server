package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.constant.StatusCode;
import cn.wzjun1.yeimServer.domain.Friend;
import cn.wzjun1.yeimServer.dto.friend.DeleteFriendDTO;
import cn.wzjun1.yeimServer.dto.friend.UpdateFriendDTO;
import cn.wzjun1.yeimServer.exception.ParamsException;
import cn.wzjun1.yeimServer.exception.friend.FriendNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.FriendMapper;
import cn.wzjun1.yeimServer.result.Result;
import cn.wzjun1.yeimServer.service.FriendService;
import cn.wzjun1.yeimServer.service.OnlineChannel;
import cn.wzjun1.yeimServer.utils.Common;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wzjun1
 * @description 针对表【friend(好友表)】的数据库操作Service实现
 * @createDate 2023-04-04 10:48:49
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend>
        implements FriendService {

    @Autowired
    OnlineChannel onlineChannel;

    /**
     * 删除好友
     *
     * @param deleteFriendDTO
     */
    @Override
    public void deleteFriend(DeleteFriendDTO deleteFriendDTO) {
        //双向删除
        deleteFriendDTO.getMembers().forEach(id -> {
            this.remove(new QueryWrapper<Friend>().eq("user_id", LoginUserContext.getUser().getUserId()).eq("friend_user_id", id));
            this.remove(new QueryWrapper<Friend>().eq("friend_user_id", LoginUserContext.getUser().getUserId()).eq("user_id", id));
            onlineChannel.send(LoginUserContext.getUser().getUserId(), Result.info(StatusCode.FRIEND_LIST_CHANGED.getCode(), StatusCode.FRIEND_LIST_CHANGED.getDesc(), null).toJSONString());
            onlineChannel.send(id, Result.info(StatusCode.FRIEND_LIST_CHANGED.getCode(), StatusCode.FRIEND_LIST_CHANGED.getDesc(), null).toJSONString());
        });
    }

    @Override
    public void updateFriend(UpdateFriendDTO updateFriendDTO) {
        Friend exist = this.getOne(new QueryWrapper<Friend>().eq("user_id", LoginUserContext.getUser().getUserId()).eq("friend_user_id", updateFriendDTO.getUserId()));
        if (exist == null || exist.getId() == 0) {
            throw new FriendNotFoundException("他不是您的好友，无法更新资料");
        }
        Friend update = new Friend();
        if (updateFriendDTO.getRemark() != null) {
            update.setRemark(updateFriendDTO.getRemark());
        }
        if (updateFriendDTO.getExtend() != null) {
            update.setExtend(updateFriendDTO.getExtend());
        }
        if (!Common.isNotEmptyBean(update)) {
            throw new ParamsException("请至少传入一个参数更新");
        }
        this.update(update, new QueryWrapper<Friend>().eq("id", exist.getId()));
        //触发好友列表更新事件
        onlineChannel.send(LoginUserContext.getUser().getUserId(), Result.info(StatusCode.FRIEND_LIST_CHANGED.getCode(), StatusCode.FRIEND_LIST_CHANGED.getDesc(), null).toJSONString());
    }
}




