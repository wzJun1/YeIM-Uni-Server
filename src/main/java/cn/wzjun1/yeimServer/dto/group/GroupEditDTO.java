package cn.wzjun1.yeimServer.dto.group;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GroupEditDTO {

    /**
     * 群ID
     */
    @NotEmpty(message = "群ID不能为空")
    @JsonProperty("groupId")
    private String groupId;

    /**
     * 群名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 群头像地址
     */
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
     * 全体禁言
     * 1=是
     * 0=否
     */
    @JsonProperty("isMute")
    private Integer isMute;

}
