package cn.wzjun1.yeimServer.service;

import cn.wzjun1.yeimServer.domain.GroupMessage;
import cn.wzjun1.yeimServer.domain.Message;
import cn.wzjun1.yeimServer.domain.User;
import cn.wzjun1.yeimServer.dto.message.MessageSaveDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【group_message】的数据库操作Service
* @createDate 2022-12-08 21:18:05
*/
public interface GroupMessageService extends IService<GroupMessage> {
    GroupMessage insertGroupMessage(User user, MessageSaveDTO message) throws Exception;
    IPage<GroupMessage> listMessage(IPage<GroupMessage> page, String conversationId) throws Exception;
    List<GroupMessage> listMessage(String conversationId, String nextMessageId, Integer limit);

}
