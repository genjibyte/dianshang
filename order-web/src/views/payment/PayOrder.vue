<template>
  <div class="pay-order">
    <el-page-header @back="$router.push(`/orders/${orderNo}`)" title="返回订单" />

    <el-card v-loading="loading" class="pay-card">
      <template #header>
        <span class="card-title">订单支付</span>
      </template>

      <!-- 订单摘要 -->
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="店铺">{{ order.shopName }}</el-descriptions-item>
        <el-descriptions-item label="商品">
          <span v-for="(item, i) in (order.items || [])" :key="i">
            {{ item.productName }} x{{ item.quantity }}
            <span v-if="i < order.items.length - 1">、</span>
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="支付金额">
          <span class="pay-amount">¥{{ order.totalAmount }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 支付方式选择 -->
      <div class="pay-method-section">
        <h4>选择支付方式</h4>
        <el-radio-group v-model="payMethod" size="large">
          <el-radio-button :label="1">
            <el-icon><Wallet /></el-icon> 余额支付
          </el-radio-button>
          <el-radio-button :label="2">
            <el-icon><ChatDotRound /></el-icon> 微信支付
          </el-radio-button>
          <el-radio-button :label="3">
            <el-icon><Money /></el-icon> 支付宝
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 确认支付 -->
      <div class="pay-action">
        <el-button
          type="primary"
          size="large"
          :loading="paying"
          @click="handlePay"
          style="width: 300px; height: 50px; font-size: 18px"
        >
          确认支付 ¥{{ order.totalAmount }}
        </el-button>
      </div>
    </el-card>

    <!-- 支付结果对话框 -->
    <el-dialog v-model="showResult" title="支付结果" width="400px" :close-on-click-modal="false">
      <el-result
        :icon="payResult.success ? 'success' : 'error'"
        :title="payResult.success ? '支付成功' : '支付失败'"
        :sub-title="payResult.message"
      >
        <template #extra>
          <el-button type="primary" @click="goToDetail">查看订单</el-button>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Wallet, ChatDotRound, Money } from '@element-plus/icons-vue'
import { getOrder } from '@/api/order'
import { payOrder } from '@/api/payment'

const route = useRoute()
const router = useRouter()
const orderNo = route.params.orderNo

const loading = ref(false)
const paying = ref(false)
const order = ref({})
const payMethod = ref(1)
const showResult = ref(false)
const payResult = ref({ success: false, message: '' })

async function fetchOrder() {
  loading.value = true
  try {
    const res = await getOrder(orderNo)
    order.value = res.data
    if (res.data.status !== 0) {
      ElMessage.warning('当前订单状态不允许支付')
      router.push(`/orders/${orderNo}`)
    }
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  paying.value = true
  try {
    const idempotencyKey = `PAY_${orderNo}_${Date.now()}`
    await payOrder({ orderNo, payMethod: payMethod.value }, idempotencyKey)
    payResult.value = { success: true, message: `支付单号已生成，金额 ¥${order.value.totalAmount}` }
    showResult.value = true
  } catch (err) {
    payResult.value = { success: false, message: err.response?.data?.message || '支付失败，请重试' }
    showResult.value = true
  } finally {
    paying.value = false
  }
}

function goToDetail() {
  router.push(`/orders/${orderNo}`)
}

onMounted(fetchOrder)
</script>

<style scoped>
.pay-order { padding: 20px; }
.pay-card { margin-top: 16px; max-width: 600px; margin-left: auto; margin-right: auto; }
.card-title { font-size: 18px; font-weight: bold; }
.pay-amount { color: #f56c6c; font-size: 24px; font-weight: bold; }
.pay-method-section { margin: 30px 0; text-align: center; }
.pay-method-section h4 { margin-bottom: 16px; color: #606266; }
.pay-action { text-align: center; margin-top: 30px; padding-bottom: 10px; }
</style>
