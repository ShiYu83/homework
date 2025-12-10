<template>
  <div class="schedule-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>调班管理</h2>
          <el-button type="primary" @click="showAddDialog">申请调班</el-button>
        </div>
      </template>
      
      <el-table :data="adjustments" style="width: 100%" v-loading="loading" empty-text="暂无调班申请">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="originalDate" label="原日期" width="120" />
        <el-table-column prop="originalTimeSlot" label="原时段" width="150" />
        <el-table-column prop="newDate" label="新日期" width="120" />
        <el-table-column prop="newTimeSlot" label="新时段" width="150" />
        <el-table-column prop="reason" label="原因" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
      </el-table>
    </el-card>
    
    <!-- 申请调班对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="申请调班"
      width="600px"
    >
      <el-form :model="adjustmentForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="选择排班" prop="scheduleId">
          <el-select v-model="adjustmentForm.scheduleId" placeholder="请选择要调整的排班" filterable>
            <el-option
              v-for="schedule in availableSchedules"
              :key="schedule.id"
              :label="`${schedule.scheduleDate} ${schedule.timeSlot}`"
              :value="schedule.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="新日期">
          <el-date-picker
            v-model="adjustmentForm.newDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="留空表示停诊"
          />
        </el-form-item>
        <el-form-item label="新时段">
          <el-select v-model="adjustmentForm.newTimeSlot" placeholder="请选择新时段" clearable>
            <el-option
              v-for="slot in timeSlots"
              :key="slot"
              :label="slot"
              :value="slot"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调班原因" prop="reason">
          <el-input
            v-model="adjustmentForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调班原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjustment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { doctorApi, type ScheduleAdjustment } from '../../api/doctor'
import { adminApi } from '../../api/admin'

const adjustments = ref<ScheduleAdjustment[]>([])
const availableSchedules = ref<any[]>([])
const timeSlots = ref<string[]>([])
const dialogVisible = ref(false)
const formRef = ref()
const loading = ref(false)

const adjustmentForm = reactive({
  scheduleId: null as number | null,
  newDate: '',
  newTimeSlot: '',
  reason: ''
})

const rules = {
  scheduleId: [{ required: true, message: '请选择排班', trigger: 'change' }],
  reason: [{ required: true, message: '请输入调班原因', trigger: 'blur' }]
}

onMounted(() => {
  loadAdjustments()
  loadTimeSlots()
})

const loadAdjustments = async () => {
  loading.value = true
  try {
    const res = await doctorApi.getScheduleAdjustments()
    if (res.code === 200) {
      adjustments.value = res.data || []
      if (adjustments.value.length === 0) {
        ElMessage.info('暂无调班申请')
      }
    } else {
      ElMessage.error(res.message || '加载调班申请失败')
    }
  } catch (error: any) {
    ElMessage.error('加载调班申请失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const loadTimeSlots = async () => {
  try {
    const res = await adminApi.getTimeSlots()
    if (res.code === 200) {
      timeSlots.value = res.data || []
    }
  } catch (error) {
    console.error('加载时段列表失败', error)
  }
}

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || ''
}

const showAddDialog = async () => {
  // 加载可用排班
  try {
    const doctorInfo = await doctorApi.getDoctorInfo()
    if (doctorInfo.code === 200) {
      availableSchedules.value = doctorInfo.data?.schedules || []
    }
  } catch (error) {
    console.error('加载排班失败', error)
  }
  
  Object.assign(adjustmentForm, {
    scheduleId: null,
    newDate: '',
    newTimeSlot: '',
    reason: ''
  })
  dialogVisible.value = true
}

const submitAdjustment = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await doctorApi.createScheduleAdjustment({
          scheduleId: adjustmentForm.scheduleId!,
          newDate: adjustmentForm.newDate || undefined,
          newTimeSlot: adjustmentForm.newTimeSlot || undefined,
          reason: adjustmentForm.reason
        })
        ElMessage.success('调班申请提交成功')
        dialogVisible.value = false
        loadAdjustments()
      } catch (error: any) {
        ElMessage.error(error.message || '提交失败')
      }
    }
  })
}
</script>

<style scoped>
.schedule-manage {
  padding: 20px;
}
</style>

