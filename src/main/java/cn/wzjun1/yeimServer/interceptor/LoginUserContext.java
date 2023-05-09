package cn.wzjun1.yeimServer.interceptor;

import cn.wzjun1.yeimServer.domain.User;

public class LoginUserContext {

    private static ThreadLocal<User> userInfo = new ThreadLocal<User>();

    private static ThreadLocal<String> token = new ThreadLocal<String>();

    public LoginUserContext() {

    }

    public static User getUser() {
        User user = (User) userInfo.get();
        return user;
    }

    public static void setUser(User user) {
        userInfo.set(user);
    }

    public static void removeUser() {
        userInfo.remove();
    }

    public static String getToken() {
        return String.valueOf(userInfo.get());
    }

    public static void setToken(String s) {
        token.set(s);
    }

    public static void removeToken() {
        token.remove();
    }


}