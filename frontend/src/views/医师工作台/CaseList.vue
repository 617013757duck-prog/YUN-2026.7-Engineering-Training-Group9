<template>
  <div class="case-list-container page-container">
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
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </span>
        <span class="stat-count pending">{{ stats.pending }}</span>
        <span class="stat-label">待审核</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon high">
          <el-icon><WarningFilled /></el-icon>
        </span>
        <span class="stat-count high">{{ stats.highRisk }}</span>
        <span class="stat-label">高风险</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon medium">
          <el-icon><InfoFilled /></el-icon>
        </span>
        <span class="stat-count medium">{{ stats.mediumRisk }}</span>
        <span class="stat-label">中风险</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon low">
          <el-icon><CircleCheckFilled /></el-icon>
        </span>
        <span class="stat-count low">{{ stats.lowRisk }}</span>
        <span class="stat-label">低风险</span>
      </div>
    </div>
    <div class="filter-bar">
      <div class="filter-header">
        <span class="filter-title">筛选条件</span>
        <el-button type="text" @click="showAdvancedFilter = !showAdvancedFilter">
          <el-icon><ArrowDown v-if="!showAdvancedFilter" /><ArrowUp v-else /></el-icon>
          {{ showAdvancedFilter ? '收起' : '展开' }}
        </el-button>
      </div>
      <div class="filter-basic">
        <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        <el-select v-model="filters.riskLevel" placeholder="风险等级" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="高风险" value="high" />
          <el-option label="中风险" value="medium" />
          <el-option label="低风险" value="low" />
        </el-select>
        <el-input v-model="filters.patientName" placeholder="患者姓名" clearable style="width: 150px" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <transition name="filter-expand">
        <div class="filter-advanced" v-if="showAdvancedFilter">
          <el-date-picker v-model="filters.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 280px" />
          <el-select v-model="filters.gender" placeholder="性别" clearable style="width: 100px">
            <el-option label="全部" value="" />
            <el-option label="男" value="male" />
            <el-option label="女" value="female" />
          </el-select>
          <el-input-number v-model="filters.minAge" :min="0" :max="150" placeholder="最小年龄" style="width: 100px" />
          <span style="margin: 0 4px">-</span>
          <el-input-number v-model="filters.maxAge" :min="0" :max="150" placeholder="最大年龄" style="width: 100px" />
        </div>
      </transition>
    </div>
    <div class="table-card">
      <div class="table-header">
        <div class="batch-actions">
          <el-checkbox 
            :indeterminate="isIndeterminate" 
            v-model="selectAll" 
            @change="handleSelectAll"
          >全选</el-checkbox>
          <el-button 
            v-if="selectedIds.length > 0" 
            type="primary" 
            size="small" 
            @click="batchApprove"
          >
            <el-icon><Check /></el-icon>
            批量通过
          </el-button>
          <el-button 
            v-if="selectedIds.length > 0" 
            type="danger" 
            size="small" 
            @click="batchReject"
          >
            <el-icon><Close /></el-icon>
            批量拒绝
          </el-button>
          <span v-if="selectedIds.length > 0" class="selected-count">已选 {{ selectedIds.length }} 条</span>
        </div>
        <div class="table-info">
          共 {{ pagination.total }} 条记录
        </div>
      </div>
      <el-table :data="filteredCaseList" border v-loading="loading" @selection-change="handleSelectionChange" :row-class-name="getRowClassName">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="病例编号" width="160" />
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">{{ row.gender === 'male' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="symptoms" label="主要症状" />
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)" size="small" effect="dark">{{ getRiskLabel(row.riskLevel) }}</el-tag>
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
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Clock, WarningFilled, InfoFilled, CircleCheckFilled, ArrowDown, ArrowUp, Check, Close } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const showAdvancedFilter = ref(false)
const selectAll = ref(false)
const isIndeterminate = ref(false)
const selectedIds = ref([])

const filters = reactive({
  status: '',
  riskLevel: '',
  patientName: '',
  dateRange: null,
  gender: '',
  minAge: null,
  maxAge: null
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

const filteredCaseList = computed(() => {
  return caseList.filter(item => {
    if (filters.status && item.status !== filters.status) return false
    if (filters.riskLevel && item.riskLevel !== filters.riskLevel) return false
    if (filters.patientName && !item.patientName.includes(filters.patientName)) return false
    if (filters.gender && item.gender !== filters.gender) return false
    if (filters.minAge !== null && item.age < filters.minAge) return false
    if (filters.maxAge !== null && item.age > filters.maxAge) return false
    return true
  })
})

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

const getRowClassName = ({ row }) => {
  if (row.riskLevel === 'high') {
    return 'high-risk-row'
  }
  return ''
}

const goToDetail = (id) => router.push(`/caseDetail/${id}`)

const approveCase = async (id) => {
  await ElMessageBox.confirm('确定要通过此病例吗？', '确认操作', { type: 'warning' })
  const caseItem = caseList.find(c => c.id === id)
  if (caseItem) {
    caseItem.status = 'approved'
    selectedIds.value = selectedIds.value.filter(i => i !== id)
    ElMessage.success('病例已通过')
  }
}

const rejectCase = async (id) => {
  await ElMessageBox.confirm('确定要拒绝此病例吗？', '确认操作', { type: 'warning' })
  const caseItem = caseList.find(c => c.id === id)
  if (caseItem) {
    caseItem.status = 'rejected'
    selectedIds.value = selectedIds.value.filter(i => i !== id)
    ElMessage.success('病例已拒绝')
  }
}

const batchApprove = async () => {
  await ElMessageBox.confirm(`确定要批量通过选中的 ${selectedIds.value.length} 条病例吗？`, '确认批量操作', { type: 'warning' })
  selectedIds.value.forEach(id => {
    const caseItem = caseList.find(c => c.id === id)
    if (caseItem && caseItem.status === 'pending') {
      caseItem.status = 'approved'
    }
  })
  selectedIds.value = []
  selectAll.value = false
  isIndeterminate.value = false
  ElMessage.success(`成功通过 ${selectedIds.value.length} 条病例`)
}

const batchReject = async () => {
  await ElMessageBox.confirm(`确定要批量拒绝选中的 ${selectedIds.value.length} 条病例吗？`, '确认批量操作', { type: 'warning' })
  selectedIds.value.forEach(id => {
    const caseItem = caseList.find(c => c.id === id)
    if (caseItem && caseItem.status === 'pending') {
      caseItem.status = 'rejected'
    }
  })
  selectedIds.value = []
  selectAll.value = false
  isIndeterminate.value = false
  ElMessage.success(`成功拒绝 ${selectedIds.value.length} 条病例`)
}

const handleSelectAll = (val) => {
  if (val) {
    selectedIds.value = filteredCaseList.value.filter(c => c.status === 'pending').map(c => c.id)
  } else {
    selectedIds.value = []
  }
  isIndeterminate.value = false
}

const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
  selectAll.value = selectedIds.value.length === filteredCaseList.value.filter(c => c.status === 'pending').length && selectedIds.value.length > 0
  isIndeterminate.value = selectedIds.value.length > 0 && !selectAll.value
}

const handleSearch = () => {
  pagination.page = 1
}

const resetFilters = () => {
  filters.status = ''
  filters.riskLevel = ''
  filters.patientName = ''
  filters.dateRange = null
  filters.gender = ''
  filters.minAge = null
  filters.maxAge = null
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

.stats-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;

  &.pending { background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%); }
  &.high { background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%); }
  &.medium { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); }
  &.low { background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%); }
}

.stat-count {
  font-size: 28px;
  font-weight: 600;

  &.pending { color: #e6a23c; }
  &.high { color: #f56c6c; }
  &.medium { color: #409eff; }
  &.low { color: #67c23a; }
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-bar {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .filter-title {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
  }
}

.filter-basic {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-advanced {
  display: flex;
  gap: 12px;
  align-items: center;
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px dashed #e4e7ed;
  flex-wrap: wrap;
}

.filter-expand-enter-active,
.filter-expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.filter-expand-enter-from,
.filter-expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.filter-expand-enter-to,
.filter-expand-leave-from {
  max-height: 100px;
}

.table-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.batch-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.selected-count {
  font-size: 13px;
  color: #409eff;
}

.table-info {
  font-size: 13px;
  color: #909399;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-table) {
  font-size: 14px;

  .el-table__row {
    &.high-risk-row {
      background: rgba(245, 108, 108, 0.05);
    }
  }
}

:deep(.el-table__body tr:hover > td) {
  background: rgba(64, 158, 255, 0.05);
}

@media (max-width: 768px) {
  .stats-bar {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-basic {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-basic .el-select,
  .filter-basic .el-input {
    width: 100% !important;
  }

  .filter-advanced {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>