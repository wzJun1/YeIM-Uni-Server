package cn.wzjun1.yeimServer.exception.message;

public class ToUserIdNotFoundException extends RuntimeException {
    public ToUserIdNotFoundException() {

    }

    public ToUserIdNotFoundException(String message) {
        super(message);
    }
}
