package cn.wzjun1.yeimServer.exception.group;

public class GroupNoEntryException extends RuntimeException {
    public GroupNoEntryException() {

    }

    public GroupNoEntryException(String message) {
        super(message);
    }
}
