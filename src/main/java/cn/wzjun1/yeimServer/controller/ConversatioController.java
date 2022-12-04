package cn.wzjun1.yeimServer.controller;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.domain.Conversation;
import cn.wzjun1.yeimServer.domain.ConversationV0;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.interceptor.LoginUserContext;
import cn.wzjun1.yeimServer.interceptor.UserAuthorizationInterceptor;
import cn.wzjun1.yeimServer.mapper.ConversationMapper;
import cn.wzjun1.yeimServer.mapper.MessageMapper;
import cn.wzjun1.yeimServer.service.ConversationService;
import cn.wzjun1.yeimServer.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
public class ConversatioController {

    @Autowired
    MessageMapper messageMapper;
    @Autowired
    ConversationMapper conversationMapper;

    @Autowired
    ConversationService conversationService;

    /**
     * 删除会话及聊天记录
     *
     * @param conversationId 会话ID
     * @return Result
     */
    @UserAuthorization
    @GetMapping(path = "/conversation/delete")
    public Result delete(@RequestParam @NotNull String conversationId) {
        try {
            //删除会话
            conversationMapper.delete(new QueryWrapper<Conversation>().eq("conversation_id", conversationId));
            //软删除会话内聊天记录
            Message update = new Message();
            update.setIsDeleted(1);
            messageMapper.update(update, new QueryWrapper<Message>().eq("user_id", LoginUserContext.getUser().getUserId()).eq("conversation_id", conversationId));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取会话列表
     *
     * @param page    页码
     * @param limit   每页数量
     *
     * @return IPage<ConversationV0>
     */
    @UserAuthorization
    @GetMapping(path = "/conversation/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer limit) {
        try {
            IPage<ConversationV0> conversationV0s = conversationMapper.listConversationV0(Page.of(page, limit), LoginUserContext.getUser().getUserId());
            return Result.success(conversationV0s);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 清除会话未读数并设置会话内接收消息已读状态
     * 更新完成后发送事件 `PRIVATE_READ_RECEIPT`
     * @param conversationId 会话ID
     * @return Result
     */
    @UserAuthorization
    @GetMapping(path = "/conversation/update/unread")
    public Result clearConversationUnread(@RequestParam @NotNull String conversationId) {
        try {
            conversationService.clearConversationUnread(conversationId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}
