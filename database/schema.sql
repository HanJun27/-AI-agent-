CREATE DATABASE IF NOT EXISTS zijin_college DEFAULT CHARACTER SET utf8mb4;

USE zijin_college;

-- 用户表
CREATE TABLE sys_user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(200) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 部门表
CREATE TABLE department (
    id INT PRIMARY KEY AUTO_INCREMENT,
    dept_no VARCHAR(20) NOT NULL UNIQUE COMMENT '部门编号',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    location VARCHAR(100) COMMENT '部门位置',
    manager VARCHAR(50) COMMENT '部门负责人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO sys_user (username, password, real_name, avatar) VALUES 
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix');

INSERT INTO department (dept_no, dept_name, location, manager) VALUES 
('A0001', '总经办', '101室', '李雷'),
('A0002', '渠道部', '102室', '韩梅梅'),
('A0003', '市场营销部', '103室', '张三丰'),
('A0004', '教质部', '104室', '李莫愁'),
('A0005', '教学部', '105室', '白字画'),
('A0006', '就业部', '106室', '花千骨');

-- 员工表
CREATE TABLE employee (
    id INT PRIMARY KEY AUTO_INCREMENT,
    emp_no VARCHAR(20) NOT NULL UNIQUE COMMENT '员工编号',
    emp_name VARCHAR(50) NOT NULL COMMENT '员工姓名',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    position VARCHAR(50) COMMENT '职位',
    dept_id INT COMMENT '所属部门ID',
    hire_date DATE COMMENT '入职日期',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-离职, 1-在职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 班级表
CREATE TABLE class_info (
    id INT PRIMARY KEY AUTO_INCREMENT,
    class_no VARCHAR(20) NOT NULL UNIQUE COMMENT '班级编号',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    major VARCHAR(100) COMMENT '专业',
    grade VARCHAR(20) COMMENT '年级',
    teacher VARCHAR(50) COMMENT '班主任',
    student_count INT DEFAULT 0 COMMENT '学生人数',
    classroom VARCHAR(50) COMMENT '教室',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 学生表
CREATE TABLE student (
    id INT PRIMARY KEY AUTO_INCREMENT,
    stu_no VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    stu_name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    class_id INT COMMENT '所属班级ID',
    major VARCHAR(100) COMMENT '专业',
    enrollment_date DATE COMMENT '入学日期',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-退学, 1-在读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入员工测试数据
INSERT INTO employee (emp_no, emp_name, gender, age, phone, email, position, dept_id, hire_date) VALUES 
('E001', '王小明', '男', 28, '13800138001', 'wangxm@zijin.edu.cn', '软件工程师', 1, '2020-03-15'),
('E002', '李小红', '女', 26, '13800138002', 'lixh@zijin.edu.cn', 'UI设计师', 1, '2021-06-01'),
('E003', '张伟', '男', 35, '13800138003', 'zhangw@zijin.edu.cn', '项目经理', 2, '2018-09-10'),
('E004', '刘芳', '女', 30, '13800138004', 'liuf@zijin.edu.cn', '市场专员', 3, '2019-11-20'),
('E005', '陈静', '女', 32, '13800138005', 'chenj@zijin.edu.cn', '教学质量主管', 4, '2017-03-01');

-- 插入班级测试数据
INSERT INTO class_info (class_no, class_name, major, grade, teacher, student_count, classroom) VALUES 
('C2021001', '计算机科学与技术2101班', '计算机科学与技术', '2021级', '赵老师', 45, '教学楼A101'),
('C2021002', '软件工程2101班', '软件工程', '2021级', '钱老师', 42, '教学楼A102'),
('C2022001', '人工智能2201班', '人工智能', '2022级', '孙老师', 40, '教学楼B201'),
('C2022002', '数据科学2201班', '数据科学与大数据技术', '2022级', '李老师', 38, '教学楼B202'),
('C2023001', '网络安全2301班', '网络空间安全', '2023级', '周老师', 43, '教学楼C301');

-- 插入学生测试数据
INSERT INTO student (stu_no, stu_name, gender, age, phone, email, class_id, major, enrollment_date) VALUES 
('S2021001', '张三', '男', 20, '13900139001', 'zhangsan@student.com', 1, '计算机科学与技术', '2021-09-01'),
('S2021002', '李四', '女', 19, '13900139002', 'lisi@student.com', 1, '计算机科学与技术', '2021-09-01'),
('S2021003', '王五', '男', 21, '13900139003', 'wangwu@student.com', 2, '软件工程', '2021-09-01'),
('S2022001', '赵六', '女', 19, '13900139004', 'zhaoliu@student.com', 3, '人工智能', '2022-09-01'),
('S2022002', '孙七', '男', 20, '13900139005', 'sunqi@student.com', 3, '人工智能', '2022-09-01'),
('S2023001', '周八', '女', 18, '13900139006', 'zhouba@student.com', 5, '网络空间安全', '2023-09-01');

-- API配置表
CREATE TABLE api_config (
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

-- 用户偏好设置表
CREATE TABLE user_preferences (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT COMMENT '用户ID',
    language VARCHAR(20) DEFAULT 'zh-CN' COMMENT '语言设置',
    theme VARCHAR(20) DEFAULT 'light' COMMENT '主题设置',
    notifications TINYINT DEFAULT 1 COMMENT '通知提醒: 0-关闭, 1-开启',
    auto_save TINYINT DEFAULT 1 COMMENT '自动保存: 0-关闭, 1-开启',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认API配置
INSERT INTO api_config (provider, provider_name, model, base_url, enabled) VALUES 
('zhipu', '智谱AI', 'glm-4-flash', 'https://open.bigmodel.cn/api/paas/v4', 0),
('deepseek', 'DeepSeek', 'deepseek-chat', 'https://api.deepseek.com/v1', 0),
('qwen', '通义千问', 'qwen-turbo', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 0),
('openai', 'OpenAI', 'gpt-3.5-turbo', 'https://api.openai.com/v1', 0);

-- AI对话会话表
CREATE TABLE chat_session (
    id INT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(100) NOT NULL UNIQUE COMMENT '会话ID',
    title VARCHAR(200) COMMENT '会话标题',
    user_id INT COMMENT '用户ID',
    message_count INT DEFAULT 0 COMMENT '消息数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- AI对话消息表
CREATE TABLE chat_message (
    id INT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
    content TEXT COMMENT '消息内容',
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '消息时间',
    INDEX idx_session (session_id)
);
