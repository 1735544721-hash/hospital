import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'

// 后台路由
export const backendRoutes = [
  {
    path: '/back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/backend/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled', roles: ['ADMIN', 'DOCTOR'] }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/backend/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'department',
        name: 'DepartmentManagement',
        component: () => import('@/views/backend/department/index.vue'),
        meta: { title: '科室管理', icon: 'Office', roles: ['ADMIN'] }
      },
      {
        path: 'doctor',
        name: 'DoctorManagement',
        component: () => import('@/views/backend/doctor/index.vue'),
        meta: { title: '医生管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'patient',
        name: 'PatientManagement',
        component: () => import('@/views/backend/patient/index.vue'),
        meta: { title: '患者管理', icon: 'User', roles: ['ADMIN', 'DOCTOR'] }
      },
      {
        path: 'medicine',
        name: 'MedicineManagement',
        component: () => import('@/views/backend/medicine/index.vue'),
        meta: { title: '药品管理', icon: 'FirstAidKit', roles: ['ADMIN'] }
      },
      {
        path: 'medicine-category',
        name: 'MedicineCategoryManagement',
        component: () => import('@/views/backend/medicine-category/index.vue'),
        meta: { title: '药品分类管理', icon: 'Menu', roles: ['ADMIN'] }
      },
      {
        path: 'schedule',
        name: 'ScheduleManagement',
        component: () => import('@/views/backend/schedule/index.vue'),
        meta: { title: '排班管理', icon: 'Calendar', roles: ['ADMIN', 'DOCTOR'] }
      },
      {
        path: 'appointment',
        name: 'AppointmentManagement',
        component: () => import('@/views/backend/appointment/index.vue'),
        meta: { title: '预约管理', icon: 'Calendar', roles: ['ADMIN', 'DOCTOR'] }
      },
      {
        path: 'medical-record',
        name: 'MedicalRecordManagement',
        component: () => import('@/views/backend/medical-record/index.vue'),
        meta: { title: '就诊记录管理', icon: 'Document', roles: ['ADMIN', 'DOCTOR'] }
      },
      {
        path: 'prescription',
        name: 'PrescriptionManagement',
        component: () => import('@/views/backend/prescription/index.vue'),
        meta: { title: '处方管理', icon: 'List', roles: ['ADMIN', 'DOCTOR'] }
      },
      {
        path: 'profile',
        name: 'BackendProfile',
        component: () => import('@/views/backend/user/PersonInfo.vue'),
        meta: { title: '个人信息', icon: 'UserFilled', roles: ['ADMIN', 'DOCTOR'] }
      }
    ]
  }
]

// 前台路由配置
const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontendLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/frontend/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/views/frontend/department/index.vue'),
        meta: { title: '科室介绍' }
      },
      {
        path: 'doctor',
        name: 'Doctor',
        component: () => import('@/views/frontend/doctor/index.vue'),
        meta: { title: '医生介绍' }
      },
      {
        path: 'appointment',
        name: 'Appointment',
        component: () => import('@/views/frontend/appointment/index.vue'),
        meta: { title: '预约挂号', requiresAuth: true }
      },
      {
        path: 'my-appointments',
        name: 'MyAppointments',
        component: () => import('@/views/frontend/appointment/my-appointments.vue'),
        meta: { title: '我的预约', requiresAuth: true }
      },
      {
        path: 'medical-record',
        name: 'MedicalRecord',
        component: () => import('@/views/frontend/medical-record/index.vue'),
        meta: { title: '我的就诊记录', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ] 
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forget',
    name: 'ForgetPassword',
    component: () => import('@/views/auth/ForgetPassword.vue'),
    meta: { title: '忘记密码' }
  }
]

// 错误页面路由
const errorRoutes = [
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...frontendRoutes,
    ...backendRoutes,
    ...errorRoutes
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 医疗信息系统`
  }

  const userStore = useUserStore()
  
  // 检查是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 已登录用户的路由控制
  if (userStore.isLoggedIn) {
    // 处理登录页面访问
    if (to.path === '/login') {
      // 根据角色重定向到不同页面
      if (userStore.role === 'PATIENT') {
        next('/')
      } else {
        next('/back/dashboard')
      }
      return
    }

    // 判断是否是医护人员（非患者用户）
    const isMedicalStaff = ['ADMIN', 'DOCTOR'].includes(userStore.role)
    
    if (isMedicalStaff) {
      // 医护人员访问后台路由时检查权限
      if (to.path.startsWith('/back')) {
        // 检查当前用户是否有权限访问该路由
        if (to.meta.roles && to.meta.roles.length > 0 && !to.meta.roles.includes(userStore.role)) {
          next('/back/dashboard') // 如果没有权限，重定向到首页
          return
        }
        next() // 有权限，允许访问
      } else {
        // 医护人员访问前台页面，重定向到后台首页
        next('/back/dashboard')
        return
      }
    } else {
      // 患者角色只能访问前台路由
      if (to.path.startsWith('/back')) {
        next('/') // 患者访问后台，重定向到前台首页
        return
      }
      next() // 患者访问前台，允许访问
    }
  } else {
    // 未登录用户
    if (to.path.startsWith('/back')) {
      next('/login')
      return
    }
    next() // 未登录用户访问前台公开页面，允许访问
  }
})

export default router
