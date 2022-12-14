package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.domain.ConversationV0;
import cn.wzjun1.yeimServer.domain.GroupMessage;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.service.GroupMessageService;
import cn.wzjun1.yeimServer.service.MessageService;
import cn.wzjun1.yeimServer.socket.cons.ConversationType;
import cn.wzjun1.yeimServer.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
                throw new Exception("发送者/接收者ID错误");
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
            Message update = new Message();
            update.setIsRevoke(1);
            //根据消息ID更新消息（两条）
            messageService.updatePrivateMessageById(update, LoginUserContext.getUser().getUserId(), messageId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取历史消息
     *
     * @param page           页码
     * @param conversationId 会话ID
     * @return IPage<Message>
     */
    @UserAuthorization
    @GetMapping(path = "/message/list")
    @Validated
    public Result list(@RequestParam(defaultValue = "1") Integer page, @RequestParam @NotEmpty String conversationId) {
        try {
            ConversationV0 conversationV0 = conversationService.getConversation(conversationId, LoginUserContext.getUser().getUserId());
            if (conversationV0 == null) {
                throw new Exception("会话不存在");
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

}
