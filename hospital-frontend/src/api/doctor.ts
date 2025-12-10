import request from './request'

export interface ScheduleAdjustment {
  id?: number
  doctorId: number
  scheduleId: number
  originalDate: string
  originalTimeSlot: string
  newDate?: string
  newTimeSlot?: string
  reason: string
  status?: number
}

export interface PatientQueueItem {
  appointmentId: number
  appointmentNo: string
  timeSlot: string
  status: number
  createTime: string
  appointmentDate?: string
  completeTime?: string
  patient?: {
    id: number
    name: string
    phone: string
    gender: number
    birthday?: string
    allergyHistory?: string
    creditScore?: number
  }
}

export interface Doctor {
  id: number
  name: string
  title: string
  specialty?: string
  introduction?: string
  departmentId: number
  status: number
}

export const doctorApi = {
  // 获取医生列表（可选按科室筛选）
  list(departmentId?: number) {
    return request.get<Doctor[]>('/doctor/list', { params: departmentId ? { departmentId } : {} })
  },

  // 获取医生信息
  getDoctorInfo() {
    return request.get('/doctor-manage/info')
  },

  // 调班管理
  createScheduleAdjustment(data: {
    scheduleId: number
    newDate?: string
    newTimeSlot?: string
    reason: string
  }) {
    return request.post<ScheduleAdjustment>('/doctor-manage/schedule-adjustment', data)
  },
  getScheduleAdjustments() {
    return request.get<ScheduleAdjustment[]>('/doctor-manage/schedule-adjustments')
  },

  // 患者队列查询
  getPatientQueue(date?: string) {
    return request.get<PatientQueueItem[]>('/doctor-manage/patient-queue', { params: { date } })
  },

  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return request.post('/doctor-manage/change-password', data)
  },

  // 叫号
  callPatient(appointmentId: number) {
    return request.post(`/doctor-manage/call-patient/${appointmentId}`)
  },

  // 创建病例
  createMedicalRecord(data: {
    appointmentId: number
    diagnosis?: string
    symptoms?: string
    advice?: string
  }) {
    return request.post('/medical-record', data)
  },

  // 创建处方
  createPrescription(data: {
    medicalRecordId: number
    appointmentId: number
    items: Array<{
      medicineId: number
      quantity: number
      price: number
      subtotal: number
    }>
  }) {
    return request.post('/prescription', data)
  },

  // 获取处方详情
  getPrescriptionDetail(id: number) {
    return request.get(`/prescription/${id}`)
  },

  // 根据预约ID获取处方
  getPrescriptionByAppointmentId(appointmentId: number) {
    return request.get(`/prescription/appointment/${appointmentId}`)
  }
}
