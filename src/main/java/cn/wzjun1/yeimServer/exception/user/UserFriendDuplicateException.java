package cn.wzjun1.yeimServer.exception.user;

public class UserFriendDuplicateException extends RuntimeException {
    public UserFriendDuplicateException() {

    }

    public UserFriendDuplicateException(String message) {
        super(message);
    }
}
