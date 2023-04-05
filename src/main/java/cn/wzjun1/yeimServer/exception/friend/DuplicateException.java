package cn.wzjun1.yeimServer.exception.friend;

public class DuplicateException extends RuntimeException {
    public DuplicateException() {

    }

    public DuplicateException(String message) {
        super(message);
    }
}
