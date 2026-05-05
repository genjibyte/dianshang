import request from './request'
import { v4 as uuidv4 } from 'uuid'

/**
 * 创建订单
 * @param {object} data - { shopId, deliveryAddress, remark, items: [{ productId, quantity }] }
 * @returns {Promise}
 */
export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    headers: {
      'Idempotency-Key': uuidv4()
    },
    data
  })
}

/**
 * 查询订单详情
 * @param {string} orderNo - 订单号
 * @returns {Promise}
 */
export function getOrder(orderNo) {
  return request({
    url: `/orders/${orderNo}`,
    method: 'get'
  })
}

/**
 * 查询用户订单列表
 * @param {string|number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserOrders(userId) {
  return request({
    url: `/orders/user/${userId}`,
    method: 'get'
  })
}

/**
 * 取消订单
 * @param {object} data - { orderNo, reason }
 * @returns {Promise}
 */
export function cancelOrder(data) {
  return request({
    url: '/orders/cancel',
    method: 'post',
    data
  })
}

/**
 * 订单配送
 * @param {string} orderNo - 订单号
 * @returns {Promise}
 */
export function deliverOrder(orderNo) {
  return request({
    url: `/orders/${orderNo}/deliver`,
    method: 'post'
  })
}

/**
 * 订单完成
 * @param {string} orderNo - 订单号
 * @returns {Promise}
 */
export function completeOrder(orderNo) {
  return request({
    url: `/orders/${orderNo}/complete`,
    method: 'post'
  })
}
