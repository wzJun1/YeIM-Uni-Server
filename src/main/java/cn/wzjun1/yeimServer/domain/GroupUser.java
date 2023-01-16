package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 群用户关联
 * @TableName group_user
 */
@TableName(value ="group_user")
@Data
public class GroupUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 群ID
     */
    private String groupId;

    /**
     * 用户ID
     */
    private String userId;

    @TableField(exist = false)
    private User userInfo;

    /**
     * 是否是管理员
     */
    private Integer isAdmin;

    /**
     * 禁言到期时间
     * 0表示不禁言
     */
    private Long muteEndTime;

    /**
     * 加入群的时间
     */
    private Long joinAt;

    private Long createdAt;

    private static final long serialVersionUID = 1L;
}