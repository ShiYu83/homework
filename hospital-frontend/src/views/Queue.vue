<template>
  <div class="queue">
    <el-card>
      <template #header>
        <h2>候诊队列</h2>
      </template>
      
      <el-form :inline="true" class="search-form">
        <el-form-item label="医生">
          <el-select v-model="doctorId" placeholder="请选择医生" style="width: 200px">
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadQueue">查询</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="queueData" style="width: 100%" v-loading="loading">
        <el-table-column prop="appointmentNo" label="预约号" width="150" />
        <el-table-column prop="timeSlot" label="时间段" width="120" />
        <el-table-column prop="patientName" label="患者姓名" />
        <el-table-column prop="createTime" label="预约时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="queue-info" v-if="queueData.length > 0">
        <el-card>
          <p>总人数：{{ queueData.length }}</p>
          <p>当前时间：{{ currentTime }}</p>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { doctorApi } from '../api/doctor'

const doctorId = ref<number | null>(null)
const selectedDate = ref<string>('')
const doctors = ref<any[]>([])
const queueData = ref<any[]>([])
const loading = ref(false)
const currentTime = ref('')

onMounted(() => {
  loadDoctors()
  // 默认选择今天
  const today = new Date()
  selectedDate.value = today.toISOString().split('T')[0]
  updateCurrentTime()
  setInterval(updateCurrentTime, 1000)
})

const loadDoctors = async () => {
  try {
    const res = await doctorApi.list()
    if (res.code === 200) {
      doctors.value = res.data
    }
  } catch (error: any) {
    ElMessage.error('加载医生列表失败')
  }
}

const loadQueue = async () => {
  if (!doctorId.value || !selectedDate.value) {
    ElMessage.warning('请选择医生和日期')
    return
  }
  
  loading.value = true
  try {
    const res = await request.get(`/queue/doctor/${doctorId.value}`, {
      params: { date: selectedDate.value }
    })
    if (res.code === 200) {
      queueData.value = res.data.slots ? Object.values(res.data.slots).flat() : []
    }
  } catch (error: any) {
    ElMessage.error('加载队列失败')
  } finally {
    loading.value = false
  }
}

const updateCurrentTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN')
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    1: 'info',
    2: 'success',
    3: 'warning',
    4: ''
  }
  return types[status] || ''
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    1: '待支付',
    2: '已预约',
    3: '已取消',
    4: '已完成'
  }
  return texts[status] || '未知'
}
</script>

<style scoped>
.queue {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.queue-info {
  margin-top: 20px;
}
</style>

