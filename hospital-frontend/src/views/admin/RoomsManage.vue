<template>
  <div class="rooms-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>诊室信息管理</h2>
          <el-button type="primary" @click="showAddDialog">新增诊室</el-button>
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
          <el-button type="primary" @click="loadRooms">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="rooms" style="width: 100%" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roomNo" label="诊室编号" width="120" />
        <el-table-column prop="name" label="诊室名称" width="150" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editRoom(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRoom(scope.row.id)">删除</el-button>
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
      <el-form :model="roomForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="诊室编号" prop="roomNo">
          <el-input v-model="roomForm.roomNo" />
        </el-form-item>
        <el-form-item label="诊室名称" prop="name">
          <el-input v-model="roomForm.name" />
        </el-form-item>
        <el-form-item label="所属科室" prop="departmentId">
          <el-select v-model="roomForm.departmentId" placeholder="请选择科室">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="roomForm.location" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roomForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoom">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, type ConsultingRoom, type Department } from '../../api/admin'
import { departmentApi } from '../../api/department'

const rooms = ref<ConsultingRoom[]>([])
const departments = ref<Department[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增诊室')
const formRef = ref()
const loading = ref(false)

const searchForm = reactive({
  departmentId: null as number | null
})

const roomForm = reactive<Partial<ConsultingRoom>>({
  roomNo: '',
  name: '',
  departmentId: undefined,
  location: '',
  status: 1
})

const rules = {
  roomNo: [{ required: true, message: '请输入诊室编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入诊室名称', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

onMounted(() => {
  loadRooms()
  loadDepartments()
})

const loadRooms = async () => {
  loading.value = true
  try {
    const res = await adminApi.getConsultingRooms(searchForm.departmentId || undefined)
    if (res.code === 200) {
      rooms.value = res.data || []
      if (rooms.value.length === 0) {
        ElMessage.info('暂无诊室数据')
      }
    } else {
      ElMessage.error(res.message || '加载诊室列表失败')
    }
  } catch (error: any) {
    ElMessage.error('加载诊室列表失败: ' + (error.message || '未知错误'))
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

const resetSearch = () => {
  searchForm.departmentId = null
  loadRooms()
}

const showAddDialog = () => {
  dialogTitle.value = '新增诊室'
  Object.assign(roomForm, {
    roomNo: '',
    name: '',
    departmentId: undefined,
    location: '',
    status: 1
  })
  dialogVisible.value = true
}

const editRoom = (room: ConsultingRoom) => {
  dialogTitle.value = '编辑诊室'
  Object.assign(roomForm, room)
  dialogVisible.value = true
}

const saveRoom = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (roomForm.id) {
          await adminApi.updateConsultingRoom(roomForm.id, roomForm as ConsultingRoom)
          ElMessage.success('更新成功')
        } else {
          await adminApi.createConsultingRoom(roomForm as ConsultingRoom)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadRooms()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const deleteRoom = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该诊室吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminApi.deleteConsultingRoom(id)
    ElMessage.success('删除成功')
    loadRooms()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.rooms-manage {
  padding: 20px;
}
</style>

