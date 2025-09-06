package com.training.controller;

import com.training.dto.CourseStudentProgressDto;
import com.training.service.CourseStudentProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 课程学员学习记录控制器
 * @author System
 */
@RestController
@RequestMapping("/api/admin/courses")
@CrossOrigin(origins = "*")
public class CourseStudentProgressController {

    @Autowired
    private CourseStudentProgressService courseStudentProgressService;

    /**
     * 获取课程的所有学员学习记录
     */
    @GetMapping("/{courseId}/students/progress")
    public ResponseEntity<Page<CourseStudentProgressDto>> getCourseStudentProgress(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(defaultValue = "enrollTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<CourseStudentProgressDto> progressPage = courseStudentProgressService
                .getCourseStudentProgress(courseId, pageable, searchKeyword);
                
        return ResponseEntity.ok(progressPage);
    }
}