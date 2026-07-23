<template>
  <div class="follow-up-container">
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
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon completed">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.completed }}</p>
          <p class="stat-label">已完成任务</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon overdue">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.overdue }}</p>
          <p class="stat-label">已逾期任务</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon plan">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.plans }}</p>
          <p class="stat-label">随访计划数</p>
        </div>
      </div>
    </div>
    <div class="tabs-container">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="随访任务" name="tasks">
          <div class="filter-bar">
            <el-form :model="taskFilters" inline>
              <el-form-item label="状态">
                <el-select v-model="taskFilters.status" placeholder="全部">
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
            <el-table :data="taskList" border v-loading="loading">
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
                  <el-button v-if="row.status === 'pending'" type="success" size="small" @click="completeTask(row.id)">完成</el-button>
                  <el-button type="warning" size="small" @click="editTask(row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="随访计划" name="plans">
          <div class="filter-bar">
            <el-form :model="planFilters" inline>
              <el-form-item label="计划名称">
                <el-input v-model="planFilters.name" placeholder="请输入计划名称" clearable />
              </el-form-item>
              <el-form-item label="计划类型">
                <el-select v-model="planFilters.type" placeholder="全部">
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
            <el-table :data="planList" border v-loading="loading">
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Clock, CircleCheckFilled, Warning, Calendar } from '@element-plus/icons-vue'

const activeTab = ref('tasks')
const loading = ref(false)
const showCreateDialog = ref(false)
const createFormRef = ref(null)

const stats = reactive({
  pending: 23,
  completed: 156,
  overdue: 5,
  plans: 12
})

const taskFilters = reactive({ status: '', patientName: '' })
const planFilters = reactive({ name: '', type: '' })

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

const completeTask = async (id) => {
  const task = taskList.find(t => t.id === id)
  if (task) {
    task.status = 'completed'
    ElMessage.success('任务已完成')
  }
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

const searchTasks = () => {}
const resetTaskFilters = () => { taskFilters.status = ''; taskFilters.patientName = '' }
const searchPlans = () => {}
const resetPlanFilters = () => { planFilters.name = ''; planFilters.type = '' }

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      showCreateDialog.value = false
      ElMessage.success('随访计划创建成功')
      createForm.name = ''
      createForm.type = ''
      createForm.frequency = ''
      createForm.duration = ''
      createForm.description = ''
    }
  })
}
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
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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
}

.tabs-container {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-bar {
  margin-bottom: 20px;
}

.table-card {
  :deep(.el-table) {
    font-size: 14px;
  }
}
</style>