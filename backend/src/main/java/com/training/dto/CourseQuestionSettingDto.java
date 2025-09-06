package com.training.dto;

import java.util.List;

/**
 * 课程问题设置DTO
 */
public class CourseQuestionSettingDto {
    private Long courseId;
    private List<RoleQuestionSettingDto> roleSettings;
    private List<UserQuestionSettingDto> userSettings;

    // Getters and Setters
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public List<RoleQuestionSettingDto> getRoleSettings() {
        return roleSettings;
    }

    public void setRoleSettings(List<RoleQuestionSettingDto> roleSettings) {
        this.roleSettings = roleSettings;
    }

    public List<UserQuestionSettingDto> getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(List<UserQuestionSettingDto> userSettings) {
        this.userSettings = userSettings;
    }

    /**
     * 角色问题设置DTO
     */
    public static class RoleQuestionSettingDto {
        private Long roleId;
        private Settings settings;

        // Getters and Setters
        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public Settings getSettings() {
            return settings;
        }

        public void setSettings(Settings settings) {
            this.settings = settings;
        }

        public static class Settings {
            private List<TimePointDto> timePoints;

            public List<TimePointDto> getTimePoints() {
                return timePoints;
            }

            public void setTimePoints(List<TimePointDto> timePoints) {
                this.timePoints = timePoints;
            }
        }
    }

    /**
     * 用户问题设置DTO
     */
    public static class UserQuestionSettingDto {
        private Long userId;
        private Long roleId;
        private List<TimePointDto> timePoints;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public List<TimePointDto> getTimePoints() {
            return timePoints;
        }

        public void setTimePoints(List<TimePointDto> timePoints) {
            this.timePoints = timePoints;
        }
    }

    /**
     * 时间点DTO
     */
    public static class TimePointDto {
        private Integer time;
        private List<QuestionDto> questions;

        // Getters and Setters
        public Integer getTime() {
            return time;
        }

        public void setTime(Integer time) {
            this.time = time;
        }

        public List<QuestionDto> getQuestions() {
            return questions;
        }

        public void setQuestions(List<QuestionDto> questions) {
            this.questions = questions;
        }
    }

    /**
     * 问题DTO
     */
    public static class QuestionDto {
        private Long id;
        private String content;
        private String type;
        private List<String> options;
        private List<String> answers;
        private String explanation;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public List<String> getAnswers() {
            return answers;
        }

        public void setAnswers(List<String> answers) {
            this.answers = answers;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }
    }
}