package cn.wzjun1.yeimServer.utils;

import cn.wzjun1.yeimServer.interceptor.UserAuthorizationInterceptor;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

public class RequestUtils {
    private RequestUtils() {
    }

    /**
     * 获取当前登陆用户ID
     *
     * @return String
     */
    public static String getUserId() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (null != requestAttributes) {
            HttpServletRequest request = requestAttributes.getRequest();
            String userId = request.getAttribute(UserAuthorizationInterceptor.REQUEST_TOKEN_USER_ID).toString();
            if (StringUtils.isNotEmpty(userId)) {
                return userId;
            }
        }
        return null;
    }

}
