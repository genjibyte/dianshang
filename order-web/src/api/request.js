import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 开发环境硬编码 userId = 1
    config.headers['X-User-Id'] = config.headers['X-User-Id'] || '1'
    return config
  },
  (error) => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 统一处理业务错误
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.message || '请求失败，请稍后重试')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    console.error('响应错误：', error)
    let message = '网络异常，请检查网络连接'
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 400:
          message = error.response.data?.message || '请求参数错误'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = error.response.data?.message || '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务暂不可用'
          break
        default:
          message = error.response.data?.message || `请求失败(${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
