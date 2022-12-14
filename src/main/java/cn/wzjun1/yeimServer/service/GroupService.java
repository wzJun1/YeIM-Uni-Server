package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.Group;
import cn.wzjun1.yeimServer.dto.group.GroupCreateDTO;
import cn.wzjun1.yeimServer.dto.group.GroupEditDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Administrator
 * @description 针对表【group】的数据库操作Service
 * @createDate 2022-12-07 20:00:11
 */
public interface GroupService extends IService<Group> {
    void createGroup(GroupCreateDTO params) throws Exception;

    void dissolveGroup(String groupId) throws Exception;

    void updateGroup(GroupEditDTO params) throws Exception;

    void transferLeader(String groupId, String userId) throws Exception;

}
