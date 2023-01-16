package cn.wzjun1.yeimServer.dto.group;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GroupUserAddDTO {

    /**
     * 群ID
     */
    @NotNull(message = "群ID不能为空")
    @Length(message = "群ID的字节长度应在1-255位之间", min = 1, max = 255)
    @JsonProperty("groupId")
    private String groupId;


    /**
     * 群聊成员用户ID数组
     */
    @NotNull(message = "请传入用户ID列表")
    @NotEmpty(message = "请传入用户ID列表")
    @JsonProperty("members")
    private List<String> members;

}
