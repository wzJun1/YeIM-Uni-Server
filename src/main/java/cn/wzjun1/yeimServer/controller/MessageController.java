package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.interceptor.UserAuthorizationInterceptor;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import cn.wzjun1.yeimServer.service.MessageService;
import cn.wzjun1.yeimServer.utils.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
public class MessageController {

    @Autowired
    MessageService messageService;

    /**
     * 发送消息
     *
     * @param params  MessageSaveDTO
     * @param request HttpServletRequest
     * @return Result
     */
    @UserAuthorization
    @PostMapping(path = "/message/save")
    public Result save(@RequestBody @Validated MessageSaveDTO params, HttpServletRequest request) {
        try {
            User user = (User) request.getAttribute(UserAuthorizationInterceptor.REQUEST_TOKEN_USER);
            if (!user.getUserId().equals(params.getFrom()) || user.getUserId().equals(params.getTo())) {
                throw new Exception("发送者/接收者ID错误");
            }
            Message message = messageService.insertMessage(user, params);
            if (message == null) {
                throw new Exception("插入数据库失败");
            }
            return Result.success(message);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 撤回消息
     *
     * @param messageId 消息ID
     * @param request   HttpServletRequest
     * @return Result
     */
    @UserAuthorization
    @GetMapping(path = "/message/revoke")
    @Validated
    public Result revoke(@RequestParam @NotNull String messageId, HttpServletRequest request) {
        try {
            Message update = new Message();
            update.setIsRevoke(1);
            //根据消息ID更新消息（两条）
            messageService.updatePrivateMessageById(update, messageId);
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
     * @param request        HttpServletRequest
     * @return IPage<Message>
     */
    @UserAuthorization
    @GetMapping(path = "/message/list")
    @Validated
    public Result list(@RequestParam(defaultValue = "1") Integer page, @RequestParam @NotNull(message = "conversationId must be not null") String conversationId, HttpServletRequest request) {
        try {
            IPage<Message> messages = messageService.listMessage(Page.of(page, 20), request.getAttribute(UserAuthorizationInterceptor.REQUEST_TOKEN_USER_ID).toString(), conversationId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
