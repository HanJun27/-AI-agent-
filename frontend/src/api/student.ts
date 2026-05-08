import request from './request'

export interface Student {
  id: number
  stuNo: string
  stuName: string
  gender: string
  age: number
  phone: string
  email: string
  classId: number
  major: string
  enrollmentDate: string
  status: number
}

export interface StudentCreateDTO {
  stuNo: string
  stuName: string
  gender: string
  age: number
  phone: string
  email: string
  classId: number
  major: string
  enrollmentDate: string
}

export interface StudentQueryParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
}

// 获取学生列表
export const getStudents = (params: StudentQueryParams) => {
  return request.get('/students', { params })
}

// 获取学生详情
export const getStudentById = (id: number) => {
  return request.get(`/students/${id}`)
}

// 添加学生
export const addStudent = (data: StudentCreateDTO) => {
  return request.post('/students', data)
}

// 更新学生
export const updateStudent = (id: number, data: StudentCreateDTO) => {
  return request.put(`/students/${id}`, data)
}

// 删除学生
export const deleteStudent = (id: number) => {
  return request.delete(`/students/${id}`)
}
