# Vite代理配置与后端路径统一修复

## 问题背景

在修复ConfigController的404错误时，发现根本原因是**Vite代理配置缺少rewrite规则**，导致`/api`前缀被重复传递到后端。

## 问题分析

### 原始配置（有问题）

**前端 vite.config.ts**：
```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    // ❌ 缺少 rewrite 规则
  },
}
```

**请求流程**：
1. 前端调用：`request.get('/employees')`
2. axios baseURL添加：`/api/employees`
3. Vite代理转发：`http://localhost:8080/api/employees` ❌（多了/api）
4. 后端期望：`/api/employees`（因为Controller有@RequestMapping("/api/employees")）

这导致：
- ConfigController修改为`/config`后仍然404
- 其他所有Controller都依赖`/api`前缀

### 正确配置

**方案选择**：在Vite代理层去掉`/api`前缀，后端Controller也不包含`/api`

**优点**：
- ✅ 前后端职责清晰：前端负责API版本管理，后端只关心业务路径
- ✅ 便于后续添加API版本：只需修改前端baseURL
- ✅ 符合RESTful规范

## 修复内容

### 1. 前端 Vite 代理配置

**文件**：`frontend/vite.config.ts`

```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),  // ✅ 添加rewrite规则
    },
  },
},
```

**作用**：将 `/api/xxx` 转换为 `/xxx` 再转发给后端

### 2. 后端 Controller 路径修改

移除所有Controller的 `@RequestMapping` 中的 `/api` 前缀：

| Controller | 修改前 | 修改后 |
|-----------|--------|--------|
| EmployeeController | `/api/employees` | `/employees` |
| StudentController | `/api/students` | `/students` |
| ClassInfoController | `/api/classes` | `/classes` |
| DepartmentController | `/api/departments` | `/departments` |
| AuthController | `/api/auth` | `/auth` |
| ConfigController | `/api/config` | `/config` |
| ChatController | `/api/chat` | `/chat` |
| TestController | `/api/test` | `/test` |

## 完整请求流程

以员工列表为例：

```
前端代码
  ↓ request.get('/employees')
axios实例 (baseURL='/api')
  ↓ /api/employees
浏览器发出请求
  ↓ http://localhost:3000/api/employees
Vite代理 (rewrite去掉/api)
  ↓ /employees
转发到后端
  ↓ http://localhost:8080/employees
EmployeeController (@RequestMapping="/employees")
  ↓ 处理请求
返回数据
```

## 路径映射对照表

| 前端调用 | 实际请求URL | 后端匹配路径 | Controller |
|---------|------------|-------------|-----------|
| `/employees` | `/api/employees` → `/employees` | `/employees` | EmployeeController |
| `/students` | `/api/students` → `/students` | `/students` | StudentController |
| `/classes` | `/api/classes` → `/classes` | `/classes` | ClassInfoController |
| `/departments` | `/api/departments` → `/departments` | `/departments` | DepartmentController |
| `/auth/login` | `/api/auth/login` → `/auth/login` | `/auth/login` | AuthController |
| `/config/api-configs` | `/api/config/api-configs` → `/config/api-configs` | `/config/api-configs` | ConfigController |
| `/chat/send` | `/api/chat/send` → `/chat/send` | `/chat/send` | ChatController |

## 重启步骤

### 1. 重启后端服务

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

等待看到：
```
Started ZijinCollegeApplication in X.XXX seconds
```

### 2. 重启前端服务

```bash
cd frontend
npm run dev
```

### 3. 刷新浏览器

按 `Ctrl + F5` 硬刷新，清除缓存。

## 验证方法

### 测试1：员工管理
访问员工管理页面，应该能正常加载数据。

### 测试2：API配置保存
在设置界面保存API配置，应该成功。

### 测试3：对话功能
在AI对话界面发送消息，应该正常响应。

### 检查Network标签

打开浏览器开发者工具 → Network标签，查看：

**员工列表请求**：
- Request URL: `http://localhost:3000/api/employees?pageNum=1&pageSize=10`
- Status: 200 ✅

**配置保存请求**：
- Request URL: `http://localhost:3000/api/config/api-config/zhipu`
- Method: PUT
- Status: 200 ✅

## 常见问题

### Q1: 为什么不在后端保留/api前缀？

**A**: 两种方案都可以，但推荐当前方案：
- **方案A（当前）**：前端baseURL含/api，Vite去掉/api，后端不含/api
  - 优点：后端路径更简洁，易于维护
  - 缺点：需要配置Vite rewrite
  
- **方案B**：前端baseURL不含/api，Vite直接转发，后端含/api
  - 优点：Vite配置简单
  - 缺点：后端所有Controller都要加/api前缀

我们选择方案A是因为它更符合分层架构原则。

### Q2: 如果我想恢复原来的配置怎么办？

**A**: 
1. 删除vite.config.ts中的rewrite行
2. 把所有Controller的@RequestMapping改回 `/api/xxx`
3. 前端request.ts的baseURL改为空字符串 `''`

但不推荐这样做。

### Q3: 为什么要重启前端服务？

**A**: Vite配置文件（vite.config.ts）修改后需要重启开发服务器才能生效。

## 最佳实践

### 前端 API 调用规范

```typescript
// request.ts - 统一配置baseURL
const request = axios.create({
  baseURL: '/api',  // 所有API请求都带/api前缀
  timeout: 10000,
})

// employee.ts - 只写相对路径
export const getEmployees = (params) => {
  return request.get('/employees', { params })  // 不需要写 /api/employees
}
```

### 后端 Controller 规范

```java
@RestController
@RequestMapping("/employees")  // 不包含 /api
@CrossOrigin
public class EmployeeController {
    
    @GetMapping  // 最终路径: /employees
    public Result<?> list() { ... }
    
    @PostMapping  // 最终路径: /employees
    public Result<?> add() { ... }
}
```

### Vite 代理规范

```typescript
// vite.config.ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, ''),  // 统一去掉/api
  },
}
```

## 相关文件清单

### 前端
- ✅ `frontend/vite.config.ts` - 添加rewrite规则

### 后端
- ✅ `EmployeeController.java` - 移除/api前缀
- ✅ `StudentController.java` - 移除/api前缀
- ✅ `ClassInfoController.java` - 移除/api前缀
- ✅ `DepartmentController.java` - 移除/api前缀
- ✅ `AuthController.java` - 移除/api前缀
- ✅ `ConfigController.java` - 移除/api前缀（之前已修改）
- ✅ `ChatController.java` - 移除/api前缀（之前已修改）
- ✅ `TestController.java` - 移除/api前缀

## 总结

这次修复统一了前后端的路径约定：
- **前端**：所有API调用通过axios baseURL自动添加`/api`前缀
- **Vite代理**：转发时去掉`/api`前缀
- **后端**：Controller路径不包含`/api`前缀

这样既保持了代码的整洁性，又避免了路径重复的问题。
