import { defineStore } from 'pinia'
import request from '@/utils/request'
import { login, register } from '@/api/user'
// import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    menus: JSON.parse(localStorage.getItem('menus')) || [],
    doctorInfo: JSON.parse(localStorage.getItem('doctorInfo')) || null,
    patientInfo: JSON.parse(localStorage.getItem('patientInfo')) || null
  }),

  getters: {
    // 判断是否登录
    isLoggedIn: (state) => !!state.token,
    // 判断是否是管理员
    isAdmin: (state) => state.role === 'ADMIN',
    // 判断是否是医生
    isDoctor: (state) => state.role === 'DOCTOR',
    // 判断是否是患者
    isPatient: (state) => state.role === 'PATIENT',
    // 用于判断是否是前台用户(患者)
    isUser: (state) => state.role === 'PATIENT',
    // 获取医生ID
    doctorId: (state) => state.doctorInfo?.id || null
  },

  actions: {
    // 更新用户信息
    updateUserInfo(data) {
      if (!data) return
      this.userInfo = data
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    
    setUserInfo(data) {
      if (!data) return
      
      this.userInfo = data.userInfo || data
      this.token = data.token
      this.role = data.roleCode
      
      // 存储到 LocalStorage
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      localStorage.setItem('token', this.token || '')
      localStorage.setItem('role', this.role || '')
    },

    // 设置医生信息
    setDoctorInfo(doctorInfo) {
      if (!doctorInfo) return
      this.doctorInfo = doctorInfo
      localStorage.setItem('doctorInfo', JSON.stringify(doctorInfo))
    },

    // 设置患者信息
    setPatientInfo(patientInfo) {
      if (!patientInfo) return
      this.patientInfo = patientInfo
      localStorage.setItem('patientInfo', JSON.stringify(patientInfo))
    },

    // 获取角色相关信息
    async fetchRoleInfo() {
      if (!this.userInfo || !this.userInfo.id) return
      
      try {
        if (this.role === 'PATIENT') {
          // 获取患者信息
          await request.get(`/patient/current`, {}, {
            onSuccess: (data) => {
              if (data) {
                this.setPatientInfo(data)
              }
            }
          })
        } else if (this.role === 'DOCTOR') {
          // 获取医生信息
          await request.get(`/doctor/user/${this.userInfo.id}`, {}, {
            onSuccess: (data) => {
              if (data) {
                this.setDoctorInfo(data)
              }
            }
          })
        }
      } catch (error) {
        console.error('获取角色信息失败:', error)
      }
    },

    clearUserInfo() {
      this.userInfo = null
      this.token = ''
      this.role = ''
      this.menus = []
      this.doctorInfo = null
      this.patientInfo = null
      
      // 清除 LocalStorage
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('menus')
      localStorage.removeItem('doctorInfo')
      localStorage.removeItem('patientInfo')
    },
    
    setMenus(menus) {
      if (!menus) return
      this.menus = menus
      localStorage.setItem('menus', JSON.stringify(menus))
    },
    
    // 获取用户信息和菜单 - 从localStorage恢复
    async getUserInfo() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      const menus = JSON.parse(localStorage.getItem('menus'))
      const doctorInfo = JSON.parse(localStorage.getItem('doctorInfo'))
      const patientInfo = JSON.parse(localStorage.getItem('patientInfo'))
      
      if (userInfo) {
        this.userInfo = userInfo
        this.menus = menus || []
        this.doctorInfo = doctorInfo
        this.patientInfo = patientInfo
        return { userInfo, menus, doctorInfo, patientInfo }
      }
      
      // 如果没有缓存的数据，清除状态并抛出错误
      this.clearUserInfo()
      throw new Error('No cached user info')
    },
    
    // 登录
    async login(loginForm) {
      try {
        const res = await request.post('/user/login', loginForm)
        this.setUserInfo(res)
        return res
      } catch (error) {
        this.clearUserInfo()
        throw error
      }
    },
    
    // 退出登录
    async logout() {
      this.clearUserInfo()
    },
    
    // 检查登录状态
    checkLoginStatus() {
      return !!this.token
    }
  }
})