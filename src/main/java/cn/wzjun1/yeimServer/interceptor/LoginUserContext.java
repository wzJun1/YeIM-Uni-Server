package cn.wzjun1.yeimServer.interceptor;

import cn.wzjun1.yeimServer.domain.User;

public class LoginUserContext {

    private static ThreadLocal<User> userInfo = new ThreadLocal<User>();


    public LoginUserContext() {

    }

    public static User getUser() {
        User user = (User) userInfo.get();
        return user;
    }

    public static void setUser(User user) {
        userInfo.set(user);
    }

    public static void remove() {
        userInfo.remove();
    }

}