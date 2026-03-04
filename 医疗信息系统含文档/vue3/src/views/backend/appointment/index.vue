<template>
  <div class="appointment-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>预约管理</h3>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="患者姓名" v-if="!isDoctor">
          <el-input v-model="searchForm.patientName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="医生姓名" v-if="!isDoctor">
          <el-input v-model="searchForm.doctorName" placeholder="请输入医生姓名" clearable />
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待就诊" :value="1" />
            <el-option label="已就诊" :value="2" />
            <el-option label="已取消" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 预约列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="appointmentNo" label="预约编号" width="180" />
        <el-table-column label="患者信息" width="150">
          <template #default="scope">
            {{ scope.row.patient?.name || '未知' }}
            <el-tag size="small" type="info">{{ scope.row.patient?.sex || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="医生信息" width="150" v-if="!isDoctor">
          <template #default="scope">
            {{ scope.row.doctor?.name || '未知' }}
            <el-tag size="small">{{ scope.row.doctor?.title || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="科室" width="120">
          <template #default="scope">
            {{ scope.row.doctor?.department?.deptName || '未知科室' }}
          </template>
        </el-table-column>
        <el-table-column prop="appointmentDate" label="预约日期" width="120" sortable />
        <el-table-column prop="timeSlot" label="时间段" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" sortable />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.status === 1" 
              type="success" 
              size="small" 
              @click="handleComplete(scope.row)"
            >
              完成就诊
            </el-button>
            <el-button 
              v-if="scope.row.status === 1" 
              type="danger" 
              size="small" 
              @click="handleCancel(scope.row)"
            >
              取消预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="预约详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预约编号">{{ currentAppointment.appointmentNo }}</el-descriptions-item>
        <el-descriptions-item label="预约状态">
          <el-tag :type="getStatusTagType(currentAppointment.status)">
            {{ getStatusText(currentAppointment.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ currentAppointment.appointmentDate }}</el-descriptions-item>
        <el-descriptions-item label="就诊时间">{{ currentAppointment.timeSlot }}</el-descriptions-item>
        <el-descriptions-item label="患者信息">
          {{ currentAppointment.patient?.name || '未知' }}，
          {{ currentAppointment.patient?.sex || '未知' }}，
          {{ currentAppointment.patient?.age || '未知' }}岁，
          {{ currentAppointment.patient?.phone || '未知' }}
        </el-descriptions-item>
        <el-descriptions-item label="科室">{{ currentAppointment.doctor?.department?.deptName || '未知科室' }}</el-descriptions-item>
        <el-descriptions-item label="医生" v-if="!isDoctor">
          {{ currentAppointment.doctor?.name || '未知' }} {{ currentAppointment.doctor?.title || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="症状描述">{{ currentAppointment.symptoms || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentAppointment.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentAppointment.status !== 1" label="更新时间">
          {{ currentAppointment.updateTime }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div v-if="currentAppointment.status === 1" class="dialog-footer">
        <el-button type="success" @click="handleComplete(currentAppointment, true)">
          完成就诊
        </el-button>
        <el-button type="danger" @click="handleCancel(currentAppointment, true)">
          取消预约
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

// 获取用户信息
const userStore = useUserStore()
const isDoctor = computed(() => userStore.isDoctor)
const doctorId = computed(() => userStore.doctorId)

// 列表数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  patientName: '',
  doctorName: '',
  status: null
})

// 日期范围
const dateRange = ref([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentAppointment = reactive({})

// 初始化
onMounted(() => {
  // 如果是医生，自动设置doctorId
  fetchAppointments()
})

// 获取预约列表
const fetchAppointments = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      status: searchForm.status
    }

    // 如果是医生，只查询自己的预约
    if (isDoctor.value && doctorId.value) {
      params.doctorId = doctorId.value
    } else {
      // 添加患者姓名和医生姓名
      if (searchForm.patientName) {
        params.patientName = searchForm.patientName
      }
      if (searchForm.doctorName) {
        params.doctorName = searchForm.doctorName
      }
    }

    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    await request.get('/appointment/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchAppointments()
}

// 重置搜索
const resetSearch = () => {
  searchForm.patientName = ''
  searchForm.doctorName = ''
  searchForm.status = null
  dateRange.value = []
  currentPage.value = 1
  fetchAppointments()
}

// 查看详情
const handleViewDetail = (row) => {
  Object.assign(currentAppointment, row)
  detailDialogVisible.value = true
}

// 完成就诊
const handleComplete = (appointment, closeDialog = false) => {
  ElMessageBox.confirm(
    '确定将此预约标记为已就诊吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await request.put(`/appointment/complete/${appointment.id}`, {}, {
          successMsg: '已完成就诊',
          onSuccess: () => {
            fetchAppointments()
            if (closeDialog) {
              detailDialogVisible.value = false
            }
          }
        })
      } catch (error) {
        console.error('完成就诊失败:', error)
      }
    })
    .catch(() => {})
}

// 取消预约
const handleCancel = (appointment, closeDialog = false) => {
  ElMessageBox.confirm(
    '确定要取消该预约吗？',
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
            fetchAppointments()
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

// 分页操作
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAppointments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAppointments()
}
</script>

<style scoped>
.appointment-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  margin-top: 20px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 