import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/views/layout/AppLayout.vue'

const routes = [
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/home/Dashboard.vue'),
        meta: { title: '控制台', icon: 'DataBoard' }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单列表', icon: 'List' }
      },
      {
        path: 'orders/create',
        name: 'CreateOrder',
        component: () => import('@/views/order/CreateOrder.vue'),
        meta: { title: '创建订单', icon: 'Plus' }
      },
      {
        path: 'orders/:orderNo',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '订单详情', icon: 'Document' },
        props: true
      },
      {
        path: 'payment/:orderNo',
        name: 'PayOrder',
        component: () => import('@/views/payment/PayOrder.vue'),
        meta: { title: '订单支付', icon: 'Money' },
        props: true
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 设置页面标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 外卖订单管理系统`
    : '外卖订单管理系统'
  next()
})

export default router
