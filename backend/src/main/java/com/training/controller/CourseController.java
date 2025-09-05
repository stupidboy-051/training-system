package com.training.controller;

import com.training.config.CompareFace;
import com.training.config.FaceDetector;
import com.training.dto.ApiResponse;
import com.training.dto.CourseDto;
import com.training.dto.MyCourseDto;
import com.training.dto.UserCourseListDto;
import com.training.entity.Course;
import com.training.entity.User;
import com.training.entity.UserCourse;
import com.training.repository.UserCourseRepository;
import com.training.service.CourseService;
import com.training.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author 14798
 */
@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCourseRepository userCourseRepository;

    // 管理员分页获取课程列表
    @GetMapping("/admin/page")
    public ResponseEntity<ApiResponse<Page<Course>>> getCoursesForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Course> courses = courseService.findCoursesForAdmin(pageable, keyword);
            return ResponseEntity.ok(ApiResponse.success("获取课程列表成功", courses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取课程列表失败: " + e.getMessage()));
        }
    }

    // 学员获取我的课程（已选课程）
    @GetMapping("/student/my-courses")
    public ResponseEntity<ApiResponse<Page<MyCourseDto>>> getMyCourses(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<MyCourseDto> userCourses = courseService.findMyCoursesDto(userId, pageable);
            return ResponseEntity.ok(ApiResponse.success("获取我的课程成功", userCourses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取我的课程失败: " + e.getMessage()));
        }
    }

    // 学员获取可购买的课程（未选课程）
    @GetMapping("/student/available-courses")
    public ResponseEntity<ApiResponse<Page<Course>>> getAvailableCourses(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long roleId) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Course> courses = courseService.findAvailableCourses(userId, roleId, pageable);
            return ResponseEntity.ok(ApiResponse.success("获取可购买课程成功", courses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取可购买课程失败: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        List<Course> courses = courseService.findAllCourses();
        return ResponseEntity.ok(ApiResponse.success("获取课程列表成功", courses));
    }

    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<Course>>> getOnlineCourses() {
        List<Course> courses = courseService.findOnlineCourses();
        return ResponseEntity.ok(ApiResponse.success("获取在线课程成功", courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable Long id) {
        try {
            return courseService.findById(id)
                    .map(course -> ResponseEntity.ok(ApiResponse.success("获取课程成功", course)))
                    .orElse(ResponseEntity.badRequest().body(ApiResponse.error("课程不存在")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取课程失败: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody CourseDto courseDto) {
        try {
            Course savedCourse = courseService.createOrUpdateCourse(null, courseDto);
            return ResponseEntity.ok(ApiResponse.success("创建课程成功", savedCourse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 更新课程状态（如果已过期则下架）
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Course>> updateCourseStatus(@PathVariable Long id) {
        try {
            Course updatedCourse = courseService.updateCourseStatus(id);
            return ResponseEntity.ok(ApiResponse.success("更新课程状态成功", updatedCourse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("更新课程状态失败: " + e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(@PathVariable Long id, @RequestBody CourseDto courseDto) {
        try {
            Course updatedCourse = courseService.createOrUpdateCourse(id, courseDto);
            return ResponseEntity.ok(ApiResponse.success("更新课程成功", updatedCourse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok(ApiResponse.success("删除课程成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<ApiResponse<Void>> enrollCourse(@PathVariable Long id, @RequestParam Long userId) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Course course = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));

            courseService.enrollCourse(user, course);
            return ResponseEntity.ok(ApiResponse.success("选课成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UserCourseListDto>>> getUserCourses(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            List<UserCourseListDto> userCourses = courseService.getUserCoursesDto(user);
            return ResponseEntity.ok(ApiResponse.success("获取用户课程成功", userCourses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/unroll/{courseId}")
    public ResponseEntity<ApiResponse<Void>> unrollCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("用户未登录"));
            }
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Course course = courseService.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));

            courseService.unrollCourse(user, course);
            return ResponseEntity.ok(ApiResponse.success("成功退出课程"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}/progress/{userId}")
    public ResponseEntity<ApiResponse<UserCourseListDto>> getUserCourseProgress(
            @PathVariable Long id,
            @PathVariable Long userId) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Course course = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));

            Optional<UserCourse> userCourseOpt = userCourseRepository.findByUserAndCourse(user, course);
            if (userCourseOpt.isPresent()) {
                UserCourseListDto dto = courseService.convertToUserCourseListDto(userCourseOpt.get());
                return ResponseEntity.ok(ApiResponse.success("获取用户课程进度成功", dto));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户未选择该课程"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<Void>> updateProgress(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam Integer progress,
            @RequestParam(required = false) String lastStudyTime) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Course course = courseService.findById(id)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));

            courseService.updateCourseProgress(user, course, progress);
            return ResponseEntity.ok(ApiResponse.success("更新进度成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 管理员手动下架过期课程
    @PostMapping("/admin/offline-expired")
    public ResponseEntity<ApiResponse<Integer>> offlineExpiredCoursesManually() {
        try {
            int count = courseService.offlineExpiredCourses();
            if (count > 0) {
                return ResponseEntity.ok(ApiResponse.success("手动下架成功，下架了 " + count + " 门已过期课程", count));
            } else {
                return ResponseEntity.ok(ApiResponse.success("没有需要下架的课程", count));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("下架失败: " + e.getMessage()));
        }
    }
    @Autowired
    private FaceDetector faceDetector;
    @PostMapping("/face")
    public ResponseEntity<ApiResponse<Integer>> registerFace(@RequestParam String faceUrl) {
        try {
            boolean isFace = faceDetector.detect(faceUrl);
            if (isFace) {
                // TODO: 存入数据库
                // contentService.insertAvatar(userId, faceUrl);
                return ResponseEntity.ok(ApiResponse.success("人脸识别成功", 0));
            } else {
                return ResponseEntity.ok(ApiResponse.success("人脸识别失败", 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("人脸识别失败: " + e.getMessage()));
        }
    }
    @Autowired
    private CompareFace compareFace;
    @PostMapping("/compareFirst")
    public String compareFacesFirst(@RequestParam String newFaceUrl) {
        try {
            String urlA = "https://web-itlasyd.oss-cn-beijing.aliyuncs.com/a69c359014c0db00713a7622a3050927552366d6b3bfdedae3247682934321f9.jpg";
            String urlB = newFaceUrl;
            boolean isSimilar = compareFace.compare(urlA, urlB);
            return isSimilar ? "1" : "0";
        } catch (Exception e) {
            e.printStackTrace();
            return "Face comparison failed: " + e.getMessage();
        }
    }

    @PostMapping("/face1")
    public ResponseEntity<ApiResponse<Integer>> verifyFace(
            @RequestParam Long userId,
            @RequestParam String faceUrl) {
        try {
            // 1. 查找用户
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            String dbFaceUrl = user.getFacePhotoUrl();
            if (dbFaceUrl == null || dbFaceUrl.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户未录入人脸信息"));
            }

            // 2. 调用 compare 方法
            boolean isSimilar = compareFace.compare(dbFaceUrl, faceUrl);

            if (isSimilar) {
                return ResponseEntity.ok(ApiResponse.success("人脸比对成功", 0));
            } else {
                return ResponseEntity.ok(ApiResponse.success("人脸比对失败", 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("人脸识别失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{courseId}/progress/{userId}")
    public ResponseEntity<ApiResponse<Void>> resetProgress(
            @PathVariable Long courseId,
            @PathVariable Long userId) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Course course = courseService.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));

            courseService.resetCourseProgress(user, course);
            return ResponseEntity.ok(ApiResponse.success("进度已重置"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
