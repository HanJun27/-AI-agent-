-- ============================================
-- AI对话历史功能数据库迁移脚本
-- 执行时间：2026-05-08
-- 说明：添加AI对话会话和消息表
-- ============================================

USE zijin_college;

-- 检查并创建AI对话会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id INT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(100) NOT NULL UNIQUE COMMENT '会话ID',
    title VARCHAR(200) COMMENT '会话标题',
    user_id INT COMMENT '用户ID',
    message_count INT DEFAULT 0 COMMENT '消息数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 检查并创建AI对话消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id INT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
    content TEXT COMMENT '消息内容',
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '消息时间',
    INDEX idx_session (session_id)
);

-- 验证表结构
SELECT 'AI对话会话表结构:' AS info;
DESCRIBE chat_session;

SELECT 'AI对话消息表结构:' AS info;
DESCRIBE chat_message;

SELECT '迁移完成！' AS result;
