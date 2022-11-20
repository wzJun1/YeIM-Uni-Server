package cn.wzjun1.yeimServer.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SocketMessage implements Serializable {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 会话类型
     * private 私聊
     * group 群聊
     */
    private String conversationType;

    /**
     * 消息发送方
     */
    private String from;

    /**
     * 消息接收方
     */
    private String to;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private String body;

    /**
     * 对方是否已读
     */
    private boolean isRead;

    /**
     * 是否被撤回的消息
     */
    private boolean isRecall;

    /**
     * 消息状态：
     unSend(未发送)
     success(发送成功)
     fail(发送失败)
     */
    private String status;

    /**
     * 消息时间，毫秒
     */
    private Long time;


}
