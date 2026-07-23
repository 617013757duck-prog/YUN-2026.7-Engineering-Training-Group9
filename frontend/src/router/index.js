import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { roles: ['patient', 'doctor', 'follow', 'admin'] }
      },
      {
        path: '/preConsult',
        name: '预问诊表单',
        component: () => import('@/views/预问诊/StepForm.vue'),
        meta: { roles: ['patient'] }
      },
      {
        path: '/caseList',
        name: '待审队列',
        component: () => import('@/views/医师工作台/CaseList.vue'),
        meta: { roles: ['doctor', 'admin'] }
      },
      {
        path: '/caseDetail/:id',
        name: '病例详情',
        component: () => import('@/views/医师工作台/CaseDetail.vue'),
        meta: { roles: ['doctor', 'admin'] }
      },
      {
        path: '/followUp',
        name: '慢病随访管理',
        component: () => import('@/views/随访管理/FollowUp.vue'),
        meta: { roles: ['follow', 'admin'] }
      },
      {
        path: '/guideSearch',
        name: '临床指南检索',
        component: () => import('@/views/指南检索/GuideSearch.vue'),
        meta: { roles: ['doctor', 'follow', 'admin'] }
      },
      {
        path: '/monitor',
        name: '高风险监控面板',
        component: () => import('@/views/监控面板/Monitor.vue'),
        meta: { roles: ['admin', 'doctor'] }
      },
      {
        path: '/adminSetting',
        name: '系统配置管理',
        component: () => import('@/views/系统管理/AdminSetting.vue'),
        meta: { roles: ['admin'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('role')

  if (to.meta.noAuth) return next()
  if (!token) return next('/login')
  if (to.meta.roles && !to.meta.roles.includes(userRole)) {
    return next('/dashboard')
  }
  next()
})

export default router