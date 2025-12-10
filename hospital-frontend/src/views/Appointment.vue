<template>
  <div class="appointment">
    <el-card>
      <template #header>
        <h2>预约挂号</h2>
      </template>
      
      <el-steps :active="currentStep" finish-status="success" style="margin-bottom: 30px">
        <el-step title="选择科室" />
        <el-step title="选择医生" />
        <el-step title="选择时间" />
        <el-step title="确认预约" />
      </el-steps>

      <!-- 步骤1: 选择科室 -->
      <div v-if="currentStep === 0">
        <h3>请选择科室</h3>
        <el-row :gutter="20" v-loading="loadingDepartments">
          <el-col :span="6" v-for="dept in departments" :key="dept.id">
            <el-card shadow="hover" @click="selectDepartment(dept)" style="cursor: pointer">
              <h4>{{ dept.name }}</h4>
              <p>{{ dept.description }}</p>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!loadingDepartments && departments.length === 0" description="暂无科室数据" />
      </div>

      <!-- 步骤2: 选择医生 -->
      <div v-if="currentStep === 1">
        <h3>请选择医生</h3>
        <el-button @click="currentStep = 0">返回</el-button>
        <el-skeleton v-if="loadingDoctors" :rows="3" animated style="margin-top: 20px" />
        <el-row v-else-if="doctors.length > 0" :gutter="20" style="margin-top: 20px">
          <el-col :span="8" v-for="doctor in doctors" :key="doctor.id">
            <el-card shadow="hover" @click="selectDoctor(doctor)" style="cursor: pointer">
              <div style="display: flex; align-items: center; gap: 15px">
                <el-avatar :src="getAvatarUrl(doctor.avatar)" :size="60">
                  <el-icon :size="30"><User /></el-icon>
                </el-avatar>
                <div style="flex: 1">
                  <h4 style="margin: 0 0 5px 0">{{ doctor.name }}</h4>
                  <p style="margin: 5px 0; color: #666">职称：{{ doctor.title }}</p>
                  <p style="margin: 5px 0; color: #666">专长：{{ doctor.specialty || '暂无' }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="该科室暂无医生，请选择其他科室" style="margin-top: 20px" />
      </div>

      <!-- 步骤3: 选择时间 -->
      <div v-if="currentStep === 2">
        <h3>请选择预约时间</h3>
        <el-button @click="currentStep = 1">返回</el-button>
        
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="loadTimeSlots"
          style="margin: 20px 0"
        />

        <el-row :gutter="20" v-if="timeSlots.length > 0">
          <el-col :span="6" v-for="slot in timeSlots" :key="slot.scheduleId">
            <el-card
              shadow="hover"
              @click="selectTimeSlot(slot)"
              style="cursor: pointer"
              :class="{ 'selected': selectedSlot?.scheduleId === slot.scheduleId }"
            >
              <h4>{{ slot.timeSlot }}</h4>
              <p>剩余号源：{{ slot.remaining }}/{{ slot.totalCount }}</p>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="该日期暂无可用号源" />
      </div>

      <!-- 步骤4: 确认预约 -->
      <div v-if="currentStep === 3">
        <h3>确认预约信息</h3>
        <div style="display: flex; gap: 20px; margin-bottom: 20px; align-items: center">
          <el-avatar :src="getAvatarUrl(selectedDoctor?.avatar)" :size="80">
            <el-icon :size="40"><User /></el-icon>
          </el-avatar>
          <div>
            <h4 style="margin: 0">{{ selectedDoctor?.name }}</h4>
            <p style="margin: 5px 0; color: #666">{{ selectedDoctor?.title }}</p>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="科室">{{ selectedDepartment?.name }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ selectedDoctor?.name }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ selectedDate }}</el-descriptions-item>
          <el-descriptions-item label="时间段">{{ selectedSlot?.timeSlot }}</el-descriptions-item>
          <el-descriptions-item label="挂号费">25.00 元</el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px">
          <el-button @click="currentStep = 2">返回</el-button>
          <el-button type="primary" @click="submitAppointment" :loading="submitting">
            确认预约
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { departmentApi, type Department } from '../api/department'
import { doctorApi } from '../api/doctor'
import type { Doctor } from '../api/doctor'
import { appointmentApi, type TimeSlotDTO } from '../api/appointment'

const currentStep = ref(0)
const departments = ref<Department[]>([])
const doctors = ref<Doctor[]>([])
const timeSlots = ref<TimeSlotDTO[]>([])
const selectedDepartment = ref<Department | null>(null)
const selectedDoctor = ref<Doctor | null>(null)
const selectedDate = ref('')
const selectedSlot = ref<TimeSlotDTO | null>(null)
const submitting = ref(false)
const loadingDoctors = ref(false)
const loadingDepartments = ref(false)

onMounted(() => {
  loadDepartments()
  // 默认选择今天
  const today = new Date()
  selectedDate.value = today.toISOString().split('T')[0]
})

const loadDepartments = async () => {
  loadingDepartments.value = true
  try {
    const res = await departmentApi.list()
    if (res.code === 200) {
      departments.value = res.data || []
      if (departments.value.length === 0) {
        ElMessage.warning('暂无科室数据')
      }
    } else {
      ElMessage.error(res.message || '加载科室列表失败')
    }
  } catch (error: any) {
    ElMessage.error('加载科室列表失败: ' + (error.message || '未知错误'))
  } finally {
    loadingDepartments.value = false
  }
}

const selectDepartment = (dept: Department) => {
  selectedDepartment.value = dept
  loadDoctors(dept.id)
  currentStep.value = 1
}

const loadDoctors = async (departmentId: number) => {
  loadingDoctors.value = true
  doctors.value = [] // 清空之前的数据
  try {
    const res = await doctorApi.list(departmentId)
    console.log('医生列表API响应:', res)
    if (res.code === 200) {
      doctors.value = res.data || []
      console.log('加载到的医生数量:', doctors.value.length)
      console.log('医生列表:', doctors.value)
      if (doctors.value.length === 0) {
        ElMessage.warning('该科室暂无医生')
      }
    } else {
      ElMessage.error(res.message || '加载医生列表失败')
    }
  } catch (error: any) {
    console.error('加载医生列表错误:', error)
    ElMessage.error(error.message || '加载医生列表失败')
  } finally {
    loadingDoctors.value = false
  }
}

const selectDoctor = (doctor: Doctor) => {
  selectedDoctor.value = doctor
  currentStep.value = 2
  loadTimeSlots()
}

const loadTimeSlots = async () => {
  if (!selectedDate.value) return
  
  try {
    const res = await appointmentApi.getAvailableSlots({
      departmentId: selectedDepartment.value?.id,
      doctorId: selectedDoctor.value?.id,
      date: selectedDate.value
    })
    if (res.code === 200) {
      timeSlots.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载号源失败')
  }
}

const selectTimeSlot = (slot: TimeSlotDTO) => {
  selectedSlot.value = slot
  currentStep.value = 3
}

const submitAppointment = async () => {
  if (!selectedSlot.value) {
    ElMessage.warning('请选择时间段')
    return
  }

  // 获取当前登录患者ID
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) {
    ElMessage.error('请先登录')
    return
  }
  
  let patientId: number | null = null
  try {
    const userInfo = JSON.parse(userInfoStr)
    patientId = userInfo.patientId
  } catch (e) {
    ElMessage.error('获取用户信息失败，请重新登录')
    return
  }
  
  if (!patientId) {
    ElMessage.error('无法获取患者ID，请重新登录')
    return
  }

  submitting.value = true
  try {
    const res = await appointmentApi.create({
      patientId,
      scheduleId: selectedSlot.value.scheduleId,
      doctorId: selectedDoctor.value?.id,
      appointmentDate: selectedDate.value,
      timeSlot: selectedSlot.value.timeSlot
    })
    
    if (res.code === 200) {
      ElMessage.success('预约成功！')
      // 重置表单
      currentStep.value = 0
      selectedDepartment.value = null
      selectedDoctor.value = null
      selectedSlot.value = null
    } else {
      ElMessage.error(res.message || '预约失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '预约失败')
  } finally {
    submitting.value = false
  }
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
.appointment {
  padding: 20px;
}

.selected {
  border: 2px solid #409eff;
}
</style>

