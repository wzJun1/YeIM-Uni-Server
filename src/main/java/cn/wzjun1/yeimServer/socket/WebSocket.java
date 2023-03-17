package cn.wzjun1.yeimServer.socket;

import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.pojo.YeIMPushConfig;
import cn.wzjun1.yeimServer.constant.StatusCode;
import cn.wzjun1.yeimServer.utils.Common;
import cn.wzjun1.yeimServer.result.Result;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class WebSocket implements WebSocketHandler {

    private YeIMPushConfig yeIMPushConfig;

    private static CopyOnWriteArraySet<WebSocket> webSockets = new CopyOnWriteArraySet<>();

    private static ConcurrentHashMap<String, String> userPool = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, WebSocketSession> sessionPool = new ConcurrentHashMap<>();

    public WebSocket(YeIMPushConfig yeIMPushConfig) {
        this.yeIMPushConfig = yeIMPushConfig;
    }

    /**
     * 连接打开
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        User user = (User) session.getAttributes().get("user");
        //检测是否在线,踢掉
        WebSocketSession online = sessionPool.get(user.getUserId());
        if (online != null) {
            sendMessage(online, Result.error(StatusCode.KICKED_OUT.getCode(), StatusCode.KICKED_OUT.getDesc()).toJSONString());
            online.close();
            sessionPool.remove(user.getUserId());
        }
        webSockets.add(this);
        setUserId(session.getId(), user.getUserId());
        sessionPool.put(user.getUserId(), session);

        sendMessage(session, Result.info(StatusCode.LOGIN_SUCCESS.getCode(), StatusCode.LOGIN_SUCCESS.getDesc(), new HashMap<String, Object>() {{
            put("user", user);
            put("pushConfig",yeIMPushConfig);
        }}).toJSONString());
        log.info("【YeIMUniServer】有新用户：" + user.getNickname() + "(userId：" + user.getUserId() + ")" + "的连接，现存总数为:" + webSockets.size());
    }

    /**
     * 消息处理
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String msgStr = (String) message.getPayload();
        //IM所有消息通过json发送，判断类型，不是就停止执行
        if (!Common.isJSONObject(msgStr)) {
            return;
        }

        JSONObject msgObj = JSONObject.parseObject(msgStr);

        /**
         * socket发消息格式：
         * { type:"", data: "" }
         */

        if (!msgObj.containsKey("type")) {
            log.info("【YeIMUniServer】消息缺失：type");
            return;
        }

        if (!msgObj.containsKey("data")) {
            log.info("【YeIMUniServer】消息缺失：data");
            return;
        }
        String type = msgObj.getString("type");
        String data = msgObj.getString("data");

        //心跳
        if (type.equals("heart")) {
            sendMessage(session, Result.info(StatusCode.HEART.getCode(), StatusCode.HEART.getDesc(), "pong").toJSONString());
        }
    }

    /**
     * 连接错误
     *
     * @param session
     * @param exception
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("【YeIMUniServer】发生错误：" + exception.getMessage());
        exception.printStackTrace();
    }

    /**
     * 连接关闭
     *
     * @param session
     * @param closeStatus
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        try {
            webSockets.remove(this);
            if (getUserId(session.getId()) != null) {
                sessionPool.remove(getUserId(session.getId()));
                log.info("【YeIMUniServer】用户：" + getUserId(session.getId()) + " 连接断开，总数为:" + webSockets.size());
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


    /**
     * 通过userId发送消息
     *
     * @param userId
     * @param message
     * @return int
     */
    public static int sendMessage(String userId, String message) {
        WebSocketSession session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            try {
                log.info("【YeIMUniServer】向userId：" + userId + " 发送消息：" + message);
                session.sendMessage(new TextMessage(message));
                return 1;
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 通过session发送消息
     *
     * @param session
     * @param message
     */
    public void sendMessage(WebSocketSession session, String message) {
        if (session != null && session.isOpen()) {
            try {
                log.info("【YeIMUniServer】向sessionId：" + session.getId() + "(userId: " + getUserId(session.getId()) + ") 发送消息：" + message);
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    private void setUserId(String sessionId, String userId) {
        String exist = userPool.get(sessionId);
        if (exist != null) {
            userPool.remove(sessionId);
        }
        userPool.put(sessionId, userId);
    }

    private String getUserId(String sessionId) {
        if (userPool != null) {
            String userId = userPool.get(sessionId);
            if (userId != null) {
                return userId;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}





