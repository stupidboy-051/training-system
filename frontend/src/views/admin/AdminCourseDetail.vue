<template>
  <div class="course-detail">
    <div class="course-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/admin/courses' }">课程管理</el-breadcrumb-item>
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
            <span><i class="el-icon-time"></i> 时长: {{ course.duration || '加载中...' }}</span>
            <span><i class="el-icon-view"></i> 观看次数: {{ course.viewCount || 0 }}</span>
            <span><i class="el-icon-user"></i> 学习人数: {{ course.studentCount || 0 }}</span>
          </div>
        </div>

        <!-- 移除用户进度显示 -->
      </div>

      <div class="video-section">
        <div class="video-player">
          <video
              ref="videoPlayer"
              class="video-js"
              controls
              preload="auto"
              width="100%"
              height="400"
              @timeupdate="onTimeUpdate"
              @ended="onVideoEnded"
          >
            <source :src="course.videoUrl" type="video/mp4">
            您的浏览器不支持视频播放。
          </video>
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
        <el-button type="primary" @click="continueLearning">
          播放视频
        </el-button>
        <el-button @click="goBack">返回课程列表</el-button>
      </div>
    </div>

    <div class="loading" v-else>
      <el-skeleton :rows="10" animated />
    </div>
  </div>
</template>
<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'

export default {
  name: 'CourseDetail',
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

    const courseId = computed(() => route.params.coursesId)

    const progressPercentage = computed(() => {
      if (!userProgress.value || !course.value.totalChapters) return 0
      return Math.round((userProgress.value.completedChapters / course.value.totalChapters) * 100)
    })

    const getChapterType = (chapter) => {
      if (userProgress.value && userProgress.value.completedChapters >= chapter.order) {
        return 'success'
      }
      return 'primary'
    }

    const loadCourse = async () => {
      try {
        console.log('Course ID:', courseId.value)
        const response = await store.dispatch('fetchCourseById', courseId.value)
        console.log('Course:', response.data)
        course.value = response.data
        await loadUserProgress()
        initializeVideoPlayer()
      } catch (error) {
        ElMessage.error('加载课程信息失败')
        console.error('Error loading course:', error)
      }
    }

    const loadUserProgress = async () => {
      try {
        const response = await store.dispatch('fetchUserCourseProgress', {
          userId: store.getters.userId,
          courseId: courseId.value
        })
        userProgress.value = response.data
        isCompleted.value = userProgress.value && userProgress.value.isCompleted
      } catch (error) {
        console.error('Error loading user progress:', error)
      }
    }

    const getVideoDuration = () => {
      if (course.value.videoUrl) {
        // 使用原生video元素获取时长
        const video = document.createElement('video');
        video.src = course.value.videoUrl;

        video.addEventListener('loadedmetadata', () => {
          const duration = Math.floor(video.duration);
          if (duration && duration > 0) {
            course.value.duration = formatDuration(duration);
            console.log('Duration:', course.value.duration);
          } else if (!course.value.duration) {
            course.value.duration = '未知';
          }
          video.remove(); // 清理创建的元素
        });

        video.addEventListener('error', () => {
          if (!course.value.duration) {
            course.value.duration = '未知';
          }
          video.remove();
        });
      } else if (!course.value.duration) {
        course.value.duration = '未知';
      }
    };

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

    const initializeVideoPlayer = () => {
      if (videoPlayer.value && course.value.videoUrl) {
        player.value = videojs(videoPlayer.value, {
          controls: true,
          autoplay: false,
          preload: 'auto',
          fluid: true,
          responsive: true
        })

        // 获取视频时长
        getVideoDuration();

        // 恢复播放进度
        if (userProgress.value && userProgress.value.currentTime) {
          player.value.currentTime(userProgress.value.currentTime)
        }

        player.value.on('beforepluginsetup', () => {
          if (player.value) {
            saveProgress(player.value.currentTime());
          }
        });
      }
    }

    const onTimeUpdate = () => {
      if (player.value) {
        currentTime.value = player.value.currentTime()
        // 每30秒保存一次进度
        if (Math.floor(currentTime.value) % 30 === 0) {
          saveProgress(currentTime.value)
        }
      }
    }

    const onVideoEnded = () => {
      // 视频结束时的操作
    }

    const saveProgress = async (time) => {
      // If time is not provided, use the last known current time
      const timeToSave = time !== undefined ? time : currentTime.value;
      if (timeToSave <= 0) return; // Don't save if there's no progress

      try {
        await store.dispatch('updateUserCourseProgress', {
          userId: store.getters.userId,
          courseId: courseId.value,
          currentTime: timeToSave,
          completedChapters: userProgress.value ? userProgress.value.completedChapters : 0
        })
      } catch (error) {
        console.error('Error saving progress:', error)
      }
    }

    const continueLearning = () => {
      if (player.value) {
        player.value.play()
      }
    }

    const goBack = () => {
      router.push('/admin/courses')
    }

    onMounted(() => {
      loadCourse()
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
      progressPercentage,
      getChapterType,
      onTimeUpdate,
      onVideoEnded,
      continueLearning,
      goBack
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


.course-progress p {
  margin: 8px 0 0 0;
  color: #606266;
  font-size: 14px;
}

.video-section {
  margin-bottom: 30px;
}

.video-player {
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
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

/* Video.js 样式覆盖 */
:deep(.video-js) {
  border-radius: 8px;
}

:deep(.vjs-big-play-button) {
  background-color: rgba(64, 158, 255, 0.8);
  border-color: #409eff;
}

:deep(.vjs-big-play-button:hover) {
  background-color: #409eff;
}
</style>