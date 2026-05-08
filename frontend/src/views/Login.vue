<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { User, Lock, Eye, EyeOff, School, AlertCircle } from 'lucide-vue-next'

const router = useRouter()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const error = ref('')
const loading = ref(false)
const rememberMe = ref(false)

const handleLogin = async () => {
  if (!username.value || !password.value) {
    error.value = '用户名或密码不能为空'
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    console.log('开始登录...', username.value)
    const response = await login({
      username: username.value,
      password: password.value,
    })
    
    console.log('登录响应:', response)
    
    // 保存token和用户信息
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('username', response.data.username)
    localStorage.setItem('realName', response.data.realName)
    localStorage.setItem('avatar', response.data.avatar)
    
    if (rememberMe.value) {
      localStorage.setItem('rememberedUsername', username.value)
    }
    
    console.log('准备跳转到dashboard')
    // 跳转到主界面
    router.push('/dashboard')
  } catch (err: any) {
    console.error('登录错误:', err)
    error.value = err.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="min-h-screen w-full flex items-center justify-center relative bg-gradient-to-br from-[#0277bd] to-[#01579b] selection:bg-blue-100 p-4 font-sans">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-1/4 -right-1/4 w-[600px] h-[600px] rounded-full bg-white/5 blur-3xl"></div>
      <div class="absolute -bottom-1/4 -left-1/4 w-[500px] h-[500px] rounded-full bg-blue-400/10 blur-3xl"></div>
    </div>

    <!-- 登录卡片 -->
    <transition
      appear
      enter-active-class="transition duration-500 ease-out"
      enter-from-class="opacity-0 translate-y-5"
      enter-to-class="opacity-100 translate-y-0"
    >
      <section class="relative z-10 w-full max-w-[400px] bg-white rounded-2xl shadow-2xl overflow-hidden">
        <!-- 头部 -->
        <header class="bg-[#005ea4] px-8 py-10 flex flex-col items-center gap-2">
          <div class="w-16 h-16 bg-white/10 rounded-full flex items-center justify-center backdrop-blur-sm mb-2">
            <School :size="40" class="text-white" />
          </div>
          <h1 class="font-semibold text-2xl text-white tracking-tight text-center">紫金学院管理系统</h1>
          <p class="text-xs text-white/80 uppercase tracking-widest font-medium">NUST ZIJIN COLLEGE</p>
        </header>

        <!-- 表单区域 -->
        <div class="p-8 pb-6 space-y-6">
          <transition
            enter-active-class="transition duration-300 ease-out"
            enter-from-class="opacity-0 -translate-y-2"
            enter-to-class="opacity-100 translate-y-0"
            leave-active-class="transition duration-200 ease-in"
            leave-from-class="opacity-100"
            leave-to-class="opacity-0"
          >
            <div v-if="error" class="flex items-center gap-2 p-3 bg-red-50 text-red-700 rounded-lg border border-red-200 text-sm">
              <AlertCircle :size="16" class="flex-shrink-0" />
              <span>{{ error }}</span>
            </div>
          </transition>

          <form @submit.prevent="handleLogin" class="space-y-4">
            <!-- 用户名 -->
            <div class="space-y-1">
              <label class="block text-sm font-semibold text-gray-600 px-1">用户名</label>
              <div class="relative flex items-center group">
                <div class="absolute left-4 text-gray-400 group-focus-within:text-[#005ea4] transition-colors">
                  <User :size="20" />
                </div>
                <input 
                  v-model="username"
                  type="text"
                  class="w-full pl-12 pr-4 py-3 bg-gray-50 border-none rounded-xl text-base text-gray-900 ring-1 ring-inset ring-gray-200 focus:ring-2 focus:ring-inset focus:ring-[#005ea4] transition-all duration-200 outline-none"
                  placeholder="请输入用户名"
                />
              </div>
            </div>

            <!-- 密码 -->
            <div class="space-y-1">
              <label class="block text-sm font-semibold text-gray-600 px-1">密码</label>
              <div class="relative flex items-center group">
                <div class="absolute left-4 text-gray-400 group-focus-within:text-[#005ea4] transition-colors">
                  <Lock :size="20" />
                </div>
                <input 
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  class="w-full pl-12 pr-12 py-3 bg-gray-50 border-none rounded-xl text-base text-gray-900 ring-1 ring-inset ring-gray-200 focus:ring-2 focus:ring-inset focus:ring-[#005ea4] transition-all duration-200 outline-none"
                  placeholder="请输入密码"
                />
                <button 
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-4 text-gray-400 hover:text-gray-600 transition-colors"
                >
                  <EyeOff v-if="showPassword" :size="20" />
                  <Eye v-else :size="20" />
                </button>
              </div>
            </div>

            <!-- 记住我 & 忘记密码 -->
            <div class="flex items-center justify-between px-1">
              <label class="flex items-center gap-2 cursor-pointer group">
                <input 
                  v-model="rememberMe"
                  type="checkbox" 
                  class="w-4 h-4 rounded border-gray-300 text-[#005ea4] focus:ring-[#005ea4] focus:ring-offset-0 cursor-pointer"
                />
                <span class="text-sm text-gray-500 group-hover:text-gray-800 transition-colors">记住我</span>
              </label>
              <a href="#" class="text-sm text-[#005ea4] hover:underline font-medium">忘记密码？</a>
            </div>

            <!-- 登录按钮 -->
            <div class="flex flex-col items-center pt-2 gap-6">
              <button 
                type="submit"
                :disabled="loading"
                class="w-32 py-2.5 bg-gradient-to-r from-[#005ea4] to-[#00629e] text-white rounded-lg font-medium shadow-md hover:shadow-lg hover:-translate-y-0.5 active:translate-y-0 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 flex items-center justify-center"
              >
                <div v-if="loading" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                <span v-else>登录</span>
              </button>
              
              <div class="center">
                <p class="text-[11px] text-gray-400 leading-relaxed max-w-[200px] mx-auto text-center">
                  © 南京理工大学紫金学院 版权所有
                </p>
              </div>
            </div>
          </form>
        </div>

        <!-- 底部装饰 -->
        <div class="relative h-24 w-full overflow-hidden mt-2">
          <img 
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuBdi0FJSl6LTVQzvITufek1cMkY__b5cPiSz_p6IT08URzCP5rwMl2sEvwvPp3hFL1gXtVWqL8B_onWxlueyoUNvy_3Vvq6yhqazh29AZTdlegMy-jyX3IMEODBgEk4MLQRAHmSsObGns4JaK1M_KmxdpTv7_hJoZU8zRc08LMircwuHtTAV7AWlrUFwmVvYMuPhmNz6L-tXdITNj-BxzFHf8sH2RCc0oi4R8ir9KYqPSD0DaZP7WlXT5ZVZkKHZBULQ2FIz3SvA6St" 
            alt="University Logo" 
            class="w-full h-full object-cover opacity-10 grayscale"
            referrerPolicy="no-referrer"
          />
          <div class="absolute inset-0 bg-gradient-to-t from-white via-white/50 to-transparent" />
        </div>
      </section>
    </transition>
  </main>
</template>

<style scoped>
/* 确保 SVG 继承颜色 */
:deep(svg) {
  display: block;
}
</style>
