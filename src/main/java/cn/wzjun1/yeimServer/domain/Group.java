package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @TableName group
 */
@TableName(value ="`group`")
@Data
public class Group implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    /**
     * 群组ID
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String groupId;

    /**
     * 群组名称
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    /**
     * 群组头像
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String avatarUrl;

    /**
     * 群主用户ID
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String leaderUserId;

    /**
     * 加群方式
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer joinMode;

    /**
     * 群介绍
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String introduction;

    /**
     * 群公告
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String notification;

    /**
     * 全体禁言
     * 1=是
     * 0=否
     */
    private Integer isMute;

    /**
     * 是否已解散群组，1=已解散 0 = 未解散
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isDissolve;

    /**
     * 创建时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long createdAt;

    private static final long serialVersionUID = 1L;
}