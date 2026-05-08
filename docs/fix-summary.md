# AI对话系统问题修复说明

## 修复的问题列表

### ✅ 1. API配置保存失败
**问题描述**：在设置界面保存API配置时显示"保存失败"

**原因分析**：
- ApiConfigMapper.xml的update语句缺少`provider_name`字段
- 导致更新操作无法正确保存提供商名称

**解决方案**：
- 修改 `ApiConfigMapper.xml` 中的update语句，添加`provider_name`字段
- 文件位置：`backend/src/main/resources/mapper/ApiConfigMapper.xml`

**修复代码**：
```xml
<update id="update">
    UPDATE api_config
    SET provider_name = #{providerName},
        api_key = #{apiKey},
        model = #{model},
        base_url = #{baseUrl},
        enabled = #{enabled}
    WHERE provider = #{provider}
</update>
```

---

### ✅ 2. API配置切换界面后数据丢失
**问题描述**：启用AI模型配置后，切换到其他界面再返回，API配置信息消失

**原因分析**：
- Vue组件使用了keep-alive缓存
- onMounted只在首次挂载时执行
- 从其他页面返回时不会重新加载数据

**解决方案**：
- 在Settings.vue中添加`onActivated`钩子
- 每次激活组件时重新加载数据

**修复代码**：
```typescript
import { ref, onMounted, onActivated } from 'vue'

// 组件挂载时加载数据
onMounted(() => {
  loadApiConfigs()
  loadUserPreferences()
})

// keep-alive 激活时重新加载
onActivated(() => {
  loadApiConfigs()
  loadUserPreferences()
})
```

---

### ✅ 3. Chat界面404错误
**问题描述**：对话界面输入消息时显示"❌ 抱歉，处理您的请求时出现错误：Request failed with status code 404"

**原因分析**：
- Chat.vue直接使用axios，没有使用项目的request模块
- 导致baseURL(`/api`)没有生效
- 请求路径变成 `/api/chat/send` 而不是正确的路径

**解决方案**：
- 导入并使用项目的request模块
- 修正响应数据的访问路径（响应拦截器已返回data）

**修复代码**：
```typescript
// 修改前
import axios from 'axios'
const response = await axios.post('/api/chat/send', {...})
if (response.data.code === 200) {...}

// 修改后
import request from '@/api/request'
const response: any = await request({
  url: '/chat/send',
  method: 'post',
  data: {...}
})
if (response.code === 200) {...}
```

---

### ✅ 4. 历史对话切换功能失效
**问题描述**：点击左侧历史对话列表，无法切换到对应的对话界面

**原因分析**：
- switchSession函数只是清空了消息列表
- 没有从后端加载该会话的历史消息

**解决方案**：
- 实现完整的会话切换逻辑
- 从数据库加载对应会话的消息历史

**修复代码**：
```typescript
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
```

---

### ✅ 5. AI对话记录无法长期存储
**问题描述**：对话记录只保存在内存中，刷新页面后丢失

**解决方案**：
实现了完整的对话历史持久化方案：

#### **数据库设计**
创建了两张新表：
- `chat_session` - 会话表
- `chat_message` - 消息表

#### **后端实现**
1. **实体类**：
   - `ChatSession.java` - 会话实体
   - `ChatMessage.java` - 消息实体

2. **Mapper层**：
   - `ChatSessionMapper.java` + XML
   - `ChatMessageMapper.java` + XML

3. **Service层**：
   - `ChatHistoryService.java`
   - `ChatHistoryServiceImpl.java`

4. **Controller层**：
   - 新增API端点：
     - `GET /api/chat/sessions` - 获取会话列表
     - `GET /api/chat/messages/{sessionId}` - 获取会话消息
     - `POST /api/chat/session` - 创建新会话
     - `DELETE /api/chat/session/{sessionId}` - 删除会话
     - `POST /api/chat/send` - 发送消息（已增强，自动保存）

#### **前端实现**
1. **会话管理**：
   - `loadSessions()` - 加载会话列表
   - `createNewSession()` - 创建新会话并保存到数据库
   - `switchSession()` - 切换会话并加载历史消息
   - `deleteSession()` - 删除会话及所有消息

2. **消息发送**：
   - 发送消息时携带sessionId
   - 后端自动保存用户消息和AI回复

---

## 📋 部署步骤

### 1. 执行数据库迁移
```bash
# 进入数据库目录
cd database

# 执行迁移脚本
mysql -u root -p zijin_college < migration_chat_history.sql
```

或者直接在MySQL客户端中执行`migration_chat_history.sql`的内容。

### 2. 重启后端服务
确保Spring Boot应用重新启动，加载新的Mapper和Service。

### 3. 测试功能
1. **API配置测试**：
   - 进入设置界面
   - 配置一个AI提供商（如智谱AI）
   - 保存配置
   - 切换到其他页面再返回，验证配置是否保留

2. **对话功能测试**：
   - 进入AI对话界面
   - 发送一条消息
   - 刷新页面，验证对话历史是否保留
   - 创建新对话，验证会话列表更新
   - 切换不同会话，验证消息正确加载

---

## 🔧 技术要点

### 响应拦截器处理
项目的request.ts已经配置了响应拦截器：
```typescript
request.interceptors.response.use(
  (response) => {
    const res = response.data  // 直接返回data
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res  // 返回的是Result对象，不是AxiosResponse
  },
  ...
)
```

因此前端调用时：
- ✅ 正确：`response.code`、`response.data`
- ❌ 错误：`response.data.code`、`response.data.data`

### Keep-Alive生命周期
- `onMounted` - 组件首次挂载时执行
- `onActivated` - keep-alive组件被激活时执行
- 两者配合使用确保数据始终最新

---

## ✨ 改进效果

修复后的系统具备以下特性：

1. **配置持久化** - API配置保存到数据库，跨会话保持
2. **对话历史** - 所有对话记录永久保存，可随时查看
3. **多会话管理** - 支持创建、切换、删除多个对话会话
4. **数据同步** - 前后端数据实时同步，无丢失风险
5. **用户体验** - 流畅的界面切换，即时加载历史数据

---

## 📝 注意事项

1. **数据库表必须创建** - 否则对话历史功能无法使用
2. **后端服务必须重启** - 加载新的Mapper和Service
3. **userId暂时硬编码为1** - 后续可从登录token中获取真实用户ID
4. **消息按时间排序** - 确保对话顺序正确

---

## 🚀 后续优化建议

1. **用户认证集成** - 从JWT token中获取真实userId
2. **消息分页加载** - 对于长对话，实现分页加载提升性能
3. **会话标题自动生成** - 根据对话内容自动生成有意义的标题
4. **搜索功能** - 支持搜索历史对话内容
5. **导出功能** - 支持导出对话记录为文本或PDF
