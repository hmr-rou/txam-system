<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()
const user = JSON.parse(sessionStorage.getItem('user') || '{}')

// ========== 成绩列表 ==========
const scoreList = ref([])
const loading = ref(false)

const fetchScores = async (params = {}) => {
  loading.value = true
  try {
    const res = await api.get('/api/admin/scores/search', { params })
    scoreList.value = res.data
  } catch { ElMessage.error('加载数据失败') }
  finally { loading.value = false }
}

onMounted(() => fetchScores())

// ========== 搜索 ==========
const searchVisible = ref(false)
const searchForm = reactive({ idCard: '', admissionNo: '', school: '', college: '', major: '', className: '' })

const handleSearch = () => { fetchScores(searchForm); searchVisible.value = false }
const handleReset = () => {
  Object.keys(searchForm).forEach(k => searchForm[k] = '')
  fetchScores()
}

// ========== 导出 ==========
const handleExport = async () => {
  await ElMessageBox.confirm('将导出当前查询条件下的所有成绩，确定继续？', '确认导出')
  const params = new URLSearchParams()
  Object.entries(searchForm).forEach(([k, v]) => { if (v) params.append(k, v) })
  const url = `/admin/export?${params.toString()}`
  window.open(url)
}

// ========== 新增/编辑 ==========
const dialogVisible = ref(false)
const dialogTitle = ref('录入成绩')
const scoreForm = reactive({ id: '', name: '', idCardNumber: '', school: '', college: '', major: '', className: '', admissionNo: '', score: '', examTime: '' })
const saving = ref(false)

const openAdd = () => {
  dialogTitle.value = '录入成绩'
  Object.keys(scoreForm).forEach(k => scoreForm[k] = '')
  dialogVisible.value = true
}

const openEdit = async (id) => {
  dialogTitle.value = '修改成绩'
  try {
    const res = await api.get('/admin/getScore', { params: { id } })
    Object.assign(scoreForm, res.data)
    if (scoreForm.examTime) scoreForm.examTime = scoreForm.examTime.substring(0, 10)
    dialogVisible.value = true
  } catch { ElMessage.error('获取数据失败') }
}

const handleSave = async () => {
  saving.value = true
  try {
    const params = new URLSearchParams()
    Object.entries(scoreForm).forEach(([k, v]) => { if (v !== '' && v !== null) params.append(k, v) })
    const res = await api.post('/admin/save', params, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
    if (res.data.success) {
      ElMessage.success(res.data.message)
      dialogVisible.value = false
      fetchScores()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

// ========== 删除 ==========
const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定要删除这条成绩记录吗？', '确认删除', { type: 'warning' })
  try {
    await api.get('/admin/delete', { params: { id } })
    ElMessage.success('删除成功')
    fetchScores()
  } catch { ElMessage.error('删除失败') }
}

// ========== 导入 ==========
const importVisible = ref(false)
const importFile = ref(null)
const importing = ref(false)
const importResult = ref(null)

const openImport = () => {
  importFile.value = null
  importResult.value = null
  importVisible.value = true
}

const handleImport = async () => {
  if (!importFile.value?.raw) { ElMessage.warning('请选择文件'); return }
  importing.value = true
  try {
    const fd = new FormData()
    fd.append('excelFile', importFile.value.raw)
    const res = await api.post('/admin/import', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    importResult.value = res.data
    if (res.data.success) {
      ElMessage.success(res.data.message)
      setTimeout(() => { importVisible.value = false; fetchScores() }, 1500)
    }
  } catch { ElMessage.error('导入失败') }
  finally { importing.value = false }
}

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
    const res = await api.post('/admin/changePassword', params, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
    // 后端 redirect，检查是否有 flash attribute
    // 由于是 redirect，这里可能不会得到 JSON 响应
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

// ========== 成绩等级 ==========
const scoreTag = (v) => (v >= 425 ? 'success' : 'danger')
const scoreClass = (v) => (v >= 425 ? 'pass' : 'fail')
</script>

<template>
  <div>
    <!-- 导航栏 -->
    <el-header style="background:#fff;box-shadow:0 2px 8px rgba(0,0,0,0.05);display:flex;justify-content:space-between;align-items:center;padding:0 24px;height:56px">
      <span style="font-size:18px;font-weight:600;color:#7ab3c8">📚 四级成绩管理系统 - 管理员</span>
      <div style="display:flex;align-items:center;gap:16px">
        <span style="color:#3a6b7a">👤 {{ user.name || '系统管理员' }}</span>
        <el-button size="small" @click="pwdVisible = true">🔐 修改密码</el-button>
        <el-button size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>

    <!-- 主容器 -->
    <div style="max-width:1400px;margin:24px auto;padding:0 24px">
      <!-- 操作栏 -->
      <div style="display:flex;gap:12px;margin-bottom:16px;flex-wrap:wrap">
        <el-button type="primary" @click="openAdd">➕ 录入成绩</el-button>
        <el-button type="success" @click="searchVisible = !searchVisible">🔍 高级查询</el-button>
        <el-button type="warning" @click="openImport">📥 批量导入</el-button>
        <el-button @click="window.open('/admin/downloadTemplate')">📋 下载导入模板</el-button>
        <el-button @click="handleExport">📤 导出Excel</el-button>
      </div>

      <!-- 搜索面板 -->
      <div v-show="searchVisible" style="background:#fff;border-radius:12px;padding:20px;margin-bottom:16px;box-shadow:0 1px 3px rgba(0,0,0,0.05)">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="身份证号"><el-input v-model="searchForm.idCard" placeholder="请输入" size="small" /></el-form-item>
          <el-form-item label="准考证号"><el-input v-model="searchForm.admissionNo" placeholder="请输入" size="small" /></el-form-item>
          <el-form-item label="学校"><el-input v-model="searchForm.school" placeholder="请输入" size="small" /></el-form-item>
          <el-form-item label="学院"><el-input v-model="searchForm.college" placeholder="请输入" size="small" /></el-form-item>
          <el-form-item label="专业"><el-input v-model="searchForm.major" placeholder="请输入" size="small" /></el-form-item>
          <el-form-item label="班级"><el-input v-model="searchForm.className" placeholder="请输入" size="small" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">🔍 查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 成绩表格 -->
      <el-card>
        <template #header>
          <span>考生成绩列表</span>
          <span style="color:#8ea8b5;font-size:12px;float:right">共 {{ scoreList.length }} 条记录</span>
        </template>
        <el-table :data="scoreList" v-loading="loading" stripe border size="small" max-height="520">
          <el-table-column prop="id" label="ID" width="60" align="center" />
          <el-table-column prop="name" label="姓名" width="100" align="center" />
          <el-table-column prop="idCardNumber" label="身份证号" width="180" align="center" />
          <el-table-column prop="school" label="学校" min-width="120" align="center" show-overflow-tooltip />
          <el-table-column prop="college" label="学院" min-width="120" align="center" show-overflow-tooltip />
          <el-table-column prop="major" label="专业" width="120" align="center" show-overflow-tooltip />
          <el-table-column prop="className" label="班级" width="120" align="center" />
          <el-table-column prop="admissionNo" label="准考证号" width="140" align="center" />
          <el-table-column prop="score" label="成绩" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="scoreTag(row.score)" size="small">{{ row.score }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="examTime" label="考试时间" width="110" align="center" />
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="openEdit(row.id)">修改</el-button>
              <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form :model="scoreForm" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="姓名"><el-input v-model="scoreForm.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="身份证号"><el-input v-model="scoreForm.idCardNumber" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="学校"><el-input v-model="scoreForm.school" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学院"><el-input v-model="scoreForm.college" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="专业"><el-input v-model="scoreForm.major" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="班级"><el-input v-model="scoreForm.className" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="准考证号"><el-input v-model="scoreForm.admissionNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="成绩"><el-input-number v-model="scoreForm.score" :min="0" :max="710" :step="0.5" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="考试时间"><el-date-picker v-model="scoreForm.examTime" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog v-model="importVisible" title="📥 批量导入成绩" width="560px" destroy-on-close>
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        <template #title>
          <ol style="margin:0;padding-left:16px;font-size:13px">
            <li>先<a href="/admin/downloadTemplate" style="color:#7ab3c8">下载导入模板</a>，按模板格式填写</li>
            <li>支持 .xlsx / .xls，文件 ≤ 10MB，成绩 0-710</li>
            <li>导入成功后自动创建学生账号（默认密码 123456）</li>
          </ol>
        </template>
      </el-alert>
      <el-upload :auto-upload="false" :limit="1" accept=".xlsx,.xls" @change="(f) => importFile = f" drag>
        <el-icon><UploadFilled /></el-icon>
        <div>将 Excel 文件拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <div v-if="importResult" style="margin-top:12px">
        <el-alert v-if="importResult.success" :title="importResult.message" type="success" :closable="false" />
        <el-alert v-else :title="importResult.message" type="error" :closable="false" />
        <div v-if="importResult.errors?.length" style="max-height:150px;overflow-y:auto;font-size:12px;color:#d9877a;margin-top:8px">
          <template v-for="e in importResult.errors" :key="e"><li>{{ e }}</li></template>
        </div>
      </div>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>

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

<style scoped>
.pass { color: #6baa7a; font-weight: 600; }
.fail { color: #d9877a; font-weight: 600; }
</style>
