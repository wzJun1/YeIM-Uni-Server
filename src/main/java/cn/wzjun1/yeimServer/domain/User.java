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
@TableName(value ="`user`")
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
     * 性别
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer gender;

    /**
     * 电话
     */
    @TableField(value = "`mobile`")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long mobile;

    /**
     * 邮箱
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;

    /**
     * 生日
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String birthday;

    /**
     * 个性签名
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String motto;

    /**
     * 自定义扩展字段
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String extend;

    /**
     * 移动端推送标识符
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mobileDeviceId;

    /**
     * 添加好友的方式
     *
     * 1：允许自由添加
     * 2：需要本人确认
     * 3：拒绝添加好友
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer addFriendType;

    /**
     * 创建时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}