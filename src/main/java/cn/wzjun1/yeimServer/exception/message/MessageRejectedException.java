package cn.wzjun1.yeimServer.exception.message;

public class MessageRejectedException extends RuntimeException {
    public MessageRejectedException() {

    }

    public MessageRejectedException(String message) {
        super(message);
    }
}
