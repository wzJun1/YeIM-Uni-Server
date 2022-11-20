package cn.wzjun1.yeimServer.pojo;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UserDTO {

    /**
     * 注册校验
     */
    public interface Register {
    }

    /**
     * Token换取校验
     */
    public interface Token {
    }

    /**
     * 用户ID
     */
    @NotNull(message = "userId must be not null", groups = {Register.class, Token.class})
    @Length(message = "userId length is between 1~32", min = 1, max = 32, groups = {Register.class, Token.class})
    @JsonProperty("userId")
    private String userId;

    /**
     * 用户昵称
     */
    @NotNull(message = "nickname must be not null", groups = {Register.class})
    @Length(message = "nickname length is between 1~32", min = 1, max = 32, groups = {Register.class})
    @JsonProperty("nickname")
    private String nickname;

    /**
     * 用户头像地址
     */
    @NotNull(message = "avatarUrl must be not null", groups = {Register.class})
    @Length(message = "avatarUrl length is between 1~300", min = 1, max = 300, groups = {Register.class})
    @JsonProperty("avatarUrl")
    private String avatarUrl;

    /**
     * 过期时间时间戳
     */
    @NotNull(message = "timestamp must be not null", groups = {Token.class})
    @Min(message = "timestamp must be not 0", value = 1, groups = {Token.class})
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * token换取校验sign
     */
    @NotNull(message = "sign must be not null", groups = {Token.class})
    @Length(message = "sign length is 32", min = 32, max = 32, groups = {Token.class})
    @JsonProperty("sign")
    private String sign;
}
