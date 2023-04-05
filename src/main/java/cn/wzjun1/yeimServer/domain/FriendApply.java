package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName friend_apply
 */
@TableName(value ="friend_apply")
@Data
public class FriendApply implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyUserId;

    private String userId;

    private String remark;

    private String extraMessage;

    private Integer status;

    private Long transformTime;

    private Integer isRead;

    private Long createdAt;

    private static final long serialVersionUID = 1L;
}