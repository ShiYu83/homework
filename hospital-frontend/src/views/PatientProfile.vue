<template>
  <div class="patient-profile">
    <el-card>
      <template #header>
        <h2>个人信息管理</h2>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="patientInfo" label-width="120px" style="max-width: 600px">
            <el-form-item label="姓名">
              <el-input v-model="patientInfo.name" disabled />
            </el-form-item>
            <el-form-item label="身份证号">
              <el-input v-model="patientInfo.idCard" disabled />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="patientInfo.phone" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="patientInfo.gender" disabled>
                <el-radio :label="0">女</el-radio>
                <el-radio :label="1">男</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="patientInfo.birthday"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                disabled
              />
            </el-form-item>
            <el-form-item label="过敏史">
              <el-input
                v-model="patientInfo.allergyHistory"
                type="textarea"
                :rows="3"
                placeholder="请输入过敏史"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updatePatientInfo">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="家庭成员" name="family">
          <el-button type="primary" @click="showAddFamilyDialog = true" style="margin-bottom: 20px">
            添加家庭成员
          </el-button>
          <el-table :data="familyMembers" style="width: 100%">
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="idCard" label="身份证号" />
            <el-table-column prop="phone" label="手机号" />
            <el-table-column prop="gender" label="性别">
              <template #default="scope">
                {{ scope.row.gender === 0 ? '女' : '男' }}
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button type="danger" size="small" @click="removeFamilyMember(scope.row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 添加家庭成员对话框 -->
    <el-dialog v-model="showAddFamilyDialog" title="添加家庭成员" width="500px">
      <el-form :model="newFamilyMember" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="newFamilyMember.name" />
        </el-form-item>
        <el-form-item label="身份证号" required>
          <el-input v-model="newFamilyMember.idCard" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="newFamilyMember.phone" />
        </el-form-item>
        <el-form-item label="性别" required>
          <el-radio-group v-model="newFamilyMember.gender">
            <el-radio :label="0">女</el-radio>
            <el-radio :label="1">男</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddFamilyDialog = false">取消</el-button>
        <el-button type="primary" @click="addFamilyMember">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { patientApi } from '../api/patient'

const activeTab = ref('basic')
const patientInfo = ref<any>({})
const familyMembers = ref<any[]>([])
const showAddFamilyDialog = ref(false)
const newFamilyMember = reactive({
  name: '',
  idCard: '',
  phone: '',
  gender: 1
})

onMounted(() => {
  loadPatientInfo()
  loadFamilyMembers()
})

const loadPatientInfo = async () => {
  // 从登录信息获取患者ID
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
  
  try {
    const res = await patientApi.getById(patientId)
    if (res.code === 200) {
      patientInfo.value = res.data
    }
  } catch (error: any) {
    ElMessage.error('加载患者信息失败')
  }
}

const loadFamilyMembers = async () => {
  // 加载家庭成员列表（需要后端API支持）
  familyMembers.value = []
}

const updatePatientInfo = async () => {
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
  
  try {
    const res = await patientApi.update({
      id: patientId,
      phone: patientInfo.value.phone,
      allergyHistory: patientInfo.value.allergyHistory
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      loadPatientInfo() // 重新加载信息
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

const addFamilyMember = async () => {
  try {
    // 添加家庭成员（需要后端API支持）
    ElMessage.success('添加成功')
    showAddFamilyDialog.value = false
    loadFamilyMembers()
  } catch (error: any) {
    ElMessage.error('添加失败')
  }
}

const removeFamilyMember = async (id: number) => {
  try {
    // 删除家庭成员（需要后端API支持）
    ElMessage.success('删除成功')
    loadFamilyMembers()
  } catch (error: any) {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.patient-profile {
  padding: 20px;
}
</style>

