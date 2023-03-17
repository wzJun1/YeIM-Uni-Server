package cn.wzjun1.yeimServer.exception.group;

public class NoGroupUserException extends RuntimeException {
    public NoGroupUserException() {

    }

    public NoGroupUserException(String message) {
        super(message);
    }
}
