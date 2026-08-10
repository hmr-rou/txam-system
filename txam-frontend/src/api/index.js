import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '',
  timeout: 30000,
  withCredentials: true,
})

// 响应拦截：统一错误处理
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401 || status === 403) {
        ElMessage.error('登录已过期，请重新登录')
        router?.push('/login')
      } else if (data && data.message) {
        ElMessage.error(data.message)
      } else {
        ElMessage.error('请求失败，请稍后重试')
      }
    } else {
      ElMessage.error('网络异常，请检查连接')
    }
    return Promise.reject(error)
  }
)

// 用于路由器跳转（由 router/index.js 注入）
let router = null
export function setRouter(r) {
  router = r
}

export default api
