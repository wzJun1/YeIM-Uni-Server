-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- 主机： localhost
-- 生成日期： 2022-11-20 17:52:46
-- 服务器版本： 5.7.31-log
-- PHP 版本： 7.4.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

--
-- 数据库： `yeim`
--

-- --------------------------------------------------------

--
-- 表的结构 `conversation`
--

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

-- --------------------------------------------------------

--
-- 表的结构 `message`
--

CREATE TABLE `message` (
                           `id` bigint(20) NOT NULL,
                           `message_id` varchar(255) NOT NULL COMMENT '消息ID',
                           `conversation_id` varchar(255) NOT NULL COMMENT '消息所属会话ID',
                           `from` varchar(255) NOT NULL COMMENT '消息发送方',
                           `to` varchar(255) NOT NULL COMMENT '消息接收方',
                           `type` varchar(255) NOT NULL COMMENT '消息类型',
                           `body` json NOT NULL COMMENT '消息内容',
                           `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '对方是否已读',
                           `is_recall` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否被撤回的消息',
                           `status` varchar(20) NOT NULL DEFAULT '' COMMENT '消息状态：\r\nunSend(未发送)\r\nsuccess(发送成功)\r\nfail(发送失败)',
                           `receive` int(11) NOT NULL DEFAULT '0' COMMENT '接收状态，0 = 未接收， 1 = 已接收',
                           `time` bigint(20) NOT NULL DEFAULT '0' COMMENT '消息时间，毫秒'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- 表的结构 `user`
--

CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL,
                        `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID',
                        `nickname` varchar(255) DEFAULT NULL COMMENT '昵称',
                        `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像地址',
                        `created_at` bigint(20) DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转储表的索引
--

--
-- 表的索引 `conversation`
--
ALTER TABLE `conversation`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `conver_id_user_id_unique` (`conversation_id`,`user_id`),
  ADD KEY `conver_id_user_id` (`conversation_id`,`user_id`);

--
-- 表的索引 `message`
--
ALTER TABLE `message`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `message_id_unique` (`message_id`);

--
-- 表的索引 `user`
--
ALTER TABLE `user`
    ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `userId_unique` (`user_id`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `conversation`
--
ALTER TABLE `conversation`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `message`
--
ALTER TABLE `message`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `user`
--
ALTER TABLE `user`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;
COMMIT;
