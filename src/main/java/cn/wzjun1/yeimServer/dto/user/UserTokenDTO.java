package cn.wzjun1.yeimServer.dto.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UserTokenDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Length(message = "用户ID的长度应在1-32位之间", min = 1, max = 32)
    @JsonProperty("userId")
    private String userId;

    /**
     * 过期时间
     */
    @NotNull(message = "过期时间不能为空")
    @Min(message = "过期时间应为毫秒级时间戳",value = 1000000000000L)
    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * sign
     *
     */
    @NotNull(message = "签名不能为空")
    @Length(message = "签名长度应为32字节", min = 32, max = 32)
    @JsonProperty("sign")
    private String sign;

}
