<template>
  <div class="schedule-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>医生排班管理</h3>
          <div class="header-buttons" v-if="!isDoctor">
            <el-button type="primary" @click="handleBatchAdd">批量排班</el-button>
            <el-button type="success" @click="handleAdd">新增排班</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="医生" v-if="!isDoctor">
          <el-select v-model="searchForm.doctorId" placeholder="请选择医生" clearable filterable>
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
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
        <el-form-item label="时间段">
          <el-select v-model="searchForm.timeSlot" placeholder="请选择时间段" clearable>
            <el-option label="上午" value="上午" />
            <el-option label="下午" value="下午" />
            <el-option label="晚上" value="晚上" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="停诊" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 排班列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="scheduleDate" label="排班日期"  sortable>
          <template #default="scope">
            {{ scope.row.scheduleDate }}
          </template>
        </el-table-column>
        <el-table-column prop="timeSlot" label="时间段" width="80" />
        <el-table-column prop="doctor.name" label="医生姓名" width="120" v-if="!isDoctor" />
        <el-table-column prop="doctor.title" label="职称" width="120" />
        <el-table-column prop="doctor.department.deptName" label="科室" width="120" />
        <el-table-column prop="maxPatients" label="最大接诊人数" width="120" />
        <el-table-column prop="currentPatients" label="当前预约人数" width="120" />
        <el-table-column label="剩余名额" width="100">
          <template #default="scope">
            {{ scope.row.maxPatients - scope.row.currentPatients }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '停诊' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" v-if="!isDoctor">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="danger"
              size="small"
              @click="handleChangeStatus(scope.row.id, 0)"
              :disabled="scope.row.currentPatients > 0"
            >
              停诊
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="handleChangeStatus(scope.row.id, 1)"
            >
              恢复
            </el-button>
            <el-popconfirm
              title="确定删除此排班吗？"
              @confirm="handleDelete(scope.row)"
              :disabled="scope.row.currentPatients > 0"
            >
              <template #reference>
                <el-button
                  type="danger"
                  size="small"
                  :disabled="scope.row.currentPatients > 0"
                >
                  删除
                </el-button>
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

    <!-- 新增/编辑排班表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增排班' : '编辑排班'"
      width="500px"
      @closed="resetForm"
    >
      <el-form
        ref="scheduleFormRef"
        :model="scheduleForm"
        :rules="scheduleFormRules"
        label-width="100px"
      >
        <el-form-item label="医生" prop="doctorId" v-if="!isDoctor">
          <el-select v-model="scheduleForm.doctorId" placeholder="请选择医生" filterable style="width: 100%">
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker
            v-model="scheduleForm.scheduleDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlot">
          <el-select v-model="scheduleForm.timeSlot" placeholder="请选择时间段" style="width: 100%">
            <el-option label="上午" value="上午" />
            <el-option label="下午" value="下午" />
            <el-option label="晚上" value="晚上" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大接诊人数" prop="maxPatients">
          <el-input-number v-model="scheduleForm.maxPatients" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="scheduleForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停诊</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量排班对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量排班"
      width="600px"
      @closed="resetBatchForm"
    >
      <el-form
        ref="batchFormRef"
        :model="batchForm"
        :rules="batchFormRules"
        label-width="100px"
      >
        <el-form-item label="医生" prop="doctorId" v-if="!isDoctor">
          <el-select v-model="batchForm.doctorId" placeholder="请选择医生" filterable style="width: 100%">
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排班时间段" prop="timeSlots">
          <el-checkbox-group v-model="batchForm.timeSlots">
            <el-checkbox label="上午" />
            <el-checkbox label="下午" />
            <el-checkbox label="晚上" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="工作日选择" prop="workDays">
          <el-checkbox-group v-model="batchForm.workDays">
            <el-checkbox label="1">周一</el-checkbox>
            <el-checkbox label="2">周二</el-checkbox>
            <el-checkbox label="3">周三</el-checkbox>
            <el-checkbox label="4">周四</el-checkbox>
            <el-checkbox label="5">周五</el-checkbox>
            <el-checkbox label="6">周六</el-checkbox>
            <el-checkbox label="0">周日</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="最大接诊人数" prop="maxPatients">
          <el-input-number v-model="batchForm.maxPatients" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBatchForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
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

// 医生选项
const doctorOptions = ref([])

// 搜索表单
const searchForm = reactive({
  doctorId: null,
  timeSlot: '',
  status: null
})

// 日期范围
const dateRange = ref([])

// 新增/编辑表单
const scheduleFormRef = ref(null)
const dialogVisible = ref(false)
const dialogType = ref('add') // 对话框类型：add-新增，edit-编辑
const scheduleForm = reactive({
  id: null,
  doctorId: null,
  scheduleDate: '',
  timeSlot: '',
  maxPatients: 20,
  currentPatients: 0,
  status: 1
})

// 批量排班表单
const batchFormRef = ref(null)
const batchDialogVisible = ref(false)
const batchForm = reactive({
  doctorId: null,
  dateRange: [],
  timeSlots: [],
  workDays: ['1', '2', '3', '4', '5'], // 默认周一至周五
  maxPatients: 20
})

// 表单验证规则
const scheduleFormRules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  maxPatients: [{ required: true, message: '请输入最大接诊人数', trigger: 'blur' }]
}

// 批量表单验证规则
const batchFormRules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  timeSlots: [{ required: true, message: '请选择排班时间段', trigger: 'change' }],
  workDays: [{ required: true, message: '请选择工作日', trigger: 'change' }],
  maxPatients: [{ required: true, message: '请输入最大接诊人数', trigger: 'blur' }]
}

// 初始化
onMounted(() => {
  // 如果是医生，自动设置doctorId并隐藏医生选择
  if (isDoctor.value && doctorId.value) {
    searchForm.doctorId = doctorId.value
    scheduleForm.doctorId = doctorId.value
    batchForm.doctorId = doctorId.value
  }
  
  fetchDoctors()
  fetchSchedules()
})

// 获取医生列表
const fetchDoctors = async () => {
  // 如果是医生，不需要获取医生列表
  if (isDoctor.value) {
    doctorOptions.value = [userStore.doctorInfo]
    return
  }
  
  try {
    await request.get('/doctor/list', {}, {
      onSuccess: (res) => {
        doctorOptions.value = res || []
      }
    })
  } catch (error) {
    console.error('获取医生列表失败:', error)
  }
}

// 获取排班列表
const fetchSchedules = async () => {
  loading.value = true
  try {
    const params = {
      doctorId: isDoctor.value ? doctorId.value : searchForm.doctorId,
      timeSlot: searchForm.timeSlot,
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    await request.get('/schedule/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取排班列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchSchedules()
}

// 重置搜索
const resetSearch = () => {
  // 如果是医生，保持doctorId
  if (!isDoctor.value) {
    searchForm.doctorId = null
  }
  searchForm.timeSlot = ''
  searchForm.status = null
  dateRange.value = []
  currentPage.value = 1
  fetchSchedules()
}

// 新增排班
const handleAdd = () => {
  dialogType.value = 'add'
  // 如果是医生，自动设置doctorId
  if (isDoctor.value) {
    scheduleForm.doctorId = doctorId.value
  }
  dialogVisible.value = true
}

// 编辑排班
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(scheduleForm).forEach(key => {
    scheduleForm[key] = row[key]
  })
  dialogVisible.value = true
}

// 批量排班
const handleBatchAdd = () => {
  // 如果是医生，自动设置doctorId
  if (isDoctor.value) {
    batchForm.doctorId = doctorId.value
  }
  batchDialogVisible.value = true
}

// 更改排班状态
const handleChangeStatus = async (id, status) => {
  try {
    await request.put(`/schedule/${id}/status`, {
      status
    }, {
      successMsg: status === 1 ? '排班已恢复' : '排班已停诊',
      onSuccess: () => {
        fetchSchedules()
      }
    })
  } catch (error) {
    console.error('更改排班状态失败:', error)
  }
}

// 删除排班
const handleDelete = async (row) => {
  if (row.currentPatients > 0) {
    ElMessage.warning('该排班已有患者预约，无法删除')
    return
  }

  try {
    await request.delete(`/schedule/${row.id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchSchedules()
      }
    })
  } catch (error) {
    console.error('删除排班失败:', error)
  }
}

// 提交表单
const submitForm = () => {
  scheduleFormRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        // 新增排班
        await request.post('/schedule', scheduleForm, {
          successMsg: '新增排班成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchSchedules()
          }
        })
      } else {
        // 编辑排班
        await request.put(`/schedule/${scheduleForm.id}`, scheduleForm, {
          successMsg: '编辑排班成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchSchedules()
          }
        })
      }
    }
  })
}

// 提交批量排班表单
const submitBatchForm = () => {
  batchFormRef.value.validate(async (valid) => {
    if (valid) {
      if (!batchForm.dateRange || batchForm.dateRange.length !== 2) {
        ElMessage.warning('请选择日期范围')
        return
      }

      // 生成排班数据
      const schedules = generateBatchSchedules()
      
      if (schedules.length === 0) {
        ElMessage.warning('没有生成任何排班，请检查日期范围和工作日设置')
        return
      }

      try {
        await request.post('/schedule/batch', schedules, {
          successMsg: `成功创建${schedules.length}条排班记录`,
          onSuccess: () => {
            batchDialogVisible.value = false
            fetchSchedules()
          }
        })
      } catch (error) {
        console.error('批量创建排班失败:', error)
      }
    }
  })
}

// 生成批量排班数据
const generateBatchSchedules = () => {
  const schedules = []
  const startDate = new Date(batchForm.dateRange[0])
  const endDate = new Date(batchForm.dateRange[1])
  
  // 遍历日期范围
  for (let date = new Date(startDate); date <= endDate; date.setDate(date.getDate() + 1)) {
    const dayOfWeek = date.getDay().toString() // 获取星期几 (0-6)
    
    // 检查是否是选中的工作日
    if (batchForm.workDays.includes(dayOfWeek)) {
      // 为每个选中的时间段创建排班
      for (const timeSlot of batchForm.timeSlots) {
        schedules.push({
          doctorId: batchForm.doctorId,
          scheduleDate: formatDate(date),
          timeSlot: timeSlot,
          maxPatients: batchForm.maxPatients,
          currentPatients: 0,
          status: 1
        })
      }
    }
  }
  
  return schedules
}

// 格式化日期为 YYYY-MM-DD
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 重置表单
const resetForm = () => {
  if (scheduleFormRef.value) {
    scheduleFormRef.value.resetFields()
  }
  Object.keys(scheduleForm).forEach(key => {
    if (key === 'maxPatients') {
      scheduleForm[key] = 20
    } else if (key === 'currentPatients') {
      scheduleForm[key] = 0
    } else if (key === 'status') {
      scheduleForm[key] = 1
    } else if (key === 'doctorId' && isDoctor.value) {
      scheduleForm[key] = doctorId.value
    } else {
      scheduleForm[key] = key === 'id' ? null : ''
    }
  })
}

// 重置批量表单
const resetBatchForm = () => {
  if (batchFormRef.value) {
    batchFormRef.value.resetFields()
  }
  batchForm.doctorId = isDoctor.value ? doctorId.value : null
  batchForm.dateRange = []
  batchForm.timeSlots = []
  batchForm.workDays = ['1', '2', '3', '4', '5']
  batchForm.maxPatients = 20
}

// 分页操作
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchSchedules()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchSchedules()
}
</script>

<style scoped>
.schedule-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 