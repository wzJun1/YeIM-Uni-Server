package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.constant.StatusCode;
import cn.wzjun1.yeimServer.domain.ConversationV0;
import cn.wzjun1.yeimServer.domain.GroupMessage;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.exception.conversation.ConversationNotFoundException;
import cn.wzjun1.yeimServer.exception.friend.FriendNotFoundException;
import cn.wzjun1.yeimServer.exception.group.GroupAllMuteException;
import cn.wzjun1.yeimServer.exception.group.GroupMuteException;
import cn.wzjun1.yeimServer.exception.group.GroupNotFoundException;
import cn.wzjun1.yeimServer.exception.group.NoGroupUserException;
import cn.wzjun1.yeimServer.exception.message.IdException;
import cn.wzjun1.yeimServer.exception.message.MessageRejectedException;
import cn.wzjun1.yeimServer.exception.message.ToUserIdNotFoundException;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.service.GroupMessageService;
import cn.wzjun1.yeimServer.service.MessageService;
import cn.wzjun1.yeimServer.constant.ConversationType;
import cn.wzjun1.yeimServer.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Validated
@RestController
public class MessageController {

    @Autowired
    MessageService messageService;

    @Autowired
    GroupMessageService groupMessageService;

    @Autowired
    ConversationService conversationService;

    /**
     * 发送消息
     *
     * @param params MessageSaveDTO
     * @return Result
     */
    @UserAuthorization
    @PostMapping(path = "/message/save")
    public Result save(@RequestBody @Validated MessageSaveDTO params) {
        try {
            User user = LoginUserContext.getUser();
            if (!user.getUserId().equals(params.getFrom()) || user.getUserId().equals(params.getTo())) {
                throw new IdException("发送者/接收者ID错误");
            }
            if (params.getConversationType().equals(ConversationType.PRIVATE)) {
                Message message = messageService.insertMessage(user, params);
                if (message == null) {
                    throw new Exception("插入数据库失败");
                }
                return Result.success(message);
            } else {
                GroupMessage message = groupMessageService.insertGroupMessage(user, params);
                if (message == null) {
                    throw new Exception("插入数据库失败");
                }
                return Result.success(message);
            }
        } catch (Exception e) {
            if (e instanceof ToUserIdNotFoundException) {
                return Result.error(StatusCode.TOUSER_ID_NOT_FOUND);
            } else if (e instanceof MessageRejectedException) {
                return Result.error(StatusCode.MESSAGE_REJECTED);
            } else if (e instanceof IdException) {
                return Result.error(StatusCode.ID_ERROR.getCode(), e.getMessage());
            } else if (e instanceof GroupAllMuteException) {
                return Result.error(StatusCode.GROUP_ALL_MUTE);
            } else if (e instanceof GroupMuteException) {
                return Result.error(StatusCode.GROUP_MUTE);
            } else if (e instanceof NoGroupUserException) {
                return Result.error(StatusCode.NO_GROUP_USER);
            } else if (e instanceof GroupNotFoundException) {
                return Result.error(StatusCode.GROUP_NOT_FOUND);
            } else if (e instanceof FriendNotFoundException) {
                return Result.error(StatusCode.FRIEND_NOT_FOUND.getCode(), e.getMessage());
            } else {
                return Result.error(e.getMessage());
            }
        }
    }

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @return Result
     */
    @UserAuthorization
    @GetMapping(path = "/message/delete")
    public Result delete(@RequestParam @NotEmpty String messageId) {
        try {
            //根据消息ID删除消息
            messageService.deleteMessage(LoginUserContext.getUser().getUserId(), messageId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @return Result
     */
    @UserAuthorization
    @GetMapping(path = "/message/revoke")
    public Result revoke(@RequestParam @NotEmpty String messageId) {
        try {
            //根据消息ID撤回消息
            messageService.revokeMessage(LoginUserContext.getUser().getUserId(), messageId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * @param page           页码
     * @param conversationId 会话ID
     * @return IPage<Message>
     * @deprecated 获取历史消息
     */
    @UserAuthorization
    @GetMapping(path = "/message/list")
    @Validated
    public Result list(@RequestParam(defaultValue = "1") Integer page, @RequestParam @NotEmpty String conversationId) {
        try {
            ConversationV0 conversationV0 = conversationService.getConversation(conversationId, LoginUserContext.getUser().getUserId());
            if (conversationV0 == null) {
                throw new ConversationNotFoundException("会话不存在");
            }
            if (conversationV0.getType().equals(ConversationType.PRIVATE)) {
                //私聊消息
                IPage<Message> messages = messageService.listMessage(Page.of(page, 20), LoginUserContext.getUser().getUserId(), conversationId);
                return Result.success(messages);
            } else {
                //群聊消息
                IPage<GroupMessage> messages = groupMessageService.listMessage(Page.of(page, 20), conversationId);
                return Result.success(messages);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    @UserAuthorization
    @GetMapping(path = "/v117/message/list")
    @Validated
    public Result list(@RequestParam @NotEmpty String conversationId, @RequestParam(defaultValue = "") String nextMessageId, @RequestParam(defaultValue = "20") Integer limit) {
        try {
            //修复因js端空值导致获取消息异常
            if (nextMessageId.equals("null")){
                nextMessageId = "";
            }
            ConversationV0 conversationV0 = conversationService.getConversation(conversationId, LoginUserContext.getUser().getUserId());
            if (conversationV0 == null) {
                throw new ConversationNotFoundException("会话不存在");
            }
            if (conversationV0.getType().equals(ConversationType.PRIVATE)) {
                //私聊消息
                List<Message> messages = messageService.listMessage(conversationId, nextMessageId, limit);
                return Result.success(new HashMap<String, Object>() {{
                    put("records", messages);
                    if (messages.size() > 0) {
                        put("nextMessageId", messages.get(messages.size() - 1).getMessageId());
                    }
                }});
            } else {
                //群聊消息
                List<GroupMessage> messages = groupMessageService.listMessage(conversationId, nextMessageId, limit);
                return Result.success(new HashMap<String, Object>() {{
                    put("records", messages);
                    if (messages.size() > 0) {
                        put("nextMessageId", messages.get(messages.size() - 1).getMessageId());
                    }
                }});
            }
        } catch (Exception e) {
            if (e instanceof ConversationNotFoundException) {
                return Result.error(StatusCode.CONVERSATION_NOT_FOUND);
            } else if (e instanceof GroupNotFoundException) {
                return Result.error(StatusCode.GROUP_NOT_FOUND);
            } else if (e instanceof NoGroupUserException) {
                return Result.error(StatusCode.NO_GROUP_USER);
            }
            return Result.error(e.getMessage());
        }
    }

}
