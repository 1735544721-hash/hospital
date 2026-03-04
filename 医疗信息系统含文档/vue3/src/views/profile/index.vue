<template>
  <div class="profile-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h2><el-icon><UserFilled /></el-icon> 个人中心</h2>
        <p>在这里管理您的个人信息，让我们更好地了解您的需求</p>
      </div>
        </div>

    <el-card class="profile-card" shadow="hover">
      <el-tabs v-model="activeTab">
        <!-- 基本信息 Tab -->
        <el-tab-pane label="基本信息" name="basic">
          <div class="profile-info">
            <div class="avatar-container">
              <el-avatar :size="120" :src="avatarUrl" @error="() => false" />
              <el-upload
                class="avatar-uploader"
                action="#"
                :auto-upload="true"
                :show-file-list="false"
                :http-request="customUploadAvatar"
                :before-upload="beforeAvatarUpload"
              >
                <el-button size="small" type="primary" round>
                  <el-icon><Upload /></el-icon>
                  更换头像
                </el-button>
              </el-upload>
            </div>

            <div class="info-form">
              <div class="section-header">
                <el-icon class="section-icon"><User /></el-icon>
                <h3>个人资料</h3>
              </div>
              <el-form
                ref="userFormRef"
                :model="userForm"
                :rules="rules"
                label-width="100px"
                status-icon
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="userForm.username" disabled class="custom-disabled-input" />
                </el-form-item>

                <el-form-item label="姓名" prop="name">
                  <el-input v-model="userForm.name" />
                </el-form-item>

                <el-form-item label="性别" prop="sex">
                  <el-radio-group v-model="userForm.sex">
                    <el-radio label="男">男</el-radio>
                    <el-radio label="女">女</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="电子邮箱" prop="email">
                  <el-input v-model="userForm.email" />
                </el-form-item>

                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="userForm.phone" />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="submitUserInfo" round>
                    <el-icon><Check /></el-icon>
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>

        <!-- 患者信息 Tab -->
        <el-tab-pane label="患者信息" name="patient">
          <div v-loading="patientLoading">
            <!-- 无患者信息时显示 -->
            <div v-if="!patientInfo && !patientLoading" class="empty-info">
              <el-empty description="暂无患者信息" :image-size="120">
                <template #image>
                  <el-icon class="empty-icon"><FirstAidKit /></el-icon>
                </template>
                <el-button type="primary" @click="handleAddPatient" round>
                  <el-icon><Plus /></el-icon>
                  添加患者信息
                </el-button>
              </el-empty>
            </div>

            <!-- 有患者信息时显示 -->
            <div v-if="patientInfo" class="patient-info">
              <div class="section-header">
                <el-icon class="section-icon"><User /></el-icon>
                <h3>患者信息</h3>
                <div class="header-actions">
                  <el-button type="primary" @click="handleEditPatient" round>
                    <el-icon><Edit /></el-icon>
                    编辑信息
                  </el-button>
                </div>
              </div>
              
              <el-descriptions :column="2" border>
                <el-descriptions-item label="患者编号">
                  <div class="info-with-icon">
                    <el-icon><Ticket /></el-icon>
                    {{ patientInfo.patientNo }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="姓名">
                  <div class="info-with-icon">
                    <el-icon><User /></el-icon>
                    {{ patientInfo.name }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="性别">
                  <div class="info-with-icon">
                    <el-icon>
                      <Male v-if="patientInfo.sex === '男'" />
                      <Female v-else />
                    </el-icon>
                    {{ patientInfo.sex }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="身份证号">
                  <div class="info-with-icon">
                    <el-icon><CreditCard /></el-icon>
                    {{ patientInfo.idCard }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="出生日期">
                  <div class="info-with-icon">
                    <el-icon><Calendar /></el-icon>
                    {{ patientInfo.birthday }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="联系电话">
                  <div class="info-with-icon">
                    <el-icon><Phone /></el-icon>
                    {{ patientInfo.phone }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="住址" :span="2">
                  <div class="info-with-icon">
                    <el-icon><Location /></el-icon>
                    {{ patientInfo.address }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="病史" :span="2">
                  <div class="health-info-box">
                    <div class="health-info-icon">
                      <el-icon><Files /></el-icon>
                    </div>
                    <div style="white-space: pre-line">{{ patientInfo.medicalHistory || '无' }}</div>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="过敏史" :span="2">
                  <div class="allergy-info-box">
                    <div class="health-info-icon">
                      <el-icon><Warning /></el-icon>
                    </div>
                    <div style="white-space: pre-line">{{ patientInfo.allergies || '无' }}</div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>

          <!-- 健康提示卡片 -->
          <el-card v-if="patientInfo" class="health-tips-card" shadow="hover">
            <div class="tips-header">
              <el-icon><InfoFilled /></el-icon>
              <h4>健康小贴士</h4>
            </div>
            <ul class="tips-list">
              <li>请确保您的患者信息真实准确，这有助于医生为您提供更好的诊疗服务</li>
              <li>如有过敏史，请务必详细填写，避免医疗风险</li>
              <li>信息变更后请及时更新，保持个人资料的准确性</li>
              <li>您的信息将被严格保密，请放心填写</li>
            </ul>
          </el-card>
        </el-tab-pane>

        <!-- 修改密码 Tab -->
        <el-tab-pane label="修改密码" name="password">
          <div class="section-header">
            <el-icon class="section-icon"><Lock /></el-icon>
            <h3>密码管理</h3>
          </div>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="password-form"
            status-icon
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                show-password
                placeholder="请输入旧密码"
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="submitPassword" round>
                <el-icon><Check /></el-icon>
                修改密码
              </el-button>
            </el-form-item>
          </el-form>

          <div class="password-tips">
            <div class="tips-header">
              <el-icon><Warning /></el-icon>
              <h4>密码安全提示</h4>
            </div>
            <ul class="tips-list">
              <li>建议使用字母、数字和特殊字符的组合</li>
              <li>密码长度至少6位，建议8位以上</li>
              <li>请勿使用与其他网站相同的密码</li>
              <li>定期更换密码可以提高账号安全性</li>
            </ul>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑患者表单对话框 -->
    <el-dialog
      v-model="patientDialogVisible"
      :title="patientDialogType === 'add' ? '添加患者信息' : '编辑患者信息'"
      width="600px"
      class="patient-dialog"
      @closed="resetPatientForm"
    >
      <div class="dialog-content">
        <div class="dialog-header-info">
          <div class="header-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="header-content">
            <h3>{{ patientDialogType === 'add' ? '新增患者' : '编辑患者' }}</h3>
            <p>请填写患者的详细信息，带 * 号的为必填项</p>
          </div>
        </div>

        <el-form
          ref="patientFormRef"
          :model="patientForm"
          :rules="patientFormRules"
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
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="patientDialogVisible = false" plain round>取消</el-button>
          <el-button type="primary" @click="submitPatientForm" round>确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/store/user";
import request from "@/utils/request";
import {
  UserFilled,
  User,
  Check,
  Upload,
  Edit,
  Plus,
  Warning,
  InfoFilled,
  Calendar,
  Phone,
  Location,
  CreditCard,
  Files,
  Male,
  Female,
  Ticket,
  Lock,
  FirstAidKit
} from '@element-plus/icons-vue';

const baseAPI = import.meta.env.VITE_API_BASE_URL || "/api";
const userStore = useUserStore();
const activeTab = ref("basic");

// 表单引用
const userFormRef = ref(null);
const passwordFormRef = ref(null);
const patientFormRef = ref(null);

// 患者信息相关
const patientInfo = ref(null);
const patientLoading = ref(false);
const patientDialogVisible = ref(false);
const patientDialogType = ref('add');

// 患者表单数据
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
});

// 患者表单验证规则
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
  ]
};

// 用户表单数据
const userForm = reactive({
  id: "",
  username: "",
  name: "",
  email: "",
  phone: "",
  sex: "",
  avatar: "",
});

// 头像地址
const avatarUrl = computed(() => {
    if (!userForm.avatar) return ''
    if (userForm.avatar.startsWith('http')) return userForm.avatar

    return baseAPI +userForm.avatar

});

// 密码表单数据
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 表单校验规则
const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: ["blur", "change"],
    },
  ],
  phone: [
    { required: false, trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号码",
      trigger: ["blur", "change"],
    },
  ],
};

// 密码表单校验规则
const passwordRules = {
  oldPassword: [
    { required: true, message: "请输入旧密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: ["blur", "change"],
    },
  ],
};

// 获取用户信息
const getUserInfo = async () => {
  try {
    // 如果用户已登录，从 store 中获取用户信息
    if (userStore.isLoggedIn) {
      // 从后端重新获取最新的用户信息
      const response = await request.get("/user/current", null, {
        showDefaultMsg: false,
      });

      // 确保返回数据存在
      if (response) {
        // 更新store中的用户信息
        userStore.updateUserInfo(response);

        // 直接更新表单数据
        userForm.id = response.id || "";
        userForm.username = response.username || "";
        userForm.name = response.name || "";
        userForm.email = response.email || "";
        userForm.phone = response.phone || "";
        userForm.sex = response.sex || "男";
        userForm.avatar = response.avatar || "";

        console.log("用户信息加载成功:", userForm);
      }
    }
  } catch (error) {
    console.error("获取用户信息失败", error);
    ElMessage.error("获取用户信息失败");
  }
};

// 上传头像前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === "image/jpeg";
  const isPNG = file.type === "image/png";
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    ElMessage.error("头像只能是 JPG 或 PNG 格式!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("头像大小不能超过 2MB!");
    return false;
  }
  return true;
};

// 自定义头像上传方法
const customUploadAvatar = async (options) => {
  try {
    const { file } = options;

    // 创建 FormData 对象
    const formData = new FormData();
    formData.append("file", file);

    // 设置自定义上传选项
    const uploadOptions = {
      headers: {
        token: localStorage.getItem("token") || "",
      },
      // 不进行JSON处理
      transformRequest: [(data) => data],
      // 自定义成功消息
      successMsg: "头像上传成功",
      // 自定义错误消息
      errorMsg: "头像上传失败",
      // 成功回调
      onSuccess: async (data) => {
        // 更新用户头像
        userForm.avatar = data;

        // 保存到后端
        await updateUserAvatar(data);

        // 通知上传成功
        options.onSuccess({ data });
      },
      // 错误回调
      onError: (error) => {
        console.error("头像上传错误:", error);
        options.onError(new Error(error.message || "上传失败"));
      },
    };

    // 发送上传请求
    await request.post("/file/upload/img", formData, uploadOptions);
  } catch (error) {
    options.onError(error);
    console.error("头像上传过程发生错误:", error);
  }
};

// 更新用户头像信息
const updateUserAvatar = async (avatarPath) => {
  try {
    await request.put(
      `/user/${userForm.id}`,
      { avatar: avatarPath },
      {
        showDefaultMsg: false,
        onSuccess: (data) => {
          // 更新本地用户信息
          const updatedUserInfo = { ...userStore.userInfo, avatar: avatarPath };
          userStore.updateUserInfo(updatedUserInfo);
        },
        onError: (error) => {
          console.error("头像信息保存失败", error);
          ElMessage.error("头像信息保存失败");
        },
      }
    );
  } catch (error) {
    console.error("头像信息保存失败", error);
    ElMessage.error("头像信息保存失败");
    throw error;
  }
};

// 提交用户信息更新
const submitUserInfo = async () => {
  if (!userFormRef.value) return;

  try {
    // 表单验证
    await userFormRef.value.validate();

    await request.put(
      `/user/${userForm.id}`,
      {
        name: userForm.name,
        email: userForm.email,
        phone: userForm.phone,
        sex: userForm.sex,
      },
      {
        showDefaultMsg: false,
        successMsg: "个人信息更新成功!",
        onSuccess: (data) => {
          // 更新本地用户信息
          const updatedUserInfo = {
            ...userStore.userInfo,
            name: userForm.name,
            email: userForm.email,
            phone: userForm.phone,
            sex: userForm.sex,
          };
          userStore.updateUserInfo(updatedUserInfo);
        },
        onError: (error) => {
          console.error("用户信息更新失败", error);
          ElMessage.error("用户信息更新失败");
        },
      }
    );

  } catch (error) {
    console.error("保存个人信息失败", error);
    ElMessage.error("保存个人信息失败");
  }
};

// 提交密码修改
const submitPassword = async () => {
  if (!passwordFormRef.value) return;

  try {
    // 表单验证
    await passwordFormRef.value.validate();

    await request.put(
      `/user/password/${userForm.id}`,
      {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      },
      {
        showDefaultMsg: false,

        onSuccess: (data) => {
          // 清空表单
          passwordForm.oldPassword = "";
          passwordForm.newPassword = "";
          passwordForm.confirmPassword = "";

          // 提示用户重新登录
          ElMessageBox.confirm("密码已修改，需要重新登录", "提示", {
            confirmButtonText: "重新登录",
            cancelButtonText: "取消",
            type: "warning",
          }).then(() => {
            // 清除用户信息并跳转到登录页
            userStore.clearUserInfo();
            window.location.href = "/login";
          });
        },
        onError: (error) => {
          console.error("密码信息保存失败", error);
          ElMessage.error("密码信息保存失败");
        },
      }
    );
  } catch (error) {
    console.error("密码修改失败", error);
    ElMessage.error(error.message || "密码修改失败");
  }
};

// 监听用户表单数据变化
watch(
  userForm,
  (newVal) => {
    console.log("用户表单数据变化:", newVal);
  },
  { deep: true }
);

// 获取当前用户的患者信息
const fetchPatientInfo = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }
  
  patientLoading.value = true;
  try {
    await request.get('/patient/current', {}, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        patientInfo.value = res;
      },
      onError: (err) => {
        // 如果是未找到患者信息的错误，不显示错误提示
        if (err && err.message && err.message.includes('未找到患者信息')) {
          patientInfo.value = null;
        }
      }
    });
  } catch (error) {
    console.error('获取患者信息失败:', error);
  } finally {
    patientLoading.value = false;
  }
};

// 新增患者信息
const handleAddPatient = () => {
  patientDialogType.value = 'add';
  
  // 预填用户信息
  patientForm.userId = userStore.userInfo?.id;
  patientForm.name = userStore.userInfo?.name || '';
  patientForm.sex = userStore.userInfo?.sex || '男';
  patientForm.phone = userStore.userInfo?.phone || '';
  
  patientDialogVisible.value = true;
};

// 编辑患者信息
const handleEditPatient = () => {
  if (!patientInfo.value) return;
  
  patientDialogType.value = 'edit';
  Object.keys(patientForm).forEach(key => {
    if (patientInfo.value[key] !== undefined) {
      patientForm[key] = patientInfo.value[key];
    }
  });
  patientDialogVisible.value = true;
};

// 提交患者表单
const submitPatientForm = () => {
  patientFormRef.value.validate(async (valid) => {
    if (valid) {
      // 确保userId是当前用户ID
      patientForm.userId = userStore.userInfo?.id;
      
      if (patientDialogType.value === 'add') {
        // 新增患者
        await request.post('/patient', patientForm, {
          successMsg: '添加患者信息成功',
          onSuccess: () => {
            patientDialogVisible.value = false;
            fetchPatientInfo();
          }
        });
      } else {
        // 编辑患者
        await request.put(`/patient/${patientForm.id}`, patientForm, {
          successMsg: '更新患者信息成功',
          onSuccess: () => {
            patientDialogVisible.value = false;
            fetchPatientInfo();
          }
        });
      }
    }
  });
};

// 重置患者表单
const resetPatientForm = () => {
  if (patientFormRef.value) {
    patientFormRef.value.resetFields();
  }
  Object.keys(patientForm).forEach(key => {
    if (key === 'sex') {
      patientForm[key] = '男';
    } else {
      patientForm[key] = key === 'id' || key === 'userId' ? null : '';
    }
  });
};

// 组件挂载时获取用户信息
onMounted(() => {
  getUserInfo();
  fetchPatientInfo();
});
</script>

<style>
.profile-container {
  padding: 20px;
  background-color: #f9f7f2;
  min-height: calc(100vh - 60px);
}

.welcome-banner {
  background: linear-gradient(to right, #a8d8ea, #c4e3b2);
  border-radius: 15px;
  padding: 25px 40px;
  margin-bottom: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  animation: fadeIn 0.8s ease-in-out;
}

.banner-content {
  max-width: 800px;
  text-align: center;
  margin: 0 auto;
}

.banner-content h2 {
  color: #3a5463;
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-content h2 .el-icon {
  margin-right: 10px;
  font-size: 28px;
  color: #76c893;
}

.banner-content p {
  color: #5a7385;
  font-size: 16px;
  margin: 0;
}

.profile-card {
  max-width: 1000px;
  margin: 0 auto;
  border-radius: 12px;
  background-color: #ffffff;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  overflow: hidden;
}

.profile-card .el-card__body {
  padding: 20px 30px;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.section-header h3 {
  margin: 0;
  font-size: 20px;
  color: #3a5463;
  font-weight: 600;
}

.section-icon {
  width: 36px;
  height: 36px;
  background-color: #a8d8ea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: white;
  font-size: 18px;
}

.header-actions {
  margin-left: auto;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 30px;
  animation: fadeIn 0.6s ease-in-out;
}

@media (min-width: 768px) {
  .profile-info {
    flex-direction: row;
  }
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background-color: #f7fbff;
  border-radius: 12px;
  border: 1px dashed #a8d8ea;
}

.avatar-container .el-avatar {
  border: 5px solid white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.avatar-uploader {
  text-align: center;
  margin-top: 10px;
}

.avatar-uploader .el-button {
  background-color: #76c893;
  border-color: #76c893;
}

.avatar-uploader .el-button:hover {
  background-color: #62b389;
  border-color: #62b389;
}

.info-form {
  flex: 1;
}

.info-form .el-form-item {
  margin-bottom: 22px;
}

.info-form .el-button {
  background-color: #76c893;
  border-color: #76c893;
}

.info-form .el-button:hover {
  background-color: #62b389;
  border-color: #62b389;
}

.custom-disabled-input input {
  background-color: #f8f9fa;
  color: #909399;
}

.el-tabs {
  margin-top: 20px;
}

.el-tabs__item {
  font-size: 16px;
  color: #5a7385;
}

.el-tabs__item.is-active {
  color: #76c893;
}

.el-tabs__active-bar {
  background-color: #76c893;
}

.patient-info {
  margin-top: 10px;
  animation: fadeIn 0.6s ease-in-out;
}

.patient-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.empty-info {
  display: flex;
  justify-content: center;
  padding: 60px 0;
  animation: fadeIn 0.6s ease-in-out;
}

.empty-info .empty-icon {
  font-size: 80px;
  color: #a8d8ea;
}

.empty-info .el-button {
  background-color: #76c893;
  border-color: #76c893;
}

.empty-info .el-button:hover {
  background-color: #62b389;
  border-color: #62b389;
}

.patient-info .el-descriptions {
  margin-bottom: 20px;
  border-radius: 8px;
}

.patient-info .el-descriptions__label {
  background-color: #f7fbff;
  color: #5a7385;
}

.patient-info .el-descriptions__content {
  padding: 12px 15px;
}

.info-with-icon {
  display: flex;
  align-items: center;
}

.info-with-icon .el-icon {
  margin-right: 8px;
  color: #76c893;
}

.health-info-box, .allergy-info-box {
  display: flex;
  padding: 10px;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.health-info-box {
  border-left: 4px solid #a8d8ea;
}

.allergy-info-box {
  border-left: 4px solid #f8ad9d;
}

.health-info-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 10px;
  font-size: 14px;
}

.health-info-box .health-info-icon {
  color: #a8d8ea;
}

.allergy-info-box .health-info-icon {
  color: #f8ad9d;
}

.health-tips-card {
  margin-top: 25px;
  border-radius: 12px;
  background-color: #f7fbff;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.tips-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.tips-header .el-icon {
  color: #76c893;
  font-size: 20px;
  margin-right: 8px;
}

.tips-header h4 {
  margin: 0;
  color: #3a5463;
  font-size: 16px;
}

.tips-list {
  margin: 0;
  padding-left: 30px;
}

.tips-list li {
  margin-bottom: 8px;
  color: #5a7385;
  line-height: 1.5;
}

.password-form {
  max-width: 500px;
  margin: 0 auto;
}

.password-tips {
  max-width: 500px;
  margin: 30px auto 0;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #f8ad9d;
}

.patient-dialog .el-dialog__header {
  padding: 20px;
  margin: 0;
  background-color: #f7fbff;
  border-bottom: 1px solid #ebeef5;
}

.dialog-content {
  padding: 20px 0;
}

.dialog-header-info {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
}

.dialog-header-info .header-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #a8d8ea;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.dialog-header-info .header-content h3 {
  margin: 0 0 5px 0;
  color: #3a5463;
  font-size: 18px;
}

.dialog-header-info .header-content p {
  margin: 0;
  color: #5a7385;
  font-size: 14px;
}

.patient-dialog .el-form-item {
  margin-bottom: 20px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style> 