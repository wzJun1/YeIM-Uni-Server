package cn.wzjun1.yeimServer.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName user_black_list
 */
@Data
public class UserBlackListV0 implements Serializable {
    private Long blackId;

    private String userId;

    private String coverUserId;

    private User coverUser;

    private Long createdAt;

    private static final long serialVersionUID = 1L;
}