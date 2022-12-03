package cn.wzjun1.yeimServer.dto.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class UserUpdateDTO {

    /**
     * 用户昵称
     */
    @NotNull(message = "用户昵称不能为空")
    @Length(message = "用户昵称的字节长度应在1-32位之间", min = 1, max = 32)
    @JsonProperty("nickname")
    private String nickname;

    /**
     * 用户头像地址
     */
    @NotNull(message = "用户头像不能为空")
    @Length(message = "用户头像的字节长度应在1-300位之间", min = 1, max = 300)
    @JsonProperty("avatarUrl")
    private String avatarUrl;

}
