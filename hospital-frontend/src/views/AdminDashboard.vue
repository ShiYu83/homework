<template>
  <div class="admin-dashboard">
    <el-card>
      <template #header>
        <h2>管理员控制台</h2>
      </template>
      
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">{{ stats.doctors }}</div>
              <div class="stat-label">医生总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">{{ stats.patients }}</div>
              <div class="stat-label">患者总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">{{ stats.appointments }}</div>
              <div class="stat-label">今日预约</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">{{ stats.departments }}</div>
              <div class="stat-label">科室总数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-tabs v-model="activeTab" style="margin-top: 20px">
        <el-tab-pane label="快速导航" name="nav">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" @click="$router.push('/admin/doctors')" style="cursor: pointer">
                <h3>医生管理</h3>
                <p>管理医生信息、职称、专长等</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" @click="$router.push('/admin/patients')" style="cursor: pointer">
                <h3>患者管理</h3>
                <p>管理患者信息、查看患者档案</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" @click="$router.push('/admin/departments')" style="cursor: pointer">
                <h3>科室管理</h3>
                <p>管理科室信息、科室设置</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" @click="$router.push('/admin/rooms')" style="cursor: pointer">
                <h3>诊室管理</h3>
                <p>管理诊室信息、诊室分配</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" @click="$router.push('/admin/schedules')" style="cursor: pointer">
                <h3>号源管理</h3>
                <p>管理医生排班、号源池</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" @click="$router.push('/admin/statistics')" style="cursor: pointer">
                <h3>统计分析</h3>
                <p>查看统计数据、生成报表</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" @click="$router.push('/admin/schedule-adjustments')" style="cursor: pointer">
                <h3>调班审核</h3>
                <p>审核医生调班申请、调整预约时间</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" @click="$router.push('/admin/medicines')" style="cursor: pointer">
                <h3>药品管理</h3>
                <p>管理药品信息、上架下架、库存管理</p>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '../api/admin'

import { ElMessage } from 'element-plus'
import { statisticsApi } from '../api/statistics'

const activeTab = ref('nav')
const loading = ref(false)
const stats = ref({
  doctors: 0,
  patients: 0,
  appointments: 0,
  departments: 0
})

onMounted(() => {
  loadStats()
})

const loadStats = async () => {
  loading.value = true
  try {
    // 尝试使用统计API，如果失败则使用列表API
    try {
      const statsRes = await statisticsApi.getOverview()
      if (statsRes.code === 200 && statsRes.data) {
        stats.value = {
          doctors: statsRes.data.doctors || 0,
          patients: statsRes.data.patients || 0,
          appointments: statsRes.data.appointments || 0,
          departments: statsRes.data.departments || 0
        }
      }
    } catch (e) {
      // 如果统计API不可用，使用列表API计算
      const [doctorsRes, patientsRes, departmentsRes] = await Promise.all([
        adminApi.getDoctors(),
        adminApi.getPatients(),
        adminApi.getDepartments()
      ])
      
      if (doctorsRes.code === 200) stats.value.doctors = doctorsRes.data?.length || 0
      if (patientsRes.code === 200) stats.value.patients = patientsRes.data?.length || 0
      if (departmentsRes.code === 200) stats.value.departments = departmentsRes.data?.length || 0
    }
  } catch (error: any) {
    ElMessage.error('加载统计数据失败: ' + (error.message || '未知错误'))
    console.error('加载统计数据失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}
</style>

