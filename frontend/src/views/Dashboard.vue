<template>
  <div class="dashboard page-container">
    <!-- 页面背景装饰 -->
    <div class="page-bg-decor">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
    </div>

    <div class="dashboard-header">
      <div class="header-left">
        <div class="header-title-row">
          <h1>数据看板</h1>
          <span class="header-divider"></span>
          <span class="header-subtitle">{{ roleText }}视角</span>
        </div>
        <p class="header-welcome">
          <span class="welcome-dot"></span>
          欢迎回来，{{ realName }}
        </p>
      </div>
      <div class="header-actions">
        <div class="date-picker-wrapper">
          <el-icon class="picker-icon"><Calendar /></el-icon>
          <el-date-picker 
            v-model="dateRange" 
            type="daterange" 
            range-separator="至" 
            start-placeholder="开始" 
            end-placeholder="结束" 
            size="small" 
          />
        </div>
        <el-button type="primary" size="small" @click="refreshData" round>
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>
    
    <div class="stats-row">
      <div class="stat-card" @click="handleStatClick('consult')">
        <div class="stat-icon-wrap blue-bg">
          <el-icon><DataBoard /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日问诊量</div>
          <div class="stat-value">{{ stats.consultCount }}</div>
          <div class="stat-trend" :class="stats.consultTrend > 0 ? 'positive' : 'negative'">
            <el-icon :size="12"><ArrowUp v-if="stats.consultTrend > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.consultTrend) }}% 较昨日
          </div>
        </div>
        <div class="stat-decor-circle blue-circle"></div>
      </div>
      <div class="stat-card" @click="handleStatClick('cases')">
        <div class="stat-icon-wrap amber-bg">
          <el-icon><Files /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">待处理病例</div>
          <div class="stat-value">{{ stats.pendingCases }}</div>
          <div class="stat-trend" :class="stats.pendingTrend > 0 ? 'positive' : 'negative'">
            <el-icon :size="12"><ArrowUp v-if="stats.pendingTrend > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.pendingTrend) }}% 较昨日
          </div>
        </div>
        <div class="stat-decor-circle amber-circle"></div>
      </div>
      <div class="stat-card" @click="handleStatClick('risk')">
        <div class="stat-icon-wrap rose-bg">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">高风险案例</div>
          <div class="stat-value">{{ stats.highRiskCases }}</div>
          <div class="stat-trend" :class="stats.riskTrend > 0 ? 'positive' : 'negative'">
            <el-icon :size="12"><ArrowUp v-if="stats.riskTrend > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.riskTrend) }}% 较昨日
          </div>
        </div>
        <div class="stat-decor-circle rose-circle"></div>
      </div>
      <div class="stat-card" @click="handleStatClick('followUp')">
        <div class="stat-icon-wrap emerald-bg">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">随访任务数</div>
          <div class="stat-value">{{ stats.followUpTasks }}</div>
          <div class="stat-trend" :class="stats.followUpTrend > 0 ? 'positive' : 'negative'">
            <el-icon :size="12"><ArrowUp v-if="stats.followUpTrend > 0" /><ArrowDown v-else /></el-icon>
            {{ Math.abs(stats.followUpTrend) }}% 较昨日
          </div>
        </div>
        <div class="stat-decor-circle emerald-circle"></div>
      </div>
    </div>

    <div class="content-row">
      <div class="glass-card chart-card">
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-title-icon chart-icon">
              <el-icon><TrendCharts /></el-icon>
            </span>
            <h3>近7日问诊趋势</h3>
          </div>
          <div class="card-actions">
            <span class="period-label">近7天</span>
          </div>
        </div>
        <div ref="trendChart" class="chart-container"></div>
      </div>
      <div class="glass-card chart-card">
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-title-icon risk-icon">
              <el-icon><PieChart /></el-icon>
            </span>
            <h3>风险等级分布</h3>
          </div>
          <div class="card-actions">
            <span class="period-label">当前</span>
          </div>
        </div>
        <div ref="riskChart" class="chart-container"></div>
      </div>
    </div>

    <div class="content-row">
      <div class="glass-card list-card">
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-title-icon case-icon">
              <el-icon><Document /></el-icon>
            </span>
            <h3>待处理病例</h3>
            <span class="badge-count">{{ caseTotal }}</span>
          </div>
          <div class="card-actions">
            <el-select v-model="caseFilter" size="small" style="width: 110px" placeholder="筛选风险">
              <el-option label="全部" value="" />
              <el-option label="高风险" value="high" />
              <el-option label="中风险" value="medium" />
              <el-option label="低风险" value="low" />
            </el-select>
            <el-button type="text" size="small" @click="viewAllCases">查看全部 →</el-button>
          </div>
        </div>
        <div v-loading="loadingCases" class="table-wrapper">
          <el-table :data="filteredCases" :header-cell-style="tableHeaderStyle">
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="patient" label="患者" />
            <el-table-column prop="symptoms" label="症状" show-overflow-tooltip />
            <el-table-column prop="riskLevel" label="风险等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getRiskTagType(row.riskLevel)" size="small" effect="light" round>{{ row.riskLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="提交时间" width="90" />
            <el-table-column label="操作" width="70" align="center">
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
          <div class="card-title-group">
            <span class="card-title-icon followup-icon">
              <el-icon><List /></el-icon>
            </span>
            <h3>随访任务提醒</h3>
            <span class="badge-count">{{ followUpTotal }}</span>
          </div>
          <div class="card-actions">
            <el-select v-model="followUpFilter" size="small" style="width: 110px" placeholder="筛选时间">
              <el-option label="全部" value="" />
              <el-option label="今日" value="today" />
              <el-option label="即将到期" value="upcoming" />
              <el-option label="已逾期" value="overdue" />
            </el-select>
            <el-button type="text" size="small" @click="viewAllFollowUp">查看全部 →</el-button>
          </div>
        </div>
        <div v-loading="loadingFollowUp" class="table-wrapper">
          <el-table :data="filteredFollowUp" :header-cell-style="tableHeaderStyle">
            <el-table-column prop="patient" label="患者" />
            <el-table-column prop="type" label="随访类型" />
            <el-table-column prop="deadline" label="截止时间" width="100">
              <template #default="{ row }">
                <span :class="getDeadlineClass(row.deadline)" class="deadline-text">{{ row.deadline }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'completed' ? 'success' : 'warning'" size="small" effect="light" round>
                  {{ row.status === 'completed' ? '已完成' : '待执行' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ row }">
                <el-button v-if="row.status !== 'completed'" type="primary" size="small" round @click="completeTask(row)">完成</el-button>
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
import { 
  DataBoard, Files, WarningFilled, Calendar, Refresh, 
  ArrowUp, ArrowDown, Document, ArrowRight, 
  TrendCharts, PieChart, List 
} from '@element-plus/icons-vue'
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

const tableHeaderStyle = {
  background: '#fafbfc',
  color: '#4a4a68',
  fontWeight: '600',
  fontSize: '13px'
}

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

const handleStatClick = (type) => {
  switch (type) {
    case 'consult':
      router.push('/preConsult')
      break
    case 'cases':
      router.push('/caseList')
      break
    case 'risk':
      router.push('/monitor')
      break
    case 'followUp':
      router.push('/followUp')
      break
  }
}

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
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: 'rgba(64, 158, 255, 0.15)',
      borderWidth: 1,
      textStyle: { color: '#1a1a2e', fontSize: 13 },
      extraCssText: 'box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08); border-radius: 8px;'
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: days, 
      axisLine: { show: false },
      axisLabel: { color: '#8e8ea9', fontSize: 12 },
      axisTick: { show: false }
    },
    yAxis: { 
      type: 'value', 
      axisLine: { show: false },
      axisLabel: { color: '#8e8ea9', fontSize: 12 },
      splitLine: { lineStyle: { color: 'rgba(0, 0, 0, 0.04)', type: 'dashed' } }
    },
    series: [{
      name: '问诊量',
      type: 'line',
      smooth: true,
      data: [120, 135, 118, 145, 132, 156, stats.consultCount],
      itemStyle: { 
        color: '#409eff',
        borderColor: '#fff',
        borderWidth: 2
      },
      lineStyle: { width: 3, color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
        { offset: 0, color: '#409eff' },
        { offset: 1, color: '#66b1ff' }
      ])},
      symbol: 'circle',
      symbolSize: 8,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.25)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: '#409eff',
          borderColor: '#fff',
          borderWidth: 3,
          shadowColor: 'rgba(64, 158, 255, 0.4)',
          shadowBlur: 12
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
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: 'rgba(64, 158, 255, 0.15)',
      borderWidth: 1,
      textStyle: { color: '#1a1a2e', fontSize: 13 },
      extraCssText: 'box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08); border-radius: 8px;'
    },
    legend: { 
      orient: 'vertical', 
      right: '5%', 
      top: 'center',
      textStyle: { color: '#4a4a68', fontSize: 13 },
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 16
    },
    series: [{
      type: 'pie',
      radius: ['48%', '78%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      labelLine: { show: false },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: '600', color: '#1a1a2e' },
        scale: true,
        scaleSize: 8,
        itemStyle: {
          shadowColor: 'rgba(0, 0, 0, 0.15)',
          shadowBlur: 10
        }
      },
      data: [
        { value: 12, name: '高风险', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#f56c6c' }, { offset: 1, color: '#f78989' }]) } },
        { value: 28, name: '中风险', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#e6a23c' }, { offset: 1, color: '#ebb563' }]) } },
        { value: 60, name: '低风险', itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [{ offset: 0, color: '#67c23a' }, { offset: 1, color: '#85ce61' }]) } }
      ],
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 3
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
// ============ 页面背景装饰 ============
.page-bg-decor {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;

  .bg-circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.4;
  }

  .bg-circle-1 {
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(64, 158, 255, 0.15) 0%, transparent 70%);
    top: -100px;
    right: -50px;
  }

  .bg-circle-2 {
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(103, 194, 58, 0.1) 0%, transparent 70%);
    bottom: -50px;
    left: -50px;
  }
}

.dashboard {
  position: relative;
  z-index: 1;
  padding-bottom: 20px;
}

// ============ 页面头部 ============
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;

  .header-left {
    .header-title-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      h1 {
        font-size: 22px;
        font-weight: 700;
        color: #1a1a2e;
        margin: 0;
        background: linear-gradient(135deg, #1a1a2e 0%, #409eff 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      .header-divider {
        width: 1px;
        height: 18px;
        background: #d9d9e3;
      }

      .header-subtitle {
        font-size: 13px;
        color: #8e8ea9;
        font-weight: 500;
      }
    }

    .header-welcome {
      font-size: 13px;
      color: #8e8ea9;
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;

      .welcome-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #67c23a;
        box-shadow: 0 0 6px rgba(103, 194, 58, 0.4);
      }
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;
    align-items: center;

    .date-picker-wrapper {
      display: flex;
      align-items: center;
      gap: 6px;

      .picker-icon {
        color: #8e8ea9;
        font-size: 16px;
      }
    }
  }
}

// ============ 统计卡片 ============
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 12px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.04), 0 12px 28px rgba(0, 0, 0, 0.08);
    border-color: rgba(255, 255, 255, 1);
  }

  .stat-decor-circle {
    position: absolute;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    opacity: 0.06;
    top: -20px;
    right: -20px;
    transition: opacity 0.3s;

    .stat-card:hover & {
      opacity: 0.1;
    }

    &.blue-circle { background: #409eff; }
    &.amber-circle { background: #e6a23c; }
    &.rose-circle { background: #f56c6c; }
    &.emerald-circle { background: #67c23a; }
  }
}

.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  flex-shrink: 0;
  transition: transform 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);

  .stat-card:hover & {
    transform: scale(1.08) rotate(-3deg);
  }

  &.blue-bg { 
    background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
    box-shadow: 0 4px 14px rgba(64, 158, 255, 0.3);
  }
  &.amber-bg { 
    background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
    box-shadow: 0 4px 14px rgba(230, 162, 60, 0.3);
  }
  &.rose-bg { 
    background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
    box-shadow: 0 4px 14px rgba(245, 108, 108, 0.3);
  }
  &.emerald-bg { 
    background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
    box-shadow: 0 4px 14px rgba(103, 194, 58, 0.3);
  }
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #8e8ea9;
  margin-bottom: 4px;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
  margin-bottom: 6px;
  font-variant-numeric: tabular-nums;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 3px;
  font-weight: 500;
  opacity: 0.85;

  &.positive { color: #67c23a; }
  &.negative { color: #f56c6c; }
}

// ============ 内容区域 ============
.content-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03), 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03), 0 8px 24px rgba(0, 0, 0, 0.06);
  }
}

.chart-card {
  padding: 20px;

  .chart-container {
    height: 280px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .card-title-group {
    display: flex;
    align-items: center;
    gap: 8px;

    h3 {
      font-size: 15px;
      font-weight: 600;
      color: #1a1a2e;
      margin: 0;
    }
  }

  .card-title-icon {
    width: 28px;
    height: 28px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #fff;
    flex-shrink: 0;

    &.chart-icon {
      background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    }
    &.risk-icon {
      background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
    }
    &.case-icon {
      background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
    }
    &.followup-icon {
      background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
    }
  }

  .badge-count {
    background: rgba(64, 158, 255, 0.1);
    color: #409eff;
    font-size: 11px;
    font-weight: 600;
    padding: 2px 8px;
    border-radius: 10px;
  }

  .period-label {
    font-size: 12px;
    color: #8e8ea9;
    background: #f5f7fa;
    padding: 4px 10px;
    border-radius: 6px;
    font-weight: 500;
  }
}

.card-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.list-card {
  padding: 20px;

  .table-wrapper {
    :deep(.el-table) {
      --el-table-border-color: rgba(0, 0, 0, 0.04);
      
      th.el-table__cell {
        border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;
      }

      td.el-table__cell {
        border-bottom-color: rgba(0, 0, 0, 0.03) !important;
      }

      .el-table__row:hover > td.el-table__cell {
        background: rgba(64, 158, 255, 0.04) !important;
      }
    }
  }
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.deadline-text {
  &.deadline-today {
    color: #e6a23c;
    font-weight: 600;
  }

  &.deadline-overdue {
    color: #f56c6c;
    font-weight: 600;
    position: relative;

    &::before {
      content: '⚠';
      margin-right: 4px;
      font-size: 12px;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  gap: 12px;

  .empty-icon {
    font-size: 40px;
    color: #d9d9e3;
  }

  .empty-text {
    font-size: 14px;
    color: #8e8ea9;
  }
}

// ============ 响应式 ============
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
