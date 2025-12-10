<template>
  <div class="doctor-info">
    <el-card>
      <template #header>
        <h2>基本信息查询</h2>
      </template>
      
      <div v-if="doctorInfo" style="display: flex; gap: 30px; margin-bottom: 20px">
        <el-avatar :src="getAvatarUrl(doctorInfo.doctor?.avatar)" :size="120">
          <el-icon :size="60"><User /></el-icon>
        </el-avatar>
        <div style="flex: 1">
          <h2 style="margin: 0 0 10px 0">{{ doctorInfo.doctor?.name }}</h2>
          <p style="color: #666; margin: 5px 0">职称：{{ doctorInfo.doctor?.title }}</p>
          <p style="color: #666; margin: 5px 0">专长：{{ doctorInfo.doctor?.specialty || '暂无' }}</p>
        </div>
      </div>
      
      <el-descriptions :column="2" border v-if="doctorInfo">
        <el-descriptions-item label="简介" :span="2">{{ doctorInfo.doctor?.introduction || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="总排班数">{{ doctorInfo.totalSchedules }}</el-descriptions-item>
        <el-descriptions-item label="可用号源">{{ doctorInfo.availableCount }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>账户管理</el-divider>
      
      <el-button type="primary" @click="showChangePasswordDialog" style="margin-bottom: 20px">
        修改密码
      </el-button>
      
      <el-divider>排班信息</el-divider>
      
      <el-table :data="schedules" style="width: 100%" v-loading="loading" empty-text="暂无排班信息">
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时段" width="150" />
        <el-table-column prop="totalCount" label="总号源" width="100" />
        <el-table-column prop="reservedCount" label="已预约" width="100" />
        <el-table-column label="剩余" width="100">
          <template #default="scope">
            {{ scope.row.totalCount - (scope.row.reservedCount || 0) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '停诊' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="500px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="changingPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { doctorApi } from '../../api/doctor'

const doctorInfo = ref<any>(null)
const schedules = ref<any[]>([])
const loading = ref(false)
const passwordDialogVisible = ref(false)
const passwordFormRef = ref<FormInstance>()
const changingPassword = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(() => {
  loadDoctorInfo()
})

const loadDoctorInfo = async () => {
  loading.value = true
  try {
    const res = await doctorApi.getDoctorInfo()
    if (res.code === 200) {
      doctorInfo.value = res.data
      schedules.value = res.data?.schedules || []
      if (schedules.value.length === 0) {
        ElMessage.info('暂无排班信息')
      }
    } else {
      ElMessage.error(res.message || '加载医生信息失败')
    }
  } catch (error: any) {
    ElMessage.error('加载医生信息失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const showChangePasswordDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      try {
        const res = await doctorApi.changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        if (res.code === 200) {
          ElMessage.success('密码修改成功')
          passwordDialogVisible.value = false
        } else {
          ElMessage.error(res.message || '密码修改失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '密码修改失败')
      } finally {
        changingPassword.value = false
      }
    }
  })
}

const getAvatarUrl = (avatar: string | undefined) => {
  if (!avatar) return ''
  // 如果是完整URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  // 如果是相对路径，添加基础URL
  if (avatar.startsWith('/')) {
    return avatar
  }
  // 如果只是文件名，添加完整路径
  if (avatar.includes('/')) {
    return avatar
  }
  return `/uploads/avatars/${avatar}`
}
</script>

<style scoped>
.doctor-info {
  padding: 20px;
}
</style>

