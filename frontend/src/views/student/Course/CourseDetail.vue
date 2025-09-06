<template>
  <div class="course-detail">
    <div class="course-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard/courses' }">我的课程</el-breadcrumb-item>
        <el-breadcrumb-item>{{ course.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="course-content" v-if="course.id">
      <div class="course-info">
        <div class="course-title">
          <h1>{{ course.title }}</h1>
          <el-tag :type="course.isOnline === true ? 'success' : 'info'">
            {{ course.isOnline === true ? '已上线' : '未上线' }}
          </el-tag>
        </div>

        <div class="course-meta">
          <p class="course-description">{{ course.description }}</p>
          <div class="course-stats">
            <span><i class="el-icon-time"></i> 时长: {{ course.duration || '未知' }}</span>
            <span><i class="el-icon-view"></i> 观看次数: {{ course.viewCount || 0 }}</span>
            <span><i class="el-icon-user"></i> 学习人数: {{ course.studentCount || 0 }}</span>
          </div>
        </div>

        <!--        <div class="course-progress" v-if="userProgress">-->
        <!--          <el-progress-->
        <!--              :percentage="userProgress.watchProgress || 0"-->
        <!--              :format="format"-->
        <!--              :stroke-width="8"-->
        <!--              color="#409EFF"-->
        <!--          />-->
        <!--          <p>视频观看进度: {{ userProgress.watchProgress || 0 }}%</p>-->
        <!--          <p>章节完成: {{ userProgress.completedChapters || 0 }}/{{ course.totalChapters || 0 }}</p>-->
        <!--        </div>-->
      </div>

      <div class="video-section">
        <div class="video-player">
          <video
              ref="videoPlayer"
              class="video-js"
              data-setup='{"autoplay": false, "controls": true}'
              preload="auto"
              @timeupdate="onTimeUpdate"
              @ended="onVideoEnded"
          >
            <source :src="course.videoUrl" type="video/mp4">
            您的浏览器不支持视频播放。
          </video>

          <!-- 播放按钮覆盖层 - 覆盖整个视频区域 -->
          <div
              class="play-overlay"
              @click="togglePlayPause"
              v-if="!isPlaying"
          >
            <div class="play-button">
              <i class="el-icon-caret-right"></i>
            </div>
          </div>

          <!-- 视频点击层 - 整个视频区域可点击 -->
          <div
              class="video-click-layer"
              @click="togglePlayPause"
              v-show="isPlaying"
          ></div>
        </div>

        <div class="video-controls">
        <el-button @click="resetProgress">重置进度</el-button>
      </div>
      </div>

      <div class="course-chapters" v-if="course.chapters && course.chapters.length">
        <h3>课程章节</h3>
        <el-timeline>
          <el-timeline-item
              v-for="(chapter, index) in course.chapters"
              :key="index"
              :timestamp="chapter.duration"
              :type="getChapterType(chapter)"
          >
            <h4>{{ chapter.title }}</h4>
            <p>{{ chapter.description }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div class="course-actions">
        <el-button type="primary" @click="continueLearning" v-if="!isCompleted">
          继续学习
        </el-button>
        <el-button @click="goBack">返回课程列表</el-button>
      </div>
    </div>

    <div class="loading" v-else>
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 弹题对话框 -->
    <el-dialog
        v-model="showQuestionDialog"
        :title="`第 ${currentQuestionIndex + 1} 题`"
        width="600px"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
    >
      <div v-if="currentQuestion" class="question-dialog-content">
        <div class="question-header">
          <div class="question-type">
            <el-tag :type="getQuestionTypeTag(currentQuestion.type)" size="small">
              {{ getQuestionTypeText(currentQuestion.type) }}
            </el-tag>
          </div>
        </div>

        <div class="question-content">
          <h4>{{ currentQuestion.content }}</h4>
        </div>

        <!-- 单选题 -->
        <div v-if="currentQuestion.type === 'SINGLE_CHOICE'" class="question-options">
          <el-radio-group v-model="userAnswer">
            <el-radio
                v-for="(option, index) in currentQuestion.options"
                :key="option"
                :label="String.fromCharCode(65 + index)"
                class="option-item"
            >
              <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
              <span class="option-content">{{ option }}</span>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 多选题 -->
        <div v-else-if="currentQuestion.type === 'MULTIPLE_CHOICE'" class="question-options">
          <el-checkbox-group v-model="userAnswers">
            <el-checkbox
                v-for="(option, index) in currentQuestion.options"
                :key="option"
                :label="String.fromCharCode(65 + index)"
                class="option-item"
            >
              <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
              <span class="option-content">{{ option }}</span>
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <!-- 判断题 -->
        <div v-else-if="currentQuestion.type === 'TRUE_FALSE'" class="question-options">
          <el-radio-group v-model="userAnswer">
            <el-radio label="true" class="option-item">
              <span class="option-label">A.</span>
              <span class="option-content">正确</span>
            </el-radio>
            <el-radio label="false" class="option-item">
              <span class="option-label">B.</span>
              <span class="option-content">错误</span>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 填空题 -->
        <div v-else-if="currentQuestion.type === 'FILL_BLANK'" class="question-answer">
          <el-input
              v-model="userAnswer"
              type="text"
              placeholder="请输入答案..."
          />
        </div>

        <!-- 简答题 -->
        <div v-else-if="currentQuestion.type === 'SHORT_ANSWER'" class="question-answer">
          <el-input
              v-model="userAnswer"
              type="textarea"
              :rows="4"
              placeholder="请输入您的答案..."
          />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button
              type="primary"
              @click="submitAnswer"
              :disabled="!canSubmitAnswer"
          >
            提交答案
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 题目结果对话框 -->
    <el-dialog
        v-model="showQuestionDialog"
        :title="`弹题练习 (${currentQuestionIndex + 1}/${currentQuestions.length})`"
        width="600px"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        :show-close="false"
    >
      <div v-if="currentQuestion" class="question-dialog-content">
        <div class="question-header">
          <div class="question-type">
            <el-tag :type="getQuestionTypeTag(currentQuestion.type)" size="small">
              {{ getQuestionTypeText(currentQuestion.type) }}
            </el-tag>
          </div>
        </div>

        <div class="question-content">
          <h4>{{ currentQuestion.content }}</h4>
        </div>

        <!-- 单选题 -->
        <div v-if="currentQuestion.type === 'SINGLE_CHOICE'" class="question-options">
          <el-radio-group v-model="userAnswer">
            <el-radio
                v-for="(option, index) in currentQuestion.options"
                :key="option"
                :label="String.fromCharCode(65 + index)"
                class="option-item"
            >
              <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
              <span class="option-content">{{ option }}</span>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 多选题 -->
        <div v-else-if="currentQuestion.type === 'MULTIPLE_CHOICE'" class="question-options">
          <el-checkbox-group v-model="userAnswers">
            <el-checkbox
                v-for="(option, index) in currentQuestion.options"
                :key="option"
                :label="String.fromCharCode(65 + index)"
                class="option-item"
            >
              <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
              <span class="option-content">{{ option }}</span>
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <!-- 判断题 -->
        <div v-else-if="currentQuestion.type === 'TRUE_FALSE'" class="question-options">
          <el-radio-group v-model="userAnswer">
            <el-radio label="true" class="option-item">
              <span class="option-label">A.</span>
              <span class="option-content">正确</span>
            </el-radio>
            <el-radio label="false" class="option-item">
              <span class="option-label">B.</span>
              <span class="option-content">错误</span>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 填空题 -->
        <div v-else-if="currentQuestion.type === 'FILL_BLANK'" class="question-answer">
          <el-input
              v-model="userAnswer"
              type="text"
              placeholder="请输入答案..."
          />
        </div>

        <!-- 简答题 -->
        <div v-else-if="currentQuestion.type === 'SHORT_ANSWER'" class="question-answer">
          <el-input
              v-model="userAnswer"
              type="textarea"
              :rows="4"
              placeholder="请输入您的答案..."
          />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
     <el-button
         @click="moveToPreviousQuestion"
         :disabled="!canGoToPrevious"
     >
        上一题
      </el-button>
          <el-button
              type="primary"
              @click="submitAnswer"
              :disabled="!canSubmitAnswer"
          >
            {{ currentQuestionIndex < currentQuestions.length - 1 ? '下一题' : '提交答案' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 所有题目完成后的结果对话框 -->
    <el-dialog
        v-model="showAllResultsDialog"
        title="弹题练习结果"
        width="600px"
    >
      <div class="all-results">
        <div class="results-summary">
          <el-statistic title="题目总数" :value="allQuestions.length" />
          <el-statistic title="正确题数" :value="correctCount" />
          <el-statistic title="正确率" :value="accuracyRate" suffix="%" />
        </div>

        <div class="results-details">
          <h4>答题详情</h4>
          <div
              v-for="(result, index) in questionResults"
              :key="index"
              class="question-result-item"
          >
            <div class="result-header">
              <span class="question-number">第 {{ index + 1 }} 题</span>
              <el-tag :type="result.isCorrect ? 'success' : 'danger'" size="small">
                {{ result.isCorrect ? '正确' : '错误' }}
              </el-tag>
            </div>
            <div class="question-content">{{ result.question.content }}</div>
            <div class="answer-info">
              <div class="user-answer">
                <span class="label">您的答案：</span>
                <span class="value">{{ displayUserAnswer(result) }}</span>
              </div>
              <div class="correct-answer" v-if="!result.isCorrect">
                <span class="label">正确答案：</span>
                <span class="value">{{ displayCorrectAnswer(result) }}</span>
              </div>
              <div v-if="result.question.explanation" class="explanation">
                <span class="label">解析：</span>
                <span class="value">{{ result.question.explanation }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="closeAllResultsDialog">继续学习</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 人脸识别弹窗 -->
    <FaceRecognitionDialog
        v-model="showFaceRecognitionDialog"
        :course-id="courseId"
        @complete="onFaceRecognitionComplete"
    />
  </div>
</template>
<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import api from '@/api'
import FaceRecognitionDialog from '@/components/FaceRecognitionDialog.vue'

export default {
  name: 'CourseDetail',
  components: {
    FaceRecognitionDialog
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    const course = ref({})
    const userProgress = ref(null)
    const videoPlayer = ref(null)
    const player = ref(null)
    const currentTime = ref(0)
    const isCompleted = ref(false)
    const initialProgress = ref(0) // 从路由参数获取的初始进度
    const isPlaying = ref(false) // 播放状态

    // 弹题相关变量 - 支持多题
    const showQuestionDialog = ref(false)
    const showAllResultsDialog = ref(false) // 所有题目完成后的结果对话框
    const questionSettings = ref([]) // 存储从后端获取的弹题设置
    const triggeredTimePoints = ref(new Set()) // 已触发的时间点，避免重复触发
    const currentQuestions = ref([]) // 当前时间点的所有题目
    const currentQuestionIndex = ref(0) // 当前题目索引
    const currentQuestion = computed(() => {
      return currentQuestions.value[currentQuestionIndex.value] || null
    })
    const userAnswer = ref('') // 当前题目用户答案（单选、判断、填空、简答）
    const userAnswers = ref([]) // 当前题目用户答案（多选）
    const questionResults = ref([]) // 所有题目的答题结果
    const allQuestions = ref([]) // 所有题目（用于统计）
    const tempAnswers = ref({}) // 临时存储各题答案

    // 人脸识别相关变量
    const showFaceRecognitionDialog = ref(false)
    const faceRecognitionConfig = ref({
      faceRecognitionEnabled: false,
      faceRecognitionFrequency: 1
    })
    const lastFaceRecognitionTime = ref(0)
    const isFaceRecognitionActive = ref(false)

    // 计算属性
    const courseId = computed(() => route.params.id)
    const progressPercentage = computed(() => {
      if (!userProgress.value || !course.value.totalChapters) return 0
      return Math.round((userProgress.value.completedChapters / course.value.totalChapters) * 100)
    })

    const canMarkCompleted = computed(() => {
      return userProgress.value && userProgress.value.completedChapters > 0
    })

    const canSubmitAnswer = computed(() => {
      if (!currentQuestion.value) return false

      if (currentQuestion.value.type === 'MULTIPLE_CHOICE') {
        return userAnswers.value && userAnswers.value.length > 0
      } else {
        return userAnswer.value && userAnswer.value.toString().trim() !== ''
      }
    })

    const canGoToPrevious = computed(() => {
      return currentQuestionIndex.value > 0
    })

    const correctCount = computed(() => {
      return questionResults.value.filter(r => r.isCorrect).length
    })

    const accuracyRate = computed(() => {
      if (allQuestions.value.length === 0) return 0
      return Math.round((correctCount.value / allQuestions.value.length) * 100)
    })

    const format = (percentage) => {
      return percentage === 100 ? '完成' : `${percentage}%`
    }

    const getChapterType = (chapter) => {
      if (userProgress.value && userProgress.value.completedChapters >= chapter.order) {
        return 'success'
      }
      return 'primary'
    }

    const formatDuration = (seconds) => {
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      const secs = seconds % 60;

      if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
      } else {
        return `${minutes}:${secs.toString().padStart(2, '0')}`;
      }
    };

    const loadCourse = async () => {
      try {
        // 获取路由参数中的进度
        const progressParam = route.query.progress
        console.log('Progress Param:', progressParam)
        if (progressParam) {
          initialProgress.value = parseFloat(progressParam)
          console.log('Initial Progress:', initialProgress.value)
        }

        const response = await store.dispatch('fetchCourseById', courseId.value)
        course.value = response.data
        console.log('Course:', response.data)
        await loadUserProgress()
        await loadQuestionSettings()
        await loadFaceRecognitionConfig()

        // 延迟初始化视频，确保DOM已渲染
        setTimeout(() => {
          initializeVideoPlayer()
        }, 100)
      } catch (error) {
        ElMessage.error('加载课程信息失败')
        console.error('Error loading course:', error)
      }
    }
    const loadUserProgress = async () => {
      try {
        const userId = store.getters.userId;
        console.log('Loading user progress:', { userId, courseId: courseId.value });

        if (!userId) {
          console.error('User ID is not available');
          // 创建默认进度对象
          userProgress.value = {
            watchProgress: 0,
            completedChapters: 0,
            isCompleted: false
          };
          return;
        }

        const response = await store.dispatch('fetchUserCourseProgress', {
          userId: userId,
          courseId: courseId.value
        });
        console.log('User progress response:', response);
        userProgress.value = response.data || {
          watchProgress: 0,
          completedChapters: 0,
          isCompleted: false
        };
        isCompleted.value = userProgress.value && userProgress.value.isCompleted;
        console.log('User progress loaded:', userProgress.value);
      } catch (error) {
        console.error('Error loading user progress:', error);
        if (error.response) {
          console.error('Response error:', error.response.status, error.response.data);

          // 如果用户未选择该课程，创建默认进度
          if (error.response.status === 400 && error.response.data.message === "用户未选择该课程") {
            console.log('User has not enrolled in this course, creating default progress');
            userProgress.value = {
              watchProgress: 0,
              completedChapters: 0,
              isCompleted: false
            };
          }
        } else {
          // 其他错误，也创建默认进度
          userProgress.value = {
            watchProgress: 0,
            completedChapters: 0,
            isCompleted: false
          };
        }
      }
    };

    // 虚拟弹题数据
    const virtualQuestionSettings = [
      {
        timePointId: 1,
        time: 3, // 30秒
        questions: [
          {
            id: 1001,
            content: "以下哪个是Java的基本数据类型？",
            type: "SINGLE_CHOICE",
            options: ["String", "Integer", "int", "Double"],
            answers: ["C"], // 正确答案是选项C (int)
            explanation: "Java的基本数据类型包括byte、short、int、long、float、double、char、boolean"
          },
          {
            id: 1002,
            content: "Spring框架的核心特性包括哪些？（多选）",
            type: "MULTIPLE_CHOICE",
            options: ["依赖注入", "面向切面编程", "事务管理", "MVC框架"],
            answers: ["A", "B", "C", "D"], // 全部正确
            explanation: "Spring框架的核心特性包括依赖注入(DI)、面向切面编程(AOP)、事务管理等"
          }
        ]
      },
      {
        timePointId: 2,
        time: 120, // 2分钟
        questions: [
          {
            id: 1003,
            content: "Java中int类型的取值范围是多少？",
            type: "FILL_BLANK",
            answers: ["-2147483648到2147483647"],
            explanation: "int类型是32位有符号整数，取值范围是-2^31到2^31-1"
          }
        ]
      }
    ]

    // 加载人脸识别配置
    const loadFaceRecognitionConfig = async () => {
      try {
        const response = await api.get(`/course-face-config/${courseId.value}`)
        if (response.data.success) {
          faceRecognitionConfig.value = response.data.data
          console.log('人脸识别配置加载成功:', faceRecognitionConfig.value)
        }
      } catch (error) {
        console.error('加载人脸识别配置失败:', error)
        // 使用默认配置
        faceRecognitionConfig.value = {
          faceRecognitionEnabled: false,
          faceRecognitionFrequency: 30
        }
      }
    }

    // 加载弹题设置
    const loadQuestionSettings = async () => {
      try {
        const response = await api.get(`/courses/${courseId.value}/question-settings/user/${store.getters.userId}`)
        if (response.data.success) {
          // 处理后端返回的格式
          const settingsData = response.data.data

          if (settingsData.userSettings && settingsData.userSettings.length > 0) {
            // 使用用户特定设置
            const userSetting = settingsData.userSettings[0]
            questionSettings.value = userSetting.timePoints.map(timePoint => ({
              timePointId: timePoint.time,
              time: timePoint.time,
              questions: timePoint.questions
            }))
          } else if (settingsData.roleSettings && settingsData.roleSettings.length > 0) {
            // 使用角色设置
            const roleSetting = settingsData.roleSettings[0]
            questionSettings.value = roleSetting.settings.timePoints.map(timePoint => ({
              timePointId: timePoint.time,
              time: timePoint.time,
              questions: timePoint.questions
            }))
          } else {
            // 如果没有设置，使用虚拟数据
            console.warn('没有找到弹题设置，使用虚拟数据')
            questionSettings.value = virtualQuestionSettings
          }

          console.log('弹题设置:', questionSettings.value)
        } else {
          console.warn('后端弹题设置获取失败，使用虚拟数据')
          questionSettings.value = virtualQuestionSettings
        }
      } catch (error) {
        console.warn('后端弹题设置获取失败，使用虚拟数据:', error)
        questionSettings.value = virtualQuestionSettings
      }
    }
    // 修复initializeVideoPlayer方法，添加loadedmetadata事件监听
    const initializeVideoPlayer = () => {
      if (!videoPlayer.value || !course.value?.videoUrl) return;

      // 清理现有的播放器实例
      if (player.value) {
        player.value.dispose();
        player.value = null;
      }

      // 配置video.js选项
      const options = {
        controls: true,
        autoplay: false,
        preload: 'auto',
        fluid: true,
        responsive: true,
        bigPlayButton: false, // 隐藏大播放按钮
        controlBar: {
          children: [
            'playToggle',
            'progressControl',
            'volumePanel',
            'currentTimeDisplay',
            'timeDivider',
            'durationDisplay',
            'fullscreenToggle'
          ]
        }
      };

      // 创建播放器实例
      player.value = videojs(videoPlayer.value, options, function onPlayerReady() {
        console.log('Video player is ready');

        // 设置视频源
        this.src({
          src: course.value.videoUrl,
          type: 'video/mp4'
        });

        // 关键修复：监听loadedmetadata事件
        this.on('loadedmetadata', function() {
          const duration = this.duration();
          if (duration > 0) {
            console.log('视频元数据加载完成，时长:', duration);
            course.value.duration = formatDuration(Math.floor(duration));
            console.log('Duration:', course.value.duration);
            handleInitialSeek(duration);
          }
        });

        // 监听播放进度更新
        this.on('timeupdate', () => {
          onTimeUpdate();
        });

        // 监听播放状态变化
        this.on('play', () => {
          isPlaying.value = true;
          console.log('视频正在播放');
        });

        this.on('pause', () => {
          isPlaying.value = false;
          console.log('视频已暂停');
        });

        // 监听视频结束
        this.on('ended', () => {
          isPlaying.value = false;
          onVideoEnded();
          ElMessage.success('课程已完成！')
        });

        // 监听错误
        this.on('error', (error) => {
          console.error('Video player error:', error);
        });
      });
    };

    // 优化handleInitialSeek，让视频在指定位置暂停
    const handleInitialSeek = (duration) => {
      let seekTime = 0
      let targetProgress = 0

      console.log('处理初始进度跳转 - 参数:', {
        initialProgress: initialProgress.value,
        userProgress: userProgress.value?.watchProgress,
        duration: duration
      })

      // 优先级1：从路由参数获取的初始进度
      if (initialProgress.value > 0) {
        targetProgress = initialProgress.value
        seekTime = (targetProgress / 100) * duration
        console.log(`从路由参数跳转到进度 ${targetProgress}% 的位置: ${seekTime}秒`)
      }
      // 优先级2：恢复用户之前的观看进度
      else if (userProgress.value && userProgress.value.watchProgress > 0) {
        targetProgress = userProgress.value.watchProgress
        seekTime = (targetProgress / 100) * duration
        console.log(`恢复观看进度到: ${seekTime}秒`)
      }

      if (seekTime > 0 && seekTime < duration) {
        // 确保seekTime有效
        seekTime = Math.max(0, Math.min(seekTime, duration - 1))

        player.value.currentTime(seekTime)
        console.log('已设置视频播放位置到:', seekTime)

        // 确保视频在指定位置暂停
        player.value.pause()
        console.log('视频已在指定位置暂停')

        // 更新进度条显示
        if (userProgress.value) {
          userProgress.value.watchProgress = Math.round((seekTime / duration) * 100)
        }
      } else {
        console.log('没有需要跳转的进度，从头开始，保持暂停状态')
        player.value.currentTime(0)
        player.value.pause()
      }
    }

    // 优化后的onTimeUpdate方法
    const onTimeUpdate = () => {
      if (!player.value) return

      const currentTimeVal = player.value.currentTime()
      const duration = player.value.duration()

      if (duration && duration > 0) {
        const progress = Math.round((currentTimeVal / duration) * 100)

        // 更新学习进度
        if (userProgress.value) {
          userProgress.value.watchProgress = Math.min(progress, 100)
        }

        currentTime.value = currentTimeVal

        // 检查是否需要人脸识别
        checkForFaceRecognition() // 添加这行


        // 检查是否需要弹题
        checkQuestionTrigger(currentTimeVal)

        // 每5秒保存一次进度（防抖）
        if (Math.floor(currentTimeVal) % 5 === 0) {
          if (!lastSaveTime || Date.now() - lastSaveTime > 4000) {
            lastSaveTime = Date.now()
            saveProgress(currentTimeVal)
          }
        }
      }
    }

// 添加防抖变量，避免频繁保存进度
    let lastSaveTime = 0;

    // 检查是否需要人脸识别
    const checkForFaceRecognition = () => {
      console.log('检查人脸识别',faceRecognitionConfig.value.faceRecognitionEnabled,isFaceRecognitionActive.value)
      if (!faceRecognitionConfig.value.faceRecognitionEnabled || isFaceRecognitionActive.value) {
        return
      }

      const now = Date.now()
      const lastTime = lastFaceRecognitionTime.value
      const frequency = faceRecognitionConfig.value.faceRecognitionFrequency  *  1000 // 转换为毫秒
      console.log('now:', now , '毫秒')
      console.log('上次人脸识别时间:', lastTime , '毫秒')
      console.log('时间差:', now-lastTime, '毫秒')
      console.log('人脸识别间隔:', frequency, '毫秒')
      // 检查是否到达触发时间（允许±30秒的误差）
      if (now - lastTime >= frequency) {
        console.log('触发人脸识别检查')
        lastFaceRecognitionTime.value = now
        isFaceRecognitionActive.value = true

        // 暂停视频
        if (player.value) {
          player.value.pause()
        }
        console.log('弹出人脸')
        // 显示人脸识别弹窗
        showFaceRecognitionDialog.value = true
      }
    }

    // 人脸识别完成后的回调
    const onFaceRecognitionComplete = (result) => {
      console.log('人脸识别完成:', result)
      isFaceRecognitionActive.value = false
      showFaceRecognitionDialog.value = false

      // 继续播放视频
      if (player.value && result.success) {
        player.value.play()
      }

      // 如果识别失败，可以显示提示
      if (!result.success) {
        ElMessage.warning('人脸识别失败，请重试')
      }
    }

    // 检查是否需要触发弹题
    const checkQuestionTrigger = (currentTime) => {
      if (!questionSettings.value || questionSettings.value.length === 0) {
        return
      }

      // 检查每个时间点
      for (const setting of questionSettings.value) {
        const timePoint = setting.time

        // 检查是否已触发过这个时间点
        if (triggeredTimePoints.value.has(timePoint)) {
          continue
        }

        // 检查是否到达触发时间（允许±1秒的误差）
        if (Math.abs(currentTime - timePoint) <= 1) {
          console.log(`触发弹题，时间点: ${timePoint}秒，题目数量: ${setting.questions?.length || 0}`)

          // 标记这个时间点已触发
          triggeredTimePoints.value.add(timePoint)

          // 设置当前时间点的所有题目
          if (setting.questions && setting.questions.length > 0) {
            currentQuestions.value = [...setting.questions]
            currentQuestionIndex.value = 0
            allQuestions.value = [...setting.questions] // 保存所有题目用于统计

            // 重置答案
            userAnswer.value = ''
            userAnswers.value = []
            tempAnswers.value = {}
            questionResults.value = []

            // 暂停视频
            if (player.value) {
              player.value.pause()
            }

            // 显示弹题对话框
            showQuestionDialog.value = true
            break // 只触发第一个匹配的时间点
          }
        }
      }
    }



    // 保存当前题目的答案到临时存储
    const saveCurrentAnswer = () => {
      if (currentQuestion.value) {
        const questionId = currentQuestion.value.id
        tempAnswers.value[questionId] = {
          userAnswer: userAnswer.value,
          userAnswers: [...userAnswers.value]
        }
      }
    }

    // 从临时存储恢复当前题目的答案
    const restoreCurrentAnswer = () => {
      if (currentQuestion.value) {
        const questionId = currentQuestion.value.id
        const saved = tempAnswers.value[questionId]
        if (saved) {
          userAnswer.value = saved.userAnswer || ''
          userAnswers.value = saved.userAnswers || []
        } else {
          resetCurrentAnswer()
        }
      }
    }

    // 播放/暂停切换
    const togglePlayPause = () => {
      if (!player.value) return

      if (isPlaying.value) {
        player.value.pause()
        isPlaying.value = false
        console.log('视频已暂停')
      } else {
        player.value.play()
        isPlaying.value = true
        console.log('视频开始播放')
      }
    }

    // 重置当前题目的答案
    const resetCurrentAnswer = () => {
      userAnswer.value = ''
      userAnswers.value = []
    }

    // 移动到上一题
    const moveToPreviousQuestion = () => {
      if (currentQuestionIndex.value > 0) {
        // 保存当前题目的答案
        saveCurrentAnswer()
        // 移动到上一题
        currentQuestionIndex.value--
        // 恢复上一题的答案
        restoreCurrentAnswer()
      }
    }

    // 移动到下一题
    const moveToNextQuestion = () => {
      // 保存当前题目的答案
      saveCurrentAnswer()

      if (currentQuestionIndex.value < currentQuestions.value.length - 1) {
        currentQuestionIndex.value++
        // 恢复下一题的答案
        restoreCurrentAnswer()
      } else {
        // 所有题目完成，提交所有答案
        submitAllAnswers()
      }
    }

    // 提交所有答案
    const submitAllAnswers = async () => {
      try {
        // 处理所有题目的答案
        for (let i = 0; i < currentQuestions.value.length; i++) {
          const question = currentQuestions.value[i]
          const tempAnswer = tempAnswers.value[question.id]

          if (!tempAnswer) continue // 跳过没有答案的题目

          let isVirtualQuestion = false

          // 检查是否是虚拟题目
          if (question.answers) {
            isVirtualQuestion = true
          }

          if (isVirtualQuestion) {
            // 处理虚拟题目
            let isCorrect = false
            let correctAnswer = ''

            if (question.type === 'MULTIPLE_CHOICE') {
              // 多选题处理
              const userAnswerArray = tempAnswer.userAnswers.map(label => {
                const index = label.charCodeAt(0) - 65
                return question.options[index]
              })
              correctAnswer = question.answers.map(idx =>
                  question.options[idx.charCodeAt(0) - 65]
              )
              isCorrect = JSON.stringify(userAnswerArray.sort()) === JSON.stringify(correctAnswer.sort())

              // 保存结果
              questionResults.value.push({
                question: question,
                userAnswer: userAnswerArray,
                isCorrect: isCorrect,
                correctAnswer: correctAnswer,
                explanation: question.explanation || ''
              })
            } else if (question.type === 'SINGLE_CHOICE' || question.type === 'TRUE_FALSE') {
              // 单选题和判断题处理
              const index = tempAnswer.userAnswer.charCodeAt(0) - 65
              const userAnswerContent = question.options[index] || tempAnswer.userAnswer
              const correctIndex = question.answers[0].charCodeAt(0) - 65
              correctAnswer = question.options[correctIndex]
              isCorrect = userAnswerContent === correctAnswer

              // 保存结果
              questionResults.value.push({
                question: question,
                userAnswer: userAnswerContent,
                isCorrect: isCorrect,
                correctAnswer: correctAnswer,
                explanation: question.explanation || ''
              })
            } else {
              // 填空题和简答题处理
              const userAnswerContent = tempAnswer.userAnswer
              correctAnswer = question.answers[0]
              isCorrect = userAnswerContent.trim() === correctAnswer.trim()

              // 保存结果
              questionResults.value.push({
                question: question,
                userAnswer: userAnswerContent,
                isCorrect: isCorrect,
                correctAnswer: correctAnswer,
                explanation: question.explanation || ''
              })
            }
          } else {
            // 处理真实题目（调用后端API）
            let answerToSubmit
            if (question.type === 'MULTIPLE_CHOICE') {
              // 多选题：将选项标签数组转换为实际选项内容
              answerToSubmit = tempAnswer.userAnswers.map(label => {
                const index = label.charCodeAt(0) - 65 // A=0, B=1, C=2...
                return question.options[index]
              }).join(',')
            } else if (question.type === 'SINGLE_CHOICE' || question.type === 'TRUE_FALSE') {
              // 单选题和判断题：将选项标签转换为实际选项内容
              const index = tempAnswer.userAnswer.charCodeAt(0) - 65 // A=0, B=1, C=2...
              answerToSubmit = question.options[index] || tempAnswer.userAnswer
            } else {
              // 填空题和简答题：直接使用答案
              answerToSubmit = tempAnswer.userAnswer
            }

            const response = await api.post(`/questions/${question.id}/check`, {
              userAnswer: answerToSubmit
            })

            if (response.data.success) {
              // 保存结果
              questionResults.value.push({
                question: question,
                userAnswer: tempAnswer.userAnswer,
                ...response.data.data
              })
            }
          }
        }

        // 显示结果对话框
        showQuestionDialog.value = false
        showAllResultsDialog.value = true
      } catch (error) {
        ElMessage.error('提交答案失败')
        console.error('提交答案失败:', error)
      }
    }
// 新增专门用于保存指定百分比的函数
    const saveProgressWithPercentage = async (percentage) => {
      try {
        await store.dispatch('updateUserCourseProgress', {
          userId: store.getters.userId,
          courseId: courseId.value,
          completedChapters: percentage
        });
      } catch (error) {
        console.error('Error saving progress:', error);
      }
    };
    const onVideoEnded = () => {
      // saveProgressWithPercentage(100);
      markAsCompleted(true) // 传递参数表示是自动完成

    }
    const saveProgress = async (time) => {
      // If time is not provided, use the last known current time
      const timeToSave = time !== undefined ? time : currentTime.value;
      if (timeToSave <= 0) return; // Don't save if there's no progress

      try {
        // 获取视频总时长
        let videoDuration = 0;
        if (player.value) {
          videoDuration = player.value.duration();
        }

        // 计算观看进度百分比
        let watchProgress = 0;
        if (videoDuration && videoDuration > 0) {
          watchProgress = Math.round((timeToSave / videoDuration) * 100);
          // 确保百分比不超过100
          watchProgress = Math.min(watchProgress, 100);
        }

        console.log('Saving progress:', {
          currentTime: timeToSave,
          videoDuration: videoDuration,
          watchProgress: watchProgress
        });

        await store.dispatch('updateUserCourseProgress', {
          userId: store.getters.userId,
          courseId: courseId.value,
          completedChapters: watchProgress,
          lastStudyTime: new Date().toISOString() // 添加当前时间作为最后学习时间
        });
      } catch (error) {
        console.error('Error saving progress:', error);
      }
    };
    const markAsCompleted = async (autoComplete = false) => {
      try {
        await store.dispatch('updateUserCourseProgress', {
          userId: store.getters.userId,
          courseId: courseId.value,
          progress: 100,
          lastStudyTime: new Date().toISOString() // 添加当前时间作为最后学习时间
        })
        isCompleted.value = true
        userProgress.value = {
          ...userProgress.value,
          isCompleted: true,
          watchProgress: 100, // 设置为100%
          completedChapters: course.value.totalChapters || 1
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('操作失败')
          console.error('Error marking course as completed:', error)
        }
      }
    }


    const resetProgress = async () => {
      try {
        await ElMessageBox.confirm(
            '确定要重置学习进度吗？这将清除所有学习记录。',
            '确认操作',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        await store.dispatch('resetCourseProgress', {
          userId: store.getters.userId,
          courseId: courseId.value
        })

        userProgress.value = null
        isCompleted.value = false
        currentTime.value = 0
        triggeredTimePoints.value.clear() // 清除已触发的时间点

        if (player.value) {
          player.value.currentTime(0)
        }

        ElMessage.success('进度已重置')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('重置失败')
          console.error('Error resetting progress:', error)
        }
      }
    }

    const continueLearning = () => {
      if (player.value) {
        player.value.play()
      }
    }

    const goBack = () => {
      router.push('/dashboard/courses')
    }

    // 获取题目类型文本
    const getQuestionTypeText = (type) => {
      const typeMap = {
        'SINGLE_CHOICE': '单选题',
        'MULTIPLE_CHOICE': '多选题',
        'TRUE_FALSE': '判断题',
        'FILL_BLANK': '填空题',
        'SHORT_ANSWER': '简答题'
      }
      return typeMap[type] || type
    }

    // 获取题目类型标签
    const getQuestionTypeTag = (type) => {
      const typeMap = {
        'SINGLE_CHOICE': 'success',
        'MULTIPLE_CHOICE': 'info',
        'TRUE_FALSE': 'warning',
        'FILL_BLANK': 'primary',
        'SHORT_ANSWER': 'danger'
      }
      return typeMap[type] || 'info'
    }

    // 提交答案（单题）
    const submitAnswer = () => {
      // 直接移动到下一题（会自动保存当前答案）
      moveToNextQuestion()
    }

    // 显示用户答案
    const displayUserAnswer = (result) => {
      if (!result || !result.question) return ''

      const question = result.question
      const userAnswer = result.userAnswer

      if (question.type === 'MULTIPLE_CHOICE') {
        return (userAnswer || []).map(a => {
          const optionIndex = question.options.indexOf(a)
          if (optionIndex >= 0) {
            return `${String.fromCharCode(65 + optionIndex)}.${a}`
          }
          return a
        }).join(', ')
      } else if (question.type === 'SINGLE_CHOICE' || question.type === 'TRUE_FALSE') {
        const optionIndex = question.options.indexOf(userAnswer)
        if (optionIndex >= 0) {
          return `${String.fromCharCode(65 + optionIndex)}.${userAnswer}`
        }
        return userAnswer
      } else {
        return userAnswer || ''
      }
    }

    // 显示正确答案
    const displayCorrectAnswer = (result) => {
      if (!result || !result.question) return ''

      const question = result.question
      const correctAnswer = result.correctAnswer

      if (question.type === 'MULTIPLE_CHOICE') {
        return (correctAnswer || []).map(a => {
          const optionIndex = question.options.indexOf(a)
          if (optionIndex >= 0) {
            return `${String.fromCharCode(65 + optionIndex)}.${a}`
          }
          return a
        }).join(', ')
      } else if (question.type === 'SINGLE_CHOICE' || question.type === 'TRUE_FALSE') {
        const optionIndex = question.options.indexOf(correctAnswer)
        if (optionIndex >= 0) {
          return `${String.fromCharCode(65 + optionIndex)}.${correctAnswer}`
        }
        return correctAnswer
      } else {
        return correctAnswer || ''
      }
    }

    // 关闭所有结果对话框并继续播放
    const closeAllResultsDialog = () => {
      showAllResultsDialog.value = false
      // 继续播放视频
      if (player.value) {
        player.value.play()
      }
    }

    onMounted(() => {
      loadCourse()
      lastFaceRecognitionTime.value = Date.now()
    })

    onUnmounted(() => {
      if (player.value && !player.value.isDisposed()) {
        saveProgress(player.value.currentTime());
        player.value.dispose()
        player.value = null
      }
    })

    return {
      course,
      userProgress,
      videoPlayer,
      currentTime,
      isCompleted,
      isPlaying,
      progressPercentage,
      canMarkCompleted,
      format,
      getChapterType,
      onTimeUpdate,
      onVideoEnded,
      markAsCompleted,
      resetProgress,
      continueLearning,
      goBack,
      togglePlayPause,
      // 弹题相关
      showQuestionDialog,
      showAllResultsDialog,
      currentQuestions,
      currentQuestionIndex,
      currentQuestion,
      userAnswer,
      userAnswers,
      questionResults,
      allQuestions,
      correctCount,
      accuracyRate,
      canSubmitAnswer,
      canGoToPrevious,
      getQuestionTypeText,
      getQuestionTypeTag,
      moveToPreviousQuestion,
      submitAnswer,
      displayUserAnswer,
      displayCorrectAnswer,
      closeAllResultsDialog,
      // 人脸识别相关
      showFaceRecognitionDialog,
      faceRecognitionConfig,
      lastFaceRecognitionTime,
      isFaceRecognitionActive,
      onFaceRecognitionComplete,
    }
  }
}
</script>

<style scoped>
.course-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.course-header {
  margin-bottom: 20px;
}

.course-content {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.course-info {
  margin-bottom: 30px;
}

.course-title {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.course-title h1 {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.course-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
}

.course-stats {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 14px;
}

.course-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.course-progress {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
}

.course-progress p {
  margin: 8px 0 0 0;
  color: #606266;
  font-size: 14px;
}

.video-section {
  margin-bottom: 30px;
  width: 100%;
}

.video-player {
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
  max-width: 100%;
}

.video-controls {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.course-chapters {
  margin-bottom: 30px;
}

.course-chapters h3 {
  margin-bottom: 16px;
  color: #303133;
}

.course-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.loading {
  padding: 40px;
}

/* 弹题对话框样式 */
.question-dialog-content {
  padding: 20px 0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-content h4 {
  margin: 0 0 20px 0;
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
}

.question-options {
  margin-top: 20px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 12px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.option-label {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-right: 10px;
  min-width: 20px;
}

.option-content {
  font-size: 14px;
  color: #303133;
  flex: 1;
}

.question-answer {
  margin-top: 20px;
}

.answer-result {
  padding: 20px 0;
}

.result-header {
  text-align: center;
  margin-bottom: 20px;
}

.answer-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-top: 20px;
}

.answer-info > div {
  margin-bottom: 12px;
}

.answer-info .label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.answer-info .value {
  color: #303133;
}

.explanation {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #dcdfe6;
}

.explanation .value {
  color: #409eff;
  font-style: italic;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* Video.js 样式覆盖 */
:deep(.video-js) {
  border-radius: 8px;
  width: 100% !important;
  height: 450px !important;
  background-color: #000;
}

:deep(.vjs-tech) {
  width: 100% !important;
  height: 100% !important;
}

:deep(.vjs-big-play-button) {
  display: none !important; /* 完全隐藏大播放按钮 */
}

:deep(.vjs-poster) {
  display: none !important; /* 隐藏海报图片 */
}

:deep(.vjs-loading-spinner) {
  display: none !important; /* 隐藏加载动画 */
}

:deep(.vjs-control-bar) {
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
}

.all-results {
  max-height: 500px;
  overflow-y: auto;
}

.results-summary {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.results-summary .el-statistic {
  text-align: center;
}

.results-details h4 {
  margin: 0 0 15px 0;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.question-result-item {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.question-number {
  font-weight: bold;
  color: #303133;
}

.question-content {
  margin-bottom: 15px;
  font-weight: bold;
  color: #303133;
}

.answer-info {
  background: #ffffff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.answer-info > div {
  margin-bottom: 8px;
}

.answer-info .label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.answer-info .value {
  color: #303133;
}

.explanation {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #dcdfe6;
}

.explanation .value {
  color: #409eff;
  font-style: italic;
}

/* 视频播放器容器 */
.video-player {
  position: relative;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.video-js {
  width: 100%;
  height: 400px;
  display: block;
}

/* 播放按钮覆盖层 - 避开底部进度条区域 */
.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: calc(100% - 24px); /* 留出底部50px给进度条 */
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: opacity 0.3s ease;
  z-index: 10;
}

.play-overlay:hover {
  background: rgba(0, 0, 0, 0.5);
}

.play-button {
  width: 80px;
  height: 80px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease, background-color 0.2s ease;
  position: relative; /* 添加相对定位 */
}

.play-button:hover {
  transform: scale(1.1);
  background: rgba(0, 0, 0, 0.9);
}

.play-button i {
  display: none; /* 隐藏原来的图标，使用CSS三角形 */
}

/* 向右三角形 */
.play-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-35%, -50%);
  width: 0;
  height: 0;
  border-left: 16px solid white;
  border-top: 10px solid transparent;
  border-bottom: 10px solid transparent;
}

/* 视频点击层 - 避开底部进度条区域 */
.video-click-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: calc(100% - 24px); /* 留出底部50px给进度条 */
  cursor: pointer;
  z-index: 5;
}

@media (max-width: 768px) {
  .course-detail {
    padding: 10px;
  }

  .video-js {
    height: 250px;
  }

  .play-button {
    width: 60px;
    height: 60px;
  }

  .play-button i {
    font-size: 24px;
  }
}
</style>
