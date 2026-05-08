-- ============================================
-- 设置功能数据库迁移脚本
-- 执行时间：2026-05-08
-- 说明：添加API配置和用户偏好设置表
-- ============================================

USE zijin_college;

-- 检查并创建API配置表
CREATE TABLE IF NOT EXISTS api_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    provider VARCHAR(50) NOT NULL UNIQUE COMMENT 'AI提供商(zhipu/deepseek/qwen/openai)',
    provider_name VARCHAR(100) NOT NULL COMMENT '提供商名称',
    api_key VARCHAR(500) COMMENT 'API密钥',
    model VARCHAR(100) COMMENT '使用的模型',
    base_url VARCHAR(500) COMMENT 'API基础URL',
    enabled TINYINT DEFAULT 0 COMMENT '是否启用: 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 检查并创建用户偏好设置表
CREATE TABLE IF NOT EXISTS user_preferences (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT COMMENT '用户ID',
    language VARCHAR(20) DEFAULT 'zh-CN' COMMENT '语言设置',
    theme VARCHAR(20) DEFAULT 'light' COMMENT '主题设置',
    notifications TINYINT DEFAULT 1 COMMENT '通知提醒: 0-关闭, 1-开启',
    auto_save TINYINT DEFAULT 1 COMMENT '自动保存: 0-关闭, 1-开启',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认API配置（如果不存在）
INSERT IGNORE INTO api_config (provider, provider_name, model, base_url, enabled) VALUES 
('zhipu', '智谱AI', 'glm-4-flash', 'https://open.bigmodel.cn/api/paas/v4', 0),
('deepseek', 'DeepSeek', 'deepseek-chat', 'https://api.deepseek.com/v1', 0),
('qwen', '通义千问', 'qwen-turbo', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 0),
('openai', 'OpenAI', 'gpt-3.5-turbo', 'https://api.openai.com/v1', 0);

-- 验证数据
SELECT 'API配置表数据:' AS info;
SELECT * FROM api_config;

SELECT '用户偏好设置表结构已创建' AS info;
