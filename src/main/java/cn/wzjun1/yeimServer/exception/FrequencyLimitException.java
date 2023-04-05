package cn.wzjun1.yeimServer.exception;

public class FrequencyLimitException extends RuntimeException {
    public FrequencyLimitException() {

    }

    public FrequencyLimitException(String message) {
        super(message);
    }
}
