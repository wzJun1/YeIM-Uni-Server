package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.interceptor.UserAuthorizationInterceptor;
import cn.wzjun1.yeimServer.pojo.message.MessageSavePojo;
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

    @UserAuthorization
    @PostMapping(path = "/message/save")
    public Result save(@RequestBody @Validated MessageSavePojo params, HttpServletRequest request) {
        try {
            User user = (User) request.getAttribute(UserAuthorizationInterceptor.REQUEST_TOKEN_USER);
            if (!user.getUserId().equals(params.getFrom())){
                throw new Exception("from error");
            }
            Message message = messageService.insertMessage(user, params);
            if (message == null){
                throw new Exception("insert error");
            }
            return Result.success(message);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    @UserAuthorization
    @GetMapping(path = "/message/list")
    @Validated
    public Result list(@RequestParam(defaultValue = "1") Integer page, @RequestParam @NotNull(message = "conversationId must be not null") String conversationId, HttpServletRequest request) {
        try {
            IPage<Message> messages = messageService.listMessage(Page.of(page, 20),request.getAttribute(UserAuthorizationInterceptor.REQUEST_TOKEN_USER_ID).toString(),conversationId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
