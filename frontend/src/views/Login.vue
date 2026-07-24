<template>
  <div class="login-container">
    <div class="background-decoration">
      <div class="deco-circle circle-1"></div>
      <div class="deco-circle circle-2"></div>
      <div class="deco-circle circle-3"></div>
      <div class="deco-line line-1"></div>
      <div class="deco-line line-2"></div>
      <div class="deco-blob blob-1"></div>
      <div class="deco-blob blob-2"></div>
    </div>
    
    <div class="login-box">
      <div class="login-header">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <svg width="48" height="48" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
              <circle cx="512" cy="512" r="416" fill="#409EFF"/>
              <circle cx="512" cy="512" r="290" fill="#fff"/>
              <circle cx="512" cy="512" r="185" fill="#409EFF"/>
              <circle cx="512" cy="512" r="92" fill="#fff"/>
            </svg>
          </div>
          <div class="logo-glow"></div>
        </div>
        <h1>医疗预问诊平台</h1>
        <p>安全、专业、高效的医疗服务</p>
      </div>
      
      <el-form ref="formRef" :model="loginForm" :rules="rules" label-width="0">
        <div class="form-group">
          <div class="input-wrapper">
            <el-icon class="input-icon"><User /></el-icon>
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              size="large"
              :class="{ 'has-focus': focusState.username }"
              @focus="focusState.username = true"
              @blur="focusState.username = false"
            />
          </div>
        </div>
        
        <div class="form-group">
          <div class="input-wrapper">
            <el-icon class="input-icon"><Lock /></el-icon>
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large"
              show-password
              :class="{ 'has-focus': focusState.password }"
              @focus="focusState.password = true"
              @blur="focusState.password = false"
            />
          </div>
        </div>
        
        <div class="form-group">
          <div class="input-wrapper">
            <el-icon class="input-icon"><SetUp /></el-icon>
            <el-select 
              v-model="loginForm.role" 
              placeholder="请选择角色" 
              size="large"
              :class="{ 'has-focus': focusState.role }"
              @focus="focusState.role = true"
              @blur="focusState.role = false"
            >
              <el-option label="患者" value="patient" />
              <el-option label="医务人员" value="doctor" />
              <el-option label="随访人员" value="follow" />
              <el-option label="管理员" value="admin" />
            </el-select>
          </div>
        </div>
        
        <div class="form-options">
          <el-checkbox v-model="rememberMe" border>记住密码</el-checkbox>
          <el-button type="text" @click="showForgotPassword" class="forgot-btn">忘记密码？</el-button>
        </div>
        
        <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">
          <span>登 录</span>
          <el-icon class="login-arrow"><ArrowRight /></el-icon>
        </el-button>
      </el-form>
      
      <div class="demo-section">
        <div class="section-header">
          <div class="section-icon">
            <el-icon><InfoFilled /></el-icon>
          </div>
          <span>快速体验 - 演示账号</span>
        </div>
        <div class="account-grid">
          <div 
            v-for="account in demoAccounts" 
            :key="account.username" 
            class="account-card"
            @click="fillAccount(account)"
          >
            <div class="account-avatar" :class="account.role">
              <el-icon><component :is="getRoleIcon(account.role)" /></el-icon>
            </div>
            <div class="account-info">
              <p class="account-name">{{ account.name }}</p>
              <p class="account-role">{{ getRoleLabel(account.role) }}</p>
            </div>
            <div class="account-creds">
              <span class="cred-item">{{ account.username }}</span>
              <span class="cred-divider">/</span>
              <span class="cred-item">{{ account.password }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <el-dialog v-model="showForgotDialog" title="忘记密码" width="400px" :show-close="false">
      <div class="forgot-icon">
        <el-icon><Key /></el-icon>
      </div>
      <p class="forgot-tip">请输入您的用户名和绑定的手机号，我们将发送验证码到您的手机。</p>
      <el-form :model="forgotForm" :rules="forgotRules" label-width="0" class="forgot-form">
        <div class="form-group">
          <div class="input-wrapper">
            <el-icon class="input-icon"><User /></el-icon>
            <el-input v-model="forgotForm.username" placeholder="请输入用户名" size="large" />
          </div>
        </div>
        <div class="form-group">
          <div class="input-wrapper">
            <el-icon class="input-icon"><Phone /></el-icon>
            <el-input v-model="forgotForm.phone" placeholder="请输入绑定的手机号" size="large" />
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showForgotDialog = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="submitForgot">获取验证码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled, User, Document, Calendar, SetUp, Lock, ArrowRight, Key, Phone } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const rememberMe = ref(false)
const showForgotDialog = ref(false)
const formRef = ref(null)

const focusState = reactive({
  username: false,
  password: false,
  role: false
})

const loginForm = reactive({
  username: '',
  password: '',
  role: ''
})

const forgotForm = reactive({
  username: '',
  phone: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const forgotRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const demoAccounts = [
  { username: 'admin', password: 'admin123', role: 'admin', name: '管理员' },
  { username: 'doctor', password: 'doctor123', role: 'doctor', name: '李医生' },
  { username: 'patient', password: 'patient123', role: 'patient', name: '张三' },
  { username: 'follow', password: 'follow123', role: 'follow', name: '随访员王' }
]

const getRoleIcon = (role) => {
  const icons = { admin: SetUp, doctor: Document, patient: User, follow: Calendar }
  return icons[role] || User
}

const getRoleLabel = (role) => {
  const labels = { admin: '管理员', doctor: '医务人员', patient: '患者', follow: '随访人员' }
  return labels[role] || role
}

const fillAccount = (account) => {
  loginForm.username = account.username
  loginForm.password = account.password
  loginForm.role = account.role
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    await new Promise(resolve => setTimeout(resolve, 800))

    const user = demoAccounts.find(a => a.username === loginForm.username)
    if (user && user.password === loginForm.password && user.role === loginForm.role) {
      // 更新localStorage
      localStorage.setItem('token', 'mock-token-' + user.role)
      localStorage.setItem('username', user.name)
      localStorage.setItem('role', user.role)
      localStorage.setItem('realName', user.name)

      // 同步更新userStore的响应式状态，修复退出后重新登录侧边栏卡死的bug
      userStore.token = 'mock-token-' + user.role
      userStore.username = user.name
      userStore.role = user.role
      userStore.realName = user.name

      if (rememberMe.value) {
        localStorage.setItem('rememberMe', 'true')
        localStorage.setItem('savedUsername', loginForm.username)
        localStorage.setItem('savedPassword', loginForm.password)
        localStorage.setItem('savedRole', loginForm.role)
      } else {
        localStorage.removeItem('rememberMe')
        localStorage.removeItem('savedUsername')
        localStorage.removeItem('savedPassword')
        localStorage.removeItem('savedRole')
      }

      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error('用户名、密码或角色不正确')
    }
    loading.value = false
  })
}

const showForgotPassword = () => {
  showForgotDialog.value = true
}

const submitForgot = () => {
  showForgotDialog.value = false
  ElMessage.success('密码重置链接已发送到您的手机')
}

onMounted(() => {
  const saved = localStorage.getItem('rememberMe')
  if (saved === 'true') {
    loginForm.username = localStorage.getItem('savedUsername') || ''
    loginForm.password = localStorage.getItem('savedPassword') || ''
    loginForm.role = localStorage.getItem('savedRole') || ''
    rememberMe.value = true
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.circle-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  top: -200px;
  right: -100px;
  animation: float 15s ease-in-out infinite;
}

.circle-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  bottom: -150px;
  left: -80px;
  animation: float 12s ease-in-out infinite reverse;
}

.circle-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  top: 50%;
  left: 10%;
  animation: float 18s ease-in-out infinite;
}

.deco-line {
  position: absolute;
  height: 2px;
  background: rgba(255, 255, 255, 0.05);
}

.line-1 {
  width: 400px;
  top: 20%;
  left: -100px;
  transform: rotate(15deg);
}

.line-2 {
  width: 300px;
  bottom: 30%;
  right: -50px;
  transform: rotate(-20deg);
}

.deco-blob {
  position: absolute;
  border-radius: 50% 40% 60% 50%;
  opacity: 0.1;
}

.blob-1 {
  width: 250px;
  height: 250px;
  background: #409eff;
  top: 40%;
  right: 20%;
  animation: blob 8s ease-in-out infinite;
}

.blob-2 {
  width: 180px;
  height: 180px;
  background: #e6a23c;
  bottom: 40%;
  left: 30%;
  animation: blob 10s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

@keyframes blob {
  0%, 100% { transform: rotate(0deg) scale(1); border-radius: 50% 40% 60% 50%; }
  25% { transform: rotate(90deg) scale(1.1); border-radius: 40% 60% 50% 50%; }
  50% { transform: rotate(180deg) scale(0.95); border-radius: 60% 50% 50% 40%; }
  75% { transform: rotate(270deg) scale(1.05); border-radius: 50% 50% 40% 60%; }
}

.login-box {
  width: 460px;
  padding: 56px 48px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4);
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
  }
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.4) 0%, transparent 70%);
  animation: glow 3s ease-in-out infinite;
}

@keyframes glow {
  0%, 100% { opacity: 0.5; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.1); }
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.7) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-header p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  letter-spacing: 1px;
}

.form-group {
  margin-bottom: 20px;
}

.input-wrapper {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  
  &:focus-within,
  .has-focus & {
    border-color: #409eff;
    background: rgba(255, 255, 255, 0.08);
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  }
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(255, 255, 255, 0.5);
  font-size: 18px;
  z-index: 1;
}

:deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  
  input, select {
    color: #fff !important;
    padding-left: 48px !important;
    font-size: 15px;
  }
  
  .el-input__placeholder {
    color: rgba(255, 255, 255, 0.4) !important;
  }
  
  .el-select__caret {
    color: rgba(255, 255, 255, 0.6) !important;
  }
}

:deep(.el-input__inner) {
  background: transparent !important;
  border: none !important;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 4px;
}

:deep(.el-checkbox__label) {
  color: rgba(255, 255, 255, 0.7) !important;
  font-size: 14px;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #409eff !important;
  border-color: #409eff !important;
}

:deep(.el-checkbox__inner) {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
}

.forgot-btn {
  color: rgba(255, 255, 255, 0.6) !important;
  font-size: 14px;
  transition: color 0.2s;
  
  &:hover {
    color: #fff !important;
  }
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
  }
  
  &:active:not(:disabled) {
    transform: translateY(0);
  }
  
  .login-arrow {
    transition: transform 0.3s ease;
  }
  
  &:hover:not(:disabled) .login-arrow {
    transform: translateX(4px);
  }
}

.demo-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.section-icon {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 12px;
}

.account-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.account-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 1px solid rgba(255, 255, 255, 0.05);
  
  &:hover {
    background: rgba(64, 158, 255, 0.15);
    border-color: rgba(64, 158, 255, 0.3);
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  }
}

.account-avatar {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  margin-bottom: 12px;
  
  &.admin { background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%); }
  &.doctor { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); }
  &.patient { background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%); }
  &.follow { background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%); }
}

.account-info {
  margin-bottom: 8px;
  
  .account-name {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 2px;
  }
  
  .account-role {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.5);
  }
}

.account-creds {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  
  .cred-item {
    font-family: 'SF Mono', Monaco, monospace;
    background: rgba(255, 255, 255, 0.1);
    padding: 3px 8px;
    border-radius: 6px;
    color: rgba(255, 255, 255, 0.7);
  }
  
  .cred-divider {
    color: rgba(255, 255, 255, 0.2);
  }
}

:deep(.el-dialog) {
  background: rgba(26, 26, 46, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 16px !important;
}

:deep(.el-dialog__title) {
  color: #fff !important;
  font-size: 18px !important;
  font-weight: 600 !important;
}

.forgot-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: rgba(64, 158, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 28px;
}

.forgot-tip {
  text-align: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin-bottom: 24px;
}

.forgot-form {
  padding: 0 20px;
}

.cancel-btn {
  color: rgba(255, 255, 255, 0.7) !important;
}

@media (max-width: 500px) {
  .login-box {
    width: 100%;
    padding: 40px 28px;
  }
  
  .account-grid {
    grid-template-columns: 1fr;
  }
  
  .login-header h1 {
    font-size: 24px;
  }
}
</style>