package com.training.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.dto.CourseQuestionSettingDto;
import com.training.entity.Course;
import com.training.entity.CourseQuestionSetting;
import com.training.repository.CourseQuestionSettingRepository;
import com.training.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 课程问题设置服务类
 */
@Service
public class CourseQuestionSettingService {

    private static final Logger logger = LoggerFactory.getLogger(CourseQuestionSettingService.class);

    @Autowired
    private CourseQuestionSettingRepository courseQuestionSettingRepository;

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestionService questionService;

    /**
     * 保存课程问题设置
     */
    @Transactional
    public void saveQuestionSettings(CourseQuestionSettingDto dto) {
        Long courseId = dto.getCourseId();
        
        try {
            logger.info("开始保存课程问题设置 - 课程ID: {}", courseId);
            
            // 验证课程是否存在
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("课程不存在: " + courseId));

            // 先删除该课程的所有现有设置
            courseQuestionSettingRepository.deleteByCourseId(courseId);
            logger.debug("已删除课程ID {} 的所有现有设置", courseId);

            // 保存角色设置
            if (dto.getRoleSettings() != null) {
                for (CourseQuestionSettingDto.RoleQuestionSettingDto roleSetting : dto.getRoleSettings()) {
                    saveRoleSettings(course, roleSetting);
                }
            }

            // 保存用户特定设置
            if (dto.getUserSettings() != null) {
                for (CourseQuestionSettingDto.UserQuestionSettingDto userSetting : dto.getUserSettings()) {
                    saveUserSettings(course, userSetting);
                }
            }
            
            logger.info("课程问题设置保存成功 - 课程ID: {}", courseId);
        } catch (Exception e) {
            logger.error("保存课程问题设置失败 - 课程ID: {}", courseId, e);
            throw new RuntimeException("保存课程问题设置失败", e);
        }
    }

    /**
     * 保存角色设置
     */
    private void saveRoleSettings(Course course, CourseQuestionSettingDto.RoleQuestionSettingDto roleSetting) {
        if (roleSetting.getSettings() == null || roleSetting.getSettings().getTimePoints() == null) {
            return;
        }

        for (CourseQuestionSettingDto.TimePointDto timePoint : roleSetting.getSettings().getTimePoints()) {
            CourseQuestionSetting setting = new CourseQuestionSetting();
            setting.setCourse(course);
            setting.setRoleId(roleSetting.getRoleId());
            setting.setTimePoint(timePoint.getTime());
            
            if (timePoint.getQuestions() != null) {
                try {
                    List<Long> questionIds = timePoint.getQuestions().stream()
                            .map(CourseQuestionSettingDto.QuestionDto::getId)
                            .collect(Collectors.toList());
                    setting.setQuestionIds(objectMapper.writeValueAsString(questionIds));
                } catch (Exception e) {
                    logger.error("序列化问题ID失败 - 角色ID: {}", roleSetting.getRoleId(), e);
                    throw new RuntimeException("序列化问题ID失败", e);
                }
            } else {
                setting.setQuestionIds("[]");
            }
            
            courseQuestionSettingRepository.save(setting);
        }
    }

    /**
     * 保存用户特定设置
     */
    private void saveUserSettings(Course course, CourseQuestionSettingDto.UserQuestionSettingDto userSetting) {
        if (userSetting.getTimePoints() == null) {
            return;
        }

        for (CourseQuestionSettingDto.TimePointDto timePoint : userSetting.getTimePoints()) {
            CourseQuestionSetting setting = new CourseQuestionSetting();
            setting.setCourse(course);
            setting.setUserId(userSetting.getUserId());
            setting.setRoleId(userSetting.getRoleId()); // 保留角色信息
            setting.setTimePoint(timePoint.getTime());
            
            if (timePoint.getQuestions() != null) {
                try {
                    List<Long> questionIds = timePoint.getQuestions().stream()
                            .map(CourseQuestionSettingDto.QuestionDto::getId)
                            .collect(Collectors.toList());
                    setting.setQuestionIds(objectMapper.writeValueAsString(questionIds));
                } catch (Exception e) {
                    logger.error("序列化问题ID失败 - 用户ID: {}", userSetting.getUserId(), e);
                    throw new RuntimeException("序列化问题ID失败", e);
                }
            } else {
                setting.setQuestionIds("[]");
            }
            
            courseQuestionSettingRepository.save(setting);
        }
    }

    /**
     * 获取课程的问题设置
     */
    public CourseQuestionSettingDto getQuestionSettings(Long courseId) {
        try {
            logger.info("开始获取课程问题设置 - 课程ID: {}", courseId);
            
            // 验证课程是否存在
            courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("课程不存在: " + courseId));

            List<CourseQuestionSetting> allSettings = courseQuestionSettingRepository.findByCourseId(courseId);
            logger.debug("查询到的设置数量: {}", allSettings.size());
            
            CourseQuestionSettingDto dto = new CourseQuestionSettingDto();
            dto.setCourseId(courseId);

            // 按角色分组
            List<CourseQuestionSettingDto.RoleQuestionSettingDto> roleSettings = allSettings.stream()
                    .filter(s -> s.getRoleId() != null && s.getUserId() == null)
                    .collect(Collectors.groupingBy(CourseQuestionSetting::getRoleId))
                    .entrySet().stream()
                    .map(entry -> {
                        CourseQuestionSettingDto.RoleQuestionSettingDto roleSetting = new CourseQuestionSettingDto.RoleQuestionSettingDto();
                        roleSetting.setRoleId(entry.getKey());
                        
                        CourseQuestionSettingDto.RoleQuestionSettingDto.Settings settings = new CourseQuestionSettingDto.RoleQuestionSettingDto.Settings();
                        settings.setTimePoints(entry.getValue().stream()
                                .map(this::convertToTimePointDto)
                                .collect(Collectors.toList()));
                        roleSetting.setSettings(settings);
                        
                        return roleSetting;
                    })
                    .collect(Collectors.toList());

            // 按用户分组
            List<CourseQuestionSettingDto.UserQuestionSettingDto> userSettings = allSettings.stream()
                    .filter(s -> s.getUserId() != null)
                    .collect(Collectors.groupingBy(CourseQuestionSetting::getUserId))
                    .entrySet().stream()
                    .map(entry -> {
                        CourseQuestionSettingDto.UserQuestionSettingDto userSetting = new CourseQuestionSettingDto.UserQuestionSettingDto();
                        userSetting.setUserId(entry.getKey());
                        userSetting.setRoleId(entry.getValue().get(0).getRoleId()); // 获取第一个设置的角色ID
                        userSetting.setTimePoints(entry.getValue().stream()
                                .map(this::convertToTimePointDto)
                                .collect(Collectors.toList()));
                        return userSetting;
                    })
                    .collect(Collectors.toList());

            dto.setRoleSettings(roleSettings);
            dto.setUserSettings(userSettings);

            logger.info("课程问题设置获取成功 - 课程ID: {}", courseId);
            return dto;
            
        } catch (Exception e) {
            logger.error("获取课程问题设置失败 - 课程ID: {}", courseId, e);
            throw new RuntimeException("获取课程问题设置失败", e);
        }
    }

    /**
     * 将实体转换为DTO（仅包含基本信息）
     */
    private CourseQuestionSettingDto.TimePointDto convertToTimePointDto(CourseQuestionSetting setting) {
        CourseQuestionSettingDto.TimePointDto timePointDto = new CourseQuestionSettingDto.TimePointDto();
        timePointDto.setTime(setting.getTimePoint());
        
        if (setting.getQuestionIds() != null && !setting.getQuestionIds().trim().isEmpty()) {
            try {
                List<Long> questionIdList = objectMapper.readValue(setting.getQuestionIds(), new TypeReference<List<Long>>() {});
                
                List<CourseQuestionSettingDto.QuestionDto> questions = questionIdList.stream()
                        .map(questionId -> {
                            CourseQuestionSettingDto.QuestionDto questionDto = new CourseQuestionSettingDto.QuestionDto();
                            questionDto.setId(questionId);
                            return questionDto;
                        })
                        .collect(Collectors.toList());
                        
                timePointDto.setQuestions(questions);
            } catch (Exception e) {
                logger.warn("转换题目ID失败 - 设置ID: {}", setting.getId(), e);
                timePointDto.setQuestions(Collections.emptyList());
            }
        } else {
            timePointDto.setQuestions(Collections.emptyList());
        }
        
        return timePointDto;
    }
    
    /**
     * 将实体转换为DTO，包含题目完整信息
     */
    private CourseQuestionSettingDto.TimePointDto convertToTimePointDtoWithFullQuestion(CourseQuestionSetting setting) {
        CourseQuestionSettingDto.TimePointDto timePointDto = new CourseQuestionSettingDto.TimePointDto();
        timePointDto.setTime(setting.getTimePoint());
        
        if (setting.getQuestionIds() != null && !setting.getQuestionIds().trim().isEmpty()) {
            try {
                List<Long> questionIdList = objectMapper.readValue(setting.getQuestionIds(), new TypeReference<List<Long>>() {});
                
                // 获取题目完整信息
                List<CourseQuestionSettingDto.QuestionDto> questions = questionIdList.stream()
                        .map(questionId -> {
                            // 获取题目详细信息
                            return questionService.findById(questionId)
                                    .map(question -> {
                                        CourseQuestionSettingDto.QuestionDto questionDto = new CourseQuestionSettingDto.QuestionDto();
                                        questionDto.setId(question.getId());
                                        questionDto.setContent(question.getContent());
                                        questionDto.setType(question.getType());
                                        
                                        // 设置选项
                                        if (question.getOptions() != null) {
                                            List<String> options = question.getOptions().stream()
                                                    .map(option -> option.getOptionContent())
                                                    .collect(Collectors.toList());
                                            questionDto.setOptions(options);
                                        }
                                        
                                        // 设置答案
                                        if (question.getAnswer() != null && !question.getAnswer().trim().isEmpty()) {
                                            String answerStr = question.getAnswer().trim();
                                            if (answerStr.contains(",")) {
                                                // 多选题：A,B 格式
                                                questionDto.setAnswers(Arrays.asList(answerStr.split(",")));
                                            } else {
                                                // 单选题、判断题、填空题、简答题：单个答案
                                                questionDto.setAnswers(Arrays.asList(answerStr));
                                            }
                                        }
                                        
                                        questionDto.setExplanation(question.getExplanation());
                                        return questionDto;
                                    })
                                    .orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                        
                timePointDto.setQuestions(questions);
            } catch (Exception e) {
                logger.warn("获取题目完整信息失败 - 设置ID: {}", setting.getId(), e);
                timePointDto.setQuestions(Collections.emptyList());
            }
        } else {
            timePointDto.setQuestions(Collections.emptyList());
        }
        
        return timePointDto;
    }

    /**
     * 获取特定用户和角色的课程问题设置
     * 优先返回用户特定设置，如果没有则返回角色设置
     */
    public CourseQuestionSettingDto getUserQuestionSettings(Long courseId, Long userId) {
        try {
            logger.info("开始获取用户特定课程问题设置 - 课程ID: {}, 用户ID: {}", courseId, userId);
            
            // 验证课程是否存在
            courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("课程不存在: " + courseId));

            List<CourseQuestionSetting> allSettings = courseQuestionSettingRepository.findByCourseId(courseId);
            logger.debug("查询到的总设置数量: {}", allSettings.size());
            
            CourseQuestionSettingDto dto = new CourseQuestionSettingDto();
            dto.setCourseId(courseId);

            // 1. 优先获取用户特定设置
            List<CourseQuestionSetting> userSpecificSettings = allSettings.stream()
                    .filter(s -> userId.equals(s.getUserId()))
                    .collect(Collectors.toList());

            // 2. 如果没有用户特定设置，获取用户角色设置
            if (userSpecificSettings.isEmpty()) {
                logger.debug("未找到用户特定设置，尝试获取角色设置");
                
                // 这里应该根据实际业务逻辑获取用户角色
                // 暂时获取所有角色设置
                List<CourseQuestionSetting> roleSettings = allSettings.stream()
                        .filter(s -> s.getRoleId() != null && s.getUserId() == null)
                        .collect(Collectors.toList());
                
                if (!roleSettings.isEmpty()) {
                    CourseQuestionSettingDto.RoleQuestionSettingDto roleSetting = new CourseQuestionSettingDto.RoleQuestionSettingDto();
                    roleSetting.setRoleId(roleSettings.get(0).getRoleId());
                    
                    CourseQuestionSettingDto.RoleQuestionSettingDto.Settings settings = new CourseQuestionSettingDto.RoleQuestionSettingDto.Settings();
                    settings.setTimePoints(roleSettings.stream()
                            .map(this::convertToTimePointDtoWithFullQuestion)
                            .collect(Collectors.toList()));
                    roleSetting.setSettings(settings);
                    
                    dto.setRoleSettings(Collections.singletonList(roleSetting));
                }
            } else {
                // 使用用户特定设置
                logger.debug("找到用户特定设置数量: {}", userSpecificSettings.size());
                
                CourseQuestionSettingDto.UserQuestionSettingDto userSetting = new CourseQuestionSettingDto.UserQuestionSettingDto();
                userSetting.setUserId(userId);
                userSetting.setRoleId(userSpecificSettings.get(0).getRoleId());
                userSetting.setTimePoints(userSpecificSettings.stream()
                        .map(this::convertToTimePointDtoWithFullQuestion)
                        .collect(Collectors.toList()));
                
                dto.setUserSettings(Collections.singletonList(userSetting));
            }

            logger.info("用户特定课程问题设置获取成功 - 课程ID: {}, 用户ID: {}", courseId, userId);
            return dto;
            
        } catch (Exception e) {
            logger.error("获取用户特定课程问题设置失败 - 课程ID: {}, 用户ID: {}", courseId, userId, e);
            throw new RuntimeException("获取用户特定课程问题设置失败", e);
        }
    }
}