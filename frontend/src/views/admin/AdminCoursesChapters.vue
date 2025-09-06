<template>
  <div class="course-students-progress">
    <div class="header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/admin/courses' }">课程管理</el-breadcrumb-item>
        <el-breadcrumb-item>学员学习记录</el-breadcrumb-item>
      </el-breadcrumb>
      <h2>课程学员学习记录</h2>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <span>{{ courseTitle || '课程' }} - 学员学习记录</span>
            <el-tag type="info" style="margin-left: 10px">
              共 {{ studentData.length }} 名学员
            </el-tag>
          </div>
          <el-button type="primary" @click="exportToExcel" :disabled="studentData.length === 0">
            导出Excel
          </el-button>
        </div>
      </template>

      <div class="search-bar" style="margin-bottom: 20px;">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学员姓名或手机号"
          prefix-icon="el-icon-search"
          style="width: 300px"
          @input="handleSearch"
        />
      </div>
      <el-table
        :data="filteredData"
        style="width: 100%"
        border
        :loading="loading"
        v-loading="loading"
      >
        <el-table-column type="index" label="序号" width="100" />
        <el-table-column prop="studentName" label="学员姓名" />
        <el-table-column prop="studentEmail" label="手机号" />
        <el-table-column label="学习时长">
          <template #default="scope">
            {{ formatLearningDuration(scope.row.learningDuration) }}
          </template>
        </el-table-column>
        <el-table-column label="完成度">
          <template #default="scope">
            <el-progress
              :percentage="formatCompletionRate(scope.row.completionRate)"
              :status="formatCompletionRate(scope.row.completionRate) === 100 ? 'success' : ''"
            />
          </template>
        </el-table-column>
        <el-table-column label="最后学习时间">
          <template #default="scope">
            {{ formatLastStudyTime(scope.row.lastStudyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.completionRate)">
              {{ getStatusText(scope.row.completionRate) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>


      <div class="pagination" style="margin-top: 20px; text-align: right;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElTable, ElTableColumn, ElButton, ElCard, ElTag, ElMessage, ElProgress, ElBreadcrumb, ElBreadcrumbItem, ElInput, ElPagination } from 'element-plus'
import * as XLSX from 'xlsx'
import api from '@/api'

// 修改script setup部分，添加视频时长和路由参数处理
const route = useRoute()
const courseId = ref(route.params.id)


const studentData = ref([])
const loading = ref(false)
const courseTitle = ref('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const videoDuration = ref(0) // 视频总时长（秒）

// 获取学员学习记录
const fetchStudentLearningData = async () => {
  loading.value = true
  try {
    // 从路由参数获取课程名称和视频时长
    courseTitle.value = route.query.courseTitle || '未知课程'
    videoDuration.value = parseInt(route.query.videoDuration) || 3600 // 默认1小时
    
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      search: searchKeyword.value || undefined
    }
    
    const response = await api.get(`/admin/courses/${courseId.value}/students/progress`, { params })
    
    // 处理返回的数据，根据完成度计算学习时长
    studentData.value = (response.data.content || []).map(item => ({
      ...item,
      // 根据完成度计算学习时长（秒）
      learningDuration: Math.round((item.completionRate || 0) * videoDuration.value),
    }))

    total.value = response.data.totalElements || 0


  } catch (error) {
    console.error('获取学员学习记录失败:', error)
    ElMessage.error('获取学员学习记录失败')
    studentData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 过滤数据 - 现在由后端处理分页和搜索
const filteredData = computed(() => {
  return studentData.value
})

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchStudentLearningData()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchStudentLearningData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchStudentLearningData()
}
// 格式化最后学习时间显示
const formatLastStudyTime = (timeString) => {
  if (!timeString) return '-'

  try {
    // 解析ISO时间字符串
    const date = new Date(timeString)

    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return timeString // 如果解析失败，返回原始字符串
    }

    // 格式化为 YYYY-MM-DD HH:mm:ss
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (error) {
    console.error('时间格式化错误:', error)
    return timeString // 出错时返回原始字符串
  }
}

// 导出为Excel
const exportToExcel = () => {
  if (studentData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  const exportData = studentData.value.map(item => ({
    '学员姓名': item.studentName,
    '手机号': item.studentEmail,
    '学习时长': formatLearningDuration(item.learningDuration),
    '完成度(%)': formatCompletionRate(item.completionRate),
    '最后学习时间': item.lastStudyTime,
    '状态': getStatusText(item.completionRate)
  }))

  const worksheet = XLSX.utils.json_to_sheet(exportData)
  worksheet['!cols'] = [
    { wch: 15 }, // 学员姓名
    { wch: 25 }, // 手机号
    { wch: 20 }, // 学习时长
    { wch: 15 }, // 完成度
    { wch: 20 }, // 最后学习时间
    { wch: 10 }  // 状态
  ]

  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '学员学习记录')
  
  const fileName = `${courseTitle.value || '课程'}_学员学习记录.xlsx`
  XLSX.writeFile(workbook, fileName)
}

// 格式化学习时长显示（修改版 - 显示到秒）
const formatLearningDuration = (seconds) => {
  if (!seconds || seconds <= 0) return '0秒'

  const hrs = Math.floor(seconds / 3600)
  const mins = Math.floor((seconds % 3600) / 60)
  const secs = Math.floor(seconds % 60)

  let result = ''
  if (hrs > 0) {
    result += `${hrs}小时`
  }
  if (mins > 0) {
    result += `${mins}分钟`
  }
  if (secs > 0 || result === '') { // 如果总时间是0秒或者还有剩余秒数
    result += `${secs}秒`
  }

  return result
}


// 格式化完成度显示
const formatCompletionRate = (rate) => {
  return Math.round(rate * 100)
}

// 获取状态类型
const getStatusType = (rate) => {
  const percentage = formatCompletionRate(rate)
  if (percentage === 100) return 'success'
  if (percentage > 0) return 'warning'
  return 'info'
}

// 获取状态文本
const getStatusText = (rate) => {
  const percentage = formatCompletionRate(rate)
  if (percentage === 100) return '已完成'
  if (percentage > 0) return '学习中'
  return '未开始'
}


// 组件挂载时获取数据
onMounted(() => {
  fetchStudentLearningData()
})
</script>

<style scoped>
.course-students-progress {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 10px 0;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-table) {
  margin-top: 20px;
}

:deep(.el-progress) {
  width: 100%;
}
</style>
