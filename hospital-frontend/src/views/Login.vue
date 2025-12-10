<template>
  <div class="login">
    <el-card style="max-width: 500px; margin: 100px auto">
      <template #header>
        <h2>用户登录</h2>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <div style="text-align: center; width: 100%">
            <el-link type="primary" @click="$router.push('/register')">
              还没有账号？立即注册
            </el-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { authApi } from '../api/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const submit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await authApi.login(form)
        if (res.code === 200) {
          // 保存token到localStorage
          if (res.data?.token) {
            localStorage.setItem('token', res.data.token)
            
            // 处理用户信息，确保格式一致性
            const userInfo = res.data.userInfo || {};
            // 规范化用户类型字段
            if (userInfo.user_type && !userInfo.userType) {
              userInfo.userType = userInfo.user_type;
            }
            localStorage.setItem('userInfo', JSON.stringify(userInfo))
            
            ElMessage.success('登录成功！')
            // 触发登录状态更新事件
            window.dispatchEvent(new Event('login-status-changed'))
            
            // 根据用户类型跳转到对应的页面
            if (userInfo.userType === 3) {
              // 管理员跳转到管理控制台
              router.push('/admin')
            } else if (userInfo.userType === 2) {
              // 医生跳转到医生信息页面
              router.push('/doctor/info')
            } else {
              // 普通用户跳转到首页
              router.push('/')
            }
          } else {
            ElMessage.error('登录失败：未返回有效的token')
          }
        } else {
          ElMessage.error(res.message || '登录失败')
        }
      } catch (error: any) {
        console.error('登录错误:', error)
        // 显示详细的错误信息
        let errorMessage = '登录失败'
        if (error.response?.data?.message) {
          errorMessage = error.response.data.message
        } else if (error.message) {
          errorMessage = error.message
        } else if (error.code === 'ERR_NETWORK') {
          errorMessage = '无法连接到服务器，请检查后端服务是否启动'
        }
        ElMessage.error(errorMessage)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login {
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
}
</style>

