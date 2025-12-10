<template>
  <div class="payment">
    <el-card style="max-width: 800px; margin: 0 auto">
      <template #header>
        <h2>支付订单</h2>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ orderInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="预约号">{{ appointmentNo }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">
          <span class="amount">¥{{ orderInfo.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ orderInfo.status }}</el-descriptions-item>
      </el-descriptions>
      
      <div class="payment-methods">
        <h3>选择支付方式</h3>
        <el-radio-group v-model="paymentMethod">
          <el-radio :label="1">
            <el-icon><Wallet /></el-icon>
            微信支付
          </el-radio>
          <el-radio :label="2">
            <el-icon><CreditCard /></el-icon>
            支付宝
          </el-radio>
          <el-radio :label="3">
            <el-icon><Document /></el-icon>
            医保支付
          </el-radio>
        </el-radio-group>
      </div>
      
      <div class="action-buttons">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handlePay" :loading="paying">
          确认支付
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Wallet, CreditCard, Document } from '@element-plus/icons-vue'
import request from '../api/request'

const route = useRoute()
const router = useRouter()
const orderInfo = ref<any>({})
const appointmentNo = ref('')
const paymentMethod = ref(1)
const paying = ref(false)

onMounted(() => {
  const paymentId = route.query.paymentId as string
  const appointmentId = route.query.appointmentId as string
  const amount = route.query.amount as string
  appointmentNo.value = route.query.appointmentNo as string || ''
  
  if (paymentId) {
    // 已有订单，直接使用
    orderInfo.value = {
      id: Number(paymentId),
      amount: Number(amount),
      orderNo: route.query.orderNo as string || ''
    }
  } else if (appointmentId && amount) {
    createPayment(Number(appointmentId), Number(amount))
  }
})

const createPayment = async (appointmentId: number, amount: number) => {
  try {
    const res = await request.post('/payment/create', null, {
      params: { appointmentId, amount }
    })
    if (res.code === 200) {
      orderInfo.value = res.data
    }
  } catch (error: any) {
    ElMessage.error('创建订单失败')
  }
}

const handlePay = async () => {
  if (!orderInfo.value.id) {
    ElMessage.error('订单信息不完整')
    return
  }
  
  paying.value = true
  try {
    const res = await request.post('/payment/pay', null, {
      params: {
        paymentId: orderInfo.value.id,
        paymentMethod: paymentMethod.value
      }
    })
    if (res.code === 200) {
      ElMessage.success('支付成功！')
      // 跳转到我的预约页面，并触发刷新
      router.push('/my-appointments').then(() => {
        // 延迟一下确保后端数据已更新，然后触发页面刷新事件
        setTimeout(() => {
          console.log('支付成功，触发刷新事件')
          window.dispatchEvent(new Event('refresh-appointments'))
          // 再次延迟刷新，确保数据已更新
          setTimeout(() => {
            window.dispatchEvent(new Event('refresh-appointments'))
          }, 1000)
        }, 300)
      })
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (error: any) {
    ElMessage.error('支付失败: ' + (error.message || '未知错误'))
  } finally {
    paying.value = false
  }
}

const handleCancel = () => {
  router.back()
}
</script>

<style scoped>
.payment {
  padding: 20px;
}

.amount {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.payment-methods {
  margin: 30px 0;
}

.payment-methods h3 {
  margin-bottom: 20px;
}

.payment-methods .el-radio {
  display: block;
  margin: 15px 0;
  font-size: 16px;
}

.action-buttons {
  margin-top: 30px;
  text-align: center;
}
</style>

