package com.training.service;

import com.training.dto.CourseStudentProgressDto;
import com.training.entity.Course;
import com.training.entity.User;
import com.training.entity.UserCourse;
import com.training.repository.CourseRepository;
import com.training.repository.UserCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程学员学习记录服务
 * @author System
 */
@Service
public class CourseStudentProgressService {

    @Autowired
    private UserCourseRepository userCourseRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 获取课程的所有学员学习记录
     */
    public Page<CourseStudentProgressDto> getCourseStudentProgress(Long courseId, Pageable pageable, String searchKeyword) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        Page<UserCourse> userCourses;
        
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            // 按关键词搜索学员
            userCourses = userCourseRepository.findByUserUsernameContainingOrUserRealNameContainingAndCourseId(
                    searchKeyword.trim(), searchKeyword.trim(), courseId, pageable);
        } else {
            // 获取课程的所有学员
            userCourses = userCourseRepository.findByCourseId(courseId, pageable);
        }

        List<CourseStudentProgressDto> progressList = userCourses.getContent().stream()
                .map(userCourse -> convertToDto(userCourse))
                .collect(Collectors.toList());

        return new PageImpl<>(progressList, pageable, userCourses.getTotalElements());
    }

    /**
     * 将UserCourse实体转换为DTO
     */
    private CourseStudentProgressDto convertToDto(UserCourse userCourse) {
        CourseStudentProgressDto dto = new CourseStudentProgressDto();
        
        User user = userCourse.getUser();
        Course course = userCourse.getCourse();
        
        dto.setId(userCourse.getId());
        dto.setStudentId(user.getId());
        dto.setStudentName(user.getRealName());
        dto.setStudentEmail(user.getPhone()); // 使用phone代替email
        dto.setStudentUsername(user.getUsername());
        dto.setCourseId(course.getId());
        dto.setCourseTitle(course.getTitle());
        dto.setIsCompleted(userCourse.getIsCompleted());
        dto.setEnrollTime(userCourse.getEnrollTime());
        dto.setCompleteTime(userCourse.getCompleteTime());
        
        // 直接从UserCourse获取进度信息
        dto.setLearningDuration(0); // 暂时设为0，后续可以扩展
        dto.setWatchProgress(userCourse.getWatchProgress() != null ? userCourse.getWatchProgress() : 0);
        dto.setCompletionRate(userCourse.getWatchProgress() != null ? userCourse.getWatchProgress() / 100.0 : 0.0);
        dto.setLastStudyTime(userCourse.getLastStudyTime()); // 获取最后学习时间
        
        return dto;
    }
}