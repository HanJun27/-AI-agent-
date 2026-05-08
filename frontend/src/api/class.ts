import request from './request'

export interface ClassInfo {
  id: number
  classNo: string
  className: string
  major: string
  grade: string
  teacher: string
  studentCount: number
  classroom: string
}

export interface ClassCreateDTO {
  classNo: string
  className: string
  major: string
  grade: string
  teacher: string
  studentCount: number
  classroom: string
}

export interface ClassQueryParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
}

// 获取班级列表
export const getClasses = (params: ClassQueryParams) => {
  return request.get('/classes', { params })
}

// 获取班级详情
export const getClassById = (id: number) => {
  return request.get(`/classes/${id}`)
}

// 添加班级
export const addClass = (data: ClassCreateDTO) => {
  return request.post('/classes', data)
}

// 更新班级
export const updateClass = (id: number, data: ClassCreateDTO) => {
  return request.put(`/classes/${id}`, data)
}

// 删除班级
export const deleteClass = (id: number) => {
  return request.delete(`/classes/${id}`)
}
