package cn.wzjun1.yeimServer.exception.user;

public class UserFriendDenyException extends RuntimeException {
    public UserFriendDenyException() {

    }

    public UserFriendDenyException(String message) {
        super(message);
    }
}
