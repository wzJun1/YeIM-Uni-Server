package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * @TableName user_black_list
 */
@TableName(value ="user_black_list")
@Data
public class UserBlackList implements Serializable {
    private Long blackId;

    private String userId;

    private String coverUserId;

    private Long createdAt;

    private static final long serialVersionUID = 1L;
}