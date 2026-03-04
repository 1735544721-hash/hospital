<template>
    <div class="frontend-layout">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="container">
          <div class="logo-container">
            <div class="logo">
              <el-icon color="#76c893" :size="32"><FirstAidKit /></el-icon>
              <span class="logo-text">康泰医疗</span>
            </div>
   
          </div>
          
          <el-menu
            :default-active="activeIndex"
            mode="horizontal"
            :ellipsis="false"
            class="nav-menu"
            background-color="transparent"
            text-color="#5a7385"
            active-text-color="#76c893"
            router
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/department">
              <el-icon><OfficeBuilding /></el-icon>
              <span>科室介绍</span>
            </el-menu-item>
            <el-menu-item index="/doctor">
              <el-icon><User /></el-icon>
              <span>医生介绍</span>
            </el-menu-item>
            <el-menu-item index="/appointment" v-if="isLoggedIn">
              <el-icon><Calendar /></el-icon>
              <span>预约挂号</span>
            </el-menu-item>
            <el-menu-item index="/my-appointments" v-if="isLoggedIn">
              <el-icon><Tickets /></el-icon>
              <span>我的预约</span>
            </el-menu-item>
            <el-menu-item index="/medical-record" v-if="isLoggedIn">
              <el-icon><DocumentChecked /></el-icon>
              <span>就诊记录</span>
            </el-menu-item>
            <el-menu-item index="/profile" v-if="isLoggedIn">
              <el-icon><UserFilled /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
          </el-menu>
          
          <div class="user-actions">
            <template v-if="isLoggedIn">
              <el-dropdown trigger="click">
                <span class="user-profile">
                
                  <el-avatar :size="36" :src="userAvatar" />
                  <span class="username">{{ userName }}</span>
                  <el-icon><CaretBottom /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="router.push('/profile')">
                      <el-icon><User /></el-icon>个人中心
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" round @click="router.push('/login')">
                <el-icon><Key /></el-icon>登录
              </el-button>
              <el-button round @click="router.push('/register')">
                <el-icon><User /></el-icon>注册
              </el-button>
            </template>
          </div>
        </div>
      </el-header>
  
      <!-- 主要内容区域 -->
      <el-main class="main-content">
        <div class="container">
        <router-view />
        </div>
      </el-main>
  
      <!-- 页脚 -->
      <el-footer class="footer">
        <div class="container">
          <div class="copyright">
            <p>&copy; {{ new Date().getFullYear() }} 康泰医疗信息系统 版权所有</p>
          </div>
        </div>
      </el-footer>
    </div>
  </template>
  
  <script setup>
  import { computed, ref, watchEffect } from 'vue'
  import { useUserStore } from '@/store/user'
  import { useRouter, useRoute } from 'vue-router'
  import { 
    User, 
    SwitchButton, 
    CaretBottom, 
    HomeFilled,
    Calendar,
    Tickets,
    DocumentChecked,
    UserFilled,
    FirstAidKit,
    OfficeBuilding,
    Key
  } from '@element-plus/icons-vue'
  import { ElMessage } from 'element-plus'
  
  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
      const baseAPI = import.meta.env.VITE_APP_BASE_API || '/api'
  const isLoggedIn = computed(() => !!userStore.token)
  const userName = computed(() => userStore.userInfo?.name || userStore.userInfo?.username || '用户')
  const userAvatar = computed(() => {
    if (!userStore.userInfo?.avatar) return ''
    if (userStore.userInfo.avatar.startsWith('http')) return userStore.userInfo.avatar

    return baseAPI + userStore.userInfo.avatar
  })
  
  const activeIndex = ref('/')
  
  watchEffect(() => {
    activeIndex.value = route.path
  })
  
  const handleLogout = () => {
    userStore.clearUserInfo()
    ElMessage.success('退出登录成功')
    router.push('/login')
  }
  </script>
  
  <style scoped>
  .frontend-layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f9f7f2;
  }
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
  }
  
  .header {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    background: linear-gradient(to right, #ffffff, #f7fbff);
    position: sticky;
    top: 0;
    z-index: 1000;
    padding: 0 20px;
    height: 70px;
    line-height: 70px;
    transition: all 0.3s ease;
  }
  
  .header .container {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
  }
  
  .logo-container {
    display: flex;
    flex-direction: column;
    line-height: normal;
  }
  
  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .logo-text {
    font-size: 24px;
    font-weight: bold;
    color: #3a5463;
    background: linear-gradient(to right, #3a5463, #76c893);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  

  
  .nav-menu {
    flex-grow: 1;
    justify-content: center;
    border-bottom: none;
  }
  
  .nav-menu :deep(.el-menu-item) {
    font-size: 16px;
    height: 70px;
    line-height: 70px;
    padding: 0 20px;
    font-weight: 500;
  }
  
  .nav-menu :deep(.el-menu-item.is-active) {
    font-weight: bold;
    color: #76c893;
    background-color: rgba(118, 200, 147, 0.1);
    border-bottom: 3px solid #76c893;
  }
  
  .nav-menu :deep(.el-menu-item:hover) {
    background-color: rgba(118, 200, 147, 0.05);
  }
  
  .nav-menu :deep(.el-menu-item .el-icon) {
    margin-right: 6px;
    font-size: 18px;
    vertical-align: middle;
  }
  
  .user-actions {
    display: flex;
    gap: 12px;
    align-items: center;
  }
  
  .user-profile {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 20px;
    transition: all 0.3s ease;
    background-color: rgba(168, 216, 234, 0.1);
  }
  
  .user-profile:hover {
    background-color: rgba(168, 216, 234, 0.2);
  }
  
  .username {
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #3a5463;
    font-weight: 500;
  }
  
  .user-actions :deep(.el-button) {
    border-radius: 20px;
    font-weight: 500;
  }
  
  .user-actions :deep(.el-button--primary) {
    background-color: #76c893;
    border-color: #76c893;
  }
  
  .user-actions :deep(.el-button--primary:hover) {
    background-color: #62b389;
    border-color: #62b389;
  }
  
  .user-actions :deep(.el-button .el-icon) {
    margin-right: 4px;
  }
  
  .main-content {
    flex: 1;
    padding-top: 2rem;
    padding-bottom: 2rem;
    background-color: #f9f7f2;
  }
  
  .footer {
    background: linear-gradient(to right, #3a5463, #4b6e83);
    color: #fff;
    padding: 20px 0;
  }
  
  .copyright {
    text-align: center;
    color: #a6a9ad;
    font-size: 14px;
  }
  </style>