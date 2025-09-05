<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    class="face-recognition-dialog"
  >
    <div class="face-recognition-content">
      <!-- 倒计时阶段 -->
      <div v-if="countdown > 0" class="countdown-section">
        <div class="countdown-circle">
          <span class="countdown-number">{{ countdown }}</span>
        </div>
        <p class="countdown-tip">准备开始人脸识别</p>
      </div>

      <!-- 摄像头预览阶段 -->
      <div v-else-if="!result" class="camera-section">
        <div class="camera-container">
          <video
            ref="videoRef"
            class="camera-preview"
            autoplay
            playsinline
            muted
          ></video>
          <div class="camera-overlay">
            <div class="face-frame"></div>
            <div class="camera-tip">{{ message }}</div>
          </div>
        </div>
        <div class="loading-spinner" v-if="loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在识别...</span>
        </div>
      </div>

      <!-- 识别结果阶段 -->
      <div v-else class="result-section">
        <div class="result-icon" :class="result.success ? 'success' : 'error'">
          <el-icon v-if="result.success" size="60" color="#67C23A">
            <CircleCheck />
          </el-icon>
          <el-icon v-else size="60" color="#F56C6C">
            <CircleClose />
          </el-icon>
        </div>
        <div class="result-info">
          <h3>{{ result.success ? '识别成功' : '识别失败' }}</h3>
          <p v-if="result.success" class="similarity">
            相似度: {{ (result.similarity * 100).toFixed(1) }}%
          </p>
          <p v-else class="error-message">
            {{ result.message || '请确保面部清晰可见，然后重试' }}
          </p>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button
          v-if="result && !result.success"
          type="primary"
          @click="retryRecognition"
        >
          重新识别
        </el-button>
        <el-button
          v-if="result"
          type="primary"
          @click="closeDialog"
        >
          {{ result.success ? '继续学习' : '稍后重试' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ElIcon } from 'element-plus'
import { CircleCheck, CircleClose, Loading } from '@element-plus/icons-vue'
import {
  startCamera,
  stopCamera,
  captureImage,
  performFaceRecognition
} from '@/utils/faceRecognition'

export default {
  name: 'FaceRecognitionDialog',
  components: {
    ElIcon,
    CircleCheck,
    CircleClose,
    Loading
  },
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    courseId: {
      type: [String, Number],
      required: true
    },
    title: {
      type: String,
      default: '人脸识别验证'
    }
  },
  emits: ['update:modelValue', 'success', 'fail', 'close'],
  setup(props, { emit }) {
    const videoRef = ref(null)
    const countdown = ref(5)
    const loading = ref(false)
    const message = ref('请保持面部正对摄像头')
    const result = ref(null)
    const stream = ref(null)

    const dialogVisible = computed({
      get: () => props.modelValue,
      set: (val) => emit('update:modelValue', val)
    })

    // 监听对话框显示状态
    watch(dialogVisible, async (newVal) => {
      if (newVal) {
        // 对话框打开时开始倒计时
        await nextTick()
        startCountdown()
      } else {
        // 对话框关闭时清理资源
        cleanup()
      }
    })

    // 开始倒计时
    const startCountdown = () => {
      countdown.value = 3
      result.value = null
      loading.value = false

      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          startRecognition()
        }
      }, 1000)
    }

    // 开始人脸识别
    const startRecognition = async () => {
      try {
        loading.value = true
        message.value = '正在启动摄像头...'

        // 启动摄像头
        stream.value = await startCamera()

        if (videoRef.value) {
          videoRef.value.srcObject = stream.value

          // 等待视频加载完成
          await new Promise((resolve) => {
            videoRef.value.onloadedmetadata = resolve
          })

          message.value = '正在识别...'

          // 延迟1秒确保图像清晰
          await new Promise(resolve => setTimeout(resolve, 1000))

          // 捕获图像并识别
          const canvas = document.createElement('canvas')
          const imageData = captureImage(videoRef.value, canvas)

          if (imageData) {
            const recognitionResult = await performFaceRecognition(imageData, props.courseId)
            result.value = recognitionResult

            if (recognitionResult.success) {
              emit('success', recognitionResult)
            } else {
              emit('fail', recognitionResult)
            }
          } else {
            throw new Error('图像捕获失败')
          }
        }
      } catch (error) {
        console.error('人脸识别失败:', error)
        result.value = {
          success: false,
          message: error.message || '识别失败，请重试'
        }
        emit('fail', result.value)
      } finally {
        loading.value = false
        stopCamera()
      }
    }

    // 重新识别
    const retryRecognition = () => {
      result.value = null
      countdown.value = 5
      startCountdown()
    }

    // 关闭对话框
    const closeDialog = () => {
      dialogVisible.value = false
      emit('close')
    }

    // 清理资源
    const cleanup = () => {
      stopCamera()
      if (stream.value) {
        stream.value.getTracks().forEach(track => track.stop())
        stream.value = null
      }
    }

    return {
      videoRef,
      countdown,
      loading,
      message,
      result,
      dialogVisible,
      retryRecognition,
      closeDialog
    }
  }
}
</script>

<style scoped>
.face-recognition-dialog {
  --el-dialog-border-radius: 12px;
}

.face-recognition-content {
  text-align: center;
  padding: 20px 0;
}

.countdown-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.countdown-circle {
  width: 120px;
  height: 120px;
  border: 4px solid #409EFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.countdown-number {
  font-size: 48px;
  font-weight: bold;
  color: #409EFF;
}

.countdown-tip {
  font-size: 16px;
  color: #666;
}

.camera-section {
  position: relative;
}

.camera-container {
  position: relative;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.camera-preview {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 8px;
}

.camera-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.face-frame {
  width: 200px;
  height: 250px;
  border: 2px solid #409EFF;
  border-radius: 50%;
  position: absolute;
}

.camera-tip {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  color: #409EFF;
}

.result-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.result-icon {
  margin-bottom: 10px;
}

.result-info h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
}

.similarity {
  color: #67C23A;
  font-size: 16px;
  font-weight: bold;
}

.error-message {
  color: #F56C6C;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>