<template>
  <div class="prescription-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>处方管理</h3>
          <el-button type="primary" @click="handleAdd">新增处方</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="处方编号">
          <el-input v-model="searchForm.prescriptionNo" placeholder="请输入处方编号" clearable />
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.patientName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="医生姓名" v-if="!isDoctor">
          <el-input v-model="searchForm.doctorName" placeholder="请输入医生姓名" clearable />
        </el-form-item>
        <el-form-item label="处方日期">
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
            <el-option label="未取药" :value="0" />
            <el-option label="已取药" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 处方列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="prescriptionNo" label="处方编号" width="180" />
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
        <el-table-column label="就诊记录" width="180">
          <template #default="scope">
            <el-tooltip
              v-if="scope.row.medicalRecord"
              :content="`诊断: ${scope.row.medicalRecord.diagnosis || '无'}`"
              placement="top"
              effect="light"
            >
              <div>{{ scope.row.medicalRecord?.recordNo || '未知' }}</div>
              <div class="text-muted">{{ scope.row.medicalRecord?.recordDate || '' }}</div>
            </el-tooltip>
            <span v-else>未关联记录</span>
          </template>
        </el-table-column>
        <el-table-column prop="prescriptionDate" label="处方日期" width="120" sortable />
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
        <el-table-column prop="createTime" label="创建时间"  sortable />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button 
              v-if="scope.row.status === 0" 
              type="success" 
              size="small" 
              @click="handleUpdateStatus(scope.row.id, 1)"
            >
              标记已取药
            </el-button>
            <el-button 
              v-else
              type="warning" 
              size="small" 
              @click="handleUpdateStatus(scope.row.id, 0)"
            >
              标记未取药
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

    <!-- 处方详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增处方' : dialogType === 'edit' ? '编辑处方' : '处方详情'"
      width="800px"
      @closed="resetForm"
    >
      <el-form
        ref="prescriptionFormRef"
        :model="prescriptionForm"
        :rules="prescriptionFormRules"
        label-width="100px"
        :disabled="dialogType === 'view'"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者" prop="patientId">
              <el-select v-model="prescriptionForm.patientId" placeholder="请选择患者" filterable style="width: 100%">
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
              <el-select v-model="prescriptionForm.doctorId" placeholder="请选择医生" filterable style="width: 100%" :disabled="isDoctor">
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
          <el-col :span="12">
            <el-form-item label="就诊记录" prop="recordId">
              <el-select v-model="prescriptionForm.recordId" placeholder="请选择就诊记录" filterable style="width: 100%">
                <el-option
                  v-for="record in recordOptions"
                  :key="record.id"
                  :label="`${record.recordNo} (${record.recordDate})`"
                  :value="record.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处方日期" prop="prescriptionDate">
              <el-date-picker
                v-model="prescriptionForm.prescriptionDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="诊断结果">
          <el-input
            v-model="prescriptionForm.diagnosis"
            type="textarea"
            :rows="2"
            placeholder="请输入诊断结果（可选，默认使用就诊记录的诊断）"
          />
        </el-form-item>
        
        <el-form-item label="处方备注">
          <el-input
            v-model="prescriptionForm.notes"
            type="textarea"
            :rows="2"
            placeholder="请输入处方备注"
          />
        </el-form-item>
        
        <el-divider>药品明细</el-divider>
        
        <div class="medicine-details">
          <div v-if="prescriptionForm.details && prescriptionForm.details.length > 0">
            <el-table :data="prescriptionForm.details" border style="width: 100%; margin-bottom: 20px;">
              <el-table-column label="药品名称" prop="medicine.medicineName" width="180" />
              <el-table-column label="规格" prop="medicine.specification" width="120" />
              <el-table-column label="用量" prop="dosage" width="100" />
              <el-table-column label="频次" prop="frequency" width="100" />
              <el-table-column label="用药天数" prop="days" width="100" />
              <el-table-column label="用法" prop="usage" width="120" />
              <el-table-column label="数量" prop="quantity" width="80" />
              <el-table-column label="操作" width="120" v-if="dialogType !== 'view'">
                <template #default="scope">
                  <el-button type="danger" size="small" @click="removeMedicine(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-else class="no-medicine">
            <el-empty description="暂无药品信息" :image-size="60" />
          </div>
          
          <div v-if="dialogType !== 'view'" class="add-medicine-form">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="药品" prop="currentMedicine.medicineId">
                  <el-select v-model="currentMedicine.medicineId" placeholder="请选择药品" filterable>
                    <el-option
                      v-for="medicine in medicineOptions"
                      :key="medicine.id"
                      :label="`${medicine.medicineName} (${medicine.specification || ''})`"
                      :value="medicine.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="用量" prop="currentMedicine.dosage">
                  <el-input v-model="currentMedicine.dosage" placeholder="如: 5ml" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="频次" prop="currentMedicine.frequency">
                  <el-select v-model="currentMedicine.frequency" placeholder="请选择频次">
                    <el-option label="一日一次" value="一日一次" />
                    <el-option label="一日两次" value="一日两次" />
                    <el-option label="一日三次" value="一日三次" />
                    <el-option label="需要时使用" value="需要时使用" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="用药天数" prop="currentMedicine.days">
                  <el-input-number v-model="currentMedicine.days" :min="1" :max="30" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="用法" prop="currentMedicine.usage">
                  <el-select v-model="currentMedicine.usage" placeholder="请选择用法">
                    <el-option label="口服" value="口服" />
                    <el-option label="外用" value="外用" />
                    <el-option label="注射" value="注射" />
                    <el-option label="滴眼" value="滴眼" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="数量" prop="currentMedicine.quantity">
                  <el-input-number v-model="currentMedicine.quantity" :min="1" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <div class="add-medicine-button">
              <el-button type="primary" @click="addMedicine">添加药品</el-button>
            </div>
          </div>
        </div>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button v-if="dialogType !== 'view'" type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

// 获取用户信息
const userStore = useUserStore()
const route = useRoute()
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
  prescriptionNo: '',
  patientName: '',
  doctorName: '',
  status: null
})

// 日期范围
const dateRange = ref([])

// 患者、医生、就诊记录和药品选项
const patientOptions = ref([])
const doctorOptions = ref([])
const recordOptions = ref([])
const medicineOptions = ref([])

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogType = ref('add') // add, edit, view
const prescriptionFormRef = ref(null)
const prescriptionForm = reactive({
  id: null,
  patientId: null,
  doctorId: null,
  recordId: null,
  prescriptionNo: '',
  prescriptionDate: '',
  diagnosis: '',
  status: 0,
  notes: '',
  details: []
})

// 当前正在添加的药品
const currentMedicine = reactive({
  medicineId: null,
  dosage: '',
  frequency: '',
  days: 7,
  usage: '',
  quantity: 1
})

// 表单验证规则
const prescriptionFormRules = {
  patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  recordId: [{ required: true, message: '请选择就诊记录', trigger: 'change' }],
  prescriptionDate: [{ required: true, message: '请选择处方日期', trigger: 'change' }]
}

// 初始化
onMounted(() => {
  fetchPatients()
  fetchDoctors()
  fetchMedicines()
  fetchPrescriptions()
  
  // 检查是否从就诊记录页面跳转过来
  if (route.query.action === 'add' && route.query.recordId) {
    handleAdd()
    // 预填充数据
    prescriptionForm.recordId = parseInt(route.query.recordId)
    prescriptionForm.patientId = parseInt(route.query.patientId)
    prescriptionForm.doctorId = parseInt(route.query.doctorId)
  }
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

// 获取药品列表
const fetchMedicines = async () => {
  try {
    await request.get('/medicine/list', {}, {
      onSuccess: (res) => {
        medicineOptions.value = res || []
      }
    })
  } catch (error) {
    console.error('获取药品列表失败:', error)
  }
}

// 获取处方列表
const fetchPrescriptions = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value
    }

    // 如果是医生，只查询自己的处方
    if (isDoctor.value && doctorId.value) {
      params.doctorId = doctorId.value
    } else {
      // 添加其他搜索条件
      if (searchForm.doctorName) {
        params.doctorName = searchForm.doctorName
      }
    }

    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    // 添加其他搜索条件
    if (searchForm.prescriptionNo) {
      params.prescriptionNo = searchForm.prescriptionNo
    }
    if (searchForm.patientName) {
      params.patientName = searchForm.patientName
    }
    if (searchForm.status !== null) {
      params.status = searchForm.status
    }

    await request.get('/prescription/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取处方列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPrescriptions()
}

// 重置搜索
const resetSearch = () => {
  searchForm.prescriptionNo = ''
  searchForm.patientName = ''
  searchForm.doctorName = ''
  searchForm.status = null
  dateRange.value = []
  currentPage.value = 1
  fetchPrescriptions()
}

// 新增处方
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  
  // 设置默认值
  prescriptionForm.prescriptionDate = formatDate(new Date())
  prescriptionForm.status = 0
  prescriptionForm.details = []
  
  // 如果是医生，自动设置医生ID
  if (isDoctor.value && doctorId.value) {
    prescriptionForm.doctorId = doctorId.value
  }
}

// 查看处方
const handleView = async (row) => {
  dialogType.value = 'view'
  dialogVisible.value = true
  
  try {
    await request.get(`/prescription/${row.id}`, {}, {
      onSuccess: (res) => {
        Object.keys(prescriptionForm).forEach(key => {
          prescriptionForm[key] = res[key]
        })
      }
    })
  } catch (error) {
    console.error('获取处方详情失败:', error)
  }
}

// 更新处方状态
const handleUpdateStatus = async (id, status) => {
  try {
    await request.put(`/prescription/status/${id}?status=${status}`, {
     
    }, {
      successMsg: status === 1 ? '已标记为已取药' : '已标记为未取药',
      onSuccess: () => {
        fetchPrescriptions()
      }
    })
  } catch (error) {
    console.error('更新处方状态失败:', error)
  }
}

// 添加药品到处方
const addMedicine = () => {
  // 验证药品信息
  if (!currentMedicine.medicineId) {
    ElMessage.warning('请选择药品')
    return
  }
  if (!currentMedicine.dosage) {
    ElMessage.warning('请输入用量')
    return
  }
  if (!currentMedicine.frequency) {
    ElMessage.warning('请选择频次')
    return
  }
  if (!currentMedicine.usage) {
    ElMessage.warning('请选择用法')
    return
  }
  
  // 获取选中的药品信息
  const medicine = medicineOptions.value.find(m => m.id === currentMedicine.medicineId)
  
  // 创建药品明细对象
  const detail = {
    medicineId: currentMedicine.medicineId,
    dosage: currentMedicine.dosage,
    frequency: currentMedicine.frequency,
    days: currentMedicine.days,
    usage: currentMedicine.usage,
    quantity: currentMedicine.quantity,
    medicine: medicine // 添加药品信息，用于显示
  }
  
  // 添加到处方明细列表
  if (!prescriptionForm.details) {
    prescriptionForm.details = []
  }
  prescriptionForm.details.push(detail)
  
  // 重置当前药品表单
  currentMedicine.medicineId = null
  currentMedicine.dosage = ''
  currentMedicine.frequency = ''
  currentMedicine.days = 7
  currentMedicine.usage = ''
  currentMedicine.quantity = 1
  
  ElMessage.success('药品添加成功')
}

// 移除药品
const removeMedicine = (index) => {
  prescriptionForm.details.splice(index, 1)
}

// 提交表单
const submitForm = () => {
  prescriptionFormRef.value.validate(async (valid) => {
    if (valid) {
      if (prescriptionForm.details.length === 0) {
        ElMessage.warning('请至少添加一种药品')
        return
      }
      
      if (dialogType.value === 'add') {
        // 新增处方
        await request.post('/prescription', prescriptionForm, {
          successMsg: '新增处方成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchPrescriptions()
          }
        })
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (prescriptionFormRef.value) {
    prescriptionFormRef.value.resetFields()
  }
  Object.keys(prescriptionForm).forEach(key => {
    if (key === 'doctorId' && isDoctor.value && doctorId.value) {
      prescriptionForm[key] = doctorId.value
    } else {
      prescriptionForm[key] = key === 'id' ? null : key === 'details' ? [] : null
    }
  })
  
  // 重置当前药品表单
  currentMedicine.medicineId = null
  currentMedicine.dosage = ''
  currentMedicine.frequency = ''
  currentMedicine.days = 7
  currentMedicine.usage = ''
  currentMedicine.quantity = 1
  
  // 清空就诊记录选项
  recordOptions.value = []
}

// 监听患者和医生选择变化，加载对应的就诊记录
watch([() => prescriptionForm.patientId, () => prescriptionForm.doctorId], async ([newPatientId, newDoctorId]) => {
  if (newPatientId && newDoctorId) {
    try {
      const params = {
        patientId: newPatientId,
        doctorId: newDoctorId
      }
      
      await request.get('/medical-record/page', params, {
        onSuccess: (res) => {
          recordOptions.value = res.records || []
        }
      })
    } catch (error) {
      console.error('获取就诊记录失败:', error)
    }
  } else {
    recordOptions.value = []
  }
}, { immediate: false })

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
  fetchPrescriptions()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPrescriptions()
}
</script>

<style scoped>
.prescription-management {
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

.medicine-details {
  margin-top: 20px;
}

.no-medicine {
  padding: 20px;
  text-align: center;
}

.add-medicine-form {
  margin-top: 20px;
  padding: 20px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}

.add-medicine-button {
  margin-top: 20px;
  text-align: center;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}
</style> 