package com.training.controller;

import com.training.dto.ApiResponse;
import com.training.dto.FaceRecognitionRecordDto;
import com.training.entity.FaceRecognitionRecord;
import com.training.entity.User;
import com.training.repository.UserRepository;
import com.training.service.FaceRecognitionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人脸识别记录控制器
 */
@RestController
@RequestMapping("/api/face-records")
@CrossOrigin(origins = "*")
public class FaceRecognitionRecordController {
    
    @Autowired
    private FaceRecognitionRecordService faceRecognitionRecordService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 保存人脸识别记录
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FaceRecognitionRecord>> saveRecord(
            @RequestParam Long courseId,
            @RequestParam Long userId,
            @RequestParam BigDecimal similarity,
            @RequestParam String status,
            HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            FaceRecognitionRecord record = faceRecognitionRecordService.saveRecord(
                    courseId, userId, similarity, status, ipAddress, userAgent
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "保存记录成功", record));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("保存记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取课程的人脸识别记录（管理员）
     */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<FaceRecognitionRecordDto>>> getRecordsForAdmin(
            @RequestParam Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<FaceRecognitionRecord> records;
            
            if (startDate != null && endDate != null) {
                records = faceRecognitionRecordService.getRecordsByCourseAndDateRange(
                        courseId, startDate, endDate, pageable);
            } else {
                records = faceRecognitionRecordService.getRecordsByCourse(courseId, pageable);
            }
            
            Page<FaceRecognitionRecordDto> dtoPage = records.map(this::convertToDto);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取记录成功", dtoPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户的人脸识别记录
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<FaceRecognitionRecord>>> getRecordsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<FaceRecognitionRecord> records = faceRecognitionRecordService.getRecordsByUser(userId, pageable);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取记录成功", records));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取记录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 导出人脸识别记录（管理员）
     */
    @GetMapping("/admin/export")
    public ResponseEntity<ApiResponse<String>> exportRecords(
            @RequestParam Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            // 这里可以实现导出功能，暂时返回提示信息
            String downloadUrl = "/api/face-records/admin/export/download?courseId=" + courseId;
            if (startDate != null && endDate != null) {
                downloadUrl += "&startDate=" + startDate + "&endDate=" + endDate;
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "导出成功", downloadUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("导出失败: " + e.getMessage()));
        }
    }
    
    /**
     * 将实体转换为DTO
     */
    private FaceRecognitionRecordDto convertToDto(FaceRecognitionRecord record) {
        FaceRecognitionRecordDto dto = new FaceRecognitionRecordDto();
        dto.setId(record.getId());
        dto.setCourseId(record.getCourseId());
        dto.setUserId(record.getUserId());
        dto.setTimestamp(record.getTimestamp());
        dto.setSimilarity(record.getSimilarity());
        dto.setStatus(record.getStatus());
        dto.setIpAddress(record.getIpAddress());
        dto.setUserAgent(record.getUserAgent());
        
        // 获取用户名
        userRepository.findById(record.getUserId()).ifPresent(user -> {
            dto.setUserName(user.getUsername());
        });
        
        return dto;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}