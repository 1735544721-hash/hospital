<template>
  <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
    <div class="logo">
      <span class="logo-icon">
        <el-icon><FirstAidKit /></el-icon>
      </span>
      <span class="logo-text" v-show="!isCollapsed">康泰医疗系统</span>
    </div>
    <div class="menu-wrapper">
      <el-menu :default-active="activeMenu" :collapse="isCollapsed" :collapse-transition="false" mode="vertical" class="sidebar-menu"
        text-color="#e0f2f1" active-text-color="#ffffff" router>
        
        <!-- 动态生成菜单项 -->
        <el-menu-item v-for="item in filteredMenuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
        
        <!-- 系统管理子菜单 -->
        <el-sub-menu index="/back/system" v-if="systemMenuItems.length > 0">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          
          <el-menu-item v-for="item in systemMenuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { 
  HomeFilled, 
  User, 
  UserFilled,
  OfficeBuilding,
  FirstAidKit,
  Calendar,
  Tickets,
  Document,
  List,
  Setting,
  Menu
} from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 获取当前用户角色
const userRole = computed(() => userStore.role)

const menuItems = [
  {
    title: '首页',
    icon: 'HomeFilled',
    path: '/back/dashboard',
    roles: ['ADMIN', 'DOCTOR', 'STAFF']
  },
  {
    title: '用户管理',
    icon: 'User',
    path: '/back/user',
    roles: ['ADMIN']
  },
  {
    title: '患者管理',
    icon: 'UserFilled',
    path: '/back/patient',
    roles: ['ADMIN', 'STAFF', 'DOCTOR']
  },
  {
    title: '个人信息',
    icon: 'UserFilled',
    path: '/back/profile',
    roles: ['ADMIN', 'STAFF', 'DOCTOR']
  }
]

// 系统管理子菜单项
const systemMenuItems = computed(() => {
  return [
    {
      title: '科室管理',
      icon: 'OfficeBuilding',
      path: '/back/department',
      roles: ['ADMIN', 'STAFF']
    },
    {
      title: '医生管理',
      icon: 'UserFilled',
      path: '/back/doctor',
      roles: ['ADMIN', 'STAFF']
    },
    {
      title: '药品管理',
      icon: 'FirstAidKit',
      path: '/back/medicine',
      roles: ['ADMIN', 'STAFF']
    },
    {
      title: '药品分类管理',
      icon: 'Menu',
      path: '/back/medicine-category',
      roles: ['ADMIN']
    },
    {
      title: '排班管理',
      icon: 'Calendar',
      path: '/back/schedule',
      roles: ['ADMIN', 'STAFF', 'DOCTOR']
    },
    {
      title: '预约管理',
      icon: 'Tickets',
      path: '/back/appointment',
      roles: ['ADMIN', 'STAFF', 'DOCTOR']
    },
    {
      title: '就诊记录管理',
      icon: 'Document',
      path: '/back/medical-record',
      roles: ['ADMIN', 'STAFF', 'DOCTOR']
    },
    {
      title: '处方管理',
      icon: 'List',
      path: '/back/prescription',
      roles: ['ADMIN', 'STAFF', 'DOCTOR']
    }
  ].filter(item => item.roles.includes(userRole.value))
})

// 根据用户角色过滤菜单项
const filteredMenuItems = computed(() => {
  return menuItems.filter(item => item.roles.includes(userRole.value))
})
</script>

<style lang="scss" scoped>
// 医疗主题色变量
$primary-color: #3cb2b8; // 医疗蓝绿色
$primary-dark: #2a8f95; // 深蓝绿色
$primary-light: #71d6dc; // 浅蓝绿色
$secondary-color: #5cbbaf; // 辅助色
$bg-dark: #1e4e52; // 背景深色
$bg-light: #2a6168; // 背景浅色
$text-light: #e0f2f1; // 文字浅色
$active-indicator: #71d6dc; // 激活指示器颜色

.sidebar-container {
  height: 100%; 
  min-height: 100vh;
  background: linear-gradient(180deg, $bg-dark 0%, $bg-light 100%);
  display: flex;
  flex-direction: column;
  width: 220px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.is-collapsed {
    width: 64px;
    
    .logo {
      padding: 0;
      justify-content: center;
      
      .logo-icon {
        margin: 0;
      }
    }

    :deep(.el-menu) {
      .el-sub-menu__title span,
      .el-menu-item span {
        opacity: 0;
        transition: opacity 0.2s;
      }
    }
  }
  
  .logo {
    height: 60px;
    flex-shrink: 0;
    line-height: 60px;
    text-align: center;
    background: rgba(255, 255, 255, 0.08);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    align-items: center;
    padding: 0 16px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    .logo-icon {
      font-size: 24px;
      margin-right: 8px;
      transition: margin 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      color: $primary-light;
    }
    
    .logo-text {
      color: #ffffff;
      font-size: 18px;
      font-weight: 600;
      white-space: nowrap;
      opacity: 1;
      transition: opacity 0.2s;
    }
  }

  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  :deep(.sidebar-menu) {
    border: none;
    background: transparent;
    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    .el-menu-item, .el-sub-menu__title {
      height: 50px;
      line-height: 50px;
      color: rgba(255, 255, 255, 0.7);
      background: transparent;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      span {
        opacity: 1;
        transition: opacity 0.3s;
      }
      
      &:hover {
        background: rgba($primary-color, 0.15) !important;
        color: #ffffff;
      }
    }

    .el-menu-item.is-active {
      background: rgba($primary-color, 0.25) !important;
      color: #ffffff !important;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        width: 3px;
        height: 100%;
        background: $active-indicator;
      }
    }

    .el-sub-menu {
      &.is-opened {
        > .el-sub-menu__title {
          color: #ffffff;
          background: rgba($primary-color, 0.2) !important;
        }
      }

      .el-menu {
        background: rgba($bg-dark, 0.4);
        
        .el-menu-item {
          background: transparent;
          
          &:hover {
            background: rgba($primary-color, 0.15) !important;
          }
          
          &.is-active {
            background: rgba($primary-color, 0.25) !important;
          }
        }
      }
    }

    // 折叠状态下的弹出菜单样式
    &.el-menu--collapse {
      .el-sub-menu {
        &.is-opened {
          > .el-sub-menu__title {
            background: transparent !important;
          }
        }
      }
    }
  }

  .el-icon {
    vertical-align: middle;
    margin-right: 5px;
    width: 24px;
    text-align: center;
    color: inherit;
  }

  span {
    vertical-align: middle;
  }
}
</style> 