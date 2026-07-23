<template>
  <div class="case-detail-container">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <h1>病例详情</h1>
      </div>
      <div class="header-right">
        <el-tag :type="getRiskTagType(caseData.riskLevel)" size="large">{{ getRiskLabel(caseData.riskLevel) }}</el-tag>
        <el-tag :type="getStatusTagType(caseData.status)" size="large">{{ getStatusLabel(caseData.status) }}</el-tag>
      </div>
    </div>
    <div class="case-info">
      <div class="info-card">
        <h3>患者基本信息</h3>
        <div class="info-grid">
          <div class="info-item"><span class="info-label">病例编号：</span>{{ caseData.id }}</div>
          <div class="info-item"><span class="info-label">患者姓名：</span>{{ caseData.patientName }}</div>
          <div class="info-item"><span class="info-label">性别：</span>{{ caseData.gender === 'male' ? '男' : '女' }}</div>
          <div class="info-item"><span class="info-label">年龄：</span>{{ caseData.age }}岁</div>
          <div class="info-item"><span class="info-label">手机号：</span>{{ caseData.phone }}</div>
          <div class="info-item"><span class="info-label">提交时间：</span>{{ caseData.submitTime }}</div>
        </div>
      </div>
      <div class="info-card">
        <h3>症状信息</h3>
        <div class="info-grid">
          <div class="info-item"><span class="info-label">主要症状：</span>{{ caseData.symptoms }}</div>
          <div class="info-item"><span class="info-label">症状开始时间：</span>{{ caseData.symptomStartTime }}</div>
          <div class="info-item"><span class="info-label">持续时间：</span>{{ caseData.duration }}</div>
          <div class="info-item"><span class="info-label">疼痛等级：</span>{{ caseData.painLevel }}/10</div>
        </div>
        <div class="info-item full"><span class="info-label">症状描述：</span>{{ caseData.symptomDescription }}</div>
      </div>
      <div class="info-card">
        <h3>病史信息</h3>
        <div class="info-grid">
          <div class="info-item"><span class="info-label">既往病史：</span>{{ caseData.pastHistory }}</div>
          <div class="info-item"><span class="info-label">过敏史：</span>{{ caseData.allergy }}</div>
          <div class="info-item"><span class="info-label">吸烟史：</span>{{ caseData.smoking === 'yes' ? '是' : '否' }}</div>
          <div class="info-item"><span class="info-label">饮酒史：</span>{{ caseData.drinking === 'yes' ? '是' : '否' }}</div>
        </div>
        <div class="info-item full"><span class="info-label">用药史：</span>{{ caseData.medication }}</div>
        <div class="info-item full"><span class="info-label">家族病史：</span>{{ caseData.familyHistory }}</div>
      </div>
    </div>
    <div class="analysis-section">
      <div class="analysis-card">
        <div class="card-header">
          <h3>AI风险分析</h3>
          <el-tag type="info">模型版本：v1.2.0</el-tag>
        </div>
        <div class="analysis-content">
          <div class="risk-factors">
            <h4>红旗症状识别</h4>
            <div v-for="(factor, index) in caseData.riskFactors" :key="index" class="risk-factor">
              <el-icon class="risk-icon" :class="factor.level"><Warning /></el-icon>
              <div class="risk-info">
                <span class="risk-name">{{ factor.name }}</span>
                <span class="risk-desc">{{ factor.description }}</span>
              </div>
              <el-tag :type="getRiskTagType(factor.level)" size="small">{{ getRiskLabel(factor.level) }}</el-tag>
            </div>
          </div>
          <div class="ai-summary">
            <h4>AI分析摘要</h4>
            <p>{{ caseData.aiSummary }}</p>
          </div>
        </div>
      </div>
      <div class="analysis-card">
        <div class="card-header">
          <h3>症状时间线</h3>
        </div>
        <div class="timeline">
          <div v-for="(event, index) in caseData.timeline" :key="index" class="timeline-item">
            <div class="timeline-dot" :class="event.type"></div>
            <div class="timeline-content">
              <span class="timeline-time">{{ event.time }}</span>
              <span class="timeline-event">{{ event.event }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="guide-section">
      <div class="guide-card">
        <div class="card-header">
          <h3>相关指南推荐</h3>
          <el-button type="primary" size="small" @click="goToGuideSearch">查看更多</el-button>
        </div>
        <el-table :data="caseData.guides" border>
          <el-table-column prop="title" label="指南名称" />
          <el-table-column prop="matchScore" label="匹配度" width="100">
            <template #default="{ row }">
              <el-progress :percentage="row.matchScore" :color="getProgressColor(row.matchScore)" />
            </template>
          </el-table-column>
          <el-table-column prop="source" label="来源" width="120" />
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="viewGuide(row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div class="action-section" v-if="caseData.status === 'pending'">
      <h3>审核操作</h3>
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef">
        <el-form-item prop="reviewResult">
          <el-radio-group v-model="reviewForm.reviewResult">
            <el-radio label="approved">通过</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="reviewComment">
          <el-input v-model="reviewForm.reviewComment" type="textarea" :rows="4" placeholder="请填写审核意见" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="submitting" @click="submitReview">确认审核</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const reviewFormRef = ref(null)

const caseData = reactive({
  id: route.params.id || 'CASE-20260722001',
  patientName: '张三',
  gender: 'male',
  age: 65,
  phone: '138****1234',
  submitTime: '2026-07-22 14:30',
  symptoms: '胸闷、气短、心悸',
  symptomStartTime: '2026-07-22 08:00',
  duration: '3-7天',
  painLevel: 7,
  symptomDescription: '患者自述近一周出现胸闷症状，活动后加重，休息后缓解。伴有气短、心悸，夜间睡眠时需垫高枕头。无胸痛、无呼吸困难。',
  pastHistory: '高血压、冠心病',
  allergy: '青霉素过敏',
  smoking: 'yes',
  drinking: 'no',
  medication: '硝苯地平缓释片 10mg bid，阿司匹林肠溶片 100mg qd',
  familyHistory: '父亲有高血压病史，母亲有糖尿病病史',
  riskLevel: 'high',
  status: 'pending',
  riskFactors: [
    { name: '胸痛/胸闷', description: '患者描述胸闷症状，可能提示心血管疾病', level: 'high' },
    { name: '年龄因素', description: '65岁高龄，心血管疾病风险增加', level: 'medium' },
    { name: '既往病史', description: '有高血压、冠心病病史', level: 'high' },
    { name: '吸烟史', description: '长期吸烟，心血管疾病危险因素', level: 'medium' }
  ],
  aiSummary: '综合分析患者症状和病史，存在较高的心血管事件风险。建议优先安排心内科门诊就诊，完善心电图、心肌酶谱等检查。同时提醒患者避免剧烈活动，随身携带硝酸甘油等急救药物。',
  timeline: [
    { time: '7月22日 08:00', event: '开始出现胸闷症状', type: 'start' },
    { time: '7月22日 10:00', event: '症状加重，活动后明显', type: 'warning' },
    { time: '7月22日 12:00', event: '休息后症状缓解', type: 'info' },
    { time: '7月22日 14:30', event: '提交预问诊表单', type: 'submit' }
  ],
  guides: [
    { id: 'guide001', title: '中国急性冠状动脉综合征诊断与治疗指南', matchScore: 92, source: '中华医学会' },
    { id: 'guide002', title: '高血压防治指南', matchScore: 78, source: '国家心血管病中心' },
    { id: 'guide003', title: '冠心病患者长期管理专家共识', matchScore: 85, source: '中华心血管病杂志' }
  ]
})

const reviewForm = reactive({
  reviewResult: '',
  reviewComment: ''
})

const reviewRules = {
  reviewResult: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  reviewComment: [{ required: true, message: '请填写审核意见', trigger: 'blur' }]
}

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

const getProgressColor = (score) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const goBack = () => router.back()
const goToGuideSearch = () => router.push('/guideSearch')
const viewGuide = (id) => router.push(`/guideSearch?id=${id}`)

const submitReview = async () => {
  if (!reviewFormRef.value) return
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await new Promise(resolve => setTimeout(resolve, 1000))
        caseData.status = reviewForm.reviewResult
        ElMessage.success('审核完成')
      } catch {
        ElMessage.error('审核失败')
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.case-detail-container {
  padding-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
  }

  .header-right {
    display: flex;
    gap: 12px;
  }
}

.case-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  font-size: 14px;

  &.full {
    grid-column: span 2;
  }

  .info-label {
    width: 100px;
    color: #909399;
    flex-shrink: 0;
  }

  span:last-child {
    color: #303133;
  }
}

.analysis-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.analysis-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.risk-factors {
  h4 {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
    margin-bottom: 12px;
  }
}

.risk-factor {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.risk-icon {
  font-size: 18px;

  &.high { color: #f56c6c; }
  &.medium { color: #e6a23c; }
  &.low { color: #67c23a; }
}

.risk-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.risk-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.risk-desc {
  font-size: 13px;
  color: #909399;
}

.ai-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;

  h4 {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
    margin-bottom: 12px;
  }

  p {
    font-size: 14px;
    color: #303133;
    line-height: 1.8;
  }
}

.timeline {
  position: relative;
  padding-left: 24px;

  &::before {
    content: '';
    position: absolute;
    left: 7px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: #e4e7ed;
  }
}

.timeline-item {
  position: relative;
  margin-bottom: 20px;

  &:last-child { margin-bottom: 0; }
}

.timeline-dot {
  position: absolute;
  left: -20px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #409eff;

  &.start { background: #f56c6c; }
  &.warning { background: #e6a23c; }
  &.info { background: #409eff; }
  &.submit { background: #67c23a; }
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.timeline-time {
  font-size: 13px;
  color: #909399;
}

.timeline-event {
  font-size: 14px;
  color: #303133;
}

.guide-section {
  margin-bottom: 24px;
}

.guide-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.action-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }
}

:deep(.el-progress) {
  margin-top: 4px;
}
</style>