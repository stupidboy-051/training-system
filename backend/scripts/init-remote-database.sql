-- 远程数据库初始化脚本
-- 专为远程MySQL数据库优化

-- 设置字符集和时区
SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;
SET sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `training_system` 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE `training_system`;

-- 检查并创建必要的表
-- 如果表已存在，则跳过创建

-- 创建课程表（如果不存在）
CREATE TABLE IF NOT EXISTS `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `duration` int DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建角色表（如果不存在）
CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建课程可见角色关联表（如果不存在）
CREATE TABLE IF NOT EXISTS `course_visible_roles` (
  `course_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`course_id`,`role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `course_visible_roles_ibfk_1` 
    FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  CONSTRAINT `course_visible_roles_ibfk_2` 
    FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建课程问题弹题设置表
-- 此表是我们的核心新增表
CREATE TABLE IF NOT EXISTS `course_question_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID（为空表示用户特定设置）',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID（为空表示角色通用设置）',
  `time_point` int NOT NULL COMMENT '时间点（秒）',
  `question_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题ID列表，JSON格式',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_course_role` (`course_id`,`role_id`),
  KEY `idx_course_user` (`course_id`,`user_id`),
  KEY `idx_time_point` (`time_point`),
  KEY `idx_created_time` (`create_time`),
  KEY `idx_updated_time` (`update_time`),
  CONSTRAINT `course_question_settings_ibfk_1` 
    FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  CONSTRAINT `course_question_settings_ibfk_2` 
    FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  CONSTRAINT `course_question_settings_ibfk_3` 
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程问题弹题设置表';

-- 插入示例数据（仅用于测试）
-- 检查是否已有数据，避免重复插入
SET @course_count = (SELECT COUNT(*) FROM courses);
SET @role_count = (SELECT COUNT(*) FROM roles);
SET @user_count = (SELECT COUNT(*) FROM users);

-- 如果没有课程数据，插入示例课程
IF @course_count = 0 THEN
    INSERT INTO `courses` (`id`, `title`, `description`, `duration`, `create_time`) VALUES
    (1, 'Java基础课程', 'Java编程基础入门课程', 3600, NOW()),
    (2, 'Spring框架实战', 'Spring框架深度解析与实践', 7200, NOW()),
    (3, '数据库设计', '关系型数据库设计与优化', 5400, NOW());
END IF;

-- 如果没有角色数据，插入示例角色
IF @role_count = 0 THEN
    INSERT INTO `roles` (`id`, `name`, `description`, `create_time`) VALUES
    (1, '管理员', '系统管理员', NOW()),
    (2, '讲师', '课程讲师', NOW()),
    (3, '学生', '普通学员', NOW());
END IF;

-- 如果没有用户数据，插入示例用户
IF @user_count = 0 THEN
    INSERT INTO `users` (`id`, `username`, `email`, `password`, `create_time`) VALUES
    (1, 'admin', 'admin@training.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iOEcalwu', NOW()),
    (2, 'teacher1', 'teacher@training.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iOEcalwu', NOW()),
    (3, 'student1', 'student@training.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iOEcalwu', NOW());
END IF;

-- 插入课程角色关联
INSERT IGNORE INTO `course_visible_roles` (`course_id`, `role_id`) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2), (2, 3),
(3, 1), (3, 2), (3, 3);

-- 插入示例课程问题设置数据
-- 仅当表为空时插入
SET @setting_count = (SELECT COUNT(*) FROM course_question_settings);

IF @setting_count = 0 THEN
    INSERT INTO `course_question_settings` (`course_id`, `role_id`, `user_id`, `time_point`, `question_ids`, `create_time`) VALUES
    (1, 2, NULL, 300, '[1,2,3]', NOW()),
    (1, 2, NULL, 900, '[4,5,6]', NOW()),
    (1, 3, NULL, 600, '[7,8,9]', NOW()),
    (2, 2, NULL, 1200, '[10,11,12]', NOW()),
    (2, 1, 1, 1800, '[13,14,15]', NOW()),
    (3, 2, NULL, 1500, '[16,17,18]', NOW());
END IF;

-- 设置外键检查
SET foreign_key_checks = 1;

-- 显示数据库状态
SELECT 
    '数据库初始化完成' AS status,
    (SELECT COUNT(*) FROM courses) AS course_count,
    (SELECT COUNT(*) FROM roles) AS role_count,
    (SELECT COUNT(*) FROM users) AS user_count,
    (SELECT COUNT(*) FROM course_question_settings) AS setting_count;