<template>
  <div class="medicine-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>药品管理</h3>
          <el-button type="primary" @click="handleAdd">新增药品</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="药品名称">
          <el-input v-model="searchForm.medicineName" placeholder="请输入药品名称" clearable />
        </el-form-item>
        <el-form-item label="药品编码">
          <el-input v-model="searchForm.medicineCode" placeholder="请输入药品编码" clearable />
        </el-form-item>
        <el-form-item label="药品分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类ID" clearable>
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称">
          <el-select v-model="searchForm.category" placeholder="请选择分类名称" clearable>
            <el-option
              v-for="category in categoryOptions"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 药品列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="medicineCode" label="药品编码" width="120" />
        <el-table-column prop="medicineName" label="药品名称" width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="dosageForm" label="剂型" width="100" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="instructions" label="使用说明" width="150">
          <template #default="scope">
            <el-tooltip
              :content="scope.row.instructions || '暂无使用说明'"
              placement="top"
              :show-after="200"
              max-width="300"
            >
              <span>{{ (scope.row.instructions || '暂无').slice(0, 15) }}{{ scope.row.instructions && scope.row.instructions.length > 15 ? '...' : '' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="生产厂家" min-width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-popconfirm title="确定删除此药品吗？" @confirm="handleDelete(scope.row)">
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

    <!-- 新增/编辑药品表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增药品' : '编辑药品'"
      width="700px"
      @closed="resetForm"
    >
      <el-form
        ref="medicineFormRef"
        :model="medicineForm"
        :rules="medicineFormRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="药品名称" prop="medicineName">
              <el-input v-model="medicineForm.medicineName" placeholder="请输入药品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="药品编码" prop="medicineCode">
              <el-input v-model="medicineForm.medicineCode" placeholder="请输入药品编码" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格" prop="specification">
              <el-input v-model="medicineForm.specification" placeholder="请输入规格" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剂型" prop="dosageForm">
              <el-select v-model="medicineForm.dosageForm" placeholder="请选择剂型" style="width: 100%">
                <el-option label="片剂" value="片剂" />
                <el-option label="胶囊" value="胶囊" />
                <el-option label="注射剂" value="注射剂" />
                <el-option label="口服液" value="口服液" />
                <el-option label="粉剂" value="粉剂" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="medicineForm.price" :precision="2" :step="0.1" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="medicineForm.stock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="药品分类" prop="categoryId">
              <el-select v-model="medicineForm.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="category in categoryList"
                  :key="category.id"
                  :label="category.categoryName"
                  :value="category.id"
                  />
              </el-select>
            </el-form-item>
            <!-- 保留category字段但隐藏，用于提交表单时同步 -->
            <el-form-item prop="category" style="display: none;">
              <el-input v-model="medicineForm.category" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生产厂家" prop="manufacturer">
              <el-input v-model="medicineForm.manufacturer" placeholder="请输入生产厂家" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
        <el-form-item label="使用说明" prop="instructions">
          <el-input
            v-model="medicineForm.instructions"
            type="textarea"
            :rows="3"
            placeholder="请输入使用说明"
          />
        </el-form-item>
          </el-col>
        </el-row>

        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="medicineForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
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

    <!-- 药品详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="药品详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="药品编码">{{ detailForm.medicineCode }}</el-descriptions-item>
        <el-descriptions-item label="药品名称">{{ detailForm.medicineName }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ detailForm.specification || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="剂型">{{ detailForm.dosageForm || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ detailForm.price }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ detailForm.stock }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ getCategoryName(detailForm.categoryId) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailForm.status === 1 ? 'success' : 'info'">
            {{ detailForm.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生产厂家">{{ detailForm.manufacturer || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="使用说明" :span="2">
          <div style="white-space: pre-line">{{ detailForm.instructions || '暂无说明' }}</div>
        </el-descriptions-item>
 
      </el-descriptions>
      
      <!-- 库存调整 -->
      <div class="stock-adjustment" v-if="detailForm.id">
        <h4>库存调整</h4>
        <div class="stock-form">
          <el-input-number v-model="stockChange" :min="-detailForm.stock" placeholder="调整数量" />
          <el-button type="primary" @click="handleStockAdjust">确认调整</el-button>
        </div>
        <div class="stock-tip">
          <p>提示：正数表示入库，负数表示出库</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
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
  medicineName: '',
  medicineCode: '',
  category: '',
  categoryId: null,
  status: null
})

// 药品分类选项
const categoryOptions = ref([])
const categoryList = ref([])
const isAddingCategory = ref(false)
const newCategory = ref('')

// 新增/编辑表单
const medicineFormRef = ref(null)
const dialogVisible = ref(false)
const dialogType = ref('add') // 对话框类型：add-新增，edit-编辑
const medicineForm = reactive({
  id: null,
  medicineName: '',
  medicineCode: '',
  specification: '',
  dosageForm: '',
  price: 0,
  stock: 0,
  manufacturer: '',
  categoryId: '',
  instructions: '',
  status: 1,
 
})

// 详情对话框
const detailDialogVisible = ref(false)
const detailForm = reactive({
  id: null,
  medicineName: '',
  medicineCode: '',
  specification: '',
  dosageForm: '',
  price: 0,
  stock: 0,
  manufacturer: '',
  categoryId: '',
  category: '',
  instructions: '',
  status: 1,
 
  createTime: '',
  updateTime: ''
})

// 库存调整
const stockChange = ref(0)

// 表单验证规则
const medicineFormRules = {
  medicineName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  medicineCode: [{ required: true, message: '请输入药品编码', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 监听分类选择
watch(() => medicineForm.categoryId, (val) => {
  if (val) {
    const category = categoryList.value.find(item => item.id === val)
    if (category) {
      medicineForm.category = category.categoryName
    }
  } else {
    medicineForm.category = ''
  }
})

// 初始化
onMounted(() => {
  fetchMedicines()
  fetchCategories()
})

// 获取药品列表
const fetchMedicines = async () => {
  loading.value = true
  try {
    await request.get('/medicine/page', {
      medicineName: searchForm.medicineName,
      medicineCode: searchForm.medicineCode,
      category: searchForm.category,
      categoryId: searchForm.categoryId,
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
    console.error('获取药品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取所有药品分类
const fetchCategories = async () => {
  try {
    await request.get('/medicine-category/list', {}, {
      onSuccess: (res) => {
        categoryList.value = res || []
        categoryOptions.value = res.map(category => category.categoryName) || []
      },
      showDefaultMsg: false,
      onError: () => {
        // 如果API调用失败，提供一些默认分类
        categoryOptions.value = ['处方药', '非处方药', '中药', '西药', '针剂', '口服药']
        console.warn('获取药品分类失败，使用默认分类')
      }
    })
  } catch (error) {
    console.error('获取药品分类失败:', error)
    // 提供默认分类作为后备方案
    categoryOptions.value = ['处方药', '非处方药', '中药', '西药', '针剂', '口服药']
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchMedicines()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    if (key === 'status') {
      searchForm[key] = null
    } else if (key === 'categoryId') {
      searchForm[key] = null
    } else {
      searchForm[key] = ''
    }
  })
  currentPage.value = 1
  fetchMedicines()
}

// 新增药品
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 编辑药品
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(medicineForm).forEach(key => {
    medicineForm[key] = row[key]
  })
  dialogVisible.value = true
}

// 查看药品详情
const handleViewDetail = async (row) => {
  try {
    await request.get(`/medicine/${row.id}`, {}, {
      onSuccess: (res) => {
        Object.keys(detailForm).forEach(key => {
          detailForm[key] = res[key]
        })
        stockChange.value = 0
        detailDialogVisible.value = true
      }
    })
  } catch (error) {
    console.error('获取药品详情失败:', error)
  }
}

// 删除药品
const handleDelete = async (row) => {
  try {
    await request.delete(`/medicine/${row.id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchMedicines()
      }
    })
  } catch (error) {
    console.error('删除药品失败:', error)
  }
}

// 库存调整
const handleStockAdjust = async () => {
  if (stockChange.value === 0) {
    ElMessage.warning('请输入调整数量')
    return
  }
  
  try {
    await request.put(`/medicine/${detailForm.id}/stock?stockChange=${stockChange.value}`, {
     
    }, {
      successMsg: '库存调整成功',
      onSuccess: () => {
        // 更新详情中的库存数量
        detailForm.stock += stockChange.value
        stockChange.value = 0
        
        // 刷新列表
        fetchMedicines()
      }
    })
  } catch (error) {
    console.error('库存调整失败:', error)
  }
}

// 提交表单
const submitForm = () => {
  medicineFormRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        // 新增药品
        await request.post('/medicine', medicineForm, {
          successMsg: '新增药品成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchMedicines()
            fetchCategories() // 刷新分类列表
          }
        })
      } else {
        // 编辑药品
        await request.put(`/medicine/${medicineForm.id}`, medicineForm, {
          successMsg: '编辑药品成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchMedicines()
            fetchCategories() // 刷新分类列表
          }
        })
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (medicineFormRef.value) {
    medicineFormRef.value.resetFields()
  }
  Object.keys(medicineForm).forEach(key => {
    if (key === 'price' || key === 'stock') {
      medicineForm[key] = 0
    } else if (key === 'status') {
      medicineForm[key] = 1
    } else {
      medicineForm[key] = key === 'id' ? null : ''
    }
  })
  medicineForm.category = ''
  medicineForm.categoryId = null
}

// 分页操作
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchMedicines()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchMedicines()
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  const category = categoryList.value.find(item => item.id === categoryId)
  return category ? category.categoryName : '未知分类'
}
</script>

<style scoped>
.medicine-management {
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

.stock-adjustment {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.stock-form {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.stock-form .el-input-number {
  width: 180px;
  margin-right: 10px;
}

.stock-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}
</style> 