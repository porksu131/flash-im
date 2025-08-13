-- 用户表
DROP TABLE IF EXISTS flashim_user;  
CREATE TABLE `flashim_user` (
                        `uid` bigint NOT NULL COMMENT '用户ID',
                        `user_name` varchar(50) NOT NULL COMMENT '用户名',
                        `password` varchar(100) NOT NULL COMMENT '密码',
                        `phone` varchar(100) DEFAULT NULL COMMENT '手机',
                        `enabled_flag` tinyint(1) DEFAULT 1 COMMENT '是否可用',
                        PRIMARY KEY (`uid`),
                        UNIQUE KEY `idx_user_name` (`user_name`) COMMENT '用户名唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 好友关系表
DROP TABLE IF EXISTS flashim_friend_relation;  
CREATE TABLE `flashim_friend_relation` (
                                   `id` bigint NOT NULL COMMENT '主键ID',
                                   `uid` bigint NOT NULL COMMENT '用户ID',
                                   `friend_id` bigint NOT NULL COMMENT '好友ID',
                                   `user_name` varchar(50) DEFAULT NULL COMMENT '用户名称(冗余)',
                                   `friend_name` varchar(50) DEFAULT NULL COMMENT '好友名称(冗余)',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uniq_uid_friend` (`uid`,`friend_id`) COMMENT '防止重复添加',
                                   KEY `idx_uid` (`uid`) COMMENT '用户ID索引',
                                   KEY `idx_friend_id` (`friend_id`) COMMENT '好友ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 聊天消息表
DROP TABLE IF EXISTS flashim_chat_message;  
CREATE TABLE `flashim_chat_message` (
                               `message_id` bigint NOT NULL COMMENT '消息ID',
                               `message_content` text NOT NULL COMMENT '消息内容',
                               `message_to` bigint NOT NULL COMMENT '接收者ID',
                               `message_to_name` varchar(50) DEFAULT NULL COMMENT '接收者名称',
                               `message_from` bigint NOT NULL COMMENT '发送者ID',
                               `message_from_name` varchar(50) DEFAULT NULL COMMENT '发送者名称',
                               `message_type` int(1) NOT NULL DEFAULT '0' COMMENT '消息类型(0:文字 1:图片 2:语音)',
                               `session_id` varchar(120) DEFAULT NULL COMMENT '会话id',
                               `sequence_id` bigint NOT NULL COMMENT '服务端分配的会话消息序列号',
                               `client_msg_id` bigint NOT NULL COMMENT '客户端消息id',
                               `client_seq` bigint NOT NULL COMMENT '客户端临时的消息序列号',
                               `client_send_time` bigint NOT NULL COMMENT '客户端发送时间(时间戳)',
                               `create_time` bigint NOT NULL COMMENT '首次创建消息时间(时间戳)',
                               `update_time` bigint NOT NULL COMMENT '每次更新时间(时间戳)',
                               `read_Time` bigint NOT NULL COMMENT '已读消息时间(时间戳)',
                               `status` int(1) NOT NULL DEFAULT '1' COMMENT '状态(1:未发送 2:已发送 3: 已读)',
                               PRIMARY KEY (`message_id`),
                               KEY `idx_unread` (`message_to`,`message_from`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';


-- 文件信息表
CREATE TABLE `file_info` (
                             `id` bigint NOT NULL COMMENT '文件ID，雪花算法生成',
                             `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
                             `storage_name` varchar(255) NOT NULL COMMENT '存储的文件名',
                             `size` bigint NOT NULL COMMENT '文件大小(字节)',
                             `md5` varchar(32) NOT NULL COMMENT '文件MD5值',
                             `content_type` varchar(100) NOT NULL COMMENT '文件类型',
                             `path` varchar(500) NOT NULL COMMENT '文件存储路径',
                             `user_id` bigint NOT NULL COMMENT '上传用户ID',
                             `status` tinyint NOT NULL DEFAULT '0' COMMENT '上传状态(0:上传中,1:上传完成,2:上传失败)',
                             `create_time` bigint NOT NULL COMMENT '创建时间(时间戳)',
                             `update_time` bigint NOT NULL COMMENT '更新时间(时间戳)',
                             PRIMARY KEY (`id`),
                             KEY `idx_md5` (`md5`),
                             KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件信息表';

-- 文件分片信息表
CREATE TABLE `file_part_info` (
                                  `id` bigint NOT NULL COMMENT '分片ID，雪花算法生成',
                                  `file_id` bigint NOT NULL COMMENT '文件ID',
                                  `part_number` int NOT NULL COMMENT '分片序号',
                                  `part_size` bigint NOT NULL COMMENT '分片大小',
                                  `part_md5` varchar(32) DEFAULT NULL COMMENT '分片MD5',
                                  `status` tinyint NOT NULL DEFAULT '0' COMMENT '分片上传状态(0:未上传,1:已上传)',
                                  `create_time` bigint NOT NULL COMMENT '创建时间(时间戳)',
                                  `update_time` bigint NOT NULL COMMENT '更新时间(时间戳)',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_file_id` (`file_id`),
                                  KEY `idx_file_id_part_number` (`file_id`, `part_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件分片信息表';