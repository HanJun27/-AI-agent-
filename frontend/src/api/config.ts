import request from './request'

// API配置接口
export const configApi = {
  // 获取所有API配置
  getAllApiConfigs() {
    return request({
      url: '/config/api-configs',
      method: 'get'
    })
  },

  // 获取单个提供商配置
  getApiConfig(provider: string) {
    return request({
      url: `/config/api-config/${provider}`,
      method: 'get'
    })
  },

  // 保存API配置
  saveApiConfig(provider: string, data: any) {
    return request({
      url: `/config/api-config/${provider}`,
      method: 'put',
      data
    })
  },

  // 删除API配置
  deleteApiConfig(provider: string) {
    return request({
      url: `/config/api-config/${provider}`,
      method: 'delete'
    })
  }
}

// 用户偏好设置接口
export const preferenceApi = {
  // 获取用户偏好
  getUserPreferences(userId: number) {
    return request({
      url: `/config/preferences/${userId}`,
      method: 'get'
    })
  },

  // 保存用户偏好
  saveUserPreferences(userId: number, data: any) {
    return request({
      url: `/config/preferences/${userId}`,
      method: 'put',
      data
    })
  }
}
