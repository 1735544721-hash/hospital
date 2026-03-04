<template>
  <div class="appointment-page">
    <div class="welcome-banner">
      <div class="banner-content">
        <h2>预约挂号</h2>
        <p>健康从这里开始，我们随时为您提供专业服务</p>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧科室和医生选择 -->
      <el-col :md="8" :sm="24">
        <el-card class="selection-card" shadow="hover">
          <div class="card-header-custom">
            <div class="header-icon">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <h3>选择科室</h3>
          </div>
          
          <!-- 科室列表 -->
          <div class="department-list">
            <el-scrollbar height="180px">
              <div class="dept-buttons">
                <el-button 
                v-for="dept in departmentList" 
                :key="dept.id" 
                  :class="['dept-button', selectedDepartmentId === dept.id ? 'dept-active' : '']"
                  @click="handleDepartmentChange(dept.id)"
              >
                {{ dept.deptName }}
                </el-button>
              </div>
            </el-scrollbar>
          </div>
          
          <!-- 医生列表 -->
          <div class="card-header-custom" style="margin-top: 30px;">
            <div class="header-icon">
              <el-icon><User /></el-icon>
            </div>
            <h3>选择医生</h3>
          </div>
          
          <div class="doctor-list">
            <el-empty v-if="!selectedDepartmentId" description="请先选择科室" />
            <el-empty v-else-if="doctorList.length === 0" description="该科室暂无可预约医生" />
            <el-scrollbar v-else height="320px">
              <div class="doctor-cards">
                <div 
                v-for="doctor in doctorList" 
                :key="doctor.id"
                  :class="['doctor-card', selectedDoctorId === doctor.id ? 'doctor-active' : '']"
                @click="handleDoctorSelect(doctor)"
              >
                  <div class="doctor-avatar">
                    <el-avatar :size="60" :src="getDoctorAvatarUrl(doctor.user?.avatar) || '/img/default_avatar.png'" />
                  </div>
                  <div class="doctor-details">
                    <h4>{{ doctor.name }}</h4>
                    <div class="doctor-title">{{ doctor.title || '医师' }}</div>
                    <p class="doctor-expertise">{{ doctor.expertise || '擅长：全科医疗' }}</p>
                  </div>
                </div>
            </div>
            </el-scrollbar>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧排班和预约 -->
      <el-col :md="16" :sm="24">
        <el-card class="schedule-card" shadow="hover">
          <template #header>
            <div class="schedule-header">
              <div class="step-indicator">
                <div class="step completed">
                  <div class="step-number">1</div>
                  <div class="step-name">选择科室</div>
                </div>
                <div class="step-line"></div>
                <div :class="['step', selectedDoctorId ? 'completed' : '']">
                  <div class="step-number">2</div>
                  <div class="step-name">选择医生</div>
                </div>
                <div class="step-line"></div>
                <div class="step">
                  <div class="step-number">3</div>
                  <div class="step-name">选择时间</div>
                </div>
              </div>
              
              <div class="date-selector" v-if="selectedDoctorId">
                <span class="date-label">选择日期范围：</span>
                <el-date-picker
                  v-model="selectedDate"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  :disabled-date="disabledDate"
                  @change="handleDateRangeChange"
                  size="large"
                  style="width: 320px;"
                />
              </div>
            </div>
          </template>
          
          <div v-if="!selectedDoctorId" class="empty-schedule-prompt">
            <el-icon :size="100" color="#a8d8ea"><User /></el-icon>
            <p>请先在左侧选择科室和医生</p>
            <p class="help-text">我们将为您展示可预约的时间</p>
          </div>
          
          <!-- 排班日历 -->
          <div v-else class="schedule-calendar">
            <el-empty v-if="scheduleList.length === 0" description="该医生暂无可预约时间">
              <template #image>
                <el-icon :size="60" color="#a8d8ea"><Calendar /></el-icon>
              </template>
              <p class="empty-schedule-text">您可以尝试选择其他日期范围或其他医生</p>
            </el-empty>
            
            <div v-else class="schedule-list">
              <div 
                v-for="schedule in scheduleList" 
                :key="schedule.id"
                :id="`schedule-${schedule.id}`"
                class="schedule-item"
              >
                <div class="schedule-date-info">
                  <div class="schedule-date">{{ formatDate(schedule.scheduleDate) }}</div>
                  <div class="schedule-day">{{ getDayOfWeek(schedule.scheduleDate) }}</div>
                  </div>
                
                <div class="schedule-time-info">
                  <el-tag :type="getTimeSlotType(schedule.timeSlot)" effect="light" round>{{ schedule.timeSlot }}</el-tag>
                  <div class="schedule-quota">剩余名额: {{ schedule.maxPatients - schedule.currentPatients }}</div>
                  </div>
                
                  <div class="schedule-action">
                    <el-button 
                    :type="schedule.maxPatients <= schedule.currentPatients ? 'info' : 'primary'"
                      :disabled="schedule.maxPatients <= schedule.currentPatients"
                      @click="handleAppointment(schedule)"
                    round
                    >
                    {{ schedule.maxPatients <= schedule.currentPatients ? '已满' : '预约' }}
                    </el-button>
                  </div>
                </div>
            </div>
          </div>
        </el-card>
        
        <!-- 健康提示 -->
        <el-card class="health-tips-card" shadow="hover">
          <div class="tips-header">
            <el-icon><InfoFilled /></el-icon>
            <h4>挂号小贴士</h4>
          </div>
          <ul class="tips-list">
            <li>请尽量提前预约，以便我们为您安排更好的就诊体验</li>
            <li>如需取消预约，请提前24小时操作，以便其他患者使用</li>
            <li>就诊时请携带有效证件，提前15分钟到达医院</li>
            <li>如有疑问，可拨打咨询电话：0123-4567890</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 预约表单对话框 -->
    <el-dialog
      v-model="appointmentDialogVisible"
      title="确认预约信息"
      width="500px"
      class="appointment-dialog"
    >
      <div class="dialog-content">
        <div class="appointment-summary">
          <div class="summary-icon">
            <el-icon><Calendar /></el-icon>
          </div>
          <h3 class="summary-title">预约详情</h3>
        </div>
        
        <el-divider />
        
      <el-form
        ref="appointmentFormRef"
        :model="appointmentForm"
        :rules="appointmentFormRules"
        label-width="100px"
      >
        <el-form-item label="就诊日期">
            <el-input v-model="appointmentInfo.date" disabled class="custom-disabled-input" />
        </el-form-item>
        <el-form-item label="就诊时间">
            <el-input v-model="appointmentInfo.timeSlot" disabled class="custom-disabled-input" />
        </el-form-item>
        <el-form-item label="科室">
            <el-input v-model="appointmentInfo.department" disabled class="custom-disabled-input" />
        </el-form-item>
        <el-form-item label="医生">
            <el-input v-model="appointmentInfo.doctor" disabled class="custom-disabled-input" />
        </el-form-item>
        <el-form-item label="症状描述" prop="symptoms">
          <el-input
            v-model="appointmentForm.symptoms"
            type="textarea"
            :rows="4"
              placeholder="请简要描述您的症状和就诊需求，帮助医生更好地了解您的情况"
          />
        </el-form-item>
      </el-form>
        
        <div class="notice-box">
          <el-icon><Warning /></el-icon>
          <p>预约成功后，请按时就诊。如需取消，请提前24小时操作。</p>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="appointmentDialogVisible = false" plain round>取消</el-button>
          <el-button type="primary" @click="submitAppointment" round>确认预约</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, User, Calendar, InfoFilled, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 获取医生头像URL
const baseAPI = import.meta.env.VITE_API_BASE_URL || '/api'
const getDoctorAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return baseAPI + avatar
}

// 科室和医生数据
const departmentList = ref([])
const doctorList = ref([])
const scheduleList = ref([])

// 选中的科室和医生ID
const selectedDepartmentId = ref(null)
const selectedDoctorId = ref(null)
const selectedDate = ref([])

// 预约相关
const appointmentDialogVisible = ref(false)
const appointmentFormRef = ref(null)
const appointmentForm = reactive({
  patientId: null,
  doctorId: null,
  scheduleId: null,
  symptoms: ''
})

// 预约信息展示
const appointmentInfo = reactive({
  date: '',
  timeSlot: '',
  department: '',
  doctor: ''
})

// 表单验证规则
const appointmentFormRules = {
  symptoms: [{ required: true, message: '请描述您的症状', trigger: 'blur' }]
}

// 加载数据
onMounted(() => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 加载科室列表
  fetchDepartments().then(() => {
    // 检查URL参数
    const { departmentId, doctorId, scheduleId } = router.currentRoute.value.query
    
    // 如果有科室ID参数，自动选择该科室
    if (departmentId && parseInt(departmentId) > 0) {
      selectedDepartmentId.value = parseInt(departmentId)
      
      // 获取该科室的医生列表
      fetchDoctorsByDepartment(selectedDepartmentId.value).then(() => {
        // 如果有医生ID参数，自动选择该医生
        if (doctorId && parseInt(doctorId) > 0) {
          selectedDoctorId.value = parseInt(doctorId)
          
          // 设置默认日期范围为今天开始的7天
          const today = new Date()
          const nextWeek = new Date()
          nextWeek.setDate(today.getDate() + 6)
          
          selectedDate.value = [today, nextWeek]
          
          // 获取医生排班
          fetchDoctorSchedules().then(() => {
            // 如果有排班ID参数，自动滚动到该排班
            if (scheduleId && parseInt(scheduleId) > 0) {
              const targetScheduleId = parseInt(scheduleId)
              
              // 给DOM一点时间渲染
              setTimeout(() => {
                const targetSchedule = scheduleList.value.find(s => s.id === targetScheduleId)
                if (targetSchedule) {
                  // 找到对应的DOM元素并滚动到视图
                  const element = document.getElementById(`schedule-${targetScheduleId}`)
                  if (element) {
                    element.scrollIntoView({ behavior: 'smooth', block: 'center' })
                    // 高亮显示
                    element.classList.add('highlight-schedule')
                    setTimeout(() => {
                      element.classList.remove('highlight-schedule')
                    }, 2000)
                  }
                }
              }, 500)
            }
          })
        }
      })
    }
  })
})

// 获取科室列表
const fetchDepartments = async () => {
  try {
    return new Promise((resolve) => {
      request.get('/department/list', {}, {
      onSuccess: (res) => {
        departmentList.value = res || []
          resolve()
      }
      })
    })
  } catch (error) {
    console.error('获取科室列表失败:', error)
    return Promise.reject(error)
  }
}

// 科室变更处理
const handleDepartmentChange = (deptId) => {
  selectedDepartmentId.value = deptId
  selectedDoctorId.value = null
  scheduleList.value = []
  fetchDoctorsByDepartment(deptId)
}

// 获取科室医生
const fetchDoctorsByDepartment = async (deptId) => {
  try {
    return new Promise((resolve) => {
      request.get(`/doctor/department/${deptId}`, {}, {
      onSuccess: (res) => {
        doctorList.value = res || []
          resolve()
      }
      })
    })
  } catch (error) {
    console.error('获取科室医生失败:', error)
    return Promise.reject(error)
  }
}

// 选择医生
const handleDoctorSelect = (doctor) => {
  selectedDoctorId.value = doctor.id
  
  // 设置默认日期范围为今天开始的7天
  const today = new Date()
  const nextWeek = new Date()
  nextWeek.setDate(today.getDate() + 6)
  
  selectedDate.value = [today, nextWeek]
  
  // 获取医生排班
  fetchDoctorSchedules()
}

// 日期范围变更
const handleDateRangeChange = () => {
  if (selectedDoctorId.value) {
    fetchDoctorSchedules()
  }
}

// 获取医生排班
const fetchDoctorSchedules = async () => {
  if (!selectedDate.value || selectedDate.value.length !== 2) {
    return Promise.resolve()
  }
  
  try {
    const startDate = formatDateForApi(selectedDate.value[0])
    const endDate = formatDateForApi(selectedDate.value[1])
    
    return new Promise((resolve) => {
      request.get(`/schedule/doctor/${selectedDoctorId.value}`, {
      startDate,
      endDate
    }, {
      onSuccess: (res) => {
        scheduleList.value = res || []
          resolve()
      }
      })
    })
  } catch (error) {
    console.error('获取医生排班失败:', error)
    return Promise.reject(error)
  }
}

// 点击预约
const handleAppointment = (schedule) => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 检查是否有患者信息
  if (!userStore.patientInfo) {
    ElMessageBox.confirm(
      '您尚未完善患者信息，需要先完善患者信息才能预约',
      '提示',
      {
        confirmButtonText: '去完善',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
      .then(() => {
        router.push('/profile')
      })
      .catch(() => {})
    return
  }
  
  // 设置预约信息
  appointmentForm.patientId = userStore.patientInfo.id
  appointmentForm.doctorId = selectedDoctorId.value
  appointmentForm.scheduleId = schedule.id
  appointmentForm.symptoms = ''
  
  // 设置预约信息展示
  const selectedDoctor = doctorList.value.find(d => d.id === selectedDoctorId.value)
  const selectedDepartment = departmentList.value.find(d => d.id === selectedDepartmentId.value)
  
  appointmentInfo.date = formatDate(schedule.scheduleDate)
  appointmentInfo.timeSlot = schedule.timeSlot
  appointmentInfo.department = selectedDepartment ? selectedDepartment.deptName : ''
  appointmentInfo.doctor = selectedDoctor ? `${selectedDoctor.name} ${selectedDoctor.title || ''}` : ''
  
  // 显示预约对话框
  appointmentDialogVisible.value = true
}

// 提交预约
const submitAppointment = () => {
  appointmentFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/appointment', appointmentForm, {
          successMsg: '预约成功',
          onSuccess: () => {
            appointmentDialogVisible.value = false
            
            // 刷新排班列表
            fetchDoctorSchedules()
            
            // 询问是否查看预约记录
            ElMessageBox.confirm(
              '预约成功，是否查看我的预约记录？',
              '提示',
              {
                confirmButtonText: '查看',
                cancelButtonText: '继续预约',
                type: 'success'
              }
            )
              .then(() => {
                router.push('/my-appointments')
              })
              .catch(() => {})
          }
        })
      } catch (error) {
        console.error('预约失败:', error)
      }
    }
  })
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

// 格式化日期为 YYYY-MM-DD
const formatDateForApi = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化日期为更友好的格式
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}年${month}月${day}日`
}

// 获取星期几
const getDayOfWeek = (dateStr) => {
  const date = new Date(dateStr)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[date.getDay()]
}

// 获取时间段标签类型
const getTimeSlotType = (timeSlot) => {
  switch (timeSlot) {
    case '上午':
      return 'success'
    case '下午':
      return 'warning'
    case '晚上':
      return 'danger'
    default:
      return 'info'
  }
}
</script>

<style scoped>
.appointment-page {
  padding: 20px;
  background-color: #f9f7f2;
  min-height: calc(100vh - 160px);
}

.welcome-banner {
  background: linear-gradient(to right, #a8d8ea, #c4e3b2);
  border-radius: 15px;
  padding: 25px 40px;
  margin-bottom: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.banner-content {
  max-width: 800px;
  text-align: center;
  margin: 0 auto;
}

.banner-content h2 {
  color: #3a5463;
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

.banner-content p {
  color: #5c7b8a;
  margin: 0;
  font-size: 16px;
}

.selection-card, .schedule-card, .health-tips-card {
  border-radius: 15px;
  margin-bottom: 20px;
  border: none;
  background-color: #ffffff;
}

.card-header-custom {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.header-icon {
  background-color: #a8d8ea;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.header-icon .el-icon {
  color: #ffffff;
  font-size: 18px;
}

.card-header-custom h3 {
  margin: 0;
  color: #3a5463;
  font-weight: 600;
  font-size: 18px;
}

.department-list {
  margin-bottom: 20px;
}

.dept-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.dept-button {
  border-radius: 20px;
  background-color: #f9f7f2;
  color: #5c7b8a;
  border: 1px solid #e8e1d0;
  padding: 8px 16px;
  transition: all 0.3s;
}

.dept-button:hover {
  background-color: #f0eada;
  color: #3a5463;
}

.dept-active {
  background-color: #c4e3b2;
  color: #3a5463;
  border-color: #c4e3b2;
}

.doctor-cards {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.doctor-card {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 12px;
  background-color: #f9f7f2;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.doctor-card:hover {
  background-color: #f0eada;
  transform: translateY(-2px);
}

.doctor-active {
  background-color: #e6f2e2;
  border: 1px solid #c4e3b2;
}

.doctor-avatar {
  margin-right: 15px;
}

.doctor-details {
  flex: 1;
}

.doctor-details h4 {
  margin: 0 0 5px 0;
  color: #3a5463;
  font-size: 16px;
  font-weight: 600;
}

.doctor-title {
  display: inline-block;
  background-color: #a8d8ea;
  color: #ffffff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  margin-bottom: 5px;
}

.doctor-expertise {
  margin: 5px 0 0 0;
  font-size: 12px;
  color: #8a9ca7;
  line-height: 1.4;
}

.schedule-header {
  padding: 10px 0;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 25px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.step-number {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #e8e1d0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8a9ca7;
  font-weight: 600;
  margin-bottom: 5px;
}

.step.completed .step-number {
  background-color: #c4e3b2;
  color: #ffffff;
}

.step-name {
  font-size: 14px;
  color: #8a9ca7;
}

.step.completed .step-name {
  color: #3a5463;
  font-weight: 500;
}

.step-line {
  width: 60px;
  height: 2px;
  background-color: #e8e1d0;
  margin: 0 15px;
}

.date-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
}

.date-label {
  margin-right: 10px;
  color: #5c7b8a;
  font-size: 15px;
}

.empty-schedule-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  color: #8a9ca7;
}

.placeholder-image {
  width: 200px;
  margin-bottom: 20px;
  opacity: 0.7;
}

.help-text {
  margin-top: 5px;
  font-size: 14px;
  color: #a8b5bd;
}

.schedule-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
  padding: 10px;
}

.schedule-item {
  display: flex;
  align-items: center;
  background-color: #f9f7f2;
  border-radius: 12px;
  padding: 15px;
  transition: all 0.3s;
}

.schedule-item:hover {
  background-color: #f0eada;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.schedule-date-info {
  min-width: 100px;
}

.schedule-date {
  font-weight: 600;
  color: #3a5463;
  margin-bottom: 5px;
}

.schedule-day {
  font-size: 13px;
  color: #8a9ca7;
}

.schedule-time-info {
  flex: 1;
  margin: 0 15px;
}

.schedule-quota {
  font-size: 13px;
  color: #8a9ca7;
  margin-top: 8px;
}

.el-tag {
  border-radius: 15px;
  padding: 0 12px;
  height: 26px;
  line-height: 26px;
}

.health-tips-card {
  padding: 15px;
}

.tips-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.tips-header .el-icon {
  color: #fbc687;
  font-size: 20px;
  margin-right: 10px;
}

.tips-header h4 {
  margin: 0;
  color: #3a5463;
  font-size: 16px;
}

.tips-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  position: relative;
  padding-left: 20px;
  margin-bottom: 10px;
  color: #5c7b8a;
  font-size: 14px;
  line-height: 1.5;
}

.tips-list li::before {
  content: "•";
  color: #a8d8ea;
  font-size: 20px;
  position: absolute;
  left: 0;
  top: -2px;
}

.appointment-dialog {
  border-radius: 15px;
}

.dialog-content {
  padding: 0 20px;
}

.appointment-summary {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.summary-icon {
  background-color: #c4e3b2;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.summary-icon .el-icon {
  color: #ffffff;
  font-size: 18px;
}

.summary-title {
  margin: 0;
  color: #3a5463;
  font-weight: 600;
  font-size: 18px;
}

.custom-disabled-input :deep(.el-input__inner) {
  background-color: #f9f7f2;
  color: #5c7b8a;
  border-color: #e8e1d0;
}

.notice-box {
  display: flex;
  align-items: flex-start;
  background-color: #fff8e6;
  padding: 12px 15px;
  border-radius: 10px;
  margin-top: 20px;
}

.notice-box .el-icon {
  color: #fbc687;
  font-size: 18px;
  margin-right: 10px;
  margin-top: 2px;
}

.notice-box p {
  margin: 0;
  color: #927d5e;
  font-size: 13px;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 10px;
}

.empty-schedule-text {
  color: #8a9ca7;
  font-size: 14px;
  margin-top: 10px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .welcome-banner {
    padding: 20px;
  }
  
  .banner-content h2 {
    font-size: 22px;
  }
  
  .step-indicator {
    flex-direction: column;
    gap: 15px;
  }
  
  .step-line {
    width: 2px;
    height: 20px;
    margin: 5px 0;
  }
  
  .schedule-list {
    grid-template-columns: 1fr;
  }
}

.highlight-schedule {
  background-color: #e6f2e2;
  box-shadow: 0 0 0 2px #c4e3b2;
  animation: pulse 1s ease-in-out;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(196, 227, 178, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(196, 227, 178, 0); }
  100% { box-shadow: 0 0 0 0 rgba(196, 227, 178, 0); }
}
</style> 