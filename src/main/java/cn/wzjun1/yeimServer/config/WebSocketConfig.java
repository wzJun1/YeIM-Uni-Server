package cn.wzjun1.yeimServer.config;

import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.service.WebSocketService;
import cn.wzjun1.yeimServer.socket.WebSocket;
import cn.wzjun1.yeimServer.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    WebSocketService webSocketService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(new WebSocket(webSocketService), "/im/*/*")
                .addInterceptors(new HttpSessionHandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        String path = request.getURI().getPath().substring(request.getURI().getPath().lastIndexOf("im/"));
                        String[] pathArr = path.split("/");
                        if (pathArr.length != 3) {
                            response.setStatusCode(HttpStatus.FORBIDDEN);
                            return false;
                        }
                        String userId = pathArr[1];
                        String token = pathArr[2];

                        token = "token:" + token;
                        if (!redisUtil.hasKey(token)) {
                            //token????????????????????????
                            response.setStatusCode(HttpStatus.UNAUTHORIZED);
                            return false;
                        } else {
                            User user = (User) redisUtil.get(token);
                            if (!userId.equals(user.getUserId())) {
                                //token????????????user???????????????
                                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                return false;
                            } else {
                                //????????????
                                attributes.put("user", user);
                            }
                        }
                        return super.beforeHandshake(request, response, wsHandler, attributes);
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
                        super.afterHandshake(serverHttpRequest, serverHttpResponse, webSocketHandler, e);
                    }
                })
                .setAllowedOrigins("*")
                .setAllowedOriginPatterns("*");
    }
}