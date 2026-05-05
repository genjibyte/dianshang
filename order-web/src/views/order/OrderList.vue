<template>
  <div class="order-list">
    <div class="page-header">
      <h2>订单列表</h2>
      <el-button type="primary" @click="$router.push('/orders/create')">
        <el-icon><CirclePlus /></el-icon>
        创建订单
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="订单状态">
          <el-select
            v-model="filterStatus"
            placeholder="全部状态"
            clearable
            style="width: 160px;"
          >
            <el-option label="全部状态" :value="''" />
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="配送中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号搜索">
          <el-input
            v-model="searchOrderNo"
            placeholder="输入订单号搜索"
            clearable
            style="width: 260px;"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单表格 -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="paginatedOrders"
        stripe
        border
        style="width: 100%"
        empty-text="暂无订单数据"
        @row-click="handleRowClick"
        row-class-name="clickable-row"
      >
        <el-table-column prop="orderNo" label="订单号" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="goDetail(row.orderNo)">
              {{ row.orderNo }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="shopId" label="店铺ID" width="100" align="center" />
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">{{ formatCurrencyYuan(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <OrderStatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="deliveryAddress" label="配送地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="下单时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="goDetail(row.orderNo)">
              详情
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              size="small"
              @click.stop="goPay(row.orderNo)"
            >
              去支付
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              link
              size="small"
              @click.stop="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              size="small"
              @click.stop="handleDeliver(row)"
            >
              配送
            </el-button>
            <el-button
              v-if="row.status === 2"
              type="success"
              link
              size="small"
              @click.stop="handleComplete(row)"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredOrders.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <!-- 取消订单对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消订单"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="订单号">
          <el-text>{{ cancelForm.orderNo }}</el-text>
        </el-form-item>
        <el-form-item label="取消原因">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消订单的原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="cancelLoading" @click="confirmCancel">
          确认取消订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useOrderStore } from '@/store/modules/order'
import { useUserStore } from '@/store/modules/user'
import { cancelOrder, deliverOrder, completeOrder } from '@/api/order'
import OrderStatusTag from '@/components/OrderStatusTag.vue'
import { formatDateTime, formatCurrencyYuan } from '@/utils/format'

const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()

const loading = computed(() => orderStore.loading)
const filterStatus = ref('')
const searchOrderNo = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 取消订单
const cancelDialogVisible = ref(false)
const cancelLoading = ref(false)
const cancelForm = reactive({
  orderNo: '',
  reason: ''
})

// 根据筛选条件过滤订单
const filteredOrders = computed(() => {
  let list = orderStore.orderList
  if (filterStatus.value !== '' && filterStatus.value !== null && filterStatus.value !== undefined) {
    list = list.filter(o => o.status === filterStatus.value)
  }
  if (searchOrderNo.value.trim()) {
    const keyword = searchOrderNo.value.trim().toLowerCase()
    list = list.filter(o => o.orderNo && o.orderNo.toLowerCase().includes(keyword))
  }
  // 按创建时间倒序
  return [...list].sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
})

// 分页数据
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredOrders.value.slice(start, start + pageSize.value)
})

function goDetail(orderNo) {
  router.push(`/orders/${orderNo}`)
}

function goPay(orderNo) {
  router.push(`/payment/${orderNo}`)
}

function handleRowClick(row) {
  goDetail(row.orderNo)
}

function handleSearch() {
  currentPage.value = 1
}

function handleRefresh() {
  orderStore.fetchUserOrders(userStore.userId)
}

// 取消订单
function handleCancel(row) {
  cancelForm.orderNo = row.orderNo
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

async function confirmCancel() {
  if (!cancelForm.reason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }
  cancelLoading.value = true
  try {
    await cancelOrder({
      orderNo: cancelForm.orderNo,
      reason: cancelForm.reason
    })
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    orderStore.fetchUserOrders(userStore.userId)
  } catch (err) {
    // 错误已在拦截器处理
  } finally {
    cancelLoading.value = false
  }
}

// 配送
async function handleDeliver(row) {
  try {
    await ElMessageBox.confirm('确认将此订单标记为配送中？', '确认配送', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await deliverOrder(row.orderNo)
    ElMessage.success('订单已开始配送')
    orderStore.fetchUserOrders(userStore.userId)
  } catch (err) {
    if (err !== 'cancel') {
      // 错误已在拦截器处理
    }
  }
}

// 完成
async function handleComplete(row) {
  try {
    await ElMessageBox.confirm('确认此订单已完成？', '确认完成', {
      type: 'success',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await completeOrder(row.orderNo)
    ElMessage.success('订单已完成')
    orderStore.fetchUserOrders(userStore.userId)
  } catch (err) {
    if (err !== 'cancel') {
      // 错误已在拦截器处理
    }
  }
}

onMounted(() => {
  orderStore.fetchUserOrders(userStore.userId)
})
</script>

<style scoped>
.order-list {
  max-width: 1400px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.filter-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.filter-card :deep(.el-card__body) {
  padding: 16px 20px 0;
}

.table-card {
  border-radius: 8px;
}

.amount-text {
  font-weight: 600;
  color: #f56c6c;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.clickable-row) {
  cursor: pointer;
}
</style>
