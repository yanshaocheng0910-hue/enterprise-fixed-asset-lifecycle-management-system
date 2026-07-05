<template>
  <div class="login-page">
    <div class="login-left">
      <div class="left-content">
        <h1 class="system-title">国企固定资产<br/>全生命周期管理系统</h1>
        <p class="system-desc">资产入库、领用、调拨、维修、盘点、折旧与报废全流程管理</p>
        <div class="feature-list">
          <div class="feature-item">▶ 资产台账管理</div>
          <div class="feature-item">▶ 全生命周期流转</div>
          <div class="feature-item">▶ 折旧计提与报表</div>
          <div class="feature-item">▶ 扫码盘点与异常追踪</div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <h2 class="login-title">系统登录</h2>
        <p class="login-hint">请输入账号密码登录系统</p>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" style="width:100%" native-type="submit">登 录</el-button>
          </el-form-item>
        </el-form>
        <p class="login-tip">默认账号：admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  width: 100%;
}
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #173B57 0%, #1A4F6E 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.left-content {
  max-width: 420px;
  padding: 40px;
}
.system-title {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 16px;
}
.system-desc {
  font-size: 15px;
  opacity: 0.8;
  line-height: 1.6;
  margin-bottom: 40px;
}
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.feature-item {
  font-size: 14px;
  opacity: 0.7;
}
.login-right {
  width: 440px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 320px;
}
.login-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}
.login-hint {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 32px;
}
.login-tip {
  text-align: center;
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 12px;
}
</style>
