package com.zijin.college.controller;

import com.zijin.college.agent.AgentService;
import com.zijin.college.agent.LLMAgentService;
import com.zijin.college.dto.Result;
import com.zijin.college.entity.ApiConfig;
import com.zijin.college.entity.ChatMessage;
import com.zijin.college.entity.ChatSession;
import com.zijin.college.service.ApiConfigService;
import com.zijin.college.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI对话控制器
 */
@RestController
@RequestMapping("/chat")
@CrossOrigin
public class ChatController {
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private LLMAgentService llmAgentService;  // 新增：LLM Agent服务
    
    @Autowired
    private ApiConfigService apiConfigService;
    
    @Autowired
    private ChatHistoryService chatHistoryService;
    
    /**
     * 发送消息并获取AI回复（传统模式）
     */
    @PostMapping("/send")
    public Result<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            String provider = request.getOrDefault("provider", "");
            String recognitionMode = request.getOrDefault("recognitionMode", "llm_first"); // 默认LLM优先
            
            System.out.println("========== ChatController收到请求 ==========");
            System.out.println("userMessage: " + userMessage);
            System.out.println("provider: " + provider);
            System.out.println("recognitionMode: " + recognitionMode);
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return Result.error("消息不能为空");
            }
            
            // 获取API配置
            String apiKey = null;
            String model = null;
            String baseUrl = null;
            
            if (!provider.isEmpty()) {
                System.out.println("查询API配置: " + provider);
                ApiConfig config = apiConfigService.getConfigByProvider(provider);
                if (config != null) {
                    System.out.println("找到配置: enabled=" + config.getEnabled());
                    if (config.getEnabled() == 1) {
                        apiKey = config.getApiKey();
                        model = config.getModel();
                        baseUrl = config.getBaseUrl();
                        System.out.println("API配置已启用，将使用LLM");
                    } else {
                        System.out.println("API配置未启用，将使用规则匹配");
                    }
                } else {
                    System.out.println("未找到API配置");
                }
            } else {
                System.out.println("provider为空，将使用规则匹配");
            }
            
            System.out.println("apiKey: " + (apiKey != null ? "已设置" : "null"));
            System.out.println("model: " + model);
            System.out.println("baseUrl: " + baseUrl);
            System.out.println("===========================================");
            
            // 处理消息
            AgentService.RecognitionMode mode = AgentService.RecognitionMode.fromCode(recognitionMode);
            String reply = agentService.processMessage(userMessage, apiKey, model, baseUrl, mode);
            
            // 保存消息到数据库（假设userId=1）
            Integer userId = 1;
            String sessionId = request.getOrDefault("sessionId", "");
            
            if (!sessionId.isEmpty()) {
                // 保存用户消息
                ChatMessage userMsg = new ChatMessage();
                userMsg.setSessionId(sessionId);
                userMsg.setRole("user");
                userMsg.setContent(userMessage);
                userMsg.setTimestamp(LocalDateTime.now());
                chatHistoryService.saveMessage(userMsg);
                
                // 保存AI回复
                ChatMessage aiMsg = new ChatMessage();
                aiMsg.setSessionId(sessionId);
                aiMsg.setRole("assistant");
                aiMsg.setContent(reply);
                aiMsg.setTimestamp(LocalDateTime.now());
                chatHistoryService.saveMessage(aiMsg);
                
                // 更新会话消息数量
                ChatSession session = chatHistoryService.getSession(sessionId);
                if (session != null) {
                    session.setMessageCount(session.getMessageCount() + 2);
                    chatHistoryService.saveSession(session);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("reply", reply);
            response.put("timestamp", System.currentTimeMillis());
            
            return Result.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("处理消息失败：" + e.getMessage());
        }
    }
    
    /**
     * 使用LLM Agent处理消息（新架构 - Function Calling模式）
     */
    @PostMapping("/agent")
    public Result<?> sendToAgent(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            String provider = request.getOrDefault("provider", "");
            
            System.out.println("========== LLM Agent收到请求 ==========");
            System.out.println("userMessage: " + userMessage);
            System.out.println("provider: " + provider);
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return Result.error("消息不能为空");
            }
            
            // 获取API配置
            String apiKey = null;
            String model = null;
            String baseUrl = null;
            
            if (!provider.isEmpty()) {
                ApiConfig config = apiConfigService.getConfigByProvider(provider);
                if (config != null && config.getEnabled() == 1) {
                    apiKey = config.getApiKey();
                    model = config.getModel();
                    baseUrl = config.getBaseUrl();
                    System.out.println("使用LLM Agent模式");
                }
            }
            
            // 使用LLM Agent处理
            String reply;
            if (apiKey != null && !apiKey.isEmpty()) {
                // 有API密钥，使用真正的LLM Agent（自动从数据库获取配置）
                reply = llmAgentService.processMessage(userMessage, provider);
            } else {
                // 没有API密钥，降级到传统Agent
                System.out.println("API未配置，降级到传统Agent");
                reply = agentService.processMessage(userMessage, null, null, null, 
                                                    AgentService.RecognitionMode.RULE_ONLY);
            }
            
            // 保存消息到数据库（与sendMessage相同逻辑）
            Integer userId = 1;
            String sessionId = request.getOrDefault("sessionId", "");
            
            if (!sessionId.isEmpty()) {
                ChatMessage userMsg = new ChatMessage();
                userMsg.setSessionId(sessionId);
                userMsg.setRole("user");
                userMsg.setContent(userMessage);
                userMsg.setTimestamp(LocalDateTime.now());
                chatHistoryService.saveMessage(userMsg);
                
                ChatMessage aiMsg = new ChatMessage();
                aiMsg.setSessionId(sessionId);
                aiMsg.setRole("assistant");
                aiMsg.setContent(reply);
                aiMsg.setTimestamp(LocalDateTime.now());
                chatHistoryService.saveMessage(aiMsg);
                
                ChatSession session = chatHistoryService.getSession(sessionId);
                if (session != null) {
                    session.setMessageCount(session.getMessageCount() + 2);
                    chatHistoryService.saveSession(session);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("reply", reply);
            response.put("timestamp", System.currentTimeMillis());
            response.put("mode", "agent");  // 标识使用Agent模式
            
            return Result.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Agent处理失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取启用的提供商列表
     */
    @GetMapping("/providers")
    public Result<?> getEnabledProviders() {
        try {
            java.util.List<ApiConfig> configs = apiConfigService.getAllConfigs();
            
            java.util.List<Map<String, Object>> providers = new java.util.ArrayList<>();
            for (ApiConfig config : configs) {
                if (config.getEnabled() == 1 && config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                    Map<String, Object> provider = new HashMap<>();
                    provider.put("id", config.getProvider());
                    provider.put("name", config.getProviderName());
                    provider.put("model", config.getModel());
                    providers.add(provider);
                }
            }
            
            return Result.success(providers);
        } catch (Exception e) {
            return Result.error("获取提供商列表失败");
        }
    }
    
    /**
     * 获取用户会话列表
     */
    @GetMapping("/sessions")
    public Result<?> getUserSessions(@RequestParam(defaultValue = "1") Integer userId) {
        try {
            List<ChatSession> sessions = chatHistoryService.getUserSessions(userId);
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error("获取会话列表失败");
        }
    }
    
    /**
     * 获取会话消息
     */
    @GetMapping("/messages/{sessionId}")
    public Result<?> getSessionMessages(@PathVariable String sessionId) {
        try {
            List<ChatMessage> messages = chatHistoryService.getSessionMessages(sessionId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取消息失败");
        }
    }
    
    /**
     * 创建新会话
     */
    @PostMapping("/session")
    public Result<?> createSession(@RequestBody Map<String, Object> requestData) {
        try {
            ChatSession session = new ChatSession();
            session.setSessionId((String) requestData.get("sessionId"));
            session.setTitle((String) requestData.get("title"));
            session.setUserId((Integer) requestData.get("userId"));
            session.setMessageCount(0);
            
            chatHistoryService.saveSession(session);
            return Result.success("会话创建成功");
        } catch (Exception e) {
            return Result.error("创建会话失败");
        }
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<?> deleteSession(@PathVariable String sessionId) {
        try {
            chatHistoryService.deleteSession(sessionId);
            return Result.success("会话删除成功");
        } catch (Exception e) {
            return Result.error("删除会话失败");
        }
    }
}
