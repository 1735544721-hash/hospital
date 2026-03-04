<template>
  <div class="auth-container">
    <div class="auth-background">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
      <div class="bg-pattern"></div>
    </div>
    
    <div class="auth-box">
      <div class="auth-header" v-if="showHeader">
        <div class="logo">
          <el-icon size="42" color="#68a86d"><FirstAidKit /></el-icon>
        </div>
        <h1 class="title">康泰医疗</h1>
 
      </div>
      
      <el-form :model="formData" :rules="rules" ref="formRef" class="auth-form">
        <slot name="form-items"></slot>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit" class="auth-button">
            {{ submitText }}
          </el-button>
        </el-form-item>
        
        <div class="auth-links">
          <slot name="auth-links"></slot>
        </div>
      </el-form>
      
 
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { FirstAidKit, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  formData: {
    type: Object,
    required: true
  },
  rules: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  submitText: {
    type: String,
    default: '提交'
  },
  showHeader: {
    type: Boolean,
    default: true
  }
})

const formRef = ref(null)

const emit = defineEmits(['submit'])

const handleSubmit = () => {
  formRef.value.validate(valid => {
    if (valid) {
      emit('submit', formRef)
    }
  })
}

defineExpose({
  formRef
})
</script>

<style lang="scss" scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f9f7f2 0%, #f0f5e9 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.auth-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
  opacity: 0.7;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  
  &.circle-1 {
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(104, 168, 109, 0.2) 0%, rgba(104, 168, 109, 0) 70%);
    top: -100px;
    right: -50px;
  }
  
  &.circle-2 {
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(163, 198, 211, 0.15) 0%, rgba(163, 198, 211, 0) 70%);
    bottom: -200px;
    left: -100px;
  }
  
  &.circle-3 {
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(246, 223, 186, 0.2) 0%, rgba(246, 223, 186, 0) 70%);
    top: 30%;
    left: 10%;
  }
}

.bg-shape {
  position: absolute;
  
  &.shape-1 {
    width: 150px;
    height: 150px;
    background-color: rgba(104, 168, 109, 0.05);
    border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
    top: 20%;
    right: 15%;
    animation: floating 15s infinite ease-in-out;
  }
  
  &.shape-2 {
    width: 100px;
    height: 100px;
    background-color: rgba(163, 198, 211, 0.07);
    border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
    bottom: 20%;
    right: 30%;
    animation: floating 12s infinite ease-in-out 2s;
  }
  
  &.shape-3 {
    width: 120px;
    height: 120px;
    background-color: rgba(246, 223, 186, 0.07);
    border-radius: 40% 60% 60% 40% / 40% 60% 40% 60%;
    top: 60%;
    left: 20%;
    animation: floating 20s infinite ease-in-out 1s;
  }
}

.bg-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(rgba(104, 168, 109, 0.07) 2px, transparent 2px),
                    radial-gradient(rgba(163, 198, 211, 0.05) 2px, transparent 2px);
  background-size: 30px 30px, 40px 40px;
  background-position: 0 0, 15px 15px;
  animation: fadePattern 20s infinite alternate;
}

@keyframes floating {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  25% {
    transform: translate(10px, 10px) rotate(5deg);
  }
  50% {
    transform: translate(5px, -5px) rotate(0deg);
  }
  75% {
    transform: translate(-5px, 8px) rotate(-5deg);
  }
  100% {
    transform: translate(0, 0) rotate(0deg);
  }
}

@keyframes fadePattern {
  0% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 0.3;
  }
}

.auth-box {
  width: 420px;
  max-width: 100%;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06),
              0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  padding: 40px;
  position: relative;
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
  
  .logo {
    display: flex;
    justify-content: center;
    margin-bottom: 16px;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      width: 70px;
      height: 70px;
      background: rgba(104, 168, 109, 0.1);
      border-radius: 50%;
      z-index: -1;
    }
  }
  
  .title {
    font-size: 28px;
    color: #455a64;
    margin: 0 0 8px;
    font-weight: 600;
    background: linear-gradient(to right, #3a5463, #68a86d);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    text-align: center;
  }
  

}

.auth-form {
  margin-bottom: 20px;
  
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 0 0 1px #e0e7e1 inset;
    padding: 0 15px;
    background-color: rgba(255, 255, 255, 0.7);
    transition: all 0.3s ease;
    
    &:hover, &:focus {
      box-shadow: 0 0 0 1px #68a86d inset;
      background-color: rgba(255, 255, 255, 0.9);
    }
  }
  
  :deep(.el-input__inner) {
    height: 45px;
  }
  
  :deep(.el-form-item__label) {
    color: #546e7a;
    font-weight: 500;
  }
  
  :deep(.el-form-item__error) {
    color: #e57373;
  }
  
  :deep(.el-textarea__inner) {
    border-radius: 12px;
    padding: 10px 15px;
    background-color: rgba(255, 255, 255, 0.7);
    
    &:hover, &:focus {
      background-color: rgba(255, 255, 255, 0.9);
    }
  }
  
  :deep(.el-select) {
    width: 100%;
  }
  
  :deep(.el-date-editor) {
    width: 100%;
  }
}

.auth-button {
  width: 100%;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  background-color: #68a86d;
  border-color: #68a86d;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      90deg,
      rgba(255, 255, 255, 0) 0%,
      rgba(255, 255, 255, 0.2) 50%,
      rgba(255, 255, 255, 0) 100%
    );
    transition: all 0.5s ease;
  }
  
  &:hover, &:focus {
    background-color: #589a5e;
    border-color: #589a5e;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(104, 168, 109, 0.2);
    
    &::before {
      left: 100%;
    }
  }
}

.auth-links {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #78909c;
  
  a {
    color: #68a86d;
    text-decoration: none;
    font-weight: 500;
    transition: color 0.3s ease;
    margin: 0 5px;
    
    &:hover {
      color: #589a5e;
    }
  }
}



@media (min-width: 992px) {
  .auth-container {
    padding: 40px;
  }
  
  .auth-box {
    width: 480px;
  }
}

@media (max-width: 767px) {
  .auth-box {
    padding: 30px 20px;
  }
  
  .bg-circle, .bg-shape {
    opacity: 0.5;
  }
}
</style> 