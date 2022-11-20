package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.entity.SocketMessage;
import cn.wzjun1.yeimServer.pojo.message.MessageSavePojo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author wzjun1
* @description 针对表【message】的数据库操作Service
* @createDate 2022-11-16 23:29:12
*/
public interface MessageService extends IService<Message> {
    Message formatMessage(SocketMessage message);
    int insertMessage(SocketMessage message);
    Message insertMessage(User user, MessageSavePojo params);



    IPage<Message> listMessage(IPage<Message> page, String userId, String conversationId);
}
