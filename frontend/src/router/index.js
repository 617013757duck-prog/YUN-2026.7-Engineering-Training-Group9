import { createRouter, createWebHistory } from 'vue-router'

const Layout = () => import('@/components/Layout.vue')

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
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { roles: ['patient', 'doctor', 'follow', 'admin'] }
      },
      {
        path: 'preConsult',
        name: 'PreConsult',
        component: () => import('@/views/预问诊/StepForm.vue'),
        meta: { roles: ['patient'] }
      },
      {
        path: 'caseList',
        name: 'CaseList',
        component: () => import('@/views/医师工作台/CaseList.vue'),
        meta: { roles: ['doctor', 'admin'] }
      },
      {
        path: 'caseDetail/:id',
        name: 'CaseDetail',
        component: () => import('@/views/医师工作台/CaseDetail.vue'),
        meta: { roles: ['doctor', 'admin'] }
      },
      {
        path: 'followUp',
        name: 'FollowUp',
        component: () => import('@/views/随访管理/FollowUp.vue'),
        meta: { roles: ['follow', 'admin'] }
      },
      {
        path: 'guideSearch',
        name: 'GuideSearch',
        component: () => import('@/views/指南检索/GuideSearch.vue'),
        meta: { roles: ['doctor', 'follow', 'admin'] }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/监控面板/Monitor.vue'),
        meta: { roles: ['admin', 'doctor'] }
      },
      {
        path: 'adminSetting',
        name: 'AdminSetting',
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
  
  if (to.meta.noAuth) {
    return next()
  }
  
  if (!token) {
    return next('/login')
  }
  
  if (to.meta.roles && !to.meta.roles.includes(userRole)) {
    return next('/dashboard')
  }
  
  next()
})

export default router