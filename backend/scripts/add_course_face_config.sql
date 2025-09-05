-- 课程人脸识别配置表
-- 用于存储课程的人脸识别相关配置

DROP TABLE IF EXISTS `course_face_config`;
CREATE TABLE `course_face_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `face_recognition_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启人脸识别 0-关闭 1-开启',
  `face_recognition_frequency` int NOT NULL DEFAULT 30 COMMENT '人脸识别频率（秒）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_course_id` (`course_id`) USING BTREE,
  KEY `idx_face_enabled` (`face_recognition_enabled`) USING BTREE,
  CONSTRAINT `fk_course_face_config_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程人脸识别配置表';

-- 插入一些测试数据
INSERT INTO `course_face_config` (`course_id`, `face_recognition_enabled`, `face_recognition_frequency`) VALUES
(1, 1, 30),
(2, 0, 60),
(3, 1, 15);