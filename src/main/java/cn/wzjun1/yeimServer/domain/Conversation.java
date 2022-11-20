package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName conversation
 */
@TableName(value ="conversation")
@Data
public class Conversation implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 所属用户

     */
    private String userId;

    /**
     * 会话类型
私聊：private
群聊：group
     */
    @TableField(value = "`type`")
    private String type;

    /**
     * 未读数
     */
    private Integer unread;

    /**
     * 最新消息ID
     */
    private String lastMessageId;

    /**
     * 更新时间
     */
    private Long updatedAt;

    /**
     * 创建时间
     */
    private Long createdAt;

}