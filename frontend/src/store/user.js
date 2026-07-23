import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo } from '../api'
import { mockLogin, mockGetUserInfo } from '../api/mock'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const realName = ref(localStorage.getItem('realName') || '')

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const loginAction = async (data) => {
    try {
      const res = await login(data)
      if (res.data) {
        setToken(res.data.token)
        username.value = res.data.username
        role.value = res.data.role
        realName.value = res.data.realName || res.data.username
        localStorage.setItem('username', res.data.username)
        localStorage.setItem('role', res.data.role)
        localStorage.setItem('realName', res.data.realName || res.data.username)
      }
      return res
    } catch {
      const res = await mockLogin(data)
      if (res.data) {
        setToken(res.data.token)
        username.value = res.data.username
        role.value = res.data.role
        realName.value = res.data.realName || res.data.username
        localStorage.setItem('username', res.data.username)
        localStorage.setItem('role', res.data.role)
        localStorage.setItem('realName', res.data.realName || res.data.username)
      }
      return res
    }
  }

  const getUserInfoAction = async () => {
    try {
      const res = await getUserInfo()
      if (res.data) {
        username.value = res.data.username
        role.value = res.data.role
        realName.value = res.data.realName || res.data.username
      }
      return res
    } catch {
      const res = await mockGetUserInfo(token.value)
      if (res.data) {
        username.value = res.data.username
        role.value = res.data.role
        realName.value = res.data.realName || res.data.username
      }
      return res
    }
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

  const roleName = () => {
    const names = {
      patient: '患者',
      doctor: '医务人员',
      follow: '随访人员',
      admin: '管理员'
    }
    return names[role.value] || role.value
  }

  return {
    token,
    username,
    role,
    realName,
    setToken,
    loginAction,
    getUserInfoAction,
    logout,
    roleName
  }
})