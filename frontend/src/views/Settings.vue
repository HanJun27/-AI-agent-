<script setup lang="ts">
import { ref, onMounted, onActivated } from 'vue'
import { Settings as SettingsIcon, Key, Info, Save, TestTube2, CheckCircle, MessageSquare } from 'lucide-vue-next'
import { configApi, preferenceApi } from '@/api/config'

// 当前激活的标签页
const activeTab = ref('api')

// 对话设置数据
const chatSettings = ref({
  priorityMode: 'llm_agent', // 优先模式
  fallbackMode: 'none'       // 降级模式
})

// 优先选项
const priorityOptions = [
  { value: 'llm_agent', label: 'LLM Agent（智能工具选择）' },
  { value: 'llm_parse', label: 'LLM解析JSON' },
  { value: 'rule', label: '规则匹配' }
]

// 降级选项
const fallbackOptions = [
  { value: 'none', label: '无（不降级）' },
  { value: 'llm_parse', label: 'LLM解析JSON' },
  { value: 'rule', label: '规则匹配' }
]

// 加载对话设置
const loadChatSettings = () => {
  const savedPriority = localStorage.getItem('priorityMode')
  const savedFallback = localStorage.getItem('fallbackMode')
  
  if (savedPriority) {
    chatSettings.value.priorityMode = savedPriority
  }
  if (savedFallback) {
    chatSettings.value.fallbackMode = savedFallback
  }
}

// 保存对话设置
const saveChatSettings = () => {
  localStorage.setItem('priorityMode', chatSettings.value.priorityMode)
  localStorage.setItem('fallbackMode', chatSettings.value.fallbackMode)
  alert('对话设置已保存！刷新Chat页面后生效。')
  console.log('对话设置已保存:', chatSettings.value)
}

interface ApiConfig {
  name: string
  key: string
  model: string
  enabled: boolean
  baseUrl: string
}

const apiConfigs = ref<Record<string, ApiConfig>>({})
const loading = ref(false)

// 当前选中的提供商
const selectedProvider = ref<string | null>(null)

// 提供商列表
const providerList = [
  { 
    id: 'zhipu', 
    name: '智谱AI', 
    icon: '🤖',
    color: 'blue',
    description: '推荐用于中文场景',
    models: ['glm-4-flash', 'glm-4-plus', 'glm-4']
  },
  { 
    id: 'deepseek', 
    name: 'DeepSeek', 
    icon: '🧠',
    color: 'green',
    description: '高性价比选择',
    models: ['deepseek-chat', 'deepseek-coder']
  },
  { 
    id: 'qwen', 
    name: '通义千问', 
    icon: '✨',
    color: 'orange',
    description: '阿里出品',
    models: ['qwen-turbo', 'qwen-plus', 'qwen-max']
  },
  { 
    id: 'openai', 
    name: 'OpenAI', 
    icon: '💬',
    color: 'indigo',
    description: '国际主流选择',
    models: ['gpt-3.5-turbo', 'gpt-4', 'gpt-4-turbo']
  }
]

// 加载API配置
const loadApiConfigs = async () => {
  try {
    const res: any = await configApi.getAllApiConfigs()
    if (res.code === 200 && res.data) {
      apiConfigs.value = res.data
    }
  } catch (error) {
    console.error('加载API配置失败:', error)
    // 如果加载失败，使用默认配置
    apiConfigs.value = {
      zhipu: {
        name: '智谱AI',
        key: '',
        model: 'glm-4-flash',
        enabled: false,
        baseUrl: 'https://open.bigmodel.cn/api/paas/v4'
      },
      deepseek: {
        name: 'DeepSeek',
        key: '',
        model: 'deepseek-chat',
        enabled: false,
        baseUrl: 'https://api.deepseek.com/v1'
      },
      qwen: {
        name: '通义千问',
        key: '',
        model: 'qwen-turbo',
        enabled: false,
        baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1'
      },
      openai: {
        name: 'OpenAI',
        key: '',
        model: 'gpt-3.5-turbo',
        enabled: false,
        baseUrl: 'https://api.openai.com/v1'
      }
    }
  }
}

// 打开提供商选择
const openProviderSelector = (providerId: string) => {
  selectedProvider.value = providerId
}

// 关闭配置面板
const closeConfigPanel = () => {
  selectedProvider.value = null
}

// 获取提供商URL
const getProviderUrl = (providerId: string): string => {
  const urls: Record<string, string> = {
    zhipu: 'https://open.bigmodel.cn/',
    deepseek: 'https://platform.deepseek.com/',
    qwen: 'https://dashscope.console.aliyun.com/',
    openai: 'https://platform.openai.com/'
  }
  return urls[providerId] || ''
}

// 保存配置
const saveConfig = async (provider: string) => {
  try {
    loading.value = true
    
    const config = apiConfigs.value[provider]
    await configApi.saveApiConfig(provider, config)
    
    alert(`${config.name} API配置已保存到数据库！`)
  } catch (error) {
    console.error('保存配置失败:', error)
    alert('保存失败，请重试')
  } finally {
    loading.value = false
  }
}

// 测试连接
const testConnection = async (provider: string) => {
  const config = apiConfigs.value[provider]
  
  if (!config.key) {
    alert('请先输入API Key')
    return
  }
  
  try {
    loading.value = true
    
    // TODO: 这里可以添加真实的API测试逻辑
    // 目前先做格式验证
    setTimeout(() => {
      if (config.key.length > 10) {
        alert(`${config.name} API Key格式验证通过！\n\n提示：可以在Chat界面使用该配置进行对话。`)
      } else {
        alert(`${config.name} API Key可能太短，请检查是否正确复制。`)
      }
      loading.value = false
    }, 800)
  } catch (error) {
    console.error('测试连接失败:', error)
    alert('测试连接失败')
    loading.value = false
  }
}

// 通用设置
const generalSettings = ref({
  language: 'zh-CN',
  theme: 'light',
  notifications: true,
  autoSave: true
})

// 加载用户偏好
const loadUserPreferences = async () => {
  try {
    // 假设当前用户ID为1，实际应该从登录信息中获取
    const userId = 1
    const res: any = await preferenceApi.getUserPreferences(userId)
    if (res.code === 200 && res.data) {
      generalSettings.value = res.data
    }
  } catch (error) {
    console.error('加载用户偏好失败:', error)
  }
}

// 保存通用设置
const saveGeneralSettings = async () => {
  try {
    const userId = 1
    await preferenceApi.saveUserPreferences(userId, generalSettings.value)
    alert('通用设置已保存到数据库！')
  } catch (error) {
    console.error('保存设置失败:', error)
    alert('保存失败，请重试')
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadApiConfigs()
  loadUserPreferences()
  loadChatSettings()
})

// keep-alive 激活时重新加载
onActivated(() => {
  loadApiConfigs()
  loadUserPreferences()
  loadChatSettings()
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-purple-50">
    <!-- Header -->
    <div class="bg-white/95 backdrop-blur-sm border-b border-slate-200 px-8 py-6 shadow-sm">
      <div class="max-w-6xl mx-auto">
        <h1 class="text-3xl font-bold text-slate-900 flex items-center gap-3">
          <SettingsIcon :size="32" class="text-purple-600" />
          系统设置
        </h1>
        <p class="text-slate-500 mt-2 ml-11">管理您的API密钥和偏好设置</p>
      </div>
    </div>

    <div class="flex max-w-6xl mx-auto">
      <!-- 左侧二级导航栏 -->
      <aside class="w-64 bg-white/95 backdrop-blur-sm border-r border-slate-200 min-h-[calc(100vh-88px)] sticky top-16">
        <nav class="p-4 space-y-1">
          <button
            @click="activeTab = 'api'"
            :class="[
              activeTab === 'api' 
                ? 'bg-purple-50 text-purple-600 border-r-4 border-purple-600' 
                : 'text-slate-600 hover:bg-slate-50'
            ]"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors font-medium"
          >
            <Key :size="20" />
            <span>API配置</span>
          </button>
          
          <button
            @click="activeTab = 'general'"
            :class="[
              activeTab === 'general' 
                ? 'bg-purple-50 text-purple-600 border-r-4 border-purple-600' 
                : 'text-slate-600 hover:bg-slate-50'
            ]"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors font-medium"
          >
            <SettingsIcon :size="20" />
            <span>通用设置</span>
          </button>
          
          <button
            @click="activeTab = 'chat'"
            :class="[
              activeTab === 'chat' 
                ? 'bg-purple-50 text-purple-600 border-r-4 border-purple-600' 
                : 'text-slate-600 hover:bg-slate-50'
            ]"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors font-medium"
          >
            <MessageSquare :size="20" />
            <span>对话设置</span>
          </button>
          
          <button
            @click="activeTab = 'about'"
            :class="[
              activeTab === 'about' 
                ? 'bg-purple-50 text-purple-600 border-r-4 border-purple-600' 
                : 'text-slate-600 hover:bg-slate-50'
            ]"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors font-medium"
          >
            <Info :size="20" />
            <span>关于</span>
          </button>
        </nav>
      </aside>

      <!-- 右侧内容区 -->
      <main class="flex-1 p-8">
        <!-- API配置标签页 -->
        <div v-if="activeTab === 'api'" class="max-w-4xl">
          <div class="mb-6">
            <h2 class="text-2xl font-bold text-slate-900 mb-2">AI模型API配置</h2>
            <p class="text-slate-500">配置您使用的AI服务提供商和API密钥</p>
          </div>

          <!-- 未选择提供商时显示提供商列表 -->
          <div v-if="!selectedProvider" class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div 
              v-for="provider in providerList" 
              :key="provider.id"
              @click="openProviderSelector(provider.id)"
              class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all cursor-pointer group"
            >
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                  <div :class="`w-12 h-12 bg-${provider.color}-100 rounded-xl flex items-center justify-center text-2xl group-hover:scale-110 transition-transform`">
                    {{ provider.icon }}
                  </div>
                  <div>
                    <h3 class="font-bold text-slate-900 text-lg">{{ provider.name }}</h3>
                    <p class="text-xs text-slate-500">{{ provider.description }}</p>
                  </div>
                </div>
                <div v-if="apiConfigs[provider.id]?.enabled" class="w-2 h-2 bg-emerald-500 rounded-full"></div>
              </div>
              
              <div class="space-y-2">
                <div class="flex items-center gap-2 text-sm text-slate-600">
                  <Key :size="16" />
                  <span>{{ apiConfigs[provider.id]?.key ? '已配置' : '未配置' }}</span>
                </div>
                <div class="flex items-center gap-2 text-sm text-slate-600">
                  <TestTube2 :size="16" />
                  <span>{{ apiConfigs[provider.id]?.model || provider.models[0] }}</span>
                </div>
              </div>
              
              <div class="mt-4 pt-4 border-t border-slate-100">
                <p class="text-xs text-purple-600 font-medium group-hover:underline">点击配置 →</p>
              </div>
            </div>
          </div>

          <!-- 选择提供商后显示配置面板 -->
          <div v-if="selectedProvider" class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
            <div class="flex items-center justify-between mb-6 pb-4 border-b border-slate-100">
              <div class="flex items-center gap-3">
                <button
                  @click="closeConfigPanel"
                  class="w-8 h-8 rounded-lg hover:bg-slate-100 flex items-center justify-center transition-colors"
                >
                  ←
                </button>
                <div>
                  <h3 class="text-xl font-bold text-slate-900">{{ apiConfigs[selectedProvider].name }} 配置</h3>
                  <p class="text-sm text-slate-500">配置您的API密钥和模型参数</p>
                </div>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input 
                  type="checkbox" 
                  v-model="apiConfigs[selectedProvider].enabled"
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-purple-600"></div>
              </label>
            </div>

            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">API Key</label>
                <input
                  type="password"
                  v-model="apiConfigs[selectedProvider].key"
                  placeholder="sk-xxxxxxxxxxxxxxxx"
                  class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                />
                <p class="text-xs text-slate-500 mt-1">
                  获取地址：<a :href="getProviderUrl(selectedProvider)" target="_blank" class="text-purple-600 hover:underline">{{ getProviderUrl(selectedProvider) }}</a>
                </p>
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">模型</label>
                <select
                  v-model="apiConfigs[selectedProvider].model"
                  class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                >
                  <option v-for="model in providerList.find(p => p.id === selectedProvider)?.models" :key="model" :value="model">
                    {{ model }}
                  </option>
                </select>
              </div>

              <div class="flex gap-3 pt-4">
                <button
                  @click="saveConfig(selectedProvider)"
                  :disabled="loading"
                  class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-medium disabled:opacity-50 flex items-center gap-2"
                >
                  <Save v-if="!loading" :size="18" />
                  {{ loading ? '保存中...' : '保存配置' }}
                </button>
                <button
                  @click="testConnection(selectedProvider)"
                  :disabled="loading"
                  class="px-6 py-2 border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-50 transition-colors font-medium disabled:opacity-50 flex items-center gap-2"
                >
                  <TestTube2 v-if="!loading" :size="18" />
                  {{ loading ? '测试中...' : '测试连接' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 通用设置标签页 -->
        <div v-if="activeTab === 'general'" class="max-w-4xl">
          <div class="mb-6">
            <h2 class="text-2xl font-bold text-slate-900 mb-2">通用设置</h2>
            <p class="text-slate-500">自定义您的使用体验</p>
          </div>

          <div class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm space-y-6">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2">语言</label>
              <select
                v-model="generalSettings.language"
                class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              >
                <option value="zh-CN">简体中文</option>
                <option value="en-US">English</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2">主题</label>
              <select
                v-model="generalSettings.theme"
                class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              >
                <option value="light">浅色模式</option>
                <option value="dark">深色模式</option>
                <option value="auto">跟随系统</option>
              </select>
            </div>

            <div class="flex items-center justify-between">
              <div>
                <p class="font-medium text-slate-900">通知提醒</p>
                <p class="text-sm text-slate-500">接收职业建议和更新通知</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input 
                  type="checkbox" 
                  v-model="generalSettings.notifications"
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-purple-600"></div>
              </label>
            </div>

            <div class="flex items-center justify-between">
              <div>
                <p class="font-medium text-slate-900">自动保存</p>
                <p class="text-sm text-slate-500">自动保存对话和设置</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input 
                  type="checkbox" 
                  v-model="generalSettings.autoSave"
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-purple-600"></div>
              </label>
            </div>

            <button
              @click="saveGeneralSettings"
              class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-medium flex items-center gap-2"
            >
              <Save :size="18" />
              保存设置
            </button>
          </div>
        </div>

        <!-- 对话设置标签页 -->
        <div v-if="activeTab === 'chat'" class="max-w-4xl">
          <div class="mb-6">
            <h2 class="text-2xl font-bold text-slate-900 mb-2">对话设置</h2>
            <p class="text-slate-500">配置AI对话的识别模式和降级策略</p>
          </div>

          <div class="bg-white rounded-xl border border-slate-200 p-6 shadow-sm space-y-6">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2">优先模式</label>
              <select
                v-model="chatSettings.priorityMode"
                class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              >
                <option v-for="option in priorityOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
              <p class="text-xs text-slate-500 mt-1">
                选择AI对话时的首选识别方式。LLM Agent模式最智能，支持自然语言操作。
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-slate-700 mb-2">降级模式</label>
              <select
                v-model="chatSettings.fallbackMode"
                class="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
              >
                <option v-for="option in fallbackOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
              <p class="text-xs text-slate-500 mt-1">
                当优先模式失败时使用的备选方案。选择“无”表示不降级。
              </p>
            </div>

            <div class="bg-blue-50 border-l-4 border-blue-500 p-4 rounded">
              <p class="text-sm text-blue-800">
                <strong>💡 提示：</strong>修改设置后需要刷新Chat页面才能生效。
              </p>
            </div>

            <button
              @click="saveChatSettings"
              class="px-6 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-medium flex items-center gap-2"
            >
              <Save :size="18" />
              保存设置
            </button>
          </div>
        </div>

        <!-- 关于标签页 -->
        <div v-if="activeTab === 'about'" class="max-w-4xl">
          <div class="mb-6">
            <h2 class="text-2xl font-bold text-slate-900 mb-2">关于</h2>
            <p class="text-slate-500">了解紫金学院管理系统</p>
          </div>

          <div class="bg-white rounded-xl border border-slate-200 p-8 shadow-sm text-center">
            <div class="w-20 h-20 bg-gradient-to-br from-purple-600 to-emerald-500 rounded-2xl mx-auto mb-4 flex items-center justify-center shadow-xl">
              <CheckCircle :size="40" class="text-white" />
            </div>
            <h3 class="text-2xl font-bold text-slate-900 mb-2">紫金学院管理系统</h3>
            <p class="text-slate-500 mb-4">基于AI的大学生职业规划智能体</p>
            <p class="text-sm text-slate-400">版本 1.0.0</p>
            
            <div class="mt-8 pt-8 border-t border-slate-200">
              <p class="text-sm text-slate-500 mb-3">技术栈</p>
              <div class="flex flex-wrap justify-center gap-2">
                <span class="px-3 py-1 bg-purple-100 text-purple-700 rounded-full text-xs font-medium">Vue 3</span>
                <span class="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-medium">TypeScript</span>
                <span class="px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-medium">Spring Boot</span>
                <span class="px-3 py-1 bg-orange-100 text-orange-700 rounded-full text-xs font-medium">MyBatis</span>
                <span class="px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full text-xs font-medium">MySQL</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 平滑过渡 */
button {
  transition: all 0.2s ease;
}
</style>
