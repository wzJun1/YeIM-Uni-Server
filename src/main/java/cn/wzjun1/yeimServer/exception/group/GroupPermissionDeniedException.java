package cn.wzjun1.yeimServer.exception.group;

public class GroupPermissionDeniedException extends RuntimeException {
    public GroupPermissionDeniedException() {

    }

    public GroupPermissionDeniedException(String message) {
        super(message);
    }
}
