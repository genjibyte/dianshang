/**
 * 订单状态映射
 */
export const ORDER_STATUS_MAP = {
  0: { label: '待支付', type: 'warning' },
  1: { label: '已支付', type: 'primary' },
  2: { label: '配送中', type: '' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'info' }
}

/**
 * 支付方式映射
 */
export const PAY_METHOD_MAP = {
  1: { label: '余额支付', type: 'warning', icon: 'Wallet' },
  2: { label: '微信支付', type: 'success', icon: 'ChatDotRound' },
  3: { label: '支付宝支付', type: 'primary', icon: 'CreditCard' }
}

/**
 * 获取订单状态标签
 * @param {number} status
 * @returns {string}
 */
export function getStatusLabel(status) {
  return ORDER_STATUS_MAP[status]?.label || '未知状态'
}

/**
 * 获取订单状态类型（用于 el-tag）
 * @param {number} status
 * @returns {string}
 */
export function getStatusType(status) {
  return ORDER_STATUS_MAP[status]?.type || 'info'
}

/**
 * 获取支付方式标签
 * @param {number} method
 * @returns {string}
 */
export function getPayMethodLabel(method) {
  return PAY_METHOD_MAP[method]?.label || '未知方式'
}

/**
 * 获取支付方式类型
 * @param {number} method
 * @returns {string}
 */
export function getPayMethodType(method) {
  return PAY_METHOD_MAP[method]?.type || 'info'
}

/**
 * 格式化日期时间
 * @param {string|Date} dateStr
 * @returns {string}
 */
export function formatDateTime(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化货币金额
 * @param {number} amount 金额（分）
 * @returns {string}
 */
export function formatCurrency(amount) {
  if (amount === null || amount === undefined) return '¥0.00'
  return `¥${(Number(amount) / 100).toFixed(2)}`
}

/**
 * 格式化货币金额（元为单位）
 * @param {number} amount 金额（元）
 * @returns {string}
 */
export function formatCurrencyYuan(amount) {
  if (amount === null || amount === undefined) return '¥0.00'
  return `¥${Number(amount).toFixed(2)}`
}
