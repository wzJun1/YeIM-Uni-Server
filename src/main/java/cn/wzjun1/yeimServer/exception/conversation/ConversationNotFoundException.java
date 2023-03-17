package cn.wzjun1.yeimServer.exception.conversation;

public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException() {

    }

    public ConversationNotFoundException(String message) {
        super(message);
    }
}
