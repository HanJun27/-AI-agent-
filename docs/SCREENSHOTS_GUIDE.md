# 项目截图指南

本文档说明如何生成和添加项目截图到GitHub仓库。

---

## 📁 文件结构

```
docs/
├── screenshots/           # 截图存放目录
│   ├── architecture.png          # 系统架构图（待添加）
│   ├── chat-interface.png        # AI对话界面（待添加）
│   └── data-management.png       # 数据管理界面（待添加）
├── architecture.puml      # PlantUML架构图源码
└── SCREENSHOTS_GUIDE.md   # 本文件
```

---

## 🏗️ 1. 生成系统架构图

### 方法一：使用在线PlantUML编辑器（推荐）

1. **访问在线编辑器**
   ```
   https://www.plantuml.com/plantuml/
   ```

2. **复制PlantUML代码**
   - 打开 `docs/architecture.puml` 文件
   - 复制全部内容

3. **粘贴并生成**
   - 将代码粘贴到在线编辑器的文本框中
   - 点击 "Submit" 按钮
   - 等待图片生成

4. **下载图片**
   - 右键点击生成的图片
   - 选择 "保存图片为..."
   - 保存为 `architecture.png`
   - 保存到 `docs/screenshots/` 目录

### 方法二：使用VS Code插件

1. **安装插件**
   - 在VS Code扩展市场搜索 "PlantUML"
   - 安装 "PlantUML" by jebbs

2. **打开PlantUML文件**
   ```bash
   code docs/architecture.puml
   ```

3. **预览和导出**
   - 按 `Alt + D` 预览图表
   - 右键点击图片区域
   - 选择 "Export Current Diagram"
   - 选择PNG格式
   - 保存到 `docs/screenshots/architecture.png`

### 方法三：本地安装PlantUML

1. **安装Java环境**
   ```bash
   # 检查是否已安装Java
   java -version
   
   # 如果未安装，从 https://adoptium.net/ 下载JDK
   ```

2. **下载PlantUML JAR**
   ```bash
   # 创建tools目录
   mkdir tools
   cd tools
   
   # 下载PlantUML
   curl -L -o plantuml.jar https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml.jar
   ```

3. **生成图片**
   ```bash
   cd ..
   java -jar tools/plantuml.jar docs/architecture.puml -o docs/screenshots
   ```

---

## 💬 2. 截取AI对话界面

### 步骤

1. **启动项目**
   ```bash
   # 启动后端
   cd backend
   mvn spring-boot:run
   
   # 启动前端（新终端）
   cd frontend
   npm run dev
   ```

2. **访问应用**
   ```
   http://localhost:5173
   ```

3. **登录系统**
   - 用户名：admin
   - 密码：123456

4. **进入AI对话页面**
   - 点击左侧菜单 "AI对话"
   - 确保LLM API已配置

5. **执行典型操作**
   
   **示例对话1：添加学生**
   ```
   👤 用户：添加一个学生，学号S2024001，姓名张三，性别男，年龄20，专业计算机科学与技术
   
   🤖 Agent：✅ 成功添加学生：张三（学号：S2024001）
   ```
   
   **示例对话2：查询学生**
   ```
   👤 用户：查询所有计算机专业的学生
   
   🤖 Agent：找到以下学生：
            1. 张三，学号S2024001，年龄20
            2. 李四，学号S2024002，年龄21
   ```
   
   **示例对话3：数据分析**
   ```
   👤 用户：分析学生性别分布
   
   🤖 Agent：学生性别分布统计：
            - 男生：120人（60%）
            - 女生：80人（40%）
   ```

6. **截取屏幕**
   
   **Windows系统：**
   - 使用 Snipaste (推荐)
     - 按 `F1` 开始截图
     - 选择对话窗口区域
     - 按 `Ctrl + S` 保存
   
   - 或使用 Windows 自带截图工具
     - 按 `Win + Shift + S`
     - 选择区域
     - 粘贴到画图工具并保存

   **macOS系统：**
   ```bash
   # 截取选定区域
   Cmd + Shift + 4
   
   # 截取整个屏幕
   Cmd + Shift + 3
   ```

7. **保存文件**
   - 文件名：`chat-interface.png`
   - 保存位置：`docs/screenshots/`
   - 建议尺寸：1920x1080 或更高

### 截图要点

✅ **应该包含：**
- 完整的对话窗口
- 至少3轮对话（展示多轮交互）
- 用户输入和Agent回复
- 操作成功的反馈信息
- 如果有数据统计，包含可视化图表

❌ **不应该包含：**
- 敏感信息（真实姓名、电话等）
- 浏览器地址栏
- 操作系统任务栏
- 无关的桌面内容

---

## 📊 3. 截取数据管理界面

### 步骤

1. **访问数据管理页面**
   - 点击左侧菜单 "学生管理" 或 "员工管理"

2. **展示功能特性**
   
   **列表视图：**
   - 显示完整的数据表格
   - 展示分页功能
   - 显示操作按钮（编辑、删除）
   
   **搜索功能：**
   - 在搜索框输入关键词
   - 展示搜索结果
   
   **表单操作：**
   - 点击 "添加" 按钮
   - 展示添加/编辑表单
   - 填写示例数据（不要提交）

3. **截取多个角度**（可选）
   
   **角度1：学生管理列表**
   - 展示学生列表
   - 包含搜索框和操作按钮
   
   **角度2：添加/编辑表单**
   - 展示表单字段
   - 显示验证提示
   
   **角度3：数据统计**
   - 如果有统计图表，一并截取

4. **保存文件**
   - 文件名：`data-management.png`
   - 保存位置：`docs/screenshots/`
   - 建议尺寸：1920x1080 或更高

### 截图要点

✅ **应该包含：**
- 清晰的表格数据
- Element Plus组件的美观设计
- 搜索、筛选功能
- 分页控件
- 操作按钮（增删改查）

❌ **不应该包含：**
- 测试数据中的敏感信息
- 空白的表格
- 错误提示信息（除非特意展示）

---

## 🎨 截图优化建议

### 图片质量

1. **分辨率**
   - 最低：1280x720 (HD)
   - 推荐：1920x1080 (Full HD)
   - 最佳：2560x1440 (2K)

2. **文件格式**
   - 使用 PNG 格式（无损压缩）
   - 避免使用 JPG（有损压缩）

3. **文件大小**
   - 单张图片控制在 500KB - 2MB
   - 过大会影响GitHub加载速度

### 图片美化

1. **添加边框**（可选）
   ```bash
   # 使用ImageMagick添加边框
   convert input.png -border 10 -bordercolor white output.png
   ```

2. **添加标注**（可选）
   - 使用 Snipaste 添加箭头、文字标注
   - 突出显示重要功能点

3. **统一风格**
   - 所有截图使用相同的浏览器主题
   - 保持一致的缩放比例
   - 使用浅色模式（更易阅读）

### 浏览器设置

1. **开发者工具**
   - 按 `F12` 打开开发者工具
   - 切换到 "Device Toolbar"
   - 选择 "Responsive" 或固定尺寸

2. **清除缓存**
   - 按 `Ctrl + Shift + Delete`
   - 清除缓存和Cookie
   - 确保显示最新数据

3. **禁用扩展**
   - 临时禁用广告拦截器等扩展
   - 避免影响页面显示

---

## 📤 上传到GitHub

### 方法一：命令行上传

```bash
# 进入项目目录
cd e:\工作台B\2026年春课设\java课设\zijin-college-system

# 添加截图文件
git add docs/screenshots/*.png

# 提交
git commit -m "docs: Add project screenshots

- System architecture diagram (generated from PlantUML)
- AI chat interface screenshot
- Data management interface screenshot"

# 推送
git push origin main
```

### 方法二：GitHub Desktop

1. 打开 GitHub Desktop
2. 查看 Changes 标签
3. 勾选 `docs/screenshots/` 下的文件
4. 填写 Commit message
5. 点击 "Commit to main"
6. 点击 "Push origin"

### 方法三：直接拖拽

1. 访问 GitHub 仓库页面
2. 进入 `docs/screenshots/` 目录
3. 点击 "Add file" > "Upload files"
4. 拖拽图片文件到上传区域
5. 填写 Commit message
6. 点击 "Commit changes"

---

## ✅ 验证清单

上传后，访问 https://github.com/HanJun27/-AI-agent-/ 确认：

- [ ] README中的图片正常显示
- [ ] 图片加载速度快（<2秒）
- [ ] 图片清晰可读
- [ ] 点击图片可以放大查看
- [ ] 移动端也能正常显示

---

## 🔧 故障排除

### 问题1：图片不显示

**可能原因：**
- 文件路径错误
- 文件名大小写不匹配
- 图片未正确上传

**解决方案：**
```bash
# 检查文件是否存在
ls docs/screenshots/

# 检查Git状态
git status

# 重新上传
git add docs/screenshots/*.png
git commit -m "fix: Re-upload screenshots"
git push
```

### 问题2：图片太大

**解决方案：**
```bash
# 使用ImageMagick压缩
convert input.png -quality 85 output.png

# 或使用在线工具
# https://tinypng.com/
```

### 问题3：PlantUML生成失败

**可能原因：**
- Java未安装
- PlantUML版本不兼容
- 语法错误

**解决方案：**
- 检查Java版本：`java -version`（需要Java 8+）
- 使用在线编辑器代替本地安装
- 检查 `.puml` 文件语法

---

## 📚 相关资源

- **PlantUML官方文档**: https://plantuml.com/zh/
- **PlantUML在线编辑器**: https://www.plantuml.com/plantuml/
- **截图工具推荐**:
  - Windows: Snipaste, ShareX
  - macOS: Shottr, CleanShot X
  - Cross-platform: Lightshot
- **图片优化工具**:
  - TinyPNG: https://tinypng.com/
  - Squoosh: https://squoosh.app/

---

## 📝 更新记录

| 日期 | 操作 | 说明 |
|------|------|------|
| 2026-05-09 | 创建文档 | 初始版本，包含截图指南 |

---

**祝您顺利完成截图添加！** 📸✨
