<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()

const form = ref({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入身份证号和密码')
    return
  }
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('username', form.value.username)
    params.append('password', form.value.password)

    const res = await api.post('/api/login', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })

    if (res.data.success) {
      sessionStorage.setItem('user', JSON.stringify(res.data.user))
      ElMessage.success('登录成功')
      router.push(res.data.user.role === 'admin' ? '/admin/home' : '/student/home')
    } else {
      ElMessage.error(res.data.message || '登录失败')
    }
  } catch {
    ElMessage.error('身份证号或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-bg">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">📚</div>
        <h1>大学英语四级考试</h1>
        <p>成绩管理系统</p>
      </div>

      <el-form @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="身份证号">
          <el-input v-model="form.username" placeholder="请输入身份证号" size="large" clearable />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width:100%;margin-top:8px">
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-bg { background: linear-gradient(135deg, #7ab3c8 0%, #a8c8d8 100%); min-height: 100vh; display: flex; justify-content: center; align-items: center; }
.login-card { background: #fff; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); width: 400px; padding: 40px; }
.login-header { text-align: center; margin-bottom: 32px; }
.logo-icon { font-size: 48px; margin-bottom: 12px; }
.login-header h1 { font-size: 22px; color: #2c5a6e; margin: 0 0 8px; }
.login-header p { color: #8ea8b5; font-size: 13px; margin: 0; }
</style>
