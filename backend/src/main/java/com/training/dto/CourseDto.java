package com.training.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 14798
 */
@Data
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private Double price;
    private Boolean isOnline;
    private String coverImageUrl;
    private List<Long> visibleRoleIds;
    private LocalDateTime StartTime;
    private LocalDateTime EndTime;
    private Integer score;
    private Boolean faceRecognitionEnabled;
    private Integer faceRecognitionFrequency;}