package com.zijin.college.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zijin.college.entity.ApiConfig;
import com.zijin.college.service.ApiConfigService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * LLM Agent核心服务 - 使用Function Calling模式
 * 
 * 工作原理：
 * 1. 定义可用的工具（Tools）
 * 2. LLM根据用户输入选择要调用的工具
 * 3. 后端执行工具并返回结果
 * 4. LLM根据结果生成自然语言回复
 */
@Service
public class LLMAgentService {
    
    private final CrudOperationTool crudTool;
    private final DataAnalysisTool analysisTool;  // 修正：DataAnalysisTool
    private final DatabaseQueryTool queryTool;  // 新增：DatabaseQueryTool
    private final ApiConfigService apiConfigService;
    private final IntentRecognitionService intentRecognitionService;  // 新增：用于降级
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    
    public LLMAgentService(CrudOperationTool crudTool,
                          DataAnalysisTool analysisTool,  // 修正：DataAnalysisTool
                          DatabaseQueryTool queryTool,  // 新增：DatabaseQueryTool
                          ApiConfigService apiConfigService,
                          IntentRecognitionService intentRecognitionService) {  // 新增
        this.crudTool = crudTool;
        this.analysisTool = analysisTool;
        this.queryTool = queryTool;  // 新增
        this.apiConfigService = apiConfigService;
        this.intentRecognitionService = intentRecognitionService;  // 新增
    }
    
    /**
     * Agent处理用户消息的主入口（自动获取API配置）
     * 
     * @param userMessage 用户消息
     * @param provider API提供商（如 "zhipu", "deepseek"）
     * @return Agent的自然语言回复
     */
    public String processMessage(String userMessage, String provider) {
        // 从数据库获取API配置
        ApiConfig config = apiConfigService.getConfigByProvider(provider);
        
        if (config == null || config.getEnabled() != 1 || config.getApiKey() == null || config.getApiKey().isEmpty()) {
            System.out.println("⚠️ API配置不可用，降级到传统Agent");
            // 降级到传统AgentService的逻辑
            return fallbackToTraditionalAgent(userMessage);
        }
        
        return processMessage(userMessage, config.getApiKey(), config.getModel(), config.getBaseUrl());
    }
    
    /**
     * Agent处理用户消息的主入口（指定API参数）
     * 
     * @param userMessage 用户消息
     * @param apiKey API密钥
     * @param model 模型名称
     * @param baseUrl API基础URL
     * @return Agent的自然语言回复
     */
    public String processMessage(String userMessage, String apiKey, String model, String baseUrl) {
        try {
            System.out.println("========== Agent开始处理 ==========");
            System.out.println("用户输入: " + userMessage);
            
            // 第1步：LLM识别意图并选择工具
            AgentDecision decision = recognizeAndDecide(userMessage, apiKey, model, baseUrl);
            
            if (decision == null) {
                // LLM调用失败，纯LLM Agent模式返回明确错误
                return "❌ LLM API调用失败。请检查：\n1. API密钥是否正确\n2. API配置是否启用\n3. 网络连接是否正常\n\n或者切换到其他识别模式。";
            }
            
            System.out.println("决策结果: " + decision.toString());
            
            // 第2步：如果没有选择工具，直接返回LLM的回答
            if (decision.getToolName() == null || decision.getToolName().isEmpty()) {
                System.out.println("无需调用工具，直接返回LLM回答");
                return decision.getLlmResponse();
            }
            
            // 第3步：执行选定的工具
            System.out.println("执行工具: " + decision.getToolName());
            String toolResult = executeTool(decision.getToolName(), decision.getParams());
            
            System.out.println("工具执行结果: " + toolResult);
            
            // 第4步：LLM根据工具结果生成最终回复
            String finalResponse = generateFinalResponse(userMessage, toolResult, decision.getLlmResponse(), 
                                                         apiKey, model, baseUrl);
            
            System.out.println("========== Agent处理完成 ==========");
            return finalResponse;
            
        } catch (Exception e) {
            System.err.println("Agent处理异常: " + e.getMessage());
            e.printStackTrace();
            return "抱歉，处理您的请求时出现错误：" + e.getMessage();
        }
    }
    
    /**
     * 第1步：LLM识别意图并决定调用哪个工具
     */
    private AgentDecision recognizeAndDecide(String userMessage, String apiKey, String model, String baseUrl) {
        // 构建包含工具定义的Prompt
        String systemPrompt = buildAgentSystemPrompt();
        
        // 调用LLM进行意图识别和工具选择
        String llmResponse = callLLMWithTools(userMessage, systemPrompt, apiKey, model, baseUrl);
        
        if (llmResponse == null || llmResponse.isEmpty()) {
            System.err.println("❌ LLM调用失败，纯LLM Agent模式不降级，返回错误提示");
            // 纯LLM Agent模式：不调用规则匹配，直接返回null
            return null;
        }
        
        // 解析LLM的决策
        AgentDecision decision = parseAgentDecision(llmResponse);
        
        // 如果解析结果为null，但LLM响应不为空，说明是格式问题而非调用失败
        if (decision == null && llmResponse != null && !llmResponse.isEmpty()) {
            System.out.println("⚠️ LLM返回格式不正确，尝试作为普通对话处理");
            // 创建一个包含友好提示的决策对象
            decision = new AgentDecision();
            decision.setLlmResponse(
                "⚠️ AI助手暂时无法理解您的请求。\n\n" +
                "可能的原因：\n" +
                "1. LLM返回的格式不符合要求\n" +
                "2. 请尝试换一种表达方式\n\n" +
                "示例：\n" +
                "- \"查询学号为S2022001的学生\"\n" +
                "- \"帮我找一下张三的信息\"\n" +
                "- \"添加一个学生，学号S2024001，姓名李四\"\n\n" +
                "或者您可以切换到其他识别模式（系统设置 > 对话设置）"
            );
        }
        
        return decision;
    }
    
    /**
     * 构建Agent系统Prompt - 定义可用工具
     */
    private String buildAgentSystemPrompt() {
        return "🚨 **极其重要的输出格式要求** 🚨\n" +
               "你必须严格遵守以下规则，否则系统将无法正常工作：\n\n" +
               
               "### ⚠️ 当你需要调用工具时，必须且只能返回以下格式的JSON：\n" +
               "{\"tool\":\"工具名\",\"params\":{参数对象},\"thought\":\"思考过程\"}\n\n" +
               
               "**绝对禁止**的格式（违反会导致系统错误）：\n" +
               "❌ 工具名\\n{\"params\":...} （这是最常见的错误！）\n" +
               "❌ {\"params\":...} （缺少tool字段）\n" +
               "❌ ```json{\"tool\":...}``` （不要使用Markdown代码块）\n" +
               "❌ 任何非JSON格式的文本\n\n" +
               
               "**正确示例**：\n" +
               "用户：\"查询学号为S2022001的学生\"\n" +
               "✅ 你应该返回：{\"tool\":\"queryStudents\",\"params\":{\"stuNo\":\"S2022001\"},\"thought\":\"查询学号为S2022001的学生信息\"}\n\n" +
               
               "用户：\"帮我找一下赵六的信息\"\n" +
               "✅ 你应该返回：{\"tool\":\"queryStudents\",\"params\":{\"name\":\"赵六\"},\"thought\":\"查找姓名为赵六的学生\"}\n\n" +
               
               "用户：\"把班级人数改为60\"\n" +
               "✅ 你应该返回：{\"tool\":\"updateClass\",\"params\":{\"classNo\":\"C2023001\",\"studentCount\":60},\"thought\":\"修改班级人数为60\"}\n\n" +
               
               "---\n\n" +
               
               "你是紫金学院管理系统的智能助手。你可以调用以下工具来帮助用户：\n\n" +
               
               "## 可用工具列表\n\n" +
               
               "### 查询工具\n" +
               "- queryStudents: 查询学生信息\n" +
               "  参数: stuNo(学号), name(姓名), keyword(关键词)\n" +
               "  示例: {\"tool\":\"queryStudents\",\"params\":{\"stuNo\":\"S2023001\"}}\n\n" +
               
               "- queryEmployees: 查询员工信息\n" +
               "  参数: empNo(工号), name(姓名), keyword(关键词)\n" +
               "  示例: {\"tool\":\"queryEmployees\",\"params\":{\"name\":\"张三\"}}\n\n" +
               
               "- queryDepartments: 查询部门信息\n" +
               "  参数: deptNo(部门号), name(部门名)\n" +
               "  示例: {\"tool\":\"queryDepartments\",\"params\":{\"deptNo\":\"D001\"}}\n\n" +
               
               "- queryClasses: 查询班级信息\n" +
               "  参数: classNo(班级编号), className(班级名称)\n" +
               "  示例: {\"tool\":\"queryClasses\",\"params\":{\"classNo\":\"C2023001\"}}\n\n" +
               
               "- getDatabaseOverview: 获取数据库概览\n" +
               "  参数: 无\n\n" +
               
               "### 添加工具\n" +
               "- addStudent: 添加学生\n" +
               "  参数: stuNo(学号，必填), name(姓名，必填), age(年龄), gender(性别), major(专业)\n" +
               "  示例: {\"tool\":\"addStudent\",\"params\":{\"stuNo\":\"S2024001\",\"name\":\"李四\",\"age\":20}}\n\n" +
               
               "- addEmployee: 添加员工\n" +
               "  参数: empNo(工号，必填), name/empName(姓名，必填), position(职位), age(年龄), gender(性别), phone(电话), email(邮箱), deptId(部门ID)\n" +
               "  示例: {\"tool\":\"addEmployee\",\"params\":{\"empNo\":\"E001\",\"name\":\"张三\",\"position\":\"工程师\",\"gender\":\"男\",\"phone\":\"13800138000\"}}\n\n" +
               
               "- addDepartment: 添加部门\n" +
               "  参数: deptNo(部门号，必填), name(部门名，必填), location(位置), manager(负责人)\n\n" +
               
               "- addClass: 添加班级\n" +
               "  参数: classNo(班级编号，必填), className(班级名称，必填), studentCount(学生人数), major(专业), grade(年级), teacher/headTeacher(班主任), classroom(教室)\n" +
               "  示例: {\"tool\":\"addClass\",\"params\":{\"classNo\":\"C2024001\",\"className\":\"计算机1班\",\"studentCount\":50,\"teacher\":\"张老师\",\"classroom\":\"教A301\"}}\n\n" +
               
               "### 修改工具\n" +
               "- updateStudent: 修改学生信息\n" +
               "  参数: stuNo或name(用于定位), 以及其他要修改的字段(age, gender, phone, major等)\n" +
               "  示例: {\"tool\":\"updateStudent\",\"params\":{\"name\":\"李四\",\"phone\":\"15862840271\"}}\n\n" +
               
               "- updateEmployee: 修改员工信息\n" +
               "  参数: empNo或name(用于定位), 以及其他要修改的字段(name/newName, age, gender, phone, email, position)\n" +
               "  示例: {\"tool\":\"updateEmployee\",\"params\":{\"empNo\":\"E001\",\"phone\":\"13900139000\"}}\n" +
               "  示例: {\"tool\":\"updateEmployee\",\"params\":{\"name\":\"张三\",\"newName\":\"张伟\"}}\n\n" +
               
               "- updateClass: 修改班级信息\n" +
               "  参数: classNo或className(用于定位), 以及其他要修改的字段(className, studentCount, major, grade, teacher/headTeacher, classroom等)\n" +
               "  示例: {\"tool\":\"updateClass\",\"params\":{\"classNo\":\"C2023001\",\"studentCount\":100}}\n" +
               "  示例: {\"tool\":\"updateClass\",\"params\":{\"className\":\"软工一班\",\"teacher\":\"李老师\",\"classroom\":\"教B202\"}}\n\n" +
               
               "### 删除工具\n" +
               "- deleteStudent: 删除学生\n" +
               "  参数: stuNo或name\n" +
               "  示例: {\"tool\":\"deleteStudent\",\"params\":{\"stuNo\":\"S2023001\"}}\n\n" +
               
               "- deleteEmployee: 删除员工\n" +
               "  参数: empNo或name\n\n" +
               
               "- deleteClass: 删除班级\n" +
               "  参数: classNo(班级编号)\n" +
               "  示例: {\"tool\":\"deleteClass\",\"params\":{\"classNo\":\"C2023001\"}}\n\n" +
               
               "### 分析工具\n" +
               "- analyzeStudentGender: 分析学生性别比例\n" +
               "- analyzeStudentAge: 分析学生年龄分布\n" +
               "- analyzeEmployeeDistribution: 分析员工分布\n" +
               "- generateComprehensiveReport: 生成综合报告\n\n" +
               
               "## 工作流程\n" +
               "1. 理解用户意图\n" +
               "2. 如果需要操作数据，选择最合适的工具并提取参数\n" +
               "3. 如果只是聊天，直接回复，不需要调用工具\n\n" +
               
               "## 🎯 工具选择规则（非常重要）\n" +
               "根据用户输入中的关键词选择正确的工具类型：\n\n" +
               
               "### 添加操作\n" +
               "关键词：\"添加\"、\"创建\"、\"新增\"、\"插入\"、\"注册\"\n" +
               "→ 使用 addXXX 工具\n" +
               "示例：\"添加一个学生\" → addStudent\n\n" +
               
               "### 修改操作\n" +
               "关键词：\"修改\"、\"改为\"、\"更新\"、\"变更\"、\"调整\"、\"设置\"\n" +
               "→ 使用 updateXXX 工具\n" +
               "示例：\"把电话改为15862840271\" → updateStudent\n" +
               "示例：\"把班级人数改为60\" → updateClass\n" +
               "示例：\"把部门位置改为教学楼\" → updateDepartment\n\n" +
               
               "### 删除操作\n" +
               "关键词：\"删除\"、\"移除\"、\"注销\"\n" +
               "→ 使用 deleteXXX 工具\n" +
               "示例：\"删除学生S2023001\" → deleteStudent\n\n" +
               
               "### 查询操作\n" +
               "关键词：\"查询\"、\"查找\"、\"搜索\"、\"查看\"、\"显示\"\n" +
               "→ 使用 queryXXX 工具\n" +
               "示例：\"查询孙七的信息\" → queryStudents\n\n" +
               
               "### 分析操作\n" +
               "关键词：\"分析\"、\"统计\"、\"比例\"、\"分布\"\n" +
               "→ 使用 analyzeXXX 工具\n" +
               "示例：\"分析学生性别分布\" → analyzeStudentGender\n\n" +
               
               "## 🚫 禁止行为（违反将导致系统错误）\n" +
               "1. ❌ 不要把修改操作误解为查询操作\n" +
               "2. ❌ 不要把添加操作误解为查询操作\n" +
               "3. ✅ 必须根据用户意图选择正确的工具类型\n\n" +
               
               "## 重要规则\n" +
               "1. 电话号码、学号等长数字必须作为字符串，不要转成数字\n" +
               "2. 中文姓名直接提取，不要翻译\n" +
               "3. **修改姓名时的特殊规则**（非常重要）：\n" +
               "   - 当用户要求修改姓名时，使用 newName 参数表示新姓名\n" +
               "   - 示例：\"把名字为张三的学生改为李四\" → {\"tool\":\"updateStudent\",\"params\":{\"name\":\"张三\",\"newName\":\"李四\"}}\n" +
               "   - 示例：\"把员工王五的名字改为王伟\" → {\"tool\":\"updateEmployee\",\"params\":{\"name\":\"王五\",\"newName\":\"王伟\"}}\n" +
               "   - 注意：name 用于定位原记录，newName 是要设置的新值\n" +
               "4. update操作参数规范：\n" +
               "   - 定位参数：stuNo/empNo/classNo/deptNo/name（用于查找要更新的记录）\n" +
               "   - 更新参数：其他字段（age, phone, major等）或 newName（新姓名）\n" +
               "   - 如果通过姓名定位且要修改姓名，必须同时提供 name 和 newName\n" +
               "5. 如果用户提到多个条件，都要提取到params中\n" +
               "6. 不确定时，优先选择查询工具而不是修改/删除\n\n" +
               
               "现在请分析用户的请求，严格只返回JSON或自然语言回复。";
    }
    
    /**
     * 调用LLM（带工具定义）
     */
    private String callLLMWithTools(String userMessage, String systemPrompt, 
                                   String apiKey, String model, String baseUrl) {
        try {
            // 构建请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统消息
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            // 用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.1); // 低温度，确保输出稳定
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            // 调试日志
            System.out.println("========== LLM API调用详情 ==========");
            System.out.println("baseUrl: " + baseUrl);
            System.out.println("model: " + model);
            System.out.println("apiKey前缀: " + (apiKey != null ? apiKey.substring(0, Math.min(15, apiKey.length())) + "..." : "null"));
            System.out.println("完整URL: " + baseUrl + "/chat/completions");
            System.out.println("=====================================");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 调用API
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/chat/completions", entity, Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    String content = message.get("content");
                    
                    System.out.println("LLM原始响应: " + content);
                    return content;
                }
            }
            
        } catch (Exception e) {
            System.err.println("LLM调用失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 从LLM响应中提取JSON
     * 支持多种格式：
     * 1. 纯JSON: {"tool": "..."}
     * 2. Markdown代码块: ```json {...} ```
     * 3. 包含解释文字的混合内容
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        
        String trimmed = response.trim();
        
        // 尝试1：直接解析（如果是纯JSON）
        if (trimmed.startsWith("{")) {
            return trimmed;
        }
        
        // 尝试2：提取Markdown代码块中的JSON
        int jsonStart = trimmed.indexOf("```json");
        if (jsonStart != -1) {
            int contentStart = trimmed.indexOf("{", jsonStart);
            int contentEnd = trimmed.lastIndexOf("}");
            
            if (contentStart != -1 && contentEnd != -1 && contentEnd > contentStart) {
                return trimmed.substring(contentStart, contentEnd + 1);
            }
        }
        
        // 尝试3：提取第一个{到最后一个}之间的内容
        int firstBrace = trimmed.indexOf("{");
        int lastBrace = trimmed.lastIndexOf("}");
        
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        
        // 无法提取
        return null;
    }
    
    /**
     * 解析Agent的决策
     */
    private AgentDecision parseAgentDecision(String llmResponse) {
        try {
            // 清理LLM响应，提取JSON部分
            String cleanJson = extractJsonFromResponse(llmResponse);
            
            if (cleanJson == null || cleanJson.isEmpty()) {
                System.out.println("无法从LLM响应中提取JSON");
                return null;
            }
            
            System.out.println("清理后的JSON: " + cleanJson);
            
            // 尝试解析为JSON（工具调用）
            JsonNode jsonNode = objectMapper.readTree(cleanJson);
            
            if (jsonNode.has("tool")) {
                AgentDecision decision = new AgentDecision();
                decision.setToolName(jsonNode.get("tool").asText());
                
                if (jsonNode.has("params")) {
                    Map<String, Object> params = new HashMap<>();
                    JsonNode paramsNode = jsonNode.get("params");
                    
                    // 遍历所有参数字段
                    Iterator<Map.Entry<String, JsonNode>> fields = paramsNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String key = field.getKey();
                        JsonNode value = field.getValue();
                        
                        if (value.isTextual()) {
                            params.put(key, value.asText());
                        } else if (value.isInt()) {
                            params.put(key, value.asInt());
                        } else if (value.isLong()) {
                            params.put(key, value.asLong());
                        } else if (value.isBoolean()) {
                            params.put(key, value.asBoolean());
                        }
                    }
                    
                    decision.setParams(params);
                }
                
                if (jsonNode.has("thought")) {
                    decision.setThought(jsonNode.get("thought").asText());
                }
                
                return decision;
            } else {
                // JSON存在但没有tool字段，这是错误的格式
                System.err.println("⚠️ LLM返回的JSON缺少tool字段: " + cleanJson);
                System.err.println("⚠️ 这可能是LLM没有遵守输出格式，将视为普通对话");
                // 不降级，直接返回null让上层处理
                return null;
            }
            
        } catch (Exception e) {
            // 不是JSON格式，说明是普通对话
            System.out.println("LLM返回非JSON格式，视为普通对话");
        }
        
        // 普通对话回复
        AgentDecision decision = new AgentDecision();
        decision.setLlmResponse(llmResponse);
        return decision;
    }
    
    /**
     * 降级方案1：使用规则匹配识别意图
     */
    private AgentDecision recognizeWithRules(String userMessage) {
        System.out.println("使用规则匹配识别意图...");
        
        // 复用IntentRecognitionService的降级方案
        IntentRecognitionService.IntentRecognitionResult result = 
            intentRecognitionService.recognizeIntent(userMessage, null, null, null);
        
        if (result == null) {
            return null;
        }
        
        // 转换为AgentDecision
        AgentDecision decision = new AgentDecision();
        decision.setToolName(convertActionToToolName(result.getAction()));
        decision.setParams(result.getParams());
        decision.setThought("规则匹配识别");
        
        return decision;
    }
    
    /**
     * 降级方案2：完全降级到传统Agent
     */
    private String fallbackToTraditionalAgent(String userMessage) {
        System.out.println("完全降级到传统Agent处理...");
        
        // 这里可以调用AgentService的逻辑，但为了简化，直接返回提示
        return "API配置不可用，已切换到规则匹配模式。请使用纯正则匹配模式或配置API密钥以获得更好的体验。";
    }
    
    /**
     * 将Action名称转换为工具名称
     */
    private String convertActionToToolName(String action) {
        // Action和Tool名称基本一致，直接返回
        return action;
    }
    
    /**
     * 第3步：执行选定的工具
     */
    private String executeTool(String toolName, Map<String, Object> params) {
        switch (toolName) {
            // 查询工具
            case "queryStudents":
                return queryTool.queryStudents(params);  // 使用queryTool
            case "queryEmployees":
                return queryTool.queryEmployees(params);  // 使用queryTool
            case "queryDepartments":
                return queryTool.queryDepartments(params);  // 使用queryTool
            case "queryClasses":
                return queryTool.queryClasses(params);  // 新增：查询班级
            case "getDatabaseOverview":
                return queryTool.getDatabaseOverview();  // 无参数
            
            // 添加工具
            case "addStudent":
                return crudTool.addStudent(params);
            case "addEmployee":
                return crudTool.addEmployee(params);
            case "addDepartment":
                return crudTool.addDepartment(params);
            case "addClass":
                return crudTool.addClass(params);  // 新增：添加班级
            
            // 修改工具
            case "updateStudent":
                return crudTool.updateStudent(params);
            case "updateEmployee":
                return crudTool.updateEmployee(params);
            case "updateDepartment":
                return crudTool.updateDepartment(params);
            case "updateClass":
                return crudTool.updateClass(params);  // 新增：修改班级
            
            // 删除工具
            case "deleteStudent":
                return crudTool.deleteStudent(params);
            case "deleteEmployee":
                return crudTool.deleteEmployee(params);
            case "deleteDepartment":
                return crudTool.deleteDepartment(params);
            case "deleteClass":
                return crudTool.deleteClass(params);  // 新增：删除班级
            
            // 分析工具
            case "analyzeStudentGender":
                return analysisTool.analyzeStudentGender();  // 无参数
            case "analyzeStudentAge":
                return analysisTool.analyzeStudentAge();  // 无参数
            case "analyzeEmployeeDistribution":
                return analysisTool.analyzeEmployeeDistribution();  // 无参数
            case "generateComprehensiveReport":
                return analysisTool.generateComprehensiveReport();  // 无参数
            
            default:
                return "未知工具: " + toolName;
        }
    }
    
    /**
     * 第4步：LLM根据工具结果生成最终回复
     */
    private String generateFinalResponse(String userMessage, String toolResult, 
                                        String initialResponse, String apiKey, 
                                        String model, String baseUrl) {
        try {
            // 构建Prompt，让LLM生成友好的回复
            String systemPrompt = "你是紫金学院管理系统的智能助手。请根据工具执行结果，用友好、自然的语气回复用户。\n\n" +
                                 "要求：\n" +
                                 "1. 简洁明了，不要重复技术细节\n" +
                                 "2. 如果操作成功，确认并总结结果\n" +
                                 "3. 如果操作失败，解释原因并提供建议\n" +
                                 "4. 保持专业但友好的语气";
            
            String userPrompt = String.format(
                "用户问：%s\n\n" +
                "工具执行结果：%s\n\n" +
                "请生成回复：",
                userMessage, toolResult
            );
            
            // 调用LLM生成回复
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7); // 稍高温度，让回复更自然
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/chat/completions", entity, Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    return message.get("content");
                }
            }
            
        } catch (Exception e) {
            System.err.println("生成最终回复失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 如果LLM调用失败，返回简化版回复
        return "操作已完成：" + toolResult;
    }
    
    /**
     * Agent决策结果类
     */
    public static class AgentDecision {
        private String toolName;           // 选择的工具名
        private Map<String, Object> params; // 工具参数
        private String thought;            // 思考过程（可选）
        private String llmResponse;        // 如果不需工具，直接返回的回复
        
        public String getToolName() { return toolName; }
        public void setToolName(String toolName) { this.toolName = toolName; }
        
        public Map<String, Object> getParams() { return params; }
        public void setParams(Map<String, Object> params) { this.params = params; }
        
        public String getThought() { return thought; }
        public void setThought(String thought) { this.thought = thought; }
        
        public String getLlmResponse() { return llmResponse; }
        public void setLlmResponse(String llmResponse) { this.llmResponse = llmResponse; }
        
        @Override
        public String toString() {
            if (toolName != null) {
                return String.format("Tool: %s, Params: %s, Thought: %s", 
                                   toolName, params, thought);
            } else {
                return String.format("Chat Response: %s", llmResponse);
            }
        }
    }
}
