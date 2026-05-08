import request from './request'

export interface Employee {
  id: number
  empNo: string
  empName: string
  gender: string
  age: number
  phone: string
  email: string
  position: string
  deptId: number
  hireDate: string
  status: number
}

export interface EmployeeCreateDTO {
  empNo: string
  empName: string
  gender: string
  age: number
  phone: string
  email: string
  position: string
  deptId: number
  hireDate: string
}

export interface EmployeeQueryParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
}

// 获取员工列表
export const getEmployees = (params: EmployeeQueryParams) => {
  return request.get('/employees', { params })
}

// 获取员工详情
export const getEmployeeById = (id: number) => {
  return request.get(`/employees/${id}`)
}

// 添加员工
export const addEmployee = (data: EmployeeCreateDTO) => {
  return request.post('/employees', data)
}

// 更新员工
export const updateEmployee = (id: number, data: EmployeeCreateDTO) => {
  return request.put(`/employees/${id}`, data)
}

// 删除员工
export const deleteEmployee = (id: number) => {
  return request.delete(`/employees/${id}`)
}
