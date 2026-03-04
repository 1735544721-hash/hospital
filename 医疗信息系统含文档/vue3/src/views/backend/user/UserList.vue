<template>
  <div class="user-list">
    <!-- 搜索栏 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleCode" placeholder="请选择角色" clearable>
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="operations">
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
      </div>
    </div>

    <!-- 用户表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      style="width: 100%"
    >
      <el-table-column prop="username" label="用户名" width="120"></el-table-column>
      <el-table-column prop="name" label="姓名" width="120"></el-table-column>
      <el-table-column prop="roleCode" label="角色" width="120">
        <template #default="scope">
          <el-tag :type="getRoleTagType(scope.row.roleCode)">
            {{ getRoleName(scope.row.roleCode) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sex" label="性别" width="80"></el-table-column>
      <el-table-column prop="phone" label="手机号" width="120"></el-table-column>
      <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" >
        <template #default="scope">
          {{ formatDate(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="220">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button 
            type="success" 
            size="small" 
            v-if="scope.row.status === 0"
            @click="handleStatusChange(scope.row, 1)"
          >启用</el-button>
          <el-button 
            type="warning" 
            size="small" 
            v-if="scope.row.status === 1"
            @click="handleStatusChange(scope.row, 0)"
          >禁用</el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="dialogType === 'edit'" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" placeholder="请选择角色">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="form.sex" placeholder="请选择性别">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { formatDate } from '@/utils/dateUtils'

// 数据定义
const tableData = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const formRef = ref(null)

// 搜索表单
const searchForm = reactive({
  username: '',
  name: '',
  roleCode: ''
})

// 表单数据
const form = reactive({
  id: null,
  username: '',
  password: '',
  name: '',
  roleCode: '',
  sex: '',
  phone: '',
  email: '',
  status: 1
})

// 角色选项
const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '医生', value: 'DOCTOR' },
  { label: '护士', value: 'NURSE' },
  { label: '普通用户', value: 'USER' }
]

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3到50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  sex: [
    { required: false }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    await request.get('/user/page', {
      username: searchForm.username,
      name: searchForm.name,
      roleCode: searchForm.roleCode,
      currentPage: currentPage.value,
      size: pageSize.value
    },{
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询按钮
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

// 重置搜索
const resetSearch = () => {
  searchForm.username = ''
  searchForm.name = ''
  searchForm.roleCode = ''
  handleSearch()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchUsers()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUsers()
}

// 获取角色名称
const getRoleName = (roleCode) => {
  const role = roleOptions.find(item => item.value === roleCode)
  return role ? role.label : roleCode
}

// 获取角色标签类型
const getRoleTagType = (roleCode) => {
  switch (roleCode) {
    case 'ADMIN':
      return 'danger'
    case 'DOCTOR':
      return 'success'
    case 'NURSE':
      return 'warning'
    case 'USER':
      return 'info'
    default:
      return ''
  }
}

// 新增用户
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  resetForm()
}

// 编辑用户
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 新增用户
        if (dialogType.value === 'add') {
          await request.post('/user/add', form, {
            successMsg: '添加用户成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchUsers()
            }
          })
        } else {
          // 编辑用户
          await request.put(`/user/${form.id}`, form, {
            successMsg: '更新用户成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchUsers()
            }
          })
        }
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户"${row.username}"吗？此操作不可恢复。`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await request.delete(`/user/delete/${row.id}`, {
          successMsg: '删除成功',
          onSuccess: () => {
            fetchUsers()
          }
        })
      } catch (error) {
        console.error('删除用户失败:', error)
      }
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '已取消删除'
      })
    })
}

// 修改用户状态
const handleStatusChange = async (row, status) => {
  const statusText = status === 1 ? '启用' : '禁用'
  try {
    await request.put(`/user/status/${row.id}?status=${status}`, {}, {
      successMsg: `${statusText}成功`,
      onSuccess: () => {
        fetchUsers()
      }
    })
  } catch (error) {
    console.error(`${statusText}用户失败:`, error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.username = ''
  form.password = ''
  form.name = ''
  form.roleCode = ''
  form.sex = ''
  form.phone = ''
  form.email = ''
  form.status = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 初始化
onMounted(() => {
  fetchUsers()
})
</script>

<style lang="scss" scoped>
.user-list {
  .search-container {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
    
    .search-form {
      flex: 1;
    }
    
    .operations {
      display: flex;
      align-items: flex-start;
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style> 