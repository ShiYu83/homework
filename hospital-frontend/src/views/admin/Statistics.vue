<template>
  <div class="statistics">
    <el-card>
      <template #header>
        <h2>统计分析</h2>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="预约统计" name="appointment">
          <el-form :inline="true" style="margin-bottom: 20px">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="appointmentParams.startDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker
                v-model="appointmentParams.endDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAppointmentStats">查询</el-button>
              <el-button @click="generateReport">生成报表</el-button>
            </el-form-item>
          </el-form>
          
          <el-row :gutter="20" v-if="appointmentStats">
            <el-col :span="6">
              <el-statistic title="总预约数" :value="appointmentStats.total" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="待支付" :value="appointmentStats.pending" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="已预约" :value="appointmentStats.confirmed" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="已取消" :value="appointmentStats.cancelled" />
            </el-col>
          </el-row>
        </el-tab-pane>
        
        <el-tab-pane label="排班统计" name="schedule">
          <el-form :inline="true" style="margin-bottom: 20px">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="scheduleParams.startDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker
                v-model="scheduleParams.endDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadScheduleStats">查询</el-button>
            </el-form-item>
          </el-form>
          
          <el-row :gutter="20" v-if="scheduleStats">
            <el-col :span="6">
              <el-statistic title="总排班数" :value="scheduleStats.total" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="总号源数" :value="scheduleStats.totalCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="已预约数" :value="scheduleStats.reservedCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="剩余号源" :value="scheduleStats.availableCount" />
            </el-col>
          </el-row>
          <div v-if="scheduleStats" style="margin-top: 20px">
            <el-progress
              :percentage="scheduleStats.utilizationRate"
              :format="(percentage) => `利用率: ${percentage.toFixed(2)}%`"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { statisticsApi, type AppointmentStatistics, type ScheduleStatistics } from '../../api/statistics'

const activeTab = ref('appointment')
const appointmentStats = ref<AppointmentStatistics | null>(null)
const scheduleStats = ref<ScheduleStatistics | null>(null)

const appointmentParams = reactive({
  startDate: '',
  endDate: ''
})

const scheduleParams = reactive({
  startDate: '',
  endDate: ''
})

onMounted(() => {
  loadAppointmentStats()
  loadScheduleStats()
})

const loadAppointmentStats = async () => {
  try {
    const res = await statisticsApi.getAppointmentStatistics({
      startDate: appointmentParams.startDate || undefined,
      endDate: appointmentParams.endDate || undefined
    })
    if (res.code === 200) {
      appointmentStats.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载预约统计失败')
  }
}

const loadScheduleStats = async () => {
  try {
    const res = await statisticsApi.getScheduleStatistics({
      startDate: scheduleParams.startDate || undefined,
      endDate: scheduleParams.endDate || undefined
    })
    if (res.code === 200) {
      scheduleStats.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载排班统计失败')
  }
}

const generateReport = async () => {
  try {
    const res = await statisticsApi.generateReport({
      startDate: appointmentParams.startDate || undefined,
      endDate: appointmentParams.endDate || undefined
    })
    if (res.code === 200) {
      ElMessage.success('报表生成成功')
      // 这里可以添加打印功能
      window.print()
    }
  } catch (error) {
    ElMessage.error('生成报表失败')
  }
}
</script>

<style scoped>
.statistics {
  padding: 20px;
}
</style>

