import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8088/admin', // 指向Java后端的/admin路径
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers['Authorization'] = token // 简单 token 传递
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
request.interceptors.response.use(response => {
  let res = response.data;
  // 如果是Result包装
  if(res.code === 200 || res.code === 0) {
    return res
  } else {
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(res.msg)
  }
}, error => {
  ElMessage.error('网络或服务器异常')
  return Promise.reject(error)
})

export default request
