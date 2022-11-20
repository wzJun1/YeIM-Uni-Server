package cn.wzjun1.yeimServer.service;

public interface WebSocketService {
    void receivedCallMessage(String userId, String message) throws Exception;
}
