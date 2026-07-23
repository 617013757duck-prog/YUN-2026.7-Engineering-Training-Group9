const mockUsers = {
  admin: { username: 'admin', password: 'admin123', role: 'admin', realName: '管理员' },
  doctor: { username: 'doctor', password: 'doctor123', role: 'doctor', realName: '李医生' },
  patient: { username: 'patient', password: 'patient123', role: 'patient', realName: '张三' },
  follow: { username: 'follow', password: 'follow123', role: 'follow', realName: '随访员王' }
}

export const mockLogin = (data) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const user = mockUsers[data.username]
      if (user && user.password === data.password && user.role === data.role) {
        resolve({
          code: 200,
          message: '登录成功',
          data: {
            token: `token_${user.username}_${Date.now()}`,
            username: user.username,
            role: user.role,
            realName: user.realName
          }
        })
      } else {
        reject({
          code: 401,
          message: '用户名、密码或角色不正确'
        })
      }
    }, 500)
  })
}

export const mockGetUserInfo = (token) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const username = token.split('_')[1]
      const user = mockUsers[username]
      if (user) {
        resolve({
          code: 200,
          message: '获取成功',
          data: {
            username: user.username,
            role: user.role,
            realName: user.realName
          }
        })
      } else {
        resolve({
          code: 401,
          message: '用户不存在'
        })
      }
    }, 300)
  })
}