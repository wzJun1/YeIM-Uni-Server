package cn.wzjun1.yeimServer.exception.user;

public class UserDuplicateException extends RuntimeException {
    public UserDuplicateException() {

    }

    public UserDuplicateException(String message) {
        super(message);
    }
}
