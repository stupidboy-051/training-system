package com.training.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.dto.CourseQuestionSettingDto;
import com.training.entity.Course;
import com.training.repository.CourseRepository;
import com.training.service.CourseQuestionSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CourseQuestionSettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseQuestionSettingService courseQuestionSettingService;

    private Long testCourseId;

    @BeforeEach
    void setUp() {
        // 创建测试课程
        Course course = new Course();
        course.setTitle("测试课程");
        course.setDescription("用于测试的课程");
        course = courseRepository.save(course);
        testCourseId = course.getId();
    }

    @Test
    void testSaveAndGetQuestionSettings() throws Exception {
        // 创建测试DTO
        CourseQuestionSettingDto dto = new CourseQuestionSettingDto();
        dto.setCourseId(testCourseId);

        // 创建角色设置
        CourseQuestionSettingDto.RoleQuestionSettingDto roleSetting = new CourseQuestionSettingDto.RoleQuestionSettingDto();
        roleSetting.setRoleId(1L);
        
        CourseQuestionSettingDto.RoleQuestionSettingDto.Settings settings = new CourseQuestionSettingDto.RoleQuestionSettingDto.Settings();
        
        CourseQuestionSettingDto.TimePointDto timePoint = new CourseQuestionSettingDto.TimePointDto();
        timePoint.setTime(30);
        
        CourseQuestionSettingDto.QuestionDto question = new CourseQuestionSettingDto.QuestionDto();
        question.setId(1001L);
        timePoint.setQuestions(Collections.singletonList(question));
        
        settings.setTimePoints(Collections.singletonList(timePoint));
        roleSetting.setSettings(settings);
        
        dto.setRoleSettings(Collections.singletonList(roleSetting));

        // 测试保存设置
        mockMvc.perform(post("/api/course-question-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("设置保存成功")));

        // 测试获取设置
        mockMvc.perform(get("/api/course-question-settings/" + testCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId", is(testCourseId.intValue())))
                .andExpect(jsonPath("$.roleSettings", hasSize(1)))
                .andExpect(jsonPath("$.roleSettings[0].roleId", is(1)))
                .andExpect(jsonPath("$.roleSettings[0].settings.timePoints", hasSize(1)))
                .andExpect(jsonPath("$.roleSettings[0].settings.timePoints[0].time", is(30)))
                .andExpect(jsonPath("$.roleSettings[0].settings.timePoints[0].questions", hasSize(1)))
                .andExpect(jsonPath("$.roleSettings[0].settings.timePoints[0].questions[0].id", is(1001)));
    }

    @Test
    void testGetNonExistentCourseSettings() throws Exception {
        mockMvc.perform(get("/api/course-question-settings/99999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("课程不存在")));
    }

    @Test
    void testSaveEmptySettings() throws Exception {
        CourseQuestionSettingDto dto = new CourseQuestionSettingDto();
        dto.setCourseId(testCourseId);
        dto.setRoleSettings(Collections.emptyList());
        dto.setUserSettings(Collections.emptyList());

        mockMvc.perform(post("/api/course-question-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("设置保存成功")));
    }
}