import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: 1,
    username: '测试用户',
    balance: 99999,
    avatar: ''
  }),

  getters: {
    /**
     * 格式化余额显示
     */
    formattedBalance: (state) => {
      return `¥${Number(state.balance).toFixed(2)}`
    }
  },

  actions: {
    /**
     * 设置用户ID
     * @param {number} id
     */
    setUserId(id) {
      this.userId = id
    },

    /**
     * 设置用户信息
     * @param {object} info
     */
    setUserInfo(info) {
      if (info.userId !== undefined) this.userId = info.userId
      if (info.username !== undefined) this.username = info.username
      if (info.balance !== undefined) this.balance = info.balance
      if (info.avatar !== undefined) this.avatar = info.avatar
    },

    /**
     * 更新余额
     * @param {number} amount
     */
    updateBalance(amount) {
      this.balance = amount
    }
  }
})
