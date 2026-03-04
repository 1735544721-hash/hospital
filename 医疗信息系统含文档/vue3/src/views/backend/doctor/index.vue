<template>
  <div class="doctor-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>医生管理</h3>
          <el-button type="primary" @click="handleAdd">新增医生</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="医生编号">
          <el-input v-model="searchForm.doctorNo" placeholder="请输入医生编号" clearable />
        </el-form-item>
        <el-form-item label="所属科室">
          <el-select v-model="searchForm.departmentId" placeholder="请选择科室" clearable>
            <el-option
              v-for="item in departmentOptions"
              :key="item.id"
              :label="item.deptName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="searchForm.title" placeholder="请输入职称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 医生列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="doctorNo" label="医生编号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="所属科室" width="120">
          <template #default="scope">
            {{ scope.row.department ? scope.row.department.deptName : '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="expertise" label="专长" min-width="180">
          <template #default="scope">
            {{ scope.row.expertise || '暂无信息' }}
          </template>
        </el-table-column>
        <el-table-column label="关联用户" width="120">
          <template #default="scope">
            {{ scope.row.user ? scope.row.user.username : '未关联' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-popconfirm title="确定删除此医生吗？" @confirm="handleDelete(scope.row)">
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

    <!-- 新增/编辑医生表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增医生' : '编辑医生'"
      width="600px"
      @closed="resetForm"
    >
      <el-form
        ref="doctorFormRef"
        :model="doctorForm"
        :rules="getDoctorFormRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="doctorForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="所属科室" prop="departmentId">
          <el-select v-model="doctorForm.departmentId" placeholder="请选择科室" clearable filterable>
            <el-option
              v-for="item in departmentOptions"
              :key="item.id"
              :label="item.deptName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-input v-model="doctorForm.title" placeholder="请输入职称" />
        </el-form-item>
        <el-form-item label="专长" prop="expertise">
          <el-input
            v-model="doctorForm.expertise"
            type="textarea"
            :rows="2"
            placeholder="请输入专长"
          />
        </el-form-item>
        <el-form-item label="简介" prop="introduction">
          <el-input
            v-model="doctorForm.introduction"
            type="textarea"
            :rows="3"
            placeholder="请输入简介"
          />
        </el-form-item>

        <!-- 关联用户部分 - 现在在新增和编辑时都可见 -->
        <el-form-item label="关联用户" prop="userType">
          <el-radio-group v-model="userType" @change="handleUserTypeChange">
            <el-radio :label="'existing'">选择已有用户</el-radio>
            <el-radio :label="'new'" v-if="dialogType === 'add'">创建新用户</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 选择已有用户 -->
        <el-form-item v-if="userType === 'existing'" label="选择用户" prop="userId">
          <el-select v-model="doctorForm.userId" placeholder="请选择关联用户" clearable filterable>
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

      <el-form
        ref="statusFormRef"
        :model="doctorForm"
        label-width="100px"
      >
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="doctorForm.status">
            <el-radio :label="1">在职</el-radio>
            <el-radio :label="0">离职</el-radio>
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

    <!-- 医生详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="医生详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="医生编号">{{ detailForm.doctorNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailForm.name }}</el-descriptions-item>
        <el-descriptions-item label="所属科室">
          {{ detailForm.department ? detailForm.department.deptName : '未分配' }}
        </el-descriptions-item>
        <el-descriptions-item label="职称">{{ detailForm.title || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="专长" :span="2">
          <div style="white-space: pre-line">{{ detailForm.expertise || '暂无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">
          <div style="white-space: pre-line">{{ detailForm.introduction || '暂无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="关联用户">
          {{ detailForm.user ? detailForm.user.username : '未关联' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailForm.status === 1 ? 'success' : 'danger'">
            {{ detailForm.status === 1 ? '在职' : '离职' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 列表数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  name: '',
  doctorNo: '',
  departmentId: null,
  title: '',
  status: null
})

// 科室选项
const departmentOptions = ref([])
// 用户选项
const userOptions = ref([])

// 新增/编辑表单
const doctorFormRef = ref(null)
const newUserFormRef = ref(null)
const statusFormRef = ref(null)
const dialogVisible = ref(false)
const dialogType = ref('add') // 对话框类型：add-新增，edit-编辑
const userType = ref('existing')
const doctorForm = reactive({
  id: null,
  name: '',
  departmentId: null,
  title: '',
  expertise: '',
  introduction: '',
  userId: null,
  status: 1
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
  doctorNo: '',
  name: '',
  departmentId: null,
  department: null,
  title: '',
  expertise: '',
  introduction: '',
  userId: null,
  user: null,
  status: 1,
  createTime: '',
  updateTime: ''
})

// 表单验证规则
const doctorFormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择所属科室', trigger: 'change' }],
  userId: [{ required: true, message: '请选择关联用户', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
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
const getDoctorFormRules = computed(() => {
  // 编辑模式下，如果用户类型为existing，需要验证userId
  if (userType.value === 'existing') {
    return {
      ...doctorFormRules,
      userId: [{ required: true, message: '请选择关联用户', trigger: 'change' }]
    }
  } else {
    // 新增+创建新用户模式，不需要验证userId
    const rules = { ...doctorFormRules }
    delete rules.userId
    return rules
  }
})

// 初始化
onMounted(() => {
  fetchDoctors()
  fetchDepartments()
  fetchUsers()
})

// 获取医生列表
const fetchDoctors = async () => {
  loading.value = true
  try {
    await request.get('/doctor/page', {
      name: searchForm.name,
      doctorNo: searchForm.doctorNo,
      departmentId: searchForm.departmentId,
      title: searchForm.title,
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取医生列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取科室列表
const fetchDepartments = async () => {
  try {
    await request.get('/department/list', {}, {
      onSuccess: (res) => {
        departmentOptions.value = res.filter(dept => dept.status === 1) // 只显示启用的科室
      }
    })
  } catch (error) {
    console.error('获取科室列表失败:', error)
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    await request.get('/user/page', {
      roleCode: 'DOCTOR', // 只获取医生角色的用户
      status: 1, // 只获取启用的用户
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
  fetchDoctors()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'departmentId' || key === 'status' ? null : ''
  })
  currentPage.value = 1
  fetchDoctors()
}

// 新增医生
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 编辑医生
const handleEdit = (row) => {
  dialogType.value = 'edit'
  
  // 复制行数据到表单
  Object.keys(doctorForm).forEach(key => {
    doctorForm[key] = row[key]
  })
  
  // 设置用户类型为已有用户
  userType.value = 'existing'
  
  dialogVisible.value = true
}

// 查看医生详情
const handleViewDetail = async (row) => {
  try {
    await request.get(`/doctor/${row.id}`, {}, {
      onSuccess: (res) => {
        Object.keys(detailForm).forEach(key => {
          detailForm[key] = res[key]
        })
        detailDialogVisible.value = true
      }
    })
  } catch (error) {
    console.error('获取医生详情失败:', error)
  }
}

// 删除医生
const handleDelete = async (row) => {
  try {
    await request.delete(`/doctor/${row.id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchDoctors()
      }
    })
  } catch (error) {
    console.error('删除医生失败:', error)
  }
}

// 处理关联用户类型变化
const handleUserTypeChange = () => {
  // 重置用户相关字段
  doctorForm.userId = null;
  
  // 重置新用户表单
  if (userType.value === 'new') {
    Object.keys(newUserForm).forEach(key => {
      newUserForm[key] = ''
    })
  }
}

// 提交表单
const submitForm = async () => {
  let doctorValid = false;
  let userValid = true;
  
  // 验证医生表单
  await doctorFormRef.value.validate((valid) => {
    doctorValid = valid;
  });
  
  // 如果选择创建新用户，验证新用户表单
  if (userType.value === 'new' && doctorValid) {
    await newUserFormRef.value.validate((valid) => {
      userValid = valid;
    });
  }
  
  if (!doctorValid || !userValid) {
    return;
  }
  
  try {
    loading.value = true;
    
      if (dialogType.value === 'add') {
        // 新增医生
      if (userType.value === 'new') {
        // 创建医生并同时创建用户
        await request.post('/doctor/with-user', {
          user: {
            username: newUserForm.username,
            password: newUserForm.password,
            email: newUserForm.email,
            phone: newUserForm.phone,
            name: doctorForm.name // 用医生姓名作为用户姓名
          },
          doctor: doctorForm
        }, {
          successMsg: '新增医生和用户成功',
          onSuccess: () => {
            dialogVisible.value = false;
            fetchDoctors();
            fetchUsers(); // 刷新用户列表
          }
        });
      } else {
        // 使用现有用户创建医生
        await request.post('/doctor', doctorForm, {
          successMsg: '新增医生成功',
          onSuccess: () => {
            dialogVisible.value = false;
            fetchDoctors();
          }
        });
      }
      } else {
        // 编辑医生
      // 获取当前医生信息
      const { data: currentDoctor } = await request.get(`/doctor/${doctorForm.id}`, {}, {
        showDefaultMsg: false
      });
      
      // 如果用户ID发生变化，处理绑定关系
      if (currentDoctor && currentDoctor.userId !== doctorForm.userId) {
        // 先解绑当前用户
        if (currentDoctor.userId) {
          await request.put(`/doctor/${doctorForm.id}/unbind`, {}, {
            showDefaultMsg: false,
            onError: (error) => {
              ElMessage.error('解绑原关联用户失败: ' + (error.message || '未知错误'));
              throw new Error('解绑用户失败');
            }
          });
        }
        
        // 再绑定新用户（如果选择了新用户）
        if (doctorForm.userId) {
          await request.put(`/doctor/${doctorForm.id}/bind/${doctorForm.userId}`, {}, {
            showDefaultMsg: false,
            onError: (error) => {
              ElMessage.error('绑定新关联用户失败: ' + (error.message || '未知错误'));
              throw new Error('绑定用户失败');
            }
          });
        }
      }
      
      // 更新医生基本信息（不包含userId，因为已经通过专门的API修改了）
      const updateData = { ...doctorForm };
      delete updateData.userId; // 移除userId，避免重复更新
      
      await request.put(`/doctor/${doctorForm.id}`, updateData, {
        successMsg: '编辑医生成功',
          onSuccess: () => {
          dialogVisible.value = false;
          fetchDoctors();
        },
        onError: (error) => {
          ElMessage.error('更新医生信息失败: ' + (error.message || '未知错误'));
      }
      });
    }
  } catch (error) {
    console.error('操作失败:', error);
    // 错误信息已在各个步骤中处理
  } finally {
    loading.value = false;
  }
}

// 重置表单
const resetForm = () => {
  if (doctorFormRef.value) {
    doctorFormRef.value.resetFields()
  }
  if (newUserFormRef.value) {
    newUserFormRef.value.resetFields()
  }
  Object.keys(doctorForm).forEach(key => {
    if (key === 'status') {
      doctorForm[key] = 1
    } else {
      doctorForm[key] = key === 'id' || key === 'departmentId' || key === 'userId' ? null : ''
    }
  })
  Object.keys(newUserForm).forEach(key => {
    newUserForm[key] = ''
  })
  
  // 重置用户类型为"选择已有用户"
  userType.value = 'existing'
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

// 获取已关联用户的用户名
const getUsernameById = (userId) => {
  const user = userOptions.value.find(user => user.id === userId)
  return user ? user.username : '未知用户'
}
</script>

<style scoped>
.doctor-management {
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