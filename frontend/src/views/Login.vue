<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>基层医疗安全型预问诊与随访平台</h1>
        <p>登录系统</p>
      </div>
      <el-form :model="loginForm" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="loginForm.role" placeholder="请选择角色">
            <el-option label="患者" value="patient" />
            <el-option label="医务人员" value="doctor" />
            <el-option label="随访人员" value="follow" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">
            登 录
          </el-button>
        </el-form-item>
        <div class="demo-accounts">
          <p>演示账号：</p>
          <ul>
            <li>管理员: admin / admin123</li>
            <li>医务人员: doctor / doctor123</li>
            <li>患者: patient / patient123</li>
            <li>随访人员: follow / follow123</li>
          </ul>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  role: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const mockUsers = {
  admin: { password: 'admin123', name: '管理员', role: 'admin' },
  doctor: { password: 'doctor123', name: '李医生', role: 'doctor' },
  patient: { password: 'patient123', name: '张三', role: 'patient' },
  follow: { password: 'follow123', name: '随访员小王', role: 'follow' }
}

const handleLogin = async () => {
  loading.value = true
  await new Promise(resolve => setTimeout(resolve, 500))
  
  const user = mockUsers[loginForm.username]
  if (user && user.password === loginForm.password && user.role === loginForm.role) {
    localStorage.setItem('token', 'mock-token-' + user.role)
    localStorage.setItem('username', user.name)
    localStorage.setItem('role', user.role)
    localStorage.setItem('realName', user.name)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } else {
    ElMessage.error('用户名、密码或角色不正确')
  }
  loading.value = false
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.demo-accounts {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #e4e7ed;
}

.demo-accounts p {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.demo-accounts ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.demo-accounts li {
  font-size: 12px;
  color: #606266;
  line-height: 1.8;
}
</style>