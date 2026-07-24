<template>
  <div class="pre-consult-container">
    <div class="page-header">
      <h1>分步骤预问诊</h1>
      <p class="page-desc">请按照步骤填写您的健康信息，以便我们为您提供更好的服务</p>
    </div>
    <div class="step-bar">
      <div class="step-item" :class="{ active: currentStep >= 1, done: currentStep > 1 }">
        <div class="step-num">1</div>
        <span>基本信息</span>
      </div>
      <div class="step-line" :class="{ active: currentStep > 1 }"></div>
      <div class="step-item" :class="{ active: currentStep >= 2, done: currentStep > 2 }">
        <div class="step-num">2</div>
        <span>症状描述</span>
      </div>
      <div class="step-line" :class="{ active: currentStep > 2 }"></div>
      <div class="step-item" :class="{ active: currentStep >= 3, done: currentStep > 3 }">
        <div class="step-num">3</div>
        <span>病史信息</span>
      </div>
      <div class="step-line" :class="{ active: currentStep > 3 }"></div>
      <div class="step-item" :class="{ active: currentStep >= 4 }">
        <div class="step-num">4</div>
        <span>确认提交</span>
      </div>
    </div>
    <div class="form-card">
      <transition name="fade" mode="out-in">
        <div v-if="currentStep === 1" key="step1">
          <el-form :model="form.step1" :rules="step1Rules" ref="step1Ref" label-width="100px">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.step1.name" placeholder="请输入您的姓名" />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.step1.gender">
                <el-radio label="male">男</el-radio>
                <el-radio label="female">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.step1.age" :min="1" :max="150" placeholder="请输入年龄" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.step1.phone" placeholder="请输入手机号码" />
            </el-form-item>
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.step1.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-form>
        </div>
        <div v-else-if="currentStep === 2" key="step2">
          <el-form :model="form.step2" :rules="step2Rules" ref="step2Ref" label-width="100px">
            <el-form-item label="主要症状" prop="mainSymptom">
              <el-select v-model="form.step2.mainSymptom" placeholder="请选择主要症状" multiple collapse-tags>
                <el-option label="发热" value="fever" />
                <el-option label="咳嗽" value="cough" />
                <el-option label="头痛" value="headache" />
                <el-option label="胸闷" value="chestPain" />
                <el-option label="腹痛" value="abdominalPain" />
                <el-option label="乏力" value="fatigue" />
                <el-option label="呼吸困难" value="dyspnea" />
                <el-option label="恶心呕吐" value="nausea" />
                <el-option label="头晕" value="dizziness" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="症状开始时间" prop="startTime">
              <el-date-picker v-model="form.step2.startTime" type="datetime" placeholder="请选择症状开始时间" />
            </el-form-item>
            <el-form-item label="症状持续时间" prop="duration">
              <el-select v-model="form.step2.duration" placeholder="请选择持续时间">
                <el-option label="小于1天" value="less1" />
                <el-option label="1-3天" value="1-3" />
                <el-option label="3-7天" value="3-7" />
                <el-option label="1-2周" value="1-2w" />
                <el-option label="2周以上" value="more2w" />
              </el-select>
            </el-form-item>
            <el-form-item label="症状描述" prop="description">
              <el-input v-model="form.step2.description" type="textarea" :rows="4" placeholder="请详细描述您的症状" />
            </el-form-item>
            <el-form-item label="疼痛等级">
              <div class="pain-scale">
                <div v-for="level in 10" :key="level" class="pain-item" :class="{ active: form.step2.painLevel >= level }" @click="form.step2.painLevel = level">
                  {{ level }}
                </div>
              </div>
              <div class="pain-labels">
                <span>无痛</span>
                <span>轻微</span>
                <span>中度</span>
                <span>剧烈</span>
              </div>
            </el-form-item>
          </el-form>
        </div>
        <div v-else-if="currentStep === 3" key="step3">
          <el-form :model="form.step3" :rules="step3Rules" ref="step3Ref" label-width="100px">
            <el-form-item label="既往病史" prop="pastHistory">
              <el-select v-model="form.step3.pastHistory" placeholder="请选择既往病史" multiple collapse-tags>
                <el-option label="高血压" value="hypertension" />
                <el-option label="糖尿病" value="diabetes" />
                <el-option label="冠心病" value="chd" />
                <el-option label="哮喘" value="asthma" />
                <el-option label="慢性肾病" value="ckd" />
                <el-option label="无" value="none" />
              </el-select>
            </el-form-item>
            <el-form-item label="过敏史" prop="allergy">
              <el-input v-model="form.step3.allergy" placeholder="请输入过敏史，无则填'无'" />
            </el-form-item>
            <el-form-item label="用药史" prop="medication">
              <el-input v-model="form.step3.medication" type="textarea" :rows="3" placeholder="请输入近期用药情况" />
            </el-form-item>
            <el-form-item label="吸烟史" prop="smoking">
              <el-radio-group v-model="form.step3.smoking">
                <el-radio label="yes">是</el-radio>
                <el-radio label="no">否</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="饮酒史" prop="drinking">
              <el-radio-group v-model="form.step3.drinking">
                <el-radio label="yes">是</el-radio>
                <el-radio label="no">否</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="家族病史" prop="familyHistory">
              <el-input v-model="form.step3.familyHistory" type="textarea" :rows="3" placeholder="请输入家族病史情况" />
            </el-form-item>
          </el-form>
        </div>
        <div v-else-if="currentStep === 4" key="step4">
          <div class="summary-card">
            <h3>信息确认</h3>
            <div class="summary-section">
              <h4>基本信息</h4>
              <div class="summary-row"><span>姓名：</span>{{ form.step1.name }}</div>
              <div class="summary-row"><span>性别：</span>{{ form.step1.gender === 'male' ? '男' : '女' }}</div>
              <div class="summary-row"><span>年龄：</span>{{ form.step1.age }}岁</div>
              <div class="summary-row"><span>手机号：</span>{{ form.step1.phone }}</div>
            </div>
            <div class="summary-section">
              <h4>症状信息</h4>
              <div class="summary-row"><span>主要症状：</span>{{ formatSymptoms(form.step2.mainSymptom) }}</div>
              <div class="summary-row"><span>开始时间：</span>{{ formatDateTime(form.step2.startTime) }}</div>
              <div class="summary-row"><span>持续时间：</span>{{ formatDuration(form.step2.duration) }}</div>
              <div class="summary-row"><span>症状描述：</span>{{ form.step2.description }}</div>
            </div>
            <div class="summary-section">
              <h4>病史信息</h4>
              <div class="summary-row"><span>既往病史：</span>{{ formatPastHistory(form.step3.pastHistory) }}</div>
              <div class="summary-row"><span>过敏史：</span>{{ form.step3.allergy }}</div>
              <div class="summary-row"><span>用药史：</span>{{ form.step3.medication }}</div>
            </div>
            <el-form-item>
              <el-checkbox v-model="agreed">我已阅读并同意隐私政策和服务条款</el-checkbox>
            </el-form-item>
          </div>
        </div>
      </transition>
      <div class="form-actions">
        <el-button v-if="currentStep > 1" @click="prevStep">上一步</el-button>
        <el-button v-if="currentStep < 4" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="currentStep === 4" type="success" :loading="submitting" @click="submitForm">提交</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const currentStep = ref(1)
const submitting = ref(false)
const agreed = ref(false)

const step1Ref = ref(null)
const step2Ref = ref(null)
const step3Ref = ref(null)

const form = reactive({
  step1: { name: '', gender: '', age: null, phone: '', idCard: '' },
  step2: { mainSymptom: [], startTime: null, duration: '', description: '', painLevel: 0 },
  step3: { pastHistory: [], allergy: '', medication: '', smoking: '', drinking: '', familyHistory: '' }
})

const step1Rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'change' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const step2Rules = {
  mainSymptom: [{ required: true, message: '请选择主要症状', trigger: 'change' }],
  description: [{ required: true, message: '请描述症状', trigger: 'blur' }]
}

const step3Rules = {
  pastHistory: [{ required: true, message: '请选择既往病史', trigger: 'change' }],
  allergy: [{ required: true, message: '请填写过敏史', trigger: 'blur' }]
}

const nextStep = async () => {
  if (currentStep.value === 1 && step1Ref.value) {
    await step1Ref.value.validate((valid) => {
      if (valid) currentStep.value++
    })
  } else if (currentStep.value === 2 && step2Ref.value) {
    await step2Ref.value.validate((valid) => {
      if (valid) currentStep.value++
    })
  } else if (currentStep.value === 3 && step3Ref.value) {
    await step3Ref.value.validate((valid) => {
      if (valid) currentStep.value++
    })
  }
}

const prevStep = () => {
  currentStep.value--
}

const submitForm = async () => {
  if (!agreed.value) {
    ElMessage.error('请先同意隐私政策和服务条款')
    return
  }
  submitting.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    ElMessage.success('提交成功！您的预问诊信息已收到')
    router.push('/dashboard')
  } catch {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const formatSymptoms = (symptoms) => {
  const map = { fever: '发热', cough: '咳嗽', headache: '头痛', chestPain: '胸闷', abdominalPain: '腹痛', fatigue: '乏力', dyspnea: '呼吸困难', nausea: '恶心呕吐', dizziness: '头晕', other: '其他' }
  return symptoms.map(s => map[s] || s).join('、')
}

const formatDateTime = (date) => {
  return date ? new Date(date).toLocaleString() : '-'
}

const formatDuration = (d) => {
  const map = { 'less1': '小于1天', '1-3': '1-3天', '3-7': '3-7天', '1-2w': '1-2周', 'more2w': '2周以上' }
  return map[d] || d
}

const formatPastHistory = (history) => {
  const map = { hypertension: '高血压', diabetes: '糖尿病', chd: '冠心病', asthma: '哮喘', ckd: '慢性肾病', none: '无' }
  return history.map(h => map[h] || h).join('、')
}
</script>

<style lang="scss" scoped>
.pre-consult-container {
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

.step-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 1;

  .step-num {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #e4e7ed;
    color: #909399;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    transition: all 0.3s;
  }

  span {
    font-size: 14px;
    color: #909399;
  }

  &.active {
    .step-num {
      background: #409eff;
      color: #fff;
    }
    span { color: #409eff; }
  }

  &.done {
    .step-num {
      background: #67c23a;
      color: #fff;
    }
    span { color: #67c23a; }
  }
}

.step-line {
  width: 80px;
  height: 4px;
  background: #e4e7ed;
  margin: 0 8px;
  transition: all 0.3s;

  &.active { background: #409eff; }
}

.form-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.pain-scale {
  display: flex;
  gap: 8px;
}

.pain-item {
  flex: 1;
  height: 40px;
  border-radius: 8px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #606266;

  &.active {
    background: #f56c6c;
    color: #fff;
  }
}

.pain-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.summary-card {
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
  }
}

.summary-section {
  margin-bottom: 20px;

  h4 {
    font-size: 14px;
    font-weight: 500;
    color: #606266;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
  }
}

.summary-row {
  display: flex;
  padding: 8px 0;
  font-size: 14px;
  color: #303133;

  span:first-child {
    width: 100px;
    color: #909399;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}
</style>