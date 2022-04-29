import qs from 'qs'
import axios from 'axios'
import store from '@/store'
import storage from 'store'
import { message } from 'ant-design-vue'
// import { getLanguage } from '@/lang/index'

import { ACCESS_TOKEN } from '@/store/mutation-types'
// import store from '../store'
// 创建axios实例

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  timeout: 30000,
  headers: []
})

// request拦截器  前端给后端的请求
service.interceptors.request.use(
  config => {
    // config.headers["Content-Type"] = "application/json;charset=utf-8"; // 配置请求头
    config.headers['Content-Type'] = 'application/x-www-form-urlencoded' // 配置请求头
    config.data = qs.stringify(config.data)
    if (storage.get(ACCESS_TOKEN)) {
      config.headers['token'] = storage.get(ACCESS_TOKEN)
    }
    return config
  },
  error => {
    // 请求错误
    console.log(error) // for debug
    return Promise.reject(error)
  }
)
// respone拦截器  后端给前端的响应
service.interceptors.response.use(
  response => {
    const { status, data } = response
    if (status !== 200) {
      message({
        message: data.message || '请联系网站后台人员解决',
        type: 'error',
        duration: 2 * 1000
      })
      // eslint-disable-next-line prefer-promise-reject-errors
      return Promise.reject('error')
    } else {
      return data
    }
  },
  error => {
    if (error.response) {
      if (error.response.status === 400) {
        // token失效 ,重新获取token
        message.error('接口参数有错误！')
      } else if (error.response.status === 403) {
        // 无auth授权，后台不允许访问
        message.error('请求资源的访问被服务器拒绝')
      } else if (error.response.status === 404) {
        // 服务器断开
        message.error('网络请求不存在')
      } else if (error.response.status === 500) {
        // 服务器断开
        message.error('服务器断开，请稍后重试。')
      } else if (error.response.status === 503) {
        // 服务器断开
        message.error('正在停机维护，无法处理请求。')
      } else {
        message.error(error.response.data.msg || '未知错误,请联系后台管理人员')
      }
    } else {
      // 请求超时或者网络有问题
      if (error.message.includes('timeout')) {
        message.error('请求超时！请检查网络是否正常')
      } else {
        message.error('请求失败，请检查网络是否已连接')
      }
    }
    return Promise.reject(error.response)
  }
)

export function post(url, data, headers) {
  return service({
    method: 'post',
    url: url,
    data: data,
    headers: headers || {}
  })
}

export function get(url, params, headers) {
  return service({
    method: 'get',
    url,
    params: params,
    headers: headers || {}
  })
}
