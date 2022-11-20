package cn.wzjun1.yeimServer.pojo.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UserTokenPojo {

    /**
     * 用户ID
     */
    @NotNull(message = "userId must be not null")
    @Length(message = "userId length is between 1~32", min = 1, max = 32)
    @JsonProperty("userId")
    private String userId;

    /**
     * 过期时间
     */
    @NotNull(message = "timestamp must be not null")
    @Min(message = "timestamp length is 13",value = 1000000000000L)
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * sign
     *
     */
    @NotNull(message = "sign must be not null")
    @Length(message = "sign length is 32", min = 32, max = 32)
    @JsonProperty("sign")
    private String sign;

}
