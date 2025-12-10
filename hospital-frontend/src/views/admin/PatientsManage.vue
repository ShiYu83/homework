<template>
  <div class="patients-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>患者信息管理</h2>
          <el-button type="primary" @click="showAddDialog">新增患者</el-button>
        </div>
      </template>
      
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/手机号/身份证号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPatients">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="patients" style="width: 100%" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="scope">
            {{ scope.row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="creditScore" label="诚信度" width="100">
          <template #default="scope">
            <el-tag :type="getCreditType(scope.row.creditScore || 100)">
              {{ scope.row.creditScore || 100 }}分
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editPatient(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deletePatient(scope.row.id)">删除</el-button>
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
      <el-form :model="patientForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="patientForm.name" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="patientForm.idCard" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="patientForm.phone" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="patientForm.gender">
            <el-radio :label="0">女</el-radio>
            <el-radio :label="1">男</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="patientForm.birthday"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="过敏史">
          <el-input v-model="patientForm.allergyHistory" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePatient">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, type Patient } from '../../api/admin'

const patients = ref<Patient[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增患者')
const formRef = ref()
const loading = ref(false)

const searchForm = reactive({
  keyword: ''
})

const patientForm = reactive<Partial<Patient>>({
  name: '',
  idCard: '',
  phone: '',
  gender: 1,
  birthday: '',
  allergyHistory: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

onMounted(() => {
  loadPatients()
})

const loadPatients = async () => {
  loading.value = true
  try {
    const res = await adminApi.getPatients(searchForm.keyword || undefined)
    if (res.code === 200) {
      patients.value = res.data || []
      if (patients.value.length === 0 && searchForm.keyword) {
        ElMessage.info('未找到匹配的患者')
      }
    } else {
      ElMessage.error(res.message || '加载患者列表失败')
    }
  } catch (error: any) {
    ElMessage.error('加载患者列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.keyword = ''
  loadPatients()
}

const getCreditType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  return 'danger'
}

const showAddDialog = () => {
  dialogTitle.value = '新增患者'
  Object.assign(patientForm, {
    name: '',
    idCard: '',
    phone: '',
    gender: 1,
    birthday: '',
    allergyHistory: ''
  })
  dialogVisible.value = true
}

const editPatient = (patient: Patient) => {
  dialogTitle.value = '编辑患者'
  Object.assign(patientForm, patient)
  dialogVisible.value = true
}

const savePatient = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (patientForm.id) {
          await adminApi.updatePatient(patientForm.id, patientForm as Patient)
          ElMessage.success('更新成功')
        } else {
          await adminApi.createPatient(patientForm as Patient)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadPatients()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const deletePatient = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该患者吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminApi.deletePatient(id)
    ElMessage.success('删除成功')
    loadPatients()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.patients-manage {
  padding: 20px;
}
</style>

