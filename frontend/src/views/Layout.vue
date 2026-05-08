<script setup lang="ts">
import { ref, computed } from 'vue'
import { 
  Bell, HelpCircle, User, Users, BookOpen, Settings, 
  UserCircle, LogOut, MessageSquare
} from 'lucide-vue-next'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const realName = ref(localStorage.getItem('realName') || '管理员')
const avatar = ref(localStorage.getItem('avatar') || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix')

// 根据当前路由确定激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/employee') return '人事管理'
  if (path === '/class') return '教学管理'
  if (path === '/student') return '学生管理'
  if (path === '/dashboard') return '部门管理'
  if (path === '/chat') return 'AI对话'
  if (path === '/settings') return '系统设置'
  return '部门管理'
})

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('realName')
  localStorage.removeItem('avatar')
  router.push('/login')
}

// 菜单配置
const menuItems = [
  { name: '人事管理', icon: Users, path: '/employee' },
  { name: '教学管理', icon: BookOpen, path: '/class' },
  { name: '学生管理', icon: User, path: '/student' },
  { name: '部门管理', icon: Settings, path: '/dashboard' },
  { name: 'AI对话', icon: MessageSquare, path: '/chat' },
  { name: '系统设置', icon: Settings, path: '/settings' },
]
</script>

<template>
  <div class="flex flex-col h-screen overflow-hidden">
    <!-- Header -->
    <header class="bg-primary h-16 flex items-center justify-between px-6 shadow-l1 z-20">
      <div class="flex items-center gap-3">
        <h1 class="text-white text-xl font-semibold tracking-wide">南京理工大学紫金学院</h1>
      </div>
      
      <div class="flex items-center gap-6">
        <div class="flex items-center gap-4 text-white/90">
          <button class="hover:text-white transition-colors cursor-pointer"><Bell :size="20" /></button>
          <button class="hover:text-white transition-colors cursor-pointer"><HelpCircle :size="20" /></button>
        </div>
        
        <div class="flex items-center gap-3 pl-6 border-l border-white/20">
          <span class="text-white text-sm font-medium">{{ realName }}</span>
          <div class="w-9 h-9 rounded-full bg-gray-200 border-2 border-white/30 overflow-hidden">
            <img :src="avatar" alt="Admin Avatar" class="w-full h-full object-cover" />
          </div>
          <button @click="handleLogout" class="ml-2 text-white/80 hover:text-white transition-colors">
            <LogOut :size="18" />
          </button>
        </div>
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <!-- Sidebar -->
      <aside class="w-64 bg-white border-r border-gray-100 flex flex-col justify-between shadow-sm z-10">
        <div class="py-6">
          <div class="flex flex-col items-center mb-8 px-4">
            <div class="w-16 h-16 bg-white rounded-md mb-2 flex items-center justify-center border border-gray-100 shadow-sm p-1">
               <img src="https://www.njustzj.edu.cn/_upload/tpl/00/f8/248/template248/images/logo.png" alt="Logo" class="w-full object-contain" />
            </div>
            <h2 class="text-primary font-bold text-lg">南京理工大学</h2>
            <p class="text-gray-500 text-xs font-medium">紫金学院</p>
          </div>

          <nav class="space-y-1">
            <button
              v-for="item in menuItems"
              :key="item.name"
              @click="router.push(item.path)"
              :class="[
                'w-full flex items-center px-6 py-3.5 text-sm font-medium transition-all duration-200',
                activeMenu === item.name ? 'active-sidebar-item' : 'text-gray-600 hover:bg-gray-50'
              ]"
            >
              <component :is="item.icon" :size="18" class="mr-3" />
              {{ item.name }}
            </button>
          </nav>

          <div class="px-5 mt-8">
            <button class="w-full bg-primary py-2.5 rounded-md text-white text-sm font-medium shadow-l1 hover:bg-primary-dark transition-colors">
              快捷办公
            </button>
          </div>
        </div>

        <div class="p-4 border-t border-gray-100">
          <button class="flex items-center gap-3 w-full px-4 py-3 text-gray-700 font-medium text-sm hover:bg-gray-50 rounded-lg transition-colors">
            <UserCircle :size="20" />
            个人中心
          </button>
        </div>
      </aside>

      <!-- Main Content - 使用 router-view 显示子路由 -->
      <main class="flex-1 overflow-y-auto bg-surface">
        <router-view />
      </main>
    </div>

    <!-- Footer Bar -->
    <footer class="bg-[#2d3133] py-2 px-8 flex items-center justify-between text-[#8e9297] text-[10px]">
      <div>© 南京理工大学紫金学院 版权所有 by-HanJun 2026</div>
      <div class="flex gap-4">
        <button class="hover:text-white transition-colors">隐私政策</button>
        <button class="hover:text-white transition-colors">使用条款</button>
        <button class="hover:text-white transition-colors">联系我们</button>
      </div>
    </footer>
  </div>
</template>
