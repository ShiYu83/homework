<template>
  <div class="schedule-adjustments-manage">
    <el-card>
      <template #header>
        <h2>调班申请审核</h2>
      </template>
      
      <el-table :data="adjustments" style="width: 100%" v-loading="loading" empty-text="暂无待审核的调班申请">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="医生信息" width="200">
          <template #default="scope">
            <div v-if="scope.row.doctor">
              <div><strong>{{ scope.row.doctor.name }}</strong></div>
              <div style="font-size: 12px; color: #666">{{ scope.row.doctor.title }}</div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="原排班" width="200">
          <template #default="scope">
            <div>{{ scope.row.originalDate }}</div>
            <div style="font-size: 12px; color: #666">{{ scope.row.originalTimeSlot }}</div>
          </template>
        </el-table-column>
        <el-table-column label="新排班" width="200">
          <template #default="scope">
            <div v-if="scope.row.newDate">{{ scope.row.newDate }}</div>
            <div v-else style="color: #999">未指定</div>
            <div v-if="scope.row.newTimeSlot" style="font-size: 12px; color: #666">{{ scope.row.newTimeSlot }}</div>
            <div v-else style="font-size: 12px; color: #999">未指定</div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="调班原因" min-width="200" />
        <el-table-column label="受影响预约" width="120">
          <template #default="scope">
            <el-tag type="warning">{{ scope.row.affectedAppointments || 0 }} 个</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="success"
              size="small"
              @click="reviewAdjustment(scope.row, true)"
              :loading="reviewingIds.includes(scope.row.id)"
            >
              同意
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="reviewAdjustment(scope.row, false)"
              :loading="reviewingIds.includes(scope.row.id)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewForm.approved ? '同意调班申请' : '拒绝调班申请'"
      width="500px"
    >
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见（可选）"
          />
        </el-form-item>
        <el-alert
          v-if="reviewForm.approved && currentAdjustment?.affectedAppointments > 0"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #title>
            <div>同意后将自动调整 <strong>{{ currentAdjustment?.affectedAppointments }}</strong> 个患者的预约时间</div>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button
          :type="reviewForm.approved ? 'success' : 'danger'"
          @click="submitReview"
          :loading="submitting"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/admin'

interface ScheduleAdjustment {
  id: number
  doctorId: number
  scheduleId: number
  originalDate: string
  originalTimeSlot: string
  newDate?: string
  newTimeSlot?: string
  reason: string
  status: number
  createTime: string
  doctor?: {
    id: number
    name: string
    title: string
  }
  affectedAppointments?: number
}

const adjustments = ref<ScheduleAdjustment[]>([])
const loading = ref(false)
const reviewingIds = ref<number[]>([])
const reviewDialogVisible = ref(false)
const submitting = ref(false)
const currentAdjustment = ref<ScheduleAdjustment | null>(null)

const reviewForm = ref({
  approved: true,
  comment: ''
})

onMounted(() => {
  loadAdjustments()
})

const loadAdjustments = async () => {
  loading.value = true
  try {
    const res = await adminApi.getScheduleAdjustments()
    if (res.code === 200) {
      adjustments.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载调班申请失败')
    }
  } catch (error: any) {
    ElMessage.error('加载调班申请失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const reviewAdjustment = (adjustment: ScheduleAdjustment, approved: boolean) => {
  currentAdjustment.value = adjustment
  reviewForm.value.approved = approved
  reviewForm.value.comment = ''
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!currentAdjustment.value) return
  
  submitting.value = true
  try {
    const res = await adminApi.reviewScheduleAdjustment(currentAdjustment.value.id, {
      approved: reviewForm.value.approved,
      comment: reviewForm.value.comment
    })
    
    if (res.code === 200) {
      ElMessage.success(res.message || (reviewForm.value.approved ? '已同意调班申请' : '已拒绝调班申请'))
      reviewDialogVisible.value = false
      await loadAdjustments()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error: any) {
    ElMessage.error('审核失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.schedule-adjustments-manage {
  padding: 20px;
}
</style>


