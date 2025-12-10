import request from './request'

export interface Department {
  id: number
  name: string
  code: string
  description: string
  status: number
}

export const departmentApi = {
  list: () => request.get('/department/list'),
  getList: () => request.get('/department/list'),
  getById: (id: number) => request.get(`/department/${id}`)
}

