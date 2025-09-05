package com.training.controller;

import com.training.dto.ApiResponse;
import com.training.dto.CourseFaceConfigDto;
import com.training.entity.CourseFaceConfig;
import com.training.service.CourseFaceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 课程人脸识别配置控制器
 */
@RestController
@RequestMapping("/api/course-face-config")
@CrossOrigin(origins = "*")
public class CourseFaceConfigController {
    
    @Autowired
    private CourseFaceConfigService courseFaceConfigService;
    
    /**
     * 获取课程人脸识别配置
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseFaceConfigDto>> getCourseFaceConfig(@PathVariable Long courseId) {
        try {
            Optional<CourseFaceConfig> configOpt = courseFaceConfigService.getCourseFaceConfig(courseId);
            CourseFaceConfig config = configOpt.orElse(null);
            if (config == null) {
                // 如果配置不存在，返回默认配置
                CourseFaceConfigDto defaultDto = new CourseFaceConfigDto(
                    courseId,
                    false, // 默认关闭人脸识别
                    30     // 默认30秒频率
                );
                return ResponseEntity.ok(new ApiResponse<>(true, "获取配置成功", defaultDto));
            }
            
            CourseFaceConfigDto dto = new CourseFaceConfigDto(
                config.getCourseId(),
                config.getFaceRecognitionEnabled(),
                config.getFaceRecognitionFrequency()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "获取配置成功", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 保存或更新课程人脸识别配置
     */
    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseFaceConfig>> saveCourseFaceConfig(
            @PathVariable Long courseId,
            @RequestBody CourseFaceConfigDto configDto) {
        try {
            CourseFaceConfig config = courseFaceConfigService.saveCourseFaceConfig(
                    courseId,
                    configDto.getFaceRecognitionEnabled(),
                    configDto.getFaceRecognitionFrequency()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "保存配置成功", config));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("保存配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新人脸识别开关状态
     */
    @PatchMapping("/{courseId}/enabled")
    public ResponseEntity<ApiResponse<CourseFaceConfig>> updateFaceRecognitionEnabled(
            @PathVariable Long courseId,
            @RequestParam Boolean enabled) {
        try {
            CourseFaceConfig config = courseFaceConfigService.updateFaceRecognitionEnabled(courseId, enabled);
            return ResponseEntity.ok(new ApiResponse<>(true, "更新状态成功", config));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("更新状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新人脸识别频率
     */
    @PatchMapping("/{courseId}/frequency")
    public ResponseEntity<ApiResponse<CourseFaceConfig>> updateFaceRecognitionFrequency(
            @PathVariable Long courseId,
            @RequestParam Integer frequency) {
        try {
            CourseFaceConfig config = courseFaceConfigService.updateFaceRecognitionFrequency(courseId, frequency);
            return ResponseEntity.ok(new ApiResponse<>(true, "更新频率成功", config));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("更新频率失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量更新课程人脸识别配置
     */
    @PutMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchUpdateCourseFaceConfig(@RequestBody List<CourseFaceConfigDto> configs) {
        try {
            for (CourseFaceConfigDto config : configs) {
                courseFaceConfigService.saveCourseFaceConfig(
                        config.getCourseId(),
                        config.getFaceRecognitionEnabled(),
                        config.getFaceRecognitionFrequency()
                );
            }
            return ResponseEntity.ok(new ApiResponse<Void>(true, "批量更新成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("批量更新失败: " + e.getMessage()));
        }
    }
}