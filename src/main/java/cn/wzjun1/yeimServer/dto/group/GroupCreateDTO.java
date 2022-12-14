package cn.wzjun1.yeimServer.dto.group;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GroupCreateDTO {

    /**
     * 群ID
     */
    @JsonProperty("groupId")
    private String groupId;

    /**
     * 群名称
     */
    @NotNull(message = "群名称不能为空")
    @Length(message = "群名称的字节长度应在1-255位之间", min = 1, max = 255)
    @JsonProperty("name")
    private String name;

    /**
     * 群头像地址
     */
    @NotNull(message = "群头像不能为空")
    @Length(message = "群头像的字节长度应在1-300位之间", min = 1, max = 300)
    @JsonProperty("avatarUrl")
    private String avatarUrl;

    /**
     * 加群方式
     * JoinGroupMode
     */
    @JsonProperty("joinMode")
    private Integer joinMode;

    /**
     * 群介绍
     */
    @JsonProperty("introduction")
    private String introduction;

    /**
     * 群公告
     */
    @JsonProperty("notification")
    private String notification;

    /**
     * 群聊成员用户ID数组
     */
    @JsonProperty("members")
    private List<String> members;

}
