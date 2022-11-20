package cn.wzjun1.yeimServer.socket;

import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.service.WebSocketService;
import cn.wzjun1.yeimServer.socket.cons.SocketStatusCode;
import cn.wzjun1.yeimServer.utils.RedisUtil;
import cn.wzjun1.yeimServer.utils.Result;
import cn.wzjun1.yeimServer.utils.SpringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
@ServerEndpoint(value = "/im/{userId}/{token}")
public class WebSocket {

    @Autowired
    RedisUtil redisUtil = SpringUtils.getBean(RedisUtil.class);

    @Autowired
    WebSocketService webSocketService = SpringUtils.getBean(WebSocketService.class);

    /**
     * 会话
     */
    private Session session;

    /**
     * 用户ID
     */
    private String userId;

    private static CopyOnWriteArraySet<WebSocket> webSockets = new CopyOnWriteArraySet<>();

    private static ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<String, Session>();


    /**
     * 连接成功，验证用户
     * url参数传递userId、token
     * 通过对比redis中token，判断此用户是否能够登陆socket端
     * 跟UserController.java中getToken联合（用户登陆体系完全可以根据实际情况自行修改）
     */
    @OnOpen
    public void onOpen(Session _session, @PathParam(value = "userId") String _userId, @PathParam(value = "token") String _token) {
        try {
            if (!redisUtil.hasKey(_token)) {
                //token不存在，验证失败
                sendMessage(_session, Result.error(SocketStatusCode.TOKEN_ERROR.getCode(), SocketStatusCode.TOKEN_ERROR.getDesc()).toJSONString());
                _session.close();
            } else {
                User user = (User) redisUtil.get(_token);
                if (!_userId.equals(user.getUserId())) {
                    sendMessage(_session, Result.error(SocketStatusCode.TOKEN_ERROR.getCode(), SocketStatusCode.TOKEN_ERROR.getDesc()).toJSONString());
                    _session.close();
                    return;
                }
                //检测是否在线,踢掉
                Session online = sessionPool.get(_userId);
                if (online != null) {
                    sendMessage(_session, Result.error(SocketStatusCode.KICKED_OUT.getCode(), SocketStatusCode.KICKED_OUT.getDesc()).toJSONString());
                    online.close();
                }
                session = _session;
                userId = _userId;
                webSockets.add(this);
                sessionPool.put(userId, session);
                sendMessage(_session, Result.info(SocketStatusCode.LOGIN_SUCCESS.getCode(), SocketStatusCode.LOGIN_SUCCESS.getDesc(), user).toJSONString());
                log.info("【YeIMServer】有新用户：" + user.getNickname() + "_" + _userId + "的连接，现存总数为:" + webSockets.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendMessage(_session, Result.error(SocketStatusCode.CONNECT_ERROR.getCode(), SocketStatusCode.CONNECT_ERROR.getDesc()).toJSONString());
                _session.close();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /**
     * 链接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        try {
            webSockets.remove(this);
            sessionPool.remove(userId);
            log.info("【YeIMServer】用户：" + userId + "连接断开，总数为:" + webSockets.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param msgStr
     */
    @OnMessage
    public void onMessage(String msgStr) {

        if (session == null) {
            return;
        }

        if (!isJSONObject(msgStr)) {
            return;
        }

        JSONObject msgObj = JSONObject.parseObject(msgStr);

        if (!msgObj.containsKey("type")) {
            log.info("【YeIMServer】消息缺失：type");
            return;
        }

        if (!msgObj.containsKey("data")) {
            log.info("【YeIMServer】消息缺失：data");
            return;
        }
        String type = msgObj.getString("type");
        String data = msgObj.getString("data");

        //心跳
        if (type.equals("heart")) {
            session.getAsyncRemote().sendText(Result.info(SocketStatusCode.HEART.getCode(), SocketStatusCode.HEART.getDesc(), "pong").toJSONString());
        } else if (type.equals("received_call")) {
            //用户接收到socket消息回调
            try {
                webSocketService.receivedCallMessage(userId, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (type.equals("message")) {
            log.info("【YeIMServer】收到一条IM消息：" + data);
            //消息
            try {
                Message messageObj = JSONObject.parseObject(data, Message.class);
                String to = messageObj.getTo();
                Session online = sessionPool.get(to);
                if (online != null) {
                    messageObj.setConversationId(messageObj.getFrom());
                    online.getAsyncRemote().sendText(Result.success(messageObj).toJSONString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送错误时的处理
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.info("【YeIMServer】发生错误：" + error.getMessage());
        error.printStackTrace();
    }

    //通过userId发送消息
    public static void sendMessage(String userId, String message) {
        Session session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            try {
                log.info("【YeIMServer】通过userId发送消息(userId: " + userId + ")：" + message);
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.info("【YeIMServer】userId:" + userId + "不在线，无法通过socket发送");
        }
    }

    //通过session发送消息
    public void sendMessage(Session session, String message) {
        if (session != null && session.isOpen()) {
            try {
                log.info("【YeIMServer】通过session发送消息(sessionId: " + session.getId() + ")：" + message);
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.info("【YeIMServer】session:" + session.getId() + "不在线，无法通过socket发送");
        }
    }

    /**
     * 检测字符串是否是json对象
     *
     * @param str
     * @return
     */
    private static boolean isJSONObject(String str) {
        boolean result = false;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

}

