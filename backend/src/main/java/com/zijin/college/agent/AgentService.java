package com.zijin.college.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI Agent核心服务
 * 负责意图识别和工具调用
 */
@Service
public class AgentService {
    
    @Autowired
    private DatabaseQueryTool queryTool;
    
    @Autowired
    private DataAnalysisTool analysisTool;
    
    @Autowired
    private CrudOperationTool crudTool;
    
    @Autowired
    private IntentRecognitionService intentRecognitionService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 意图识别模式枚举
     */
    public enum RecognitionMode {
        LLM_ONLY("llm_only", "纯LLM模式"),
        RULE_ONLY("rule_only", "纯正则匹配模式"),
        LLM_FIRST("llm_first", "优先LLM，降级正则"),
        RULE_FIRST("rule_first", "优先正则，降级LLM");
        
        private final String code;
        private final String description;
        
        RecognitionMode(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static RecognitionMode fromCode(String code) {
            for (RecognitionMode mode : values()) {
                if (mode.code.equals(code)) {
                    return mode;
                }
            }
            return LLM_FIRST; // 默认模式
        }
    }
    
    /**
     * 处理用户消息，返回AI回复
     */
    public String processMessage(String userMessage, String apiKey, String model, String baseUrl) {
        // 默认使用 LLM_FIRST 模式
        return processMessage(userMessage, apiKey, model, baseUrl, RecognitionMode.LLM_FIRST);
    }
    
    /**
     * 处理用户消息，返回AI回复（带识别模式）
     */
    public String processMessage(String userMessage, String apiKey, String model, String baseUrl, RecognitionMode mode) {
        try {
            // 1. 意图识别 - 根据选择的模式进行识别
            AgentService.Intent intent;
            Map<String, Object> params;
            
            boolean hasApiConfig = (apiKey != null && !apiKey.isEmpty() && baseUrl != null && !baseUrl.isEmpty());
            
            System.out.println("========== 意图识别模式: " + mode.getDescription() + " ==========");
            
            switch (mode) {
                case LLM_ONLY:
                    // 纯LLM模式
                    if (!hasApiConfig) {
                        return "❌ API配置未设置，无法使用纯LLM模式。请在设置中配置API或切换识别模式。";
                    }
                    System.out.println("使用纯LLM模式进行意图识别...");
                    IntentRecognitionService.IntentRecognitionResult llmResult = 
                        intentRecognitionService.recognizeIntent(userMessage, apiKey, model, baseUrl);
                    
                    if (llmResult != null) {
                        intent = convertLLMResultToIntent(llmResult);
                        params = llmResult.getParams();
                        System.out.println("LLM识别结果: " + llmResult.getAction() + ", 置信度: " + llmResult.getConfidence());
                    } else {
                        return "❌ LLM识别失败，请检查API配置或稍后重试。";
                    }
                    break;
                    
                case RULE_ONLY:
                    // 纯正则模式
                    System.out.println("使用纯正则模式进行意图识别...");
                    intent = recognizeIntent(userMessage);
                    params = extractParameters(userMessage, intent);
                    break;
                    
                case LLM_FIRST:
                    // 优先LLM，降级正则
                    if (hasApiConfig) {
                        System.out.println("使用LLM进行意图识别...");
                        IntentRecognitionService.IntentRecognitionResult llmResult1 = 
                            intentRecognitionService.recognizeIntent(userMessage, apiKey, model, baseUrl);
                        
                        if (llmResult1 != null && llmResult1.getConfidence() > 0.7) {
                            // LLM识别成功且置信度高
                            intent = convertLLMResultToIntent(llmResult1);
                            params = llmResult1.getParams();
                            System.out.println("LLM识别成功: " + llmResult1.getAction() + ", 置信度: " + llmResult1.getConfidence());
                        } else {
                            // LLM识别失败或置信度低，降级到规则匹配
                            System.out.println("LLM识别置信度低，降级到规则匹配...");
                            intent = recognizeIntent(userMessage);
                            params = extractParameters(userMessage, intent);
                        }
                    } else {
                        // 没有API配置，直接使用规则匹配
                        System.out.println("未配置API，使用规则匹配...");
                        intent = recognizeIntent(userMessage);
                        params = extractParameters(userMessage, intent);
                    }
                    break;
                    
                case RULE_FIRST:
                    // 优先正则，降级LLM
                    System.out.println("使用规则匹配进行意图识别...");
                    intent = recognizeIntent(userMessage);
                    params = extractParameters(userMessage, intent);
                    
                    // 如果规则匹配失败且API可用，尝试LLM
                    if (intent.getAction() == null || intent.getAction().equals("generalChat")) {
                        if (hasApiConfig) {
                            System.out.println("规则匹配未识别，尝试LLM...");
                            IntentRecognitionService.IntentRecognitionResult llmResult2 = 
                                intentRecognitionService.recognizeIntent(userMessage, apiKey, model, baseUrl);
                            
                            if (llmResult2 != null && llmResult2.getConfidence() > 0.5) {
                                intent = convertLLMResultToIntent(llmResult2);
                                params = llmResult2.getParams();
                                System.out.println("LLM补充识别: " + llmResult2.getAction());
                            }
                        }
                    }
                    break;
                    
                default:
                    // 默认使用 LLM_FIRST
                    System.out.println("使用默认模式（LLM优先）...");
                    if (hasApiConfig) {
                        IntentRecognitionService.IntentRecognitionResult llmResult3 = 
                            intentRecognitionService.recognizeIntent(userMessage, apiKey, model, baseUrl);
                        
                        if (llmResult3 != null && llmResult3.getConfidence() > 0.7) {
                            intent = convertLLMResultToIntent(llmResult3);
                            params = llmResult3.getParams();
                        } else {
                            intent = recognizeIntent(userMessage);
                            params = extractParameters(userMessage, intent);
                        }
                    } else {
                        intent = recognizeIntent(userMessage);
                        params = extractParameters(userMessage, intent);
                    }
                    break;
            }
            
            // 2. 根据意图执行相应操作
            String result = executeIntentWithParams(intent, userMessage, params);
            
            // 3. 如果有API配置，可以让AI优化回复格式（可选）
            if (apiKey != null && !apiKey.isEmpty()) {
                return enhanceResponseWithAI(result, userMessage, apiKey, model, baseUrl);
            }
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，处理您的请求时出现错误：" + e.getMessage();
        }
    }
    
    /**
     * 意图识别
     */
    private Intent recognizeIntent(String message) {
        Intent intent = new Intent();
        String lowerMsg = message.toLowerCase();
        
        // 查询类意图
        if (lowerMsg.contains("查询") || lowerMsg.contains("查看") || lowerMsg.contains("显示") 
            || lowerMsg.contains("列表") || lowerMsg.contains("所有")) {
            intent.setType(IntentType.QUERY);
            
            if (lowerMsg.contains("员工") || lowerMsg.contains("教师")) {
                intent.setAction("queryEmployees");
            } else if (lowerMsg.contains("学生")) {
                intent.setAction("queryStudents");
            } else if (lowerMsg.contains("班级")) {
                intent.setAction("queryClasses");
            } else if (lowerMsg.contains("部门")) {
                intent.setAction("queryDepartments");
            } else if (lowerMsg.contains("概览") || lowerMsg.contains("总览")) {
                intent.setAction("getDatabaseOverview");
            }
        }
        // 统计类意图
        else if (lowerMsg.contains("统计") || lowerMsg.contains("多少") || lowerMsg.contains("数量")) {
            intent.setType(IntentType.QUERY);
            
            if (lowerMsg.contains("员工")) {
                intent.setAction("countEmployees");
            } else if (lowerMsg.contains("学生")) {
                intent.setAction("countStudents");
            } else if (lowerMsg.contains("班级")) {
                intent.setAction("countClasses");
            }
        }
        // 分析类意图
        else if (lowerMsg.contains("分析") || lowerMsg.contains("报告") || lowerMsg.contains("分布")
                 || lowerMsg.contains("比例") || lowerMsg.contains("趋势")) {
            intent.setType(IntentType.ANALYSIS);
            
            if (lowerMsg.contains("综合") || lowerMsg.contains("完整")) {
                intent.setAction("generateComprehensiveReport");
            } else if (lowerMsg.contains("员工")) {
                intent.setAction("analyzeEmployeeDistribution");
            } else if (lowerMsg.contains("性别") || lowerMsg.contains("男女")) {
                intent.setAction("analyzeStudentGender");
            } else if (lowerMsg.contains("班级规模") || lowerMsg.contains("班级大小")) {
                intent.setAction("analyzeClassSize");
            } else if (lowerMsg.contains("年龄")) {
                intent.setAction("analyzeStudentAge");
            }
        }
        // CRUD操作意图
        else if (lowerMsg.contains("添加") || lowerMsg.contains("新增") || lowerMsg.contains("创建")) {
            intent.setType(IntentType.CREATE);
            
            if (lowerMsg.contains("员工")) {
                intent.setAction("addEmployee");
            } else if (lowerMsg.contains("学生")) {
                intent.setAction("addStudent");
            } else if (lowerMsg.contains("班级")) {
                intent.setAction("addClass");
            } else if (lowerMsg.contains("部门")) {
                intent.setAction("addDepartment");
            }
        }
        else if (lowerMsg.contains("删除") || lowerMsg.contains("移除")) {
            intent.setType(IntentType.DELETE);
            
            if (lowerMsg.contains("员工")) {
                intent.setAction("deleteEmployee");
            } else if (lowerMsg.contains("学生")) {
                intent.setAction("deleteStudent");
            } else if (lowerMsg.contains("班级")) {
                intent.setAction("deleteClass");
            } else if (lowerMsg.contains("部门")) {
                intent.setAction("deleteDepartment");
            }
        }
        else if (lowerMsg.contains("修改") || lowerMsg.contains("更新") || lowerMsg.contains("更改")) {
            intent.setType(IntentType.UPDATE);
            
            if (lowerMsg.contains("员工")) {
                intent.setAction("updateEmployee");
            } else if (lowerMsg.contains("学生")) {
                intent.setAction("updateStudent");
            } else if (lowerMsg.contains("班级")) {
                intent.setAction("updateClass");
            } else if (lowerMsg.contains("部门")) {
                intent.setAction("updateDepartment");
            }
        }
        // 默认：通用对话
        else {
            intent.setType(IntentType.CHAT);
            intent.setAction("generalChat");
        }
        
        return intent;
    }
    
    /**
     * 将LLM识别结果转换为内部Intent对象
     */
    private Intent convertLLMResultToIntent(IntentRecognitionService.IntentRecognitionResult llmResult) {
        Intent intent = new Intent();
        
        // 转换意图类型
        switch (llmResult.getIntent()) {
            case "QUERY":
                intent.setType(IntentType.QUERY);
                break;
            case "ANALYSIS":
                intent.setType(IntentType.ANALYSIS);
                break;
            case "CREATE":
                intent.setType(IntentType.CREATE);
                break;
            case "DELETE":
                intent.setType(IntentType.DELETE);
                break;
            case "UPDATE":
                intent.setType(IntentType.UPDATE);
                break;
            default:
                intent.setType(IntentType.CHAT);
                break;
        }
        
        // 设置action
        intent.setAction(llmResult.getAction());
        
        return intent;
    }
    
    /**
     * 执行意图（带参数）
     */
    private String executeIntentWithParams(Intent intent, String userMessage, Map<String, Object> params) {
        // 参数已经由LLM提取或规则提取，直接使用
        
        switch (intent.getAction()) {
            // 查询操作
            case "queryEmployees":
                return queryTool.queryEmployees(params);
            case "queryStudents":
                return queryTool.queryStudents(params);
            case "queryClasses":
                return queryTool.queryClasses(params);
            case "queryDepartments":
                return queryTool.queryDepartments(params);
            case "getDatabaseOverview":
                return queryTool.getDatabaseOverview();
            case "countEmployees":
                return queryTool.countEmployees();
            case "countStudents":
                return queryTool.countStudents();
            case "countClasses":
                return queryTool.countClasses();
            
            // 分析操作
            case "analyzeEmployeeDistribution":
                return analysisTool.analyzeEmployeeDistribution();
            case "analyzeStudentGender":
                return analysisTool.analyzeStudentGender();
            case "analyzeClassSize":
                return analysisTool.analyzeClassSize();
            case "analyzeStudentAge":
                return analysisTool.analyzeStudentAge();
            case "generateComprehensiveReport":
                return analysisTool.generateComprehensiveReport();
            
            // CRUD操作
            case "addEmployee":
                return crudTool.addEmployee(params);
            case "deleteEmployee":
                return crudTool.deleteEmployee(params);
            case "updateEmployee":
                return crudTool.updateEmployee(params);
            case "addStudent":
                return crudTool.addStudent(params);
            case "deleteStudent":
                return crudTool.deleteStudent(params);
            case "updateStudent":
                return crudTool.updateStudent(params);
            case "addClass":
                return crudTool.addClass(params);
            case "deleteClass":
                return crudTool.deleteClass(params);
            case "updateClass":
                return crudTool.updateClass(params);
            case "addDepartment":
                return crudTool.addDepartment(params);
            case "deleteDepartment":
                return crudTool.deleteDepartment(params);
            case "updateDepartment":
                return crudTool.updateDepartment(params);
            
            // 通用对话
            case "generalChat":
                return handleGeneralChat(userMessage);
            
            default:
                return "抱歉，我还不太理解您的需求。您可以尝试：\n" +
                       "• 查询数据：'查看所有员工'\n" +
                       "• 统计分析：'分析学生性别比例'\n" +
                       "• 添加数据：'添加一名新员工'\n" +
                       "• 删除数据：'删除ID为1的学生'";
        }
    }
    
    /**
     * 提取参数
     */
    private Map<String, Object> extractParameters(String message, Intent intent) {
        Map<String, Object> params = new HashMap<>();
        
        System.out.println("========== 规则匹配参数提取 ==========");
        System.out.println("消息: " + message);
        
        // 1. 优先提取完整的学号/工号（字母+数字+下划线组合）
        // 匹配：学号S2023001、学号为S2023001、工号E001、编号C001、学号TEST_001等
        java.util.regex.Pattern codePattern = java.util.regex.Pattern.compile(
            "(?:学号|工号|编号)[^A-Za-z0-9_]*([A-Za-z][A-Za-z0-9_]*)"
        );
        java.util.regex.Matcher codeMatcher = codePattern.matcher(message);
        if (codeMatcher.find()) {
            String code = codeMatcher.group(1);
            System.out.println("提取到编码: " + code);
            
            if (intent.getAction().contains("Student")) {
                params.put("stuNo", code);
                System.out.println("设置 stuNo: " + code);
            } else if (intent.getAction().contains("Employee")) {
                params.put("empNo", code);
                System.out.println("设置 empNo: " + code);
            } else if (intent.getAction().contains("Class")) {
                params.put("classNo", code);
                System.out.println("设置 classNo: " + code);
            } else if (intent.getAction().contains("Department")) {
                params.put("deptNo", code);
                System.out.println("设置 deptNo: " + code);
            }
            // 注意：UPDATE和CREATE操作不提前返回，继续提取其他字段
            if (intent.getType() != IntentType.UPDATE && intent.getType() != IntentType.CREATE) {
                return params;
            }
        }
        
        // 2. 提取数字ID（各种格式）
        // 匹配：ID为123、id=123、编号123等
        java.util.regex.Pattern idPattern = java.util.regex.Pattern.compile(
            "(?:id|ID|编号)[\\s:=：]*?(\\d+)",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher idMatcher = idPattern.matcher(message);
        if (idMatcher.find()) {
            String idStr = idMatcher.group(1);
            params.put("id", Integer.parseInt(idStr));
            System.out.println("设置 id: " + idStr);
        }
        
        // 3. 如果没有找到ID，尝试提取纯数字（作为最后的手段）
        if (!params.containsKey("id")) {
            java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("(\\d+)");
            java.util.regex.Matcher numMatcher = numPattern.matcher(message);
            if (numMatcher.find()) {
                // 只有当上下文明确是ID时才使用纯数字
                if (message.contains("ID") || message.contains("id")) {
                    params.put("id", Integer.parseInt(numMatcher.group(1)));
                    System.out.println("设置 id (纯数字): " + numMatcher.group(1));
                }
            }
        }
        
        // 4. 提取姓名（中文名字）- 支持多种表达方式
        // 匹配：姓名为张三、名字叫张三、改为张三、设置为张三等
        // [^\u4e00-\u9fa5]* 匹配所有非中文字符（包括“为”、空格、冒号等）
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(
            "(?:姓名|名字|叫|改为|设置为)[^\u4e00-\u9fa5]*([\u4e00-\u9fa5]{2,4})"
        );
        java.util.regex.Matcher nameMatcher = namePattern.matcher(message);
        if (nameMatcher.find()) {
            String extractedName = nameMatcher.group(1).trim();
            // 额外检查：如果提取的姓名以"为"开头，去掉它
            if (extractedName.startsWith("为")) {
                extractedName = extractedName.substring(1);
            }
            params.put("name", extractedName);
            System.out.println("设置 name: " + extractedName);
        }
        
        // 5. 提取年龄
        java.util.regex.Pattern agePattern = java.util.regex.Pattern.compile(
            "(?:年龄|岁数)[\\s:=：]*?(\\d+)"
        );
        java.util.regex.Matcher ageMatcher = agePattern.matcher(message);
        if (ageMatcher.find()) {
            params.put("age", Integer.parseInt(ageMatcher.group(1)));
            System.out.println("设置 age: " + ageMatcher.group(1));
        }
        
        // 6. 提取电话
        java.util.regex.Pattern phonePattern = java.util.regex.Pattern.compile(
            "(?:电话|手机|联系方式)[\\s:=：]*?(1\\d{10})"
        );
        java.util.regex.Matcher phoneMatcher = phonePattern.matcher(message);
        if (phoneMatcher.find()) {
            params.put("phone", phoneMatcher.group(1));
            System.out.println("设置 phone: " + phoneMatcher.group(1));
        }
        
        // 7. 提取邮箱
        java.util.regex.Pattern emailPattern = java.util.regex.Pattern.compile(
            "(?:邮箱|email|邮件)[\\s:=：]*?([\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,})"
        );
        java.util.regex.Matcher emailMatcher = emailPattern.matcher(message);
        if (emailMatcher.find()) {
            params.put("email", emailMatcher.group(1));
            System.out.println("设置 email: " + emailMatcher.group(1));
        }
        
        // 8. 提取职位
        java.util.regex.Pattern positionPattern = java.util.regex.Pattern.compile(
            "(?:职位|职务|岗位)[\\s:=：]*?([\u4e00-\u9fa5]{2,10})"
        );
        java.util.regex.Matcher positionMatcher = positionPattern.matcher(message);
        if (positionMatcher.find()) {
            params.put("position", positionMatcher.group(1));
            System.out.println("设置 position: " + positionMatcher.group(1));
        }
        
        System.out.println("最终参数: " + params);
        System.out.println("=====================================");
        
        return params;
    }
    
    /**
     * 处理通用对话
     */
    private String handleGeneralChat(String message) {
        return "您好！我是紫金学院智能管理顾问。我可以帮您：\n\n" +
               "📊 **查询数据**\n" +
               "   - '查看所有员工'\n" +
               "   - '显示学生列表'\n" +
               "   - '数据库概览'\n\n" +
               "📈 **数据分析**\n" +
               "   - '分析员工分布'\n" +
               "   - '学生性别比例'\n" +
               "   - '生成综合报告'\n\n" +
               "➕ **数据管理**\n" +
               "   - '添加新员工'\n" +
               "   - '删除某个学生'\n\n" +
               "请告诉我您需要什么帮助？";
    }
    
    /**
     * 使用AI增强回复（可选）
     */
    private String enhanceResponseWithAI(String baseResponse, String userMessage, 
                                         String apiKey, String model, String baseUrl) {
        try {
            // 构建提示词
            String prompt = String.format(
                "你是一个紫金学院的管理助手。用户的问题是：%s\n\n" +
                "我已经从数据库中获取了以下信息：\n%s\n\n" +
                "请用友好、专业的语气回答用户的问题，可以适当格式化输出使其更易读。",
                userMessage, baseResponse
            );
            
            // 调用AI API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);
            
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
                    Map<String, String> msg = (Map<String, String>) choice.get("message");
                    return msg.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("AI增强失败，使用基础回复: " + e.getMessage());
        }
        
        // 如果AI调用失败，返回基础回复
        return baseResponse;
    }
    
    /**
     * 意图类
     */
    static class Intent {
        private IntentType type;
        private String action;
        
        public IntentType getType() {
            return type;
        }
        
        public void setType(IntentType type) {
            this.type = type;
        }
        
        public String getAction() {
            return action;
        }
        
        public void setAction(String action) {
            this.action = action;
        }
    }
    
    enum IntentType {
        QUERY,      // 查询
        ANALYSIS,   // 分析
        CREATE,     // 创建
        DELETE,     // 删除
        UPDATE,     // 更新
        CHAT        // 通用对话
    }
}
