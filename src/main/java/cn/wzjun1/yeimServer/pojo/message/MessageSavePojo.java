package cn.wzjun1.yeimServer.pojo.message;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MessageSavePojo {

    //消息ID
    @NotNull(message = "messageId must be not null")
    @JsonProperty("messageId")
    private String messageId;

    //会话ID
    @NotNull(message = "conversationId must be not null")
    @JsonProperty("conversationId")
    private String conversationId;

    //会话类型
    @NotNull(message = "conversationType must be not null")
    @JsonProperty("conversationType")
    private String conversationType;

    //消息发送方ID
    @NotNull(message = "from must be not null")
    @JsonProperty("from")
    private String from;

    //消息接收方ID
    @NotNull(message = "to must be not null")
    @JsonProperty("to")
    private String to;

    //消息类型
    @NotNull(message = "type must be not null")
    @JsonProperty("type")
    private String type;

    //消息体
    @NotNull(message = "body must be not null")
    @JsonProperty("body")
    private JSONObject body;

    //发送方发送时间
    @NotNull(message = "time must be not null")
    @JsonProperty("time")
    private Long time;


}
