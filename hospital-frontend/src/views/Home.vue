<template>
  <div class="home">
    <el-card>
      <template #header>
        <h2>欢迎使用智能医院挂号管理系统</h2>
        <div v-if="isLoggedIn && userType === 1" style="margin-top: 10px; color: #409eff;">
          <el-icon><User /></el-icon> 欢迎回来，{{ userInfo?.username || '患者' }}
        </div>
      </template>
      
      <!-- 已登录患者的功能卡片 -->
      <div v-if="isLoggedIn && userType === 1">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover" class="function-card" @click="$router.push('/appointment')">
              <el-icon :size="50" color="#67c23a"><Calendar /></el-icon>
              <h3>预约挂号</h3>
              <p>选择科室和医生，轻松完成预约</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="function-card" @click="$router.push('/my-appointments')">
              <el-icon :size="50" color="#e6a23c"><Document /></el-icon>
              <h3>我的预约</h3>
              <p>查看预约记录、病例和处方详情</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="function-card" @click="$router.push('/profile')">
              <el-icon :size="50" color="#409eff"><User /></el-icon>
              <h3>个人信息</h3>
              <p>查看和修改个人资料</p>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="function-card" @click="$router.push('/guide')">
              <el-icon :size="50" color="#f56c6c"><Guide /></el-icon>
              <h3>智能导诊</h3>
              <p>根据症状推荐合适的科室和医生</p>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 未登录用户的功能卡片 -->
      <div v-else>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="hover">
              <el-icon :size="50" color="#409eff"><User /></el-icon>
              <h3>患者注册</h3>
              <p>快速注册，享受便捷的挂号服务</p>
              <el-button type="primary" @click="$router.push('/register')">立即注册</el-button>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <el-icon :size="50" color="#67c23a"><Calendar /></el-icon>
              <h3>在线预约</h3>
              <p>选择科室和医生，轻松完成预约</p>
              <el-button type="success" @click="$router.push('/appointment')">开始预约</el-button>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <el-icon :size="50" color="#e6a23c"><Document /></el-icon>
              <h3>我的预约</h3>
              <p>查看和管理您的预约记录</p>
              <el-button type="warning" @click="$router.push('/my-appointments')">查看预约</el-button>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { User, Calendar, Document, Guide } from '@element-plus/icons-vue'

const isLoggedIn = ref(false)
const userType = ref<number | null>(null)
const userInfo = ref<any>(null)

const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
  
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const info = JSON.parse(userInfoStr)
      userType.value = info.userType || null
      userInfo.value = info
    } catch (e) {
      userType.value = null
      userInfo.value = null
    }
  } else {
    userType.value = null
    userInfo.value = null
  }
}

onMounted(() => {
  checkLoginStatus()
  window.addEventListener('login-status-changed', checkLoginStatus)
  window.addEventListener('storage', checkLoginStatus)
})
</script>

<style scoped>
.home {
  padding: 20px;
}

.el-card {
  text-align: center;
  margin-bottom: 20px;
}

.el-card h3 {
  margin: 10px 0;
}

.function-card {
  cursor: pointer;
  transition: all 0.3s;
}

.function-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>

