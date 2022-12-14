package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * @TableName group_apply
 */
@TableName(value ="group_apply")
@Data
public class GroupApply implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群ID
     */
    private String groupId;

    @TableField(exist = false)
    private Group groupInfo;

    /**
     * 申请用户ID
     */
    private String userId;
    @TableField(exist = false)
    private User userInfo;

    /**
     * 邀请人id
     * 如果是别人代拉userid入群，此字段为操作者ID
     */
    private String inviterId;

    /**
     * 申请附言
     */
    private String extraMessage;

    /**
     * 申请状态
     */
    private Integer status;

    /**
     * 处理申请的管理员ID
     */
    private String adminId;

    /**
     * 处理时间
     */
    private Long transformTime;

    /**
     * 处理附言
     */
    private String transformMessage;

    /**
     * 申请的时间
     */
    private Long createdAt;

    private static final long serialVersionUID = 1L;
}