package cn.wzjun1.yeimServer.exception.group;

public class GroupDuplicateException extends RuntimeException {
    public GroupDuplicateException() {

    }

    public GroupDuplicateException(String message) {
        super(message);
    }
}
