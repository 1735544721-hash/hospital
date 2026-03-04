<template>
  <div class="department-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h2><el-icon><OfficeBuilding /></el-icon> 科室介绍</h2>
        <p>了解我院各科室专业特色，为您提供专业的医疗服务</p>
      </div>
        </div>

    <el-card class="box-card" shadow="hover">
      <div class="department-container">
        <el-row :gutter="30">
          <!-- 左侧科室列表 -->
          <el-col :span="6">
            <div class="department-list">
              <div class="list-header">
                <el-icon><Menu /></el-icon>
              <h4>科室列表</h4>
              </div>
              <el-menu
                :default-active="activeIndex.toString()"
                class="department-menu"
                @select="handleSelect"
              >
                <el-menu-item v-for="dept in departments" :key="dept.id" :index="dept.id.toString()">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>{{ dept.deptName }}</span>
                </el-menu-item>
              </el-menu>
            </div>
          </el-col>

          <!-- 右侧科室详情 -->
          <el-col :span="18">
            <div v-if="currentDepartment" class="department-detail">
              <div class="department-header">
                <div class="header-icon">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
                <div class="header-content">
                <h2>{{ currentDepartment.deptName }}</h2>
                  <el-tag size="small" effect="plain" class="dept-code">{{ currentDepartment.deptCode }}</el-tag>
                </div>
              </div>

              <div class="department-description info-card">
                <div class="info-card-header">
                  <el-icon><InfoFilled /></el-icon>
                <h4>科室简介</h4>
                </div>
                <div class="info-card-content">
                <p>{{ currentDepartment.description || '暂无简介' }}</p>
                </div>
              </div>

              <div class="department-director info-card" v-if="currentDepartment.director">
                <div class="info-card-header">
                  <el-icon><UserFilled /></el-icon>
                <h4>科室主任</h4>
                </div>
                <div class="info-card-content">
                  <div class="director-info">
                    <div class="director-avatar">
                      <el-avatar :size="80" :src="getAvatarUrl(currentDepartment.director.user?.avatar)" />
                    </div>
                    <div class="director-details">
                      <h3>{{ currentDepartment.director.name }}
                        <el-tag size="small" type="info">{{ currentDepartment.director.title }}</el-tag>
                      </h3>
                      <p class="expertise">{{ currentDepartment.director.expertise || '暂无专长信息' }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div class="department-doctors info-card">
                <div class="info-card-header">
                  <el-icon><User /></el-icon>
                  <h4>科室医生</h4>
                  <el-button type="primary" size="small" round @click="goToAppointment" v-if="isLoggedIn">
                    <el-icon><Calendar /></el-icon>
                    预约挂号
                  </el-button>
                </div>

                <div class="info-card-content">
                  <div v-if="doctors.length > 0" class="doctor-grid">
                    <el-card v-for="doctor in doctors" :key="doctor.id" class="doctor-item" shadow="hover" @click="viewDoctorDetail(doctor)">
                      <div class="doctor-item-content">
                        <el-avatar :size="60" :src="getAvatarUrl(doctor.user?.avatar)" />
                        <h3>{{ doctor.name }}</h3>
                        <el-tag size="small" type="info">{{ doctor.title }}</el-tag>
                        <p class="expertise" v-if="doctor.expertise">{{ doctor.expertise }}</p>
                        <div class="doctor-actions">
                          <el-button type="primary" size="small" round @click.stop="goToAppointmentWithDoctor(doctor)" v-if="isLoggedIn">
                            <el-icon><Calendar /></el-icon>
                            预约挂号
                          </el-button>
                          <el-button type="info" size="small" round @click.stop="viewDoctorDetail(doctor)">
                            <el-icon><View /></el-icon>
                        查看详情
                      </el-button>
                        </div>
                      </div>
                    </el-card>
                  </div>
                <el-empty v-else description="暂无医生信息" />
                </div>
              </div>
            </div>
            <el-empty v-else description="请选择科室查看详情" :image-size="200">
              <template #image>
                <el-icon class="empty-icon"><OfficeBuilding /></el-icon>
              </template>
            </el-empty>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { 
  OfficeBuilding, 
  InfoFilled, 
  UserFilled, 
  User, 
  Menu, 
  Calendar,
  View
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 用户是否登录
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 科室列表
const departments = ref([])
const activeIndex = ref('1')
const currentDepartment = ref(null)
const doctors = ref([])

// 获取头像URL
const baseAPI = import.meta.env.VITE_API_BASE_URL || '/api'
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return baseAPI + avatar
}

// 获取科室列表
const fetchDepartments = async () => {
  try {
    await request.get('/department/list', {}, {
      onSuccess: (res) => {
        departments.value = res || []
        if (departments.value.length > 0) {
          activeIndex.value = departments.value[0].id.toString()
          handleSelect(activeIndex.value)
        }
      }
    })
  } catch (error) {
    console.error('获取科室列表失败:', error)
  }
}

// 获取科室详情
const fetchDepartmentDetail = async (id) => {
  try {
    await request.get(`/department/${id}`, {}, {
      onSuccess: async (res) => {
        currentDepartment.value = res
        
        // 如果有科室主任，获取主任信息
        if (currentDepartment.value.directorId) {
          await fetchDoctorDetail(currentDepartment.value.directorId)
        }
        
        // 获取科室医生
        await fetchDepartmentDoctors(id)
      }
    })
  } catch (error) {
    console.error('获取科室详情失败:', error)
  }
}

// 获取医生详情
const fetchDoctorDetail = async (id) => {
  try {
    await request.get(`/doctor/${id}`, {}, {
      onSuccess: (res) => {
        currentDepartment.value.director = res
      }
    })
  } catch (error) {
    console.error('获取医生详情失败:', error)
  }
}

// 获取科室医生列表
const fetchDepartmentDoctors = async (deptId) => {
  try {
    await request.get(`/doctor/department/${deptId}`, {}, {
      onSuccess: (res) => {
        doctors.value = res || []
      }
    })
  } catch (error) {
    console.error('获取科室医生列表失败:', error)
  }
}

// 选择科室
const handleSelect = (index) => {
  activeIndex.value = index
  fetchDepartmentDetail(index)
}

// 查看医生详情
const viewDoctorDetail = (doctor) => {
  router.push(`/doctor?id=${doctor.id}`)
}

// 前往预约页面
const goToAppointment = () => {
  router.push({
    path: '/appointment',
    query: {
      departmentId: currentDepartment.value.id
    }
  })
}

// 带医生信息前往预约页面
const goToAppointmentWithDoctor = (doctor) => {
  router.push({
    path: '/appointment',
    query: {
      departmentId: currentDepartment.value.id,
      doctorId: doctor.id
    }
  })
}

onMounted(() => {
  fetchDepartments()
})
</script>

<style scoped>
.department-page {
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

.department-container {
  margin-top: 10px;
}

.department-list {
  border-right: none;
  height: 100%;
  background-color: #f9f7f2;
  border-radius: 10px;
  padding: 10px 0;
}

.list-header {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  margin-bottom: 10px;
  border-bottom: 1px solid rgba(118, 200, 147, 0.2);
}

.list-header .el-icon {
  color: #76c893;
  margin-right: 8px;
  font-size: 18px;
}

.list-header h4 {
  margin: 0;
  color: #3a5463;
  font-weight: 600;
}

.department-menu {
  border-right: none;
}

.department-menu :deep(.el-menu-item) {
  padding: 0 20px;
  height: 50px;
  line-height: 50px;
  font-size: 15px;
  color: #5a7385;
}

.department-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(118, 200, 147, 0.1);
  color: #76c893;
  font-weight: 500;
}

.department-menu :deep(.el-menu-item:hover) {
  background-color: rgba(118, 200, 147, 0.05);
}

.department-menu :deep(.el-menu-item .el-icon) {
  color: #a8d8ea;
  margin-right: 8px;
}

.department-detail {
  padding: 0 10px;
  animation: fadeIn 0.6s ease-in-out;
}

.department-header {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.header-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #a8d8ea;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.header-content {
  flex: 1;
}

.header-content h2 {
  margin: 0 0 5px 0;
  color: #3a5463;
  font-size: 22px;
}

.dept-code {
  background-color: #f7fbff;
  color: #5a7385;
  border-color: #a8d8ea;
}

.info-card {
  background-color: #ffffff;
  border-radius: 10px;
  padding: 0;
  margin-bottom: 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.info-card-header {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: #f7fbff;
  border-bottom: 1px solid #ebeef5;
}

.info-card-header .el-icon {
  color: #76c893;
  margin-right: 8px;
  font-size: 18px;
}

.info-card-header h4 {
  margin: 0;
  color: #3a5463;
  font-size: 16px;
  font-weight: 500;
  flex: 1;
}

.info-card-content {
  padding: 20px;
}

.info-card-content p {
  margin: 0;
  line-height: 1.6;
  color: #5a7385;
}

.director-info {
  display: flex;
  align-items: center;
}

.director-avatar {
  margin-right: 20px;
}

.director-details {
  flex: 1;
}

.director-details h3 {
  margin: 0 0 10px 0;
  color: #3a5463;
  display: flex;
  align-items: center;
  gap: 10px;
}

.expertise {
  color: #5a7385;
  margin: 0;
}

.doctor-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.doctor-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  border-radius: 10px;
}

.doctor-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.doctor-item-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 10px;
}

.doctor-item-content h3 {
  margin: 10px 0 5px;
  color: #3a5463;
}

.doctor-item-content .expertise {
  margin: 10px 0;
  font-size: 13px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.doctor-item-content .el-button {
  margin-top: 10px;
  background-color: #76c893;
  border-color: #76c893;
}

.empty-icon {
  font-size: 80px;
  color: #a8d8ea;
}

.doctor-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
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