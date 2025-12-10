import request from './request'

export interface Medicine {
  id: number
  name: string
  price: number
  unit: string
  specification?: string
  stock: number
  status: number // 0-下架 1-上架
  category?: string
  createTime?: string
  updateTime?: string
}

export const medicineApi = {
  // 获取所有药品（管理员）
  getAllMedicines: () => {
    return request.get<Medicine[]>('/medicine/all')
  },

  // 获取已上架药品（医生可用）
  getAvailableMedicines: (keyword?: string) => {
    return request.get<Medicine[]>('/medicine/available', {
      params: keyword ? { keyword } : undefined
    })
  },

  // 根据ID获取药品
  getMedicineById: (id: number) => {
    return request.get<Medicine>(`/medicine/${id}`)
  },

  // 创建药品（管理员）
  createMedicine: (medicine: Partial<Medicine>) => {
    return request.post<Medicine>('/medicine', medicine)
  },

  // 更新药品（管理员）
  updateMedicine: (id: number, medicine: Partial<Medicine>) => {
    return request.put<Medicine>(`/medicine/${id}`, medicine)
  },

  // 删除药品（管理员）
  deleteMedicine: (id: number) => {
    return request.delete(`/medicine/${id}`)
  },

  // 上架/下架药品（管理员）
  updateStatus: (id: number, status: number) => {
    return request.put(`/medicine/${id}/status`, null, {
      params: { status }
    })
  }
}

