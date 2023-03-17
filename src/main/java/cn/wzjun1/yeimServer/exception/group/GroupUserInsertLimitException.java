package cn.wzjun1.yeimServer.exception.group;

public class GroupUserInsertLimitException extends RuntimeException {
    public GroupUserInsertLimitException() {

    }

    public GroupUserInsertLimitException(String message) {
        super(message);
    }
}
