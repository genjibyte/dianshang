import { defineStore } from 'pinia'
import { getUserOrders, getOrder } from '@/api/order'

export const useOrderStore = defineStore('order', {
  state: () => ({
    // 订单列表
    orderList: [],
    // 当前查看的订单详情
    currentOrder: null,
    // 加载状态
    loading: false,
    // 统计数据
    stats: {
      total: 0,
      pending: 0,
      paid: 0,
      delivering: 0,
      completed: 0,
      cancelled: 0
    }
  }),

  getters: {
    /**
     * 按状态筛选订单
     */
    ordersByStatus: (state) => (status) => {
      if (status === null || status === undefined || status === '') {
        return state.orderList
      }
      return state.orderList.filter(order => order.status === status)
    }
  },

  actions: {
    /**
     * 获取用户订单列表
     * @param {string|number} userId
     */
    async fetchUserOrders(userId) {
      this.loading = true
      try {
        const res = await getUserOrders(userId)
        const list = res.data || []
        this.orderList = list
        this.calculateStats(list)
        return list
      } catch (error) {
        console.error('获取订单列表失败：', error)
        this.orderList = []
        return []
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取订单详情
     * @param {string} orderNo
     */
    async fetchOrderDetail(orderNo) {
      this.loading = true
      try {
        const res = await getOrder(orderNo)
        this.currentOrder = res.data
        return res.data
      } catch (error) {
        console.error('获取订单详情失败：', error)
        this.currentOrder = null
        return null
      } finally {
        this.loading = false
      }
    },

    /**
     * 计算订单统计数据
     * @param {Array} list
     */
    calculateStats(list) {
      this.stats = {
        total: list.length,
        pending: list.filter(o => o.status === 0).length,
        paid: list.filter(o => o.status === 1).length,
        delivering: list.filter(o => o.status === 2).length,
        completed: list.filter(o => o.status === 3).length,
        cancelled: list.filter(o => o.status === 4).length
      }
    },

    /**
     * 清除当前订单
     */
    clearCurrentOrder() {
      this.currentOrder = null
    }
  }
})
