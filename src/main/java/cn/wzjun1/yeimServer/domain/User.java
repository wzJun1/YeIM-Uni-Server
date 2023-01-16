package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 移动端推送标识符
     */
    private String mobileDeviceId;

    /**
     * 创建时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}