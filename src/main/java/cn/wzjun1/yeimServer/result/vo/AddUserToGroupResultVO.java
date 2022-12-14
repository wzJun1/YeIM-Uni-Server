package cn.wzjun1.yeimServer.result.vo;

import cn.wzjun1.yeimServer.domain.Group;
import lombok.Data;

import java.util.List;


/**
 * addUserToGroup 方法返回值
 *
 * @List<String> successList 操作成功的用户ID列表
 * @List<String> failList 操作失败的用户ID列表
 * @List<String> ignoreList 忽略了的用户ID列表
 *
 */
@Data
public class AddUserToGroupResultVO {

    /**
     * 群组信息
     */
    private Group group;

    /**
     * 操作成功的用户ID列表
     */
    private List<String> successList;

    /**
     * 操作失败的用户ID列表
     */
    private List<String> failList;

    /**
     * 忽略了的用户ID列表
     */
    private List<String> ignoreList;

}
