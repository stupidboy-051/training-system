package com.training.config;
import com.training.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CourseScheduleTask {

    @Autowired
    private final CourseService courseService;

    public CourseScheduleTask(CourseService courseService) {
        this.courseService = courseService;
    }

    // 每小时执行一次（整点触发）
    @Scheduled(cron = "0 0 * * * ?")
    @Bean
    public void autoOfflineExpiredCourses() {
        int count = courseService.offlineExpiredCourses();
        if (count > 0) {
            log.info("定时任务执行完成：下架了 {} 门已过期课程", count);
        } else {
            log.info("定时任务执行完成：没有需要下架的课程");
        }
    }
}