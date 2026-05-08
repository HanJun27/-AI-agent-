# 截图占位符

此目录用于存放项目截图，请添加以下图片：

## 必需的图片

1. **architecture.png** - 系统架构图
   - 从 `../architecture.puml` 生成
   - 建议使用PlantUML在线编辑器或VS Code插件生成
   - 推荐尺寸：1920x1080

2. **chat-interface.png** - AI对话界面截图
   - 运行项目后截取AI对话页面
   - 展示自然语言交互功能
   - 推荐尺寸：1920x1080

3. **data-management.png** - 数据管理界面截图
   - 截取学生管理或员工管理页面
   - 展示CRUD操作界面
   - 推荐尺寸：1920x1080

## 如何生成截图

详细步骤请参考：[SCREENSHOTS_GUIDE.md](./SCREENSHOTS_GUIDE.md)

### 快速生成架构图

1. 访问 https://www.plantuml.com/plantuml/
2. 复制 `architecture.puml` 的内容
3. 粘贴并点击 "Submit"
4. 右键保存图片为 `architecture.png`

### 截取界面

1. 启动项目（后端 + 前端）
2. 访问 http://localhost:5173
3. 使用截图工具（如Snipaste）截取
4. 保存到此目录

---

**提示**：添加截图后，请在README中查看效果！
