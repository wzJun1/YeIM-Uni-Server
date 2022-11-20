package cn.wzjun1.yeimServer.interceptor;

import cn.wzjun1.yeimServer.annotation.UserAuthorization;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
public class UserAuthorizationInterceptor extends HandlerInterceptorAdapter {

    private RedisUtil redisUtil;

    public UserAuthorizationInterceptor(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }


    public static final String REQUEST_TOKEN_USER = "REQUEST_TOKEN_USER";

    public static final String REQUEST_TOKEN_USER_ID = "REQUEST_TOKEN_USER_ID";
    /**
     * 存放鉴权信息的Header名称
     */
    private String httpHeaderName = "token";


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 如果不是映射到方法直接通过
         */
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        String token = request.getHeader(httpHeaderName);
        if (token != null && token.length() > 0) {
            log.info("token" + token);
            String key = "token:" + token;
            if (redisUtil.hasKey(key)) {
                User user = (User) redisUtil.get("token:" + token);
                request.setAttribute(REQUEST_TOKEN_USER, user);
                request.setAttribute(REQUEST_TOKEN_USER_ID, user.getUserId());
                return true;
            }
        }
        /**
         * 如果验证token失败，并且方法或类注明了Authorization,返回错误
         */
        if (method.getAnnotation(UserAuthorization.class) != null || handlerMethod.getBeanType().getAnnotation(UserAuthorization.class) != null) {
            throw new Exception("用户权限验证错误");
        }
        request.setAttribute(REQUEST_TOKEN_USER, null);
        return true;
    }


}