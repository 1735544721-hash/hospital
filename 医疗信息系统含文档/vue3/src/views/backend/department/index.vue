<template>
  <div class="department-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>科室管理</h3>
          <el-button type="primary" @click="handleAdd">新增科室</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="科室名称">
          <el-input v-model="searchForm.deptName" placeholder="请输入科室名称" clearable />
        </el-form-item>
        <el-form-item label="科室编码">
          <el-input v-model="searchForm.deptCode" placeholder="请输入科室编码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 科室列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="deptCode" label="科室编码" width="120" />
        <el-table-column prop="deptName" label="科室名称" width="150" />
        <el-table-column prop="description" label="科室描述" min-width="250">
          <template #default="scope">
            {{ scope.row.description || '暂无描述' }}
          </template>
        </el-table-column>
        <el-table-column prop="director" label="负责人" width="150">
          <template #default="scope">
            {{ scope.row.director ? scope.row.director.name : '暂无负责人' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除此科室吗？" @confirm="handleDelete(scope.row)">
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

    <!-- 新增/编辑科室表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增科室' : '编辑科室'"
      width="500px"
      @closed="resetForm"
    >
      <el-form
        ref="departmentFormRef"
        :model="departmentForm"
        :rules="departmentFormRules"
        label-width="100px"
      >
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="departmentForm.deptName" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="科室编码" prop="deptCode">
          <el-input v-model="departmentForm.deptCode" placeholder="请输入科室编码" />
        </el-form-item>
        <el-form-item label="科室描述" prop="description">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入科室描述"
          />
        </el-form-item>
        <el-form-item label="负责人" prop="directorId">
          <el-select v-model="departmentForm.directorId" placeholder="请选择负责人" clearable filterable>
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="departmentForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
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
  deptName: '',
  deptCode: '',
  status: null
})

// 新增/编辑表单
const departmentFormRef = ref(null)
const dialogVisible = ref(false)
const dialogType = ref('add') // 对话框类型：add-新增，edit-编辑
const departmentForm = reactive({
  id: null,
  deptName: '',
  deptCode: '',
  description: '',
  directorId: null,
  status: 1
})

// 医生选项
const doctorOptions = ref([])

// 表单验证规则
const departmentFormRules = {
  deptName: [{ required: true, message: '请输入科室名称', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入科室编码', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 初始化
onMounted(() => {
  fetchDepartments()
  fetchDoctors()
})

// 获取科室列表
const fetchDepartments = async () => {
  loading.value = true
  try {
    await request.get('/department/page', {
      deptName: searchForm.deptName,
      deptCode: searchForm.deptCode,
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
    console.error('获取科室列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取医生列表(用于负责人选择)
const fetchDoctors = async () => {
  try {
    await request.get('/doctor/page', {
      status: 1, // 仅获取在职医生
      size: 100
    }, {
      onSuccess: (res) => {
        doctorOptions.value = res.records
      }
    })
  } catch (error) {
    console.error('获取医生列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchDepartments()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'status' ? null : ''
  })
  currentPage.value = 1
  fetchDepartments()
}

// 新增科室
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 编辑科室
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(departmentForm).forEach(key => {
    departmentForm[key] = row[key]
  })
  dialogVisible.value = true
}

// 删除科室
const handleDelete = async (row) => {
  try {
    await request.delete(`/department/${row.id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchDepartments()
      }
    })
  } catch (error) {
    console.error('删除科室失败:', error)
  }
}

// 提交表单
const submitForm = () => {
  departmentFormRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        // 新增科室
        await request.post('/department', departmentForm, {
          successMsg: '新增科室成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchDepartments()
          }
        })
      } else {
        // 编辑科室
        await request.put(`/department/${departmentForm.id}`, departmentForm, {
          successMsg: '编辑科室成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchDepartments()
          }
        })
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (departmentFormRef.value) {
    departmentFormRef.value.resetFields()
  }
  Object.keys(departmentForm).forEach(key => {
    if (key === 'status') {
      departmentForm[key] = 1
    } else {
      departmentForm[key] = key === 'id' || key === 'directorId' ? null : ''
    }
  })
}

// 分页操作
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchDepartments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchDepartments()
}
</script>

<style scoped>
.department-management {
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