<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <el-icon class="logo-icon">
            <Hospital />
          </el-icon>
          <h1>基层医疗安全型预问诊与随访平台</h1>
        </div>
        <p>安全 · 高效 · 便捷</p>
      </div>
      
      <el-form ref="loginForm" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="role">
          <el-select 
            v-model="form.role" 
            placeholder="请选择角色"
            size="large"
          >
            <el-option label="患者" value="patient" />
            <el-option label="医务人员" value="doctor" />
            <el-option label="随访人员" value="follow" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            style="width: 100%"
            @click="handleLogin"
            :loading="loading"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span>测试账号：</span>
        <span class="test-account">admin/admin123</span>
        <span class="test-account">doctor/doctor123</span>
        <span class="test-account">patient/patient123</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Hospital } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginForm = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  role: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const handleLogin = async () => {
  if (!loginForm.value) return
  
  try {
    await loginForm.value.validate()
    loading.value = true
    
    const result = await userStore.login(form)
    
    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录验证失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 10px;
    
    .logo-icon {
      font-size: 36px;
      color: #409eff;
    }
    
    h1 {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
  
  p {
    color: #909399;
    margin: 0;
    font-size: 14px;
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #909399;
  font-size: 12px;
  
  .test-account {
    display: inline-block;
    margin: 0 8px;
    padding: 2px 8px;
    background: #f5f7fa;
    border-radius: 4px;
    color: #606266;
  }
}
</style>