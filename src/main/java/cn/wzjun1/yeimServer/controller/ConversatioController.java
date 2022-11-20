package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.domain.ConversationV0;
import cn.wzjun1.yeimServer.interceptor.UserAuthorizationInterceptor;
import cn.wzjun1.yeimServer.mapper.ConversationMapper;
import cn.wzjun1.yeimServer.utils.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class ConversatioController {

    @Autowired
    ConversationMapper conversationMapper;

    @UserAuthorization
    @GetMapping(path = "/conversation/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer limit, HttpServletRequest request) {
        try {
            IPage<ConversationV0> conversationV0s = conversationMapper.listConversationV0(Page.of(page, limit), request.getAttribute(UserAuthorizationInterceptor.REQUEST_TOKEN_USER_ID).toString());
            return Result.success(conversationV0s);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


}
