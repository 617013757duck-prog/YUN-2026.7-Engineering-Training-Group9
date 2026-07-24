<template>
  <div class="follow-up-container page-container">
    <div class="page-header">
      <div class="header-left">
        <h1>慢病随访管理</h1>
        <p class="page-desc">管理慢病患者随访计划，跟踪随访任务完成情况</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建随访计划
        </el-button>
      </div>
    </div>
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.pending }}</p>
          <p class="stat-label">待执行任务</p>
          <p class="stat-trend">
            <el-icon><ArrowUp v-if="trends.pending > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(trends.pending) }}% 较上周
          </p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon completed">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.completed }}</p>
          <p class="stat-label">已完成任务</p>
          <p class="stat-trend">
            <el-icon><ArrowUp v-if="trends.completed > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(trends.completed) }}% 较上周
          </p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon overdue">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.overdue }}</p>
          <p class="stat-label">已逾期任务</p>
          <p class="stat-trend">
            <el-icon><ArrowUp v-if="trends.overdue > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(trends.overdue) }}% 较上周
          </p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon plan">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.plans }}</p>
          <p class="stat-label">随访计划数</p>
          <p class="stat-trend">
            <el-icon><ArrowUp v-if="trends.plans > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(trends.plans) }}% 较上周
          </p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon patient">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.patients }}</p>
          <p class="stat-label">管理患者数</p>
          <p class="stat-trend">
            <el-icon><ArrowUp v-if="trends.patients > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(trends.patients) }}% 较上周
          </p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon rate">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.completionRate }}%</p>
          <p class="stat-label">任务完成率</p>
          <p class="stat-trend">
            <el-icon><ArrowUp v-if="trends.completionRate > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(trends.completionRate) }}% 较上周
          </p>
        </div>
      </div>
    </div>
    <div class="tabs-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="随访任务" name="tasks">
          <div class="filter-bar">
            <el-form :model="taskFilters" inline>
              <el-form-item label="状态">
                <el-select v-model="taskFilters.status" placeholder="全部" clearable>
                  <el-option label="全部" value="" />
                  <el-option label="待执行" value="pending" />
                  <el-option label="已完成" value="completed" />
                  <el-option label="已逾期" value="overdue" />
                </el-select>
              </el-form-item>
              <el-form-item label="患者姓名">
                <el-input v-model="taskFilters.patientName" placeholder="请输入姓名" clearable />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchTasks">搜索</el-button>
                <el-button @click="resetTaskFilters">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="table-card">
            <el-table :data="filteredTaskList" border v-loading="taskLoading">
              <el-table-column prop="id" label="任务编号" width="140" />
              <el-table-column prop="patientName" label="患者姓名" width="100" />
              <el-table-column prop="planName" label="随访计划" />
              <el-table-column prop="planType" label="计划类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small">{{ getPlanTypeLabel(row.planType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="nextTime" label="下次随访" width="160" />
              <el-table-column prop="lastTime" label="上次随访" width="160" />
              <el-table-column label="操作" width="180">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="viewTask(row)">详情</el-button>
                  <el-button v-if="row.status === 'pending'" type="success" size="small" @click="completeTask(row)">完成</el-button>
                  <el-button type="warning" size="small" @click="editTask(row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-bar">
              <el-pagination
                @size-change="handleTaskSizeChange"
                @current-change="handleTaskCurrentChange"
                :current-page="taskPagination.page"
                :page-sizes="[10, 20, 50]"
                :page-size="taskPagination.size"
                layout="total, sizes, prev, pager, next, jumper"
                :total="taskPagination.total"
              />
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="随访计划" name="plans">
          <div class="filter-bar">
            <el-form :model="planFilters" inline>
              <el-form-item label="计划名称">
                <el-input v-model="planFilters.name" placeholder="请输入计划名称" clearable />
              </el-form-item>
              <el-form-item label="计划类型">
                <el-select v-model="planFilters.type" placeholder="全部" clearable>
                  <el-option label="全部" value="" />
                  <el-option label="高血压" value="hypertension" />
                  <el-option label="糖尿病" value="diabetes" />
                  <el-option label="冠心病" value="chd" />
                  <el-option label="慢阻肺" value="copd" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchPlans">搜索</el-button>
                <el-button @click="resetPlanFilters">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div class="table-card">
            <el-table :data="filteredPlanList" border v-loading="planLoading">
              <el-table-column prop="id" label="计划编号" width="140" />
              <el-table-column prop="name" label="计划名称" />
              <el-table-column prop="type" label="计划类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small">{{ getPlanTypeLabel(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="frequency" label="随访频率" width="120" />
              <el-table-column prop="duration" label="计划时长" width="100" />
              <el-table-column prop="patientCount" label="关联患者" width="100" />
              <el-table-column prop="createTime" label="创建时间" width="160" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="viewPlan(row)">详情</el-button>
                  <el-button type="warning" size="small" @click="editPlan(row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="日历视图" name="calendar">
          <div class="calendar-card">
            <div class="calendar-header">
              <el-button @click="prevMonth">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <span class="calendar-title">{{ currentYear }}年{{ currentMonth }}月</span>
              <el-button @click="nextMonth">
                <el-icon><ArrowRight /></el-icon>
              </el-button>
              <el-button @click="goToday" type="text">今天</el-button>
            </div>
            <div class="calendar-body">
              <div class="calendar-weekdays">
                <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
              </div>
              <div class="calendar-days">
                <div
                  v-for="(day, index) in calendarDays"
                  :key="index"
                  class="calendar-day"
                  :class="{
                    'other-month': !day.currentMonth,
                    'today': day.isToday,
                    'has-task': day.taskCount > 0,
                    'selected': isSelectedDate(day)
                  }"
                  @click="selectDate(day)"
                >
                  <span class="day-number">{{ day.date }}</span>
                  <span v-if="day.taskCount > 0" class="task-dot"></span>
                </div>
              </div>
            </div>
            <div v-if="selectedDate" class="calendar-detail">
              <h3>{{ selectedDate.year }}年{{ selectedDate.month }}月{{ selectedDate.date }}日</h3>
              <div v-if="selectedDayTasks.length === 0" class="empty-state">
                <el-icon><Calendar /></el-icon>
                <p>当天没有随访任务</p>
              </div>
              <div v-else class="task-list">
                <div
                  v-for="task in selectedDayTasks"
                  :key="task.id"
                  class="task-item"
                  :class="task.status"
                >
                  <div class="task-time">{{ formatTime(task.nextTime) }}</div>
                  <div class="task-info">
                    <span class="task-patient">{{ task.patientName }}</span>
                    <span class="task-plan">{{ task.planName }}</span>
                  </div>
                  <el-tag :type="getStatusTagType(task.status)" size="small">{{ getStatusLabel(task.status) }}</el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <el-dialog v-model="showCreateDialog" title="创建随访计划" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="计划名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="计划类型" prop="type">
          <el-select v-model="createForm.type" placeholder="请选择计划类型">
            <el-option label="高血压" value="hypertension" />
            <el-option label="糖尿病" value="diabetes" />
            <el-option label="冠心病" value="chd" />
            <el-option label="慢阻肺" value="copd" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="随访频率" prop="frequency">
          <el-select v-model="createForm.frequency" placeholder="请选择随访频率">
            <el-option label="每周一次" value="weekly" />
            <el-option label="每月一次" value="monthly" />
            <el-option label="每季度一次" value="quarterly" />
            <el-option label="每半年一次" value="halfYearly" />
            <el-option label="每年一次" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划时长" prop="duration">
          <el-select v-model="createForm.duration" placeholder="请选择计划时长">
            <el-option label="6个月" value="6m" />
            <el-option label="1年" value="1y" />
            <el-option label="2年" value="2y" />
            <el-option label="长期" value="long" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入计划描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Clock, CircleCheckFilled, Warning, Calendar, User, TrendCharts, ArrowUp, ArrowDown, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const activeTab = ref('tasks')
const taskLoading = ref(false)
const planLoading = ref(false)
const showCreateDialog = ref(false)
const createFormRef = ref(null)

const stats = reactive({
  pending: 23,
  completed: 156,
  overdue: 5,
  plans: 12,
  patients: 256,
  completionRate: 87
})

const trends = reactive({
  pending: -12,
  completed: 8,
  overdue: -5,
  plans: 5,
  patients: 3,
  completionRate: 2
})

const taskFilters = reactive({ status: '', patientName: '' })
const planFilters = reactive({ name: '', type: '' })

const taskPagination = reactive({
  page: 1,
  size: 10,
  total: 6
})

const taskList = [
  { id: 'FU-20260722001', patientName: '张三', planName: '高血压随访计划', planType: 'hypertension', status: 'pending', nextTime: '2026-07-23 09:00', lastTime: '2026-06-23 09:00' },
  { id: 'FU-20260722002', patientName: '李四', planName: '糖尿病随访计划', planType: 'diabetes', status: 'pending', nextTime: '2026-07-23 10:00', lastTime: '2026-06-23 10:00' },
  { id: 'FU-20260722003', patientName: '王五', planName: '冠心病随访计划', planType: 'chd', status: 'completed', nextTime: '2026-07-24 14:00', lastTime: '2026-07-22 14:00' },
  { id: 'FU-20260722004', patientName: '赵六', planName: '慢阻肺随访计划', planType: 'copd', status: 'pending', nextTime: '2026-07-23 15:00', lastTime: '2026-06-23 15:00' },
  { id: 'FU-20260722005', patientName: '钱七', planName: '高血压随访计划', planType: 'hypertension', status: 'overdue', nextTime: '2026-07-21 09:00', lastTime: '2026-06-21 09:00' },
  { id: 'FU-20260722006', patientName: '孙八', planName: '糖尿病随访计划', planType: 'diabetes', status: 'completed', nextTime: '2026-07-25 11:00', lastTime: '2026-07-22 11:00' }
]

const planList = [
  { id: 'PLAN-001', name: '高血压随访计划', type: 'hypertension', frequency: '每月一次', duration: '1年', patientCount: 45, createTime: '2026-01-15 10:00' },
  { id: 'PLAN-002', name: '糖尿病随访计划', type: 'diabetes', frequency: '每月一次', duration: '1年', patientCount: 38, createTime: '2026-01-15 10:00' },
  { id: 'PLAN-003', name: '冠心病随访计划', type: 'chd', frequency: '每季度一次', duration: '2年', patientCount: 22, createTime: '2026-02-20 14:00' },
  { id: 'PLAN-004', name: '慢阻肺随访计划', type: 'copd', frequency: '每半年一次', duration: '长期', patientCount: 15, createTime: '2026-03-10 09:00' }
]

const createForm = reactive({
  name: '',
  type: '',
  frequency: '',
  duration: '',
  description: ''
})

const createRules = {
  name: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  frequency: [{ required: true, message: '请选择随访频率', trigger: 'change' }],
  duration: [{ required: true, message: '请选择计划时长', trigger: 'change' }]
}

const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const currentDate = new Date()
const currentYear = ref(currentDate.getFullYear())
const currentMonth = ref(currentDate.getMonth() + 1)
const selectedDate = ref(null)

const filteredTaskList = computed(() => {
  let list = taskList.filter(item => {
    if (taskFilters.status && item.status !== taskFilters.status) return false
    if (taskFilters.patientName && !item.patientName.includes(taskFilters.patientName)) return false
    return true
  })
  taskPagination.total = list.length
  const start = (taskPagination.page - 1) * taskPagination.size
  return list.slice(start, start + taskPagination.size)
})

const filteredPlanList = computed(() => {
  return planList.filter(item => {
    if (planFilters.name && !item.name.includes(planFilters.name)) return false
    if (planFilters.type && item.type !== planFilters.type) return false
    return true
  })
})

const calendarDays = computed(() => {
  const days = []
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1)
  const lastDay = new Date(currentYear.value, currentMonth.value, 0)
  const today = new Date()
  
  const startDay = firstDay.getDay()
  const prevMonthLastDay = new Date(currentYear.value, currentMonth.value - 1, 0).getDate()
  
  for (let i = startDay - 1; i >= 0; i--) {
    const date = prevMonthLastDay - i
    const month = currentMonth.value - 1 || 12
    const year = currentMonth.value === 1 ? currentYear.value - 1 : currentYear.value
    days.push({
      date,
      month,
      year,
      currentMonth: false,
      isToday: false,
      taskCount: getTaskCountForDate(year, month, date)
    })
  }
  
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const isToday = today.getFullYear() === currentYear.value &&
                   today.getMonth() + 1 === currentMonth.value &&
                   today.getDate() === i
    days.push({
      date: i,
      month: currentMonth.value,
      year: currentYear.value,
      currentMonth: true,
      isToday,
      taskCount: getTaskCountForDate(currentYear.value, currentMonth.value, i)
    })
  }
  
  const remainingDays = 42 - days.length
  for (let i = 1; i <= remainingDays; i++) {
    const month = currentMonth.value + 1 > 12 ? 1 : currentMonth.value + 1
    const year = currentMonth.value === 12 ? currentYear.value + 1 : currentYear.value
    days.push({
      date: i,
      month,
      year,
      currentMonth: false,
      isToday: false,
      taskCount: getTaskCountForDate(year, month, i)
    })
  }
  
  return days
})

const selectedDayTasks = computed(() => {
  if (!selectedDate.value) return []
  return taskList.filter(task => {
    const taskDate = new Date(task.nextTime)
    return taskDate.getFullYear() === selectedDate.value.year &&
           taskDate.getMonth() + 1 === selectedDate.value.month &&
           taskDate.getDate() === selectedDate.value.date
  })
})

const getTaskCountForDate = (year, month, date) => {
  return taskList.filter(task => {
    const taskDate = new Date(task.nextTime)
    return taskDate.getFullYear() === year &&
           taskDate.getMonth() + 1 === month &&
           taskDate.getDate() === date
  }).length
}

const isSelectedDate = (day) => {
  if (!selectedDate.value) return false
  return selectedDate.value.year === day.year &&
         selectedDate.value.month === day.month &&
         selectedDate.value.date === day.date
}

const selectDate = (day) => {
  selectedDate.value = { year: day.year, month: day.month, date: day.date }
}

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
}

const goToday = () => {
  const today = new Date()
  currentYear.value = today.getFullYear()
  currentMonth.value = today.getMonth() + 1
  selectedDate.value = {
    year: today.getFullYear(),
    month: today.getMonth() + 1,
    date: today.getDate()
  }
}

const formatTime = (datetime) => {
  return datetime.split(' ')[1] || ''
}

const getPlanTypeLabel = (type) => {
  const labels = { hypertension: '高血压', diabetes: '糖尿病', chd: '冠心病', copd: '慢阻肺', other: '其他' }
  return labels[type] || type
}

const getStatusTagType = (status) => {
  const types = { pending: 'warning', completed: 'success', overdue: 'danger' }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = { pending: '待执行', completed: '已完成', overdue: '已逾期' }
  return labels[status] || status
}

const handleTabChange = (tabName) => {
  activeTab.value = tabName
}

const completeTask = async (row) => {
  row.status = 'completed'
  stats.pending--
  stats.completed++
  ElMessage.success('任务已完成')
}

const viewTask = (row) => {
  ElMessage.info(`查看任务: ${row.id}`)
}

const editTask = (row) => {
  ElMessage.info(`编辑任务: ${row.id}`)
}

const viewPlan = (row) => {
  ElMessage.info(`查看计划: ${row.id}`)
}

const editPlan = (row) => {
  ElMessage.info(`编辑计划: ${row.id}`)
}

const searchTasks = () => {
  taskPagination.page = 1
}

const resetTaskFilters = () => {
  taskFilters.status = ''
  taskFilters.patientName = ''
  taskPagination.page = 1
}

const searchPlans = () => {}

const resetPlanFilters = () => {
  planFilters.name = ''
  planFilters.type = ''
}

const handleTaskSizeChange = (size) => {
  taskPagination.size = size
}

const handleTaskCurrentChange = (page) => {
  taskPagination.page = page
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      showCreateDialog.value = false
      stats.plans++
      ElMessage.success('随访计划创建成功')
      createForm.name = ''
      createForm.type = ''
      createForm.frequency = ''
      createForm.duration = ''
      createForm.description = ''
    }
  })
}

onMounted(() => {
  goToday()
})
</script>

<style lang="scss" scoped>
.follow-up-container {
  padding-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;

  .header-left {
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
    }
    .page-desc {
      font-size: 14px;
      color: #909399;
    }
  }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-3px) scale(1.01);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;

  &.pending { background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%); }
  &.completed { background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%); }
  &.overdue { background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%); }
  &.plan { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); }
  &.patient { background: linear-gradient(135deg, #909399 0%, #b4b7bb 100%); }
  &.rate { background: linear-gradient(135deg, #c0c4cc 0%, #d9dde3 100%); }
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;

  :deep(.el-icon) {
    font-size: 12px;
  }

  &:has(:deep(.el-icon-arrow-up)) {
    color: #67c23a;
  }

  &:has(:deep(.el-icon-arrow-down)) {
    color: #f56c6c;
  }
}

.tabs-container {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.filter-bar {
  margin-bottom: 20px;
}

.table-card {
  :deep(.el-table) {
    font-size: 14px;
  }
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.calendar-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 24px;

  .calendar-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  :deep(.el-button) {
    padding: 8px;
  }
}

.calendar-body {
  margin-bottom: 24px;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 8px;

  .weekday {
    text-align: center;
    font-size: 14px;
    color: #909399;
    padding: 8px 0;
    font-weight: 500;
  }
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: #f5f7fa;
  }

  &.other-month {
    color: #c0c4cc;
  }

  &.today {
    background: #ecf5ff;

    .day-number {
      color: #409eff;
      font-weight: 600;
    }
  }

  &.has-task {
    background: rgba(64, 158, 255, 0.08);
  }

  &.selected {
    background: #409eff;
    color: #fff;

    .day-number {
      color: #fff;
    }

    .task-dot {
      background: #fff;
    }
  }

  .day-number {
    font-size: 14px;
    color: #303133;
  }

  .task-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #409eff;
    margin-top: 4px;
  }
}

.calendar-detail {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #909399;

  :deep(.el-icon) {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    font-size: 14px;
  }
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #f5f7fa;
  transition: all 0.2s;

  &:hover {
    background: #ecf5ff;
  }

  &.pending {
    border-left: 4px solid #e6a23c;
  }

  &.completed {
    border-left: 4px solid #67c23a;
  }

  &.overdue {
    border-left: 4px solid #f56c6c;
    background: rgba(245, 108, 108, 0.05);
  }

  .task-time {
    font-size: 14px;
    color: #409eff;
    font-weight: 500;
    width: 80px;
  }

  .task-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .task-patient {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }

    .task-plan {
      font-size: 12px;
      color: #909399;
    }
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-bar {
    :deep(.el-form-item) {
      width: 100%;
    }

    :deep(.el-select),
    :deep(.el-input) {
      width: 100% !important;
    }
  }
}

@media (min-width: 769px) and (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>