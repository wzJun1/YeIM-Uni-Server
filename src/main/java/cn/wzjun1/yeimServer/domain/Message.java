package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName message
 */
@Data
@TableName(value = "message")
public class Message implements Serializable {

    @TableId(type = IdType.AUTO,value = "id")
    private Long sequence;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 消息所属会话ID
     */
    private String conversationId;

    /**
     * 消息发送方
     */
    @TableField(value = "`from`")
    private String from;


    private User fromUserInfo;

    /**
     * 消息接收方
     */
    @TableField(value = "`to`")
    private String to;

    /**
     * 消息类型
     */
    @TableField(value = "`type`")
    private String type;

    /**
     * 消息内容
     */
    @TableField(value = "`body`", typeHandler = FastjsonTypeHandler.class)
    private Object body;

    /**
     * 对方是否已读
     */
    private Integer isRead;

    /**
     * 是否被撤回的消息
     */
    private Integer isRecall;

    /**
     * 消息状态：
     * unSend(未发送)
     * success(发送成功)
     * fail(发送失败)
     */
    @TableField(value = "`status`")
    private String status;

    /**
     * 对方接收状态
     * 0 = 未接收
     * 1 = 已接收
     */
    @TableField(value = "`receive`")
    private Integer receive;

    /**
     * 消息时间，毫秒
     */
    @TableField(value = "`time`")
    private Long time;

}