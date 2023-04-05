package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName friend
 */
@TableName(value ="friend")
@Data
public class Friend implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private String friendUserId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String remark;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String extend;

    private Long createdAt;

    private static final long serialVersionUID = 1L;
}