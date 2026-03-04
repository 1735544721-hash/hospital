<template>
  <div class="doctor-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h2><el-icon><User /></el-icon> 医生介绍</h2>
        <p>了解我院优秀医生团队，为您提供专业医疗服务</p>
      </div>
        </div>

    <el-card class="box-card" shadow="hover">
      <div class="filter-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="科室">
            <el-select v-model="searchForm.departmentId" placeholder="选择科室" clearable>
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.deptName"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="医生姓名">
            <el-input v-model="searchForm.name" placeholder="请输入医生姓名" clearable />
          </el-form-item>
          <el-form-item label="职称">
            <el-select v-model="searchForm.title" placeholder="选择职称" clearable>
              <el-option label="主任医师" value="主任医师" />
              <el-option label="副主任医师" value="副主任医师" />
              <el-option label="主治医师" value="主治医师" />
              <el-option label="住院医师" value="住院医师" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" round>
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button @click="resetSearch" round>
              <el-icon><RefreshRight /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 医生列表 -->
      <div v-if="!selectedDoctor" class="doctor-list">
        <el-row :gutter="24">
          <el-col :span="8" v-for="doctor in doctors" :key="doctor.id">
            <el-card class="doctor-card" shadow="hover" @click="viewDoctorDetail(doctor)">
              <div class="doctor-card-content">
                <div class="doctor-avatar">
                  <el-avatar :size="80" :src="getAvatarUrl(doctor.user?.avatar)" />
                </div>
                <div class="doctor-info">
                  <h3>{{ doctor.name }}
                    <el-tag size="small" :type="getTagType(doctor.title)">{{ doctor.title }}</el-tag>
                  </h3>
                  <p class="department">{{ doctor.department?.deptName || '未知科室' }}</p>
                  <p class="expertise" v-if="doctor.expertise">专长：{{ doctor.expertise }}</p>
                </div>
              </div>
              <div class="doctor-action">
                <el-button type="primary" size="small" round @click.stop="goToAppointment(doctor)" v-if="isLoggedIn">
                  <el-icon><Calendar /></el-icon> 预约挂号
                </el-button>
                <el-button size="small" round @click.stop="viewDoctorDetail(doctor)">
                  <el-icon><View /></el-icon> 查看详情
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 分页 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[9, 18, 36]"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>

        <el-empty v-if="doctors.length === 0" description="暂无医生信息">
          <template #image>
            <el-icon class="empty-icon"><User /></el-icon>
          </template>
        </el-empty>
      </div>

      <!-- 医生详情 -->
      <div v-else class="doctor-detail">
        <div class="detail-header">
          <el-button icon="ArrowLeft" @click="backToList" round>返回列表</el-button>
        </div>

        <el-row :gutter="30">
          <!-- 左侧基本信息 -->
          <el-col :span="8">
            <div class="doctor-profile">
              <div class="doctor-avatar">
                <el-avatar :size="120" :src="getAvatarUrl(selectedDoctor.user?.avatar)" />
              </div>
              <h2>{{ selectedDoctor.name }}</h2>
              <el-tag size="large" :type="getTagType(selectedDoctor.title)">{{ selectedDoctor.title }}</el-tag>
              <p class="department">{{ selectedDoctor.department?.deptName }}</p>
              
              <div class="doctor-stats">
                <div class="stat-item">
                  <el-icon><Medal /></el-icon>
                  <div class="stat-info">
                    <span class="stat-label">从医经验</span>
                    <span class="stat-value">{{ calculateExperience() }}年</span>
                  </div>
                </div>
                <div class="stat-item">
                  <el-icon><Star /></el-icon>
                  <div class="stat-info">
                    <span class="stat-label">患者好评</span>
                    <span class="stat-value">96%</span>
                  </div>
                </div>
              </div>
              
              <el-divider />
              
              <el-button type="primary" round @click="goToAppointment(selectedDoctor)" v-if="isLoggedIn">
                <el-icon><Calendar /></el-icon> 预约挂号
              </el-button>
            </div>
          </el-col>
          
          <!-- 右侧详细信息 -->
          <el-col :span="16">
            <el-tabs class="custom-tabs">
              <el-tab-pane label="医生简介">
                <div class="doctor-introduction">
                  <div class="info-section">
                    <div class="section-header">
                      <el-icon><InfoFilled /></el-icon>
                  <h4>个人简介</h4>
                    </div>
                    <div class="section-content">
                  <p v-if="selectedDoctor.introduction">{{ selectedDoctor.introduction }}</p>
                  <el-empty v-else description="暂无简介信息" :image-size="60" />
                    </div>
                  </div>
                  
                  <div class="info-section">
                    <div class="section-header">
                      <el-icon><Aim /></el-icon>
                  <h4>专业特长</h4>
                    </div>
                    <div class="section-content">
                  <p v-if="selectedDoctor.expertise">{{ selectedDoctor.expertise }}</p>
                  <el-empty v-else description="暂无专业特长信息" :image-size="60" />
                    </div>
                  </div>
                </div>
              </el-tab-pane>
              
              <el-tab-pane label="出诊信息">
                <div class="doctor-schedule">
                  <div class="schedule-legend">
                    <div class="legend-item">
                      <el-tag type="success" size="small">可预约</el-tag>
                      <span>有号源可预约</span>
                    </div>
                    <div class="legend-item">
                      <el-tag type="warning" size="small">即将满员</el-tag>
                      <span>剩余号源较少</span>
                    </div>
                    <div class="legend-item">
                      <el-tag type="info" size="small">停诊</el-tag>
                      <span>暂停接诊</span>
                    </div>
                    <div class="legend-item">
                      <el-tag type="danger" size="small">已满</el-tag>
                      <span>号源已满</span>
                    </div>
                  </div>
                  
                  <el-empty v-if="schedules.length === 0" description="暂无排班信息" />
                  
                  <div v-else class="schedule-cards">
                    <el-card 
                      v-for="schedule in schedules" 
                      :key="schedule.id" 
                      class="schedule-card"
                      :class="{
                        'available': schedule.status === 1 && schedule.currentPatients < schedule.maxPatients,
                        'almost-full': schedule.status === 1 && schedule.currentPatients >= schedule.maxPatients * 0.8 && schedule.currentPatients < schedule.maxPatients,
                        'full': schedule.status === 1 && schedule.currentPatients >= schedule.maxPatients,
                        'stopped': schedule.status === 0
                      }"
                    >
                      <div class="schedule-card-header">
                        <div class="schedule-date">{{ formatDisplayDate(schedule.scheduleDate) }}</div>
                        <div class="schedule-time">{{ schedule.timeSlot }}</div>
                      </div>
                      <div class="schedule-card-body">
                        <div class="schedule-status">
                          <el-tag 
                            :type="getScheduleTagType(schedule)" 
                            size="small"
                          >
                            {{ getScheduleStatusText(schedule) }}
                          </el-tag>
                        </div>
                        <div class="schedule-count">
                          <el-progress 
                            :percentage="calculatePercentage(schedule)" 
                            :status="getProgressStatus(schedule)"
                            :stroke-width="10"
                            :show-text="false"
                          ></el-progress>
                          <div class="count-text">{{ schedule.currentPatients }}/{{ schedule.maxPatients }}</div>
                        </div>
                        <div class="schedule-action">
                          <el-button 
                            type="primary" 
                            size="small" 
                            round 
                            :disabled="!canBook(schedule)"
                            @click="goToAppointmentWithSchedule(schedule)"
                            v-if="isLoggedIn"
                          >
                            <el-icon><Calendar /></el-icon> 立即预约
                          </el-button>
                        </div>
                      </div>
                    </el-card>
                  </div>
                </div>
              </el-tab-pane>

              <el-tab-pane label="患者评价">
                <div class="doctor-reviews">
                  <div class="review-stats">
                    <div class="rating">
                      <span class="rating-score">4.8</span>
                      <div class="rating-stars">
                        <el-icon v-for="i in 5" :key="i" class="star-icon">
                          <Star :fill="i <= 4 ? '#FFB800' : '#E0E0E0'" />
                        </el-icon>
                      </div>
                      <span class="rating-count">96% 好评率</span>
                    </div>
                  </div>
                  
                  <div class="review-list">
                    <div class="empty-reviews">
                      <el-empty description="暂无患者评价" :image-size="80" />
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { 
  ArrowLeft, 
  Search, 
  RefreshRight, 
  Calendar, 
  User, 
  View, 
  Medal, 
  Star, 
  InfoFilled, 
  Aim
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 用户是否登录
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 科室列表
const departments = ref([])
// 医生列表
const doctors = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(9)

// 搜索表单
const searchForm = reactive({
  departmentId: null,
  name: '',
  title: ''
})

// 选中的医生
const selectedDoctor = ref(null)
// 医生排班
const schedules = ref([])

// 计算医生经验年限（随机生成8-25年之间的数字）
const calculateExperience = () => {
  // 使用医生ID作为种子生成一个相对固定的随机数
  const seed = selectedDoctor.value?.id || 1
  return 8 + (seed % 18) // 8到25年之间
}

// 根据职称获取标签类型
const getTagType = (title) => {
  switch(title) {
    case '主任医师': return 'danger'
    case '副主任医师': return 'warning'
    case '主治医师': return 'success'
    case '住院医师': return 'info'
    default: return ''
  }
}

// 获取科室列表
const fetchDepartments = async () => {
  try {
    await request.get('/department/list', {}, {
      onSuccess: (res) => {
        departments.value = res || []
      }
    })
  } catch (error) {
    console.error('获取科室列表失败:', error)
  }
}

// 获取医生列表
const fetchDoctors = async () => {
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      departmentId: searchForm.departmentId,
      name: searchForm.name,
      title: searchForm.title
    }
    
    await request.get('/doctor/page', params, {
      onSuccess: (res) => {
        doctors.value = res.records || []
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取医生列表失败:', error)
  }
}

// 获取医生详情
const fetchDoctorDetail = async (id) => {
  try {
    await request.get(`/doctor/${id}`, {}, {
      onSuccess: (res) => {
        selectedDoctor.value = res
        fetchDoctorSchedule(id)
      }
    })
  } catch (error) {
    console.error('获取医生详情失败:', error)
  }
}

// 获取医生排班
const fetchDoctorSchedule = async (doctorId) => {
  try {
    // 获取未来一个月的排班
    const today = new Date()
    const nextMonth = new Date()
    nextMonth.setMonth(today.getMonth() + 1)
    
    const params = {
      startDate: formatDate(today),
      endDate: formatDate(nextMonth)
    }
    
    await request.get(`/schedule/doctor/${doctorId}`, params, {
      onSuccess: (res) => {
        schedules.value = res || []
      }
    })
  } catch (error) {
    console.error('获取医生排班失败:', error)
  }
}

// 格式化日期
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 根据日期获取排班
const getSchedulesByDate = (dateStr) => {
  return schedules.value.filter(s => s.scheduleDate === dateStr)
}

// 获取头像URL
const baseAPI = import.meta.env.VITE_API_BASE_URL || '/api'
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return baseAPI + avatar
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchDoctors()
}

// 重置搜索
const resetSearch = () => {
  searchForm.departmentId = null
  searchForm.name = ''
  searchForm.title = ''
  currentPage.value = 1
  fetchDoctors()
}

// 查看医生详情
const viewDoctorDetail = (doctor) => {
  selectedDoctor.value = doctor
  fetchDoctorDetail(doctor.id)
}

// 返回列表
const backToList = () => {
  selectedDoctor.value = null
}

// 前往预约页面
const goToAppointment = (doctor) => {
  router.push(`/appointment?doctorId=${doctor.id}`)
}

// 分页操作
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchDoctors()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchDoctors()
}

// 格式化显示日期
const formatDisplayDate = (dateStr) => {
  const date = new Date(dateStr);
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekday = weekdays[date.getDay()];
  return `${month}月${day}日 ${weekday}`;
}

// 获取排班状态标签类型
const getScheduleTagType = (schedule) => {
  if (schedule.status === 0) return 'info';
  if (schedule.currentPatients >= schedule.maxPatients) return 'danger';
  if (schedule.currentPatients >= schedule.maxPatients * 0.8) return 'warning';
  return 'success';
}

// 获取排班状态文本
const getScheduleStatusText = (schedule) => {
  if (schedule.status === 0) return '停诊';
  if (schedule.currentPatients >= schedule.maxPatients) return '已满';
  if (schedule.currentPatients >= schedule.maxPatients * 0.8) return '即将满员';
  return '可预约';
}

// 计算进度条百分比
const calculatePercentage = (schedule) => {
  if (schedule.maxPatients === 0) return 0;
  return Math.round((schedule.currentPatients / schedule.maxPatients) * 100);
}

// 获取进度条状态
const getProgressStatus = (schedule) => {
  if (schedule.status === 0) return 'info';
  if (schedule.currentPatients >= schedule.maxPatients) return 'exception';
  if (schedule.currentPatients >= schedule.maxPatients * 0.8) return 'warning';
  return 'success';
}

// 判断是否可以预约
const canBook = (schedule) => {
  return schedule.status === 1 && schedule.currentPatients < schedule.maxPatients;
}

// 带排班信息前往预约页面
const goToAppointmentWithSchedule = (schedule) => {
  router.push({
    path: '/appointment',
    query: {
      doctorId: selectedDoctor.value.id,
      scheduleId: schedule.id,
      departmentId: selectedDoctor.value.departmentId
    }
  });
}

onMounted(() => {
  fetchDepartments()
  
  // 检查URL参数是否有医生ID
  const doctorId = route.query.id
  if (doctorId) {
    fetchDoctorDetail(doctorId)
  } else {
    fetchDoctors()
  }
})
</script>

<style scoped>
.doctor-page {
  padding: 20px;
}

.welcome-banner {
  background: linear-gradient(to right, #a8d8ea, #c4e3b2);
  border-radius: 15px;
  padding: 25px 40px;
  margin-bottom: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  animation: fadeIn 0.8s ease-in-out;
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-content h2 .el-icon {
  margin-right: 10px;
  font-size: 28px;
  color: #76c893;
}

.banner-content p {
  color: #5a7385;
  font-size: 16px;
  margin: 0;
}

.box-card {
  border-radius: 12px;
  background-color: #ffffff;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
  margin-bottom: 30px;
}

.filter-container {
  background-color: #f9f7f2;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-form :deep(.el-form-item__label) {
  color: #5a7385;
  font-weight: 500;
}

.search-form :deep(.el-button--primary) {
  background-color: #76c893;
  border-color: #76c893;
}

.search-form :deep(.el-button--primary:hover) {
  background-color: #62b389;
  border-color: #62b389;
}

.doctor-list {
  margin-top: 20px;
  animation: fadeIn 0.6s ease-in-out;
}

.doctor-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  border-radius: 10px;
  overflow: hidden;
}

.doctor-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.doctor-card-content {
  display: flex;
  margin-bottom: 15px;
}

.doctor-avatar {
  margin-right: 15px;
  display: flex;
  justify-content: center;
}

.doctor-info {
  flex: 1;
}

.doctor-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  color: #3a5463;
  font-size: 18px;
}

.doctor-info h3 .el-tag {
  margin-left: 10px;
}

.department {
  color: #5a7385;
  margin-bottom: 5px;
  font-weight: 500;
}

.expertise {
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}

.doctor-action {
  display: flex;
  justify-content: space-between;
}

.doctor-action .el-button {
  flex: 1;
  margin: 0 5px;
}

.doctor-action .el-button--primary {
  background-color: #76c893;
  border-color: #76c893;
}

.doctor-action .el-button--primary:hover {
  background-color: #62b389;
  border-color: #62b389;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.empty-icon {
  font-size: 80px;
  color: #a8d8ea;
}

.doctor-detail {
  margin-top: 20px;
  animation: fadeIn 0.6s ease-in-out;
}

.detail-header {
  margin-bottom: 20px;
}

.doctor-profile {
  text-align: center;
  padding: 30px 20px;
  background-color: #f9f7f2;
  border-radius: 10px;
  height: 100%;
}

.doctor-profile .doctor-avatar {
  margin-bottom: 15px;
  display: flex;
  justify-content: center;
}

.doctor-profile h2 {
  margin: 15px 0 10px;
  color: #3a5463;
}

.doctor-profile .department {
  margin: 10px 0;
  color: #5a7385;
  font-weight: 500;
}

.doctor-stats {
  display: flex;
  justify-content: center;
  margin: 20px 0;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  background-color: rgba(168, 216, 234, 0.2);
  padding: 8px 15px;
  border-radius: 8px;
}

.stat-item .el-icon {
  color: #76c893;
  font-size: 20px;
  margin-right: 8px;
}

.stat-info {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.stat-label {
  font-size: 12px;
  color: #5a7385;
}

.stat-value {
  font-weight: bold;
  color: #3a5463;
}

.custom-tabs :deep(.el-tabs__item) {
  color: #5a7385;
  font-size: 16px;
  font-weight: 500;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #76c893;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  background-color: #76c893;
}

.doctor-introduction {
  padding: 10px;
}

.info-section {
  margin-bottom: 25px;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.section-header .el-icon {
  color: #76c893;
  margin-right: 8px;
  font-size: 18px;
}

.section-header h4 {
  margin: 0;
  color: #3a5463;
  font-size: 18px;
  font-weight: 500;
}

.section-content {
  padding: 0 10px;
}

.section-content p {
  margin: 0;
  line-height: 1.8;
  color: #5a7385;
}

.schedule-legend {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f9f7f2;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-item span {
  color: #5a7385;
  font-size: 14px;
}

.schedule-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.schedule-card {
  border-radius: 8px;
  transition: all 0.3s ease;
  border: none;
}

.schedule-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.schedule-card.available {
  background-color: rgba(103, 194, 58, 0.05);
}

.schedule-card.almost-full {
  background-color: rgba(230, 162, 60, 0.05);
}

.schedule-card.full {
  background-color: rgba(245, 108, 108, 0.05);
}

.schedule-card.stopped {
  background-color: rgba(144, 147, 153, 0.05);
}

.schedule-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.schedule-date {
  font-weight: 500;
  color: #3a5463;
}

.schedule-time {
  background-color: #f0f9eb;
  color: #67c23a;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.schedule-card.almost-full .schedule-time {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.schedule-card.full .schedule-time {
  background-color: #fef0f0;
  color: #f56c6c;
}

.schedule-card.stopped .schedule-time {
  background-color: #f4f4f5;
  color: #909399;
}

.schedule-card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.schedule-status {
  display: flex;
  justify-content: center;
}

.schedule-count {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.count-text {
  text-align: center;
  font-size: 12px;
  color: #5a7385;
}

.schedule-action {
  display: flex;
  justify-content: center;
  margin-top: 5px;
}

.doctor-reviews {
  padding: 10px;
}

.review-stats {
  background-color: #f9f7f2;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

.rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.rating-score {
  font-size: 32px;
  font-weight: bold;
  color: #3a5463;
}

.rating-stars {
  display: flex;
}

.star-icon {
  color: #FFB800;
  font-size: 20px;
}

.rating-count {
  color: #5a7385;
  font-size: 14px;
}

.empty-reviews {
  display: flex;
  justify-content: center;
  padding: 30px 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style> 