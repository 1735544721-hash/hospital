<template>
  <div class="medical-record-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>就诊记录管理</h3>
          <el-button type="primary" @click="handleAdd">新增就诊记录</el-button>
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
        <el-form-item label="就诊日期">
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 就诊记录列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="recordNo" label="记录编号" width="180" />
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
        <el-table-column prop="recordDate" label="就诊日期" width="120" sortable />
        <el-table-column prop="diagnosis" label="诊断结果" width="200">
          <template #default="scope">
            <el-tooltip
              v-if="scope.row.diagnosis"
              :content="scope.row.diagnosis"
              placement="top"
              effect="light"
            >
              <div class="ellipsis">{{ scope.row.diagnosis }}</div>
            </el-tooltip>
            <span v-else>暂无诊断</span>
          </template>
        </el-table-column>
        <el-table-column prop="followUp" label="随访日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" sortable />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button type="success" size="small" @click="handlePrescription(scope.row)">处方</el-button>
            <el-button type="warning" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确定删除此就诊记录吗？"
              @confirm="handleDelete(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
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

    <!-- 新增/编辑就诊记录对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增就诊记录' : dialogType === 'edit' ? '编辑就诊记录' : '查看就诊记录'"
      width="650px"
      @closed="resetForm"
    >
      <el-form
        ref="recordFormRef"
        :model="recordForm"
        :rules="recordFormRules"
        label-width="100px"
        :disabled="dialogType === 'view'"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者" prop="patientId">
              <el-select v-model="recordForm.patientId" placeholder="请选择患者" filterable style="width: 100%">
                <el-option
                  v-for="patient in patientOptions"
                  :key="patient.id"
                  :label="patient.name"
                  :value="patient.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生" prop="doctorId">
              <el-select v-model="recordForm.doctorId" placeholder="请选择医生" filterable style="width: 100%" :disabled="isDoctor">
                <el-option
                  v-for="doctor in doctorOptions"
                  :key="doctor.id"
                  :label="`${doctor.name} (${doctor.department?.deptName || ''})`"
                  :value="doctor.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="关联预约">
              <div style="display: flex; gap: 10px; width: 100%;">
                <el-select v-model="recordForm.appointmentId" placeholder="请选择预约" filterable clearable style="flex: 1;">
                  <el-option
                    v-for="appointment in appointmentOptions"
                    :key="appointment.id"
                    :label="`${appointment.appointmentNo} (${appointment.appointmentDate} ${appointment.timeSlot})`"
                    :value="appointment.id"
                  />
                </el-select>
                <el-button type="success" @click="fetchAppointments(true)" :loading="loadingAppointments">
                  <el-icon><Refresh /></el-icon>
                  刷新预约列表
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="就诊日期" prop="recordDate">
              <el-date-picker
                v-model="recordForm.recordDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="诊断结果" prop="diagnosis">
          <el-input
            v-model="recordForm.diagnosis"
            type="textarea"
            :rows="3"
            placeholder="请输入诊断结果"
          />
        </el-form-item>
        
        <el-form-item label="治疗方案" prop="treatment">
          <el-input
            v-model="recordForm.treatment"
            type="textarea"
            :rows="3"
            placeholder="请输入治疗方案"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="随访日期">
              <el-date-picker
                v-model="recordForm.followUp"
                type="date"
                placeholder="选择随访日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="医生备注">
          <el-input
            v-model="recordForm.notes"
            type="textarea"
            :rows="2"
            placeholder="请输入医生备注"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button v-if="dialogType !== 'view'" type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 处方管理对话框 -->
    <el-dialog
      v-model="prescriptionDialogVisible"
      title="处方管理"
      width="800px"
    >
      <div v-if="currentRecord.id" class="prescription-container">
        <div class="record-info">
          <h4>就诊信息</h4>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="患者">{{ currentRecord.patient?.name }}</el-descriptions-item>
            <el-descriptions-item label="医生">{{ currentRecord.doctor?.name }}</el-descriptions-item>
            <el-descriptions-item label="就诊日期">{{ currentRecord.recordDate }}</el-descriptions-item>
            <el-descriptions-item label="诊断结果" :span="3">{{ currentRecord.diagnosis || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="prescription-list">
          <div class="section-header">
            <h4>处方列表</h4>
            <el-button type="primary" size="small" @click="handleAddPrescription">新增处方</el-button>
          </div>
          
          <el-table v-loading="prescriptionLoading" :data="prescriptionList" border style="width: 100%">
            <el-table-column prop="prescriptionNo" label="处方编号" width="180" />
            <el-table-column prop="prescriptionDate" label="处方日期" width="120" />
            <el-table-column prop="diagnosis" label="诊断" width="200">
              <template #default="scope">
                <el-tooltip
                  v-if="scope.row.diagnosis"
                  :content="scope.row.diagnosis"
                  placement="top"
                  effect="light"
                >
                  <div class="ellipsis">{{ scope.row.diagnosis }}</div>
                </el-tooltip>
                <span v-else>同就诊记录</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                  {{ scope.row.status === 1 ? '已取药' : '未取药' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewPrescription(scope.row)">查看</el-button>
                <el-button 
                  v-if="scope.row.status === 0" 
                  type="success" 
                  size="small" 
                  @click="handleUpdatePrescriptionStatus(scope.row.id, 1)"
                >
                  标记已取药
                </el-button>
                <el-button 
                  v-else
                  type="warning" 
                  size="small" 
                  @click="handleUpdatePrescriptionStatus(scope.row.id, 0)"
                >
                  标记未取药
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

// 获取用户信息
const userStore = useUserStore()
const router = useRouter()
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
  doctorName: ''
})

// 日期范围
const dateRange = ref([])

// 患者和医生选项
const patientOptions = ref([])
const doctorOptions = ref([])
const appointmentOptions = ref([])

// 加载预约的loading状态
const loadingAppointments = ref(false)
// 标志：是否应该自动加载预约（用于避免编辑时重复加载）
const shouldAutoLoadAppointments = ref(true)

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogType = ref('add') // add, edit, view
const recordFormRef = ref(null)
const recordForm = reactive({
  id: null,
  patientId: null,
  doctorId: null,
  appointmentId: null,
  recordNo: '',
  diagnosis: '',
  treatment: '',
  recordDate: '',
  notes: '',
  followUp: null
})

// 表单验证规则
const recordFormRules = {
  patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择就诊日期', trigger: 'change' }],
  diagnosis: [{ required: true, message: '请输入诊断结果', trigger: 'blur' }]
}

// 处方相关
const prescriptionDialogVisible = ref(false)
const prescriptionLoading = ref(false)
const prescriptionList = ref([])
const currentRecord = reactive({})

// 初始化
onMounted(() => {
  fetchPatients()
  fetchDoctors()
  fetchMedicalRecords()
})

// 获取患者列表
const fetchPatients = async () => {
  try {
    await request.get('/patient/list', {}, {
      onSuccess: (res) => {
        patientOptions.value = res || []
      }
    })
  } catch (error) {
    console.error('获取患者列表失败:', error)
  }
}

// 获取医生列表
const fetchDoctors = async () => {
  try {
    // 如果是医生，只需要获取自己的信息
    if (isDoctor.value && doctorId.value) {
      doctorOptions.value = [userStore.doctorInfo]
    } else {
      await request.get('/doctor/list', {}, {
        onSuccess: (res) => {
          doctorOptions.value = res || []
        }
      })
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
  }
}

// 获取就诊记录列表
const fetchMedicalRecords = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value
    }

    // 如果是医生，只查询自己的就诊记录
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

    await request.get('/medical-record/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取就诊记录列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchMedicalRecords()
}

// 重置搜索
const resetSearch = () => {
  searchForm.patientName = ''
  searchForm.doctorName = ''
  dateRange.value = []
  currentPage.value = 1
  fetchMedicalRecords()
}

// 新增就诊记录
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  
  // 设置默认值
  recordForm.recordDate = formatDate(new Date())
  
  // 如果是医生，自动设置医生ID
  if (isDoctor.value && doctorId.value) {
    recordForm.doctorId = doctorId.value
  }
}

// 编辑就诊记录
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  
  // 禁用自动加载，避免触发 watch
  shouldAutoLoadAppointments.value = false
  
  Object.keys(recordForm).forEach(key => {
    recordForm[key] = row[key]
  })
  
  dialogVisible.value = true
  
  // 如果有关联的预约，加载预约详情进行回显
  if (recordForm.appointmentId) {
    await loadAppointmentDetail(recordForm.appointmentId)
  }
  
  // 重新启用自动加载
  shouldAutoLoadAppointments.value = true
}

// 查看就诊记录
const handleView = async (row) => {
  dialogType.value = 'view'
  
  // 禁用自动加载，避免触发 watch
  shouldAutoLoadAppointments.value = false
  
  Object.keys(recordForm).forEach(key => {
    recordForm[key] = row[key]
  })
  
  dialogVisible.value = true
  
  // 如果有关联的预约，加载预约详情进行回显
  if (recordForm.appointmentId) {
    await loadAppointmentDetail(recordForm.appointmentId)
  }
  
  // 重新启用自动加载
  shouldAutoLoadAppointments.value = true
}

// 处理处方
const handlePrescription = async (row) => {
  // 保存当前就诊记录
  Object.assign(currentRecord, row)
  
  // 显示处方对话框
  prescriptionDialogVisible.value = true
  
  // 加载处方列表
  await fetchPrescriptions(row.id)
}

// 获取就诊记录关联的处方列表
const fetchPrescriptions = async (recordId) => {
  prescriptionLoading.value = true
  try {
    await request.get(`/prescription/record/${recordId}`, {}, {
      onSuccess: (res) => {
        prescriptionList.value = res || []
      }
    })
  } catch (error) {
    console.error('获取处方列表失败:', error)
  } finally {
    prescriptionLoading.value = false
  }
}

// 新增处方
const handleAddPrescription = () => {
  // 关闭处方列表对话框
  prescriptionDialogVisible.value = false
  
  // 跳转到处方管理页面
  router.push({
    name: 'PrescriptionManagement',
    query: {
      recordId: currentRecord.id,
      patientId: currentRecord.patientId,
      doctorId: currentRecord.doctorId,
      action: 'add'
    }
  })
}

// 查看处方详情
const handleViewPrescription = (prescription) => {
  // TODO: 跳转到处方详情页面
  ElMessageBox.alert(
    `处方编号: ${prescription.prescriptionNo}<br>
     处方日期: ${prescription.prescriptionDate}<br>
     诊断: ${prescription.diagnosis || '同就诊记录'}<br>
     状态: ${prescription.status === 1 ? '已取药' : '未取药'}<br>
     药品数量: ${prescription.details?.length || 0}种`,
    '处方详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

// 更新处方状态
const handleUpdatePrescriptionStatus = async (id, status) => {
  try {
    await request.put(`/prescription/status/${id}`, {
      status
    }, {
      successMsg: status === 1 ? '已标记为已取药' : '已标记为未取药',
      onSuccess: () => {
        // 刷新处方列表
        fetchPrescriptions(currentRecord.id)
      }
    })
  } catch (error) {
    console.error('更新处方状态失败:', error)
  }
}

// 删除就诊记录
const handleDelete = async (id) => {
  try {
    await request.delete(`/medical-record/${id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchMedicalRecords()
      }
    })
  } catch (error) {
    console.error('删除就诊记录失败:', error)
  }
}

// 提交表单
const submitForm = () => {
  recordFormRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        // 新增就诊记录
        await request.post('/medical-record', recordForm, {
          successMsg: '新增就诊记录成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchMedicalRecords()
          }
        })
      } else {
        // 编辑就诊记录
        await request.put(`/medical-record/${recordForm.id}`, recordForm, {
          successMsg: '编辑就诊记录成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchMedicalRecords()
          }
        })
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (recordFormRef.value) {
    recordFormRef.value.resetFields()
  }
  Object.keys(recordForm).forEach(key => {
    if (key === 'doctorId' && isDoctor.value && doctorId.value) {
      recordForm[key] = doctorId.value
    } else {
      recordForm[key] = key === 'id' ? null : null
    }
  })
  
  // 清空预约选项
  appointmentOptions.value = []
}

// 加载预约详情用于回显
const loadAppointmentDetail = async (appointmentId) => {
  try {
    await request.get(`/appointment/${appointmentId}`, {}, {
      onSuccess: (res) => {
        // 将预约详情添加到选项列表中，用于回显
        if (res) {
          appointmentOptions.value = [res]
        }
      }
    })
  } catch (error) {
    console.error('获取预约详情失败:', error)
  }
}

// 当患者或医生选择变化时，自动加载未取消的预约
const fetchAppointments = async (showMessage = true, onlyShowEmptyMessage = false) => {
  if (!recordForm.patientId || !recordForm.doctorId) {
    appointmentOptions.value = []
    return
  }
  
  loadingAppointments.value = true
  try {
    const params = {
      patientId: recordForm.patientId,
      doctorId: recordForm.doctorId
    }
    
    await request.get('/appointment/get-no-cancel', params, {
      onSuccess: (res) => {
        // 确保结果是数组，如果是 null 或 undefined 则设为空数组
        const appointments = Array.isArray(res) ? res : []
        appointmentOptions.value = appointments
        
        if (showMessage) {
          if (appointments.length > 0) {
            // 如果只显示空消息模式，有记录时不显示
            if (!onlyShowEmptyMessage) {
              ElMessage.success(`加载到 ${appointments.length} 条预约记录`)
            }
          } else {
            ElMessage.info('该患者和医生暂无预约记录')
          }
        }
      },
      onError: () => {
        // 请求失败时清空预约选项
        appointmentOptions.value = []
      }
    })
  } catch (error) {
    console.error('获取预约列表失败:', error)
    if (showMessage) {
      ElMessage.error('加载预约失败')
    }
    appointmentOptions.value = []
  } finally {
    loadingAppointments.value = false
  }
}

// 监听患者和医生选择变化，自动加载预约
watch(
  () => [recordForm.patientId, recordForm.doctorId],
  ([newPatientId, newDoctorId], [oldPatientId, oldDoctorId]) => {
    // 如果禁用了自动加载，则跳过
    if (!shouldAutoLoadAppointments.value) {
      return
    }
    
    // 当患者或医生发生变化时
    if (newPatientId !== oldPatientId || newDoctorId !== oldDoctorId) {
      // 先清空预约选项和已选择的预约
      appointmentOptions.value = []
      recordForm.appointmentId = null
      
      // 如果患者和医生都已选择，则加载预约（只在没有记录时显示消息）
      if (newPatientId && newDoctorId) {
        fetchAppointments(true, true)
      }
    }
  }
)

// 格式化日期
const formatDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 分页操作
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchMedicalRecords()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchMedicalRecords()
}
</script>

<style scoped>
.medical-record-management {
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

.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.prescription-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.record-info {
  margin-bottom: 10px;
}
</style> 