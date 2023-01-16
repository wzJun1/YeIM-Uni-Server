package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.constant.MessageType;
import cn.wzjun1.yeimServer.domain.*;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.mapper.ConversationMapper;
import cn.wzjun1.yeimServer.mapper.GroupMapper;
import cn.wzjun1.yeimServer.mapper.GroupUserMapper;
import cn.wzjun1.yeimServer.pojo.YeIMPushConfig;
import cn.wzjun1.yeimServer.service.AsyncService;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.service.PushService;
import cn.wzjun1.yeimServer.socket.WebSocket;
import cn.wzjun1.yeimServer.constant.ConversationType;
import cn.wzjun1.yeimServer.constant.MessageStatus;
import cn.wzjun1.yeimServer.constant.SocketStatusCode;
import cn.wzjun1.yeimServer.result.Result;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.wzjun1.yeimServer.service.GroupMessageService;
import cn.wzjun1.yeimServer.mapper.GroupMessageMapper;
import com.github.yitter.idgen.YitIdHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【group_message】的数据库操作Service实现
 * @createDate 2022-12-08 21:18:05
 */
@Service
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageMapper, GroupMessage>
        implements GroupMessageService {

    @Autowired
    GroupMapper groupMapper;

    @Autowired
    GroupUserMapper groupUserMapper;

    @Autowired
    GroupMessageMapper groupMessageMapper;

    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    ConversationService conversationService;

    @Autowired
    AsyncService asyncService;

    /**
     * 发送群消息
     *
     * @param user    当前操作者
     * @param message 消息
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = {RuntimeException.class, Exception.class})
    @Override
    public GroupMessage insertGroupMessage(User user, MessageSaveDTO message) throws Exception {
        try {

            //判断群组是否存在
            Group isExistGroup = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", message.getTo()).eq("is_dissolve", 0));
            if (isExistGroup == null || isExistGroup.getGroupId() == null) {
                throw new Exception("当前群组不存在或已解散");
            }

            boolean isAdmin = false;
            if (isExistGroup.getLeaderUserId().equals(user.getUserId())) {
                isAdmin = true;
            } else {
                GroupUser operatorGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", isExistGroup.getGroupId()).eq("user_id", user.getUserId()));
                if (operatorGroupUser != null && operatorGroupUser.getIsAdmin() == 1) {
                    isAdmin = true;
                }
            }

            //判断是否有发言权限(群主、管理员禁言状态下也可以发言)
            if (isExistGroup.getIsMute().equals(1) && !isAdmin) {
                throw new Exception("当前群组全体禁言");
            }

            GroupUser isGroupUser = groupUserMapper.selectOne(new QueryWrapper<GroupUser>().eq("group_id", message.getTo()).eq("user_id", user.getUserId()));
            if (isGroupUser == null || isGroupUser.getUserId() == null) {
                throw new Exception("非群成员无法给当前群组发送消息");
            }

            if (isGroupUser.getMuteEndTime() > System.currentTimeMillis() && !isAdmin) {
                throw new Exception("您在当前群组内已被禁言");
            }

            //1.插入消息到数据库

            //消息入库数据
            long time = System.currentTimeMillis();
            //统一消息ID
            String messageId = YitIdHelper.nextId() + "-" + time;
            //群消息的direction只有out

            //1.1 保存消息到数据库
            GroupMessage out = new GroupMessage();
            out.setMessageId(messageId);
            out.setUserId("");
            out.setConversationId(message.getTo());
            out.setDirection("out");
            out.setType(message.getType());
            out.setFrom(message.getFrom());
            out.setTo(message.getTo());
            out.setIsRead(0);
            out.setIsRevoke(0);
            out.setStatus(MessageStatus.SUCCESS);
            out.setTime(time);
            out.setBody(message.getBody());
            out.setExtra(message.getExtra());
            boolean messageResult = this.save(out);
            GroupMessage outResultMessage = groupMessageMapper.getMessageById(messageId, message.getTo());

            if (messageResult) {
                //2.更新多端会话
                ///异步批量更新群用户会话 + 转发事件
                asyncService.updateGroupConversationSendEvent(message.getTo(), outResultMessage);
                return outResultMessage;
            } else {
                throw new Exception("insertMessage error");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public IPage<GroupMessage> listMessage(IPage<GroupMessage> page, String conversationId) throws Exception {

        //判断群组是否存在
        Group group = groupMapper.selectOne(new QueryWrapper<Group>().eq("group_id", conversationId).eq("is_dissolve", 0));
        if (group == null || group.getGroupId() == null) {
            throw new Exception("当前群组不存在或已解散");
        }

        //判断是否有权限
        boolean isGroupUser = groupUserMapper.exists(new QueryWrapper<GroupUser>().eq("group_id", conversationId).eq("user_id", LoginUserContext.getUser().getUserId()));
        if (!isGroupUser) {
            throw new Exception("非群成员无法获取群聊天记录");
        }

        return groupMessageMapper.listMessage(page, conversationId);
    }


}




