<template>
  <Auth 
    :formData="forgetForm" 
    :rules="rules" 
    :loading="loading"
    submitText="重置密码"
    @submit="handleSubmit"
  >
    <template #form-items>
      <div class="welcome-message">
        <h2>找回密码</h2>
        <p>请填写您的注册邮箱和新密码</p>
      </div>
      
      <div class="reset-steps">
        <el-steps :active="activeStep" finish-status="success" simple>
          <el-step title="填写邮箱"></el-step>
          <el-step title="重置密码"></el-step>
          <el-step title="完成"></el-step>
        </el-steps>
      </div>
      
      <div class="info-message">
        <el-icon><InfoFilled /></el-icon>
        <span>重置密码前，请确保您能够访问该邮箱</span>
      </div>
      
      <el-form-item prop="email">
        <el-input 
          v-model="forgetForm.email"
          :prefix-icon="Message"
          placeholder="请输入注册邮箱">
        </el-input>
      </el-form-item>
      <el-form-item prop="newPassword">
        <el-input 
          v-model="forgetForm.newPassword"
          :prefix-icon="Lock"
          type="password"
          placeholder="请输入新密码">
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input 
          v-model="forgetForm.confirmPassword"
          :prefix-icon="Lock"
          type="password"
          placeholder="请确认新密码">
        </el-input>
      </el-form-item>
      
      <div class="password-strength" v-if="forgetForm.newPassword">
        <div class="strength-label">密码强度:</div>
        <div class="strength-meter">
          <div 
            class="strength-value" 
            :class="passwordStrengthClass"
            :style="{ width: passwordStrength + '%' }"
          ></div>
        </div>
        <div class="strength-text" :class="passwordStrengthClass">{{ passwordStrengthText }}</div>
      </div>
    </template>

    <template #auth-links>
      <div class="auth-links-row">
        <router-link to="/login" class="back-link">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回登录</span>
        </router-link>
      </div>
    </template>
  </Auth>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Lock, InfoFilled, ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'
import Auth from './Auth.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const activeStep = ref(1)

const forgetForm = reactive({
  email: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value !== forgetForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 计算密码强度
const getPasswordStrength = (password) => {
  if (!password) return 0
  
  let strength = 0
  
  // 长度检查
  if (password.length >= 8) strength += 25
  else if (password.length >= 6) strength += 10
  
  // 包含数字
  if (/\d/.test(password)) strength += 25
  
  // 包含小写字母
  if (/[a-z]/.test(password)) strength += 15
  
  // 包含大写字母
  if (/[A-Z]/.test(password)) strength += 15
  
  // 包含特殊字符
  if (/[^A-Za-z0-9]/.test(password)) strength += 20
  
  return Math.min(strength, 100)
}

const passwordStrength = computed(() => getPasswordStrength(forgetForm.newPassword))

const passwordStrengthClass = computed(() => {
  if (passwordStrength.value < 30) return 'weak'
  if (passwordStrength.value < 60) return 'medium'
  return 'strong'
})

const passwordStrengthText = computed(() => {
  if (passwordStrength.value < 30) return '弱'
  if (passwordStrength.value < 60) return '中'
  return '强'
})

const handleSubmit = (form) => {
  formRef.value = form.value
  handleForgetPassword()
}

const handleForgetPassword = () => {
  formRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        activeStep.value = 2
        await request.post("/user/forget", {
          email: forgetForm.email,
          newPassword: forgetForm.newPassword
        }, {
          successMsg: "密码重置成功，请使用新密码登录",
          onSuccess: () => {
            activeStep.value = 3
            ElMessage({
              type: 'success',
              message: '密码已重置，2秒后将跳转到登录页面'
            })
            setTimeout(() => {
              router.push('/login')
            }, 2000)
          }
        })
      } finally {
        loading.value = false
      }
    }
  })
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

.reset-steps {
  margin-bottom: 24px;
  
  :deep(.el-step__title) {
    font-size: 14px;
  }
  
  :deep(.el-step__head.is-process) {
    color: #68a86d;
    border-color: #68a86d;
  }
  
  :deep(.el-step__head.is-finish) {
    color: #68a86d;
    border-color: #68a86d;
  }
  
  :deep(.el-step__title.is-process) {
    color: #68a86d;
  }
  
  :deep(.el-step__title.is-finish) {
    color: #68a86d;
  }
}

.info-message {
  display: flex;
  align-items: center;
  background-color: #f0f5e9;
  padding: 12px 15px;
  border-radius: 12px;
  margin-bottom: 20px;
  border-left: 4px solid #68a86d;
  
  .el-icon {
    color: #68a86d;
    margin-right: 10px;
    font-size: 18px;
  }
  
  span {
    color: #546e7a;
    font-size: 14px;
    line-height: 1.5;
  }
}

.password-strength {
  margin-bottom: 20px;
  
  .strength-label {
    font-size: 14px;
    color: #546e7a;
    margin-bottom: 6px;
  }
  
  .strength-meter {
    height: 6px;
    background-color: #eaeaea;
    border-radius: 3px;
    overflow: hidden;
    
    .strength-value {
      height: 100%;
      border-radius: 3px;
      transition: width 0.3s ease;
      
      &.weak {
        background-color: #ef5350;
      }
      
      &.medium {
        background-color: #ffb74d;
      }
      
      &.strong {
        background-color: #68a86d;
      }
    }
  }
  
  .strength-text {
    text-align: right;
    font-size: 12px;
    margin-top: 6px;
    
    &.weak {
      color: #ef5350;
    }
    
    &.medium {
      color: #ffb74d;
    }
    
    &.strong {
      color: #68a86d;
    }
  }
}

.auth-links-row {
  display: flex;
  justify-content: center;
  align-items: center;
  
  .back-link {
    display: flex;
    align-items: center;
    color: #68a86d;
    font-weight: 500;
    
    .el-icon {
      margin-right: 4px;
      font-size: 16px;
      transition: transform 0.3s ease;
    }
    
    &:hover {
      .el-icon {
        transform: translateX(-3px);
      }
    }
  }
}
</style> 