<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { Send, Bot, User, Sparkles, ChevronLeft, ChevronRight, Plus, Trash2 } from 'lucide-vue-next'
import request from '@/api/request'

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

interface ChatSession {
  id: string
  title: string
  createdAt: Date
  messageCount: number
}

// State
const messageInput = ref('')
const messages = ref<Message[]>([])
const isTyping = ref(false)
const selectedProvider = ref<string>('') // 当前选择的AI提供商
let messageId = 1

// 从 localStorage 读取对话设置
const getPriorityMode = (): string => {
  return localStorage.getItem('priorityMode') || 'llm_agent'
}

const getFallbackMode = (): string => {
  return localStorage.getItem('fallbackMode') || 'none'
}

// 会话管理
const sessions = ref<ChatSession[]>([
  { id: '1', title: '欢迎对话', createdAt: new Date(), messageCount: 1 }
])
const activeSessionId = ref<string>('1')
const showSidebar = ref(true)

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  const container = document.querySelector('.chat-messages')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

// 发送消息 - 调用后端API
const sendMessage = async () => {
  if (!messageInput.value.trim() || isTyping.value) return
  
  // 添加用户消息
  const userMessage: Message = {
    id: messageId++,
    role: 'user',
    content: messageInput.value,
    timestamp: new Date()
  }
  messages.value.push(userMessage)
  
  const question = messageInput.value
  messageInput.value = ''
  await scrollToBottom()
  
  // 调用后端AI Agent
  isTyping.value = true
  try {
    // 根据优先和降级选项组合，决定使用哪个接口
    let apiUrl = '/chat/send'  // 默认传统接口
    let modeParam = ''
    
    const priorityMode = getPriorityMode()
    const fallbackMode = getFallbackMode()
    
    if (priorityMode === 'llm_agent') {
      // 使用LLM Agent模式
      apiUrl = '/chat/agent'
      modeParam = 'agent'
    } else {
      // 使用传统模式，计算recognitionMode
      if (priorityMode === 'llm_parse' && fallbackMode === 'none') {
        modeParam = 'llm_only'
      } else if (priorityMode === 'rule' && fallbackMode === 'none') {
        modeParam = 'rule_only'
      } else if (priorityMode === 'llm_parse' && fallbackMode === 'rule') {
        modeParam = 'llm_first'
      } else if (priorityMode === 'rule' && fallbackMode === 'llm_parse') {
        modeParam = 'rule_first'
      } else {
        modeParam = 'llm_first'  // 默认
      }
    }
    
    const response: any = await request({
      url: apiUrl,
      method: 'post',
      data: {
        message: question,
        provider: selectedProvider.value,
        recognitionMode: modeParam,  // 传递计算后的模式
        sessionId: activeSessionId.value
      }
    })
    
    if (response.code === 200) {
      const aiMessage: Message = {
        id: messageId++,
        role: 'assistant',
        content: response.data.reply,
        timestamp: new Date(response.data.timestamp)
      }
      messages.value.push(aiMessage)
    } else {
      throw new Error(response.message || '请求失败')
    }
  } catch (error: any) {
    console.error('发送消息失败:', error)
    
    // 判断是否为超时错误
    let errorMessageContent = ''
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      // 超时错误：操作可能已在后台执行
      errorMessageContent = '⚠️ 请求超时，但操作可能已在后台执行。\n\n' +
                           '建议您：\n' +
                           '1. 刷新页面查看最新状态\n' +
                           '2. 如果数据已变更，说明操作已成功\n' +
                           '3. 如未变更，请重试或联系管理员'
    } else {
      // 其他错误
      errorMessageContent = '❌ 抱歉，处理您的请求时出现错误：' + (error.message || '未知错误')
    }
    
    const errorMessage: Message = {
      id: messageId++,
      role: 'assistant',
      content: errorMessageContent,
      timestamp: new Date()
    }
    messages.value.push(errorMessage)
  } finally {
    isTyping.value = false
    await scrollToBottom()
    
    // 更新会话消息数量
    const session = sessions.value.find(s => s.id === activeSessionId.value)
    if (session) {
      session.messageCount = messages.value.length
    }
  }
}

// 回车发送
const handleEnter = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 格式化时间
const formatTime = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

// 创建新对话
const createNewSession = async () => {
  const sessionId = String(Date.now())
  const newSession: ChatSession = {
    id: sessionId,
    title: '新的对话',
    createdAt: new Date(),
    messageCount: 0
  }
  
  try {
    // 保存到数据库
    await request({
      url: '/chat/session',
      method: 'post',
      data: {
        sessionId: sessionId,
        title: '新的对话',
        userId: 1
      }
    })
    
    sessions.value.unshift(newSession)
    switchSession(sessionId)
  } catch (error) {
    console.error('创建会话失败:', error)
  }
}

// 切换会话
const switchSession = async (sessionId: string) => {
  activeSessionId.value = sessionId
  
  try {
    // 从数据库加载消息
    const response: any = await request({
      url: `/chat/messages/${sessionId}`,
      method: 'get'
    })
    
    if (response.code === 200 && response.data) {
      messages.value = response.data.map((msg: any) => ({
        id: msg.id,
        role: msg.role,
        content: msg.content,
        timestamp: new Date(msg.timestamp)
      }))
    } else {
      messages.value = []
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    messages.value = []
  }
}

// 删除会话
const deleteSession = async (sessionId: string, event: Event) => {
  event.stopPropagation()
  
  if (!confirm('确定要删除这个对话吗？')) return
  
  try {
    // 从数据库删除
    await request({
      url: `/chat/session/${sessionId}`,
      method: 'delete'
    })
    
    const index = sessions.value.findIndex(s => s.id === sessionId)
    if (index !== -1) {
      sessions.value.splice(index, 1)
      
      // 如果删除的是当前会话，切换到第一个
      if (sessionId === activeSessionId.value && sessions.value.length > 0) {
        switchSession(sessions.value[0].id)
      } else if (sessions.value.length === 0) {
        messages.value = []
      }
    }
  } catch (error) {
    console.error('删除会话失败:', error)
    alert('删除失败')
  }
}

// 加载会话列表
const loadSessions = async () => {
  try {
    const response: any = await request({
      url: '/chat/sessions',
      method: 'get',
      params: { userId: 1 }
    })
    
    if (response.code === 200 && response.data) {
      sessions.value = response.data.map((s: any) => ({
        id: s.sessionId,
        title: s.title || '未命名对话',
        createdAt: new Date(s.createTime),
        messageCount: s.messageCount || 0
      }))
      
      // 如果有会话，切换到第一个
      if (sessions.value.length > 0) {
        switchSession(sessions.value[0].id)
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 初始化
onMounted(async () => {
  loadSessions()
  // 加载已启用的API配置
  await loadEnabledProvider()
  // 对话设置已从 Settings 页面管理，通过 localStorage 读取
})

// 加载已启用的API提供商
const loadEnabledProvider = async () => {
  try {
    const response: any = await request({
      url: '/config/api-configs',  // 修正为正确的接口路径
      method: 'get'
    })
    
    if (response.code === 200 && response.data) {
      // response.data 是一个对象，key是provider，value是配置
      const configs = response.data
      
      // 查找第一个启用的配置
      for (const [provider, config] of Object.entries(configs)) {
        const configObj = config as any
        if (configObj.enabled === true) {
          selectedProvider.value = provider
          console.log('自动选择API提供商:', selectedProvider.value)
          break
        }
      }
    }
  } catch (error) {
    console.error('加载API配置失败:', error)
  }
}
</script>

<template>
  <div class="relative h-full flex bg-gradient-to-br from-purple-50 via-white to-emerald-50">
    <!-- 左侧对话列表面板 -->
    <aside 
      v-if="showSidebar" 
      class="fixed left-64 top-16 bottom-8 w-64 bg-white/95 backdrop-blur-sm border border-slate-200 rounded-2xl shadow-xl flex flex-col z-40 transition-all duration-300"
    >
      <!-- 列表标题栏 -->
      <div class="px-4 py-3 border-b border-slate-200 flex items-center justify-between">
        <h2 class="text-sm font-bold text-slate-900 uppercase tracking-wider">历史对话</h2>
        <button
          @click="showSidebar = false"
          class="w-7 h-7 flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors"
          title="收起"
        >
          <ChevronLeft :size="18" />
        </button>
      </div>
      
      <!-- 新建对话按钮 -->
      <div class="p-3 border-b border-slate-200">
        <button
          @click="createNewSession"
          class="w-full px-4 py-2.5 bg-gradient-to-r from-purple-600 to-emerald-500 text-white rounded-xl hover:shadow-lg transition-all flex items-center gap-2 text-sm font-medium"
        >
          <Plus :size="16" />
          开启新对话
        </button>
      </div>
      
      <!-- 会话列表 -->
      <div class="flex-1 overflow-y-auto p-3 space-y-2">
        <div
          v-for="session in sessions"
          :key="session.id"
          @click="switchSession(session.id)"
          :class="[
            'group p-3 rounded-xl cursor-pointer transition-all border',
            activeSessionId === session.id
              ? 'bg-white border-purple-300 shadow-md'
              : 'bg-transparent border-transparent hover:bg-white hover:border-slate-200'
          ]"
        >
          <div class="flex items-start justify-between gap-2">
            <div class="flex-1 min-w-0">
              <h3 class="text-sm font-semibold text-slate-900 truncate mb-1">
                {{ session.title }}
              </h3>
              <p class="text-xs text-slate-500">
                {{ formatTime(session.createdAt) }} · {{ session.messageCount }} 条消息
              </p>
            </div>
            <button
              @click="deleteSession(session.id, $event)"
              class="opacity-0 group-hover:opacity-100 p-1.5 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all flex-shrink-0"
              title="删除对话"
            >
              <Trash2 :size="14" />
            </button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="sessions.length === 0" class="text-center py-8 text-slate-400">
          <Bot :size="40" class="mx-auto mb-2 opacity-50" />
          <p class="text-sm">暂无对话历史</p>
          <p class="text-xs mt-1">点击"开启新对话"开始</p>
        </div>
      </div>
    </aside>

    <!-- 展开按钮（当侧边栏收起时显示） -->
    <div 
      v-if="!showSidebar"
      class="fixed left-64 top-20 z-40"
    >
      <button
        @click="showSidebar = true"
        class="w-8 h-8 flex items-center justify-center text-slate-400 hover:text-slate-600 bg-white/95 backdrop-blur-sm border border-slate-200 rounded-full shadow-sm transition-all"
        title="展开历史对话"
      >
        <ChevronRight :size="18" />
      </button>
    </div>

    <!-- 主内容区 -->
    <div class="flex-1 flex flex-col relative" :class="showSidebar ? 'ml-64' : ''">
      <!-- 聊天头部 -->
      <div class="bg-white/95 backdrop-blur-sm border-b border-slate-200 px-8 py-4 shadow-sm">
        <div class="flex items-center justify-between max-w-4xl mx-auto">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-purple-600 to-emerald-500 flex items-center justify-center shadow-lg shadow-purple-500/20">
              <Sparkles :size="20" class="text-white" />
            </div>
            <div>
              <h1 class="text-lg font-semibold text-slate-900">智能管理顾问</h1>
              <p class="text-xs text-slate-500">基于AI的智能管理系统顾问</p>
            </div>
          </div>
          
          <!-- AI提供商选择器 -->
          <div class="flex items-center gap-2">
            <label class="text-sm text-slate-600">AI引擎：</label>
            <select 
              v-model="selectedProvider"
              class="px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white"
            >
              <option value="">默认模式（本地Agent）</option>
              <option value="zhipu">智谱AI</option>
              <option value="deepseek">DeepSeek</option>
              <option value="qwen">通义千问</option>
              <option value="openai">OpenAI</option>
            </select>
          </div>
        </div>
      </div>

      <!-- 聊天消息区域 -->
      <div class="chat-messages flex-1 overflow-y-auto px-8 py-8">
        <div class="max-w-4xl mx-auto space-y-6">
          <!-- 欢迎区域 -->
          <div v-if="messages.length === 0" class="flex flex-col items-center text-center py-16">
            <div class="w-20 h-20 rounded-2xl bg-gradient-to-br from-purple-100 to-emerald-100 flex items-center justify-center mb-6">
              <Bot :size="40" class="text-purple-600" />
            </div>
            <h2 class="text-2xl font-semibold text-slate-900 mb-3">开始与 AI 助手对话</h2>
            <p class="text-slate-500 text-base max-w-lg">让我为你更好的管理系统</p>
          </div>

          <!-- 消息列表 -->
          <template v-for="message in messages" :key="message.id">
            <!-- AI消息 -->
            <div v-if="message.role === 'assistant'" class="flex gap-4 group">
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-purple-600 to-emerald-500 flex items-center justify-center flex-shrink-0 mt-1 shadow-md">
                <Bot :size="16" class="text-white" />
              </div>
              <div class="flex-1 min-w-0">
                <div class="bg-white rounded-2xl px-5 py-3.5 shadow-sm border border-slate-100">
                  <p class="text-slate-900 leading-relaxed whitespace-pre-wrap">{{ message.content }}</p>
                </div>
                <p class="text-xs text-slate-400 mt-1.5 ml-1">{{ formatTime(message.timestamp) }}</p>
              </div>
            </div>

            <!-- 用户消息 -->
            <div v-else class="flex gap-4 flex-row-reverse group">
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-pink-500 to-rose-500 flex items-center justify-center flex-shrink-0 mt-1 shadow-md">
                <User :size="16" class="text-white" />
              </div>
              <div class="flex-1 min-w-0 flex justify-end">
                <div class="bg-gradient-to-br from-purple-600 to-emerald-500 rounded-2xl px-5 py-3.5 shadow-md max-w-[80%]">
                  <p class="text-white leading-relaxed whitespace-pre-wrap">{{ message.content }}</p>
                </div>
              </div>
            </div>
          </template>

          <!-- 正在输入提示 -->
          <div v-if="isTyping" class="flex gap-4">
            <div class="w-8 h-8 rounded-full bg-gradient-to-br from-purple-600 to-emerald-500 flex items-center justify-center flex-shrink-0 mt-1 shadow-md">
              <Bot :size="16" class="text-white" />
            </div>
            <div class="flex-1">
              <div class="bg-white rounded-2xl px-5 py-3.5 shadow-sm border border-slate-100 inline-flex items-center gap-1.5">
                <span class="w-2 h-2 bg-purple-600 rounded-full animate-bounce"></span>
                <span class="w-2 h-2 bg-purple-600 rounded-full animate-bounce" style="animation-delay: 0.2s"></span>
                <span class="w-2 h-2 bg-purple-600 rounded-full animate-bounce" style="animation-delay: 0.4s"></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="bg-gradient-to-t from-white via-white to-transparent pt-6 pb-6 px-8">
        <div class="max-w-4xl mx-auto">
          <div class="relative bg-white rounded-2xl shadow-xl p-2 flex items-end gap-2 border border-slate-200 focus-within:border-purple-400 focus-within:ring-4 focus-within:ring-purple-600/10 transition-all">
            <textarea
              v-model="messageInput"
              @keydown="handleEnter"
              placeholder="输入你的问题，或者操作说明..."
              class="flex-1 bg-transparent border-none focus:ring-0 py-3 px-4 text-slate-900 text-base resize-none max-h-32 leading-relaxed"
              rows="1"
            ></textarea>
            
            <button 
              @click="sendMessage"
              :disabled="!messageInput.trim() || isTyping"
              class="w-11 h-11 rounded-xl bg-gradient-to-r from-purple-600 to-emerald-500 text-white hover:shadow-lg hover:scale-105 disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:scale-100 transition-all flex items-center justify-center flex-shrink-0"
            >
              <Send :size="18" />
            </button>
          </div>
          <p class="text-[10px] uppercase tracking-widest text-slate-400 font-bold text-center mt-3">AI 生成的内容仅供参考</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}
</style>
