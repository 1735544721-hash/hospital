<template>
  <div class="my-appointments-page">
    <div class="welcome-banner">
      <div class="banner-content">
        <h2>我的预约记录</h2>
        <p>在这里查看您的所有预约情况，我们随时为您提供贴心服务</p>
      </div>
    </div>

    <el-card class="appointments-card" shadow="hover">
      <!-- 筛选条件 -->
      <div class="filter-container">
        <div class="filter-header">
          <div class="filter-icon">
            <el-icon><Filter /></el-icon>
          </div>
          <h4>筛选预约</h4>
        </div>
        <el-radio-group v-model="statusFilter" @change="filterAppointments" size="large">
          <el-radio-button :label="null">全部预约</el-radio-button>
          <el-radio-button :label="1">待就诊</el-radio-button>
          <el-radio-button :label="2">已就诊</el-radio-button>
          <el-radio-button :label="0">已取消</el-radio-button>
        </el-radio-group>
        <el-button 
          type="primary" 
          class="new-appointment-btn"
          @click="goToAppointment" 
          round
        >
          <el-icon><Plus /></el-icon>新预约
        </el-button>
      </div>

      <!-- 预约列表 -->
      <div v-loading="loading" class="appointment-list">
        <el-empty 
          v-if="filteredAppointments.length === 0" 
          description="暂无预约记录"
          :image-size="120"
        >
          <template #image>
            <el-icon class="empty-icon"><Calendar /></el-icon>
          </template>
          <el-button type="primary" @click="goToAppointment" round>立即预约</el-button>
        </el-empty>
        
        <el-timeline v-else>
          <el-timeline-item
            v-for="appointment in filteredAppointments"
            :key="appointment.id"
            :type="getTimelineItemType(appointment.status)"
            :color="getTimelineItemColor(appointment.status)"
            :timestamp="formatDate(appointment.appointmentDate) + ' ' + appointment.timeSlot"
            placement="top"
          >
            <el-card class="appointment-card" shadow="hover">
              <div class="appointment-info">
                <div class="appointment-header">
                  <div class="appointment-title">
                    <el-icon><OfficeBuilding /></el-icon>
                    <h4>{{ appointment.doctor?.department?.deptName || '未知科室' }}</h4>
                    <el-divider direction="vertical" />
                    <el-icon><User /></el-icon>
                    <h4>{{ appointment.doctor?.name || '未知医生' }}</h4>
                    <el-tag 
                      :type="getStatusTagType(appointment.status)"
                      class="status-tag"
                      effect="light"
                      round
                    >
                      {{ getStatusText(appointment.status) }}
                    </el-tag>
                  </div>
                  <div class="appointment-no">
                    预约编号: {{ appointment.appointmentNo }}
                  </div>
                </div>
                
                <div class="appointment-content">
                  <div class="content-item">
                    <el-icon><Timer /></el-icon>
                    <span><strong>就诊时间:</strong> {{ formatDate(appointment.appointmentDate) }} {{ appointment.timeSlot }}</span>
                  </div>
                  <div class="content-item">
                    <el-icon><Medal /></el-icon>
                    <span><strong>医生职称:</strong> {{ appointment.doctor?.title || '普通医师' }}</span>
                  </div>
                  <div class="content-item">
                    <el-icon><ChatLineRound /></el-icon>
                    <span><strong>症状描述:</strong> {{ appointment.symptoms || '无' }}</span>
                  </div>
                </div>
                
                <div class="appointment-actions">
                  <el-button 
                    v-if="appointment.status === 1" 
                    type="danger" 
                    @click="handleCancelAppointment(appointment)"
                    round
                    plain
                  >
                    <el-icon><Close /></el-icon>取消预约
                  </el-button>
                  <el-button 
                    type="primary" 
                    @click="handleViewDetail(appointment)"
                    round
                  >
                    <el-icon><View /></el-icon>查看详情
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>

    <!-- 预约提示卡片 -->
    <el-card class="health-tips-card" shadow="hover">
      <div class="tips-header">
        <el-icon><InfoFilled /></el-icon>
        <h4>就诊小贴士</h4>
      </div>
      <ul class="tips-list">
        <li>请在预约时间前15分钟到达医院，做好就诊准备</li>
        <li>如需取消预约，请提前24小时操作，以便其他患者使用</li>
        <li>就诊时请携带有效证件和医保卡，方便医生查询您的病史</li>
        <li>如有疑问，可拨打咨询电话：0123-4567890</li>
      </ul>
    </el-card>

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="预约详情"
      width="600px"
      class="detail-dialog"
    >
      <div class="dialog-header">
        <div class="dialog-icon">
          <el-icon><Document /></el-icon>
        </div>
        <h3>预约信息详情</h3>
      </div>
      
      <el-divider />
      
      <el-descriptions :column="1" border class="detail-descriptions">
        <el-descriptions-item label="预约编号">
          <div class="description-content">{{ currentAppointment.appointmentNo }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="预约状态">
          <div class="description-content">
            <el-tag 
              :type="getStatusTagType(currentAppointment.status)"
              class="status-tag"
              effect="light"
              round
            >
              {{ getStatusText(currentAppointment.status) }}
            </el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="就诊日期">
          <div class="description-content">{{ formatDate(currentAppointment.appointmentDate) }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="就诊时间">
          <div class="description-content">{{ currentAppointment.timeSlot }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="科室">
          <div class="description-content">{{ currentAppointment.doctor?.department?.deptName || '未知科室' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="医生">
          <div class="description-content">
            {{ currentAppointment.doctor?.name || '未知' }} {{ currentAppointment.doctor?.title || '' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="症状描述">
          <div class="description-content">{{ currentAppointment.symptoms || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          <div class="description-content">{{ formatDateTime(currentAppointment.createTime) }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentAppointment.status !== 1" label="更新时间">
          <div class="description-content">{{ formatDateTime(currentAppointment.updateTime) }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="notice-box" v-if="currentAppointment.status === 1">
        <el-icon><Warning /></el-icon>
        <p>如需取消预约，请提前24小时操作，谢谢配合。</p>
      </div>
      
      <div v-if="currentAppointment.status === 1" class="dialog-footer">
        <el-button @click="detailDialogVisible = false" plain round>返回</el-button>
        <el-button type="danger" @click="handleCancelAppointment(currentAppointment, true)" round>
          取消预约
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Calendar, 
  User, 
  Filter, 
  OfficeBuilding, 
  Timer, 
  Medal, 
  ChatLineRound, 
  InfoFilled, 
  Document, 
  Warning, 
  Close, 
  View, 
  Plus 
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 数据
const appointments = ref([])
const loading = ref(false)
const statusFilter = ref(null)

// 详情对话框
const detailDialogVisible = ref(false)
const currentAppointment = reactive({})

// 计算过滤后的预约
const filteredAppointments = computed(() => {
  if (statusFilter.value === null) {
    return appointments.value
  }
  return appointments.value.filter(a => a.status === statusFilter.value)
})

// 初始化
onMounted(() => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  fetchAppointments()
})

// 获取预约列表
const fetchAppointments = async () => {
  loading.value = true
  try {
    await request.get('/appointment/my', {}, {
      onSuccess: (res) => {
        appointments.value = res || []
      }
    })
  } catch (error) {
    console.error('获取预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 筛选预约
const filterAppointments = () => {
  // 状态已经通过计算属性筛选了
}

// 查看预约详情
const handleViewDetail = (appointment) => {
  // 复制预约信息到当前预约
  Object.assign(currentAppointment, appointment)
  detailDialogVisible.value = true
}

// 取消预约
const handleCancelAppointment = (appointment, closeDialog = false) => {
  ElMessageBox.confirm(
    '确定要取消该预约吗？取消后将无法恢复',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await request.put(`/appointment/cancel/${appointment.id}`, {}, {
          successMsg: '预约已取消',
          onSuccess: () => {
            // 刷新预约列表
            fetchAppointments()
            
            // 关闭详情对话框
            if (closeDialog) {
              detailDialogVisible.value = false
            }
          }
        })
      } catch (error) {
        console.error('取消预约失败:', error)
      }
    })
    .catch(() => {})
}

// 前往预约页面
const goToAppointment = () => {
  router.push('/appointment')
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0:
      return '已取消'
    case 1:
      return '待就诊'
    case 2:
      return '已就诊'
    default:
      return '未知状态'
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 0:
      return 'info'
    case 1:
      return 'warning'
    case 2:
      return 'success'
    default:
      return 'info'
  }
}

// 获取时间线项目类型
const getTimelineItemType = (status) => {
  switch (status) {
    case 0:
      return 'info'
    case 1:
      return 'warning'
    case 2:
      return 'success'
    default:
      return 'info'
  }
}

// 获取时间线项目颜色
const getTimelineItemColor = (status) => {
  switch (status) {
    case 0:
      return '#909399'
    case 1:
      return '#E6A23C'
    case 2:
      return '#67C23A'
    default:
      return '#909399'
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}年${month}月${day}日`
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}年${month}月${day}日 ${hour}:${minute}:${second}`
}
</script>

<style scoped>
.my-appointments-page {
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

.appointments-card, .health-tips-card {
  border-radius: 15px;
  margin-bottom: 20px;
  border: none;
  background-color: #ffffff;
}

.filter-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-header {
  display: flex;
  align-items: center;
  margin-right: 15px;
}

.filter-icon {
  background-color: #c4e3b2;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

.filter-icon .el-icon {
  color: #ffffff;
  font-size: 18px;
}

.filter-header h4 {
  margin: 0;
  color: #3a5463;
  font-size: 16px;
  font-weight: 600;
}

.new-appointment-btn {
  background-color: #c4e3b2;
  border-color: #c4e3b2;
  color: #ffffff;
}

.new-appointment-btn:hover {
  background-color: #b3d8a1;
  border-color: #b3d8a1;
}

.appointment-list {
  margin-top: 20px;
}

.empty-icon {
  font-size: 80px;
  color: #a8d8ea;
  margin-bottom: 20px;
}

.appointment-card {
  margin-bottom: 15px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.appointment-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.appointment-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0eada;
  padding-bottom: 15px;
}

.appointment-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.appointment-title .el-icon {
  color: #a8d8ea;
  font-size: 18px;
}

.appointment-title h4 {
  margin: 0;
  color: #3a5463;
  font-weight: 600;
}

.status-tag {
  margin-left: 10px;
  padding: 0 12px;
  height: 24px;
  line-height: 24px;
  border-radius: 12px;
}

.appointment-no {
  font-size: 14px;
  color: #8a9ca7;
}

.appointment-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 5px 0;
}

.content-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #5c7b8a;
  font-size: 14px;
}

.content-item .el-icon {
  color: #a8d8ea;
  font-size: 16px;
}

.appointment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0eada;
}

.health-tips-card {
  padding: 15px 20px;
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
  font-weight: 600;
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

.detail-dialog {
  border-radius: 15px;
}

.dialog-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.dialog-icon {
  background-color: #a8d8ea;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.dialog-icon .el-icon {
  color: #ffffff;
  font-size: 20px;
}

.dialog-header h3 {
  margin: 0;
  color: #3a5463;
  font-weight: 600;
}

.detail-descriptions {
  margin-bottom: 20px;
}

.description-content {
  padding: 5px 0;
  color: #5c7b8a;
}

.notice-box {
  display: flex;
  align-items: flex-start;
  background-color: #fff8e6;
  padding: 12px 15px;
  border-radius: 10px;
  margin: 20px 0;
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
  margin-top: 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .welcome-banner {
    padding: 20px;
  }
  
  .banner-content h2 {
    font-size: 22px;
  }
  
  .filter-container {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .appointment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .appointment-no {
    margin-top: 5px;
  }
}
</style>