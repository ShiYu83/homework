<template>
  <el-container>
    <el-header>
      <div class="header-content">
        <h1>智能医院挂号管理系统</h1>
        <el-menu
          mode="horizontal"
          :default-active="activeIndex"
          router
        >
          <el-menu-item index="/">首页</el-menu-item>
          <!-- 管理员控制台菜单 -->
          <el-menu-item index="/admin" v-if="userType === 3">管理员控制台</el-menu-item>
          <!-- 患者菜单 -->
          <template v-if="userType === 1 || !isLoggedIn">
            <el-menu-item index="/guide">智能导诊</el-menu-item>
            <el-menu-item index="/appointment">预约挂号</el-menu-item>
            <el-menu-item index="/my-appointments" v-if="isLoggedIn && userType === 1">我的预约</el-menu-item>
            <el-menu-item index="/queue">候诊队列</el-menu-item>
            <el-menu-item index="/profile" v-if="isLoggedIn && userType === 1">个人信息</el-menu-item>
          </template>
          <!-- 医生菜单 -->
          <template v-if="userType === 2">
            <el-menu-item index="/doctor/info">我的信息</el-menu-item>
            <el-menu-item index="/doctor/schedule">调班管理</el-menu-item>
            <el-menu-item index="/doctor/queue">患者队列</el-menu-item>
          </template>
          <!-- 管理员菜单 -->
          <template v-if="userType === 3">
            <el-menu-item index="/admin/doctors">医生管理</el-menu-item>
            <el-menu-item index="/admin/patients">患者管理</el-menu-item>
            <el-menu-item index="/admin/departments">科室管理</el-menu-item>
            <el-menu-item index="/admin/rooms">诊室管理</el-menu-item>
            <el-menu-item index="/admin/schedules">号源管理</el-menu-item>
            <el-menu-item index="/admin/statistics">统计分析</el-menu-item>
          </template>
          <el-menu-item index="/login" v-if="!isLoggedIn">登录</el-menu-item>
          <el-menu-item index="/register" v-if="!isLoggedIn">注册</el-menu-item>
          <el-menu-item v-if="isLoggedIn" @click="handleLogout">退出</el-menu-item>
        </el-menu>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const activeIndex = computed(() => route.path)
const isLoggedIn = ref(false)
const userType = ref<number | null>(null)

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
  
  // 获取用户类型
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      userType.value = userInfo.userType || null
    } catch (e) {
      userType.value = null
    }
  } else {
    userType.value = null
  }
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  isLoggedIn.value = false
  userType.value = null
  ElMessage.success('已退出登录')
  // 触发登录状态变化事件
  window.dispatchEvent(new Event('login-status-changed'))
  router.push('/login')
}

onMounted(() => {
  checkLoginStatus()
  // 监听存储变化（当其他标签页登录/登出时）
  window.addEventListener('storage', checkLoginStatus)
  // 监听自定义事件（当前标签页登录/登出时）
  window.addEventListener('login-status-changed', checkLoginStatus)
})

// 组件卸载时移除监听器
onUnmounted(() => {
  window.removeEventListener('storage', checkLoginStatus)
  window.removeEventListener('login-status-changed', checkLoginStatus)
})
</script>

<style scoped>
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-content h1 {
  margin: 0;
  color: #409eff;
}
</style>

