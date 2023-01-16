package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;

/**
 * @TableName group_message
 */
@TableName(value ="group_message")
@Data
public class GroupMessage implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long sequence;

    /**
     * 消息ID
     * 私聊消息ID组成：分布式唯一ID-毫秒级时间戳-发送端1（接收端2）
     */
    private String messageId;

    /**
     * 消息所属用户
     */
    private String userId;


    /**
     * 消息所属会话ID
     */
    private String conversationId;

    /**
     * 消息方向：in=接收 out等于发出
     */
    private String direction;

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
     * 扩展的自定义数据(字符串类型)
     */
    private String extra;

    /**
     * 对方是否已读
     */
    private Integer isRead;

    /**
     * 是否被撤回的消息
     */
    private Integer isRevoke;

    /**
     * 是否被删除的消息
     */
    private Integer isDeleted;

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
     * 消息时间，毫秒级时间戳
     */
    @TableField(value = "`time`")
    private Long time;


    private static final long serialVersionUID = 1L;
}