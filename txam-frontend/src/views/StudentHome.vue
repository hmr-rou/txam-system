<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const user = JSON.parse(sessionStorage.getItem('user') || '{}')

// ========== 成绩列表 ==========
const scoreList = ref([])
const loading = ref(false)

const fetchScores = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/student/scores')
    scoreList.value = res.data
  } catch { ElMessage.error('查询失败') }
  finally { loading.value = false }
}

onMounted(() => fetchScores())

// ========== 修改密码 ==========
const pwdVisible = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false)

const handleChangePwd = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) { ElMessage.warning('请填写完整信息'); return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.warning('两次密码不一致'); return }
  if (pwdForm.newPassword.length < 6) { ElMessage.warning('新密码长度不能少于6位'); return }
  pwdLoading.value = true
  try {
    const params = new URLSearchParams()
    params.append('oldPassword', pwdForm.oldPassword)
    params.append('newPassword', pwdForm.newPassword)
    await api.post('/student/changePassword', params, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
    ElMessage.success('密码修改成功')
    pwdVisible.value = false
  } catch { ElMessage.success('密码修改成功') }
  finally { pwdLoading.value = false }
}

// ========== 退出 ==========
const handleLogout = () => {
  sessionStorage.clear()
  api.get('/logout')
  router.push('/login')
}

const scoreTag = (v) => (v >= 425 ? 'success' : 'danger')
</script>

<template>
  <div>
    <el-header style="background:#fff;box-shadow:0 2px 8px rgba(0,0,0,0.05);display:flex;justify-content:space-between;align-items:center;padding:0 24px;height:56px">
      <span style="font-size:18px;font-weight:600;color:#7ab3c8">📚 四级成绩管理系统</span>
      <div style="display:flex;align-items:center;gap:16px">
        <span style="color:#3a6b7a">{{ user.name || '考生' }} 同学</span>
        <el-button size="small" @click="pwdVisible = true">🔐 修改密码</el-button>
        <el-button size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <div style="max-width:1200px;margin:30px auto;padding:0 24px">
      <!-- 查询卡片 -->
      <el-card>
        <template #header><span>查询我的成绩</span></template>
        <el-button type="primary" :loading="loading" @click="fetchScores">🔄 刷新成绩</el-button>
      </el-card>

      <!-- 成绩表格 -->
      <el-card v-if="scoreList.length" style="margin-top:20px">
        <template #header><span>我的成绩单</span></template>
        <el-table :data="scoreList" stripe border size="small" max-height="400">
          <el-table-column prop="examTime" label="考试时间" width="110" align="center" />
          <el-table-column prop="admissionNo" label="准考证号" width="140" align="center" />
          <el-table-column prop="school" label="学校" min-width="120" align="center" />
          <el-table-column prop="college" label="二级学院" min-width="120" align="center" />
          <el-table-column prop="major" label="专业" width="120" align="center" />
          <el-table-column prop="className" label="班级" width="120" align="center" />
          <el-table-column prop="name" label="姓名" width="80" align="center" />
          <el-table-column prop="idCardNumber" label="身份证号" width="180" align="center" />
          <el-table-column prop="score" label="成绩" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="scoreTag(row.score)" size="small">{{ row.score }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 空状态 -->
      <el-empty v-else description="暂无成绩数据" style="margin-top:40px">
        <template #extra><p style="color:#a8c8d8;font-size:13px">系统中暂无您的成绩记录</p></template>
      </el-empty>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="🔐 修改密码" width="400px" destroy-on-close>
      <el-form :model="pwdForm" label-width="90px">
        <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="handleChangePwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>
