package cn.wzjun1.yeimServer.dto.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserBlackListAddDTO {

    /**
     * 黑名单用户ID数组
     */
    @NotNull(message = "请传入用户ID列表")
    @NotEmpty(message = "请传入用户ID列表")
    @JsonProperty("members")
    private List<String> members;

}
