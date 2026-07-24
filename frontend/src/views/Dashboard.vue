<template>
  <div class="dashboard page-container">
    <div class="dashboard-header">
      <div class="header-left">
        <h1>数据看板</h1>
        <p>欢迎回来，{{ realName }} | {{ roleText }}</p>
      </div>
      <div class="header-actions">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="small" />
        <el-button type="primary" size="small" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>
    
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon><DataBoard /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.consultCount }}</div>
          <div class="stat-label">今日问诊量</div>
          <div class="stat-trend" :class="stats.consultTrend > 0 ? 'positive' : 'negative'">
            <el-icon><ArrowUp v-if="stats.consultTrend > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.consultTrend) }}%
          </div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon><Files /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingCases }}</div>
          <div class="stat-label">待处理病例</div>
          <div class="stat-trend" :class="stats.pendingTrend > 0 ? 'positive' : 'negative'">
            <el-icon><ArrowUp v-if="stats.pendingTrend > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.pendingTrend) }}%
          </div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.highRiskCases }}</div>
          <div class="stat-label">高风险案例</div>
          <div class="stat-trend" :class="stats.riskTrend > 0 ? 'positive' : 'negative'">
            <el-icon><ArrowUp v-if="stats.riskTrend > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.riskTrend) }}%
          </div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.followUpTasks }}</div>
          <div class="stat-label">随访任务数</div>
          <div class="stat-trend" :class="stats.followUpTrend > 0 ? 'positive' : 'negative'">
            <el-icon><ArrowUp v-if="stats.followUpTrend > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.followUpTrend) }}%
          </div>
        </div>
      </div>
    </div>

    <div class="content-row">
      <div class="glass-card chart-card">
        <div class="card-header">
          <h3>近7日问诊趋势</h3>
        </div>
        <div ref="trendChart" class="chart-container"></div>
      </div>
      <div class="glass-card chart-card">
        <div class="card-header">
          <h3>风险等级分布</h3>
        </div>
        <div ref="riskChart" class="chart-container"></div>
      </div>
    </div>

    <div class="content-row">
      <div class="glass-card list-card">
        <div class="card-header">
          <h3>待处理病例</h3>
          <div class="card-actions">
            <el-button type="text" size="small" @click="viewAllCases">查看全部</el-button>
            <el-select v-model="caseFilter" size="small" style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="高风险" value="high" />
              <el-option label="中风险" value="medium" />
              <el-option label="低风险" value="low" />
            </el-select>
          </div>
        </div>
        <div v-loading="loadingCases">
          <el-table :data="filteredCases" stripe>
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="patient" label="患者" />
            <el-table-column prop="symptoms" label="症状" />
            <el-table-column prop="riskLevel" label="风险等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getRiskTagType(row.riskLevel)" size="small">{{ row.riskLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="提交时间" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="viewCase(row.id)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="filteredCases.length === 0 && !loadingCases" class="empty-state">
            <el-icon class="empty-icon"><Document /></el-icon>
            <span class="empty-text">暂无待处理病例</span>
          </div>
        </div>
        <div class="card-footer">
          <el-pagination 
            v-model:current-page="casePage" 
            v-model:page-size="casePageSize" 
            :total="caseTotal" 
            :page-sizes="[5, 10, 20]" 
            layout="total, sizes, prev, pager, next"
            size="small"
          />
        </div>
      </div>
      <div class="glass-card list-card">
        <div class="card-header">
          <h3>随访任务提醒</h3>
          <div class="card-actions">
            <el-button type="text" size="small" @click="viewAllFollowUp">查看全部</el-button>
            <el-select v-model="followUpFilter" size="small" style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="今日" value="today" />
              <el-option label="即将到期" value="upcoming" />
              <el-option label="已逾期" value="overdue" />
            </el-select>
          </div>
        </div>
        <div v-loading="loadingFollowUp">
          <el-table :data="filteredFollowUp" stripe>
            <el-table-column prop="patient" label="患者" />
            <el-table-column prop="type" label="随访类型" />
            <el-table-column prop="deadline" label="截止时间" width="100">
              <template #default="{ row }">
                <span :class="getDeadlineClass(row.deadline)">{{ row.deadline }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'completed' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'completed' ? '已完成' : '待执行' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button v-if="row.status !== 'completed'" type="primary" size="small" @click="completeTask(row)">完成</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="filteredFollowUp.length === 0 && !loadingFollowUp" class="empty-state">
            <el-icon class="empty-icon"><Calendar /></el-icon>
            <span class="empty-text">暂无随访任务</span>
          </div>
        </div>
        <div class="card-footer">
          <el-pagination 
            v-model:current-page="followUpPage" 
            v-model:page-size="followUpPageSize" 
            :total="followUpTotal" 
            :page-sizes="[5, 10, 20]" 
            layout="total, sizes, prev, pager, next"
            size="small"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataBoard, Files, WarningFilled, Calendar, Refresh, ArrowUp, ArrowDown, Document } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()
const trendChart = ref(null)
const riskChart = ref(null)
const loadingCases = ref(false)
const loadingFollowUp = ref(false)

const dateRange = ref([])
const caseFilter = ref('')
const followUpFilter = ref('')
const casePage = ref(1)
const casePageSize = ref(5)
const followUpPage = ref(1)
const followUpPageSize = ref(5)

const role = localStorage.getItem('role') || 'patient'
const realName = localStorage.getItem('realName') || '用户'

const roleText = computed(() => {
  const map = { patient: '患者', doctor: '医务人员', follow: '随访人员', admin: '管理员' }
  return map[role] || '用户'
})

const stats = reactive({
  consultCount: 156,
  consultTrend: 12.5,
  pendingCases: 23,
  pendingTrend: -8.3,
  highRiskCases: 8,
  riskTrend: 5.2,
  followUpTasks: 45,
  followUpTrend: 3.7
})

const caseList = [
  { id: 'C001', patient: '张三', symptoms: '发热、咳嗽', riskLevel: '高风险', time: '10:30' },
  { id: 'C002', patient: '李四', symptoms: '胸闷、气短', riskLevel: '高风险', time: '09:15' },
  { id: 'C003', patient: '王五', symptoms: '头痛、乏力', riskLevel: '中风险', time: '08:45' },
  { id: 'C004', patient: '赵六', symptoms: '腹痛、腹泻', riskLevel: '低风险', time: '08:00' },
  { id: 'C005', patient: '钱七', symptoms: '呼吸困难', riskLevel: '高风险', time: '07:30' },
  { id: 'C006', patient: '孙八', symptoms: '恶心呕吐', riskLevel: '中风险', time: '07:00' }
]

const followUpList = [
  { patient: '张三', type: '高血压随访', deadline: '今日', status: 'pending' },
  { patient: '李四', type: '糖尿病随访', deadline: '今日', status: 'pending' },
  { patient: '王五', type: '慢病复诊', deadline: '明日', status: 'pending' },
  { patient: '赵六', type: '术后随访', deadline: '3天后', status: 'pending' },
  { patient: '钱七', type: '冠心病随访', deadline: '已逾期', status: 'pending' },
  { patient: '孙八', type: '慢阻肺随访', deadline: '本周', status: 'completed' }
]

const caseTotal = ref(caseList.length)
const followUpTotal = ref(followUpList.length)

const filteredCases = computed(() => {
  let filtered = caseList
  if (caseFilter.value) {
    filtered = filtered.filter(item => {
      if (caseFilter.value === 'high') return item.riskLevel === '高风险'
      if (caseFilter.value === 'medium') return item.riskLevel === '中风险'
      if (caseFilter.value === 'low') return item.riskLevel === '低风险'
      return true
    })
  }
  const start = (casePage.value - 1) * casePageSize.value
  return filtered.slice(start, start + casePageSize.value)
})

const filteredFollowUp = computed(() => {
  let filtered = followUpList
  if (followUpFilter.value) {
    filtered = filtered.filter(item => {
      if (followUpFilter.value === 'today') return item.deadline === '今日'
      if (followUpFilter.value === 'upcoming') return ['明日', '3天后', '本周'].includes(item.deadline)
      if (followUpFilter.value === 'overdue') return item.deadline === '已逾期'
      return true
    })
  }
  const start = (followUpPage.value - 1) * followUpPageSize.value
  return filtered.slice(start, start + followUpPageSize.value)
})

const getRiskTagType = (level) => {
  const types = { '高风险': 'danger', '中风险': 'warning', '低风险': 'success' }
  return types[level] || 'info'
}

const getDeadlineClass = (deadline) => {
  if (deadline === '已逾期') return 'deadline-overdue'
  if (deadline === '今日') return 'deadline-today'
  return ''
}

const viewAllCases = () => router.push('/caseList')
const viewCase = (id) => router.push(`/caseDetail/${id}`)
const viewAllFollowUp = () => router.push('/followUp')

const completeTask = (row) => {
  row.status = 'completed'
  ElMessage.success(`已完成${row.patient}的随访任务`)
}

const refreshData = () => {
  loadingCases.value = true
  loadingFollowUp.value = true
  setTimeout(() => {
    stats.consultCount += Math.floor(Math.random() * 10) - 5
    stats.pendingCases = Math.max(0, stats.pendingCases + Math.floor(Math.random() * 5) - 2)
    loadingCases.value = false
    loadingFollowUp.value = false
    initTrendChart()
    initRiskChart()
    ElMessage.success('数据已刷新')
  }, 800)
}

const initTrendChart = () => {
  if (!trendChart.value) return
  const chart = echarts.init(trendChart.value)
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { 
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: 'rgba(64, 158, 255, 0.2)',
      borderWidth: 1,
      textStyle: { color: '#1a1a2e' },
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)'
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: days, 
      axisLine: { lineStyle: { color: 'rgba(0, 0, 0, 0.08)' } },
      axisLabel: { color: '#8e8ea9', fontSize: 12 },
      axisTick: { show: false }
    },
    yAxis: { 
      type: 'value', 
      axisLine: { lineStyle: { color: 'rgba(0, 0, 0, 0.08)' } },
      axisLabel: { color: '#8e8ea9', fontSize: 12 },
      splitLine: { lineStyle: { color: 'rgba(0, 0, 0, 0.04)' } }
    },
    series: [{
      name: '问诊量',
      type: 'line',
      smooth: true,
      data: [120, 135, 118, 145, 132, 156, stats.consultCount],
      itemStyle: { color: '#409eff' },
      lineStyle: { width: 3, color: '#409eff' },
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: '#409eff',
          borderColor: '#fff',
          borderWidth: 2,
          shadowColor: 'rgba(64, 158, 255, 0.4)',
          shadowBlur: 10
        }
      }
    }]
  })
}

const initRiskChart = () => {
  if (!riskChart.value) return
  const chart = echarts.init(riskChart.value)
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: { 
      trigger: 'item', 
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: 'rgba(64, 158, 255, 0.2)',
      borderWidth: 1,
      textStyle: { color: '#1a1a2e' },
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)'
    },
    legend: { 
      orient: 'vertical', 
      right: '5%', 
      top: 'center',
      textStyle: { color: '#4a4a68', fontSize: 13 },
      itemWidth: 12,
      itemHeight: 12
    },
    series: [{
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      labelLine: { show: false },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: '600', color: '#1a1a2e' },
        scale: true,
        scaleSize: 10
      },
      data: [
        { value: 12, name: '高风险', itemStyle: { color: '#f56c6c' } },
        { value: 28, name: '中风险', itemStyle: { color: '#e6a23c' } },
        { value: 60, name: '低风险', itemStyle: { color: '#67c23a' } }
      ],
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      }
    }]
  })
}

let resizeTimer = null

const handleResize = () => {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    trendChart.value && echarts.init(trendChart.value).resize()
    riskChart.value && echarts.init(riskChart.value).resize()
  }, 200)
}

onMounted(() => {
  setTimeout(() => {
    initTrendChart()
    initRiskChart()
  }, 100)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  clearTimeout(resizeTimer)
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding-bottom: 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;

  .header-left {
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #1a1a2e;
      margin-bottom: 8px;
      background: linear-gradient(135deg, #1a1a2e 0%, #409eff 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    p {
      font-size: 14px;
      color: #8e8ea9;
      display: flex;
      align-items: center;
      gap: 8px;

      &::before {
        content: '';
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background: #409eff;
      }
    }
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.3), transparent);
    opacity: 0;
    transition: opacity 0.25s;
  }

  &:hover {
    transform: translateY(-3px) scale(1.01);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    background: rgba(255, 255, 255, 0.95);

    &::before {
      opacity: 1;
    }

    .stat-icon {
      transform: scale(1.1);
    }
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  transition: transform 0.25s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

  &.blue { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); }
  &.orange { background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%); }
  &.red { background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%); }
  &.green { background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%); }
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 4px;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #8e8ea9;
  margin-bottom: 6px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;

  &.positive { color: #67c23a; }
  &.negative { color: #f56c6c; }
}

.content-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  padding: 24px;

  .chart-container {
    height: 280px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 8px;

    &::before {
      content: '';
      width: 4px;
      height: 4px;
      border-radius: 50%;
      background: #409eff;
    }
  }
}

.card-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.deadline-today {
  color: #e6a23c;
  font-weight: 500;
}

.deadline-overdue {
  color: #f56c6c;
  font-weight: 500;
}

@media (max-width: 992px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-row {
    grid-template-columns: 1fr;
  }

  .dashboard-header {
    flex-direction: column;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>