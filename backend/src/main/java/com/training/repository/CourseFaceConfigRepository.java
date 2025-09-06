package com.training.repository;

import com.training.entity.CourseFaceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 课程人脸识别配置仓库接口
 */
@Repository
public interface CourseFaceConfigRepository extends JpaRepository<CourseFaceConfig, Long> {
    
    /**
     * 根据课程ID查找人脸识别配置
     * @param courseId 课程ID
     * @return 人脸识别配置
     */
    Optional<CourseFaceConfig> findByCourseId(Long courseId);
    
    /**
     * 根据课程ID删除人脸识别配置
     * @param courseId 课程ID
     */
    void deleteByCourseId(Long courseId);
    
    /**
     * 检查课程是否存在人脸识别配置
     * @param courseId 课程ID
     * @return 是否存在
     */
    boolean existsByCourseId(Long courseId);
}