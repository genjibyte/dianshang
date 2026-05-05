import request from './request'
import { v4 as uuidv4 } from 'uuid'

/**
 * 发起支付
 * @param {object} data - { orderNo, payMethod }
 * @returns {Promise}
 */
export function payOrder(data) {
  return request({
    url: '/payments',
    method: 'post',
    headers: {
      'Idempotency-Key': uuidv4()
    },
    data
  })
}

/**
 * 查询订单支付信息
 * @param {string} orderNo - 订单号
 * @returns {Promise}
 */
export function getPaymentByOrderNo(orderNo) {
  return request({
    url: `/payments/order/${orderNo}`,
    method: 'get'
  })
}
