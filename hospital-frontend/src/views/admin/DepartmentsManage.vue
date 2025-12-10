<template>
  <div class="departments-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>科室信息管理</h2>
          <el-button type="primary" @click="showAddDialog">新增科室</el-button>
        </div>
      </template>
      
      <el-table :data="departments" style="width: 100%" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="科室名称" width="150" />
        <el-table-column prop="code" label="科室编码" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editDepartment(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteDepartment(scope.row.id)">删除</el-button>
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
      <el-form :model="departmentForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="departmentForm.name" />
        </el-form-item>
        <el-form-item label="科室编码" prop="code">
          <el-input v-model="departmentForm.code" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="departmentForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="departmentForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDepartment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, type Department } from '../../api/admin'

const departments = ref<Department[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增科室')
const formRef = ref()
const loading = ref(false)

const departmentForm = reactive<Partial<Department>>({
  name: '',
  code: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入科室编码', trigger: 'blur' }]
}

onMounted(() => {
  loadDepartments()
})

const loadDepartments = async () => {
  loading.value = true
  try {
    const res = await adminApi.getDepartments()
    if (res.code === 200) {
      departments.value = res.data || []
      if (departments.value.length === 0) {
        ElMessage.info('暂无科室数据')
      }
    } else {
      ElMessage.error(res.message || '加载科室列表失败')
    }
  } catch (error: any) {
    ElMessage.error('加载科室列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  dialogTitle.value = '新增科室'
  Object.assign(departmentForm, {
    name: '',
    code: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const editDepartment = (department: Department) => {
  dialogTitle.value = '编辑科室'
  Object.assign(departmentForm, department)
  dialogVisible.value = true
}

const saveDepartment = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (departmentForm.id) {
          await adminApi.updateDepartment(departmentForm.id, departmentForm as Department)
          ElMessage.success('更新成功')
        } else {
          await adminApi.createDepartment(departmentForm as Department)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadDepartments()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

const deleteDepartment = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该科室吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await adminApi.deleteDepartment(id)
    ElMessage.success('删除成功')
    loadDepartments()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}
</script>

<style scoped>
.departments-manage {
  padding: 20px;
}
</style>

