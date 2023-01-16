package cn.wzjun1.yeimServer.service;

public interface PushService {
    void pushSingleByDeviceId(String deviceId, String pushTitle, String pushContent);
}
