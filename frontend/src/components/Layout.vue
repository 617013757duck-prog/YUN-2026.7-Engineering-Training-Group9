<template>
  <div class="layout-container">
    <header class="layout-header" :class="{ 'sidebar-collapsed': isCollapse }">
      <div class="header-left">
        <button class="menu-toggle" @click="toggleSidebar">
          <el-icon :size="20"><Menu v-if="isMobile || isCollapse" /><Close v-else /></el-icon>
        </button>
      </div>
      <div class="header-center">
        <div class="search-box">
          <el-icon :size="16"><Search /></el-icon>
          <input type="text" placeholder="搜索病例、患者..." class="search-input" />
        </div>
      </div>
      <div class="header-right">
        <button class="header-btn" @click="showNotification">
          <el-icon :size="18"><Bell /></el-icon>
          <span v-if="notificationCount > 0" class="notification-badge">{{ notificationCount }}</span>
        </button>
        <el-dropdown trigger="click">
          <span class="user-info">
            <div class="user-avatar">
              <el-icon :size="20"><User /></el-icon>
            </div>
            <div class="user-detail">
              <span class="user-name">{{ userStore.realName }}</span>
              <span class="user-role">{{ userStore.roleName() }}</span>
            </div>
            <el-icon :size="14" class="arrow-icon"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goProfile">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item @click="goSettings">
                <el-icon><SetUp /></el-icon>
                账号设置
              </el-dropdown-item>
              <el-dropdown-item divided @click="logout">
                <el-icon><TurnOff /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <aside class="layout-sidebar" :class="{ 'is-collapsed': isCollapse, 'is-hidden': isMobile && !showMobileSidebar }">
      <div class="sidebar-header">
        <div class="mobile-close" @click="showMobileSidebar = false">
          <el-icon><Close /></el-icon>
        </div>
      </div>
      <div class="sidebar-logo">
        <div class="sidebar-logo-icon">
          <svg width="28" height="28" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
            <circle cx="512" cy="512" r="416" fill="#409EFF"/>
            <circle cx="512" cy="512" r="290" fill="#fff"/>
            <circle cx="512" cy="512" r="185" fill="#409EFF"/>
            <circle cx="512" cy="512" r="92" fill="#fff"/>
          </svg>
        </div>
        <span class="sidebar-logo-text">医疗预问诊平台</span>
      </div>
      <div class="sidebar-menu-container">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          :collapse="isCollapse"
          :collapse-transition="false"
        >
          <template v-for="(item, index) in menuList" :key="item.path">
            <div v-if="item.divider" class="menu-divider"></div>
            <el-menu-item v-if="!item.children" :index="item.path" :disabled="!hasRole(item.roles)">
              <div class="menu-icon-wrapper">
                <component :is="item.icon" />
              </div>
              <template #title>
                <span class="menu-text">{{ item.name }}</span>
                <span v-if="item.badge" class="menu-badge">{{ item.badge }}</span>
              </template>
            </el-menu-item>
            <el-sub-menu v-else :index="item.path">
              <template #title>
                <div class="menu-icon-wrapper">
                  <component :is="item.icon" />
                </div>
                <span class="menu-text">{{ item.name }}</span>
                <span v-if="item.badge" class="menu-badge">{{ item.badge }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="child.path"
                :disabled="!hasRole(child.roles)"
              >
                <template #title>
                  <span class="sub-menu-indicator"></span>
                  <span class="menu-text sub-menu-text">{{ child.name }}</span>
                </template>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </div>
      <div class="sidebar-footer">
        <div class="sidebar-version" v-if="!isCollapse">
          <span>版本 1.0.0</span>
        </div>
      </div>
    </aside>
    <div v-if="isMobile && showMobileSidebar" class="sidebar-mask" @click="showMobileSidebar = false"></div>
    <main class="layout-main" :class="{ 'mobile-full': isMobile && showMobileSidebar, 'sidebar-collapsed': isCollapse }">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'
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
  SetUp,
  Menu,
  Close,
  Search,
  Bell
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const isMobile = ref(false)
const showMobileSidebar = ref(false)
const notificationCount = ref(3)

const menuList = [
  { path: '/dashboard', name: '数据看板', icon: DataBoard, roles: ['patient', 'doctor', 'follow', 'admin'] },
  { path: '/preConsult', name: '预问诊表单', icon: FirstAidKit, roles: ['patient'] },
  {
    path: '/case',
    name: '病例管理',
    icon: Files,
    roles: ['doctor', 'admin'],
    badge: '3',
    children: [
      { path: '/caseList', name: '待审队列', roles: ['doctor', 'admin'] },
      { path: '/caseDetail/:id', name: '病例详情', roles: ['doctor', 'admin'] }
    ]
  },
  { path: '/followUp', name: '慢病随访管理', icon: Calendar, roles: ['follow', 'admin'] },
  { path: '/guideSearch', name: '临床指南检索', icon: Reading, roles: ['doctor', 'follow', 'admin'] },
  { path: '/monitor', name: '高风险监控面板', icon: WarningFilled, roles: ['admin', 'doctor'], badge: '2' },
  { divider: true },
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

const toggleSidebar = () => {
  if (isMobile.value) {
    showMobileSidebar.value = !showMobileSidebar.value
  } else {
    isCollapse.value = !isCollapse.value
  }
}

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    isCollapse.value = false
    showMobileSidebar.value = false
  }
}

const showNotification = () => {
  ElMessage.info('暂无新通知')
}

const goProfile = () => {
  ElMessage.info('个人中心功能开发中')
}

const goSettings = () => {
  ElMessage.info('账号设置功能开发中')
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
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
  left: 240px;
  right: 0;
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
  z-index: 100;
  transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.sidebar-collapsed {
    left: 64px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .menu-toggle {
    width: 36px;
    height: 36px;
    border: none;
    background: rgba(64, 158, 255, 0.06);
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #409eff;
    transition: all 0.2s;

    &:hover {
      background: rgba(64, 158, 255, 0.12);
      transform: scale(1.05);
    }
  }

  .header-center {
    flex: 1;
    max-width: 360px;
    margin: 0 30px;
  }

  .search-box {
    display: flex;
    align-items: center;
    background: #f5f7fa;
    border-radius: 8px;
    padding: 7px 14px;
    transition: all 0.2s;

    &:hover {
      background: #eef2f7;
    }

    &:focus-within {
      background: #fff;
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
    }

    :deep(.el-icon) {
      color: #8e8ea9;
      margin-right: 10px;
    }
  }

  .search-input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 13px;
    color: #1a1a2e;
    outline: none;

    &::placeholder {
      color: #c0c4cc;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .header-btn {
    position: relative;
    width: 36px;
    height: 36px;
    border: none;
    background: transparent;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #666;
    transition: all 0.2s;

    &:hover {
      background: rgba(0, 0, 0, 0.04);
      color: #409eff;
    }

    .notification-badge {
      position: absolute;
      top: 4px;
      right: 4px;
      min-width: 16px;
      height: 16px;
      padding: 0 4px;
      background: #409eff;
      color: #fff;
      font-size: 10px;
      font-weight: 600;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 5px 12px;
    border-radius: 8px;
    transition: all 0.2s;

    &:hover {
      background: rgba(64, 158, 255, 0.06);
    }
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .user-detail {
    display: flex;
    flex-direction: column;
    gap: 1px;
  }

  .user-name {
    font-size: 13px;
    font-weight: 600;
    color: #1a1a2e;
  }

  .user-role {
    font-size: 10px;
    color: #8e8ea9;
  }

  .arrow-icon {
    font-size: 12px;
    color: #8e8ea9;
  }

  :deep(.el-dropdown-menu) {
    background: #fff !important;
    border-radius: 10px !important;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
    border: 1px solid rgba(0, 0, 0, 0.06) !important;
    padding: 4px 0 !important;
    margin-top: 8px !important;

    .el-dropdown-menu__item {
      color: #303133 !important;
      font-size: 13px !important;
      height: 40px !important;
      line-height: 40px !important;
      padding: 0 20px !important;
      margin: 0 4px !important;
      border-radius: 6px !important;
      transition: all 0.2s !important;

      &:hover {
        background: rgba(64, 158, 255, 0.06) !important;
        color: #409eff !important;
      }

      &.is-divided {
        border-top: 1px solid rgba(0, 0, 0, 0.06) !important;
        margin-top: 4px !important;
        padding-top: 4px !important;
      }

      .el-icon {
        margin-right: 8px !important;
        font-size: 14px !important;
      }
    }
  }
}

.layout-sidebar {
  width: 240px;
  height: 100vh;
  background: linear-gradient(180deg, #1a237e 0%, #0d1442 100%);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 101;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);

  &.is-collapsed {
    width: 64px;

    .sidebar-logo-text,
    .sidebar-header,
    .sidebar-version,
    .menu-badge,
    .menu-divider {
      display: none;
    }

    .sidebar-logo {
      justify-content: center;
      padding: 20px 0;
      border-bottom-width: 0;
    }

    .sidebar-logo-icon {
      margin-right: 0;
      width: 40px;
      height: 40px;
    }

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      justify-content: center;
      padding-left: 0 !important;
      padding-right: 0 !important;
      border-left: none !important;
    }

    :deep(.el-sub-menu .el-menu-item) {
      margin: 0 12px !important;
      padding-left: 0 !important;
      justify-content: center;
    }

    .menu-icon-wrapper {
      margin-right: 0;
    }

    .menu-text {
      display: none;
    }

    .sub-menu-indicator {
      display: none;
    }
  }

  &.is-hidden {
    left: -240px;
  }

  .sidebar-header {
    display: none;
    padding: 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);

    .mobile-close {
      display: flex;
      justify-content: flex-end;
      cursor: pointer;
      color: rgba(255, 255, 255, 0.5);
      font-size: 20px;

      &:hover {
        color: #fff;
      }
    }
  }

  .sidebar-logo {
    display: flex;
    align-items: center;
    padding: 20px 20px 18px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);

    .sidebar-logo-icon {
      width: 44px;
      height: 44px;
      border-radius: 14px;
      background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4), inset 0 1px 2px rgba(255, 255, 255, 0.2);
      transition: all 0.3s;

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 8px 24px rgba(64, 158, 255, 0.5), inset 0 1px 2px rgba(255, 255, 255, 0.2);
      }
    }

    .sidebar-logo-text {
      font-size: 18px;
      font-weight: 700;
      color: #fff;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      letter-spacing: 1px;
    }
  }

  .sidebar-menu-container {
    flex: 1;
    padding-top: 8px;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 2px;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }

  .sidebar-menu {
    border-right: none;
    font-size: 14px;
    background: transparent !important;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 46px;
      line-height: 46px;
      margin: 0 12px;
      border-radius: 8px;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      display: flex;
      align-items: center;
      padding-left: 14px !important;
      padding-right: 10px !important;
      border-left: 3px solid transparent;

      &:hover {
        background: rgba(255, 255, 255, 0.05) !important;
      }
    }

    :deep(.el-menu-item.is-active),
    :deep(.el-sub-menu__title.is-active) {
      background: rgba(64, 158, 255, 0.08) !important;
      border-left: 3px solid #409eff;

      .menu-icon-wrapper {
        background: rgba(64, 158, 255, 0.2);
      }

      svg {
        color: #409eff !important;
      }

      .menu-text {
        color: #fff;
        font-weight: 600;
        opacity: 1;
      }
    }

    .menu-icon-wrapper {
      width: 34px;
      height: 34px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 10px;
      transition: all 0.2s;
      background: rgba(255, 255, 255, 0.04);
      flex-shrink: 0;
    }

    :deep(.el-menu-item svg),
    :deep(.el-sub-menu__title svg) {
      font-size: 19px;
      color: rgba(255, 255, 255, 0.75);
      transition: color 0.2s;
    }

    :deep(.el-menu-item:hover svg),
    :deep(.el-sub-menu__title:hover svg) {
      color: rgba(255, 255, 255, 0.9);
    }

    .menu-text {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.75);
      font-weight: 500;
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      transition: color 0.2s;
    }

    :deep(.el-menu-item:hover .menu-text),
    :deep(.el-sub-menu__title:hover .menu-text) {
      color: rgba(255, 255, 255, 0.9);
    }

    .menu-badge {
      margin-left: 6px;
      margin-right: 8px;
      background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
      color: #fff;
      font-size: 10px;
      width: 18px;
      height: 18px;
      padding: 0;
      border-radius: 50%;
      font-weight: 600;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
      animation: pulseBadge 2s ease-in-out infinite;
    }

    .menu-divider {
      height: 1px;
      background: rgba(255, 255, 255, 0.06);
      margin: 8px 12px;
    }

    :deep(.el-sub-menu .el-menu-item) {
      height: 40px;
      line-height: 40px;
      margin: 0 12px 0 36px;
      padding-left: 20px !important;
      padding-right: 10px !important;
      border-radius: 6px;
      border-left: none !important;
      background: transparent !important;

      &:hover {
        background: rgba(255, 255, 255, 0.04) !important;
      }

      &.is-active {
        background: rgba(64, 158, 255, 0.1) !important;
        border-left: 3px solid #409eff !important;

        .menu-text {
          color: #fff;
          font-weight: 600;
          opacity: 1;
        }

        .sub-menu-indicator {
          background: #409eff;
        }
      }

      .menu-text {
        font-size: 13px;
        color: #8e99a4;
        padding-left: 0;
      }
    }

    .sub-menu-indicator {
      width: 3px;
      height: 3px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.4);
      margin-right: 8px;
      flex-shrink: 0;
      transition: background 0.2s;
    }

    :deep(.el-sub-menu.is-active .el-sub-menu__title) {
      background: rgba(64, 158, 255, 0.08) !important;
      border-left: 3px solid #409eff;
    }
  }

  .sidebar-footer {
    padding: 12px;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
  }

  .sidebar-version {
    font-size: 10px;
    color: rgba(255, 255, 255, 0.25);
    text-align: center;
  }
}

@keyframes pulseBadge {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.9;
  }
}

.sidebar-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  z-index: 100;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.layout-main {
  position: absolute;
  top: 60px;
  left: 240px;
  right: 0;
  bottom: 0;
  padding: 20px;
  overflow-y: auto;
  transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1;

  &.mobile-full {
    left: 0;
  }

  &.sidebar-collapsed {
    left: 64px;
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .layout-header {
    left: 0;
    padding: 0 14px;

    &.sidebar-collapsed {
      left: 64px;
    }

    .header-center {
      display: none;
    }

    .user-detail {
      display: none;
    }
  }

  .layout-sidebar {
    .sidebar-header {
      display: block;
    }
  }

  .layout-main {
    left: 0;
    padding: 14px;

    &.sidebar-collapsed {
      left: 64px;
    }
  }
}

@media (min-width: 769px) and (max-width: 992px) {
  .layout-header {
    left: 240px;

    &.sidebar-collapsed {
      left: 64px;
    }
  }

  .layout-sidebar {
    width: 240px;
  }

  .layout-main {
    left: 240px;

    &.sidebar-collapsed {
      left: 64px;
    }
  }
}

@media (min-width: 993px) {
  .layout-header {
    .menu-toggle {
      display: flex;
    }
  }
}
</style>