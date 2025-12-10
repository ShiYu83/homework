import request from './request'

export interface AppointmentCreateDTO {
  patientId: number
  scheduleId: number
  doctorId?: number
  appointmentDate?: string
  timeSlot?: string
}

export interface TimeSlotDTO {
  scheduleId: number
  timeSlot: string
  remaining: number
  totalCount: number
}

export interface ModifyAppointmentDTO {
  scheduleId: number
  timeSlot: string
}

export const appointmentApi = {
  getAvailableSlots: (params: { departmentId?: number; doctorId?: number; date: string }) =>
    request.get('/appointment/available-slots', { params }),
  create: (data: AppointmentCreateDTO) => request.post('/appointment/create', data),
  cancel: (id: number, reason?: string) => request.put(`/appointment/cancel/${id}`, null, { params: { reason } }),
  modify: (id: number, data: ModifyAppointmentDTO) => request.put(`/appointment/modify/${id}`, data),
  queryByPatient: (patientId: number) => request.get(`/appointment/patient/${patientId}`)
}

