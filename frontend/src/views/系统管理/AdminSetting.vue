<template>
  <div class="admin-setting-container">
    <div class="page-header">
      <h1>系统配置管理</h1>
      <p class="page-desc">管理系统参数、用户权限、指南库和模型版本</p>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="系统参数" name="system">
        <div class="setting-card">
          <div class="card-header">
            <h3>系统参数设置</h3>
            <el-button type="primary" @click="saveSystemSettings">保存设置</el-button>
          </div>
          <el-form :model="systemSettings" label-width="150px">
            <el-form-item label="系统名称">
              <el-input v-model="systemSettings.systemName" />
            </el-form-item>
            <el-form-item label="系统版本">
              <el-input v-model="systemSettings.version" disabled />
            </el-form-item>
            <el-form-item label="AI服务地址">
              <el-input v-model="systemSettings.aiServiceUrl" />
            </el-form-item>
            <el-form-item label="AI服务超时(秒)">
              <el-input-number v-model="systemSettings.aiServiceTimeout" :min="10" :max="300" />
            </el-form-item>
            <el-form-item label="自动审核阈值">
              <el-input-number v-model="systemSettings.autoReviewThreshold" :min="0" :max="100" />
              <span class="form-tip">低于此分数的病例自动进入人工审核</span>
            </el-form-item>
            <el-form-item label="高危告警阈值">
              <el-input-number v-model="systemSettings.highRiskThreshold" :min="0" :max="100" />
              <span class="form-tip">高于此分数标记为高风险</span>
            </el-form-item>
            <el-form-item label="随访提醒提前(天)">
              <el-input-number v-model="systemSettings.followUpRemindDays" :min="1" :max="7" />
            </el-form-item>
            <el-form-item label="日志保留天数">
              <el-input-number v-model="systemSettings.logRetentionDays" :min="7" :max="365" />
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      <el-tab-pane label="用户管理" name="users">
        <div class="setting-card">
          <div class="card-header">
            <h3>用户列表</h3>
            <div class="card-actions">
              <el-input v-model="userSearch" placeholder="搜索用户名" clearable style="width: 200px" />
              <el-button type="primary" @click="showAddUser = true">添加用户</el-button>
            </div>
          </div>
          <el-table :data="userList" border>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="真实姓名" width="120" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="getRoleTagType(row.role)" size="small">{{ getRoleLabel(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-switch v-model="row.status" @change="toggleUserStatus(row)" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="editUser(row)">编辑</el-button>
                <el-button type="warning" size="small" @click="resetPassword(row)">重置密码</el-button>
                <el-button type="danger" size="small" @click="deleteUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="指南管理" name="guides">
        <div class="setting-card">
          <div class="card-header">
            <h3>指南库管理</h3>
            <div class="card-actions">
              <el-input v-model="guideSearch" placeholder="搜索指南名称" clearable style="width: 200px" />
              <el-button type="primary" @click="showImportGuide = true">导入指南</el-button>
            </div>
          </div>
          <el-table :data="guideList" border>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="title" label="指南名称" />
            <el-table-column prop="source" label="来源" width="120" />
            <el-table-column prop="publishDate" label="发布日期" width="120" />
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">{{ row.status === 'active' ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewGuide(row)">查看</el-button>
                <el-button type="warning" size="small" @click="editGuide(row)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteGuide(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="模型管理" name="models">
        <div class="setting-card">
          <div class="card-header">
            <h3>AI模型管理</h3>
          </div>
          <div class="model-grid">
            <div v-for="model in modelList" :key="model.id" class="model-card" :class="{ active: model.isActive }">
              <div class="model-header">
                <h4>{{ model.name }}</h4>
                <el-tag v-if="model.isActive" type="success">当前使用</el-tag>
              </div>
              <div class="model-info">
                <p><span class="info-label">版本：</span>{{ model.version }}</p>
                <p><span class="info-label">类型：</span>{{ model.type }}</p>
                <p><span class="info-label">部署时间：</span>{{ model.deployTime }}</p>
                <p><span class="info-label">状态：</span>
                  <el-tag :type="model.status === 'online' ? 'success' : 'danger'" size="small">{{ model.status === 'online' ? '在线' : '离线' }}</el-tag>
                </p>
              </div>
              <div class="model-metrics">
                <div class="metric">
                  <span class="metric-value">{{ model.accuracy }}%</span>
                  <span class="metric-label">准确率</span>
                </div>
                <div class="metric">
                  <span class="metric-value">{{ model.latency }}ms</span>
                  <span class="metric-label">延迟</span>
                </div>
              </div>
              <div class="model-actions">
                <el-button v-if="!model.isActive" type="primary" size="small" @click="activateModel(model)">设为默认</el-button>
                <el-button type="warning" size="small" @click="testModel(model)">测试</el-button>
                <el-button type="danger" size="small" @click="deleteModel(model)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="审计日志" name="logs">
        <div class="setting-card">
          <div class="card-header">
            <h3>系统审计日志</h3>
            <el-form :model="logFilters" inline>
              <el-form-item label="操作类型">
                <el-select v-model="logFilters.type" placeholder="全部">
                  <el-option label="全部" value="" />
                  <el-option label="登录" value="login" />
                  <el-option label="审核" value="review" />
                  <el-option label="修改" value="update" />
                  <el-option label="删除" value="delete" />
                </el-select>
              </el-form-item>
              <el-form-item label="日期范围">
                <el-date-picker v-model="logFilters.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
              </el-form-item>
              <el-button type="primary" @click="searchLogs">搜索</el-button>
            </el-form>
          </div>
          <el-table :data="auditLogs" border>
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="user" label="操作人" width="120" />
            <el-table-column prop="type" label="操作类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ getLogTypeLabel(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="action" label="操作内容" />
            <el-table-column prop="ip" label="IP地址" width="140" />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('system')
const userSearch = ref('')
const guideSearch = ref('')
const showAddUser = ref(false)
const showImportGuide = ref(false)

const systemSettings = reactive({
  systemName: '基层医疗安全型预问诊与随访平台',
  version: '1.0.0',
  aiServiceUrl: 'http://localhost:8000',
  aiServiceTimeout: 30,
  autoReviewThreshold: 80,
  highRiskThreshold: 70,
  followUpRemindDays: 3,
  logRetentionDays: 90
})

const logFilters = reactive({ type: '', dateRange: null })

const userList = [
  { id: 1, username: 'admin', realName: '管理员', role: 'admin', status: true, createTime: '2026-01-15 10:00' },
  { id: 2, username: 'doctor', realName: '张医生', role: 'doctor', status: true, createTime: '2026-01-20 14:00' },
  { id: 3, username: 'nurse', realName: '李护士', role: 'follow', status: true, createTime: '2026-02-10 09:00' },
  { id: 4, username: 'patient', realName: '测试患者', role: 'patient', status: true, createTime: '2026-03-01 11:00' },
  { id: 5, username: 'testuser', realName: '测试用户', role: 'patient', status: false, createTime: '2026-04-15 16:00' }
]

const guideList = [
  { id: 1, title: '中国急性冠状动脉综合征诊断与治疗指南', source: '中华医学会', publishDate: '2023年', version: 'V1.0', status: 'active' },
  { id: 2, title: '高血压防治指南', source: '国家心血管病中心', publishDate: '2022年', version: 'V2.0', status: 'active' },
  { id: 3, title: '2型糖尿病防治指南', source: '中华医学会内分泌分会', publishDate: '2022年', version: 'V1.0', status: 'active' },
  { id: 4, title: '慢性阻塞性肺疾病诊治指南', source: '中华医学会呼吸分会', publishDate: '2021年', version: 'V1.0', status: 'active' }
]

const modelList = [
  { id: 1, name: 'ChatGLM3-6B', version: 'v1.0', type: '对话模型', deployTime: '2026-06-01', status: 'online', isActive: true, accuracy: 94.5, latency: 320 },
  { id: 2, name: 'Qwen-7B', version: 'v1.1', type: '对话模型', deployTime: '2026-07-15', status: 'online', isActive: false, accuracy: 95.2, latency: 280 },
  { id: 3, name: 'Medical-BERT', version: 'v2.0', type: '文本分类', deployTime: '2026-05-20', status: 'online', isActive: false, accuracy: 92.8, latency: 45 }
]

const auditLogs = [
  { time: '2026-07-22 14:30', user: 'admin', type: 'login', action: '管理员登录系统', ip: '192.168.1.100' },
  { time: '2026-07-22 14:25', user: 'doctor', type: 'review', action: '审核通过病例 CASE-20260722003', ip: '192.168.1.101' },
  { time: '2026-07-22 14:20', user: 'admin', type: 'update', action: '修改系统参数', ip: '192.168.1.100' },
  { time: '2026-07-22 14:15', user: 'nurse', type: 'update', action: '完成随访任务 FU-20260722003', ip: '192.168.1.102' },
  { time: '2026-07-22 14:10', user: 'patient', type: 'login', action: '患者登录系统', ip: '192.168.1.103' },
  { time: '2026-07-22 14:05', user: 'doctor', type: 'review', action: '审核拒绝病例 CASE-20260722007', ip: '192.168.1.101' }
]

const getRoleTagType = (role) => {
  const types = { admin: 'danger', doctor: 'primary', follow: 'warning', patient: 'info' }
  return types[role] || 'info'
}

const getRoleLabel = (role) => {
  const labels = { admin: '管理员', doctor: '医务人员', follow: '随访人员', patient: '患者' }
  return labels[role] || role
}

const getLogTypeLabel = (type) => {
  const labels = { login: '登录', review: '审核', update: '修改', delete: '删除' }
  return labels[type] || type
}

const saveSystemSettings = () => {
  ElMessage.success('系统参数保存成功')
}

const toggleUserStatus = (row) => {
  ElMessage.success(`${row.realName}状态已${row.status ? '启用' : '禁用'}`)
}

const editUser = (row) => {
  ElMessage.info(`编辑用户: ${row.username}`)
}

const resetPassword = (row) => {
  ElMessageBox.confirm(`确定重置${row.realName}的密码吗？`, '确认操作', { type: 'warning' })
    .then(() => ElMessage.success('密码重置成功'))
}

const deleteUser = (row) => {
  ElMessageBox.confirm(`确定删除用户${row.realName}吗？`, '确认操作', { type: 'warning' })
    .then(() => ElMessage.success('用户删除成功'))
}

const viewGuide = (row) => {
  ElMessage.info(`查看指南: ${row.title}`)
}

const editGuide = (row) => {
  ElMessage.info(`编辑指南: ${row.title}`)
}

const deleteGuide = (row) => {
  ElMessageBox.confirm(`确定删除指南${row.title}吗？`, '确认操作', { type: 'warning' })
    .then(() => ElMessage.success('指南删除成功'))
}

const activateModel = (model) => {
  modelList.forEach(m => m.isActive = false)
  model.isActive = true
  ElMessage.success(`${model.name}已设为默认模型`)
}

const testModel = (model) => {
  ElMessage.info(`测试模型: ${model.name}`)
}

const deleteModel = (model) => {
  if (model.isActive) {
    ElMessage.warning('不能删除当前使用的模型')
    return
  }
  ElMessageBox.confirm(`确定删除模型${model.name}吗？`, '确认操作', { type: 'warning' })
    .then(() => ElMessage.success('模型删除成功'))
}

const searchLogs = () => {}
</script>

<style lang="scss" scoped>
.admin-setting-container {
  padding-bottom: 20px;
}

.page-header {
  margin-bottom: 24px;

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

.setting-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.card-actions {
  display: flex;
  gap: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

:deep(.el-table) {
  font-size: 14px;
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.model-card {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 20px;
  border: 2px solid transparent;
  transition: all 0.3s;

  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }
}

.model-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.model-info {
  margin-bottom: 16px;

  p {
    font-size: 14px;
    color: #606266;
    margin-bottom: 4px;
  }
}

.info-label {
  color: #909399;
}

.model-metrics {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
}

.metric {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.metric-value {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.metric-label {
  font-size: 12px;
  color: #909399;
}

.model-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>