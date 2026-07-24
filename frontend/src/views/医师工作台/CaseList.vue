<template>
  <div class="case-list-container">
    <div class="page-header">
      <div class="header-left">
        <h1>病例待审队列</h1>
        <p class="page-desc">审核患者预问诊信息，查看风险提示和AI分析结果</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="refreshList">
          <el-icon><Refresh /></el-icon>
          刷新列表
        </el-button>
      </div>
    </div>
    <div class="filter-bar">
      <el-form :model="filters" inline>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部">
            <el-option label="全部" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="filters.riskLevel" placeholder="全部">
            <el-option label="全部" value="" />
            <el-option label="高风险" value="high" />
            <el-option label="中风险" value="medium" />
            <el-option label="低风险" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input v-model="filters.patientName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="filters.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-count pending">{{ stats.pending }}</span>
        <span class="stat-label">待审核</span>
      </div>
      <div class="stat-item">
        <span class="stat-count high">{{ stats.highRisk }}</span>
        <span class="stat-label">高风险</span>
      </div>
      <div class="stat-item">
        <span class="stat-count medium">{{ stats.mediumRisk }}</span>
        <span class="stat-label">中风险</span>
      </div>
      <div class="stat-item">
        <span class="stat-count low">{{ stats.lowRisk }}</span>
        <span class="stat-label">低风险</span>
      </div>
    </div>
    <div class="table-card">
      <el-table :data="caseList" border v-loading="loading">
        <el-table-column prop="id" label="病例编号" width="160" />
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">{{ row.gender === 'male' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="symptoms" label="主要症状" />
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)" size="small">{{ getRiskLabel(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="goToDetail(row.id)">详情</el-button>
            <el-button v-if="row.status === 'pending'" type="success" size="small" @click="approveCase(row.id)">通过</el-button>
            <el-button v-if="row.status === 'pending'" type="danger" size="small" @click="rejectCase(row.id)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)

const filters = reactive({
  status: '',
  riskLevel: '',
  patientName: '',
  dateRange: null
})

const stats = reactive({
  pending: 23,
  highRisk: 8,
  mediumRisk: 12,
  lowRisk: 5
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 45
})

const caseList = [
  { id: 'CASE-20260722001', patientName: '张三', gender: 'male', age: 65, symptoms: '胸闷、气短、心悸', riskLevel: 'high', status: 'pending', submitTime: '2026-07-22 14:30' },
  { id: 'CASE-20260722002', patientName: '李四', gender: 'female', age: 45, symptoms: '头痛、恶心、呕吐', riskLevel: 'medium', status: 'pending', submitTime: '2026-07-22 13:45' },
  { id: 'CASE-20260722003', patientName: '王五', gender: 'male', age: 32, symptoms: '发热、咳嗽、乏力', riskLevel: 'low', status: 'approved', submitTime: '2026-07-22 12:20' },
  { id: 'CASE-20260722004', patientName: '赵六', gender: 'female', age: 58, symptoms: '腹痛、腹泻、食欲不振', riskLevel: 'medium', status: 'pending', submitTime: '2026-07-22 11:15' },
  { id: 'CASE-20260722005', patientName: '钱七', gender: 'male', age: 72, symptoms: '胸痛、放射至左肩', riskLevel: 'high', status: 'pending', submitTime: '2026-07-22 10:00' },
  { id: 'CASE-20260722006', patientName: '孙八', gender: 'female', age: 28, symptoms: '头晕、乏力、面色苍白', riskLevel: 'low', status: 'approved', submitTime: '2026-07-22 09:30' },
  { id: 'CASE-20260722007', patientName: '周九', gender: 'male', age: 41, symptoms: '呼吸困难、喘息', riskLevel: 'high', status: 'rejected', submitTime: '2026-07-22 08:45' },
  { id: 'CASE-20260722008', patientName: '吴十', gender: 'female', age: 55, symptoms: '关节疼痛、肿胀', riskLevel: 'medium', status: 'pending', submitTime: '2026-07-22 08:00' }
]

const getRiskTagType = (level) => {
  const types = { high: 'danger', medium: 'warning', low: 'success' }
  return types[level] || 'info'
}

const getRiskLabel = (level) => {
  const labels = { high: '高风险', medium: '中风险', low: '低风险' }
  return labels[level] || level
}

const getStatusTagType = (status) => {
  const types = { pending: 'warning', approved: 'success', rejected: 'danger' }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
  return labels[status] || status
}

const goToDetail = (id) => router.push(`/caseDetail/${id}`)

const approveCase = async (id) => {
  await ElMessageBox.confirm('确定要通过此病例吗？', '确认操作', { type: 'warning' })
  const caseItem = caseList.find(c => c.id === id)
  if (caseItem) {
    caseItem.status = 'approved'
    ElMessage.success('病例已通过')
  }
}

const rejectCase = async (id) => {
  await ElMessageBox.confirm('确定要拒绝此病例吗？', '确认操作', { type: 'warning' })
  const caseItem = caseList.find(c => c.id === id)
  if (caseItem) {
    caseItem.status = 'rejected'
    ElMessage.success('病例已拒绝')
  }
}

const handleSearch = () => {
  pagination.page = 1
}

const resetFilters = () => {
  filters.status = ''
  filters.riskLevel = ''
  filters.patientName = ''
  filters.dateRange = null
  pagination.page = 1
}

const refreshList = () => {
  loading.value = true
  setTimeout(() => loading.value = false, 500)
}

const handleSizeChange = (size) => {
  pagination.size = size
}

const handleCurrentChange = (page) => {
  pagination.page = page
}
</script>

<style lang="scss" scoped>
.case-list-container {
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

.filter-bar {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stats-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-count {
  font-size: 28px;
  font-weight: 600;

  &.pending { color: #e6a23c; }
  &.high { color: #f56c6c; }
  &.medium { color: #e6a23c; }
  &.low { color: #67c23a; }
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-table) {
  font-size: 14px;
}
</style>