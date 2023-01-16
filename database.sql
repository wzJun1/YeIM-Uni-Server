SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;


CREATE TABLE `conversation` (
  `id` bigint(20) NOT NULL,
  `conversation_id` varchar(255) NOT NULL COMMENT '会话ID',
  `user_id` varchar(255) NOT NULL COMMENT '所属用户\r\n',
  `type` varchar(255) NOT NULL COMMENT '会话类型\r\n私聊：private\r\n群聊：group',
  `unread` int(11) NOT NULL COMMENT '未读数',
  `last_message_id` varchar(255) NOT NULL COMMENT '最新消息ID',
  `updated_at` bigint(20) NOT NULL DEFAULT '0' COMMENT '更新时间',
  `created_at` bigint(20) NOT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

CREATE TABLE `group` (
  `id` bigint(20) NOT NULL,
  `group_id` varchar(255) NOT NULL COMMENT '群组ID',
  `name` varchar(255) NOT NULL COMMENT '群组名称',
  `avatar_url` varchar(255) NOT NULL COMMENT '群组头像',
  `leader_user_id` varchar(32) NOT NULL COMMENT '群主用户ID',
  `join_mode` int(11) NOT NULL DEFAULT '1' COMMENT '加群方式，1=自由无限制2=需群主管理验证3=禁止',
  `introduction` varchar(255) DEFAULT NULL COMMENT '群介绍',
  `notification` varchar(255) DEFAULT NULL COMMENT '群公告',
  `is_mute` int(11) NOT NULL DEFAULT '0' COMMENT '全体禁言，0=否 ，1=是',
  `is_dissolve` int(11) NOT NULL DEFAULT '0' COMMENT '是否已解散，0=未解散，1=已解散',
  `created_at` bigint(20) NOT NULL COMMENT '创建时间	'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组表';

CREATE TABLE `group_apply` (
  `id` bigint(20) NOT NULL,
  `group_id` varchar(255) NOT NULL COMMENT '群组ID',
  `user_id` varchar(255) NOT NULL COMMENT '申请入群用户ID',
  `inviter_id` varchar(255) DEFAULT NULL COMMENT '邀请人用户ID，没有可为空',
  `extra_message` varchar(255) DEFAULT NULL COMMENT '申请附言',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态,1=未处理2=同意3=拒绝',
  `admin_id` varchar(255) DEFAULT NULL COMMENT '处理申请管理员用户ID',
  `transform_time` bigint(20) DEFAULT '0' COMMENT '处理时间',
  `transform_message` varchar(255) DEFAULT NULL COMMENT '处理附言',
  `created_at` bigint(20) NOT NULL COMMENT '申请时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入群申请表';

CREATE TABLE `group_message` (
  `sequence` bigint(20) NOT NULL COMMENT '顺序',
  `message_id` varchar(255) NOT NULL COMMENT '消息ID',
  `user_id` varchar(255) NOT NULL COMMENT '消息所属用户ID',
  `conversation_id` varchar(255) NOT NULL COMMENT '消息所属会话ID',
  `direction` varchar(50) NOT NULL COMMENT '消息方向：in=接收 out等于发出（群消息应用层控制）',
  `from` varchar(255) NOT NULL COMMENT '消息发送方',
  `to` varchar(255) NOT NULL COMMENT '消息接收方',
  `type` varchar(255) NOT NULL COMMENT '消息类型',
  `body` json NOT NULL COMMENT '消息内容',
  `extra` longtext COMMENT '扩展的自定义数据(字符串类型)',
  `is_read` int(11) NOT NULL DEFAULT '0' COMMENT '对方是否已读',
  `is_revoke` int(11) NOT NULL DEFAULT '0' COMMENT '是否被撤回的消息',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '是否被删除的消息',
  `status` varchar(20) NOT NULL DEFAULT '' COMMENT '消息状态：\r\nunSend(未发送)\r\nsuccess(发送成功)\r\nfail(发送失败)',
  `receive` int(11) NOT NULL DEFAULT '0' COMMENT '接收状态，0 = 未接收， 1 = 已接收',
  `time` bigint(20) NOT NULL DEFAULT '0' COMMENT '消息时间，毫秒'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群消息表';

CREATE TABLE `group_user` (
  `id` int(11) NOT NULL,
  `group_id` varchar(255) NOT NULL COMMENT '群组ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `mute_end_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '禁言结束时间(毫秒级时间戳)，0=不禁言',
  `is_admin` int(11) NOT NULL DEFAULT '0' COMMENT '是否群管理员，1=是，0=不是',
  `join_at` bigint(20) NOT NULL COMMENT '加入时间',
  `created_at` bigint(20) NOT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群成员表';

CREATE TABLE `message` (
  `sequence` bigint(20) NOT NULL COMMENT '顺序',
  `message_id` varchar(255) NOT NULL COMMENT '消息ID',
  `user_id` varchar(255) NOT NULL COMMENT '消息所属用户ID',
  `conversation_id` varchar(255) NOT NULL COMMENT '消息所属会话ID',
  `direction` varchar(50) NOT NULL COMMENT '消息方向：in=接收 out等于发出',
  `from` varchar(255) NOT NULL COMMENT '消息发送方',
  `to` varchar(255) NOT NULL COMMENT '消息接收方',
  `type` varchar(255) NOT NULL COMMENT '消息类型',
  `body` json NOT NULL COMMENT '消息内容',
  `extra` longtext COMMENT '扩展的自定义数据(字符串类型)',
  `is_read` int(11) NOT NULL DEFAULT '0' COMMENT '对方是否已读',
  `is_revoke` int(11) NOT NULL DEFAULT '0' COMMENT '是否被撤回的消息',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '是否被删除的消息',
  `status` varchar(20) NOT NULL DEFAULT '' COMMENT '消息状态：\r\nunSend(未发送)\r\nsuccess(发送成功)\r\nfail(发送失败)',
  `receive` int(11) NOT NULL DEFAULT '0' COMMENT '接收状态，0 = 未接收， 1 = 已接收',
  `time` bigint(20) NOT NULL DEFAULT '0' COMMENT '消息时间，毫秒'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私聊消息表';

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `nickname` varchar(255) DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `mobile_device_id` varchar(255) DEFAULT NULL COMMENT '移动APP端推送标识符',
  `created_at` bigint(20) DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


ALTER TABLE `conversation`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `conversation_id_user_id_unique` (`conversation_id`,`user_id`) USING BTREE,
  ADD KEY `user_id_idx` (`user_id`) USING BTREE,
  ADD KEY `conversation_id_user_id_idx` (`conversation_id`,`user_id`) USING BTREE,
  ADD KEY `updated_at_idx` (`updated_at`),
  ADD KEY `user_id_updated_at_idx` (`user_id`,`updated_at`),
  ADD KEY `type_idx` (`type`),
  ADD KEY `user_id_type_idx` (`user_id`,`type`),
  ADD KEY `user_id_updated_at_type_idx` (`user_id`,`type`,`updated_at`);

ALTER TABLE `group`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `group_id_unique` (`group_id`) USING BTREE,
  ADD KEY `group_id_idx` (`group_id`);

ALTER TABLE `group_apply`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `group_message`
  ADD PRIMARY KEY (`sequence`),
  ADD UNIQUE KEY `message_id_conversation_id_unique` (`message_id`,`conversation_id`) USING BTREE,
  ADD KEY `message_id_idx` (`message_id`);

ALTER TABLE `group_user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `group_id_user_id_unique` (`group_id`,`user_id`);

ALTER TABLE `message`
  ADD PRIMARY KEY (`sequence`),
  ADD UNIQUE KEY `message_id_conversation_id_unique` (`message_id`,`conversation_id`) USING BTREE,
  ADD KEY `message_id_idx` (`message_id`);

ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id_unique` (`user_id`) USING BTREE,
  ADD KEY `user_id_idx` (`user_id`);


ALTER TABLE `conversation`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `group`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `group_apply`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `group_message`
  MODIFY `sequence` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '顺序';

ALTER TABLE `group_user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `message`
  MODIFY `sequence` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '顺序';

ALTER TABLE `user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
