package cn.wzjun1.yeimServer.pojo.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class UserUpdatePojo {

    /**
     * 用户昵称
     */
    @NotNull(message = "nickname must be not null")
    @Length(message = "nickname length is between 1~32", min = 1, max = 32)
    @JsonProperty("nickname")
    private String nickname;

    /**
     * 用户头像地址
     */
    @NotNull(message = "avatarUrl must be not null")
    @Length(message = "avatarUrl length is between 1~300", min = 1, max = 300)
    @JsonProperty("avatarUrl")
    private String avatarUrl;

}
