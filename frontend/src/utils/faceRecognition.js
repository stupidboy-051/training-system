/**
 * 人脸识别模块
 * 提供人脸识别相关的所有功能封装
 */

import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

// 人脸识别状态管理
const faceRecognitionState = reactive({
  isEnabled: false,
  frequency: 30, // 默认30秒
  lastRecognitionTime: 0,
  isRecognizing: false,
  recognitionResult: null,
  stream: null,
  videoElement: null,
  canvasElement: null
})

// 人脸识别弹窗状态
const faceDialogState = reactive({
  visible: false,
  title: '人脸识别验证',
  loading: false,
  countdown: 5,
  message: '请保持面部正对摄像头',
  result: null
})

/**
 * 获取课程人脸识别配置
 * @param {number} courseId 课程ID
 * @returns {Promise} 人脸识别配置
 */
export const getFaceRecognitionConfig = async (courseId) => {
  try {
    const response = await api.get(`/course-face-config/${courseId}`)
    if (response.data.success) {
      return response.data.data
    }
    return { faceRecognitionEnabled: false, faceRecognitionFrequency: 30 }
  } catch (error) {
    console.error('获取人脸识别配置失败:', error)
    return { faceRecognitionEnabled: false, faceRecognitionFrequency: 30 }
  }
}

/**
 * 初始化人脸识别配置
 * @param {number} courseId 课程ID
 */
export const initFaceRecognition = async (courseId) => {
  const config = await getFaceRecognitionConfig(courseId)
  faceRecognitionState.isEnabled = config.faceRecognitionEnabled
  faceRecognitionState.frequency = config.faceRecognitionFrequency || 30
  return config
}

/**
 * 检查是否需要进行人脸识别
 * @param {number} currentTime 当前视频播放时间（秒）
 * @returns {boolean} 是否需要识别
 */
export const shouldPerformRecognition = (currentTime) => {
  if (!faceRecognitionState.isEnabled) return false
  
  const timeSinceLastRecognition = currentTime - faceRecognitionState.lastRecognitionTime
  return timeSinceLastRecognition >= faceRecognitionState.frequency
}

/**
 * 启动摄像头
 * @returns {Promise<MediaStream>} 摄像头流
 */
export const startCamera = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { 
        width: { ideal: 640 },
        height: { ideal: 480 },
        facingMode: 'user'
      },
      audio: false
    })
    faceRecognitionState.stream = stream
    return stream
  } catch (error) {
    console.error('启动摄像头失败:', error)
    throw new Error('无法访问摄像头，请检查权限设置')
  }
}

/**
 * 停止摄像头
 */
export const stopCamera = () => {
  if (faceRecognitionState.stream) {
    faceRecognitionState.stream.getTracks().forEach(track => track.stop())
    faceRecognitionState.stream = null
  }
}

/**
 * 捕获图像
 * @param {HTMLVideoElement} video 视频元素
 * @param {HTMLCanvasElement} canvas 画布元素
 * @returns {string} Base64格式的图像数据
 */
export const captureImage = (video, canvas) => {
  if (!video || !canvas) return null
  
  const context = canvas.getContext('2d')
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  
  // 绘制视频帧到画布
  context.drawImage(video, 0, 0, canvas.width, canvas.height)
  
  // 转换为base64
  return canvas.toDataURL('image/jpeg', 0.8)
}

/**
 * 执行人脸识别
 * @param {string} imageData 图像base64数据
 * @param {number} courseId 课程ID
 * @returns {Promise} 识别结果
 */
export const performFaceRecognition = async (imageData, courseId) => {
  try {
    const response = await api.post('/face-recognition/verify', {
      image: imageData,
      courseId: courseId
    })
    
    if (response.data.success) {
      return {
        success: true,
        similarity: response.data.data.similarity || 0,
        userId: response.data.data.userId,
        userName: response.data.data.userName,
        message: response.data.message || '识别成功'
      }
    } else {
      return {
        success: false,
        message: response.data.message || '识别失败',
        similarity: 0
      }
    }
  } catch (error) {
    console.error('人脸识别失败:', error)
    return {
      success: false,
      message: '识别服务异常，请稍后重试',
      similarity: 0
    }
  }
}

/**
 * 显示人脸识别弹窗
 * @param {number} courseId 课程ID
 * @param {Function} onSuccess 成功回调
 * @param {Function} onFail 失败回调
 */
export const showFaceRecognitionDialog = async (courseId, onSuccess, onFail) => {
  faceDialogState.visible = true
  faceDialogState.loading = true
  faceDialogState.countdown = 5
  faceDialogState.message = '请保持面部正对摄像头'
  faceDialogState.result = null

  try {
    // 启动倒计时
    const countdownInterval = setInterval(() => {
      faceDialogState.countdown--
      if (faceDialogState.countdown <= 0) {
        clearInterval(countdownInterval)
        performRecognition(courseId, onSuccess, onFail)
      }
    }, 1000)

  } catch (error) {
    ElMessage.error('启动人脸识别失败')
    faceDialogState.visible = false
    if (onFail) onFail(error)
  }
}

/**
 * 执行实际的人脸识别
 * @param {number} courseId 课程ID
 * @param {Function} onSuccess 成功回调
 * @param {Function} onFail 失败回调
 */
const performRecognition = async (courseId, onSuccess, onFail) => {
  try {
    // 创建视频和画布元素
    const video = document.createElement('video')
    const canvas = document.createElement('canvas')
    
    // 启动摄像头
    const stream = await startCamera()
    video.srcObject = stream
    
    // 等待视频加载
    await new Promise((resolve) => {
      video.onloadedmetadata = resolve
    })
    
    // 捕获图像
    const imageData = captureImage(video, canvas)
    
    if (!imageData) {
      throw new Error('图像捕获失败')
    }
    
    // 执行人脸识别
    const result = await performFaceRecognition(imageData, courseId)
    
    // 更新最后识别时间
    faceRecognitionState.lastRecognitionTime = Date.now() / 1000
    
    // 停止摄像头
    stopCamera()
    
    // 处理结果
    faceDialogState.result = result
    faceDialogState.loading = false
    
    if (result.success) {
      faceDialogState.message = `识别成功！相似度: ${(result.similarity * 100).toFixed(1)}%`
      setTimeout(() => {
        faceDialogState.visible = false
        if (onSuccess) onSuccess(result)
      }, 2000)
    } else {
      faceDialogState.message = result.message || '识别失败，请重试'
      setTimeout(() => {
        faceDialogState.visible = false
        if (onFail) onFail(result)
      }, 2000)
    }
    
  } catch (error) {
    console.error('人脸识别过程出错:', error)
    stopCamera()
    faceDialogState.visible = false
    if (onFail) onFail(error)
  }
}

/**
 * 记录人脸识别结果
 * @param {Object} result 识别结果
 * @param {number} courseId 课程ID
 */
export const recordFaceRecognition = async (result, courseId) => {
  try {
    await api.post('/face-recognition/record', {
      courseId: courseId,
      success: result.success,
      similarity: result.similarity,
      userId: result.userId,
      timestamp: new Date().toISOString()
    })
  } catch (error) {
    console.error('记录人脸识别结果失败:', error)
  }
}

/**
 * 获取人脸识别状态
 */
export const getFaceRecognitionState = () => {
  return {
    ...faceRecognitionState,
    dialog: faceDialogState
  }
}

/**
 * 重置人脸识别状态
 */
export const resetFaceRecognition = () => {
  faceRecognitionState.lastRecognitionTime = 0
  faceRecognitionState.isRecognizing = false
  faceRecognitionState.recognitionResult = null
}

// 导出状态和方法（已单独导出，这里不需要重复导出）
// export {
//   faceRecognitionState,
//   faceDialogState,
//   getFaceRecognitionConfig,
//   initFaceRecognition,
//   shouldPerformRecognition,
//   startCamera,
//   stopCamera,
//   captureImage,
//   performFaceRecognition,
//   showFaceRecognitionDialog,
//   recordFaceRecognition,
//   getFaceRecognitionState,
//   resetFaceRecognition
// }