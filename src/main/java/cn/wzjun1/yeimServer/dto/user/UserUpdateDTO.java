package cn.wzjun1.yeimServer.dto.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserUpdateDTO {

    /**
     * 用户昵称
     */
    @JsonProperty("nickname")
    private String nickname;

    /**
     * 用户头像地址
     */
    @JsonProperty("avatarUrl")
    private String avatarUrl;

    /**
     * 性别
     */
    @JsonProperty("gender")
    private Integer gender;

    /**
     * 电话
     */
    @JsonProperty("mobile")
    private Long mobile;

    /**
     * 邮箱
     */
    @JsonProperty("email")
    private String email;

    /**
     * 生日
     */
    @JsonProperty("birthday")
    private String birthday;

    /**
     * 个性签名
     */
    @JsonProperty("motto")
    private String motto;

    /**
     * 自定义扩展字段
     */
    @JsonProperty("extend")
    private String extend;

    /**
     * 添加好友的方式
     *
     * 1：允许自由添加
     * 2：需要本人确认
     * 3：拒绝添加好友
     */
    @JsonProperty("addFriendType")
    private Integer addFriendType;

}
