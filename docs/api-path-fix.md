# API路径404错误修复说明

## 问题描述
在设置界面保存API配置时出现404错误：
```
AxiosError: Request failed with status code 404
```

## 原因分析

### 路径重复问题
前端使用了统一的axios实例（request.ts），其baseURL配置为 `/api`：

```typescript
// request.ts
const request = axios.create({
  baseURL: '/api',  // 基础路径
  timeout: 10000,
})
```

当前端调用API时：
```typescript
// config.ts
return request({
  url: '/config/api-configs',  // 相对路径
  method: 'get'
})
```

实际请求的完整路径是：`/api` + `/config/api-configs` = `/api/config/api-configs`

但是后端的Controller配置了完整的路径：
```java
// ConfigController.java (错误)
@RequestMapping("/api/config")  // 包含了 /api
```

这导致最终的路径变成：`/api/api/config/...`，造成404错误。

## 解决方案

### 修改后端Controller路径

**原则**：后端Controller的 `@RequestMapping` 不应该包含 `/api` 前缀，因为前端的baseURL已经包含了。

#### 1. ConfigController修复
```java
// 修改前
@RestController
@RequestMapping("/api/config")  // ❌ 错误
@CrossOrigin
public class ConfigController {

// 修改后
@RestController
@RequestMapping("/config")  // ✅ 正确
@CrossOrigin
public class ConfigController {
```

#### 2. ChatController修复
```java
// 修改前
@RestController
@RequestMapping("/api/chat")  // ❌ 错误
@CrossOrigin
public class ChatController {

// 修改后
@RestController
@RequestMapping("/chat")  // ✅ 正确
@CrossOrigin
public class ChatController {
```

## 验证方法

### 1. 检查其他Controller
确保所有Controller都遵循这个规则：

```java
// ✅ 正确的示例
@RestController
@RequestMapping("/employees")  // 不包含 /api
public class EmployeeController {

@RestController
@RequestMapping("/students")  // 不包含 /api
public class StudentController {
```

### 2. 测试API调用
重启后端服务后，测试以下功能：

1. **设置界面**：
   - 加载API配置
   - 保存API配置
   - 加载用户偏好
   - 保存用户偏好

2. **对话界面**：
   - 发送消息
   - 加载会话列表
   - 切换会话
   - 创建新会话

## 最佳实践

### 前后端路径约定

**前端（request.ts）**：
```typescript
const request = axios.create({
  baseURL: '/api',  // 统一的基础路径
  timeout: 10000,
})
```

**后端（Controller）**：
```java
@RestController
@RequestMapping("/模块名")  // 只写模块名，不包含 /api
@CrossOrigin
public class XxxController {
```

**前端调用**：
```typescript
request({
  url: '/模块名/具体接口',  // 相对路径
  method: 'get'
})
```

### 路径映射关系

| 前端调用 | 实际请求 | 后端路径 |
|---------|---------|---------|
| `/config/api-configs` | `/api/config/api-configs` | `/config/api-configs` |
| `/chat/send` | `/api/chat/send` | `/chat/send` |
| `/employees` | `/api/employees` | `/employees` |

## 注意事项

1. **不要在后端重复添加 `/api`** - 这是最常见的错误
2. **保持前后端路径一致** - 前端url + baseURL = 后端完整路径
3. **使用浏览器开发者工具** - 查看Network标签中的实际请求URL
4. **统一规范** - 团队中所有成员都应遵循这个约定

## 相关修改文件

- ✅ `ConfigController.java` - 修改 `@RequestMapping` 为 `/config`
- ✅ `ChatController.java` - 修改 `@RequestMapping` 为 `/chat`

## 重启服务

修改完成后，必须重启Spring Boot后端服务才能生效：

```bash
# 停止当前服务
# 重新启动
mvn spring-boot:run
```

或者在IDE中重新运行Application主类。
