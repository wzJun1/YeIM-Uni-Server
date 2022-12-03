package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.service.MessageService;
import cn.wzjun1.yeimServer.service.WebSocketService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wzjun1
 */
@Service
@Slf4j
public class WebsocketServiceImpl implements WebSocketService {

    @Autowired
    MessageService messageService;

    @Override
    public void receivedCallMessage(String userId, String message) throws Exception {
        JSONObject messageObj = JSONObject.parseObject(message);
        if (!messageObj.containsKey("messageId")) {
            throw new Exception("messageId 参数错误");
        }
        String messageId = messageObj.getString("messageId");
        long exist = messageService.count(new QueryWrapper<Message>().eq("message_id", messageId).eq("`to`", userId).eq("receive", 0));
        if (exist > 0) {
            Message update = new Message();
            update.setReceive(1);
            //根据消息ID更新消息（两条）
            messageService.updatePrivateMessageById(update, messageId);
        }
    }
}




