package cn.wzjun1.yeimServer.exception.group;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException() {

    }

    public GroupNotFoundException(String message) {
        super(message);
    }
}
