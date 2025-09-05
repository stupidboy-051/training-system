-- 验证最后学习时间是否正确保存的SQL查询
SELECT 
    u.username,
    c.title as course_title,
    uc.watch_progress,
    uc.last_study_time,
    uc.is_completed,
    uc.complete_time
FROM user_courses uc
JOIN users u ON uc.user_id = u.id
JOIN courses c ON uc.course_id = c.id
WHERE u.id = 1 AND c.id = 1;