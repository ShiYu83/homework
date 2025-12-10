import request from './request'

export interface AppointmentStatistics {
  total: number
  pending: number
  confirmed: number
  cancelled: number
  completed: number
  dailyStats: Record<string, number>
  departmentStats: Record<string, number>
}

export interface ScheduleStatistics {
  total: number
  totalCount: number
  reservedCount: number
  availableCount: number
  utilizationRate: number
}

export interface ReportData {
  appointmentStats: {
    total: number
    confirmed: number
    cancelled: number
    completed: number
  }
  scheduleStats: {
    total: number
    totalCount: number
    reservedCount: number
  }
  startDate: string
  endDate: string
  generateTime: string
}

export interface OverviewStats {
  doctors: number
  patients: number
  appointments: number
  departments: number
}

export const statisticsApi = {
  // 概览统计
  getOverview() {
    return request.get<OverviewStats>('/statistics/overview')
  },
  
  // 预约统计
  getAppointmentStatistics(params?: {
    startDate?: string
    endDate?: string
    departmentId?: number
  }) {
    return request.get<AppointmentStatistics>('/statistics/appointments', { params })
  },
  
  // 排班统计
  getScheduleStatistics(params?: {
    startDate?: string
    endDate?: string
  }) {
    return request.get<ScheduleStatistics>('/statistics/schedules', { params })
  },
  
  // 生成报表
  generateReport(params?: {
    startDate?: string
    endDate?: string
  }) {
    return request.get<ReportData>('/statistics/report', { params })
  }
}

