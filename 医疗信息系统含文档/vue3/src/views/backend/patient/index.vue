<template>
  <div class="patient-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>患者管理</h3>
          <el-button type="primary" @click="handleAdd">新增患者</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="searchForm.idCard" placeholder="请输入身份证号" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="关联用户">
          <el-input v-model="searchForm.username" placeholder="请输入关联用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 患者列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="patientNo" label="患者编号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="sex" label="性别" width="60" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="birthday" label="出生日期" width="100">
          <template #default="scope">
            {{ scope.row.birthday ? scope.row.birthday : '未设置' }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="住址" min-width="200">
          <template #default="scope">
            {{ scope.row.address || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column prop="user" label="关联用户" width="120">
          <template #default="scope">
            {{ scope.row.user ? scope.row.user.username : '未关联' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-popconfirm title="确定删除此患者吗？" @confirm="handleDelete(scope.row)">
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

    <!-- 新增/编辑患者表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增患者' : '编辑患者'"
      width="600px"
      @closed="resetForm"
    >
      <el-form
        ref="patientFormRef"
        :model="patientForm"
        :rules="getPatientFormRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="patientForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="patientForm.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="patientForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker
            v-model="patientForm.birthday"
            type="date"
            placeholder="请选择出生日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="patientForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="住址" prop="address">
          <el-input
            v-model="patientForm.address"
            type="textarea"
            :rows="2"
            placeholder="请输入住址"
          />
        </el-form-item>
        <el-form-item label="病史" prop="medicalHistory">
          <el-input
            v-model="patientForm.medicalHistory"
            type="textarea"
            :rows="3"
            placeholder="请输入病史"
          />
        </el-form-item>
        <el-form-item label="过敏史" prop="allergies">
          <el-input
            v-model="patientForm.allergies"
            type="textarea"
            :rows="2"
            placeholder="请输入过敏史"
          />
        </el-form-item>
        
        <!-- 关联用户部分 -->
        <el-form-item label="关联用户" prop="userType">
          <el-radio-group v-model="userType" @change="handleUserTypeChange">
            <el-radio :label="'existing'">选择已有用户</el-radio>
            <el-radio :label="'new'" v-if="dialogType === 'add'">创建新用户</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 选择已有用户 -->
        <el-form-item v-if="userType === 'existing'" label="选择用户" prop="userId">
          <el-select v-model="patientForm.userId" placeholder="请选择关联用户" clearable filterable>
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 创建新用户表单 -->
      <el-form
        v-if="userType === 'new'"
        ref="newUserFormRef"
        :model="newUserForm"
        :rules="newUserFormRules"
        label-width="100px"
      >
        <el-divider content-position="center">新用户信息</el-divider>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="newUserForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="newUserForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="newUserForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="newUserForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 患者详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="患者详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="患者编号">{{ detailForm.patientNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailForm.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detailForm.sex }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailForm.idCard }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ detailForm.birthday }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailForm.phone }}</el-descriptions-item>
        <el-descriptions-item label="住址" :span="2">{{ detailForm.address }}</el-descriptions-item>
        <el-descriptions-item label="病史" :span="2">
          <div style="white-space: pre-line">{{ detailForm.medicalHistory || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="过敏史" :span="2">
          <div style="white-space: pre-line">{{ detailForm.allergies || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="关联用户">
          {{ detailForm.user ? detailForm.user.username : '未关联' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(detailForm.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/dateUtils'

// 列表数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  name: '',
  idCard: '',
  phone: '',
  username: ''
})

// 用户选项
const userOptions = ref([])

// 新增/编辑表单
const patientFormRef = ref(null)
const newUserFormRef = ref(null)
const dialogVisible = ref(false)
const dialogType = ref('add') // 对话框类型：add-新增，edit-编辑
const userType = ref('existing')
const patientForm = reactive({
  id: null,
  name: '',
  sex: '男',
  idCard: '',
  birthday: '',
  phone: '',
  address: '',
  medicalHistory: '',
  allergies: '',
  userId: null
})

// 新用户表单
const newUserForm = reactive({
  username: '',
  password: '',
  email: '',
  phone: ''
})

// 详情对话框
const detailDialogVisible = ref(false)
const detailForm = reactive({
  id: null,
  patientNo: '',
  name: '',
  sex: '',
  idCard: '',
  birthday: '',
  phone: '',
  address: '',
  medicalHistory: '',
  allergies: '',
  createTime: '',
  updateTime: '',
  user: null
})

// 表单验证规则
const patientFormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  userId: [{ required: true, message: '请选择关联用户', trigger: 'change' }]
}

// 新用户表单验证规则
const newUserFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3到50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6到20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// 监听用户类型变化，动态调整验证规则
const getPatientFormRules = computed(() => {
  if (userType.value === 'existing') {
    return {
      ...patientFormRules,
      userId: [{ required: true, message: '请选择关联用户', trigger: 'change' }]
    }
  } else {
    const rules = { ...patientFormRules }
    delete rules.userId
    return rules
  }
})

// 初始化
onMounted(() => {
  fetchPatients()
  fetchUsers()
})

// 获取患者列表
const fetchPatients = async () => {
  loading.value = true
  try {
    await request.get('/patient/page', {
      name: searchForm.name,
      idCard: searchForm.idCard,
      phone: searchForm.phone,
      username: searchForm.username,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取患者列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    await request.get('/user/page', {
      roleCode: 'PATIENT',
      status: 1,
      size: 100
    }, {
      onSuccess: (res) => {
        userOptions.value = res.records
      }
    })
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPatients()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchPatients()
}

// 新增患者
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 编辑患者
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(patientForm).forEach(key => {
    patientForm[key] = row[key]
  })
  dialogVisible.value = true
}

// 查看患者详情
const handleViewDetail = async (row) => {
  try {
    await request.get(`/patient/${row.id}`, {}, {
      onSuccess: (res) => {
        Object.keys(detailForm).forEach(key => {
          detailForm[key] = res[key]
        })
        detailDialogVisible.value = true
      }
    })
  } catch (error) {
    console.error('获取患者详情失败:', error)
  }
}

// 删除患者
const handleDelete = async (row) => {
  try {
    await request.delete(`/patient/${row.id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchPatients()
      }
    })
  } catch (error) {
    console.error('删除患者失败:', error)
  }
}

// 处理关联用户类型变化
const handleUserTypeChange = () => {
  patientForm.userId = null;
  
  if (userType.value === 'new') {
    Object.keys(newUserForm).forEach(key => {
      newUserForm[key] = ''
    })
  }
}

// 提交表单
const submitForm = async () => {
  let patientValid = false;
  let userValid = true;
  
  await patientFormRef.value.validate((valid) => {
    patientValid = valid;
  });
  
  if (userType.value === 'new' && patientValid) {
    await newUserFormRef.value.validate((valid) => {
      userValid = valid;
    });
  }
  
  if (!patientValid || !userValid) {
    return;
  }
  
  try {
    loading.value = true;
    
    if (dialogType.value === 'add') {
      if (userType.value === 'new') {
        await request.post('/patient/with-user', {
          user: {
            username: newUserForm.username,
            password: newUserForm.password,
            email: newUserForm.email,
            phone: newUserForm.phone,
            name: patientForm.name,
            roleCode: 'PATIENT'
          },
          patient: patientForm
        }, {
          successMsg: '新增患者和用户成功',
          onSuccess: () => {
            dialogVisible.value = false;
            fetchPatients();
            fetchUsers();
          }
        });
      } else {
        await request.post('/patient', patientForm, {
          successMsg: '新增患者成功',
          onSuccess: () => {
            dialogVisible.value = false;
            fetchPatients();
          }
        });
      }
    } else {
      const { data: currentPatient } = await request.get(`/patient/${patientForm.id}`, {}, {
        showDefaultMsg: false
      });
      
      if (currentPatient && currentPatient.userId !== patientForm.userId) {
        if (currentPatient.userId) {
          await request.put(`/patient/unbind/${patientForm.id}`, {}, {
            showDefaultMsg: false,
            onError: (error) => {
              ElMessage.error('解绑原关联用户失败: ' + (error.message || '未知错误'));
              throw new Error('解绑用户失败');
            }
          });
        }
        
        if (patientForm.userId) {
          await request.put(`/patient/bind/${patientForm.id}/${patientForm.userId}`, {}, {
            showDefaultMsg: false,
            onError: (error) => {
              ElMessage.error('绑定新关联用户失败: ' + (error.message || '未知错误'));
              throw new Error('绑定用户失败');
            }
          });
        }
      }
      
      const updateData = { ...patientForm };
      delete updateData.userId;
      
      await request.put(`/patient/${patientForm.id}`, updateData, {
        successMsg: '编辑患者成功',
        onSuccess: () => {
          dialogVisible.value = false;
          fetchPatients();
        },
        onError: (error) => {
          ElMessage.error('更新患者信息失败: ' + (error.message || '未知错误'));
        }
      });
    }
  } catch (error) {
    console.error('操作失败:', error);
  } finally {
    loading.value = false;
  }
}

// 重置表单
const resetForm = () => {
  if (patientFormRef.value) {
    patientFormRef.value.resetFields()
  }
  if (newUserFormRef.value) {
    newUserFormRef.value.resetFields()
  }
  Object.keys(patientForm).forEach(key => {
    if (key === 'sex') {
      patientForm[key] = '男'
    } else {
      patientForm[key] = key === 'id' || key === 'userId' ? null : ''
    }
  })
  Object.keys(newUserForm).forEach(key => {
    newUserForm[key] = ''
  })
  
  userType.value = 'existing'
}

// 分页操作
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchPatients()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPatients()
}
</script>

<style scoped>
.patient-management {
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
</style> 