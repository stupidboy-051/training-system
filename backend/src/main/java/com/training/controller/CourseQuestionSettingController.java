package com.training.controller;

import com.training.dto.ApiResponse;
import com.training.dto.CourseQuestionSettingDto;
import com.training.service.CourseQuestionSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 课程问题设置控制器
 * 处理课程中问题弹题设置的HTTP请求
 */
@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseQuestionSettingController {

    @Autowired
    private CourseQuestionSettingService courseQuestionSettingService;

    /**
     * 保存课程问题设置
     * POST /api/courses/{courseId}/question-settings
     */
    @PostMapping("/{courseId}/question-settings")
    public ResponseEntity<ApiResponse<Void>> saveQuestionSettings(
            @PathVariable Long courseId,
            @RequestBody CourseQuestionSettingDto dto) {
        try {
            // 确保courseId与DTO中的courseId一致
            if (!courseId.equals(dto.getCourseId())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("课程ID不匹配"));
            }
            
            courseQuestionSettingService.saveQuestionSettings(dto);
            return ResponseEntity.ok(ApiResponse.success("弹题设置保存成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("保存弹题设置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取课程问题设置
     * GET /api/courses/{courseId}/question-settings
     */
    @GetMapping("/{courseId}/question-settings")
    public ResponseEntity<ApiResponse<CourseQuestionSettingDto>> getQuestionSettings(
            @PathVariable Long courseId) {
        try {
            CourseQuestionSettingDto settings = courseQuestionSettingService.getQuestionSettings(courseId);
            return ResponseEntity.ok(ApiResponse.success("获取弹题设置成功", settings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取弹题设置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定用户和角色的课程问题设置
     * GET /api/courses/{courseId}/question-settings/user/{userId}
     */
    @GetMapping("/{courseId}/question-settings/user/{userId}")
    public ResponseEntity<ApiResponse<CourseQuestionSettingDto>> getUserQuestionSettings(
            @PathVariable Long courseId,
            @PathVariable Long userId) {
        try {
            CourseQuestionSettingDto settings = courseQuestionSettingService.getUserQuestionSettings(courseId, userId);
            return ResponseEntity.ok(ApiResponse.success("获取用户弹题设置成功", settings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户弹题设置失败: " + e.getMessage()));
        }
    }
}