# API 404错误快速诊断指南

## 立即检查清单

### ✅ 1. 确认后端服务已重启

修改Controller的@RequestMapping后，**必须重启Spring Boot应用**才能生效。

```bash
# 停止当前运行的服务（Ctrl+C）
# 然后重新启动
cd backend
mvn spring-boot:run
```

看到以下日志表示启动成功：
```
Started ZijinCollegeApplication in X.XXX seconds
```

### ✅ 2. 检查实际请求URL

打开浏览器开发者工具（F12）→ Network标签 → 点击保存按钮 → 查看：

**期望的请求URL**：
```
http://localhost:8080/api/config/api-config/zhipu
```

**如果看到的是**：
- `http://localhost:8080/api/api/config/...` → 后端路径多了/api
- `http://localhost:8080/config/...` → 前端baseURL没生效
- 其他路径 → 配置有问题

### ✅ 3. 验证后端Controller配置

检查文件：`ConfigController.java`

```java
@RestController
@RequestMapping("/config")  // ✅ 应该是这个，不是 /api/config
@CrossOrigin
public class ConfigController {
    
    @PutMapping("/api-config/{provider}")  // ✅ 这个方法路径
    public Result<?> saveApiConfig(...) {
        ...
    }
}
```

**完整路径计算**：
- 前端baseURL: `/api`
- 前端url: `/config/api-config/zhipu`
- 后端Controller: `/config`
- 后端Method: `/api-config/{provider}`
- **最终匹配**: `/api` + `/config/api-config/zhipu` = `/api/config/api-config/zhipu` ✅

### ✅ 4. 测试后端API直接访问

在浏览器或Postman中直接访问：

```
GET http://localhost:8080/api/config/api-configs
```

**期望返回**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "zhipu": {...},
    "deepseek": {...}
  }
}
```

**如果返回404**：
- 后端服务未启动
- Controller路径配置错误
- 应用上下文路径问题

### ✅ 5. 检查application.yml配置

查看 `backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8080
  servlet:
    context-path: /  # ✅ 确保这里是 /，不是 /api
```

如果context-path是 `/api`，那么所有路径都会多一层 `/api`。

## 常见错误场景

### 场景1：后端未重启
**症状**：修改代码后仍然404
**解决**：重启Spring Boot应用

### 场景2：context-path配置错误
**症状**：所有API都404
**检查**：application.yml中的context-path

### 场景3：CORS跨域问题
**症状**：控制台有CORS错误
**解决**：确保Controller有@CrossOrigin注解

### 场景4：Maven编译问题
**症状**：代码修改未生效
**解决**：
```bash
mvn clean compile
mvn spring-boot:run
```

## 快速修复步骤

```bash
# 1. 停止后端服务（如果在运行）

# 2. 清理并重新编译
cd backend
mvn clean compile

# 3. 重新启动
mvn spring-boot:run

# 4. 等待启动完成，看到 "Started ZijinCollegeApplication"

# 5. 刷新前端页面，重试保存操作
```

## 调试技巧

### 在后端添加日志

在ConfigController中添加：

```java
@PutMapping("/api-config/{provider}")
public Result<?> saveApiConfig(@PathVariable String provider, 
                               @RequestBody Map<String, Object> configData) {
    System.out.println("=== 收到保存配置请求 ===");
    System.out.println("Provider: " + provider);
    System.out.println("Data: " + configData);
    
    // ... 原有代码
}
```

重启后，如果控制台打印了这些信息，说明请求到达了后端。

### 在前端添加日志

在config.ts中：

```typescript
saveApiConfig(provider: string, data: any) {
  console.log('保存API配置:', provider, data)
  return request({
    url: `/config/api-config/${provider}`,
    method: 'put',
    data
  })
}
```

## 如果还是404

请提供以下信息：

1. **浏览器Network标签中的Request URL**
2. **后端启动日志**（最后10行）
3. **application.yml完整内容**
4. **ConfigController.java完整内容**

这样可以精确定位问题所在。
