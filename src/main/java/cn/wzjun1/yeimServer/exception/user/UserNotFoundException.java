package cn.wzjun1.yeimServer.exception.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {

    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
