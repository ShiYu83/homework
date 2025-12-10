import request from './request'

export interface Doctor {
  id?: number
  name: string
  departmentId: number
  title: string
  specialty?: string
  introduction?: string
  status?: number
  entryDate?: string
  consultingRoomId?: number
  username?: string
  avatar?: string
}

export interface Patient {
  id?: number
  name: string
  idCard: string
  phone: string
  gender: number
  birthday?: string
  allergyHistory?: string
  creditScore?: number
}

export interface Department {
  id?: number
  name: string
  code: string
  description?: string
  status?: number
}

export interface ConsultingRoom {
  id?: number
  roomNo: string
  name: string
  departmentId: number
  doctorId?: number
  location?: string
  status?: number
}

export interface Schedule {
  id?: number
  doctorId: number
  departmentId: number
  scheduleDate: string
  timeSlot: string
  totalCount: number
  reservedCount?: number
  status?: number
}

export const adminApi = {
  // 医生管理
  getDoctors(departmentId?: number) {
    return request.get<Doctor[]>('/admin/doctors', { params: { departmentId } })
  },
  createDoctor(data: Doctor) {
    return request.post<Doctor>('/admin/doctors', data)
  },
  updateDoctor(id: number, data: Doctor) {
    return request.put(`/admin/doctors/${id}`, data)
  },
  deleteDoctor(id: number) {
    return request.delete(`/admin/doctors/${id}`)
  },
  getDoctorAccount(id: number) {
    return request.get<{ username: string; password: string }>(`/admin/doctors/${id}/account`)
  },
  
  // 文件上传（使用原生axios，因为request拦截器会处理响应）
  uploadAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    // 使用原生axios上传，避免request拦截器处理
    const axios = require('axios').default
    const token = localStorage.getItem('token')
    return axios.post('/api/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': token ? `Bearer ${token}` : ''
      }
    })
  },
  
  // 调班审核管理
  getScheduleAdjustments() {
    return request.get('/admin/schedule-adjustments')
  },
  
  reviewScheduleAdjustment(id: number, data: { approved: boolean; comment?: string }) {
    return request.post(`/admin/schedule-adjustments/${id}/review`, data)
  },
  
  // 患者管理
  getPatients(keyword?: string) {
    return request.get<Patient[]>('/admin/patients', { params: { keyword } })
  },
  createPatient(data: Patient) {
    return request.post<Patient>('/admin/patients', data)
  },
  updatePatient(id: number, data: Patient) {
    return request.put(`/admin/patients/${id}`, data)
  },
  deletePatient(id: number) {
    return request.delete(`/admin/patients/${id}`)
  },
  
  // 科室管理
  getDepartments() {
    return request.get<Department[]>('/admin/departments')
  },
  createDepartment(data: Department) {
    return request.post<Department>('/admin/departments', data)
  },
  updateDepartment(id: number, data: Department) {
    return request.put(`/admin/departments/${id}`, data)
  },
  deleteDepartment(id: number) {
    return request.delete(`/admin/departments/${id}`)
  },
  
  // 诊室管理
  getConsultingRooms(departmentId?: number) {
    return request.get<ConsultingRoom[]>('/admin/consulting-rooms', { params: { departmentId } })
  },
  createConsultingRoom(data: ConsultingRoom) {
    return request.post<ConsultingRoom>('/admin/consulting-rooms', data)
  },
  updateConsultingRoom(id: number, data: ConsultingRoom) {
    return request.put(`/admin/consulting-rooms/${id}`, data)
  },
  deleteConsultingRoom(id: number) {
    return request.delete(`/admin/consulting-rooms/${id}`)
  },
  
  // 号源管理
  getSchedules(params?: {
    doctorId?: number
    departmentId?: number
    startDate?: string
    endDate?: string
  }) {
    return request.get<Schedule[]>('/admin/schedules', { params })
  },
  createSchedule(data: Schedule) {
    return request.post<Schedule>('/admin/schedules', data)
  },
  updateSchedule(id: number, data: Schedule) {
    return request.put(`/admin/schedules/${id}`, data)
  },
  deleteSchedule(id: number) {
    return request.delete(`/admin/schedules/${id}`)
  },
  
  // 时段管理
  getTimeSlots() {
    return request.get<string[]>('/admin/time-slots')
  },
  
  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return request.post('/admin/change-password', data)
  }
}

