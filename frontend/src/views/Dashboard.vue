<template>
  <div class="dashboard">
    <h2>数据看板</h2>
    <p>欢迎回来，{{ realName }}！</p>
    <p>当前角色：{{ roleName }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const realName = computed(() => userStore.realName)

const roleMap = {
  patient: '患者',
  doctor: '医务人员',
  follow: '随访人员',
  admin: '管理员'
}

const roleName = computed(() => roleMap[userStore.role] || '')
</script>

<style scoped>
.dashboard {
  padding: 20px;
}
</style>