<template>
  <div class="monitor-container">
    <div class="page-header">
      <h1>高风险监控面板</h1>
      <p class="page-desc">实时监控高风险案例、AI输出安全和系统告警</p>
    </div>
    <div class="stats-row">
      <div class="stat-card critical">
        <div class="stat-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.criticalAlerts }}</p>
          <p class="stat-label">严重告警</p>
        </div>
      </div>
      <div class="stat-card warning">
        <div class="stat-icon">
          <el-icon><WarnTriangleFilled /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.warningAlerts }}</p>
          <p class="stat-label">警告</p>
        </div>
      </div>
      <div class="stat-card blocked">
        <div class="stat-icon">
          <el-icon><Block /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.blockedRequests }}</p>
          <p class="stat-label">拦截请求</p>
        </div>
      </div>
      <div class="stat-card safe">
        <div class="stat-icon">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
        <div class="stat-info">
          <p class="stat-value">{{ stats.safeRate }}%</p>
          <p class="stat-label">安全通过率</p>
        </div>
      </div>
    </div>
    <div class="content-row">
      <div class="chart-card">
        <div class="card-header">
          <h3>风险分布统计</h3>
        </div>
        <div ref="riskChart" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="card-header">
          <h3>告警趋势</h3>
        </div>
        <div ref="alertChart" class="chart-container"></div>
      </div>
    </div>
    <div class="content-row">
      <div class="list-card">
        <div class="card-header">
          <h3>高风险案例</h3>
          <el-button type="primary" size="small" @click="viewAllCases">查看全部</el-button>
        </div>
        <el-table :data="highRiskCases" border>
          <el-table-column prop="id" label="病例编号" width="140" />
          <el-table-column prop="patientName" label="患者姓名" width="100" />
          <el-table-column prop="symptoms" label="主要症状" />
          <el-table-column prop="riskLevel" label="风险等级" width="100">
            <template #default="{ row }">
              <el-tag type="danger" size="small">{{ row.riskLevel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="riskFactors" label="风险因素" width="150">
            <template #default="{ row }">
              <span class="risk-factor-tag">{{ row.riskFactors }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewCase(row.id)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="list-card">
        <div class="card-header">
          <h3>安全告警日志</h3>
          <el-select v-model="alertFilter" size="small" style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="严重" value="critical" />
            <el-option label="警告" value="warning" />
            <el-option label="拦截" value="blocked" />
          </el-select>
        </div>
        <el-table :data="alertLogs" border>
          <el-table-column prop="time" label="时间" width="160" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getAlertTagType(row.type)" size="small">{{ getAlertLabel(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="告警信息" />
          <el-table-column prop="source" label="来源" width="120" />
        </el-table>
      </div>
    </div>
    <div class="safety-section">
      <div class="safety-card">
        <div class="card-header">
          <h3>AI安全监控</h3>
          <div class="safety-status" :class="safetyStatus">
            <span class="status-dot"></span>
            {{ safetyStatus === 'safe' ? '系统安全' : '存在风险' }}
          </div>
        </div>
        <div class="safety-grid">
          <div class="safety-item">
            <h4>隐私泄露检测</h4>
            <div class="safety-bar">
              <div class="bar-fill" :style="{ width: safetyMetrics.privacy + '%' }"></div>
            </div>
            <span class="safety-value">{{ safetyMetrics.privacy }}%</span>
          </div>
          <div class="safety-item">
            <h4>越权诊断检测</h4>
            <div class="safety-bar">
              <div class="bar-fill" :style="{ width: safetyMetrics.auth + '%' }"></div>
            </div>
            <span class="safety-value">{{ safetyMetrics.auth }}%</span>
          </div>
          <div class="safety-item">
            <h4>提示词攻击检测</h4>
            <div class="safety-bar">
              <div class="bar-fill" :style="{ width: safetyMetrics.prompt + '%' }"></div>
            </div>
            <span class="safety-value">{{ safetyMetrics.prompt }}%</span>
          </div>
          <div class="safety-item">
            <h4>输出合规检测</h4>
            <div class="safety-bar">
              <div class="bar-fill" :style="{ width: safetyMetrics.compliance + '%' }"></div>
            </div>
            <span class="safety-value">{{ safetyMetrics.compliance }}%</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Warning, WarnTriangleFilled, Block, CircleCheckFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()
const riskChart = ref(null)
const alertChart = ref(null)
const alertFilter = ref('')

const stats = reactive({
  criticalAlerts: 3,
  warningAlerts: 8,
  blockedRequests: 15,
  safeRate: 98.5
})

const safetyStatus = ref('safe')

const safetyMetrics = reactive({
  privacy: 96,
  auth: 94,
  prompt: 98,
  compliance: 95
})

const highRiskCases = [
  { id: 'CASE-20260722001', patientName: '张三', symptoms: '胸闷、气短、心悸', riskLevel: '高风险', riskFactors: '胸痛、高血压病史' },
  { id: 'CASE-20260722005', patientName: '钱七', symptoms: '胸痛、放射至左肩', riskLevel: '高风险', riskFactors: '胸痛、高龄' },
  { id: 'CASE-20260722007', patientName: '周九', symptoms: '呼吸困难、喘息', riskLevel: '高风险', riskFactors: '呼吸困难' },
  { id: 'CASE-20260722009', patientName: '郑十一', symptoms: '意识模糊、血压骤降', riskLevel: '高风险', riskFactors: '意识障碍、休克' }
]

const alertLogs = [
  { time: '2026-07-22 14:35', type: 'critical', message: '检测到疑似隐私数据泄露', source: 'AI服务' },
  { time: '2026-07-22 14:28', type: 'blocked', message: '拦截越权诊断请求', source: '安全Agent' },
  { time: '2026-07-22 14:15', type: 'warning', message: '检测到异常提示词模式', source: '安全Agent' },
  { time: '2026-07-22 13:45', type: 'warning', message: '高风险病例未及时处理', source: '系统' },
  { time: '2026-07-22 13:20', type: 'blocked', message: '拦截敏感信息查询', source: 'API网关' },
  { time: '2026-07-22 12:55', type: 'critical', message: '模型输出超出安全范围', source: 'AI服务' }
]

const getAlertTagType = (type) => {
  const types = { critical: 'danger', warning: 'warning', blocked: 'info' }
  return types[type] || 'info'
}

const getAlertLabel = (type) => {
  const labels = { critical: '严重', warning: '警告', blocked: '拦截' }
  return labels[type] || type
}

const viewAllCases = () => router.push('/caseList')
const viewCase = (id) => router.push(`/caseDetail/${id}`)

const initRiskChart = () => {
  if (!riskChart.value) return
  const chart = echarts.init(riskChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['今日', '昨日', '本周', '本月'], axisLine: { lineStyle: { color: '#dcdfe6' } } },
    yAxis: { type: 'value', axisLine: { lineStyle: { color: '#dcdfe6' } } },
    series: [
      { name: '高风险', type: 'bar', data: [15, 12, 45, 120], itemStyle: { color: '#f56c6c' } },
      { name: '中风险', type: 'bar', data: [28, 32, 95, 280], itemStyle: { color: '#e6a23c' } },
      { name: '低风险', type: 'bar', data: [52, 48, 156, 520], itemStyle: { color: '#67c23a' } }
    ]
  })
}

const initAlertChart = () => {
  if (!alertChart.value) return
  const chart = echarts.init(alertChart.value)
  const hours = ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00']
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: hours, axisLine: { lineStyle: { color: '#dcdfe6' } } },
    yAxis: { type: 'value', axisLine: { lineStyle: { color: '#dcdfe6' } } },
    series: [
      { name: '严重', type: 'line', data: [1, 2, 0, 2, 1, 0], itemStyle: { color: '#f56c6c' }, lineStyle: { width: 2 } },
      { name: '警告', type: 'line', data: [2, 3, 4, 2, 3, 1], itemStyle: { color: '#e6a23c' }, lineStyle: { width: 2 } },
      { name: '拦截', type: 'line', data: [3, 5, 4, 3, 2, 1], itemStyle: { color: '#409eff' }, lineStyle: { width: 2 } }
    ]
  })
}

onMounted(() => {
  initRiskChart()
  initAlertChart()
  window.addEventListener('resize', () => {
    riskChart.value && echarts.init(riskChart.value).resize()
    alertChart.value && echarts.init(alertChart.value).resize()
  })
})
</script>

<style lang="scss" scoped>
.monitor-container {
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

  &.critical { border-left: 4px solid #f56c6c; }
  &.warning { border-left: 4px solid #e6a23c; }
  &.blocked { border-left: 4px solid #409eff; }
  &.safe { border-left: 4px solid #67c23a; }
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
  background: #f5f7fa;

  .stat-card.critical & { background: #fef0f0; color: #f56c6c; }
  .stat-card.warning & { background: #fdf6ec; color: #e6a23c; }
  .stat-card.blocked & { background: #ecf5ff; color: #409eff; }
  .stat-card.safe & { background: #f0f9eb; color: #67c23a; }
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

.content-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card, .list-card {
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

.chart-container {
  height: 280px;
}

.risk-factor-tag {
  font-size: 12px;
  color: #f56c6c;
}

:deep(.el-table) {
  font-size: 14px;
}

.safety-section {
  margin-bottom: 24px;
}

.safety-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.safety-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;

  &.safe { color: #67c23a; }
  &.risk { color: #f56c6c; }
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.safety-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.safety-item {
  h4 {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
    margin-bottom: 12px;
  }
}

.safety-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #67c23a, #85ce61);
  border-radius: 4px;
  transition: width 0.5s;
}

.safety-value {
  font-size: 14px;
  font-weight: 600;
  color: #67c23a;
}
</style>