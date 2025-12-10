import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加token到请求头
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 如果是文件上传，不设置Content-Type，让浏览器自动设置
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 确保响应数据使用UTF-8编码
    if (response.headers) {
      const contentType = response.headers['content-type'] || response.headers['Content-Type']
      if (contentType && !contentType.includes('charset')) {
        response.headers['content-type'] = contentType + '; charset=UTF-8'
      } else if (!contentType) {
        response.headers['content-type'] = 'application/json; charset=UTF-8'
      }
    }
    
    // 如果响应数据是字符串，确保使用UTF-8解码
    if (typeof response.data === 'string') {
      try {
        // 尝试解析为JSON
        response.data = JSON.parse(response.data)
      } catch (e) {
        // 如果不是JSON，直接返回字符串
      }
    }
    
    return response.data
  },
  (error) => {
    // 处理401未授权错误
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      // 触发登录状态变化事件
      window.dispatchEvent(new Event('login-status-changed'))
    }
    
    // 统一错误处理，提取错误信息
    if (error.response?.data) {
      const errorData = error.response.data
      // 如果后端返回的是Result格式
      if (errorData.code !== undefined && errorData.message) {
        error.message = errorData.message
      } else if (errorData.message) {
        error.message = errorData.message
      }
    }
    
    return Promise.reject(error)
  }
)

export default request

