package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class ConversationV0 implements Serializable {

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 用户信息

     */
    private User userInfo;

    /**
     * 群组信息
     */
    private Group groupInfo;


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

    private Message lastMessage;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 更新时间
     */
    private Long updatedAt;



}