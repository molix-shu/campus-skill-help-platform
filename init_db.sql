-- 0. 创建数据库并使用
CREATE DATABASE IF NOT EXISTS `skill_help_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `skill_help_db`;

-- 1. 用户表 (记录学生信息与认证、信用情况)
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(64) NOT NULL COMMENT '微信openid',
  `nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `real_name` varchar(32) DEFAULT NULL COMMENT '真实姓名',
  `college` varchar(64) DEFAULT NULL COMMENT '所属学院',
  `major` varchar(64) DEFAULT NULL COMMENT '专业',
  `student_id` varchar(32) DEFAULT NULL COMMENT '学号',
  `auth_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '认证状态：0未认证，1认证中，2已认证，3认证失败',
  `credit_score` int(11) NOT NULL DEFAULT '50' COMMENT '信用积分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础信息表';

-- 2. 分类表 (用于技能和需求的分类查询，如：设计、编程、考试)
CREATE TABLE `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(32) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标URL',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序权重',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0禁用，1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能与需求分类表';

-- 3. 技能供给表 (用户发帖说：我会做XXX)
CREATE TABLE `skill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '发布者用户ID',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `title` varchar(128) NOT NULL COMMENT '技能标题',
  `description` text NOT NULL COMMENT '技能详细描述',
  `images` json DEFAULT NULL COMMENT '相关图片展示(JSON数组格式)',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签(逗号分隔)，如 #PR剪辑,#四级辅导',
  `service_mode` tinyint(4) NOT NULL DEFAULT '1' COMMENT '服务方式：1线上，2线下，3不限',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0下架，1正常上架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户技能供给表';

-- 4. 互助需求表 (用户发帖说：我需要XXX帮助)
CREATE TABLE `demand` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '求助者用户ID',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `title` varchar(128) NOT NULL COMMENT '需求标题',
  `description` text NOT NULL COMMENT '需求描述',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签(逗号分隔)',
  `expected_time` varchar(128) DEFAULT NULL COMMENT '期望完成时间或时间段',
  `reward_desc` varchar(128) DEFAULT NULL COMMENT '酬劳/感谢方式，如：请喝奶茶、互换技能',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0已关闭/撤销，1寻找中，2对接中，3已完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户互助需求表';

-- 5. 互助订单表 (你的开题难点破解：用这个表管理互助的整个状态节点)
CREATE TABLE `help_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单流水号',
  `type` tinyint(4) NOT NULL COMMENT '类型：1求助者申请技能，2技能者接单需求',
  `target_id` bigint(20) NOT NULL COMMENT '对应的 技能ID 或 需求ID',
  `publisher_id` bigint(20) NOT NULL COMMENT '技能或信息的发布者ID',
  `applicant_id` bigint(20) NOT NULL COMMENT '申请接单者ID',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0待同意(MATCHING)，1进行中(PROCESSING)，2待评价(COMPLETED)，3已关闭(CLOSED)，4已取消',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因（可选）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_publisher_id` (`publisher_id`),
  KEY `idx_applicant_id` (`applicant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互助流程业务状态订单表';

-- 6. 评价反馈表 (用于提升平台信任度)
CREATE TABLE `evaluation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(20) NOT NULL COMMENT '关联的互助订单表ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '评价人ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '被评价人ID',
  `score` int(11) NOT NULL DEFAULT '5' COMMENT '评分(1-5星)',
  `content` text COMMENT '评价内容留言',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互助评价反馈表';

-- 7. 聊天消息表 (用于订单内的即时通讯)
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  `content_type` varchar(16) DEFAULT 'text',
  `content` longtext NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
