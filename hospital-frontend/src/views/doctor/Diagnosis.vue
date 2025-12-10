<template>
  <div class="diagnosis">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>看病开药</h2>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <!-- 患者信息 -->
      <el-card style="margin-bottom: 20px" v-if="appointment">
        <template #header>
          <h3>患者信息</h3>
        </template>
        <el-descriptions :column="2" border v-if="patient">
          <el-descriptions-item label="姓名">{{ patient.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            <el-tag :type="patient.gender === 1 ? 'primary' : 'danger'">
              {{ patient.gender === 1 ? '男' : '女' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">{{ patient.phone }}</el-descriptions-item>
          <el-descriptions-item label="预约号">{{ appointment.appointmentNo }}</el-descriptions-item>
          <el-descriptions-item label="预约日期">{{ formatDate(appointment.appointmentDate) }}</el-descriptions-item>
          <el-descriptions-item label="时段">{{ appointment.timeSlot }}</el-descriptions-item>
          <el-descriptions-item label="过敏史" :span="2" v-if="patient.allergyHistory">
            <el-tag type="warning">{{ patient.allergyHistory }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 诊断信息 -->
      <el-card style="margin-bottom: 20px">
        <template #header>
          <h3>诊断信息</h3>
        </template>
        <el-form :model="diagnosisForm" label-width="100px">
          <el-form-item label="症状描述">
            <el-input
              v-model="diagnosisForm.symptoms"
              type="textarea"
              :rows="3"
              placeholder="请输入患者症状描述"
            />
          </el-form-item>
          <el-form-item label="诊断结果">
            <el-input
              v-model="diagnosisForm.diagnosis"
              type="textarea"
              :rows="3"
              placeholder="请输入诊断结果"
            />
          </el-form-item>
          <el-form-item label="医嘱建议">
            <el-input
              v-model="diagnosisForm.advice"
              type="textarea"
              :rows="3"
              placeholder="请输入医嘱建议"
            />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 开药 -->
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <h3>开药</h3>
            <el-button type="primary" @click="showMedicineDialog">
              <el-icon><Plus /></el-icon> 添加药品
            </el-button>
          </div>
        </template>

        <!-- 药品搜索 -->
        <div style="margin-bottom: 20px">
          <el-input
            v-model="medicineKeyword"
            placeholder="搜索药品名称"
            style="width: 300px"
            clearable
            @input="searchMedicines"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 已选药品列表 -->
        <el-table :data="selectedMedicines" style="width: 100%" empty-text="暂未添加药品">
          <el-table-column prop="medicineName" label="药品名称" min-width="150" />
          <el-table-column prop="specification" label="规格" width="120" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="scope">
              ¥{{ scope.row.price?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="数量" width="150">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.quantity"
                :min="1"
                :max="scope.row.stock"
                @change="updateSubtotal(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="subtotal" label="小计" width="100">
            <template #default="scope">
              ¥{{ scope.row.subtotal?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="danger" size="small" @click="removeMedicine(scope.$index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 总金额 -->
        <div style="margin-top: 20px; text-align: right">
          <el-text size="large" type="primary">
            处方总金额：<strong style="font-size: 20px">¥{{ totalAmount.toFixed(2) }}</strong>
          </el-text>
        </div>
      </el-card>

      <!-- 提交按钮 -->
      <div style="margin-top: 30px; text-align: center">
        <el-button size="large" @click="goBack">取消</el-button>
        <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
          提交诊断和处方
        </el-button>
      </div>
    </el-card>

    <!-- 药品选择对话框 -->
    <el-dialog
      v-model="medicineDialogVisible"
      title="选择药品"
      width="800px"
    >
      <el-table
        :data="availableMedicines"
        style="width: 100%"
        v-loading="medicineLoading"
        @row-click="selectMedicine"
      >
        <el-table-column prop="name" label="药品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" size="small" @click="selectMedicine(scope.row)">
              选择
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { medicineApi, type Medicine } from '../../api/medicine'
import { doctorApi } from '../../api/doctor'
import { patientApi } from '../../api/patient'
import request from '../../api/request'

const route = useRoute()
const router = useRouter()

const appointment = ref<any>(null)
const patient = ref<any>(null)
const diagnosisForm = ref({
  symptoms: '',
  diagnosis: '',
  advice: ''
})
const selectedMedicines = ref<Array<{
  medicineId: number
  medicineName: string
  specification?: string
  unit: string
  price: number
  quantity: number
  subtotal: number
  stock: number
}>>([])
const medicineKeyword = ref('')
const availableMedicines = ref<Medicine[]>([])
const medicineDialogVisible = ref(false)
const medicineLoading = ref(false)
const submitting = ref(false)

const totalAmount = computed(() => {
  return selectedMedicines.value.reduce((sum, item) => sum + (item.subtotal || 0), 0)
})

onMounted(async () => {
  const appointmentId = route.query.appointmentId as string
  if (appointmentId) {
    await loadAppointmentData(Number(appointmentId))
  } else {
    ElMessage.error('缺少预约ID')
    goBack()
  }
})

const loadAppointmentData = async (appointmentId: number) => {
  try {
    // 获取预约信息
    const res = await request.get(`/appointment/${appointmentId}`)
    if (res.code === 200) {
      appointment.value = res.data
      if (appointment.value && appointment.value.patientId) {
        // 获取患者信息
        const patientRes = await patientApi.getById(appointment.value.patientId)
        if (patientRes.code === 200) {
          patient.value = patientRes.data
        } else {
          ElMessage.warning('获取患者信息失败')
        }
      } else {
        ElMessage.error('预约信息不完整')
      }
    } else {
      ElMessage.error(res.message || '加载预约信息失败')
    }
  } catch (error: any) {
    ElMessage.error('加载预约信息失败: ' + (error.message || '未知错误'))
  }
}

const searchMedicines = async () => {
  medicineLoading.value = true
  try {
    const res = await medicineApi.getAvailableMedicines(medicineKeyword.value)
    if (res.code === 200) {
      availableMedicines.value = res.data || []
    }
  } catch (error: any) {
    ElMessage.error('搜索药品失败: ' + (error.message || '未知错误'))
  } finally {
    medicineLoading.value = false
  }
}

const showMedicineDialog = async () => {
  medicineDialogVisible.value = true
  await searchMedicines()
}

const selectMedicine = (medicine: Medicine) => {
  // 检查是否已添加
  const existing = selectedMedicines.value.find(m => m.medicineId === medicine.id)
  if (existing) {
    ElMessage.warning('该药品已添加')
    return
  }

  selectedMedicines.value.push({
    medicineId: medicine.id,
    medicineName: medicine.name,
    specification: medicine.specification,
    unit: medicine.unit,
    price: medicine.price,
    quantity: 1,
    subtotal: medicine.price,
    stock: medicine.stock
  })

  medicineDialogVisible.value = false
  ElMessage.success('药品已添加')
}

const removeMedicine = (index: number) => {
  selectedMedicines.value.splice(index, 1)
}

const updateSubtotal = (item: any) => {
  item.subtotal = item.price * item.quantity
}

const handleSubmit = async () => {
  if (!diagnosisForm.value.diagnosis && selectedMedicines.value.length === 0) {
    ElMessage.warning('请至少填写诊断结果或添加药品')
    return
  }

  submitting.value = true
  try {
    let medicalRecordId: number | null = null

    // 如果有诊断信息，创建病例
    if (diagnosisForm.value.diagnosis || diagnosisForm.value.symptoms || diagnosisForm.value.advice) {
      const recordRes = await doctorApi.createMedicalRecord({
        appointmentId: appointment.value.id,
        diagnosis: diagnosisForm.value.diagnosis,
        symptoms: diagnosisForm.value.symptoms,
        advice: diagnosisForm.value.advice
      })
      if (recordRes.code === 200) {
        medicalRecordId = recordRes.data.id
      }
    }

    // 如果有药品，创建处方
    let prescriptionId: number | null = null
    if (selectedMedicines.value.length > 0) {
      if (!medicalRecordId) {
        ElMessage.error('创建处方前需要先创建病例')
        submitting.value = false
        return
      }

      const prescriptionRes = await doctorApi.createPrescription({
        medicalRecordId: medicalRecordId,
        appointmentId: appointment.value.id,
        items: selectedMedicines.value.map(m => ({
          medicineId: m.medicineId,
          quantity: m.quantity,
          price: m.price,
          subtotal: m.subtotal
        }))
      })
      if (prescriptionRes.code === 200) {
        prescriptionId = prescriptionRes.data.id
      }
    }

    ElMessage.success('诊断和处方提交成功')
    goBack()
  } catch (error: any) {
    ElMessage.error('提交失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/doctor/patient-queue')
}

const formatDate = (date: string | Date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.diagnosis {
  padding: 20px;
}
</style>

