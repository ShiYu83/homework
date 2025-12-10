<template>
  <div class="guide">
    <el-card>
      <template #header>
        <h2>智能导诊</h2>
      </template>
      
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="选择症状" />
        <el-step title="推荐科室" />
        <el-step title="选择医生" />
      </el-steps>
      
      <div class="step-content">
        <!-- 步骤1: 选择症状 -->
        <div v-if="activeStep === 0" class="symptom-selection">
          <h3>请选择您的症状</h3>
          <el-row :gutter="20">
            <el-col :span="12" v-for="(symptoms, dept) in symptomMap" :key="dept">
              <el-card shadow="hover" class="dept-card">
                <template #header>
                  <strong>{{ dept }}</strong>
                </template>
                <el-checkbox-group v-model="selectedSymptoms">
                  <el-checkbox
                    v-for="symptom in symptoms"
                    :key="symptom"
                    :label="symptom"
                    @change="handleSymptomChange"
                  >
                    {{ symptom }}
                  </el-checkbox>
                </el-checkbox-group>
              </el-card>
            </el-col>
          </el-row>
          <div class="action-buttons">
            <el-button type="primary" @click="nextStep" :disabled="selectedSymptoms.length === 0">
              下一步
            </el-button>
          </div>
        </div>
        
        <!-- 步骤2: 推荐科室 -->
        <div v-if="activeStep === 1" class="department-recommendation">
          <h3>为您推荐的科室</h3>
          <el-row :gutter="20">
            <el-col :span="8" v-for="dept in recommendedDepartments" :key="dept.id">
              <el-card shadow="hover" class="dept-card" @click="selectDepartment(dept)">
                <h4>{{ dept.name }}</h4>
                <p>{{ dept.description }}</p>
              </el-card>
            </el-col>
          </el-row>
          <div class="action-buttons">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" @click="nextStep" :disabled="!selectedDepartment">
              下一步
            </el-button>
          </div>
        </div>
        
        <!-- 步骤3: 选择医生 -->
        <div v-if="activeStep === 2" class="doctor-selection">
          <h3>选择医生</h3>
          <el-table :data="recommendedDoctors" style="width: 100%">
            <el-table-column prop="name" label="医生姓名" />
            <el-table-column prop="title" label="职称" />
            <el-table-column prop="specialty" label="专长" />
            <el-table-column prop="introduction" label="简介" />
            <el-table-column label="操作">
              <template #default="scope">
                <el-button type="primary" @click="selectDoctor(scope.row)">选择</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="action-buttons">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" @click="goToAppointment" :disabled="!selectedDoctor">
              去预约
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const activeStep = ref(0)
const symptomMap = ref<Record<string, string[]>>({})
const selectedSymptoms = ref<string[]>([])
const recommendedDepartments = ref<any[]>([])
const selectedDepartment = ref<any>(null)
const recommendedDoctors = ref<any[]>([])
const selectedDoctor = ref<any>(null)

onMounted(() => {
  loadSymptoms()
})

const loadSymptoms = async () => {
  try {
    const res = await request.get('/guide/symptoms')
    if (res.code === 200) {
      symptomMap.value = res.data
    }
  } catch (error: any) {
    ElMessage.error('加载症状列表失败')
  }
}

const handleSymptomChange = () => {
  // 症状选择变化时的处理
}

const nextStep = async () => {
  if (activeStep.value === 0) {
    // 根据症状推荐科室
    if (selectedSymptoms.value.length > 0) {
      try {
        const res = await request.get('/guide/recommend', {
          params: { symptom: selectedSymptoms.value[0] }
        })
        if (res.code === 200) {
          recommendedDepartments.value = res.data
          activeStep.value = 1
        }
      } catch (error: any) {
        ElMessage.error('获取推荐科室失败')
      }
    }
  } else if (activeStep.value === 1) {
    // 加载医生列表
    if (selectedDepartment.value) {
      try {
        const res = await request.get('/guide/doctors', {
          params: { departmentId: selectedDepartment.value.id }
        })
        if (res.code === 200) {
          recommendedDoctors.value = res.data
          activeStep.value = 2
        }
      } catch (error: any) {
        ElMessage.error('获取医生列表失败')
      }
    }
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const selectDepartment = (dept: any) => {
  selectedDepartment.value = dept
}

const selectDoctor = (doctor: any) => {
  selectedDoctor.value = doctor
}

const goToAppointment = () => {
  if (selectedDoctor.value) {
    router.push({
      path: '/appointment',
      query: { doctorId: selectedDoctor.value.id }
    })
  }
}
</script>

<style scoped>
.guide {
  padding: 20px;
}

.step-content {
  margin-top: 40px;
  min-height: 400px;
}

.symptom-selection,
.department-recommendation,
.doctor-selection {
  padding: 20px;
}

.dept-card {
  margin-bottom: 20px;
  cursor: pointer;
}

.dept-card:hover {
  border-color: #409eff;
}

.action-buttons {
  margin-top: 30px;
  text-align: center;
}
</style>

