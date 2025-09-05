-- 检查courses表结构
DESCRIBE courses;

-- 检查course_face_config表结构
DESCRIBE course_face_config;

-- 查看courses表中是否有face_recognition_enabled字段
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'courses' 
AND COLUMN_NAME = 'face_recognition_enabled';