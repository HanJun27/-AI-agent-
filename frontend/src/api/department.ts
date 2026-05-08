import request from './request'

export interface Department {
  id?: number
  deptNo: string
  deptName: string
  location: string
  manager: string
}

export interface DepartmentQuery {
  pageNum?: number
  pageSize?: number
  keyword?: string
}

export interface DepartmentListResponse {
  list: Department[]
  total: number
  pageNum: number
  pageSize: number
}

export function getDepartments(params: DepartmentQuery) {
  return request.get<any, { data: DepartmentListResponse }>('/departments', { params })
}

export function getDepartmentById(id: number) {
  return request.get<any, { data: Department }>(`/departments/${id}`)
}

export function addDepartment(data: Department) {
  return request.post('/departments', data)
}

export function updateDepartment(id: number, data: Department) {
  return request.put(`/departments/${id}`, data)
}

export function deleteDepartment(id: number) {
  return request.delete(`/departments/${id}`)
}
