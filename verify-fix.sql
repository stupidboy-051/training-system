-- 验证修复是否成功
-- 检查courses表中是否包含新添加的字段
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'courses' 
AND COLUMN_NAME IN ('face_recognition_enabled', 'face_recognition_frequency');

-- 检查是否可以插入包含新字段的课程数据
INSERT INTO courses (title, description, video_url, price, is_online, face_recognition_enabled, face_recognition_frequency) 
VALUES ('测试修复课程', '测试人脸识别字段修复', 'http://test.com/video.mp4', 99.99, true, true, 30);

-- 查询刚插入的测试数据
SELECT id, title, face_recognition_enabled, face_recognition_frequency 
FROM courses 
WHERE title = '测试修复课程';

-- 清理测试数据
DELETE FROM courses WHERE title = '测试修复课程';