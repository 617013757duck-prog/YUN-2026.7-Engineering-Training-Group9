<template>
  <div class="layout-container">
    <header class="layout-header">
      <div class="header-left">
        <div class="logo">
          <svg width="32" height="32" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <path fill="#409EFF" d="M512 96C264.58 96 64 296.58 64 544s200.58 448 448 448 448-200.58 448-448S759.42 96 512 96z"/>
            <path fill="#fff" d="M512 192c160.71 0 290.93 130.22 290.93 290.93S672.71 773.86 512 773.86 221.07 643.64 221.07 482.93 351.29 192 512 192z"/>
            <path fill="#409EFF" d="M512 288c101.71 0 184.93 83.22 184.93 184.93S613.71 657.86 512 657.86 327.07 574.64 327.07 472.93 410.29 288 512 288z"/>
            <path fill="#fff" d="M512 384c53.02 0 96 42.98 96 96s-42.98 96-96 96-96-42.98-96-96 42.98-96 96-96z"/>
          </svg>
          <span class="logo-text">医疗预问诊平台</span>
        </div>
      </div>
      <div class="header-right">
        <el-dropdown>
          <span class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ userStore.realName }}</span>
            <span class="role-tag">{{ userStore.roleName() }}</span>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="logout">
                <el-icon><TurnOff /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <aside class="layout-sidebar">
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        :collapse="isCollapse"
      >
        <template v-for="item in menuList" :key="item.path">
          <el-menu-item v-if="!item.children" :index="item.path" :disabled="!hasRole(item.roles)">
            <component :is="item.icon" />
            <template #title>{{ item.name }}</template>
          </el-menu-item>
          <el-sub-menu v-else :index="item.path">
            <template #title>
              <component :is="item.icon" />
              <span>{{ item.name }}</span>
            </template>
            <el-menu-item
              v-for="child in item.children"
              :key="child.path"
              :index="child.path"
              :disabled="!hasRole(child.roles)"
            >
              {{ child.name }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </aside>
    <main class="layout-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import {
  User,
  ArrowDown,
  TurnOff,
  DataBoard,
  Files,
  FirstAidKit,
  Calendar,
  Reading,
  WarningFilled,
  SetUp
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const isCollapse = computed(() => false)

const menuList = [
  { path: '/dashboard', name: '数据看板', icon: DataBoard, roles: ['patient', 'doctor', 'follow', 'admin'] },
  { path: '/preConsult', name: '预问诊表单', icon: FirstAidKit, roles: ['patient'] },
  {
    path: '/case',
    name: '病例管理',
    icon: Files,
    roles: ['doctor', 'admin'],
    children: [
      { path: '/caseList', name: '待审队列', roles: ['doctor', 'admin'] },
      { path: '/caseDetail/:id', name: '病例详情', roles: ['doctor', 'admin'] }
    ]
  },
  { path: '/followUp', name: '慢病随访管理', icon: Calendar, roles: ['follow', 'admin'] },
  { path: '/guideSearch', name: '临床指南检索', icon: Reading, roles: ['doctor', 'follow', 'admin'] },
  { path: '/monitor', name: '高风险监控面板', icon: WarningFilled, roles: ['admin', 'doctor'] },
  { path: '/adminSetting', name: '系统配置管理', icon: SetUp, roles: ['admin'] }
]

const activeMenu = computed(() => {
  return route.path
})

const hasRole = (roles) => {
  if (!roles) return true
  return roles.includes(userStore.role)
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
}

.layout-header {
  position: fixed;
  top: 0;
  left: 200px;
  right: 0;
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  transition: background 0.2s;
  color: #606266;

  &:hover {
    background: #f5f7fa;
  }
}

.role-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #ecf5ff;
  color: #409eff;
}

.arrow-icon {
  font-size: 14px;
}

.layout-sidebar {
  width: 200px;
  height: 100vh;
  background: #001529;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 101;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
  font-size: 14px;

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 52px;
    line-height: 52px;
    margin: 2px 8px;
    border-radius: 8px;
    transition: all 0.2s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.08) !important;
    }
  }

  :deep(.el-menu-item.is-active),
  :deep(.el-sub-menu__title.is-active) {
    background: #1890ff !important;

    svg {
      color: #fff !important;
    }
  }

  :deep(.el-menu-item svg),
  :deep(.el-sub-menu__title svg) {
    font-size: 18px;
    color: rgba(255, 255, 255, 0.7);
  }

  :deep(.el-sub-menu .el-menu-item) {
    margin: 0;
    padding-left: 48px !important;
    border-radius: 0;

    &:hover {
      background: rgba(255, 255, 255, 0.05) !important;
    }
  }
}

.layout-main {
  flex: 1;
  margin-left: 200px;
  margin-top: 60px;
  padding: 20px;
  overflow-y: auto;
}
</style>