package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.GroupMessage;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wzjun1
 * @description 针对表【message】的数据库操作Service
 * @createDate 2022-11-16 23:29:12
 */
public interface MessageService extends IService<Message> {

    Message insertMessage(User user, MessageSaveDTO params) throws Exception;

    @Deprecated
    void updatePrivateMessageById(Message update, String userId, String messageId) throws Exception;

    void deleteMessage(String userId, String messageId) throws Exception;

    void revokeMessage(String userId, String messageId) throws Exception;

    IPage<Message> listMessage(IPage<Message> page, String userId, String conversationId);

    List<Message> listMessage(String conversationId, String nextMessageId, Integer limit);

}
