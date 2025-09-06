package com.training.dto;

import java.time.LocalDateTime;

/**
 * 课程学员学习记录DTO
 * @author System
 */
public class CourseStudentProgressDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentUsername;
    private Long courseId;
    private String courseTitle;
    private Integer learningDuration; // 学习时长（秒）
    private Double completionRate; // 完成度（0-1）
    private Integer watchProgress; // 观看进度百分比
    private Boolean isCompleted;
    private LocalDateTime lastStudyTime;
    private LocalDateTime enrollTime;
    private LocalDateTime completeTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public Integer getLearningDuration() {
        return learningDuration;
    }

    public void setLearningDuration(Integer learningDuration) {
        this.learningDuration = learningDuration;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Integer getWatchProgress() {
        return watchProgress;
    }

    public void setWatchProgress(Integer watchProgress) {
        this.watchProgress = watchProgress;
        if (watchProgress != null) {
            this.completionRate = watchProgress / 100.0;
        }
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public LocalDateTime getLastStudyTime() {
        return lastStudyTime;
    }

    public void setLastStudyTime(LocalDateTime lastStudyTime) {
        this.lastStudyTime = lastStudyTime;
    }

    public LocalDateTime getEnrollTime() {
        return enrollTime;
    }

    public void setEnrollTime(LocalDateTime enrollTime) {
        this.enrollTime = enrollTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }
}