<template>
  <Auth 
    :formData="registerForm" 
    :rules="rules" 
    :loading="loading"
    submitText="完成注册"
    @submit="handleSubmit"
  >
    <template #form-items>
      <div class="welcome-message">
        <h2>加入我们</h2>
        <p>填写信息，开启您的健康管理旅程</p>
      </div>
      
      <div class="form-section">
        <div class="section-title">
          <el-icon><UserFilled /></el-icon>
          <span>账号信息</span>
        </div>
        
        <el-form-item prop="user.username">
          <el-input 
            v-model="registerForm.user.username"
            :prefix-icon="User"
            placeholder="请输入用户名（用于登录）">
          </el-input>
        </el-form-item>
        <el-form-item prop="user.password">
          <el-input 
            v-model="registerForm.user.password"
            :prefix-icon="Lock"
            type="password"
            placeholder="请输入密码">
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword"
            :prefix-icon="Lock"
            type="password"
            placeholder="请确认密码">
          </el-input>
        </el-form-item>
      </div>
      
      <div class="form-section">
        <div class="section-title">
          <el-icon><Document /></el-icon>
          <span>基本信息</span>
        </div>
        
        <el-form-item prop="patient.name">
          <el-input 
            v-model="registerForm.patient.name"
            :prefix-icon="UserFilled"
            placeholder="请输入真实姓名">
          </el-input>
        </el-form-item>
        
        <div class="form-row">
          <el-form-item prop="patient.sex" class="form-item-half">
            <el-select v-model="registerForm.patient.sex" placeholder="请选择性别" style="width: 100%">
              <el-option label="男" value="男"></el-option>
              <el-option label="女" value="女"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item prop="patient.birthday" class="form-item-half">
            <el-date-picker
              v-model="registerForm.patient.birthday"
              type="date"
              placeholder="出生日期"
              style="width: 100%"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD">
            </el-date-picker>
          </el-form-item>
        </div>
        
        <el-form-item prop="patient.idCard">
          <el-input 
            v-model="registerForm.patient.idCard"
            :prefix-icon="Document"
            placeholder="请输入身份证号">
          </el-input>
        </el-form-item>
      </div>
      
      <div class="form-section">
        <div class="section-title">
          <el-icon><Message /></el-icon>
          <span>联系方式</span>
        </div>
        
        <el-form-item prop="patient.phone">
          <el-input 
            v-model="registerForm.patient.phone"
            :prefix-icon="Phone"
            placeholder="请输入手机号">
          </el-input>
        </el-form-item>
        <el-form-item prop="user.email">
          <el-input 
            v-model="registerForm.user.email"
            :prefix-icon="Message"
            placeholder="请输入邮箱">
          </el-input>
        </el-form-item>
        <el-form-item prop="patient.address">
          <el-input 
            v-model="registerForm.patient.address"
            :prefix-icon="Location"
            placeholder="请输入住址">
          </el-input>
        </el-form-item>
      </div>
      
      <div class="form-section">
        <div class="section-title">
          <el-icon><FirstAidKit /></el-icon>
          <span>健康信息（选填）</span>
        </div>
        
        <el-form-item prop="patient.medicalHistory">
          <el-input 
            v-model="registerForm.patient.medicalHistory"
            type="textarea"
            :rows="2"
            placeholder="请简要描述您的病史（如有）">
          </el-input>
        </el-form-item>
        <el-form-item prop="patient.allergies">
          <el-input 
            v-model="registerForm.patient.allergies"
            type="textarea"
            :rows="2"
            placeholder="请描述您的过敏史（如有）">
          </el-input>
        </el-form-item>
      </div>
      
      <el-form-item prop="agreement">
        <el-checkbox v-model="registerForm.agreement">我已阅读并同意<a href="javascript:void(0)" @click="showTerms">《用户协议》</a>和<a href="javascript:void(0)" @click="showPrivacy">《隐私政策》</a></el-checkbox>
      </el-form-item>
    </template>

    <template #auth-links>
      已有账号？<router-link to="/login">立即登录</router-link>
    </template>
  </Auth>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message, Phone, UserFilled, Document, Location, FirstAidKit } from '@element-plus/icons-vue'
import request from '@/utils/request'
import Auth from './Auth.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  user: {
    username: '',
    password: '',
    email: '',
    roleCode: 'PATIENT', // 默认注册为患者
  },
  patient: {
    name: '',
    sex: '',
    birthday: '',
    idCard: '',
    phone: '',
    address: '',
    medicalHistory: '',
    allergies: '',
  },
  confirmPassword: '',
  agreement: false,
})

const validatePass2 = (rule, value, callback) => {
  if (value !== registerForm.user.password) {
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

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const validateIdCard = (rule, value, callback) => {
  if (value && !/^\d{17}[\dX]$/.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议和隐私政策'))
  } else {
    callback()
  }
}

const rules = {
  'user.username': [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3到50个字符之间', trigger: 'blur' }
  ],
  'user.password': [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  'patient.name': [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  'patient.sex': [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  'patient.birthday': [
    { required: false, message: '请选择出生日期', trigger: 'change' }
  ],
  'patient.idCard': [
    { required: false, message: '请输入身份证号', trigger: 'blur' },
    { validator: validateIdCard, trigger: 'blur' }
  ],
  'patient.phone': [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  'user.email': [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  'patient.address': [
    { required: false, message: '请输入住址', trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

const showTerms = () => {
  ElMessageBox.alert(
    '用户在注册和使用过程中，必须遵守相关法律法规，不得用于非法用途。医院有权在用户违反规定时终止服务。',
    '用户协议',
    {
      confirmButtonText: '我知道了',
      type: 'info',
    }
  )
}

const showPrivacy = () => {
  ElMessageBox.alert(
    '我们会收集您的基本信息和健康数据，仅用于提供医疗服务和改善用户体验。我们承诺对您的信息进行严格保密，不会在未经您授权的情况下向第三方透露。',
    '隐私政策',
    {
      confirmButtonText: '我知道了',
      type: 'info',
    }
  )
}

const handleSubmit = (form) => {
  formRef.value = form.value
  registerFormRef.value = form.value
  handleRegister()
}

const handleRegister = () => {
  registerFormRef.value.validate(async valid => {
    if (valid) {
      loading.value = true
      try {
        // 同步用户名和真实姓名
        registerForm.user.name = registerForm.patient.name
        registerForm.user.sex = registerForm.patient.sex
        registerForm.user.phone = registerForm.patient.phone
        
        // 移除协议同意，添加确认密码
        const { agreement, ...registerData } = registerForm
        
        // 使用患者注册接口
        await request.post("/patient/register", registerData, {
          successMsg: "注册成功，欢迎加入康泰医疗",
          showDefaultMsg: true,
          onSuccess: () => {
            ElMessage({
              type: 'success',
              message: '您的账号已创建成功，正在跳转到登录页面...'
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

.form-section {
  margin-bottom: 24px;
  
  .section-title {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 8px;
    color: #546e7a;
    font-weight: 500;
    
    .el-icon {
      color: #68a86d;
      margin-right: 8px;
      font-size: 18px;
    }
    
    span {
      font-size: 16px;
    }
  }
}

.form-row {
  display: flex;
  gap: 16px;
  
  .form-item-half {
    flex: 1;
  }
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #68a86d;
  border-color: #68a86d;
}

:deep(.el-checkbox__label) {
  color: #546e7a;
  
  a {
    color: #68a86d;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style> 