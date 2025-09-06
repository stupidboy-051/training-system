-- 课程问题弹题设置表
-- 用于存储课程中不同角色和用户的问题弹题设置
-- 适用于远程MySQL数据库

-- 如果表已存在，先删除
DROP TABLE IF EXISTS `course_question_settings`;

-- 创建表
CREATE TABLE `course_question_settings` (
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
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='课程问题弹题设置表';

-- 添加性能优化索引
-- 用于支持高频查询场景
CREATE INDEX `idx_created_time` ON `course_question_settings` (`create_time`);
CREATE INDEX `idx_updated_time` ON `course_question_settings` (`update_time`);