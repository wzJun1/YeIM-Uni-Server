package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.constant.*;
import cn.wzjun1.yeimServer.domain.*;
import cn.wzjun1.yeimServer.dto.friend.AddFriendDTO;
import cn.wzjun1.yeimServer.dto.friend.DeleteFriendDTO;
import cn.wzjun1.yeimServer.exception.FrequencyLimitException;
import cn.wzjun1.yeimServer.exception.ParamsException;
import cn.wzjun1.yeimServer.exception.friend.ApplyNeedException;
import cn.wzjun1.yeimServer.exception.friend.DuplicateException;
import cn.wzjun1.yeimServer.exception.friend.FriendApplyNotFoundException;
import cn.wzjun1.yeimServer.exception.user.UserFriendDenyException;
import cn.wzjun1.yeimServer.exception.user.UserFriendDuplicateException;
import cn.wzjun1.yeimServer.exception.user.UserNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.FriendApplyMapper;
import cn.wzjun1.yeimServer.mapper.MessageMapper;
import cn.wzjun1.yeimServer.mapper.UserMapper;
import cn.wzjun1.yeimServer.result.Result;
import cn.wzjun1.yeimServer.service.*;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yitter.idgen.YitIdHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author wzjun1
 */
@Service
public class FriendApplyServiceImpl extends ServiceImpl<FriendApplyMapper, FriendApply>
        implements FriendApplyService {

    @Autowired
    UserService userService;

    @Autowired
    FriendService friendService;

    @Autowired
    FriendApplyMapper friendApplyMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ConversationService conversationService;

    @Autowired
    MessageService messageService;

    @Autowired
    MessageMapper messageMapper;

    @Autowired
    AsyncService asyncService;

    @Autowired
    OnlineChannel onlineChannel;

    /**
     * 添加好友，处理申请
     *
     * @param addFriendDTO
     */
    @Override
    public void addFriend(AddFriendDTO addFriendDTO) {

        User user = userService.getUserById(addFriendDTO.getUserId());
        if (Objects.isNull(user)) {
            throw new UserNotFoundException("申请添加的用户不存在");
        }

        if (addFriendDTO.getUserId().equals(LoginUserContext.getUser().getUserId())){
            throw new ParamsException("要添加的用户ID错误");
        }

        //判断当前用户是否已存在对方好友
        Friend nowUserFriend = friendService.getOne(new QueryWrapper<Friend>().eq("user_id", LoginUserContext.getUser().getUserId()).eq("friend_user_id", addFriendDTO.getUserId()));

        //对方好友列表是否存在当前用户
        Friend sideFriend = friendService.getOne(new QueryWrapper<Friend>().eq("user_id", addFriendDTO.getUserId()).eq("friend_user_id", LoginUserContext.getUser().getUserId()));

        //双向好友均存在，不能重复添加
        if (nowUserFriend != null && nowUserFriend.getId() != 0 && sideFriend != null && sideFriend.getId() != 0) {
            throw new UserFriendDuplicateException("添加的用户已经是好友，请勿重复添加");
        } else if (nowUserFriend == null && sideFriend != null && sideFriend.getId() != 0) {
            //当前用户好友列表中没有对方，但对方中有当前用户，不再判断好友的添加方式，直接单向添加好友
            Friend friend = new Friend();
            friend.setFriendUserId(addFriendDTO.getUserId());
            friend.setUserId(LoginUserContext.getUser().getUserId());
            friend.setCreatedAt(System.currentTimeMillis());
            if (addFriendDTO.getRemark() != null) {
                friend.setRemark(addFriendDTO.getRemark());
            }
            friendService.save(friend);
            //发送当前用户的好友列表更新事件（仅发送事件给前端SDK，前端SDK接收事件调用http获取最新好友列表并同步触发好友列表更新事件）
            onlineChannel.send(LoginUserContext.getUser().getUserId(), Result.info(StatusCode.FRIEND_LIST_CHANGED.getCode(), StatusCode.FRIEND_LIST_CHANGED.getDesc(), null).toJSONString());

            //给当前用户发送好友添加系统消息
            String messageId = YitIdHelper.nextId() + "-" + System.currentTimeMillis();
            String inMessageId = messageId + "-2";
            Message out = new Message();
            out.setMessageId(inMessageId);
            out.setUserId(LoginUserContext.getUser().getUserId());
            out.setConversationId(addFriendDTO.getUserId());
            out.setDirection("in");
            out.setType(MessageType.SYS_NOTICE); //系统消息
            out.setFrom("0"); //系统消息
            out.setTo(LoginUserContext.getUser().getUserId());
            out.setIsRead(0);
            out.setIsRevoke(0);
            out.setStatus(MessageStatus.SUCCESS);
            out.setTime(System.currentTimeMillis());
            out.setBody(new JSONObject(new HashMap<String, Object>() {{
                put("tips", "你们已经成为好友了，快来聊天吧");
            }}));
            out.setExtra("");
            boolean messageResult = messageService.save(out);

            if (messageResult) {
                //更新当前用户会话
                conversationService.updateConversation(LoginUserContext.getUser().getUserId(), addFriendDTO.getUserId(), ConversationType.PRIVATE, inMessageId, 0, true);
                //给接收方推送在线消息[Socket Message]
                Message inResultMessage = messageMapper.getMessageById(inMessageId, LoginUserContext.getUser().getUserId());
                asyncService.emitJSSDKMessageReceive(inResultMessage);
            }

        } else {

            //判断对方的添加方式

            //拒绝
            if (user.getAddFriendType().equals(AddFriendType.DENY)) {
                throw new UserFriendDenyException("对方拒绝添加好友");
            }

            //好友允许直接添加，给对方关系链中插入当前用户
            if (user.getAddFriendType().equals(AddFriendType.ALLOW)) {

                Friend friend1 = new Friend();
                friend1.setFriendUserId(LoginUserContext.getUser().getUserId());
                friend1.setUserId(addFriendDTO.getUserId());
                friend1.setCreatedAt(System.currentTimeMillis());
                try {
                    friendService.save(friend1);
                    //给双方发送好友列表更新事件
                    onlineChannel.send(LoginUserContext.getUser().getUserId(), Result.info(StatusCode.FRIEND_LIST_CHANGED.getCode(), StatusCode.FRIEND_LIST_CHANGED.getDesc(), null).toJSONString());
                } catch (Exception e) {
                    //
                }

                Friend friend2 = new Friend();
                friend2.setFriendUserId(addFriendDTO.getUserId());
                friend2.setUserId(LoginUserContext.getUser().getUserId());
                if (addFriendDTO.getRemark() != null) {
                    friend2.setRemark(addFriendDTO.getRemark());
                }
                friend2.setCreatedAt(System.currentTimeMillis());
                try {
                    friendService.save(friend2);
                    //给双方发送好友列表更新事件
                    onlineChannel.send(addFriendDTO.getUserId(), Result.info(StatusCode.FRIEND_LIST_CHANGED.getCode(), StatusCode.FRIEND_LIST_CHANGED.getDesc(), null).toJSONString());
                } catch (Exception e) {
                    //
                }

                //给当前用户发送好友添加系统消息
                String messageId = YitIdHelper.nextId() + "-" + System.currentTimeMillis();
                String inMessageId = messageId + "-2";
                Message out = new Message();
                out.setMessageId(inMessageId);
                out.setUserId(LoginUserContext.getUser().getUserId());
                out.setConversationId(addFriendDTO.getUserId());
                out.setDirection("in");
                out.setType(MessageType.SYS_NOTICE); //系统消息
                out.setFrom("0"); //系统消息
                out.setTo(LoginUserContext.getUser().getUserId());
                out.setIsRead(0);
                out.setIsRevoke(0);
                out.setStatus(MessageStatus.SUCCESS);
                out.setTime(System.currentTimeMillis());
                out.setBody(new JSONObject(new HashMap<String, Object>() {{
                    put("tips", "你们已经成为好友了，快来聊天吧");
                }}));
                out.setExtra("");
                boolean messageResult = messageService.save(out);

                if (messageResult) {
                    //更新当前用户会话
                    conversationService.updateConversation(LoginUserContext.getUser().getUserId(), addFriendDTO.getUserId(), ConversationType.PRIVATE, inMessageId, 0, true);
                    //给接收方推送在线消息[Socket Message]
                    Message inResultMessage = messageMapper.getMessageById(inMessageId, LoginUserContext.getUser().getUserId());
                    asyncService.emitJSSDKMessageReceive(inResultMessage);
                }

                //给对方发送好友添加系统消息
                String messageId2 = YitIdHelper.nextId() + "-" + System.currentTimeMillis();
                String inMessageId2 = messageId2 + "-2";
                Message out2 = new Message();
                out2.setMessageId(inMessageId2);
                out2.setUserId(addFriendDTO.getUserId());
                out2.setConversationId(LoginUserContext.getUser().getUserId());
                out2.setDirection("in");
                out2.setType(MessageType.SYS_NOTICE); //系统消息
                out2.setFrom("0"); //系统消息
                out2.setTo(addFriendDTO.getUserId());
                out2.setIsRead(0);
                out2.setIsRevoke(0);
                out2.setStatus(MessageStatus.SUCCESS);
                out2.setTime(System.currentTimeMillis());
                out2.setBody(new JSONObject(new HashMap<String, Object>() {{
                    put("tips", "你们已经成为好友了，快来聊天吧");
                }}));
                out2.setExtra("");
                boolean messageResult2 = messageService.save(out2);
                if (messageResult2) {
                    //更新当前用户会话
                    conversationService.updateConversation(addFriendDTO.getUserId(), LoginUserContext.getUser().getUserId(), ConversationType.PRIVATE, inMessageId2, 0, true);
                    //给接收方推送在线消息[Socket Message]
                    Message inResultMessage = messageMapper.getMessageById(inMessageId2, addFriendDTO.getUserId());
                    asyncService.emitJSSDKMessageReceive(inResultMessage);
                }

            } else {
                //需要发送申请等待同意才能添加

                FriendApply exist = this.getOne(new QueryWrapper<FriendApply>().eq("apply_user_id", LoginUserContext.getUser().getUserId()).eq("user_id", addFriendDTO.getUserId()).orderByDesc("created_at").last("limit 1"));

                if (exist != null && exist.getId() != 0) {
                    System.out.println(JSONObject.toJSONString(exist));
                    //如果发过请求，检查一下请求时间，60分钟内只能发送一条。
                    if (System.currentTimeMillis() - exist.getCreatedAt() < 3600 * 1000) {
                        throw new FrequencyLimitException("同一个好友添加申请只能每小时申请一次");
                    }
                }

                FriendApply friendApply = new FriendApply();
                friendApply.setApplyUserId(LoginUserContext.getUser().getUserId());
                friendApply.setUserId(addFriendDTO.getUserId());
                friendApply.setStatus(FriendApplyStatus.PENDING);
                friendApply.setCreatedAt(System.currentTimeMillis());
                if (addFriendDTO.getRemark() != null) {
                    friendApply.setRemark(addFriendDTO.getRemark());
                }
                if (addFriendDTO.getExtraMessage() != null) {
                    friendApply.setExtraMessage(addFriendDTO.getExtraMessage());
                }
                this.save(friendApply);
                //给对方发送添加好友申请列表更新事件
                onlineChannel.send(addFriendDTO.getUserId(), Result.info(StatusCode.FRIEND_APPLY_LIST_CHANGED.getCode(), StatusCode.FRIEND_APPLY_LIST_CHANGED.getDesc(), null).toJSONString());

                throw new ApplyNeedException("已发送好友申请，请等待对方处理");
            }
        }
    }

    /**
     * 同意好友申请
     *
     * @param id     申请ID
     * @param remark 备注
     */
    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    public void acceptApply(Integer id, String remark) {

        FriendApply exist = this.getById(id);
        if (exist == null) {
            throw new FriendApplyNotFoundException("申请ID错误");
        }

        //如果不是等待处理的，忽略
        if (!exist.getStatus().equals(FriendApplyStatus.PENDING)) {
            throw new DuplicateException("已经处理过此申请，请勿重复处理");
        }

        //查询用户是否存在
        User isUserExist = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", exist.getUserId()));
        if (isUserExist == null || isUserExist.getUserId() == null) {
            throw new UserNotFoundException("被添加的用户不存在");
        }

        //申请者的关系链
        Friend friend1 = new Friend();
        friend1.setFriendUserId(exist.getUserId());
        friend1.setUserId(exist.getApplyUserId());
        friend1.setCreatedAt(System.currentTimeMillis());
        if (exist.getRemark() != null) {
            friend1.setRemark(exist.getRemark());
        }
        boolean friendResult = friendService.save(friend1);

        //被申请者的关系链
        Friend friend2 = new Friend();
        friend2.setFriendUserId(exist.getApplyUserId());
        friend2.setUserId(exist.getUserId());
        if (remark != null && !remark.equals("")) {
            friend2.setRemark(remark);
        }
        friend2.setCreatedAt(System.currentTimeMillis());
        boolean friendResult2 = friendService.save(friend2);

        //给申请用户发送好友添加同意系统消息
        String messageId = YitIdHelper.nextId() + "-" + System.currentTimeMillis();
        String inMessageId = messageId + "-2";
        Message out = new Message();
        out.setMessageId(inMessageId);
        out.setUserId(exist.getApplyUserId());
        out.setConversationId(exist.getUserId());
        out.setDirection("in");
        out.setType(MessageType.SYS_NOTICE); //系统消息
        out.setFrom("0"); //系统消息
        out.setTo(exist.getApplyUserId());
        out.setIsRead(0);
        out.setIsRevoke(0);
        out.setStatus(MessageStatus.SUCCESS);
        out.setTime(System.currentTimeMillis());
        out.setBody(new JSONObject(new HashMap<String, Object>() {{
            put("tips", "你们已经成为好友了，快来聊天吧");
        }}));
        out.setExtra("");
        boolean messageResult = messageService.save(out);

        //给被申请人发送好友添加系统消息
        String messageId2 = YitIdHelper.nextId() + "-" + System.currentTimeMillis();
        String inMessageId2 = messageId2 + "-2";
        Message out2 = new Message();
        out2.setMessageId(inMessageId2);
        out2.setUserId(exist.getUserId());
        out2.setConversationId(exist.getApplyUserId());
        out2.setDirection("in");
        out2.setType(MessageType.SYS_NOTICE); //系统消息
        out2.setFrom("0"); //系统消息
        out2.setTo(exist.getUserId());
        out2.setIsRead(0);
        out2.setIsRevoke(0);
        out2.setStatus(MessageStatus.SUCCESS);
        out2.setTime(System.currentTimeMillis());
        out2.setBody(new JSONObject(new HashMap<String, Object>() {{
            put("tips", "你们已经成为好友了，快来聊天吧");
        }}));
        out2.setExtra("");
        boolean messageResult2 = messageService.save(out2);

        //修改申请状态
        FriendApply update = new FriendApply();
        update.setStatus(FriendApplyStatus.AGREE);//同意
        update.setTransformTime(System.currentTimeMillis());
        update.setIsRead(1);
        this.update(update, new QueryWrapper<FriendApply>().eq("id", exist.getId()));

        //更新申请用户会话
        conversationService.updateConversation(exist.getApplyUserId(), exist.getUserId(), ConversationType.PRIVATE, inMessageId, 0, true);
        //给接收方推送在线消息[Socket Message]
        Message inResultMessage = messageMapper.getMessageById(inMessageId, exist.getApplyUserId());
        asyncService.emitJSSDKMessageReceive(inResultMessage);

        //更新被申请用户会话
        conversationService.updateConversation(exist.getUserId(), exist.getApplyUserId(), ConversationType.PRIVATE, inMessageId2, 0, true);
        //给接收方推送在线消息[Socket Message]
        Message inResultMessage2 = messageMapper.getMessageById(inMessageId2, exist.getUserId());
        asyncService.emitJSSDKMessageReceive(inResultMessage2);

        //给双方发送好友列表更新事件
        onlineChannel.send(exist.getApplyUserId(), Result.info(StatusCode.FRIEND_LIST_CHANGED.getCode(), StatusCode.FRIEND_LIST_CHANGED.getDesc(), null).toJSONString());

        //给双方发送好友列表更新事件
        onlineChannel.send(exist.getUserId(), Result.info(StatusCode.FRIEND_LIST_CHANGED.getCode(), StatusCode.FRIEND_LIST_CHANGED.getDesc(), null).toJSONString());

        //给操作方发送申请列表更新事件
        onlineChannel.send(exist.getUserId(), Result.info(StatusCode.FRIEND_APPLY_LIST_CHANGED.getCode(), StatusCode.FRIEND_APPLY_LIST_CHANGED.getDesc(), null).toJSONString());
    }

    @Override
    public void refuseApply(Integer id) {

        FriendApply exist = this.getById(id);
        if (exist == null) {
            throw new FriendApplyNotFoundException("申请ID错误");
        }

        //如果不是等待处理的，忽略
        if (!exist.getStatus().equals(FriendApplyStatus.PENDING)) {
            throw new DuplicateException("已经处理过此申请，请勿重复处理");
        }

        //修改申请状态
        FriendApply update = new FriendApply();
        update.setStatus(FriendApplyStatus.REFUSE);//拒绝
        update.setTransformTime(System.currentTimeMillis());
        update.setIsRead(1);
        this.update(update, new QueryWrapper<FriendApply>().eq("id", exist.getId()));

        //发送申请被拒绝事件
        onlineChannel.send(exist.getApplyUserId(), Result.info(StatusCode.FRIEND_APPLY_REFUSE.getCode(), StatusCode.FRIEND_APPLY_REFUSE.getDesc(), friendApplyMapper.fetchApplyById(exist.getId())).toJSONString());
    }


}




