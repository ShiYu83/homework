<template>
  <div class="schedules-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>号源管理</h2>
          <el-button type="primary" @click="showAddDialog">新增排班</el-button>
        </div>
      </template>
      
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="医生">
          <el-select v-model="searchForm.doctorId" placeholder="请选择医生" clearable filterable>
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="searchForm.departmentId" placeholder="请选择科室" clearable>
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="searchForm.startDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="searchForm.endDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadSchedules">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="schedules" style="width: 100%" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
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
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editSchedule(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteSchedule(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="scheduleForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="scheduleForm.doctorId" placeholder="请选择医生" filterable>
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科室" prop="departmentId">
          <el-select v-model="scheduleForm.departmentId" placeholder="请选择科室">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="scheduleDate">
          <el-date-picker
            v-model="scheduleForm.scheduleDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="时段" prop="timeSlot">
          <el-select v-model="scheduleForm.timeSlot" placeholder="请选择时段">
            <el-option
              v-for="slot in timeSlots"
              :key="slot"
              :label="slot"
              :value="slot"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总号源数" prop="totalCount">
          <el-input-number v-model="scheduleForm.totalCount" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="scheduleForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停诊</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, type Schedule, type Doctor, type Department } from '../../api/admin'
import { departmentApi } from '../../api/department'

const schedules = ref<Schedule[]>([])
const doctors = ref<Doctor[]>([])
const departments = ref<Department[]>([])
const timeSlots = ref<string[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增排班')
const formRef = ref()
const loading = ref(false)

const searchForm = reactive({
  doctorId: null as number | null,
  departmentId: null as number | null,
  startDate: '',
  endDate: ''
})

const scheduleForm = reactive<Partial<Schedule>>({
  doctorId: undefined,
  departmentId: undefined,
  scheduleDate: '',
  timeSlot: '',
  totalCount: 20,
  reservedCount: 0,
  status: 1
})

const rules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时段', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入总号源数', trigger: 'blur' }]
}

onMounted(() => {
  loadSchedules()
  loadDoctors()
  loadDepartments()
  loadTimeSlots()
})

const loadSchedules = async () => {
  loading.value = true
  try {
    const res = await adminApi.getSchedules({
      doctorId: searchForm.doctorId || undefined,
      departmentId: searchForm.departmentId || undefined,
      startDate: searchForm.startDate || undefined,
      endDate: searchForm.endDate || undefined
    })
    if (res.code === 200) {
      schedules.value = res.data || []
      if (schedules.value.length === 0) {
        ElMessage.info('暂无排班数据')
      }
    } else {
      ElMessage.error(res.message || '加载排班列表失败')
    }
  } catch (error: any) {
    ElMessage.error('加载排班列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const loadDoctors = async () => {
  try {
    const res = await adminApi.getDoctors()
    if (res.code === 200) {
      doctors.value = res.data || []
    } else {
      ElMessage.warning('加载医生列表失败: ' + (res.message || '未知错误'))
    }
  } catch (error: any) {
    ElMessage.error('加载医生列表失败: ' + (error.message || '未知错误'))
  }
}

const loadDepartments = async () => {
  try {
    const res = await departmentApi.getList()
    if (res.code === 200) {
      departments.value = res.data || []
    } else {
      ElMessage.warning('加载科室列表失败: ' + (res.message || '未知错误'))
    }
  } catch (error: any) {
    ElMessage.error('加载科室列表失败: ' + (error.message || '未知错误'))
  }
}

const loadTimeSlots = async () => {
  try {
    const res = await adminApi.getTimeSlots()
    if (res.code === 200) {
      timeSlots.value = res.data || []
    } else {
      ElMessage.warning('加载时段列表失败: ' + (res.message || '未知错误'))
    }
  } catch (error: any) {
    ElMessage.error('加载时段列表失败: ' + (error.message || '未知错误'))
  }
}

const resetSearch = () => {
  searchForm.doctorId = null
  searchForm.departmentId = null
  searchForm.startDate = ''
  searchForm.endDate = ''
  loadSchedules()
}

const showAddDialog = () => {
  dialogTitle.value = '新增排班'
  Object.assign(scheduleForm, {
    doctorId: undefined,
    departmentId: undefined,
    scheduleDate: '',
    timeSlot: '',
    totalCount: 20,
    reservedCount: 0,
    status: 1
  })
  dialogVisible.value = true
}

const editSchedule = (schedule: Schedule) => {
  dialogTitle.value = '编辑排班'
  Object.assign(scheduleForm, schedule)
  dialogVisible.value = true
}

const saveSchedule = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (scheduleForm.id) {
          await adminApi.updateSchedule(scheduleForm.id, scheduleForm as Schedule)
          ElMessage.success('更新成功')
        } else {
          await adminApi.createSchedule(scheduleForm as Schedule)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadSchedules()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const deleteSchedule = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminApi.deleteSchedule(id)
    ElMessage.success('删除成功')
    loadSchedules()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.schedules-manage {
  padding: 20px;
}
</style>

