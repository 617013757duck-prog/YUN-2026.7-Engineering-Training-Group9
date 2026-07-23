<template>
  <div class="layout-container">
    <aside class="sidebar">
      <div class="sidebar-header">
        <el-icon size="28" class="logo-icon">User</el-icon>
        <span class="logo-text">医疗随访平台</span>
      </div>
      
      <el-menu 
        :default-active="activeMenu" 
        class="sidebar-menu"
        background-color="#1a1a2e"
        text-color="#b8c5d6"
        active-text-color="#fff"
        router
      >
        <el-menu-item v-for="item in menuList" :key="item.path" :index="item.path">
          <el-icon>{{ item.icon }}</el-icon>
          <span>{{ item.name }}</span>
        </el-menu-item>
        
        <el-sub-menu v-for="item in subMenuList" :key="item.path" :index="item.path">
          <template #title>
            <el-icon>{{ item.icon }}</el-icon>
            <span>{{ item.name }}</span>
          </template>
          <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
            <span>{{ child.name }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>
    
    <main class="main-content">
      <header class="top-header">
        <div class="header-left">
          <h2>{{ currentPageName }}</h2>
        </div>
        <div class="header-right">
          <div class="user-info">
            <span>{{ realName }}</span>
            <span class="role-tag">{{ roleName }}</span>
          </div>
          <el-button text @click="handleLogout">
            <el-icon>TurnOff</el-icon>
            <span>退出登录</span>
          </el-button>
        </div>
      </header>
      
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const realName = computed(() => userStore.realName || localStorage.getItem('realName') || '')

const roleMap = {
  patient: '患者',
  doctor: '医务人员',
  follow: '随访人员',
  admin: '管理员'
}

const roleName = computed(() => {
  const role = userStore.role || localStorage.getItem('role') || ''
  return roleMap[role] || ''
})

const activeMenu = computed(() => {
  return route.path
})

const currentPageName = computed(() => {
  const routeMap = {
    '/dashboard': '数据看板',
    '/preConsult': '预问诊表单',
    '/caseList': '病例待审队列',
    '/caseDetail': '病例详情',
    '/followUp': '慢病随访管理',
    '/guideSearch': '临床指南检索',
    '/monitor': '高风险监控面板',
    '/adminSetting': '系统配置管理'
  }
  return routeMap[route.path] || '数据看板'
})

const role = computed(() => userStore.role || localStorage.getItem('role') || '')

const menuList = computed(() => {
  const baseMenu = [
    { path: '/dashboard', name: '数据看板', icon: 'DataBoard' }
  ]
  
  const roleMenus = {
    patient: [
      { path: '/preConsult', name: '预问诊表单', icon: 'Plus' }
    ],
    doctor: [
      { path: '/caseList', name: '待审队列', icon: 'List' },
      { path: '/guideSearch', name: '指南检索', icon: 'Document' },
      { path: '/monitor', name: '监控面板', icon: 'Warning' }
    ],
    follow: [
      { path: '/followUp', name: '随访管理', icon: 'Calendar' },
      { path: '/guideSearch', name: '指南检索', icon: 'Document' }
    ],
    admin: [
      { path: '/caseList', name: '待审队列', icon: 'List' },
      { path: '/followUp', name: '随访管理', icon: 'Calendar' },
      { path: '/guideSearch', name: '指南检索', icon: 'Document' },
      { path: '/monitor', name: '监控面板', icon: 'Warning' },
      { path: '/adminSetting', name: '系统配置', icon: 'SetUp' }
    ]
  }
  
  return [...baseMenu, ...(roleMenus[role.value] || [])]
})

const subMenuList = computed(() => {
  return []
})

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  width: 220px;
  background: #1a1a2e;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;

  .sidebar-header {
    display: flex;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .logo-icon {
      color: #409eff;
      margin-right: 10px;
    }

    .logo-text {
      color: #fff;
      font-size: 16px;
      font-weight: 600;
    }
  }

  .sidebar-menu {
    flex: 1;
    border-right: none;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 48px;
      line-height: 48px;
      margin: 4px 8px;
      border-radius: 8px;
      transition: all 0.2s ease;

      &:hover {
        background: rgba(255, 255, 255, 0.08) !important;
      }
    }

    :deep(.el-menu-item.is-active) {
      background: #409eff !important;
    }

    :deep(.el-menu-item svg),
    :deep(.el-sub-menu__title svg) {
      font-size: 18px;
    }
  }
}

.main-content {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.top-header {
  background: #fff;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 50;

  .header-left h2 {
    margin: 0;
    font-size: 18px;
    color: #303133;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;

      span {
        color: #606266;
        font-size: 14px;
      }

      .role-tag {
        padding: 2px 8px;
        background: #ecf5ff;
        color: #409eff;
        border-radius: 4px;
        font-size: 12px;
      }
    }
  }
}

.content-wrapper {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>