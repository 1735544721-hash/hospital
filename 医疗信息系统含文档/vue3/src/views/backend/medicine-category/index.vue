<template>
  <div class="medicine-category-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>药品分类管理</h3>
          <el-button type="primary" @click="handleAdd">新增分类</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.categoryName" placeholder="请输入分类名称" clearable />
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="searchForm.categoryCode" placeholder="请输入分类编码" clearable />
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

      <!-- 分类列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="categoryCode" label="分类编码" width="120" />
        <el-table-column prop="categoryName" label="分类名称" width="150" />
        <el-table-column prop="description" label="分类描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="scope">
            {{ new Date(scope.row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除此分类吗？" @confirm="handleDelete(scope.row)">
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

    <!-- 新增/编辑分类表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增分类' : '编辑分类'"
      width="600px"
      @closed="resetForm"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryFormRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="categoryForm.categoryCode" placeholder="请输入分类编码" />
        </el-form-item>
        
        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
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
  categoryName: '',
  categoryCode: '',
  status: null
})

// 新增/编辑表单
const categoryFormRef = ref(null)
const dialogVisible = ref(false)
const dialogType = ref('add') // 对话框类型：add-新增，edit-编辑
const categoryForm = reactive({
  id: null,
  categoryName: '',
  categoryCode: '',
  description: '',
  status: 1
})

// 表单验证规则
const categoryFormRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  categoryCode: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9]+$/, message: '分类编码只能包含大写字母和数字', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 初始化
onMounted(() => {
  fetchCategories()
})

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    await request.get('/medicine-category/page', {
      categoryName: searchForm.categoryName,
      categoryCode: searchForm.categoryCode,
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
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCategories()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    if (key === 'status') {
      searchForm[key] = null
    } else {
      searchForm[key] = ''
    }
  })
  currentPage.value = 1
  fetchCategories()
}

// 新增分类
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(categoryForm).forEach(key => {
    categoryForm[key] = row[key]
  })
  dialogVisible.value = true
}

// 删除分类
const handleDelete = async (row) => {
  try {
    await request.delete(`/medicine-category/${row.id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchCategories()
      }
    })
  } catch (error) {
    console.error('删除分类失败:', error)
  }
}

// 提交表单
const submitForm = () => {
  categoryFormRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        // 新增分类
        await request.post('/medicine-category', categoryForm, {
          successMsg: '新增分类成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchCategories()
          }
        })
      } else {
        // 编辑分类
        await request.put(`/medicine-category/${categoryForm.id}`, categoryForm, {
          successMsg: '编辑分类成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchCategories()
          }
        })
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
  Object.keys(categoryForm).forEach(key => {
    if (key === 'status') {
      categoryForm[key] = 1
    } else {
      categoryForm[key] = key === 'id' ? null : ''
    }
  })
}

// 分页操作
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchCategories()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCategories()
}
</script>

<style scoped>
.medicine-category-management {
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