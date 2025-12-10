<template>
  <div class="patient-queue">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>患者队列查询</h2>
          <el-date-picker
            v-model="selectedDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            @change="loadQueue"
          />
        </div>
      </template>
      
      <el-table :data="queue" style="width: 100%" v-loading="loading" empty-text="暂无患者队列">
        <el-table-column prop="appointmentNo" label="预约号" width="150" />
        <el-table-column label="患者信息" min-width="250">
          <template #default="scope">
            <div v-if="scope.row.patient">
              <div style="margin-bottom: 5px">
                <strong style="font-size: 16px">{{ scope.row.patient.name }}</strong>
                <el-tag v-if="scope.row.patient.gender === 1" size="small" style="margin-left: 8px">男</el-tag>
                <el-tag v-else-if="scope.row.patient.gender === 0" size="small" type="danger" style="margin-left: 8px">女</el-tag>
              </div>
              <div style="font-size: 13px; color: #666; margin-bottom: 3px">
                <el-icon><Phone /></el-icon> {{ scope.row.patient.phone }}
              </div>
              <div style="font-size: 13px; color: #666; margin-bottom: 3px" v-if="scope.row.patient.birthday">
                <el-icon><Calendar /></el-icon> {{ formatDate(scope.row.patient.birthday) }}
              </div>
              <div style="font-size: 12px; color: #f56c6c; margin-top: 5px" v-if="scope.row.patient.allergyHistory">
                <el-icon><Warning /></el-icon> 过敏史：{{ scope.row.patient.allergyHistory }}
              </div>
            </div>
            <el-empty v-else description="患者信息缺失" :image-size="50" />
          </template>
        </el-table-column>
        <el-table-column prop="timeSlot" label="时段" width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
            <div v-if="scope.row.status === 1" style="font-size: 11px; color: #999; margin-top: 2px">
              (待支付)
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="appointmentDate" label="预约日期" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.appointmentDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预约时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button
              v-if="!scope.row.completeTime && scope.row.patient"
              type="primary"
              size="small"
              @click="callPatient(scope.row)"
              :loading="callingIds.includes(scope.row.appointmentId)"
            >
              <el-icon><Bell /></el-icon> 叫号
            </el-button>
            <el-button
              v-if="scope.row.completeTime && scope.row.patient"
              type="success"
              size="small"
              @click="goToDiagnosis(scope.row)"
            >
              <el-icon><Document /></el-icon> 看病
            </el-button>
            <!-- 查看病例按钮：如果已有病例记录，显示查看按钮 -->
            <el-button
              v-if="scope.row.hasMedicalRecord && scope.row.patient"
              type="info"
              size="small"
              @click="showMedicalRecord(scope.row)"
            >
              <el-icon><View /></el-icon> 查看病例
            </el-button>
            <el-tag v-if="scope.row.completeTime && !scope.row.patient" type="success" size="small">
              <el-icon><Check /></el-icon> 已叫号
            </el-tag>
            <span v-else-if="!scope.row.patient" style="color: #999; font-size: 12px">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 病例详情对话框 -->
    <el-dialog v-model="recordDialogVisible" title="患者病例详情" width="900px">
      <div v-if="currentRecord" v-loading="recordLoading">
        <!-- 患者基本信息 -->
        <el-card style="margin-bottom: 20px">
          <template #header>
            <h3>患者信息</h3>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ currentRecord.patient?.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              <el-tag :type="currentRecord.patient?.gender === 1 ? 'primary' : 'danger'">
                {{ currentRecord.patient?.gender === 1 ? '男' : '女' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="手机号">{{ currentRecord.patient?.phone }}</el-descriptions-item>
            <el-descriptions-item label="预约号">{{ currentRecord.appointment?.appointmentNo }}</el-descriptions-item>
            <el-descriptions-item label="预约日期">{{ formatDate(currentRecord.appointment?.appointmentDate) }}</el-descriptions-item>
            <el-descriptions-item label="时段">{{ currentRecord.appointment?.timeSlot }}</el-descriptions-item>
            <el-descriptions-item label="过敏史" :span="2" v-if="currentRecord.patient?.allergyHistory">
              <el-tag type="warning">{{ currentRecord.patient.allergyHistory }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 病例信息 -->
        <el-card style="margin-bottom: 20px" v-if="currentRecord.medicalRecord">
          <template #header>
            <h3>病例信息</h3>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="症状描述" v-if="currentRecord.medicalRecord.symptoms">
              <div style="white-space: pre-wrap;">{{ currentRecord.medicalRecord.symptoms }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="诊断结果" v-if="currentRecord.medicalRecord.diagnosis">
              <div style="white-space: pre-wrap; color: #409EFF; font-weight: 500;">{{ currentRecord.medicalRecord.diagnosis }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="医嘱建议" v-if="currentRecord.medicalRecord.advice">
              <div style="white-space: pre-wrap;">{{ currentRecord.medicalRecord.advice }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" v-if="currentRecord.medicalRecord.createTime">
              {{ formatDateTime(currentRecord.medicalRecord.createTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 处方信息 -->
        <el-card v-if="currentRecord.prescription">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <h3>处方信息</h3>
              <el-tag :type="currentRecord.prescription.status === 2 ? 'success' : 'warning'">
                {{ currentRecord.prescription.status === 2 ? '已支付' : '待支付' }}
              </el-tag>
            </div>
          </template>
          <el-table 
            :data="currentRecord.prescriptionItems" 
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
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-size: 16px; font-weight: 500; color: #303133;">处方总金额：</span>
              <span style="font-size: 20px; font-weight: bold; color: #f56c6c;">
                ¥{{ currentRecord.prescription.totalAmount?.toFixed(2) || '0.00' }}
              </span>
            </div>
          </div>
        </el-card>

        <el-empty 
          v-if="!recordLoading && !currentRecord.medicalRecord && !currentRecord.prescription" 
          description="暂无病例和处方信息"
          :image-size="100"
        >
          <template #description>
            <p style="color: #909399; margin-top: 10px;">该患者尚未创建病例和处方</p>
          </template>
        </el-empty>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Phone, Calendar, Warning, Bell, Check, Document, View } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { doctorApi, type PatientQueueItem } from '../../api/doctor'
import request from '../../api/request'

const router = useRouter()
const queue = ref<any[]>([])
const selectedDate = ref('')
const loading = ref(false)
const callingIds = ref<number[]>([])
const recordDialogVisible = ref(false)
const recordLoading = ref(false)
const currentRecord = ref<any>(null)

onMounted(() => {
  // 默认选择今天
  const today = new Date()
  selectedDate.value = today.toISOString().split('T')[0]
  loadQueue()
})

const loadQueue = async () => {
  loading.value = true
  try {
    const res = await doctorApi.getPatientQueue(selectedDate.value)
    if (res.code === 200) {
      // 过滤掉没有患者信息的记录（只显示有完整患者信息的预约）
      queue.value = (res.data || []).filter((item: any) => item.patient != null)
      
      // 检查每个预约是否有病例记录
      for (const item of queue.value) {
        try {
          const recordRes = await request.get(`/medical-record/appointment/${item.appointmentId}`).catch(() => ({ code: 404 }))
          item.hasMedicalRecord = recordRes.code === 200 && recordRes.data !== null
        } catch (e) {
          item.hasMedicalRecord = false
        }
      }
      
      if (queue.value.length === 0) {
        ElMessage.warning({
          message: `该日期（${formatDate(selectedDate.value)}）暂无患者队列`,
          duration: 3000
        })
      } else {
        // 不显示成功消息，避免干扰
        console.log(`已加载 ${queue.value.length} 位患者`)
      }
    } else {
      ElMessage.error(res.message || '加载患者队列失败')
    }
  } catch (error: any) {
    ElMessage.error('加载患者队列失败: ' + (error.message || '未知错误'))
    console.error('加载患者队列失败:', error)
  } finally {
    loading.value = false
  }
}

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '待支付',
    2: '已预约',
    3: '已取消',
    4: '已完成'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    1: 'warning',
    2: 'success',
    3: 'info',
    4: ''
  }
  return typeMap[status] || ''
}

const goToDiagnosis = (item: any) => {
  router.push({
    path: '/doctor/diagnosis',
    query: { appointmentId: item.appointmentId }
  })
}

const callPatient = async (item: any) => {
  if (callingIds.value.includes(item.appointmentId)) {
    return
  }
  
  if (!item.patient) {
    ElMessage.warning('患者信息缺失，无法叫号')
    return
  }
  
  // 只要医生点击叫号就会成功，无需确认
  callingIds.value.push(item.appointmentId)
  try {
    const res = await doctorApi.callPatient(item.appointmentId)
    if (res.code === 200) {
      ElMessage.success(`叫号成功：${item.patient.name} (${item.appointmentNo})`)
      // 刷新队列
      await loadQueue()
    } else {
      ElMessage.error(res.message || '叫号失败')
    }
  } catch (error: any) {
    ElMessage.error('叫号失败: ' + (error.message || '未知错误'))
  } finally {
    const index = callingIds.value.indexOf(item.appointmentId)
    if (index > -1) {
      callingIds.value.splice(index, 1)
    }
  }
}

const showMedicalRecord = async (item: any) => {
  if (!item || !item.appointmentId) {
    ElMessage.error('预约信息不完整')
    return
  }
  
  recordDialogVisible.value = true
  recordLoading.value = true
  currentRecord.value = null
  
  try {
    // 并行获取预约、病例和处方信息
    const [appointmentRes, recordRes, prescriptionRes] = await Promise.all([
      request.get(`/appointment/${item.appointmentId}`).catch(() => ({ code: 404, data: null })),
      request.get(`/medical-record/appointment/${item.appointmentId}`).catch(() => ({ code: 404, data: null })),
      request.get(`/prescription/appointment/${item.appointmentId}`).catch(() => ({ code: 404, data: null }))
    ])
    
    let appointment = null
    if (appointmentRes.code === 200 && appointmentRes.data) {
      appointment = appointmentRes.data
    }
    
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
        const detailRes = await doctorApi.getPrescriptionDetail(prescription.id)
        if (detailRes.code === 200 && detailRes.data) {
          prescriptionItems = detailRes.data.items || []
        }
      } catch (e) {
        console.warn('获取处方详情失败:', e)
      }
    }
    
    currentRecord.value = {
      patient: item.patient,
      appointment,
      medicalRecord,
      prescription,
      prescriptionItems
    }
  } catch (error: any) {
    ElMessage.error('加载病例信息失败: ' + (error.message || '未知错误'))
  } finally {
    recordLoading.value = false
  }
}

const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit' 
    })
  } catch {
    return dateStr
  }
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
.patient-queue {
  padding: 20px;
}
</style>

