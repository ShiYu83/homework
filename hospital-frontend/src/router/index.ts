import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Appointment from '../views/Appointment.vue'
import MyAppointments from '../views/MyAppointments.vue'
import Register from '../views/Register.vue'
import Login from '../views/Login.vue'
import Guide from '../views/Guide.vue'
import Payment from '../views/Payment.vue'
import PatientProfile from '../views/PatientProfile.vue'
import Queue from '../views/Queue.vue'
import AdminDashboard from '../views/AdminDashboard.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/guide',
      name: 'Guide',
      component: Guide
    },
    {
      path: '/appointment',
      name: 'Appointment',
      component: Appointment
    },
    {
      path: '/my-appointments',
      name: 'MyAppointments',
      component: MyAppointments
    },
    {
      path: '/payment',
      name: 'Payment',
      component: Payment
    },
    {
      path: '/profile',
      name: 'PatientProfile',
      component: PatientProfile
    },
    {
      path: '/queue',
      name: 'Queue',
      component: Queue
    },
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: AdminDashboard,
      meta: { requiresAuth: true, role: 3 }
    },
    {
      path: '/admin/doctors',
      name: 'AdminDoctors',
      component: () => import('../views/admin/DoctorsManage.vue'),
      meta: { requiresAuth: true, role: 3 }
    },
    {
      path: '/admin/patients',
      name: 'AdminPatients',
      component: () => import('../views/admin/PatientsManage.vue'),
      meta: { requiresAuth: true, role: 3 }
    },
    {
      path: '/admin/departments',
      name: 'AdminDepartments',
      component: () => import('../views/admin/DepartmentsManage.vue'),
      meta: { requiresAuth: true, role: 3 }
    },
    {
      path: '/admin/rooms',
      name: 'AdminRooms',
      component: () => import('../views/admin/RoomsManage.vue'),
      meta: { requiresAuth: true, role: 3 }
    },
    {
      path: '/admin/schedules',
      name: 'AdminSchedules',
      component: () => import('../views/admin/SchedulesManage.vue'),
      meta: { requiresAuth: true, role: 3 }
    },
    {
      path: '/admin/statistics',
      name: 'AdminStatistics',
      component: () => import('../views/admin/Statistics.vue'),
      meta: { requiresAuth: true, role: 3 }
    },
    {
      path: '/admin/schedule-adjustments',
      name: 'AdminScheduleAdjustments',
      component: () => import('../views/admin/ScheduleAdjustmentsManage.vue'),
      meta: { requiresAuth: true, role: 3 }
    },
    {
      path: '/doctor/info',
      name: 'DoctorInfo',
      component: () => import('../views/doctor/DoctorInfo.vue'),
      meta: { requiresAuth: true, role: 2 }
    },
    {
      path: '/doctor/schedule',
      name: 'DoctorSchedule',
      component: () => import('../views/doctor/ScheduleManage.vue'),
      meta: { requiresAuth: true, role: 2 }
    },
    {
      path: '/doctor/queue',
      name: 'DoctorQueue',
      component: () => import('../views/doctor/PatientQueue.vue'),
      meta: { requiresAuth: true, role: 2 }
    },
    {
      path: '/doctor/diagnosis',
      name: 'DoctorDiagnosis',
      component: () => import('../views/doctor/Diagnosis.vue'),
      meta: { requiresAuth: true, role: 2 }
    },
    {
      path: '/admin/medicines',
      name: 'AdminMedicines',
      component: () => import('../views/admin/MedicinesManage.vue'),
      meta: { requiresAuth: true, role: 3 }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
  // 检查角色权限
  if (to.meta.role && userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      // 支持多种可能的用户类型属性名，确保兼容性
      const userType = userInfo.userType || userInfo.user_type || userInfo.type
      if (userType !== to.meta.role) {
        console.warn('权限不足: 用户类型', userType, '，需要类型', to.meta.role)
        // 权限不足，根据用户类型跳转到合适的首页
        if (userType === 3) {
          // 管理员
          next('/admin')
        } else if (userType === 2) {
          // 医生
          next('/doctor/info')
        } else {
          // 普通用户
          next('/')
        }
        return
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
      next('/login')
      return
    }
  }
  
  next()
})

export default router

