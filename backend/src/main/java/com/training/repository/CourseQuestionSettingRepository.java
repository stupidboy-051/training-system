package com.training.repository;

import com.training.entity.CourseQuestionSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 课程问题设置仓库接口
 */
@Repository
public interface CourseQuestionSettingRepository extends JpaRepository<CourseQuestionSetting, Long> {

    /**
     * 根据课程ID查找所有问题设置
     */
    List<CourseQuestionSetting> findByCourseId(Long courseId);

    /**
     * 根据课程ID和角色ID查找问题设置
     */
    List<CourseQuestionSetting> findByCourseIdAndRoleId(Long courseId, Long roleId);

    /**
     * 根据课程ID和用户ID查找问题设置
     */
    List<CourseQuestionSetting> findByCourseIdAndUserId(Long courseId, Long userId);

    /**
     * 根据课程ID删除所有问题设置
     */
    void deleteByCourseId(Long courseId);

    /**
     * 根据课程ID和角色ID删除问题设置
     */
    void deleteByCourseIdAndRoleId(Long courseId, Long roleId);

    /**
     * 根据课程ID和用户ID删除问题设置
     */
    void deleteByCourseIdAndUserId(Long courseId, Long userId);

    /**
     * 根据课程ID、角色ID和时间点查找问题设置
     */
    @Query("SELECT c FROM CourseQuestionSetting c WHERE c.course.id = :courseId AND c.roleId = :roleId AND c.timePoint = :timePoint")
    List<CourseQuestionSetting> findByCourseIdAndRoleIdAndTimePoint(
            @Param("courseId") Long courseId, 
            @Param("roleId") Long roleId, 
            @Param("timePoint") Integer timePoint);

    /**
     * 根据课程ID、用户ID和时间点查找问题设置
     */
    @Query("SELECT c FROM CourseQuestionSetting c WHERE c.course.id = :courseId AND c.userId = :userId AND c.timePoint = :timePoint")
    List<CourseQuestionSetting> findByCourseIdAndUserIdAndTimePoint(
            @Param("courseId") Long courseId, 
            @Param("userId") Long userId, 
            @Param("timePoint") Integer timePoint);
}