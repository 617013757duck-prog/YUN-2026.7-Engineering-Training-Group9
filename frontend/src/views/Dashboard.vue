<template>
  <div class="dashboard">
    <div class="welcome-section">
      <h2>数据看板</h2>
      <p>欢迎回来，{{ realName }}！</p>
    </div>
    
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon blue">
            <el-icon size="32">TrendCharts</el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.todayConsult }}</p>
            <p class="stat-label">今日问诊量</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon orange">
            <el-icon size="32">List</el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.pendingCase }}</p>
            <p class="stat-label">待处理病例</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon red">
            <el-icon size="32">Warning</el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.highRisk }}</p>
            <p class="stat-label">高风险案例</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon green">
            <el-icon size="32">Calendar</el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.followUpTask }}</p>
            <p class="stat-label">随访任务数</p>
          </div>
        </div>
      </el-card>
    </div>
    
    <div class="charts-section">
      <el-card class="chart-card">
        <h3>近7日问诊趋势</h3>
        <div ref="trendChart" class="chart"></div>
      </el-card>
      
      <el-card class="chart-card">
        <h3>风险等级分布</h3>
        <div ref="riskChart" class="chart"></div>
      </el-card>
    </div>
    
    <div class="bottom-section">
      <el-card class="table-card">
        <h3>待处理病例</h3>
        <el-table :data="pendingCases" border style="width: 100%">
          <el-table-column prop="id" label="病例编号" width="120" />
          <el-table-column prop="patientName" label="患者姓名" width="100" />
          <el-table-column prop="symptom" label="症状描述" />
          <el-table-column prop="riskLevel" label="风险等级" width="100">
            <template #default="scope">
              <el-tag :type="getRiskType(scope.row.riskLevel)">{{ scope.row.riskLevel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="160" />
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button text size="small">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      
      <el-card class="table-card">
        <h3>随访任务提醒</h3>
        <el-table :data="followUpTasks" border style="width: 100%">
          <el-table-column prop="id" label="任务编号" width="120" />
          <el-table-column prop="patientName" label="患者姓名" width="100" />
          <el-table-column prop="disease" label="慢病类型" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getTaskType(scope.row.status)">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dueTime" label="截止时间" width="160" />
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button text size="small">完成</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import * as echarts from 'echarts'

const userStore = useUserStore()

const realName = computed(() => userStore.realName || localStorage.getItem('realName') || '')

const stats = ref({
  todayConsult: 156,
  pendingCase: 23,
  highRisk: 8,
  followUpTask: 45
})

const pendingCases = ref([
  { id: 'CASE001', patientName: '张三', symptom: '持续胸痛3天，伴有呼吸困难', riskLevel: '高', submitTime: '2026-07-23 09:30' },
  { id: 'CASE002', patientName: '李四', symptom: '反复发热一周，体温最高39.5℃', riskLevel: '中', submitTime: '2026-07-23 08:15' },
  { id: 'CASE003', patientName: '王五', symptom: '糖尿病患者，血糖控制不佳', riskLevel: '中', submitTime: '2026-07-23 07:45' },
  { id: 'CASE004', patientName: '赵六', symptom: '高血压患者，血压波动明显', riskLevel: '低', submitTime: '2026-07-22 16:20' },
  { id: 'CASE005', patientName: '钱七', symptom: '咳嗽咳痰两周，加重伴胸闷', riskLevel: '中', submitTime: '2026-07-22 14:30' }
])

const followUpTasks = ref([
  { id: 'TASK001', patientName: '张三', disease: '冠心病', status: '待随访', dueTime: '2026-07-24' },
  { id: 'TASK002', patientName: '李四', disease: '糖尿病', status: '待随访', dueTime: '2026-07-25' },
  { id: 'TASK003', patientName: '王五', disease: '高血压', status: '已完成', dueTime: '2026-07-22' },
  { id: 'TASK004', patientName: '赵六', disease: '慢阻肺', status: '待随访', dueTime: '2026-07-26' },
  { id: 'TASK005', patientName: '钱七', disease: '糖尿病', status: '进行中', dueTime: '2026-07-23' }
])

const trendChart = ref(null)
const riskChart = ref(null)

const getRiskType = (level) => {
  const types = { '高': 'danger', '中': 'warning', '低': 'success' }
  return types[level] || 'info'
}

const getTaskType = (status) => {
  const types = { '待随访': 'warning', '进行中': 'primary', '已完成': 'success' }
  return types[status] || 'info'
}

onMounted(() => {
  initTrendChart()
  initRiskChart()
})

const initTrendChart = () => {
  const chart = echarts.init(trendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [{
      name: '问诊量',
      type: 'line',
      smooth: true,
      data: [120, 132, 101, 134, 190, 230, 156],
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      },
      lineStyle: { color: '#409eff', width: 3 },
      itemStyle: { color: '#409eff' }
    }]
  })
}

const initRiskChart = () => {
  const chart = echarts.init(riskChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      name: '风险等级',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 18, fontWeight: 'bold' }
      },
      data: [
        { value: 8, name: '高风险', itemStyle: { color: '#f56c6c' } },
        { value: 35, name: '中风险', itemStyle: { color: '#e6a23c' } },
        { value: 57, name: '低风险', itemStyle: { color: '#67c23a' } }
      ]
    }]
  })
}
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-section {
    margin-bottom: 24px;
    
    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #303133;
    }
    
    p {
      margin: 0;
      color: #909399;
    }
  }
  
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;
    
    .stat-card {
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
      
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
      }
      
      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.blue { background: #ecf5ff; color: #409eff; }
        &.orange { background: #fdf6ec; color: #e6a23c; }
        &.red { background: #fef0f0; color: #f56c6c; }
        &.green { background: #f0f9eb; color: #67c23a; }
      }
      
      .stat-info {
        .stat-value {
          margin: 0;
          font-size: 28px;
          font-weight: 600;
          color: #303133;
        }
        
        .stat-label {
          margin: 4px 0 0 0;
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
  
  .charts-section {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 24px;
    
    .chart-card {
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
      
      h3 {
        margin: 0 0 16px 0;
        font-size: 16px;
        color: #303133;
      }
      
      .chart {
        height: 280px;
      }
    }
  }
  
  .bottom-section {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    
    .table-card {
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
      
      h3 {
        margin: 0 0 16px 0;
        font-size: 16px;
        color: #303133;
      }
    }
  }
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-section,
  .bottom-section {
    grid-template-columns: 1fr;
  }
}
</style>