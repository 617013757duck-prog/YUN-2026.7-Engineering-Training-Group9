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
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true, roles: ['patient', 'doctor', 'follow', 'admin'] }
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