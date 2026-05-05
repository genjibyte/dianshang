<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>控制台</h2>
      <p class="page-desc">欢迎使用外卖订单管理系统，以下是您的订单概览</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card stat-total">
          <div class="stat-icon">
            <el-icon :size="32"><DataBoard /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">全部订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card stat-pending">
          <div class="stat-icon">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待支付</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card stat-paid">
          <div class="stat-icon">
            <el-icon :size="32"><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.paid }}</div>
            <div class="stat-label">已支付</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card stat-delivering">
          <div class="stat-icon">
            <el-icon :size="32"><Van /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.delivering }}</div>
            <div class="stat-label">配送中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card stat-completed">
          <div class="stat-icon">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card stat-cancelled">
          <div class="stat-icon">
            <el-icon :size="32"><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.cancelled }}</div>
            <div class="stat-label">已取消</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="$router.push('/orders/create')">
              <el-icon><CirclePlus /></el-icon>
              创建新订单
            </el-button>
            <el-button type="success" size="large" @click="$router.push('/orders')">
              <el-icon><List /></el-icon>
              查看全部订单
            </el-button>
            <el-button size="large" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近订单</span>
              <el-button text type="primary" @click="$router.push('/orders')">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="loading">
            <el-table
              :data="recentOrders"
              stripe
              size="small"
              :show-header="true"
              empty-text="暂无订单数据"
              style="width: 100%"
            >
              <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <OrderStatusTag :status="row.status" size="small" />
                </template>
              </el-table-column>
              <el-table-column label="金额" width="100" align="right">
                <template #default="{ row }">
                  {{ formatCurrencyYuan(row.totalAmount) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    link
                    size="small"
                    @click="$router.push(`/orders/${row.orderNo}`)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useOrderStore } from '@/store/modules/order'
import { useUserStore } from '@/store/modules/user'
import OrderStatusTag from '@/components/OrderStatusTag.vue'
import { formatCurrencyYuan } from '@/utils/format'

const orderStore = useOrderStore()
const userStore = useUserStore()

const loading = computed(() => orderStore.loading)
const stats = computed(() => orderStore.stats)

const recentOrders = computed(() => {
  const list = [...orderStore.orderList]
  // 按创建时间倒序，取最近5条
  list.sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
  return list.slice(0, 5)
})

function handleRefresh() {
  orderStore.fetchUserOrders(userStore.userId)
}

onMounted(() => {
  orderStore.fetchUserOrders(userStore.userId)
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: #909399;
}

.stat-row {
  margin-bottom: 0;
}

.stat-card {
  border-radius: 8px;
  cursor: default;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-total .stat-icon { background-color: #ecf5ff; color: #409EFF; }
.stat-pending .stat-icon { background-color: #fdf6ec; color: #E6A23C; }
.stat-paid .stat-icon { background-color: #ecf5ff; color: #409EFF; }
.stat-delivering .stat-icon { background-color: #f0f9eb; color: #67C23A; }
.stat-completed .stat-icon { background-color: #f0f9eb; color: #67C23A; }
.stat-cancelled .stat-icon { background-color: #f4f4f5; color: #909399; }

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
