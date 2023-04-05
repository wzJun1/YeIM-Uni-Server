package cn.wzjun1.yeimServer.dto.friend;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class AddFriendDTO {

    /**
     * 好友ID
     */
    @NotNull(message = "好友用户ID不能为空")
    @Length(message = "好友用户ID的字节长度应在1-32位之间", min = 1, max = 32)
    @JsonProperty("userId")
    private String userId;

    /**
     * 好友备注
     */
    @JsonProperty("remark")
    private String remark;

    /**
     * 附言
     */
    @JsonProperty("extraMessage")
    private String extraMessage;

}
