package cn.wzjun1.yeimServer.exception.user;

public class ExpireException extends RuntimeException {
    public ExpireException() {

    }

    public ExpireException(String message) {
        super(message);
    }
}
