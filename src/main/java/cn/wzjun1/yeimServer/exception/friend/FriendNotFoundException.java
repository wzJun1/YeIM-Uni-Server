package cn.wzjun1.yeimServer.exception.friend;

public class FriendNotFoundException extends RuntimeException {
    public FriendNotFoundException() {

    }

    public FriendNotFoundException(String message) {
        super(message);
    }
}
