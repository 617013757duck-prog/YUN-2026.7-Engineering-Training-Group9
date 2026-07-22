import { defineStore } from 'pinia'
import { ref } from 'vue'

const mockUsers = {
  admin: { username: 'admin', password: 'admin123', role: 'admin', realName: '管理员' },
  doctor: { username: 'doctor', password: 'doctor123', role: 'doctor', realName: '张医生' },
  patient: { username: 'patient', password: 'patient123', role: 'patient', realName: '李患者' },
  follow: { username: 'follow', password: 'follow123', role: 'follow', realName: '王随访' }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const realName = ref(localStorage.getItem('realName') || '')

  const login = async (form) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        const user = mockUsers[form.username]
        if (user && user.password === form.password && user.role === form.role) {
          const mockToken = 'Bearer ' + Math.random().toString(36).substring(2)
          token.value = mockToken
          username.value = user.username
          role.value = user.role
          realName.value = user.realName
          
          localStorage.setItem('token', mockToken)
          localStorage.setItem('username', user.username)
          localStorage.setItem('role', user.role)
          localStorage.setItem('realName', user.realName)
          
          resolve({ success: true, message: '登录成功' })
        } else {
          resolve({ success: false, message: '用户名、密码或角色错误' })
        }
      }, 500)
    })
  }

  const logout = () => {
    token.value = ''
    username.value = ''
    role.value = ''
    realName.value = ''
    
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    localStorage.removeItem('realName')
  }

  const isAuthenticated = () => {
    return !!token.value
  }

  return {
    token,
    username,
    role,
    realName,
    login,
    logout,
    isAuthenticated
  }
})