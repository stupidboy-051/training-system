-- 人脸识别记录表
-- 用于存储学员在学习过程中的人脸识别记录

DROP TABLE IF EXISTS `face_recognition_records`;
CREATE TABLE `face_recognition_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `timestamp` datetime NOT NULL COMMENT '识别时间',
  `similarity` decimal(3,2) NOT NULL DEFAULT 0.00 COMMENT '相似度（0-1之间）',
  `status` varchar(20) NOT NULL DEFAULT 'success' COMMENT '识别状态 success-成功 failed-失败',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text COMMENT '用户代理信息',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_course_id` (`course_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_timestamp` (`timestamp`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  CONSTRAINT `fk_face_records_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_face_records_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人脸识别记录表';