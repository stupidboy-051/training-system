package com.training.dto;

/**
 * 课程人脸识别配置DTO
 */
public class CourseFaceConfigDto {
    private Long courseId;
    private Boolean faceRecognitionEnabled;
    private Integer faceRecognitionFrequency;
    
    public CourseFaceConfigDto() {}
    
    public CourseFaceConfigDto(Long courseId, Boolean faceRecognitionEnabled, Integer faceRecognitionFrequency) {
        this.courseId = courseId;
        this.faceRecognitionEnabled = faceRecognitionEnabled;
        this.faceRecognitionFrequency = faceRecognitionFrequency;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public Boolean getFaceRecognitionEnabled() {
        return faceRecognitionEnabled;
    }
    
    public void setFaceRecognitionEnabled(Boolean faceRecognitionEnabled) {
        this.faceRecognitionEnabled = faceRecognitionEnabled;
    }
    
    public Integer getFaceRecognitionFrequency() {
        return faceRecognitionFrequency;
    }
    
    public void setFaceRecognitionFrequency(Integer faceRecognitionFrequency) {
        this.faceRecognitionFrequency = faceRecognitionFrequency;
    }
}