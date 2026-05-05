<template>
  <div class="create-order">
    <div class="page-header">
      <h2>创建订单</h2>
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <el-card shadow="never" class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
      >
        <!-- 店铺选择 -->
        <el-divider content-position="left">
          <el-icon><Shop /></el-icon>
          店铺信息
        </el-divider>
        <el-form-item label="选择店铺" prop="shopId">
          <el-select
            v-model="form.shopId"
            placeholder="请选择店铺"
            style="width: 100%; max-width: 400px;"
            @change="handleShopChange"
          >
            <el-option
              v-for="shop in shopList"
              :key="shop.id"
              :label="shop.name"
              :value="shop.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ shop.name }}</span>
                <el-tag size="small" type="info">{{ shop.category }}</el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 商品选择 -->
        <el-divider content-position="left">
          <el-icon><GoodsFilled /></el-icon>
          选择商品
        </el-divider>
        <el-form-item label="商品列表" prop="items">
          <div class="product-list">
            <el-table :data="currentProducts" border stripe style="width: 100%;">
              <el-table-column prop="name" label="商品名称" min-width="160">
                <template #default="{ row }">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <el-icon :size="20" color="#409EFF"><Burger /></el-icon>
                    <span>{{ row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="120" align="right">
                <template #default="{ row }">
                  <span class="price-text">¥{{ row.price.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="200" align="center">
                <template #default="{ row }">
                  <div class="quantity-control">
                    <el-button
                      circle
                      size="small"
                      :icon="Minus"
                      :disabled="getItemQuantity(row.id) === 0"
                      @click="decreaseQuantity(row.id)"
                    />
                    <el-input-number
                      :model-value="getItemQuantity(row.id)"
                      :min="0"
                      :max="99"
                      size="small"
                      controls-position="right"
                      style="width: 80px;"
                      @change="(val) => setQuantity(row.id, val)"
                    />
                    <el-button
                      circle
                      size="small"
                      type="primary"
                      :icon="Plus"
                      @click="increaseQuantity(row.id)"
                    />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="小计" width="120" align="right">
                <template #default="{ row }">
                  <span class="subtotal-text">
                    ¥{{ (row.price * getItemQuantity(row.id)).toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="!form.shopId" class="empty-tip">
              <el-empty description="请先选择店铺" :image-size="80" />
            </div>
          </div>
        </el-form-item>

        <!-- 配送信息 -->
        <el-divider content-position="left">
          <el-icon><Location /></el-icon>
          配送信息
        </el-divider>
        <el-form-item label="配送地址" prop="deliveryAddress">
          <el-input
            v-model="form.deliveryAddress"
            placeholder="请输入详细配送地址"
            maxlength="200"
            show-word-limit
            style="max-width: 600px;"
          >
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="如有特殊要求请在此备注，如：不要辣、多加饭等"
            maxlength="500"
            show-word-limit
            style="max-width: 600px;"
          />
        </el-form-item>

        <!-- 订单汇总 -->
        <el-divider content-position="left">
          <el-icon><Tickets /></el-icon>
          订单汇总
        </el-divider>
        <el-form-item>
          <div class="order-summary">
            <div class="summary-row">
              <span class="summary-label">已选商品：</span>
              <span class="summary-value">{{ selectedItemCount }} 件</span>
            </div>
            <div class="summary-row total-row">
              <span class="summary-label">合计金额：</span>
              <span class="summary-total">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            :disabled="selectedItemCount === 0"
            @click="handleSubmit"
          >
            <el-icon><ShoppingCart /></el-icon>
            提交订单
          </el-button>
          <el-button size="large" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Minus, Plus } from '@element-plus/icons-vue'
import { createOrder } from '@/api/order'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

// 模拟店铺数据
const shopList = ref([
  { id: 1, name: '黄焖鸡米饭（大学城店）', category: '中餐' },
  { id: 2, name: '麦当劳（中心广场店）', category: '快餐' },
  { id: 3, name: '沙县小吃（步行街店）', category: '小吃' },
  { id: 4, name: '蜜雪冰城（科技路店）', category: '饮品' },
  { id: 5, name: '张亮麻辣烫（高新店）', category: '麻辣烫' }
])

// 模拟商品数据
const productsMap = {
  1: [
    { id: 101, name: '黄焖鸡米饭（小份）', price: 18.00 },
    { id: 102, name: '黄焖鸡米饭（大份）', price: 25.00 },
    { id: 103, name: '黄焖排骨米饭', price: 28.00 },
    { id: 104, name: '可乐', price: 5.00 },
    { id: 105, name: '米饭加量', price: 2.00 }
  ],
  2: [
    { id: 201, name: '巨无霸套餐', price: 39.90 },
    { id: 202, name: '麦辣鸡腿堡套餐', price: 35.00 },
    { id: 203, name: '薯条（大份）', price: 13.50 },
    { id: 204, name: '麦旋风', price: 12.00 },
    { id: 205, name: '可口可乐（大杯）', price: 9.00 }
  ],
  3: [
    { id: 301, name: '蒸饺（一笼）', price: 8.00 },
    { id: 302, name: '拌面', price: 10.00 },
    { id: 303, name: '炖罐汤', price: 12.00 },
    { id: 304, name: '卤蛋', price: 2.00 },
    { id: 305, name: '鸡腿', price: 8.00 }
  ],
  4: [
    { id: 401, name: '冰鲜柠檬水', price: 4.00 },
    { id: 402, name: '珍珠奶茶', price: 7.00 },
    { id: 403, name: '芋圆奶茶', price: 8.00 },
    { id: 404, name: '满杯百香果', price: 6.00 },
    { id: 405, name: '摇摇杯草莓', price: 8.00 }
  ],
  5: [
    { id: 501, name: '麻辣烫（小份）', price: 15.00 },
    { id: 502, name: '麻辣烫（大份）', price: 25.00 },
    { id: 503, name: '麻辣烫（特大份）', price: 35.00 },
    { id: 504, name: '凉皮', price: 8.00 },
    { id: 505, name: '酸梅汤', price: 5.00 }
  ]
}

// 表单数据
const form = reactive({
  shopId: null,
  deliveryAddress: '',
  remark: '',
  items: {} // { productId: quantity }
})

// 验证规则
const rules = {
  shopId: [{ required: true, message: '请选择店铺', trigger: 'change' }],
  deliveryAddress: [
    { required: true, message: '请输入配送地址', trigger: 'blur' },
    { min: 5, message: '地址不少于5个字符', trigger: 'blur' }
  ]
}

// 当前店铺的商品
const currentProducts = computed(() => {
  if (!form.shopId) return []
  return productsMap[form.shopId] || []
})

// 已选商品数量
const selectedItemCount = computed(() => {
  return Object.values(form.items).reduce((sum, qty) => sum + (qty || 0), 0)
})

// 合计金额
const totalAmount = computed(() => {
  let total = 0
  for (const product of currentProducts.value) {
    const qty = form.items[product.id] || 0
    total += product.price * qty
  }
  return total
})

function getItemQuantity(productId) {
  return form.items[productId] || 0
}

function setQuantity(productId, val) {
  form.items[productId] = val || 0
}

function increaseQuantity(productId) {
  form.items[productId] = (form.items[productId] || 0) + 1
}

function decreaseQuantity(productId) {
  if (form.items[productId] && form.items[productId] > 0) {
    form.items[productId]--
  }
}

function handleShopChange() {
  // 切换店铺时清空已选商品
  form.items = {}
}

function handleReset() {
  form.shopId = null
  form.deliveryAddress = ''
  form.remark = ''
  form.items = {}
  formRef.value?.resetFields()
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善订单信息')
    return
  }

  // 构建商品列表
  const items = []
  for (const [productId, quantity] of Object.entries(form.items)) {
    if (quantity > 0) {
      items.push({ productId: Number(productId), quantity })
    }
  }

  if (items.length === 0) {
    ElMessage.warning('请至少选择一件商品')
    return
  }

  submitting.value = true
  try {
    const res = await createOrder({
      shopId: form.shopId,
      deliveryAddress: form.deliveryAddress,
      remark: form.remark,
      items
    })
    ElMessage.success('订单创建成功')
    const orderNo = res.data?.orderNo || res.data
    if (orderNo) {
      router.push(`/orders/${orderNo}`)
    } else {
      router.push('/orders')
    }
  } catch (err) {
    // 错误已在拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-order {
  max-width: 1000px;
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

.form-card {
  border-radius: 8px;
}

.form-card :deep(.el-divider__text) {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.product-list {
  width: 100%;
}

.empty-tip {
  padding: 20px 0;
}

.price-text {
  color: #f56c6c;
  font-weight: 500;
}

.subtotal-text {
  color: #e6a23c;
  font-weight: 600;
}

.quantity-control {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.order-summary {
  background-color: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px 24px;
  min-width: 300px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.summary-label {
  font-size: 14px;
  color: #606266;
}

.summary-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.total-row {
  border-top: 1px dashed #dcdfe6;
  margin-top: 8px;
  padding-top: 16px;
}

.summary-total {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}
</style>
