package cn.wzjun1.yeimServer.dto.friend;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DeleteFriendDTO {

    /**
     * 用户ID数组
     */
    @NotNull(message = "请传入用户ID列表")
    @NotEmpty(message = "请传入用户ID列表")
    @JsonProperty("members")
    private List<String> members;

}
