<template>
  <div class="face-identify">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>人脸识别管理</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="content">
      <!-- 课程列表卡片 -->
      <el-card class="course-card">
        <template #header>
          <div class="card-header">
            <span>课程人脸识别设置</span>
            <div class="header-actions">
              <el-input
                  v-model="searchQuery"
                  placeholder="搜索课程..."
                  prefix-icon="el-icon-search"
                  style="width: 200px; margin-right: 10px"
                  @input="handleSearch"
              />
              <el-button type="primary" @click="showBatchSettingsDialog = true">
                批量设置
              </el-button>
            </div>
          </div>
        </template>

        <el-table
            :data="courses"
            style="width: 100%"
            v-loading="loading"
            @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="title" label="课程名称" min-width="150" />
          <el-table-column prop="description" label="课程介绍" min-width="200" />
          <el-table-column prop="isOnline" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isOnline ? 'success' : 'info'">
                {{ row.isOnline ? '已上线' : '未上线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="人脸识别" width="100">
            <template #default="{ row }">
              <el-switch
                  v-model="row.faceRecognitionEnabled"
                  @change="(val) => toggleFaceRecognition(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="识别频率" width="120">
            <template #default="{ row }">
              <span>{{ getFrequencyText(row.faceRecognitionFrequency) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openSettingsDialog(row)">
                设置
              </el-button>
              <el-button size="small" @click="openRecordsDialog(row)">
                查看记录
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            style="margin-top: 20px; text-align: right"
        />
      </el-card>
    </div>

    <!-- 单个课程设置对话框 -->
    <el-dialog
        v-model="showSettingsDialog"
        title="课程人脸识别设置"
        width="400px"
    >
      <el-form :model="currentConfig" label-width="100px">
        <el-form-item label="识别频率">
          <el-select v-model="currentConfig.faceRecognitionFrequency" style="width: 100%">
            <el-option label="15秒" :value="15" />
            <el-option label="30秒" :value="30" />
            <el-option label="1分钟" :value="60" />
            <el-option label="2分钟" :value="120" />
            <el-option label="5分钟" :value="300" />
            <el-option label="10分钟" :value="600" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSettingsDialog = false">取消</el-button>
          <el-button type="primary" @click="saveSettings">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量设置对话框 -->
    <el-dialog
        v-model="showBatchSettingsDialog"
        title="批量设置人脸识别"
        width="400px"
    >
      <el-form :model="batchConfig" label-width="100px">
        <el-form-item label="识别频率">
          <el-select v-model="batchConfig.faceRecognitionFrequency" style="width: 100%">
            <el-option label="15秒" :value="15" />
            <el-option label="30秒" :value="30" />
            <el-option label="1分钟" :value="60" />
            <el-option label="2分钟" :value="120" />
            <el-option label="5分钟" :value="300" />
            <el-option label="10分钟" :value="600" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchSettingsDialog = false">取消</el-button>
          <el-button type="primary" @click="saveBatchSettings">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 识别记录对话框 -->
    <el-dialog
        v-model="showRecordsDialog"
        :title="`人脸识别记录 - ${currentCourse?.title}`"
        width="800px"
        :close-on-click-modal="false"
    >
      <div class="records-header" style="margin-bottom: 20px;">
        <el-date-picker
            v-model="recordDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadFaceRecords"
            style="margin-right: 10px"
        />
        <el-button @click="exportRecords">导出记录</el-button>
      </div>

      <el-table :data="faceRecords" v-loading="recordsLoading" style="width: 100%">
        <el-table-column prop="userName" label="学员姓名" width="120" />
        <el-table-column prop="userId" label="学员ID" width="80" />
        <el-table-column prop="timestamp" label="识别时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="similarity" label="相似度" width="100">
          <template #default="{ row }">
            <el-tag :type="row.similarity >= 0.8 ? 'success' : 'warning'">
              {{ (row.similarity * 100).toFixed(1) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="120" />
        <el-table-column prop="userAgent" label="设备信息" min-width="200" />
      </el-table>

      <el-pagination
          @size-change="handleRecordsSizeChange"
          @current-change="handleRecordsCurrentChange"
          :current-page="recordsCurrentPage"
          :page-size="recordsPageSize"
          :page-sizes="[10, 20, 50]"
          :total="recordsTotal"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px; text-align: right"
      />
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

export default {
  name: 'FaceIdentify',
  setup() {
    // 课程列表相关
    const courses = ref([])
    const loading = ref(false)
    const searchQuery = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const selectedCourses = ref([])

    // 设置相关
    const showSettingsDialog = ref(false)
    const showBatchSettingsDialog = ref(false)
    const currentCourse = ref(null)
    const currentConfig = reactive({
      faceRecognitionEnabled: false,
      faceRecognitionFrequency: 30
    })
    const batchConfig = reactive({
      faceRecognitionEnabled: false,
      faceRecognitionFrequency: 30
    })

    // 记录相关
    const showRecordsDialog = ref(false)
    const faceRecords = ref([])
    const recordsLoading = ref(false)
    const recordDateRange = ref([])
    const recordsCurrentPage = ref(1)
    const recordsPageSize = ref(10)
    const recordsTotal = ref(0)

    // 存储所有课程数据，用于前端搜索
    const allCourses = ref([])
    
    // 加载所有课程数据
    const loadAllCourses = async () => {
      loading.value = true
      try {
        let allData = []
        let page = 0
        const size = 100 // 每页获取100条数据
        let hasMore = true
        
        // 循环获取所有数据
        while (hasMore) {
          const response = await api.get('/courses/admin/page', {
            params: { page, size }
          })
          const data = response.data.data
          allData = [...allData, ...data.content]
          hasMore = !data.last
          page++
        }
        
        allCourses.value = allData.map(course => ({
          ...course,
          faceRecognitionEnabled: false,
          faceRecognitionFrequency: 30
        }))
        
        // 加载所有人脸识别配置
        await loadAllFaceConfigs()
        
        // 应用搜索和分页
        applySearchAndPagination()
        
      } catch (error) {
        ElMessage.error('加载课程列表失败')
        console.error(error)
      } finally {
        loading.value = false
      }
    }

    // 加载所有课程的人脸识别配置
    const loadAllFaceConfigs = async () => {
      try {
        const promises = allCourses.value.map(async (course) => {
          try {
            const response = await api.get(`/course-face-config/${course.id}`)
            if (response.data.success === true) {
              course.faceRecognitionEnabled = response.data.data.faceRecognitionEnabled
              course.faceRecognitionFrequency = response.data.data.faceRecognitionFrequency
            }
          } catch (error) {
            // 如果配置不存在，使用默认值
            if (error.response?.status === 404) {
              course.faceRecognitionEnabled = false
              course.faceRecognitionFrequency = 30
            } else {
              console.error(`加载课程 ${course.id} 人脸识别配置失败:`, error)
            }
          }
        })
        await Promise.all(promises)
      } catch (error) {
        console.error('加载人脸识别配置失败:', error)
      }
    }

    // 应用搜索和分页
    const applySearchAndPagination = () => {
      let filteredCourses = allCourses.value
      
      // 前端搜索过滤
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filteredCourses = allCourses.value.filter(course => 
          course.title?.toLowerCase().includes(query) ||
          course.description?.toLowerCase().includes(query) ||
          course.instructor?.toLowerCase().includes(query)
        )
      }
      
      // 计算总数
      total.value = filteredCourses.length
      
      // 应用分页
      const startIndex = (currentPage.value - 1) * pageSize.value
      const endIndex = startIndex + pageSize.value
      courses.value = filteredCourses.slice(startIndex, endIndex)
    }



    // 打开单个设置对话框
    const openSettingsDialog = (course) => {
      currentCourse.value = course
      currentConfig.faceRecognitionEnabled = course.faceRecognitionEnabled
      currentConfig.faceRecognitionFrequency = course.faceRecognitionFrequency
      showSettingsDialog.value = true
    }

    // 保存单个设置
    const saveSettings = async () => {
      try {
        const response = await api.put(`/course-face-config/${currentCourse.value.id}`, currentConfig)
        if (response.data.success === true) {
          ElMessage.success('设置保存成功')
          showSettingsDialog.value = false
          // 更新当前课程的数据
          const course = courses.value.find(c => c.id === currentCourse.value.id)
          if (course) {
            course.faceRecognitionEnabled = currentConfig.faceRecognitionEnabled
            course.faceRecognitionFrequency = currentConfig.faceRecognitionFrequency
          }
        }
      } catch (error) {
        ElMessage.error('保存设置失败')
        console.error(error)
      }
    }

    // 切换人脸识别开关
    const toggleFaceRecognition = async (course, enabled) => {
      try {
        const config = {
          faceRecognitionEnabled: enabled,
          faceRecognitionFrequency: course.faceRecognitionFrequency
        }
        const response = await api.put(`/course-face-config/${course.id}`, config)
        if (response.data.success === true) {
          ElMessage.success('设置更新成功')
        }
      } catch (error) {
        ElMessage.error('更新设置失败')
        course.faceRecognitionEnabled = !enabled
      }
    }

    // 批量设置
    const handleSelectionChange = (selection) => {
      selectedCourses.value = selection
    }

    const saveBatchSettings = async () => {
      if (selectedCourses.value.length === 0) {
        ElMessage.warning('请先选择课程')
        return
      }

      try {
        const configs = selectedCourses.value.map(course => ({
          courseId: course.id,
          faceRecognitionEnabled: true, // 批量设置时默认开启
          faceRecognitionFrequency: batchConfig.faceRecognitionFrequency
        }))

        const response = await api.put('/course-face-config/batch', configs)
        if (response.data.success === true) {
          ElMessage.success('批量设置成功')
          showBatchSettingsDialog.value = false
          // 更新本地数据
          selectedCourses.value.forEach(course => {
            const targetCourse = courses.value.find(c => c.id === course.id)
            if (targetCourse) {
              targetCourse.faceRecognitionEnabled = true
              targetCourse.faceRecognitionFrequency = batchConfig.faceRecognitionFrequency
            }
          })
        }
      } catch (error) {
        ElMessage.error('批量设置失败')
        console.error(error)
      }
    }

    // 打开记录对话框
    const openRecordsDialog = async (course) => {
      currentCourse.value = course
      showRecordsDialog.value = true
      recordDateRange.value = []
      recordsCurrentPage.value = 1
      await loadFaceRecords()
    }

    // 加载人脸识别记录
    const loadFaceRecords = async () => {
      recordsLoading.value = true
      try {
        const params = {
          courseId: currentCourse.value.id,
          page: recordsCurrentPage.value - 1,
          size: recordsPageSize.value
        }

        if (recordDateRange.value && recordDateRange.value.length === 2) {
          params.startDate = recordDateRange.value[0].toISOString()
          params.endDate = recordDateRange.value[1].toISOString()
        }

        const response = await api.get('/admin/face-records', { params })
        if (response.data.success === true) {
          const data = response.data.data
          faceRecords.value = data.content
          recordsTotal.value = data.totalElements
        }
      } catch (error) {
        ElMessage.error('加载识别记录失败')
        console.error(error)
      } finally {
        recordsLoading.value = false
      }
    }

    // 导出记录
    const exportRecords = () => {
      const params = {
        courseId: currentCourse.value.id
      }

      if (recordDateRange.value && recordDateRange.value.length === 2) {
        params.startDate = recordDateRange.value[0].toISOString()
        params.endDate = recordDateRange.value[1].toISOString()
      }

      const queryString = new URLSearchParams(params).toString()
      window.open(`/admin/face-records/export?${queryString}`, '_blank')
    }

    // 工具方法
    const getFrequencyText = (frequency) => {
      const map = {
        15: '15秒',
        30: '30秒',
        60: '1分钟',
        120: '2分钟',
        300: '5分钟',
        600: '10分钟'
      }
      return map[frequency] || `${frequency}秒`
    }

    const formatDateTime = (dateString) => {
      return new Date(dateString).toLocaleString('zh-CN')
    }

    const handleSearch = () => {
      currentPage.value = 1
      applySearchAndPagination()
    }

    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1 // 重置到第一页
      applySearchAndPagination()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      applySearchAndPagination()
    }

    const handleRecordsSizeChange = (val) => {
      recordsPageSize.value = val
      loadFaceRecords()
    }

    const handleRecordsCurrentChange = (val) => {
      recordsCurrentPage.value = val
      loadFaceRecords()
    }

    onMounted(() => {
      loadAllCourses()
    })

    return {
      courses,
      loading,
      searchQuery,
      currentPage,
      pageSize,
      total,
      selectedCourses,
      showSettingsDialog,
      showBatchSettingsDialog,
      currentCourse,
      currentConfig,
      batchConfig,
      showRecordsDialog,
      faceRecords,
      recordsLoading,
      recordDateRange,
      recordsCurrentPage,
      recordsPageSize,
      recordsTotal,
      openSettingsDialog,
      saveSettings,
      toggleFaceRecognition,
      openRecordsDialog,
      saveBatchSettings,
      handleSelectionChange,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      handleRecordsSizeChange,
      handleRecordsCurrentChange,
      loadFaceRecords,
      exportRecords,
      getFrequencyText,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.face-identify {
  padding: 20px;
}

.content {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.course-card {
  margin-bottom: 20px;
}

.records-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
</style>
