SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

CREATE DATABASE IF NOT EXISTS `yeim` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `yeim`;

CREATE TABLE `conversation` (
  `id` bigint(20) NOT NULL,
  `conversation_id` varchar(255) NOT NULL COMMENT '会话ID',
  `user_id` varchar(255) NOT NULL COMMENT '所属用户\r\n',
  `type` varchar(255) NOT NULL COMMENT '会话类型\r\n私聊：private\r\n群聊：group',
  `unread` int(11) NOT NULL COMMENT '未读数',
  `last_message_id` varchar(255) NOT NULL COMMENT '最新消息ID',
  `updated_at` bigint(20) NOT NULL DEFAULT '0' COMMENT '更新时间',
  `created_at` bigint(20) NOT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
  `nickname` varchar(255) DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `created_at` bigint(20) DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


ALTER TABLE `conversation`
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `conver_id_user_id_unique` (`conversation_id`,`user_id`),
  ADD KEY `conver_id_user_id` (`conversation_id`,`user_id`);

ALTER TABLE `message`
  ADD PRIMARY KEY (`sequence`),
  ADD UNIQUE KEY `messageId_conversationId_unique` (`message_id`,`conversation_id`) USING BTREE;

ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `userId_unique` (`user_id`);


ALTER TABLE `conversation`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `message`
  MODIFY `sequence` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '顺序';

ALTER TABLE `user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
