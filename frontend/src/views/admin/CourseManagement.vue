<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">课程管理</h3>
      <div class="page-actions">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon>
            <Plus/>
          </el-icon>
          创建课程
        </el-button>
      </div>
    </div>

    <div class="card">
      <div class="card-body">
        <!-- 搜索栏 -->
        <div class="search-section">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索课程..."
              class="search-input"
              clearable
              @input="handleSearch"
          >
            <template #prefix>
              <el-icon>
                <Search/>
              </el-icon>
            </template>
          </el-input>
        </div>

        <!-- 课程列表 -->
        <div class="table-container">
          <el-table :data="courses" style="width: 100%" v-loading="loading">
            <el-table-column prop="title" label="课程名称" min-width="200"/>
            <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip/>
            <el-table-column prop="title" label="课程名称" min-width="200" />
            <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
            <el-table-column prop="score" label="学时" width="100">
              <template #default="scope">
                {{ scope.row.score }} 小时
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="120">
              <template #default="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="videoUrl" label="视频链接" width="200" show-overflow-tooltip/>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.isOnline ? 'success' : 'info'">
                  {{ scope.row.isOnline ? '已上线' : '未上线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="320" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="viewCourse(scope.row)">查看</el-button>
                <el-button size="small" type="primary" @click="editCourse(scope.row)">
                  编辑
                </el-button>
                <el-button size="small" type="warning" @click="question(scope.row)">
                  弹题
                </el-button>
                <el-button size="small" type="" @click="manageChapters(scope.row)">
                  预览
                </el-button>
                <el-button size="small" type="danger" @click="deleteCourse(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination-container">
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

        <!-- 空状态 -->
        <el-empty v-if="!loading && courses.length === 0" description="暂无课程数据"/>
      </div>
    </div>

    <!-- 创建/编辑课程对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑课程' : '创建课程'"
        width="600px"
        class="dialog-container"
    >
      <div class="dialog-body">
        <el-form :model="courseForm" :rules="rules" ref="courseFormRef" label-width="100px">
          <el-form-item label="课程名称" prop="title">
            <el-input v-model="courseForm.title" placeholder="请输入课程名称"/>
          </el-form-item>
          <el-form-item label="课程描述" prop="description">
            <el-input
                v-model="courseForm.description"
                type="textarea"
                :rows="3"
                placeholder="请输入课程描述"
            />
          </el-form-item>
          <el-form-item label="视频链接" prop="videoUrl">
            <el-input v-model="courseForm.videoUrl" placeholder="请输入视频链接"/>
          </el-form-item>
          <el-form-item label="封面图片" prop="coverImageUrl">
            <el-input v-model="courseForm.coverImageUrl" placeholder="请输入封面图片链接"/>
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="课程价格" prop="price">
                <el-input-number v-model="courseForm.price" :min="0" :max="10000"/>
                <span style="margin-left: 10px">元</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学时" prop="score">
                <el-input-number v-model="courseForm.score" :min="0" :max="1000" />
                <span style="margin-left: 10px">小时</span>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开始时间" prop="startTime">
                <el-date-picker
                    v-model="courseForm.startTime"
                    type="datetime"
                    placeholder="选择开始时间"
                    style="width: 100%"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    @change="checkTimeRange"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束时间" prop="endTime">
                <el-date-picker
                    v-model="courseForm.endTime"
                    type="datetime"
                    placeholder="选择结束时间"
                    style="width: 100%"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    @change="checkTimeRange"
                />
              </el-form-item>
            </el-col>
          </el-row>


          <el-form-item label="是否上线" prop="isOnline">
            <el-switch v-model="courseForm.isOnline" />
          </el-form-item>
          <el-form-item label="可见角色" prop="visibleRoleIds">
            <el-select
                v-model="courseForm.visibleRoleIds"
                multiple
                placeholder="请选择可见角色分类"
                style="width: 100%"
            >
              <el-option
                  v-for="role in roleCategories"
                  :key="role.id"
                  :label="role.name"
                  :value="role.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCourse">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog
        v-model="questionDialogVisible"
        :title="`弹题设置 - ${selectedCourse?.title}`"
        width="900px"
        class="dialog-container"
    >
      <div class="dialog-body">
        <div v-loading="userLoading">
          <!-- 视频信息和时长 -->
          <div class="video-info" v-if="videoDuration > 0">
            <el-text>视频时长: {{ formatVideoTime(videoDuration) }}</el-text>
          </div>

          <!-- 按课程可见角色分组显示用户 -->
          <el-collapse v-model="activeRoleGroups" accordion v-if="roleGroupedUsers.length > 0">
            <el-collapse-item
                v-for="group in roleGroupedUsers"
                :key="group.roleId"
                :name="group.roleId"
            >
              <template #title>
                <div style="display: flex; align-items: center; width: 100%;">
                  <span style="font-weight: bold; margin-right: 10px;">{{ group.roleName }}</span>
                  <el-tag size="small">{{ group.users.length }} 人</el-tag>
                  <div style="flex: 1;"></div>
                  <el-button
                      size="small"
                      type="primary"
                      @click.stop="applySettingsToRoleGroup(group)"
                  >
                    应用设置到组
                  </el-button>
                </div>
              </template>

              <div style="padding: 10px;">
                <div style="margin-bottom: 15px;">
                  <el-text style="margin-right: 15px;">组内统一设置:</el-text>
                  <el-button size="small" @click="addTimePointToGroup(group)">
                    添加时间点
                  </el-button>

                  <div class="time-points-container">
                    <div
                        v-for="(timePoint, index) in group.settings.timePoints"
                        :key="index"
                        class="time-point-item"
                    >
                      <el-input-number
                          v-model="timePoint.time"
                          :min="0"
                          :max="videoDuration"
                          :step="1"
                          size="small"
                          controls-position="right"                      style="width: 120px;"
                      />
                      <el-text style="margin: 0 5px;">秒</el-text>
                      <!-- 显示已选择的题目数量 -->
                      <el-tag
                          v-if="timePoint.questions && timePoint.questions.length > 0"
                          type="success"
                          size="small"    style="margin-right: 10px;"
                      >
                        {{ timePoint.questions.length }} 题
                      </el-tag>

                      <!-- 选择题目按钮 -->
                      <el-button
                          size="small"
                          type="primary"
                          @click="openQuestionSelectDialog(timePoint)"    style="margin-right: 10px;"
                      >
                        选择题目
                      </el-button>
                      <el-button
                          size="small"
                          type="danger"
                          @click="removeTimePointFromGroup(group, index)"                      style="margin-left: 10px;"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>

                <el-table :data="group.users" style="width: 100%" max-height="300">
                  <el-table-column prop="username" label="用户名" width="150" />
                  <el-table-column prop="realName" label="真实姓名" width="150" />
                  <el-table-column label="弹题时间点(秒)" width="300">
                    <template #default="scope">
                      <div class="user-time-points">
                        <div
                            v-for="(timePoint, index) in scope.row.questionSettings.timePoints"
                            :key="index"
                            class="time-point-item"
                        >
                          <el-input-number
                              v-model="timePoint.time"
                              :min="0"
                              :max="videoDuration"
                              :step="1"
                              size="small"
                              controls-position="right"                          style="width: 100px;"
                          />
                          <el-text style="margin: 0 10px;">秒</el-text>

                          <!-- 显示已选择的题目数量 -->
                          <el-tag
                              v-if="timePoint.questions && timePoint.questions.length > 0"
                              type="success"
                              size="small"        style="margin-right: 10px;"
                          >
                            {{ timePoint.questions.length }} 题
                          </el-tag>

                          <!-- 选择题目按钮 -->
                          <el-button
                              size="small"
                              type="primary"
                              @click="openQuestionSelectDialog(timePoint, scope.row.id)"        style="margin-right: 5px;"
                          >
                            选择题目
                          </el-button>
                          <el-button
                              size="small"
                              type="danger"
                              @click="removeTimePointFromUser(scope.row, index)"                          style="margin-left: 5px;"
                          >
                            删除
                          </el-button>
                        </div>
                        <el-button
                            size="small"
                            @click="addTimePointToUser(scope.row)"                        style="margin-top: 5px;"
                        >
                          添加时间点
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-collapse-item>
          </el-collapse>

          <el-empty v-else description="暂无用户数据" />
        </div>
      </div>


      <!-- 在这里添加题目选择对话框 -->
      <el-dialog
          v-model="questionSelectDialogVisible"
          :title="`选择题目 - 时间点: ${currentTimePoint ? formatVideoTime(currentTimePoint.time) : ''}秒`"
          width="1200px"
          class="question-select-dialog"
          append-to-body
      >
        <div class="question-select-container">
          <!-- 题库列表 -->
          <div class="question-bank-list">
            <div class="section-header">
              <h4>题库列表</h4>
              <el-input
                  v-model="questionBankSearchKeyword"
                  placeholder="搜索题库..."
                  class="search-input"
                  clearable
                  @input="handleQuestionBankSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>

            <div class="bank-list-container" v-loading="questionBankLoading">
              <el-table
                  :data="questionBanks"
                  style="width: 100%"
                  highlight-current-row
                  @current-change="handleQuestionBankSelect"
              >
                <el-table-column prop="title" label="题库名称" min-width="150"/>
                <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip/>
                <el-table-column prop="price" label="价格" width="80">
                  <template #default="scope">
                    ¥{{ scope.row.price }}
                  </template>
                </el-table-column>
              </el-table>

              <!-- 题库分页 -->
              <div class="pagination-container">
                <el-pagination
                    v-model:current-page="questionBankCurrentPage"
                    v-model:page-size="questionBankPageSize"
                    :page-sizes="[5, 10, 20]"
                    :total="questionBankTotal"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleQuestionBankSizeChange"
                    @current-change="handleQuestionBankCurrentChange"
                />
              </div>
            </div>
          </div>

          <!-- 题目列表 -->
          <div class="question-list">
            <div class="section-header">
              <h4>题目列表</h4>
              <el-input
                  v-model="questionSearchKeyword"
                  placeholder="搜索题目..."
                  class="search-input"
                  clearable
                  @input="handleQuestionSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>

            <div class="question-list-container" v-loading="questionLoading">
              <el-table
                  :data="questions"
                  style="width: 100%"
                  @selection-change="handleQuestionSelectionChange"
                  ref="questionTableRef"
              >
                <el-table-column type="selection" width="55"/>
                <el-table-column prop="content" label="题目内容" show-overflow-tooltip min-width="250"/>
                <el-table-column prop="type" label="题目类型" width="100">
                  <template #default="scope">
                    <el-tag :type="getQuestionTypeTag(scope.row.type)">
                      {{ getQuestionTypeText(scope.row.type) }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 题目分页 -->
              <div class="pagination-container">
                <el-pagination
                    v-model:current-page="questionCurrentPage"
                    v-model:page-size="questionPageSize"
                    :page-sizes="[10, 20, 50]"
                    :total="questionTotal"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleQuestionSizeChange"
                    @current-change="handleQuestionCurrentChange"
                />
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="questionSelectDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmQuestionSelection" :disabled="selectedQuestions.length === 0">
              确定选择 ({{ selectedQuestions.length }}题)
            </el-button>
          </div>
        </template>
      </el-dialog>



      <template #footer>
        <div class="dialog-footer">
          <el-button @click="questionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveQuestionSettings">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {ref, reactive, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Search} from '@element-plus/icons-vue'
import api from '@/api'
import { nextTick } from 'vue'

export default {
  name: 'CourseManagement',
  components: {
    Plus,
    Search
  },
  setup() {
    const router = useRouter()

    const courses = ref([])
    const loading = ref(false)
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const courseFormRef = ref(null)
    const searchKeyword = ref('')
    const roleCategories = ref([])
    const currentPage = ref(1)
    const pageSize = ref(20)
    const total = ref(0)

    const courseForm = reactive({
      id: null,
      title: '',
      description: '',
      videoUrl: '',
      isOnline: true,
      visibleRoleIds: [],
      price: 0,
      coverImageUrl: '',
      score: 0,
      startTime: '',
      endTime: ''
    })

    const rules = {
      title: [
        {required: true, message: '请输入课程名称', trigger: 'blur'}
      ],
      description: [
        {required: true, message: '请输入课程描述', trigger: 'blur'}
      ],
      videoUrl: [
        {required: true, message: '请输入视频链接', trigger: 'blur'}
      ],
      visibleRoleIds: [
        {type: 'array', required: true, message: '请选择可见用户角色', trigger: 'change'}
      ],
      price: [
        { required: true, message: '请输入课程价格', trigger: 'blur' }
      ],
      score: [
        { required: true, message: '请输入学时', trigger: 'blur' }
      ],
      startTime: [
        { required: true, message: '请选择开始时间', trigger: 'change' }
      ],
      endTime: [
        { required: true, message: '请选择结束时间', trigger: 'change' }
      ]
    }
    const checkTimeRange = () => {
      if (courseForm.startTime && courseForm.endTime) {
        const startTime = new Date(courseForm.startTime)
        const endTime = new Date(courseForm.endTime)

        if (startTime >= endTime) {
          ElMessage.warning('注意：结束时间应晚于开始时间')
        }
      }
    }

    const loadCourses = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value,
          keyword: searchKeyword.value
        }
        const response = await api.get('/courses/admin/page', {params})
        if (response.data.success) {
          courses.value = response.data.data.content
          total.value = response.data.data.totalElements
        } else {
          ElMessage.error(response.data.message || '加载课程失败')
        }
      } catch (error) {
        console.error('Load courses error:', error)
        const errorMessage = error.response?.data?.message || error.message || '加载课程失败'
        ElMessage.error(errorMessage)
      } finally {
        loading.value = false
      }
    }

    const showCreateDialog = () => {
      isEdit.value = false
      Object.assign(courseForm, {
        id: null,
        title: '',
        description: '',
        videoUrl: '',
        coverImageUrl: '',
        isOnline: true,
        visibleRoleIds: [],
        price: 0,
        score: 0,
        startTime: '',
        endTime: '',
      })
      dialogVisible.value = true
    }

    const editCourse = (course) => {
      isEdit.value = true
      Object.assign(courseForm, {
        id: course.id,
        title: course.title,
        description: course.description,
        videoUrl: course.videoUrl,
        coverImageUrl: course.coverImageUrl,
        isOnline: course.isOnline,
        visibleRoleIds: course.visibleRoles ? course.visibleRoles.map(role => role.id) : [],
        price: course.price || 0,
        score: course.score || 0,
        startTime: course.startTime,
        endTime: course.endTime,
      })
      dialogVisible.value = true
    }

    const submitCourse = async () => {
      try {
        await courseFormRef.value.validate()

        // 添加时间范围验证
        if (courseForm.startTime && courseForm.endTime) {
          const startTime = new Date(courseForm.startTime)
          const endTime = new Date(courseForm.endTime)

          if (startTime >= endTime) {
            ElMessage.error('结束时间必须晚于开始时间')
            return
          }
        }

        if (isEdit.value) {
          console.log('请求参数', courseForm)
          await api.put(`/courses/${courseForm.id}`, courseForm)
          ElMessage.success('更新成功')
        } else {
          await api.post('/courses', courseForm)
          ElMessage.success('创建成功')
        }

        dialogVisible.value = false
        loadCourses()
      } catch (error) {
        console.error('Submit course error:', error)
        const errorMessage = error.response?.data?.message || error.message || '操作失败'
        ElMessage.error(errorMessage)
      }
    }

    const deleteCourse = async (course) => {
      try {
        await ElMessageBox.confirm('确定要删除这个课程吗？此操作不可逆！', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await api.delete(`/courses/${course.id}`)
        ElMessage.success('删除成功')
        loadCourses()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Delete course error:', error)
          const errorMessage = error.response?.data?.message || error.message || '删除失败'
          ElMessage.error(errorMessage)
        }
      }
    }

    const viewCourse = (course) => {
      router.push(`/admin/courses/${course.id}`)
    }

    const manageChapters = (course) => {
      router.push(`/admin/courses/${course.id}/chapters`)
    }

    const handleSearch = () => {
      currentPage.value = 1
      loadCourses()
    }

    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadCourses()
    }

    const handleCurrentChange = (page) => {
      currentPage.value = page
      loadCourses()
    }

    const formatDateTime = (dateTime) => {
      return new Date(dateTime).toLocaleString()
    }

    onMounted(() => {
      loadCourses()
      loadRoleCategories()
    })

    const loadRoleCategories = async () => {
      try {
        const response = await api.get('/categories/roles')
        roleCategories.value = response.data.data
      } catch (error) {
        console.error('Failed to load role categories:', error)
        ElMessage.error('获取角色分类失败')
      }
    }

          // 在现有的 ref 和 reactive 声明中添加/修改
      const questionDialogVisible = ref(false)
      const activeRoleGroups = ref([])
      const userLoading = ref(false)
      const selectedCourse = ref(null)

      // 按角色分组的用户数据（基于课程可见角色）
      const roleGroupedUsers = ref([])

      // 在 methods 中添加以下方法
    const question = async (course) => {
      selectedCourse.value = course
      questionDialogVisible.value = true

      // 获取视频时长
      try {
        videoDuration.value = await getVideoDuration(course.videoUrl)
      } catch (error) {
        console.error('获取视频时长失败:', error)
        ElMessage.warning('无法获取视频时长，将使用默认值')
        videoDuration.value = 3600 // 默认1小时
      }

      // 加载用户数据并按课程可见角色分组
      await loadUsersByVisibleRoles(course)

      // 加载已有的弹题设置
      await loadExistingQuestionSettings(course.id)
    }
// 加载已有的弹题设置
    const loadExistingQuestionSettings = async (courseId) => {
      try {
        const response = await api.get(`/courses/${courseId}/question-settings`)
        if (response.data.success) {
          const settings = response.data.data

          // 更新角色设置
          if (settings.roleSettings) {
            settings.roleSettings.forEach(roleSetting => {
              const group = roleGroupedUsers.value.find(g => g.roleId === roleSetting.roleId)
              if (group) {
                group.settings.timePoints = roleSetting.timePoints.map(tp => ({
                  time: tp.time,
                  questions: tp.questions || []
                }))

                // 更新该组下所有用户的时间点设置
                group.users.forEach(user => {
                  // 从用户设置中查找该用户的设置，如果没有则使用组设置
                  const userSetting = settings.userSettings.find(us => us.userId === user.id)
                  if (userSetting) {
                    user.questionSettings = {
                      timePoints: userSetting.timePoints.map(tp => ({
                        time: tp.time,
                        questions: tp.questions || []
                      }))
                    }
                  } else {
                    // 如果没有用户特定设置，则复制组设置
                    user.questionSettings = {
                      timePoints: group.settings.timePoints.map(tp => ({ ...tp }))
                    }
                  }
                })
              }
            })
          }

          // 更新没有在roleSettings中但有特定用户设置的用户
          if (settings.userSettings) {
            settings.userSettings.forEach(userSetting => {
              // 查找该用户是否已经在某个组中
              let foundUser = false
              for (const group of roleGroupedUsers.value) {
                const user = group.users.find(u => u.id === userSetting.userId)
                if (user) {
                  user.questionSettings = {
                    timePoints: userSetting.timePoints.map(tp => ({
                      time: tp.time,
                      questions: tp.questions || []
                    }))
                  }
                  foundUser = true
                  break
                }
              }
            })
          }
        }
      } catch (error) {
        console.error('加载弹题设置失败:', error)
        ElMessage.error('加载已有弹题设置失败')
      }
    }
      const loadUsersByVisibleRoles = async (course) => {
        userLoading.value = true
        try {
          // 基于课程的可见角色创建分组
          const groups = []

          // 初始化分组（基于课程可见角色）
          course.visibleRoles.forEach(role => {
            groups.push({
              roleId: role.id,
              roleName: role.name,
              settings: {
                timePoints: [{ time: 300, questions: [] }] // 默认5分钟，添加questions数组
              },
              users: []
            })
          })

          // 获取所有用户（按照 UserManagement.vue 的方式）
          const response = await api.get('/users/page', {
            params: {
              page: 0,
              size: 10000,  // 获取所有用户
              keyword: ''
            }
          })

          if (response.data.success) {
            const allUsers = response.data.data.content || []

            // 将用户按角色分组
            allUsers.forEach(user => {
              // 查找用户角色是否在课程可见角色中
              const group = groups.find(g => g.roleId === user.role?.id)
              if (group) {
                // 为每个用户初始化弹题设置
                user.questionSettings = {
                  timePoints: [{ time: 300, questions: [] }] // 默认5分钟，添加questions数组
                }
                group.users.push(user)
              }
            })
          }

          roleGroupedUsers.value = groups
        } catch (error) {
          console.error('加载用户数据失败', error)
          ElMessage.error('加载用户数据失败')
        } finally {
          userLoading.value = false
        }
      }


    // 添加视频时长相关变量
    const videoDuration = ref(0)
    const videoElement = ref(null)

    // 添加格式化视频时间的方法
    const formatVideoTime = (seconds) => {
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    }

    // 获取视频时长的方法
    const getVideoDuration = (videoUrl) => {
      return new Promise((resolve, reject) => {
        if (!videoUrl) {
          reject(new Error('视频URL为空'))
          return
        }

        // 创建临时video元素来获取时长
        const video = document.createElement('video')
        videoElement.value = video
        video.src = videoUrl

        video.addEventListener('loadedmetadata', () => {
          resolve(video.duration)
        })

        video.addEventListener('error', (err) => {
          reject(err)
        })
      })
    }
    // 添加时间点到组
    const addTimePointToGroup = (group) => {
      group.settings.timePoints.push({
        time: Math.min(300, videoDuration.value),
        questions: [] // 添加空的题目数组
      })
    }

    // 为用户添加时间点
    const addTimePointToUser = (user) => {
      user.questionSettings.timePoints.push({
        time: Math.min(300, videoDuration.value),
        questions: [] // 添加空的题目数组
      })
    }


    // 从组中移除时间点
    const removeTimePointFromGroup = (group, index) => {
      if (group.settings.timePoints.length > 1) {
        group.settings.timePoints.splice(index, 1)
      } else {
        ElMessage.warning('至少需要保留一个时间点')
      }
    }

    // 应用组设置到所有用户
    const applySettingsToRoleGroup = (group) => {
      group.users.forEach(user => {
        // 深拷贝时间点数组
        user.questionSettings.timePoints = group.settings.timePoints.map(tp => ({ time: tp.time }))
      })
      ElMessage.success(`已将设置应用到 ${group.roleName} 组的所有用户`)
    }



    // 从用户中移除时间点
    const removeTimePointFromUser = (user, index) => {
      if (user.questionSettings.timePoints.length > 1) {
        user.questionSettings.timePoints.splice(index, 1)
      } else {
        ElMessage.warning('至少需要保留一个时间点')
      }
    }

    // 修改保存设置的方法
    const saveQuestionSettings = async () => {
      try {
        // 构造保存数据
        const payload = {
          courseId: selectedCourse.value.id,
          roleSettings: roleGroupedUsers.value.map(group => ({
            roleId: group.roleId,
            settings: {
              timePoints: group.settings.timePoints
            }
          })),
          userSettings: roleGroupedUsers.value.flatMap(group =>
              group.users.map(user => ({
                userId: user.id,
                roleId: group.roleId,
                timePoints: user.questionSettings.timePoints
              }))
          )
        }

        await api.post(`/courses/${selectedCourse.value.id}/question-settings`, payload)
        ElMessage.success('弹题设置保存成功')
        questionDialogVisible.value = false
      } catch (error) {
        console.error('保存弹题设置失败', error)
        ElMessage.error('保存弹题设置失败: ' + (error.response?.data?.message || error.message))
      }
    }
    // 添加题目选择相关变量
    const questionSelectDialogVisible = ref(false)
    const currentTimePoint = ref(null)
    const currentUserId = ref(null)

    // 题库相关
    const questionBanks = ref([])
    const questionBankLoading = ref(false)
    const questionBankCurrentPage = ref(1)
    const questionBankPageSize = ref(10)
    const questionBankTotal = ref(0)
    const questionBankSearchKeyword = ref('')
    const selectedQuestionBank = ref(null)

    // 题目相关
    const questions = ref([])
    const questionLoading = ref(false)
    const questionCurrentPage = ref(1)
    const questionPageSize = ref(20)
    const questionTotal = ref(0)
    const questionSearchKeyword = ref('')
    const selectedQuestions = ref([])
    const questionTableRef = ref(null)

    // 添加题目选择对话框打开方法
    const openQuestionSelectDialog = (timePoint, userId = null) => {
      currentTimePoint.value = timePoint
      currentUserId.value = userId
      questionSelectDialogVisible.value = true

      // 重置状态
      selectedQuestionBank.value = null
      questions.value = []
      selectedQuestions.value = []

      // 加载题库
      loadQuestionBanks()
    }

    // 加载题库列表
    const loadQuestionBanks = async () => {
      questionBankLoading.value = true
      try {
        const params = {
          page: questionBankCurrentPage.value - 1,
          size: questionBankPageSize.value,
          keyword: questionBankSearchKeyword.value
        }
        const response = await api.get('/question-banks/admin/page', {params})
        if (response.data.success) {
          questionBanks.value = response.data.data.content
          questionBankTotal.value = response.data.data.totalElements
        }
      } catch (error) {
        console.error('Load question banks error:', error)
        ElMessage.error('加载题库失败')
      } finally {
        questionBankLoading.value = false
      }
    }

    // 题库搜索
    const handleQuestionBankSearch = () => {
      questionBankCurrentPage.value = 1
      loadQuestionBanks()
    }

    // 题库分页
    const handleQuestionBankSizeChange = (size) => {
      questionBankPageSize.value = size
      questionBankCurrentPage.value = 1
      loadQuestionBanks()
    }

    const handleQuestionBankCurrentChange = (page) => {
      questionBankCurrentPage.value = page
      loadQuestionBanks()
    }

    // 选择题库
    const handleQuestionBankSelect = async (questionBank) => {
      if (!questionBank) return

      selectedQuestionBank.value = questionBank
      questionCurrentPage.value = 1
      await loadQuestions()
    }

    // 加载题目列表
    const loadQuestions = async () => {
      if (!selectedQuestionBank.value) return

      questionLoading.value = true
      try {
        const params = {
          page: questionCurrentPage.value - 1,
          size: questionPageSize.value,
          questionBankId: selectedQuestionBank.value.id,
          keyword: questionSearchKeyword.value
        }
        const response = await api.get('/questions', {params})
        if (response.data.success) {
          questions.value = response.data.data.content
          questionTotal.value = response.data.data.totalElements

          // 清除之前的选择
          if (questionTableRef.value) {
            questionTableRef.value.clearSelection()
          }
        }
      } catch (error) {
        console.error('Load questions error:', error)
        ElMessage.error('加载题目失败')
      } finally {
        questionLoading.value = false
      }
    }

    // 题目搜索
    const handleQuestionSearch = () => {
      questionCurrentPage.value = 1
      loadQuestions()
    }

    // 题目分页
    const handleQuestionSizeChange = (size) => {
      questionPageSize.value = size
      questionCurrentPage.value = 1
      loadQuestions()
    }

    const handleQuestionCurrentChange = (page) => {
      questionCurrentPage.value = page
      loadQuestions()
    }

    // 题目选择变化
    const handleQuestionSelectionChange = (selection) => {
      selectedQuestions.value = selection
    }

    // 确认题目选择
    const confirmQuestionSelection = () => {
      if (selectedQuestions.value.length === 0) {
        ElMessage.warning('请至少选择一道题目')
        return
      }

      // 保存题目选择到当前时间点
      currentTimePoint.value.questions = selectedQuestions.value.map(q => ({
        id: q.id,
        content: q.content
      }))

      questionSelectDialogVisible.value = false
      ElMessage.success(`已选择 ${selectedQuestions.value.length} 道题目`)
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
      const tagMap = {
        'SINGLE_CHOICE': 'primary',
        'MULTIPLE_CHOICE': 'success',
        'TRUE_FALSE': 'warning',
        'FILL_BLANK': 'info',
        'SHORT_ANSWER': 'danger'
      }
      return tagMap[type] || 'info'
    }

    return {
      courses,
      loading,
      dialogVisible,
      isEdit,
      courseForm,
      courseFormRef,
      rules,
      loadCourses,
      showCreateDialog,
      editCourse,
      submitCourse,
      deleteCourse,
      viewCourse,
      manageChapters,
      searchKeyword,
      currentPage,
      pageSize,
      total,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      roleCategories,
      formatDateTime,
      checkTimeRange,
      question,
      questionDialogVisible,
      activeRoleGroups,
      userLoading,
      selectedCourse,
      roleGroupedUsers,
      applySettingsToRoleGroup,
      saveQuestionSettings,
      videoDuration,
      formatVideoTime,
      addTimePointToGroup,
      removeTimePointFromGroup,
      addTimePointToUser,
      removeTimePointFromUser,
      questionSelectDialogVisible,
      currentTimePoint,
      currentUserId,
      questionBanks,
      questionBankLoading,
      questionBankCurrentPage,
      questionBankPageSize,
      questionBankTotal,
      questionBankSearchKeyword,
      selectedQuestionBank,
      questions,
      questionLoading,
      questionCurrentPage,
      questionPageSize,
      questionTotal,
      questionSearchKeyword,
      selectedQuestions,
      questionTableRef,
      openQuestionSelectDialog,
      loadQuestionBanks,
      handleQuestionBankSearch,
      handleQuestionBankSizeChange,
      handleQuestionBankCurrentChange,
      handleQuestionBankSelect,
      loadQuestions,
      handleQuestionSearch,
      handleQuestionSizeChange,
      handleQuestionCurrentChange,
      handleQuestionSelectionChange,
      confirmQuestionSelection,
      getQuestionTypeText,
      getQuestionTypeTag
    }
  }
}
</script>
<style scoped>.video-info {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.time-points-container {
  margin-top: 10px;
}

.time-point-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.user-time-points {
  padding: 5px 0;
}
.question-select-container {
  display: flex;
  height: 600px;
  gap: 20px;
}

.question-bank-list,
.question-list {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.question-bank-list {
  flex: 1;
}

.question-list {
  flex: 2;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.section-header h4 {
  margin: 0;
  color: #303133;
}

.search-input {
  width: 200px;
}

.bank-list-container,
.question-list-container {
  flex: 1;
  overflow: auto;
}

.question-select-dialog :deep(.el-dialog__body) {
  padding: 10px 20px;
}

.question-select-dialog :deep(.el-table) {
  height: calc(100% - 60px);
}

.question-select-dialog :deep(.el-table__body-wrapper) {
  height: calc(100% - 45px);
  overflow-y: auto;
}

.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

</style>
