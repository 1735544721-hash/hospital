<template>
  <div class="home-container">
    <!-- 轮播图区域 -->
    <div class="banner-section">
      <el-carousel height="400px" indicator-position="outside">
        <el-carousel-item v-for="item in bannerList" :key="item.id">
          <div class="banner-content" :style="{ backgroundImage: `url(${item.imageUrl})` }">
            <div class="banner-text">
              <h2>{{ item.title }}</h2>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 快捷服务区域 -->
    <div class="quick-service-section">
      <div class="section-title">
        <h2>快捷服务</h2>
      </div>
      <div class="service-cards">
        <div class="service-card" v-for="service in quickServices" :key="service.id" @click="handleServiceClick(service.route)">
          <el-icon class="service-icon" :size="40"><component :is="service.icon" /></el-icon>
          <div class="service-title">{{ service.title }}</div>
  
        </div>
      </div>
    </div>

    <!-- 预约指南区域 -->
    <div class="guide-section">
      <div class="section-title">
        <h2>预约挂号指南</h2>
      </div>
      <div class="guide-steps">
        <el-steps :active="1" finish-status="success" simple>
          <el-step title="注册登录" description="注册账号并完善个人信息"></el-step>
          <el-step title="选择科室医生" description="根据症状选择合适的科室和医生"></el-step>
          <el-step title="选择就诊时间" description="选择合适的就诊日期和时间段"></el-step>
          <el-step title="确认预约信息" description="核对并确认预约信息"></el-step>
          <el-step title="按时就诊" description="凭预约号在指定时间前往就诊"></el-step>
        </el-steps>
        <div class="guide-action">
          <el-button type="primary" size="large" round @click="handleServiceClick('/appointment')">立即预约</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, User, Location, Notification } from '@element-plus/icons-vue'

// 正确导入图片
import banner1 from '../../assets/banner/banner1.png'
import banner2 from '../../assets/banner/banner2.png'
import banner3 from '../../assets/banner/banner3.png'
import banner4 from '../../assets/banner/banner4.png'

const router = useRouter()
const baseAPI = import.meta.env.VITE_APP_BASE_API || '/api'

// 轮播图数据
const bannerList = ref([
  {
    id: 1,
    title: '专业医疗团队，贴心守护您的健康',
    description: '我们的医疗团队将以专业的技术和温暖的关怀，为您提供全方位的健康服务',
    imageUrl: banner1
  },
  {
    id: 2,
    title: '舒适就医环境，温馨呵护每一位患者',
    description: '我们精心打造温馨舒适的就医环境，让您在治疗过程中倍感关怀与尊重',
    imageUrl: banner2
  },
  {
    id: 3,
    title: '便捷医疗服务，智慧医院尽在掌握',
    description: '通过智能预约系统，轻松安排就诊时间，减少等待，提升您的就医体验',
    imageUrl: banner3
  },
  {
    id: 4,
    title: '专注健康管理，呵护您的幸福生活',
    description: '我们致力于为您提供全面的健康管理服务，让健康成为您生活的坚实保障',
    imageUrl: banner4
  }
])

// 快捷服务数据
const quickServices = ref([
  {
    id: 1,
    title: '预约挂号',
    description: '在线预约，避免排队等候，轻松安排就诊时间',
    icon: 'Calendar',
    route: '/appointment'
  },
  {
    id: 2,
    title: '就诊记录',
 
    icon: 'Notification',
    route: '/medical-record'
  },
  {
    id: 3,
    title: '科室导航',

    icon: 'Location',
    route: '/department'
  },
  {
    id: 4,
    title: '名医团队',

    icon: 'User',
    route: '/doctor'
  }
])

// 处理服务点击
const handleServiceClick = (route) => {
  router.push(route)
}

onMounted(() => {
  // 页面加载时的初始化操作
})
</script>

<style lang="scss" scoped>
:root {
  --warm-white: #faf7f2;
  --light-beige: #f5efe2;
  --soft-wood: #e8dcc7;
  --soft-green: #a9c8a3;
  --soft-blue: #a3c6d3;
  --text-primary: #5a5a5a;
  --text-secondary: #7a7a7a;
  --shadow-soft: 0 4px 12px rgba(0, 0, 0, 0.05);
  --radius-large: 16px;
  --radius-medium: 12px;
  --radius-small: 8px;
}

.home-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 25px;
  background-color: var(--warm-white);
  font-family: 'Nunito', 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e8dcc7;

  h2 {
    font-size: 24px;
    color: var(--text-primary);
    margin: 0;
    position: relative;
    padding-left: 15px;
    font-weight: 600;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 5px;
      height: 24px;
      background-color: var(--soft-green);
      border-radius: 10px;
    }
  }

  .view-more {
    color: var(--soft-blue);
    text-decoration: none;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s ease;
    
    &:hover {
      color: darken(#a3c6d3, 15%);
    }
  }
}

// 轮播图样式
.banner-section {
  margin-bottom: 35px;
  border-radius: var(--radius-large);
  overflow: hidden;
  box-shadow: var(--shadow-soft);

  .banner-content {
    height: 100%;
    background-size: cover;
    background-position: center;
    display: flex;
    align-items: center;
    padding: 0 60px;
  }

  .banner-text {
    background-color: rgba(255, 255, 255, 0.9);
    padding: 25px 30px;
    border-radius: var(--radius-medium);
    max-width: 500px;
    box-shadow: var(--shadow-soft);

    h2 {
      font-size: 28px;
      margin-bottom: 12px;
      color: var(--text-primary);
      font-weight: 600;
    }

    p {
      font-size: 16px;
      color: var(--text-secondary);
      margin: 0;
      line-height: 1.6;
    }
  }
}

// 快捷服务样式
.quick-service-section {
  margin-bottom: 35px;

  .service-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 25px;
  }

  .service-card {
    background-color: #fff;
    border-radius: var(--radius-medium);
    padding: 25px;
    text-align: center;
    box-shadow: var(--shadow-soft);
    transition: all 0.3s ease;
    cursor: pointer;
    border: 1px solid var(--light-beige);

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
    }

    .service-icon {
      color: var(--soft-green);
      margin-bottom: 18px;
      background-color: rgba(169, 200, 163, 0.1);
      padding: 15px;
      border-radius: 50%;
      display: inline-block;
    }

    .service-title {
      font-size: 19px;
      font-weight: 600;
      margin-bottom: 12px;
      color: var(--text-primary);
    }

  }
}

// 预约指南样式
.guide-section {
  margin-bottom: 35px;
  background-color: var(--light-beige);
  border-radius: var(--radius-medium);
  padding: 30px;
  box-shadow: var(--shadow-soft);

  .guide-steps {
    margin-top: 25px;
  }

  :deep(.el-step__title) {
    font-size: 16px;
    font-weight: 500;
    color: var(--text-primary);
  }

  :deep(.el-step__description) {
    font-size: 14px;
    color: var(--text-secondary);
  }

  :deep(.el-step__icon) {
    background-color: var(--soft-blue);
    color: white;
  }

  .guide-action {
    text-align: center;
    margin-top: 35px;
    
    .el-button {
      border-radius: 30px;
      padding: 12px 30px;
      font-size: 16px;
      font-weight: 600;
      background-color: #5595aa;
      border-color: #5595aa;
      color: white;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      
      &:hover {
        background-color: #457d8f;
        border-color: #457d8f;
      }
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .banner-section {
    .banner-text {
      h2 {
        font-size: 22px;
      }
      
      p {
        font-size: 14px;
      }
    }
  }
  
  .quick-service-section {
    .service-cards {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  
  .guide-section {
    padding: 20px;
    
    :deep(.el-steps) {
      padding: 0;
    }
  }
}

@media (max-width: 576px) {
  .home-container {
    padding: 15px;
  }

  .quick-service-section {
    .service-cards {
      grid-template-columns: 1fr;
    }
  }
  
  .banner-section {
    .banner-content {
      padding: 0 20px;
    }
    
    .banner-text {
      padding: 15px;
      max-width: 100%;
    }
  }
}
</style>


