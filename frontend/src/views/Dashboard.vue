<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2>数据看板</h2>
      <p>欢迎回来，{{ realName }} | {{ roleText }}</p>
    </div>
    
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon blue">
            <el-icon><DataBoard /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">156</div>
            <div class="stat-label">今日问诊量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon orange">
            <el-icon><Files /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">23</div>
            <div class="stat-label">待处理病例</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon red">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">8</div>
            <div class="stat-label">高风险案例</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon green">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">45</div>
            <div class="stat-label">随访任务数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="14">
        <el-card>
          <template #header>
            <h3>近7日问诊趋势</h3>
          </template>
          <div class="chart-placeholder">
            <el-progress type="line" :percentage="65" :stroke-width="8" />
            <p>图表区域（需集成 ECharts）</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>
            <h3>风险等级分布</h3>
          </template>
          <div class="risk-distribution">
            <div class="risk-item high">
              <span class="risk-color"></span>
              <span class="risk-text">高风险</span>
              <span class="risk-count">12%</span>
            </div>
            <div class="risk-item medium">
              <span class="risk-color"></span>
              <span class="risk-text">中风险</span>
              <span class="risk-count">28%</span>
            </div>
            <div class="risk-item low">
              <span class="risk-color"></span>
              <span class="risk-text">低风险</span>
              <span class="risk-count">60%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>待处理病例</h3>
            <el-button type="text" size="small">查看全部</el-button>
          </template>
          <el-table :data="caseList" stripe>
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="patient" label="患者" />
            <el-table-column prop="symptoms" label="症状" />
            <el-table-column prop="time" label="提交时间" />
            <el-table-column label="操作">
              <template #default>
                <el-button type="text" size="small">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>随访任务提醒</h3>
            <el-button type="text" size="small">查看全部</el-button>
          </template>
          <el-table :data="followUpList" stripe>
            <el-table-column prop="patient" label="患者" />
            <el-table-column prop="type" label="随访类型" />
            <el-table-column prop="deadline" label="截止时间" />
            <el-table-column label="操作">
              <template #default>
                <el-button type="primary" size="small">完成</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { DataBoard, Files, WarningFilled, Calendar } from '@element-plus/icons-vue'

const role = localStorage.getItem('role') || 'patient'
const realName = localStorage.getItem('realName') || '用户'

const roleText = computed(() => {
  const map = {
    patient: '患者',
    doctor: '医务人员',
    follow: '随访人员',
    admin: '管理员'
  }
  return map[role] || '用户'
})

const caseList = [
  { id: 'C001', patient: '张三', symptoms: '发热、咳嗽', time: '10:30' },
  { id: 'C002', patient: '李四', symptoms: '胸闷、气短', time: '09:15' },
  { id: 'C003', patient: '王五', symptoms: '头痛、乏力', time: '08:45' },
  { id: 'C004', patient: '赵六', symptoms: '腹痛、腹泻', time: '08:00' }
]

const followUpList = [
  { patient: '张三', type: '高血压随访', deadline: '今日' },
  { patient: '李四', type: '糖尿病随访', deadline: '今日' },
  { patient: '王五', type: '慢病复诊', deadline: '明日' },
  { patient: '赵六', type: '术后随访', deadline: '3天后' }
]
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 20px;
}

.dashboard-header h2 {
  font-size: 24px;
  margin: 0;
}

.dashboard-header p {
  color: #909399;
  margin-top: 5px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  margin-right: 20px;
}

.stat-icon.blue {
  background: #ecf5ff;
  color: #409eff;
}

.stat-icon.orange {
  background: #fff7e6;
  color: #e6a23c;
}

.stat-icon.red {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-icon.green {
  background: #f0f9eb;
  color: #67c23a;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.chart-placeholder {
  height: 250px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
}

.chart-placeholder p {
  margin-top: 15px;
  color: #909399;
}

.risk-distribution {
  padding: 10px;
}

.risk-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.risk-item:last-child {
  border-bottom: none;
}

.risk-color {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 12px;
}

.risk-item.high .risk-color {
  background: #f56c6c;
}

.risk-item.medium .risk-color {
  background: #e6a23c;
}

.risk-item.low .risk-color {
  background: #67c23a;
}

.risk-text {
  flex: 1;
}

.risk-count {
  font-weight: bold;
}
</style>