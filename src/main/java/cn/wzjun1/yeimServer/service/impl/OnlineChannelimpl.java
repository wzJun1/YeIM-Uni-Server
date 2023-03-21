package cn.wzjun1.yeimServer.service.impl;

import cn.wzjun1.yeimServer.service.OnlineChannel;
import cn.wzjun1.yeimServer.socket.WebSocket;
import org.springframework.stereotype.Service;

/**
 * 实时消息通道实现类
 */
@Service
public class OnlineChannelimpl implements OnlineChannel {

    /**
     * 通过WebSocket发送消息
     *
     * @param userId
     * @param data
     */
    @Override
    public void send(String userId, String data) {
        WebSocket.sendMessage(userId, data);
    }
}
