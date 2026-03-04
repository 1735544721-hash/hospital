<template>
  <Auth 
    :formData="loginForm" 
    :rules="rules" 
    :loading="loading"
    submitText="登录"
    @submit="handleSubmit"
  >
    <template #form-items>
      <div class="welcome-message">
        <h2>欢迎回来</h2>
        <p>请登录您的账号，开始健康管理之旅</p>
      </div>
      <el-form-item prop="username">
        <el-input 
          v-model="loginForm.username"
          :prefix-icon="User"
          placeholder="请输入用户名">
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input 
          v-model="loginForm.password"
          :prefix-icon="Lock"
          type="password"
          placeholder="请输入密码">
        </el-input>
      </el-form-item>
      <div class="remember-forgot">
        <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        <router-link to="/forget" class="forget-link">忘记密码？</router-link>
      </div>
    </template>

    <template #auth-links>
      <div class="auth-links-row">
        <a href="javascript:void(0)" @click="handleRegister" class="register-link">
          <el-icon><ArrowRight /></el-icon>
          <span>立即注册</span>
        </a>
      </div>
    </template>
  </Auth>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { User, Lock, ArrowRight } from '@element-plus/icons-vue'
import Auth from './Auth.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(localStorage.getItem('rememberMe') === 'true')

const loginForm = reactive({
  username: localStorage.getItem('rememberMe') === 'true' ? (localStorage.getItem('rememberedUsername') || '') : '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleSubmit = (form) => {
  formRef.value = form.value
  loginFormRef.value = form.value
  handleLogin()
}

const handleRegister = () => {
  router.push('/register');
}

const loginFormRef = ref(null)

const handleLogin = () => {
  loginFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        // 统一使用用户登录接口
        const res = await request.post("/user/login", loginForm, {
          successMsg: "登录成功，欢迎回来",
          showDefaultMsg: true,
          onSuccess: async (data) => {
            // 处理记住我功能
            if (rememberMe.value) {
              localStorage.setItem('rememberMe', 'true')
              localStorage.setItem('rememberedUsername', loginForm.username)
            } else {
              localStorage.removeItem('rememberMe')
              localStorage.removeItem('rememberedUsername')
            }
            
            userStore.setUserInfo(data)
            
            // 获取角色相关信息（医生或患者详情）
            await userStore.fetchRoleInfo()
            
            // 根据返回的角色决定跳转路径
            if (['ADMIN', 'DOCTOR'].includes(data.roleCode)) {
              // 医护人员登录，设置菜单
              userStore.setMenus(data.menuList)
              
              // 如果是医生，获取医生详情信息
              if (data.roleCode === 'DOCTOR') {
                await fetchDoctorInfo(data.id)
              }
              
              // 直接导航到后台仪表盘
              await router.isReady()
              router.push(route.query.redirect || '/back/dashboard')
            } else {
              // 患者登录，直接跳转到前台
              const redirect = route.query.redirect || '/'
              // 确保redirect不是指向后台页面
              if (redirect.startsWith('/back')) {
                router.push('/')
              } else {
                router.push(redirect)
              }
            }
          },
          onError: (error) => {
            console.error('登录失败:', error)
            ElMessage.error('登录失败，请检查用户名和密码是否正确')
          }
        })
      } finally {
        loading.value = false
      }
    }
  })
}

// 获取医生详细信息
const fetchDoctorInfo = async (userId) => {
  try {
    await request.get(`/doctor/user/${userId}`, {}, {
      onSuccess: (doctorData) => {
        if (doctorData) {
          // 存储医生信息到store中
          userStore.setDoctorInfo(doctorData)
          console.log('医生信息获取成功:', doctorData)
        }
      },
      onError: (error) => {
        console.error('获取医生信息失败:', error)
      }
    })
  } catch (error) {
    console.error('获取医生信息请求失败:', error)
  }
}
</script>

<style lang="scss" scoped>
.welcome-message {
  text-align: center;
  margin-bottom: 24px;
  
  h2 {
    font-size: 24px;
    color: #455a64;
    margin: 0 0 8px;
    font-weight: 600;
  }
  
  p {
    font-size: 14px;
    color: #78909c;
    margin: 0;
  }
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  :deep(.el-checkbox__label) {
    color: #546e7a;
  }
  
  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background-color: #68a86d;
    border-color: #68a86d;
  }
  
  .forget-link {
    color: #78909c;
    font-size: 14px;
    
    &:hover {
      color: #68a86d;
    }
  }
}

.auth-links-row {
  display: flex;
  justify-content: center;
  align-items: center;
  
  .register-link {
    display: flex;
    align-items: center;
    color: #68a86d;
    font-weight: 500;
    cursor: pointer;
    text-decoration: none;
    
    .el-icon {
      margin-left: 4px;
      font-size: 16px;
      transition: transform 0.3s ease;
    }
    
    &:hover {
      .el-icon {
        transform: translateX(3px);
      }
    }
  }
}
</style> 