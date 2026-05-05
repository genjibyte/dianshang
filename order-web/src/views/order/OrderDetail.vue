<template>
  <div class="order-detail">
    <el-page-header @back="$router.push('/orders')" title="返回订单列表" />

    <el-card v-loading="loading" class="detail-card">
      <template #header>
        <div class="card-header">
          <span>订单号：{{ order.orderNo }}</span>
          <order-status-tag :status="order.status" />
        </div>
      </template>

      <!-- 状态时间线 -->
      <el-timeline class="status-timeline">
        <el-timeline-item
          v-for="step in timelineSteps"
          :key="step.label"
          :type="step.active ? 'primary' : 'info'"
          :hollow="!step.active"
          :timestamp="step.time"
          placement="top"
        >
          {{ step.label }}
        </el-timeline-item>
      </el-timeline>

      <!-- 订单信息 -->
      <el-descriptions title="订单信息" :column="2" border>
        <el-descriptions-item label="店铺">{{ order.shopName }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="amount">¥{{ order.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ order.deliveryAddress }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ order.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(order.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" v-if="order.status === 4">
          {{ order.cancelReason }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 商品列表 -->
      <h4 style="margin: 20px 0 10px">商品明细</h4>
      <el-table :data="order.items || []" border stripe>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="unitPrice" label="单价" width="120">
          <template #default="{ row }">¥{{ row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="subtotal" label="小计" width="120">
          <template #default="{ row }">¥{{ row.subtotal }}</template>
        </el-table-column>
      </el-table>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button
          v-if="order.status === 0"
          type="primary"
          size="large"
          @click="goToPay"
        >
          立即支付
        </el-button>
        <el-button
          v-if="order.status === 0 || order.status === 1"
          type="danger"
          size="large"
          @click="showCancelDialog = true"
        >
          取消订单
        </el-button>
        <el-button
          v-if="order.status === 1"
          type="warning"
          size="large"
          @click="handleDeliver"
        >
          开始配送
        </el-button>
        <el-button
          v-if="order.status === 2"
          type="success"
          size="large"
          @click="handleComplete"
        >
          确认送达
        </el-button>
      </div>
    </el-card>

    <!-- 支付信息卡片 -->
    <el-card v-if="payment" class="detail-card" style="margin-top: 16px">
      <template #header>支付信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="支付单号">{{ payment.paymentNo }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">
          <span class="amount">¥{{ payment.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <pay-method-tag :method="payment.payMethod" />
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ payment.statusDesc }}</el-descriptions-item>
        <el-descriptions-item label="交易流水号">{{ payment.transactionId }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatDate(payment.paidTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 取消订单对话框 -->
    <el-dialog v-model="showCancelDialog" title="取消订单" width="400px">
      <el-form>
        <el-form-item label="取消原因" required>
          <el-input
            v-model="cancelReason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCancelDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCancel" :loading="cancelLoading">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrder, cancelOrder, deliverOrder, completeOrder } from '@/api/order'
import { getPaymentByOrderNo } from '@/api/payment'
import OrderStatusTag from '@/components/OrderStatusTag.vue'
import PayMethodTag from '@/components/PayMethodTag.vue'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref({})
const payment = ref(null)
const showCancelDialog = ref(false)
const cancelReason = ref('')
const cancelLoading = ref(false)

const timelineSteps = computed(() => {
  const o = order.value
  const steps = [
    { label: '创建订单', time: formatDate(o.createTime), active: true },
    { label: '支付完成', time: formatDate(o.paidTime), active: o.status >= 1 && o.status !== 4 },
    { label: '开始配送', time: formatDate(o.deliveredTime), active: o.status >= 2 && o.status !== 4 },
    { label: '订单完成', time: formatDate(o.completedTime), active: o.status === 3 },
  ]
  if (o.status === 4) {
    steps.push({ label: '已取消', time: formatDate(o.cancelledTime), active: true })
  }
  return steps
})

async function fetchOrder() {
  loading.value = true
  try {
    const res = await getOrder(route.params.orderNo)
    order.value = res.data
    if (res.data.status >= 1 && res.data.status !== 4) {
      try {
        const payRes = await getPaymentByOrderNo(route.params.orderNo)
        payment.value = payRes.data
      } catch (_) {}
    }
  } finally {
    loading.value = false
  }
}

function goToPay() {
  router.push(`/payment/${order.value.orderNo}`)
}

async function handleCancel() {
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  cancelLoading.value = true
  try {
    await cancelOrder({ orderNo: order.value.orderNo, reason: cancelReason.value })
    ElMessage.success('订单已取消')
    showCancelDialog.value = false
    fetchOrder()
  } finally {
    cancelLoading.value = false
  }
}

async function handleDeliver() {
  await ElMessageBox.confirm('确认开始配送？', '提示')
  await deliverOrder(order.value.orderNo)
  ElMessage.success('已开始配送')
  fetchOrder()
}

async function handleComplete() {
  await ElMessageBox.confirm('确认订单已送达？', '提示')
  await completeOrder(order.value.orderNo)
  ElMessage.success('订单已完成')
  fetchOrder()
}

onMounted(fetchOrder)
</script>

<style scoped>
.order-detail { padding: 20px; }
.detail-card { margin-top: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; }
.amount { color: #f56c6c; font-weight: bold; font-size: 16px; }
.status-timeline { margin: 20px 0; }
.actions { margin-top: 24px; display: flex; gap: 12px; justify-content: center; }
</style>
