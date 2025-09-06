package com.training.repository;

import com.training.entity.FaceRecognitionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人脸识别记录仓库接口
 */
@Repository
public interface FaceRecognitionRecordRepository extends JpaRepository<FaceRecognitionRecord, Long> {
    
    /**
     * 根据课程ID分页查询人脸识别记录
     * @param courseId 课程ID
     * @param pageable 分页信息
     * @return 人脸识别记录分页
     */
    Page<FaceRecognitionRecord> findByCourseIdOrderByTimestampDesc(Long courseId, Pageable pageable);
    
    /**
     * 根据课程ID和时间范围查询人脸识别记录
     * @param courseId 课程ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageable 分页信息
     * @return 人脸识别记录分页
     */
    Page<FaceRecognitionRecord> findByCourseIdAndTimestampBetweenOrderByTimestampDesc(
            Long courseId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * 根据用户ID查询人脸识别记录
     * @param userId 用户ID
     * @param pageable 分页信息
     * @return 人脸识别记录分页
     */
    Page<FaceRecognitionRecord> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    /**
     * 统计课程的人脸识别记录数量
     * @param courseId 课程ID
     * @return 记录数量
     */
    long countByCourseId(Long courseId);
    
    /**
     * 统计课程某天的识别记录数量
     * @param courseId 课程ID
     * @param date 日期
     * @return 记录数量
     */
    @Query("SELECT COUNT(f) FROM FaceRecognitionRecord f WHERE f.courseId = :courseId AND DATE(f.timestamp) = DATE(:date)")
    long countByCourseIdAndDate(@Param("courseId") Long courseId, @Param("date") LocalDateTime date);
}