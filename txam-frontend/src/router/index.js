import { createRouter, createWebHistory } from 'vue-router'
import { setRouter } from '../api/index.js'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/admin/home',
    name: 'AdminHome',
    component: () => import('../views/AdminHome.vue'),
    meta: { role: 'admin' }
  },
  {
    path: '/student/home',
    name: 'StudentHome',
    component: () => import('../views/StudentHome.vue'),
    meta: { role: 'student' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：登录拦截 + 角色校验
router.beforeEach((to, from, next) => {
  if (to.meta.noAuth) {
    next()
    return
  }

  // 从 sessionStorage 获取当前用户（登录后由 Login.vue 写入）
  const user = JSON.parse(sessionStorage.getItem('user') || 'null')

  if (!user) {
    // 未登录 → 跳转登录页
    next('/login')
    return
  }

  if (to.meta.role && to.meta.role !== user.role) {
    // 角色不匹配 → 跳回自己的主页
    next(user.role === 'admin' ? '/admin/home' : '/student/home')
    return
  }

  next()
})

// 注入到 axios 拦截器，实现 401 → 跳转登录页
setRouter(router)

export default router
