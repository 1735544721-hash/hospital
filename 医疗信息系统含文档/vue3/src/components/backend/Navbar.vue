<template>
  <div class="navbar">
    <div class="left-menu">
      <el-icon class="hamburger" :size="20" @click="toggleSidebar">
        <component :is="appStore.sidebarCollapsed ? Expand : Fold" />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <div class="right-menu">
      <div class="right-menu-item health-stats" title="系统健康状态">
        <el-icon><DataAnalysis /></el-icon>
        <span>系统正常</span>
      </div>
      
      <div class="right-menu-item" @click="toggleFullScreen" title="全屏切换">
        <el-icon :size="20">
          <component :is="isFullscreen ? Aim : FullScreen" />
        </el-icon>
      </div>
      
      <el-dropdown trigger="click">
        <div class="avatar-wrapper">
          <el-avatar :size="32" :src="avatarUrl" class="user-avatar">
            {{ userInfo?.name?.charAt(0)?.toUpperCase() || userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
          <span class="user-name">{{ userInfo?.name || userInfo?.username || '用户' }}</span>
          <el-icon class="el-icon--right">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/back/profile')">
              <el-icon><UserFilled /></el-icon>
              个人信息
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { ElMessageBox } from 'element-plus'
import { 
  Expand, 
  Fold, 
  ArrowDown, 
  UserFilled, 
  SwitchButton, 
  FullScreen, 
  Aim,
  DataAnalysis
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const baseAPI = import.meta.env.VITE_API_BASE_URL || '/api'
const userInfo = computed(() => userStore.userInfo)
const isFullscreen = ref(false)

const toggleSidebar = () => {
  appStore.toggleSidebar()
}
const avatarUrl = computed(() => {
  const url = userStore.userInfo?.avatar
  if (!url) return ''
  if(url.startsWith("https")||url.startsWith("http")) return url
  return baseAPI + userStore.userInfo.avatar
})
const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
}

// 监听全屏状态变化
const fullscreenChangeHandler = () => {
  isFullscreen.value = !!document.fullscreenElement
}

document.addEventListener('fullscreenchange', fullscreenChangeHandler)

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('fullscreenchange', fullscreenChangeHandler)
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
// 医疗主题色变量
$primary-color: #3cb2b8; // 医疗蓝绿色
$primary-dark: #2a8f95; // 深蓝绿色
$primary-light: #71d6dc; // 浅蓝绿色
$secondary-color: #5cbbaf; // 辅助色
$bg-dark: #1e4e52; // 背景深色
$text-color: #2c3e50; // 主要文字颜色
$text-light: #5c6b73; // 次要文字颜色
$border-color: #e9f7f7; // 边框颜色
$success-color: #4caf50; // 成功色

.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: linear-gradient(to right, #ffffff, #f1fafa);
  box-shadow: 0 2px 10px rgba($primary-dark, 0.08);
  z-index: 10;
  border-bottom: 1px solid $border-color;

  .left-menu {
    display: flex;
    align-items: center;
    gap: 16px;

    .hamburger {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      padding: 8px;
      border-radius: 8px;
      color: $primary-dark;
      height: 32px;
      width: 32px;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba($primary-light, 0.12);
        transform: scale(1.05);
      }
    }

    :deep(.el-breadcrumb__inner) {
      color: $text-color;
      line-height: 32px;
      font-weight: 500;
      
      &.is-link {
        color: $text-light;
        
        &:hover {
          color: $primary-color;
        }
      }
    }
  }

  .right-menu {
    display: flex;
    align-items: center;
    gap: 12px;

    .right-menu-item {
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: $text-light;
      border-radius: 8px;
      transition: all 0.3s;
      height: 36px;
      min-width: 36px;
      padding: 0 8px;
      
      &:hover {
        background: rgba($primary-light, 0.12);
        color: $primary-dark;
      }
      
      &.health-stats {
        background-color: rgba($success-color, 0.1);
        color: $success-color;
        padding: 0 12px;
        
        .el-icon {
          margin-right: 6px;
        }
        
        span {
          font-size: 13px;
          white-space: nowrap;
        }
        
        &:hover {
          background-color: rgba($success-color, 0.15);
          color: $success-color;
        }
      }
    }
    
    .avatar-wrapper {
      display: flex;
      align-items: center;
      padding: 4px 12px;
      height: 36px;
      cursor: pointer;
      border-radius: 18px;
      transition: all 0.3s ease;
      background: rgba($primary-light, 0.08);
      
      &:hover {
        background: rgba($primary-light, 0.15);
      }
      
      .user-avatar {
        background: linear-gradient(135deg, $primary-color, $secondary-color);
        color: white;
        font-weight: 500;
      }
      
      .user-name {
        margin: 0 8px;
        font-size: 14px;
        color: $text-color;
        font-weight: 500;
        max-width: 120px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .el-icon {
        color: $text-light;
        display: flex;
        align-items: center;
      }
    }
  }

  :deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    height: 40px;
    color: $text-color;
    
    &:hover {
      background-color: rgba($primary-light, 0.1);
      color: $primary-color;
    }
    
    .el-icon {
      margin-right: 4px;
      display: flex;
      align-items: center;
      color: $primary-color;
    }
  }
}
</style> 