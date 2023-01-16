package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.GroupMessage;
import cn.wzjun1.yeimServer.domain.Message;

public interface AsyncService {
    void emitJSSDKMessageReceive(Message message);

    void updateGroupConversationSendEvent(String groupId, GroupMessage message);
}
