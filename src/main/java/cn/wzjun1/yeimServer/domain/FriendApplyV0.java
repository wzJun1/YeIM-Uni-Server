package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName friend_apply
 */
@TableName(value ="friend_apply")
@Data
public class FriendApplyV0 implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyUserId;

    private String userId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String remark;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String extraMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long transformTime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isRead;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long createdAt;

    /**
     * 用户信息
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User userInfo;

    private static final long serialVersionUID = 1L;
}