#file:C:\Users\22374\IdeaProjects\training-system-new2\frontend\src\views\admin\CourseDetail.vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElButton, ElCard, ElTag, ElMessage, ElProgress } from 'element-plus'
import * as XLSX from 'xlsx'
import api from '@/api'

// 学员学习数据
const studentData = ref([])
const loading = ref(false)

// 模拟数据用于测试
const mockStudentData = [
  {
    id: 1,
    name: '张三',
    learningDuration: 45000, // 12.5小时
    completionRate: 0.85,
    lastStudyTime: '2023-10-15 14:30:22'
  },
  {
    id: 2,
    name: '李四',
    learningDuration: 29700, // 8.25小时
    completionRate: 0.60,
    lastStudyTime: '2023-10-14 09:15:45'
  }
]

// 获取学员学习信息
const fetchStudentLearningData = async () => {
  loading.value = true
  try {
    const response = await api.get('/course/students/progress')
    studentData.value = response.data.data
  } catch (error) {
    ElMessage.warning('获取学员信息失败，使用模拟数据进行测试')
    // 使用模拟数据
    studentData.value = mockStudentData
  } finally {
    loading.value = false
  }
}

// 导出为Excel
const exportToExcel = () => {
  // 准备导出数据
  const exportData = studentData.value.map(item => ({
    '学员姓名': item.name,
    '学习时长': formatLearningDuration(item.learningDuration),
    '完成度(%)': formatCompletionRate(item.completionRate),
    '最后学习时间': item.lastStudyTime
  }))

  // 创建工作表
  const worksheet = XLSX.utils.json_to_sheet(exportData)

  // 设置列宽
  worksheet['!cols'] = [
    { wch: 15 }, // 学员姓名
    { wch: 20 }, // 学习时长
    { wch: 15 }, // 完成度
    { wch: 25 }  // 最后学习时间
  ]

  // 创建工作簿
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '学员学习进度')

  // 导出文件
  XLSX.writeFile(workbook, '学员学习进度.xlsx')
}

// 格式化学习时长显示（将秒数转换为小时和分钟）
const formatLearningDuration = (seconds) => {
  if (!seconds) return '0分钟'

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 格式化完成度显示（将小数转换为百分比）
const formatCompletionRate = (rate) => {
  return Math.round(rate * 100)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchStudentLearningData()
})
</script>

<template>
  <div class="course-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程学员学习进度</span>
          <el-button type="primary" @click="exportToExcel" :disabled="studentData.length === 0">
            导出Excel
          </el-button>
        </div>
      </template>

      <el-table :data="studentData" style="width: 100%" border :loading="loading">
        <el-table-column prop="id" label="序号" width="80" />
        <el-table-column prop="name" label="学员姓名" width="120" />
        <el-table-column label="学习时长" width="150">
          <template #default="scope">
            {{ formatLearningDuration(scope.row.learningDuration) }}
          </template>
        </el-table-column>
        <el-table-column label="完成度" width="150">
          <template #default="scope">
            <el-progress
                :percentage="formatCompletionRate(scope.row.completionRate)"
                :status="formatCompletionRate(scope.row.completionRate) === 100 ? 'success' : ''"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastStudyTime" label="最后学习时间" width="200" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag
                :type="formatCompletionRate(scope.row.completionRate) === 100 ? 'success' :
                     formatCompletionRate(scope.row.completionRate) > 0 ? 'warning' : 'info'">
              {{ formatCompletionRate(scope.row.completionRate) === 100 ? '已完成' :
                formatCompletionRate(scope.row.completionRate) > 0 ? '学习中' : '未开始' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-detail {
  padding: 20px;
}

.el-table {
  margin-top: 20px;
}
</style>
