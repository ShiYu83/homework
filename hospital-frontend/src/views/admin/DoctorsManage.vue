<template>
  <div class="doctors-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>医生信息管理</h2>
          <el-button type="primary" @click="showAddDialog">新增医生</el-button>
        </div>
      </template>
      
      <el-form :inline="true" style="margin-bottom: 20px">
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
        <el-form-item>
          <el-button type="primary" @click="loadDoctors">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="doctors" style="width: 100%" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :src="getAvatarUrl(scope.row.avatar)" :size="50">
              <el-icon><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="specialty" label="专长" />
        <el-table-column prop="username" label="账户" width="120">
          <template #default="scope">
            <span v-if="scope.row.username">{{ scope.row.username }}</span>
            <el-tag v-else type="info" size="small">未生成</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button size="small" @click="viewAccount(scope.row)">查看账户</el-button>
            <el-button size="small" @click="editDoctor(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteDoctor(scope.row.id)">删除</el-button>
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
      <el-form :model="doctorForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :on-error="handleAvatarError"
            :auto-upload="true"
            name="file"
          >
            <img v-if="doctorForm.avatar" :src="getAvatarUrl(doctorForm.avatar)" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div style="margin-top: 10px; font-size: 12px; color: #999">
            <p>支持 JPG、PNG 格式，大小不超过 5MB</p>
            <el-button v-if="doctorForm.avatar" size="small" type="danger" @click="removeAvatar">删除头像</el-button>
          </div>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="doctorForm.name" />
        </el-form-item>
        <el-form-item label="科室" prop="departmentId">
          <el-select v-model="doctorForm.departmentId" placeholder="请选择科室" @change="onDepartmentChange">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="诊室" prop="consultingRoomId">
          <el-select v-model="doctorForm.consultingRoomId" placeholder="请选择诊室" clearable>
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="room.name"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入职时间" prop="entryDate">
          <el-date-picker
            v-model="doctorForm.entryDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="选择入职时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-input v-model="doctorForm.title" />
        </el-form-item>
        <el-form-item label="专长">
          <el-input v-model="doctorForm.specialty" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="doctorForm.introduction" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="doctorForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDoctor">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 查看账户对话框 -->
    <el-dialog v-model="accountDialogVisible" title="医生账户信息" width="500px">
      <el-descriptions :column="1" border v-if="accountInfo">
        <el-descriptions-item label="账户">
          <el-text copyable>{{ accountInfo.username }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="密码">
          <el-text copyable>{{ accountInfo.password }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
      <el-alert
        type="info"
        :closable="false"
        style="margin-top: 20px"
      >
        <template #title>
          <span>提示：账户格式为：入职年(4位) + 科室代码(2位) + 诊室编号(4位) = 10位</span>
        </template>
      </el-alert>
      <template #footer>
        <el-button @click="accountDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User } from '@element-plus/icons-vue'
import { adminApi, type Doctor, type Department, type ConsultingRoom } from '../../api/admin'
import { departmentApi } from '../../api/department'
import request from '../../api/request'

const doctors = ref<any[]>([])
const submitting = ref(false)
const departments = ref<Department[]>([])
const availableRooms = ref<ConsultingRoom[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增医生')
const formRef = ref()
const loading = ref(false)
const accountDialogVisible = ref(false)
const accountInfo = ref<{ username: string; password: string } | null>(null)

const searchForm = reactive({
  departmentId: null as number | null
})

const doctorForm = reactive<Partial<Doctor>>({
  name: '',
  departmentId: undefined,
  consultingRoomId: undefined,
  entryDate: '',
  title: '',
  specialty: '',
  introduction: '',
  status: 1,
  avatar: ''
})

// 上传配置
const uploadAction = computed(() => {
  // 直接使用后端服务器地址
  const baseURL = 'http://localhost:9090'
  return `${baseURL}/api/upload/avatar`
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

// 头像URL处理
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
  return `/uploads/avatars/${avatar}`
}

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  consultingRoomId: [{ required: false, message: '请选择诊室', trigger: 'change' }], // 改为非必填
  entryDate: [{ required: false, message: '请选择入职时间', trigger: 'change' }], // 改为非必填，如果没有则使用当前年份
  title: [{ required: true, message: '请输入职称', trigger: 'blur' }]
}

onMounted(() => {
  loadDoctors()
  loadDepartments()
})

const loadDoctors = async () => {
  loading.value = true
  try {
    const res = await adminApi.getDoctors(searchForm.departmentId || undefined)
    if (res.code === 200) {
      doctors.value = res.data || []
      if (doctors.value.length === 0) {
        ElMessage.info('暂无医生数据')
      }
    } else {
      ElMessage.error(res.message || '加载医生列表失败')
    }
  } catch (error: any) {
    ElMessage.error('加载医生列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
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

const onDepartmentChange = async (departmentId: number) => {
  if (departmentId) {
    try {
      const res = await adminApi.getConsultingRooms(departmentId)
      if (res.code === 200) {
        availableRooms.value = res.data || []
      }
    } catch (error: any) {
      ElMessage.error('加载诊室列表失败: ' + (error.message || '未知错误'))
    }
  } else {
    availableRooms.value = []
  }
}

const resetSearch = () => {
  searchForm.departmentId = null
  loadDoctors()
}

const showAddDialog = () => {
  dialogTitle.value = '新增医生'
  Object.assign(doctorForm, {
    name: '',
    departmentId: undefined,
    consultingRoomId: undefined,
    entryDate: '',
    title: '',
    specialty: '',
    introduction: '',
    status: 1,
    avatar: ''
  })
  availableRooms.value = []
  dialogVisible.value = true
}

const editDoctor = async (doctor: any) => {
  dialogTitle.value = '编辑医生'
  Object.assign(doctorForm, doctor)
  if (doctor.departmentId) {
    await onDepartmentChange(doctor.departmentId)
  }
  dialogVisible.value = true
}

const viewAccount = async (doctor: any) => {
  try {
    const res = await adminApi.getDoctorAccount(doctor.id)
    if (res.code === 200) {
      accountInfo.value = res.data
      accountDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '查询账户失败')
    }
  } catch (error: any) {
    ElMessage.error('查询账户失败: ' + (error.message || '未知错误'))
  }
}

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleAvatarSuccess = (response: any, uploadFile: any) => {
  console.log('上传响应:', response)
  // 处理不同的响应格式
  let result = response
  // 如果是经过axios处理的响应，会有config属性，此时取data
  if (response && response.config) {
    result = response.data
  }
  
  // 正确处理后端返回的Result格式
  if (result && result.code === 200 && result.data) {
    doctorForm.avatar = result.data.url
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(result?.message || '头像上传失败')
  }
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败')
}

const removeAvatar = () => {
  doctorForm.avatar = ''
  ElMessage.success('已移除头像')
}

const saveDoctor = async () => {
  if (!formRef.value || submitting.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        submitting.value = true
        if (doctorForm.id) {
          await adminApi.updateDoctor(doctorForm.id, doctorForm as Doctor)
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadDoctors()
        } else {
          const res = await adminApi.createDoctor(doctorForm as Doctor)
          if (res.code === 200) {
            // 显示账户信息
            if (res.data) {
              const accountData = res.data as any
              if (accountData.username && accountData.password) {
                accountInfo.value = {
                  username: accountData.username,
                  password: accountData.password
                }
                accountDialogVisible.value = true
                ElMessage.success('创建成功，账户已生成')
              } else {
                ElMessage.success(res.message || '创建成功')
              }
            } else {
              ElMessage.success(res.message || '创建成功')
            }
            dialogVisible.value = false
            loadDoctors()
          } else {
            ElMessage.error(res.message || '创建失败')
          }
        }
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const deleteDoctor = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该医生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminApi.deleteDoctor(id)
    ElMessage.success('删除成功')
    loadDoctors()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.doctors-manage {
  padding: 20px;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
  }

  :deep(.el-upload:hover) {
    border-color: var(--el-color-primary);
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}
</style>

