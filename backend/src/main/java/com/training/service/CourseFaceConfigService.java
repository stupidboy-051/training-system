package com.training.service;

import com.training.entity.CourseFaceConfig;
import com.training.repository.CourseFaceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 课程人脸识别配置服务
 */
@Service
public class CourseFaceConfigService {
    
    @Autowired
    private CourseFaceConfigRepository courseFaceConfigRepository;
    
    /**
     * 获取课程人脸识别配置
     * @param courseId 课程ID
     * @return 人脸识别配置
     */
    public Optional<CourseFaceConfig> getCourseFaceConfig(Long courseId) {
        return courseFaceConfigRepository.findByCourseId(courseId);
    }
    
    /**
     * 保存或更新课程人脸识别配置
     * @param courseId 课程ID
     * @param enabled 是否开启人脸识别
     * @param frequency 人脸识别频率（秒）
     * @return 保存后的配置
     */
    @Transactional
    public CourseFaceConfig saveCourseFaceConfig(Long courseId, Boolean enabled, Integer frequency) {
        CourseFaceConfig config = courseFaceConfigRepository.findByCourseId(courseId)
                .orElse(new CourseFaceConfig());
        
        config.setCourseId(courseId);
        config.setFaceRecognitionEnabled(enabled);
        config.setFaceRecognitionFrequency(frequency);
        
        return courseFaceConfigRepository.save(config);
    }
    
    /**
     * 更新人脸识别开关状态
     * @param courseId 课程ID
     * @param enabled 是否开启
     * @return 更新后的配置
     */
    @Transactional
    public CourseFaceConfig updateFaceRecognitionEnabled(Long courseId, Boolean enabled) {
        CourseFaceConfig config = courseFaceConfigRepository.findByCourseId(courseId)
                .orElse(new CourseFaceConfig());
        
        config.setCourseId(courseId);
        config.setFaceRecognitionEnabled(enabled);
        if (config.getFaceRecognitionFrequency() == null) {
            config.setFaceRecognitionFrequency(30); // 默认值
        }
        
        return courseFaceConfigRepository.save(config);
    }
    
    /**
     * 更新人脸识别频率
     * @param courseId 课程ID
     * @param frequency 频率（秒）
     * @return 更新后的配置
     */
    @Transactional
    public CourseFaceConfig updateFaceRecognitionFrequency(Long courseId, Integer frequency) {
        CourseFaceConfig config = courseFaceConfigRepository.findByCourseId(courseId)
                .orElse(new CourseFaceConfig());
        
        config.setCourseId(courseId);
        config.setFaceRecognitionFrequency(frequency);
        if (config.getFaceRecognitionEnabled() == null) {
            config.setFaceRecognitionEnabled(false);
        }
        
        return courseFaceConfigRepository.save(config);
    }
    
    /**
     * 删除课程人脸识别配置
     * @param courseId 课程ID
     */
    @Transactional
    public void deleteCourseFaceConfig(Long courseId) {
        courseFaceConfigRepository.deleteByCourseId(courseId);
    }
    
    /**
     * 检查课程是否存在人脸识别配置
     * @param courseId 课程ID
     * @return 是否存在
     */
    public boolean existsByCourseId(Long courseId) {
        return courseFaceConfigRepository.existsByCourseId(courseId);
    }
}