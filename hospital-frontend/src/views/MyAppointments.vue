<template>
  <div class="my-appointments">
    <el-card>
      <template #header>
        <h2>我的预约</h2>
      </template>
      
      <el-table :data="appointments" style="width: 100%" v-loading="loading" empty-text="暂无预约记录">
        <el-table-column prop="appointmentNo" label="预约号" width="150" />
        <el-table-column prop="appointmentDate" label="预约日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="150" />
        <el-table-column prop="fee" label="挂号费" width="100">
          <template #default="scope">
            ¥{{ scope.row.fee }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <!-- 已完成状态（status=4）：只显示查看详情，不显示修改和取消 -->
            <template v-if="getAppointmentStatus(scope.row) === 4">
              <el-button
                type="info"
                size="small"
                @click="showDetailDialog(scope.row)"
              >
                查看详情
              </el-button>
            </template>
            <!-- 已预约状态（status=2）：显示修改、取消、查看详情 -->
            <template v-else-if="getAppointmentStatus(scope.row) === 2">
              <el-button
                type="primary"
                size="small"
                @click="showModifyDialog(scope.row)"
              >
                修改预约
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="cancelAppointment(scope.row)"
              >
                取消预约
              </el-button>
              <el-button
                type="info"
                size="small"
                @click="showDetailDialog(scope.row)"
              >
                查看详情
              </el-button>
            </template>
            <!-- 待支付状态（status=1）：显示取消、查看详情（如果有病例或处方） -->
            <template v-else-if="getAppointmentStatus(scope.row) === 1">
              <el-button
                type="danger"
                size="small"
                @click="cancelAppointment(scope.row)"
              >
                取消预约
              </el-button>
              <el-button
                v-if="scope.row.hasMedicalRecord || scope.row.hasPrescription"
                type="info"
                size="small"
                @click="showDetailDialog(scope.row)"
              >
                查看详情
              </el-button>
            </template>
            <!-- 其他状态：显示查看详情（如果有病例或处方） -->
            <el-button
              v-else-if="scope.row.hasMedicalRecord || scope.row.hasPrescription"
              type="info"
              size="small"
              @click="showDetailDialog(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 详情对话框 -->
      <el-dialog v-model="detailDialogVisible" title="预约详情" width="900px">
        <div v-if="currentDetail" v-loading="detailLoading">
          <!-- 预约基本信息 -->
          <el-card style="margin-bottom: 20px">
            <template #header>
              <h3>预约信息</h3>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="预约号">{{ currentDetail.appointment?.appointmentNo }}</el-descriptions-item>
              <el-descriptions-item label="预约日期">{{ formatDate(currentDetail.appointment?.appointmentDate) }}</el-descriptions-item>
              <el-descriptions-item label="时间段">{{ currentDetail.appointment?.timeSlot }}</el-descriptions-item>
              <el-descriptions-item label="挂号费">¥{{ currentDetail.appointment?.fee?.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(currentDetail.appointment?.status)">
                  {{ getStatusText(currentDetail.appointment?.status) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 病例信息 -->
          <el-card style="margin-bottom: 20px" v-if="currentDetail.medicalRecord">
            <template #header>
              <h3>病例信息</h3>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="症状描述" v-if="currentDetail.medicalRecord.symptoms">
                <div style="white-space: pre-wrap;">{{ currentDetail.medicalRecord.symptoms }}</div>
              </el-descriptions-item>
              <el-descriptions-item label="诊断结果" v-if="currentDetail.medicalRecord.diagnosis">
                <div style="white-space: pre-wrap; color: #409EFF; font-weight: 500;">{{ currentDetail.medicalRecord.diagnosis }}</div>
              </el-descriptions-item>
              <el-descriptions-item label="医嘱建议" v-if="currentDetail.medicalRecord.advice">
                <div style="white-space: pre-wrap;">{{ currentDetail.medicalRecord.advice }}</div>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间" v-if="currentDetail.medicalRecord.createTime">
                {{ formatDateTime(currentDetail.medicalRecord.createTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 处方信息 -->
          <el-card v-if="currentDetail.prescription">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <h3>处方信息</h3>
                <el-tag :type="currentDetail.prescription.status === 2 ? 'success' : 'warning'">
                  {{ currentDetail.prescription.status === 2 ? '已支付' : '待支付' }}
                </el-tag>
              </div>
            </template>
            <el-table 
              :data="currentDetail.prescriptionItems" 
              style="width: 100%"
              empty-text="暂无药品"
              border
            >
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="medicineName" label="药品名称" min-width="150" />
              <el-table-column prop="specification" label="规格" width="120" />
              <el-table-column prop="unit" label="单位" width="80" align="center" />
              <el-table-column prop="quantity" label="数量" width="80" align="center" />
              <el-table-column prop="price" label="单价" width="100" align="right">
                <template #default="scope">
                  ¥{{ scope.row.price?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="subtotal" label="小计" width="120" align="right">
                <template #default="scope">
                  <strong style="color: #409EFF;">¥{{ scope.row.subtotal?.toFixed(2) }}</strong>
                </template>
              </el-table-column>
            </el-table>
            <div style="margin-top: 20px; padding: 15px; background-color: #f5f7fa; border-radius: 4px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                <span style="font-size: 14px; color: #606266;">挂号费：</span>
                <span style="font-size: 14px; color: #606266;">¥{{ currentDetail.appointment?.fee?.toFixed(2) || '0.00' }}</span>
              </div>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                <span style="font-size: 14px; color: #606266;">药品费：</span>
                <span style="font-size: 14px; color: #606266;">¥{{ currentDetail.prescription.totalAmount?.toFixed(2) || '0.00' }}</span>
              </div>
              <el-divider />
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 16px; font-weight: 500; color: #303133;">合计：</span>
                <span style="font-size: 20px; font-weight: bold; color: #f56c6c;">
                  ¥{{ ((currentDetail.appointment?.fee || 0) + (currentDetail.prescription.totalAmount || 0)).toFixed(2) }}
                </span>
              </div>
            </div>
            <div style="margin-top: 20px; text-align: right" v-if="currentDetail.prescription.status === 1">
              <el-button type="primary" size="large" @click="payCombinedOrder(currentDetail.appointment)">
                <el-icon style="margin-right: 5px;"><CreditCard /></el-icon>
                支付订单（挂号费+药品费）
              </el-button>
            </div>
          </el-card>

          <el-empty 
            v-if="!detailLoading && !currentDetail.medicalRecord && !currentDetail.prescription" 
            description="暂无病例和处方信息"
            :image-size="100"
          >
            <template #description>
              <p style="color: #909399; margin-top: 10px;">医生尚未为您创建病例和处方</p>
            </template>
          </el-empty>
        </div>
      </el-dialog>

      <el-dialog v-model="modifyDialogVisible" title="修改预约" width="500px">
        <el-form :model="modifyForm" label-width="100px">
          <el-form-item label="选择日期">
            <el-date-picker
              v-model="modifyForm.date"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="选择时段">
            <el-select v-model="modifyForm.scheduleId" placeholder="请选择时段">
              <el-option
                v-for="slot in availableSlots"
                :key="slot.scheduleId"
                :label="`${slot.timeSlot} (剩余${slot.remaining}个)`"
                :value="slot.scheduleId"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="modifyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmModify" :loading="modifying">确定</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, onActivated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CreditCard } from '@element-plus/icons-vue'
import { appointmentApi } from '../api/appointment'
import { patientApi } from '../api/patient'
import type { TimeSlotDTO } from '../api/appointment'
import request from '../api/request'

const router = useRouter()
const route = useRoute()

const appointments = ref<any[]>([])
const creditInfo = ref<any>(null)
const modifyDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const modifying = ref(false)
const loading = ref(false)
const currentAppointment = ref<any>(null)
const currentDetail = ref<any>(null)
const availableSlots = ref<TimeSlotDTO[]>([])
const modifyForm = ref({
  date: '',
  scheduleId: null as number | null
})

// 刷新处理函数
const handleRefresh = () => {
  console.log('刷新预约列表')
  loadAppointments()
}

onMounted(() => {
  loadAppointments()
  loadCreditInfo()
  
  // 监听刷新事件
  window.addEventListener('refresh-appointments', handleRefresh)
})

// 组件激活时也刷新（从其他页面返回时）
onActivated(() => {
  console.log('组件激活，刷新数据')
  loadAppointments()
})

// 组件卸载时移除监听器
onUnmounted(() => {
  window.removeEventListener('refresh-appointments', handleRefresh)
})

// 监听路由变化，从支付页面返回时刷新数据
watch(() => route.path, (newPath) => {
  if (newPath === '/my-appointments') {
    // 从其他页面返回时刷新数据
    console.log('路由变化到我的预约页面，刷新数据')
    loadAppointments()
  }
})

const loadAppointments = async () => {
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
  
  loading.value = true
  try {
    const res = await appointmentApi.queryByPatient(patientId)
    if (res.code === 200) {
      appointments.value = (res.data || []).map((appointment: any) => {
        // 确保status是数字类型
        if (typeof appointment.status === 'string') {
          appointment.status = parseInt(appointment.status, 10)
        } else if (appointment.status != null) {
          appointment.status = Number(appointment.status)
        }
        console.log(`预约 ${appointment.appointmentNo} 状态:`, appointment.status, typeof appointment.status)
        return appointment
      })
      
      // 检查每个预约是否有病例或处方
      for (const appointment of appointments.value) {
        // 确保 appointment.id 存在且有效
        if (!appointment.id) {
          appointment.hasMedicalRecord = false
          appointment.hasPrescription = false
          continue
        }
        
        try {
          // 并行检查病例和处方
          const [recordRes, prescriptionRes] = await Promise.all([
            request.get(`/medical-record/appointment/${appointment.id}`).catch(() => ({ code: 404 })),
            request.get(`/prescription/appointment/${appointment.id}`).catch(() => ({ code: 404 }))
          ])
          
          appointment.hasMedicalRecord = recordRes.code === 200 && recordRes.data !== null
          appointment.hasPrescription = prescriptionRes.code === 200 && prescriptionRes.data !== null
        } catch (e) {
          appointment.hasMedicalRecord = false
          appointment.hasPrescription = false
        }
      }
      
      if (appointments.value.length === 0) {
        ElMessage.info('暂无预约记录')
      }
    } else {
      ElMessage.error(res.message || '加载预约记录失败')
    }
  } catch (error: any) {
    ElMessage.error('加载预约记录失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const cancelAppointment = async (appointment: any) => {
  try {
    await ElMessageBox.prompt('请输入取消预约的原因:', '取消预约', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入取消原因',
      inputValidator: (value) => {
        if (!value || value.trim() === '') {
          return '取消原因不能为空'
        }
        return true
      }
    }).then(async ({ value: reason }) => {
      const res = await appointmentApi.cancel(appointment.id, reason)
      if (res.code === 200) {
        ElMessage.success('预约取消成功')
        // 重新加载预约列表
        loadAppointments()
      } else {
        ElMessage.error(res.message || '取消预约失败')
      }
    })
  } catch (error: any) {
    if (error === 'cancel') {
      // 用户取消了操作，不做处理
      return
    }
    ElMessage.error('取消预约失败: ' + (error.message || '未知错误'))
  }
}

// 获取预约状态的辅助函数，确保返回数字类型
const getAppointmentStatus = (appointment: any): number => {
  if (!appointment || appointment.status == null) return 0
  const status = appointment.status
  if (typeof status === 'string') {
    return parseInt(status, 10)
  }
  return Number(status)
}

const getStatusText = (status: number | string) => {
  const statusNum = typeof status === 'string' ? parseInt(status, 10) : Number(status)
  const statusMap: Record<number, string> = {
    1: '待支付',
    2: '已预约',
    3: '已取消',
    4: '已完成',
    5: '已爽约'
  }
  return statusMap[statusNum] || '未知'
}

const getStatusType = (status: number | string) => {
  const statusNum = typeof status === 'string' ? parseInt(status, 10) : Number(status)
  const typeMap: Record<number, string> = {
    1: 'warning',  // 待支付 - 橙色
    2: 'success',  // 已预约 - 绿色
    3: 'info',     // 已取消 - 灰色
    4: 'success',  // 已完成 - 绿色
    5: 'danger'    // 已爽约 - 红色
  }
  return typeMap[statusNum] || 'info'
}

const loadCreditInfo = async () => {
  try {
    const res = await patientApi.getCreditInfo()
    if (res.code === 200) {
      creditInfo.value = res.data
    }
  } catch (error) {
    console.error('加载诚信度信息失败:', error)
  }
}

const showModifyDialog = (appointment: any) => {
  currentAppointment.value = appointment
  modifyForm.value = {
    date: appointment.appointmentDate,
    scheduleId: null
  }
  modifyDialogVisible.value = true
  loadAvailableSlots()
}

const loadAvailableSlots = async () => {
  if (!modifyForm.value.date) return
  
  try {
    const res = await appointmentApi.getAvailableSlots({
      doctorId: currentAppointment.value.doctorId,
      date: modifyForm.value.date
    })
    if (res.code === 200) {
      availableSlots.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载可用时段失败')
  }
}

const showDetailDialog = async (appointment: any) => {
  if (!appointment || !appointment.id) {
    ElMessage.error('预约信息不完整')
    return
  }
  
  currentAppointment.value = appointment
  detailDialogVisible.value = true
  detailLoading.value = true
  currentDetail.value = null
  
  try {
    // 并行获取病例和处方
    const [recordRes, prescriptionRes] = await Promise.all([
      request.get(`/medical-record/appointment/${appointment.id}`).catch(() => ({ code: 404, data: null })),
      request.get(`/prescription/appointment/${appointment.id}`).catch(() => ({ code: 404, data: null }))
    ])
    
    let medicalRecord = null
    if (recordRes.code === 200 && recordRes.data) {
      medicalRecord = recordRes.data
    }

    let prescription = null
    let prescriptionItems: any[] = []
    
    if (prescriptionRes.code === 200 && prescriptionRes.data) {
      prescription = prescriptionRes.data
      // 获取处方详情
      try {
        const detailRes = await patientApi.getPrescriptionDetail(prescription.id)
        if (detailRes.code === 200 && detailRes.data) {
          prescriptionItems = detailRes.data.items || []
        }
      } catch (e) {
        console.warn('获取处方详情失败:', e)
      }
    }

    currentDetail.value = {
      appointment,
      medicalRecord,
      prescription,
      prescriptionItems
    }
  } catch (error: any) {
    ElMessage.error('加载详情失败: ' + (error.message || '未知错误'))
  } finally {
    detailLoading.value = false
  }
}

const payCombinedOrder = async (appointment: any) => {
  if (!appointment || !appointment.id) {
    ElMessage.error('预约信息不完整')
    return
  }
  
  try {
    // 创建合并订单
    const orderRes = await patientApi.createCombinedOrder({
      appointmentId: appointment.id,
      prescriptionId: currentDetail.value?.prescription?.id || null
    })
    
    if (orderRes.code === 200) {
      const paymentId = orderRes.data.id
      // 跳转到支付页面
      router.push({
        path: '/payment',
        query: {
          paymentId: paymentId,
          amount: orderRes.data.amount,
          orderNo: orderRes.data.orderNo
        }
      })
    } else {
      ElMessage.error(orderRes.message || '创建订单失败')
    }
  } catch (error: any) {
    ElMessage.error('创建订单失败: ' + (error.message || '未知错误'))
  }
}

const confirmModify = async () => {
  if (!modifyForm.value.scheduleId) {
    ElMessage.warning('请选择时段')
    return
  }
  
  const slot = availableSlots.value.find(s => s.scheduleId === modifyForm.value.scheduleId)
  if (!slot) {
    ElMessage.warning('请选择有效的时段')
    return
  }
  
  modifying.value = true
  try {
    const res = await appointmentApi.modify(currentAppointment.value.id, {
      scheduleId: modifyForm.value.scheduleId,
      timeSlot: slot.timeSlot
    })
    
    if (res.code === 200) {
      ElMessage.success('修改成功')
      modifyDialogVisible.value = false
      loadAppointments()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')
  } finally {
    modifying.value = false
  }
}

const formatDate = (date: string | Date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN')
}

const formatDateTime = (dateTime: string | Date) => {
  if (!dateTime) return ''
  const d = new Date(dateTime)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>

<style scoped>
.my-appointments {
  padding: 20px;
}
</style>