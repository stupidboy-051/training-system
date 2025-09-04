<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'
// 人脸识别状态
const faceRecognitionEnabled = ref(false)

// 识别频率设置（秒）
const recognitionFrequency = ref(5)

// 识别记录列表
const recognitionRecords = ref([])

// 加载状态
const loading = ref(false)

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 频率选项
const frequencyOptions = [
  { label: '每5秒', value: 5 },
  { label: '每10秒', value: 10 },
  { label: '每30秒', value: 30 },
  { label: '每60秒', value: 60 }
]

// 获取识别记录列表
const fetchRecognitionRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.currentPage,
      size: pagination.value.pageSize
    }
    const response = await api.get('/admin/face-records/page', { params })
    recognitionRecords.value = response.data.records
    pagination.value.total = response.data.total
  } catch (error) {
    ElMessage.error('获取识别记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取识别记录详情
const viewRecordDetail = async (record) => {
  const response = await api.get(`/admin/face-records/${record.id}`)
  console.log(response.data)
}

// 切换人脸识别开关
const toggleFaceRecognition = () => {
  if (faceRecognitionEnabled.value) {
    ElMessageBox.confirm(
        '确定要关闭人脸识别功能吗？',
        '关闭确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    ).then(() => {
      faceRecognitionEnabled.value = false
      ElMessage({
        type: 'success',
        message: '人脸识别功能已关闭',
      })
    }).catch(() => {
      // 用户取消操作
    })
  } else {
    faceRecognitionEnabled.value = true
    ElMessage({
      type: 'success',
      message: '人脸识别功能已开启',
    })
  }
}

// 保存识别频率设置
const saveFrequency = async () => {
  try {
    await api.post('/admin/face-config/frequency', {
      frequency: recognitionFrequency.value
    })
    ElMessage({
      type: 'success',
      message: `识别频率已设置为每${recognitionFrequency.value}秒`,
    })
  } catch (error) {
    ElMessage.error('保存频率设置失败')
    console.error(error)
  }
}

// 清空识别记录
const clearRecords = () => {
  ElMessageBox.confirm(
      '确定要清空所有识别记录吗？此操作不可恢复',
      '清空确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    try {
      await api.delete('/admin/face-records')
      recognitionRecords.value = []
      pagination.value.total = 0
      ElMessage({
        type: 'success',
        message: '识别记录已清空',
      })
    } catch (error) {
      ElMessage.error('清空记录失败')
      console.error(error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 导出识别记录
const exportRecords = async () => {
  try {
    const response = await api.get('/admin/face-records/export', {
      responseType: 'blob'
    })
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'face-records.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    ElMessage({
      type: 'success',
      message: '识别记录导出成功',
    })
  } catch (error) {
    ElMessage.error('导出记录失败')
    console.error(error)
  }
}

// 处理分页变化
const handlePageChange = (page) => {
  pagination.value.currentPage = page
  fetchRecognitionRecords()
}

// 组件挂载时初始化
onMounted(() => {
  fetchRecognitionRecords()
})
</script>

<template>
  <div class="face-identify-container">
    <el-card class="control-panel">
      <template #header>
        <div class="card-header">
          <span>人脸识别控制面板</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="control-item">
            <span class="control-label">人脸识别开关：</span>
            <el-switch
                v-model="faceRecognitionEnabled"
                active-text="开启"
                inactive-text="关闭"
                @change="toggleFaceRecognition"
                size="large"
            />
          </div>
        </el-col>

        <el-col :span="12">
          <div class="control-item">
            <span class="control-label">识别频率设置：</span>
            <el-select
                v-model="recognitionFrequency"
                placeholder="请选择识别频率"
                size="default"
                style="width: 150px"
            >
              <el-option
                  v-for="item in frequencyOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
            <el-button
                type="primary"
                @click="saveFrequency"
                style="margin-left: 15px"
            >
              保存设置
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="records-panel">
      <template #header>
        <div class="card-header">
          <span>识别记录</span>
          <div>
            <el-button
                type="danger"
                @click="clearRecords"
                :disabled="recognitionRecords.length === 0"
            >
              清空记录
            </el-button>
            <el-button
                type="success"
                @click="exportRecords"
                style="margin-left: 10px"
                :disabled="recognitionRecords.length === 0"
            >
              导出记录
            </el-button>
          </div>
        </div>
      </template>

      <el-table
          :data="recognitionRecords"
          style="width: 100%"
          stripe
          v-loading="loading"
      >
        <el-table-column prop="id" label="记录ID" align="center" />
        <el-table-column prop="userId" label="用户ID" align="center" />
        <el-table-column prop="userName" label="用户名" align="center" />
        <el-table-column prop="timestamp" label="识别时间" align="center" />
        <el-table-column prop="status" label="识别状态" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewRecordDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            layout="prev, pager, next"
            :total="pagination.total"
            :page-size="pagination.pageSize"
            :current-page="pagination.currentPage"
            @current-change="handlePageChange"
            background
            hide-on-single-page
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.face-identify-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.control-panel {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.control-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-right: 15px;
  white-space: nowrap;
}

.records-panel {
  margin-top: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
