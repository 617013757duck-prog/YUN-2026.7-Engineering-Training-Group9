<template>
  <div class="dashboard">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <div class="logo">医疗平台</div>
        <el-menu router>
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/pre-diagnosis">
            <el-icon><Document /></el-icon>
            <span>预问诊</span>
          </el-menu-item>
          <el-menu-item index="/workbench">
            <el-icon><Briefcase /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/followup">
            <el-icon><Clock /></el-icon>
            <span>随访管理</span>
          </el-menu-item>
          <el-menu-item index="/ai-monitor">
            <el-icon><Monitor /></el-icon>
            <span>AI 监控</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header>
          <div class="header-title">医疗安全平台</div>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </el-header>

        <el-main>
          <el-card>
            <template #header>
              <span>欢迎来到医疗平台</span>
            </template>
            <p>这是一个基层医疗安全型预问诊与随访平台的教学辅助系统。</p>
            <p>请选择左侧菜单开始操作。</p>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const handleLogout = () => {
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.dashboard {
  height: 100vh;
}

.el-aside {
  background-color: #545c64;
  color: #fff;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #434a50;
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.el-main {
  background-color: #f0f2f5;
}
</style>