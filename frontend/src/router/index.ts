import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/employee',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'employee',
        name: 'EmployeeManagement',
        component: () => import('@/views/EmployeeManagement.vue'),
      },
      {
        path: 'dashboard',
        name: 'SystemManagement',
        component: () => import('@/views/SystemManagement.vue'),
      },
      {
        path: 'class',
        name: 'ClassManagement',
        component: () => import('@/views/ClassManagement.vue'),
      },
      {
        path: 'student',
        name: 'StudentManagement',
        component: () => import('@/views/StudentManagement.vue'),
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/Chat.vue'),
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
      },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
