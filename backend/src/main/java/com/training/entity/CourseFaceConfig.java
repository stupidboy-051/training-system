package com.training.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 课程人脸识别配置实体
 */
@Entity
@Table(name = "course_face_config")
public class CourseFaceConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "course_id", nullable = false, unique = true)
    private Long courseId;
    
    @Column(name = "face_recognition_enabled", nullable = false)
    private Boolean faceRecognitionEnabled = false;
    
    @Column(name = "face_recognition_frequency", nullable = false)
    private Integer faceRecognitionFrequency = 30;
    
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}