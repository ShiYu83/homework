import request from './request'

export interface PatientRegisterDTO {
  name: string
  idCard: string
  phone: string
  password: string
  gender?: number
  birthday?: string
  allergyHistory?: string
}

export interface CreditInfo {
  creditScore: number
  totalAppointments: number
  cancelledCount: number
  completedCount: number
  cancelRate: string
  level: string
}

export interface MedicalRecord {
  id: number
  appointmentId: number
  patientId: number
  doctorId: number
  diagnosis?: string
  symptoms?: string
  advice?: string
  createTime?: string
  updateTime?: string
}

export interface Prescription {
  id: number
  medicalRecordId: number
  appointmentId: number
  totalAmount: number
  status: number // 1-待支付 2-已支付
  createTime?: string
  updateTime?: string
}

export interface PrescriptionItem {
  id: number
  prescriptionId: number
  medicineId: number
  quantity: number
  price: number
  subtotal: number
  medicineName?: string
  unit?: string
  specification?: string
}

export const patientApi = {
  register: (data: PatientRegisterDTO) => request.post('/patient/register', data),
  getById: (id: number) => request.get(`/patient/${id}`),
  update: (data: any) => request.put('/patient/update', data),
  getCreditInfo: (patientId: number) => request.get<CreditInfo>(`/patient/credit/${patientId}`),
  
  // 获取病例列表
  getMedicalRecords: () => request.get<MedicalRecord[]>('/medical-record/patient'),
  
  // 获取处方列表
  getPrescriptions: () => request.get<Prescription[]>('/prescription/patient'),
  
  // 获取处方详情
  getPrescriptionDetail: (id: number) => request.get(`/prescription/${id}`),
  
  // 创建合并订单
  createCombinedOrder: (data: { appointmentId: number; prescriptionId?: number }) => {
    return request.post('/payment/create-combined', data)
  },
  
  // 支付订单
  payOrder: (paymentId: number, paymentMethod: number) => {
    return request.post('/payment/pay', null, {
      params: { paymentId, paymentMethod }
    })
  }
}

