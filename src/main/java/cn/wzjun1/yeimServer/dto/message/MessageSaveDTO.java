package cn.wzjun1.yeimServer.dto.message;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MessageSaveDTO {

//    消息ID
//    @NotNull(message = "messageId 不能为空")
//    @JsonProperty("messageId")
//    private String messageId;

    //会话ID
    @NotNull(message = "conversationId 不能为空")
    @JsonProperty("conversationId")
    private String conversationId;

    //会话类型
    @NotNull(message = "conversationType 不能为空")
    @JsonProperty("conversationType")
    private String conversationType;

    //消息发送方ID
    @NotNull(message = "from 不能为空")
    @JsonProperty("from")
    private String from;

    //消息接收方ID
    @NotNull(message = "to 不能为空")
    @JsonProperty("to")
    private String to;

    //消息类型
    @NotNull(message = "type 不能为空")
    @JsonProperty("type")
    private String type;

    //消息体
    @NotNull(message = "body 不能为空")
    @JsonProperty("body")
    private JSONObject body;

    //消息体
    @JsonProperty("extra")
    private String extra;

    //发送方发送时间
    @NotNull(message = "time 不能为空")
    @JsonProperty("time")
    private Long time;


}
