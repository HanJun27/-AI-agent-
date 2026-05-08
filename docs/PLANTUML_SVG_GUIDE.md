# PlantUML SVG 嵌入指南

## 🚀 快速方案：使用PlantUML在线服务器

### 步骤1：编码PlantUML代码

PlantUML服务器需要URL编码的代码。我们可以使用在线工具或脚本。

**在线工具**：
1. 访问：https://www.plantuml.com/plantuml/
2. 粘贴您的 `.puml` 代码
3. 点击 "Submit"
4. 复制浏览器地址栏的URL

**URL格式**：
```
https://www.plantuml.com/plantuml/svg/<encoded-code>
```

### 步骤2：在README中嵌入SVG

```markdown
![系统架构图](https://www.plantuml.com/plantuml/svg/<encoded-code>)
```

---

## 💡 实用方案：使用GitHub Actions自动转换

### 创建自动化工作流

创建一个GitHub Action，每次推送时自动将 `.puml` 转换为 SVG 并提交。

**文件**: `.github/workflows/plantuml.yml`

```yaml
name: Generate PlantUML Diagrams

on:
  push:
    paths:
      - 'docs/**/*.puml'

jobs:
  generate-diagrams:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
      
    - name: Setup Java
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        
    - name: Download PlantUML
      run: |
        wget https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml.jar
        
    - name: Generate SVG diagrams
      run: |
        java -jar plantuml.jar -tsvg docs/architecture.puml -o docs/screenshots
        
    - name: Commit and push SVG
      run: |
        git config --local user.email "action@github.com"
        git config --local user.name "GitHub Action"
        git add docs/screenshots/*.svg
        git diff --staged --quiet || git commit -m "docs: Auto-generate PlantUML SVG diagrams"
        git push
```

然后在README中使用：
```markdown
![系统架构图](docs/screenshots/architecture.svg)
```

---

## 🎨 最佳实践方案：手动转换并上传SVG

### 步骤1：本地生成SVG

**方法A：使用PlantUML命令行**
```bash
# 下载PlantUML JAR
wget https://github.com/plantuml/plantuml/releases/download/v1.2024.0/plantuml.jar

# 生成SVG
java -jar plantuml.jar -tsvg docs/architecture.puml -o docs/screenshots
```

**方法B：使用VS Code插件**
1. 安装 "PlantUML" 扩展
2. 打开 `docs/architecture.puml`
3. 按 `Alt + D` 预览
4. 右键 → "Export Current Diagram" → 选择SVG格式
5. 保存到 `docs/screenshots/architecture.svg`

**方法C：使用在线编辑器**
1. 访问 https://www.plantuml.com/plantuml/
2. 粘贴代码
3. 点击 "Submit"
4. 右键图片 → "保存图片"（选择SVG格式）

### 步骤2：在README中引用SVG

```markdown
### 🏗️ 系统架构图

![系统架构图](docs/screenshots/architecture.svg)

> 💡 SVG格式支持无损缩放，可在任何分辨率下清晰显示
```

### 步骤3：提交到GitHub

```bash
git add docs/screenshots/architecture.svg
git commit -m "docs: Add architecture diagram in SVG format"
git push origin main
```

---

## 🔧 我为您准备的解决方案

基于您的需求，我建议采用**方案3（手动转换SVG）**，原因：
1. ✅ SVG是矢量格式，无限缩放不失真
2. ✅ GitHub原生支持SVG渲染
3. ✅ 文件大小比PNG小
4. ✅ 无需外部依赖，加载速度快

### 立即执行

让我帮您生成SVG文件：
