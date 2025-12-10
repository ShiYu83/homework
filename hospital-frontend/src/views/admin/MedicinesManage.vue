<template>
  <div class="medicines-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <h2>药品管理</h2>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 添加药品
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div style="margin-bottom: 20px">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索药品名称"
          style="width: 300px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 药品列表 -->
      <el-table :data="filteredMedicines" style="width: 100%" v-loading="loading" empty-text="暂无药品">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="药品名称" min-width="150" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="showEditDialog(scope.row)">
              编辑
            </el-button>
            <el-button
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="药品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number
            v-model="form.price"
            :precision="2"
            :min="0"
            style="width: 100%"
            placeholder="请输入单价"
          />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：盒、瓶、片" />
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="form.specification" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number
            v-model="form.stock"
            :min="0"
            style="width: 100%"
            placeholder="请输入库存"
          />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">下架</el-radio>
            <el-radio :label="1">上架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { medicineApi, type Medicine } from '../../api/medicine'

const medicines = ref<Medicine[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('添加药品')
const submitting = ref(false)
const formRef = ref()

const form = ref<Partial<Medicine>>({
  name: '',
  price: 0,
  unit: '',
  specification: '',
  stock: 0,
  category: '',
  status: 0
})

const rules = {
  name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const filteredMedicines = computed(() => {
  if (!searchKeyword.value) {
    return medicines.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return medicines.value.filter(m => m.name.toLowerCase().includes(keyword))
})

onMounted(() => {
  loadMedicines()
})

const loadMedicines = async () => {
  loading.value = true
  try {
    const res = await medicineApi.getAllMedicines()
    if (res.code === 200) {
      medicines.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载药品列表失败')
    }
  } catch (error: any) {
    ElMessage.error('加载药品列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索逻辑已在computed中实现
}

const showAddDialog = () => {
  dialogTitle.value = '添加药品'
  form.value = {
    name: '',
    price: 0,
    unit: '',
    specification: '',
    stock: 0,
    category: '',
    status: 0
  }
  dialogVisible.value = true
}

const showEditDialog = (medicine: Medicine) => {
  dialogTitle.value = '编辑药品'
  form.value = { ...medicine }
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (form.value.id) {
        // 更新
        const res = await medicineApi.updateMedicine(form.value.id, form.value)
        if (res.code === 200) {
          ElMessage.success('药品更新成功')
          dialogVisible.value = false
          loadMedicines()
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } else {
        // 创建
        const res = await medicineApi.createMedicine(form.value)
        if (res.code === 200) {
          ElMessage.success('药品创建成功')
          dialogVisible.value = false
          loadMedicines()
        } else {
          ElMessage.error(res.message || '创建失败')
        }
      }
    } catch (error: any) {
      ElMessage.error('操作失败: ' + (error.message || '未知错误'))
    } finally {
      submitting.value = false
    }
  })
}

const toggleStatus = async (medicine: Medicine) => {
  const newStatus = medicine.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  
  try {
    await ElMessageBox.confirm(`确定要${action}该药品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await medicineApi.updateStatus(medicine.id, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadMedicines()
    } else {
      ElMessage.error(res.message || `${action}失败`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败: ` + (error.message || '未知错误'))
    }
  }
}

const handleDelete = async (medicine: Medicine) => {
  try {
    await ElMessageBox.confirm('确定要删除该药品吗？删除后无法恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await medicineApi.deleteMedicine(medicine.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadMedicines()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}
</script>

<style scoped>
.medicines-manage {
  padding: 20px;
}
</style>

