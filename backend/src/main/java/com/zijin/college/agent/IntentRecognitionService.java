package com.zijin.college.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * LLM意图识别服务
 * 使用大语言模型理解用户自然语言，提取意图和参数
 */
@Service
public class IntentRecognitionService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 系统提示词 - 定义Agent的能力和输出格式
     */
    private static final String SYSTEM_PROMPT = 
        "你是紫金学院管理系统的智能助手。你的任务是分析用户的自然语言请求，" +
        "识别其意图并提取相关参数。\n\n" +
        
        "## 支持的意图类型：\n" +
        "1. QUERY - 查询数据（查看、显示、列表、所有）\n" +
        "2. ANALYSIS - 数据分析（统计、分析、分布、比例）\n" +
        "3. CREATE - 添加数据（添加、新增、创建）\n" +
        "4. DELETE - 删除数据（删除、移除）\n" +
        "5. UPDATE - 更新数据（修改、更新、更改）\n" +
        "6. CHAT - 通用对话（问候、闲聊、帮助）\n\n" +
        
        "## 可操作的数据类型：\n" +
        "- employees（员工/教师）\n" +
        "- students（学生）\n" +
        "- classes（班级）\n" +
        "- departments（部门）\n\n" +
        
        "## 参数提取规则（非常重要）：\n" +
        "- id: 数据库主键ID（仅当用户明确说'ID为X'时使用）\n" +
        "- stuNo: 学号（当用户说'学号为X'、'学号X'时必须使用此字段）\n" +
        "- empNo: 工号（当用户说'工号为X'、'工号X'时必须使用此字段）\n" +
        "- classNo: 班级号（如 C001）\n" +
        "- name: 姓名（中文2-4个字）\n" +
        "- keyword: 搜索关键词\n\n" +
        
        "⚠️ 重要提醒：\n" +
        "- 用户说'学号为1' → 必须用 {\"stuNo\":\"1\"}，不能用 {\"id\":1}\n" +
        "- 用户说'工号E001' → 必须用 {\"empNo\":\"E001\"}，不能用 {\"id\":1}\n" +
        "- 只有用户明确说'ID为5'时，才用 {\"id\":5}\n\n" +
        
        "## 输出格式（严格JSON）：\n" +
        "{\n" +
        "  \"intent\": \"QUERY|ANALYSIS|CREATE|DELETE|UPDATE|CHAT\",\n" +
        "  \"action\": \"具体操作名称\",\n" +
        "  \"targetType\": \"employees|students|classes|departments\",\n" +
        "  \"params\": {\n" +
        "    \"id\": 数字,\n" +
        "    \"stuNo\": \"字符串\",\n" +
        "    \"name\": \"字符串\"\n" +
        "  },\n" +
        "  \"confidence\": 0.0-1.0\n" +
        "}\n\n" +
        
        "## Action映射表：\n" +
        "### QUERY:\n" +
        "- queryEmployees, queryStudents, queryClasses, queryDepartments\n" +
        "- getDatabaseOverview, countEmployees, countStudents, countClasses\n\n" +
        
        "### ANALYSIS:\n" +
        "- analyzeEmployeeDistribution, analyzeStudentGender\n" +
        "- analyzeClassSize, analyzeStudentAge, generateComprehensiveReport\n\n" +
        
 "### CREATE:\n" +
        "- addEmployee, addStudent, addClass, addDepartment\n\n" +
        
        "### DELETE:\n" +
        "- deleteEmployee, deleteStudent, deleteClass, deleteDepartment\n\n" +
        
        "### UPDATE:\n" +
        "- updateEmployee, updateStudent, updateClass, updateDepartment\n\n" +
        
        "### CHAT:\n" +
        "- generalChat\n\n" +
        
        "## 示例：\n\n" +
        
        "用户：\"删除学号为1的学生\"\n" +
        "输出：{\"intent\":\"DELETE\",\"action\":\"deleteStudent\",\"targetType\":\"students\",\"params\":{\"stuNo\":\"1\"},\"confidence\":0.95}\n\n" +
        
        "用户：\"查看所有员工\"\n" +
        "输出：{\"intent\":\"QUERY\",\"action\":\"queryEmployees\",\"targetType\":\"employees\",\"params\":{},\"confidence\":0.98}\n\n" +
        
        "用户：\"分析学生性别比例\"\n" +
        "输出：{\"intent\":\"ANALYSIS\",\"action\":\"analyzeStudentGender\",\"targetType\":\"students\",\"params\":{},\"confidence\":0.97}\n\n" +
        
        "用户：\"你好\"\n" +
        "输出：{\"intent\":\"CHAT\",\"action\":\"generalChat\",\"targetType\":null,\"params\":{},\"confidence\":1.0}\n\n" +
        
        "请严格只返回JSON，不要有任何其他文字。";
    
    /**
     * 使用LLM识别意图
     * 
     * @param userMessage 用户消息
     * @param apiKey API密钥
     * @param model 模型名称
     * @param baseUrl API基础URL
     * @return 识别结果
     */
    public IntentRecognitionResult recognizeIntent(String userMessage, String apiKey, 
                                                    String model, String baseUrl) {
        try {
            // 构建请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统消息
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
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
                    
                    // 解析JSON响应
                    return parseIntentResult(content);
                }
            }
            
        } catch (Exception e) {
            System.err.println("LLM意图识别失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 如果LLM调用失败，返回默认结果
        return createFallbackIntent(userMessage);
    }
    
    /**
     * 解析LLM返回的JSON结果
     */
    private IntentRecognitionResult parseIntentResult(String jsonContent) {
        try {
            // 清理可能的Markdown格式
            String cleanJson = jsonContent.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            
            System.out.println("LLM原始响应: " + jsonContent);
            System.out.println("清理后JSON: " + cleanJson);
            
            JsonNode rootNode = objectMapper.readTree(cleanJson);
            
            IntentRecognitionResult result = new IntentRecognitionResult();
            result.setIntent(rootNode.get("intent").asText());
            result.setAction(rootNode.get("action").asText());
            
            if (rootNode.has("targetType") && !rootNode.get("targetType").isNull()) {
                result.setTargetType(rootNode.get("targetType").asText());
            }
            
            if (rootNode.has("params")) {
                JsonNode paramsNode = rootNode.get("params");
                Map<String, Object> params = new HashMap<>();
                
                System.out.println("LLM提取的参数节点: " + paramsNode.toString());
                
                // 遍历所有参数字段
                Iterator<Map.Entry<String, JsonNode>> fields = paramsNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String key = field.getKey();
                    JsonNode value = field.getValue();
                    
                    // 根据字段类型进行转换
                    if (value.isTextual()) {
                        // 字符串类型（学号、工号、姓名、电话等）
                        params.put(key, value.asText());
                        System.out.println("  - " + key + ": " + value.asText() + " (String)");
                    } else if (value.isInt()) {
                        // 整数类型（年龄、ID等）
                        params.put(key, value.asInt());
                        System.out.println("  - " + key + ": " + value.asInt() + " (Integer)");
                    } else if (value.isLong()) {
                        // 长整型（大数字，如电话号码可能被识别为long）
                        // 如果值超过Integer范围，转为String
                        long longValue = value.asLong();
                        if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
                            params.put(key, String.valueOf(longValue));
                            System.out.println("  - " + key + ": " + longValue + " (String, 超出Integer范围)");
                        } else {
                            params.put(key, (int) longValue);
                            System.out.println("  - " + key + ": " + longValue + " (Integer)");
                        }
                    } else if (value.isBoolean()) {
                        // 布尔类型
                        params.put(key, value.asBoolean());
                        System.out.println("  - " + key + ": " + value.asBoolean() + " (Boolean)");
                    } else {
                        // 其他类型，尝试作为字符串处理
                        params.put(key, value.asText());
                        System.out.println("  - " + key + ": " + value.asText() + " (Default String)");
                    }
                }
                
                result.setParams(params);
            }
            
            if (rootNode.has("confidence")) {
                result.setConfidence(rootNode.get("confidence").asDouble());
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("解析LLM结果失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 创建降级方案（当LLM失败时使用）
     */
    private IntentRecognitionResult createFallbackIntent(String userMessage) {
        IntentRecognitionResult result = new IntentRecognitionResult();
        String lowerMsg = userMessage.toLowerCase();
        
        // 增强的关键词匹配
        if (lowerMsg.contains("添加") || lowerMsg.contains("新增") || lowerMsg.contains("创建")) {
            result.setIntent("ADD");
            if (lowerMsg.contains("学生")) {
                result.setAction("addStudent");
                result.setTargetType("students");
            } else if (lowerMsg.contains("员工")) {
                result.setAction("addEmployee");
                result.setTargetType("employees");
            } else if (lowerMsg.contains("部门")) {
                result.setAction("addDepartment");
                result.setTargetType("departments");
            } else {
                result.setAction("generalChat");
            }
        } else if (lowerMsg.contains("删除") || lowerMsg.contains("移除")) {
            result.setIntent("DELETE");
            if (lowerMsg.contains("学生")) {
                result.setAction("deleteStudent");
                result.setTargetType("students");
            } else if (lowerMsg.contains("员工")) {
                result.setAction("deleteEmployee");
                result.setTargetType("employees");
            } else {
                result.setAction("generalChat");
            }
        } else if (lowerMsg.contains("修改") || lowerMsg.contains("更新") || lowerMsg.contains("改") || lowerMsg.contains("改为") || lowerMsg.contains("改成")) {
            result.setIntent("UPDATE");
            if (lowerMsg.contains("学生")) {
                result.setAction("updateStudent");
                result.setTargetType("students");
            } else if (lowerMsg.contains("员工")) {
                result.setAction("updateEmployee");
                result.setTargetType("employees");
            } else if (lowerMsg.contains("部门")) {
                result.setAction("updateDepartment");
                result.setTargetType("departments");
            } else {
                result.setAction("generalChat");
            }
        } else if (lowerMsg.contains("查询") || lowerMsg.contains("查看") || lowerMsg.contains("搜索") || lowerMsg.contains("找")) {
            result.setIntent("QUERY");
            if (lowerMsg.contains("学生")) {
                result.setAction("queryStudents");
                result.setTargetType("students");
            } else if (lowerMsg.contains("员工")) {
                result.setAction("queryEmployees");
                result.setTargetType("employees");
            } else if (lowerMsg.contains("部门")) {
                result.setAction("queryDepartments");
                result.setTargetType("departments");
            } else {
                result.setAction("getDatabaseOverview");
            }
        } else {
            result.setIntent("CHAT");
            result.setAction("generalChat");
        }
        
        result.setParams(new HashMap<>());
        result.setConfidence(0.5); // 低置信度
        
        // 增强的参数提取逻辑
        Map<String, Object> params = new HashMap<>();
        
        // 1. 尝试提取数字（但需要智能判断类型）
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("(\\d+)");
        java.util.regex.Matcher numberMatcher = numberPattern.matcher(userMessage);
        if (numberMatcher.find()) {
            String numberStr = numberMatcher.group(1);
            
            try {
                long number = Long.parseLong(numberStr);
                // 如果数字超出Integer范围，作为字符串处理（如电话号码）
                if (number > Integer.MAX_VALUE || number < Integer.MIN_VALUE) {
                    // 检查是否是电话号码上下文
                    if (lowerMsg.contains("电话") || lowerMsg.contains("手机") || lowerMsg.contains("联系方式")) {
                        params.put("phone", numberStr);
                        System.out.println("降级方案：提取电话号码: " + numberStr);
                    } else {
                        params.put("keyword", numberStr); // 大数字作为关键词
                        System.out.println("降级方案：大数字作为关键词: " + numberStr);
                    }
                } else {
                    // 小数字，根据上下文判断
                    if (lowerMsg.contains("年龄") || lowerMsg.contains("岁")) {
                        params.put("age", (int) number);
                        System.out.println("降级方案：提取年龄: " + number);
                    } else if (lowerMsg.contains("id") || lowerMsg.contains("编号")) {
                        params.put("id", (int) number);
                        System.out.println("降级方案：提取ID: " + number);
                    } else {
                        params.put("id", (int) number); // 默认作为ID
                        System.out.println("降级方案：提取ID: " + number);
                    }
                }
            } catch (NumberFormatException e) {
                // 解析失败，忽略
                System.out.println("降级方案：数字解析失败，忽略");
            }
        }
        
        // 2. 尝试提取中文姓名（在"学生"、"员工"等词后面的名字）
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile("(?:学生|员工|部门)([\\u4e00-\\u9fa5]{2,4})");
        java.util.regex.Matcher nameMatcher = namePattern.matcher(userMessage);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            params.put("name", name);
            System.out.println("降级方案：提取姓名: " + name);
        }
        
        // 3. 尝试提取学号/工号格式（如 S-2023-001, E_001）
        java.util.regex.Pattern codePattern = java.util.regex.Pattern.compile("([SE]-?\\d{4}-?\\d{3}|[SE]_\\d{3})");
        java.util.regex.Matcher codeMatcher = codePattern.matcher(userMessage);
        if (codeMatcher.find()) {
            String code = codeMatcher.group(1);
            if (code.startsWith("S")) {
                params.put("stuNo", code);
                System.out.println("降级方案：提取学号: " + code);
            } else if (code.startsWith("E")) {
                params.put("empNo", code);
                System.out.println("降级方案：提取工号: " + code);
            }
        }
        
        result.setParams(params);
        
        return result;
    }
    
    /**
     * 意图识别结果类
     */
    public static class IntentRecognitionResult {
        private String intent;
        private String action;
        private String targetType;
        private Map<String, Object> params;
        private double confidence;
        
        public String getIntent() {
            return intent;
        }
        
        public void setIntent(String intent) {
            this.intent = intent;
        }
        
        public String getAction() {
            return action;
        }
        
        public void setAction(String action) {
            this.action = action;
        }
        
        public String getTargetType() {
            return targetType;
        }
        
        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }
        
        public Map<String, Object> getParams() {
            return params;
        }
        
        public void setParams(Map<String, Object> params) {
            this.params = params;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }
}
