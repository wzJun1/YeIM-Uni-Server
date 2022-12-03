package cn.wzjun1.yeimServer.socket.cons;

public enum SocketStatusCode {

    //网络连接
    CONNECT_ERROR(100, "服务连接错误"),
    //用户验权
    TOKEN_ERROR(101, "用户验证失败，请检查userId、token"),
    //多端挤下线
    KICKED_OUT(109, "有新连接踢掉了当前连接"),
    //登陆成功
    LOGIN_SUCCESS(201, "登陆成功"),
    //心跳
    HEART(202, ""),
    //会话列表更新
    CONVERSATION_CHANGED(203, "会话更新"),
    //会话列表更新
    CONVERSATION_LIST_CHANGED(204, "会话列表更新"),
    //私聊会话已读回执
    PRIVATE_READ_RECEIPT(205, "会话已读回执"),
    //推送新消息
    MESSAGE_RECEIVE(200, "");
    private Integer code;
    private String desc;

    private SocketStatusCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private SocketStatusCode() {
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
