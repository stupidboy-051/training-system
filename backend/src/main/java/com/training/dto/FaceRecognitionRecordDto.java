package com.training.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人脸识别记录DTO
 */
public class FaceRecognitionRecordDto {
    private Long id;
    private Long courseId;
    private Long userId;
    private String userName;
    private LocalDateTime timestamp;
    private BigDecimal similarity;
    private String status;
    private String ipAddress;
    private String userAgent;
    
    public FaceRecognitionRecordDto() {}
    
    public FaceRecognitionRecordDto(Long id, Long courseId, Long userId, String userName, 
                                    LocalDateTime timestamp, BigDecimal similarity, String status, 
                                    String ipAddress, String userAgent) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timestamp;
        this.similarity = similarity;
        this.status = status;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public BigDecimal getSimilarity() {
        return similarity;
    }
    
    public void setSimilarity(BigDecimal similarity) {
        this.similarity = similarity;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}