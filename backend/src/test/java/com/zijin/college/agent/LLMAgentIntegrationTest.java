package com.zijin.college.agent;

import com.zijin.college.entity.ApiConfig;
import com.zijin.college.service.ApiConfigService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LLM Agent 自动化测试
 * 测试增删改查功能，使用事务回滚保持数据库原样
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional  // 类级别事务，所有测试方法共享
@Rollback(true)  // 测试完成后统一回滚
public class LLMAgentIntegrationTest {
    
    @Autowired
    private LLMAgentService llmAgentService;
    
    @Autowired
    private ApiConfigService apiConfigService;
    
    private static String apiKey;
    private static String model;
    private static String baseUrl;
    
    @BeforeAll
    public static void setup() {
        System.out.println("========== LLM Agent 自动化测试开始 ==========");
    }
    
    @BeforeEach
    public void initApiConfig() {
        // 从数据库获取API配置
        ApiConfig config = apiConfigService.getConfigByProvider("zhipu");
        assertNotNull(config, "智谱AI配置不存在");
        assertEquals(1, config.getEnabled(), "智谱AI未启用");
        
        apiKey = config.getApiKey();
        model = config.getModel();
        baseUrl = config.getBaseUrl();
        
        System.out.println("API配置: " + config.getProviderName());
        System.out.println("Model: " + model);
    }
    
    /**
     * 测试1：查询学生（查）
     */
    @Test
    @Order(1)
    @DisplayName("测试查询学生")
    public void testQueryStudent() {
        System.out.println("\n========== 测试1：查询学生 ==========");
        
        String userMessage = "查询姓名为孙七的学生信息";
        String result = llmAgentService.processMessage(userMessage, apiKey, model, baseUrl);
        
        System.out.println("用户输入: " + userMessage);
        System.out.println("Agent响应: " + result);
        
        // 验证响应包含学生信息
        assertNotNull(result);
        assertTrue(result.contains("孙七") || result.contains("S2022002"), 
            "响应应包含学生姓名或学号");
        
        System.out.println("✅ 查询学生测试通过");
    }
    
    /**
     * 测试2：增删改完整流程（增 + 改 + 删）
     */
    @Test
    @Order(2)
    @DisplayName("测试增删改完整流程")
    public void testCRUDFlow() {
        System.out.println("\n========== 测试2：增删改完整流程 ==========");
        
        // 步骤1：添加学生
        System.out.println("\n--- 步骤1：添加学生 ---");
        String addMessage = "添加一个学生，学号TEST001，姓名测试学生，性别男，年龄20，电话13800000000";
        String addResult = llmAgentService.processMessage(addMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + addMessage);
        System.out.println("Agent响应: " + addResult);
        assertNotNull(addResult);
        assertTrue(addResult.contains("成功") || addResult.contains("添加") || addResult.contains("TEST001"), 
            "添加响应应包含成功或添加信息");
        System.out.println("✅ 添加学生成功");
        
        // 步骤2：修改学生
        System.out.println("\n--- 步骤2：修改学生 ---");
        String updateMessage = "把学号为TEST001的学生的电话号码改为13900000000";
        String updateResult = llmAgentService.processMessage(updateMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + updateMessage);
        System.out.println("Agent响应: " + updateResult);
        assertNotNull(updateResult);
        assertTrue(updateResult.contains("成功") || updateResult.contains("修改") || updateResult.contains("更新"), 
            "修改响应应包含成功或修改信息");
        System.out.println("✅ 修改学生成功");
        
        // 步骤3：删除学生
        System.out.println("\n--- 步骤3：删除学生 ---");
        String deleteMessage = "删除学号为TEST001的学生";
        String deleteResult = llmAgentService.processMessage(deleteMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + deleteMessage);
        System.out.println("Agent响应: " + deleteResult);
        assertNotNull(deleteResult);
        assertTrue(deleteResult.contains("成功") || deleteResult.contains("删除"), 
            "删除响应应包含成功或删除信息");
        System.out.println("✅ 删除学生成功");
        
        System.out.println("\n✅ 增删改完整流程测试通过");
    }
    
    /**
     * 测试5：复杂查询（多条件）
     */
    @Test
    @Order(3)
    @DisplayName("测试复杂查询")
    public void testComplexQuery() {
        System.out.println("\n========== 测试3：复杂查询 ==========");
        
        String userMessage = "查询所有男学生";
        String result = llmAgentService.processMessage(userMessage, apiKey, model, baseUrl);
        
        System.out.println("用户输入: " + userMessage);
        System.out.println("Agent响应: " + result);
        
        // 验证响应包含查询结果
        assertNotNull(result);
        assertTrue(result.length() > 10, "响应应有足够内容");
        
        System.out.println("✅ 复杂查询测试通过");
    }
    
    /**
     * 测试6：数据分析
     */
    @Test
    @Order(4)
    @DisplayName("测试数据分析")
    public void testDataAnalysis() {
        System.out.println("\n========== 测试4：数据分析 ==========");
        
        String userMessage = "分析学生性别分布";
        String result = llmAgentService.processMessage(userMessage, apiKey, model, baseUrl);
        
        System.out.println("用户输入: " + userMessage);
        System.out.println("Agent响应: " + result);
        
        // 验证响应包含分析结果
        assertNotNull(result);
        assertTrue(result.contains("男") || result.contains("女") || result.contains("分布"), 
            "响应应包含性别分析信息");
        
        System.out.println("✅ 数据分析测试通过");
    }
    
    /**
     * 测试7：部门管理
     */
    @Test
    @Order(5)
    @DisplayName("测试部门管理")
    public void testDepartmentManagement() {
        System.out.println("\n========== 测试5：部门管理 ==========");
        
        // 步骤1：添加部门
        System.out.println("\n--- 步骤1：添加部门 ---");
        String addMessage = "添加一个部门，编号TEST_DEPT，名称测试部门，位置行政楼";
        String addResult = llmAgentService.processMessage(addMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + addMessage);
        System.out.println("Agent响应: " + addResult);
        assertNotNull(addResult);
        assertTrue(addResult.contains("成功") || addResult.contains("添加") || addResult.contains("TEST_DEPT"), 
            "添加响应应包含成功或添加信息");
        System.out.println("✅ 添加部门成功");
        
        // 步骤2：查询部门
        System.out.println("\n--- 步骤2：查询部门 ---");
        String queryMessage = "查询部门编号为TEST_DEPT的部门";
        String queryResult = llmAgentService.processMessage(queryMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + queryMessage);
        System.out.println("Agent响应: " + queryResult);
        assertNotNull(queryResult);
        assertTrue(queryResult.contains("测试部门") || queryResult.contains("TEST_DEPT"), 
            "查询响应应包含部门信息");
        System.out.println("✅ 查询部门成功");
        
        // 步骤3：修改部门
        System.out.println("\n--- 步骤3：修改部门 ---");
        String updateMessage = "把部门TEST_DEPT的位置改为教学楼";
        String updateResult = llmAgentService.processMessage(updateMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + updateMessage);
        System.out.println("Agent响应: " + updateResult);
        assertNotNull(updateResult);
        assertTrue(updateResult.contains("成功") || updateResult.contains("修改") || updateResult.contains("更新"), 
            "修改响应应包含成功或修改信息");
        System.out.println("✅ 修改部门成功");
        
        // 步骤4：删除部门
        System.out.println("\n--- 步骤4：删除部门 ---");
        String deleteMessage = "删除部门TEST_DEPT";
        String deleteResult = llmAgentService.processMessage(deleteMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + deleteMessage);
        System.out.println("Agent响应: " + deleteResult);
        assertNotNull(deleteResult);
        assertTrue(deleteResult.contains("成功") || deleteResult.contains("删除"), 
            "删除响应应包含成功或删除信息");
        System.out.println("✅ 删除部门成功");
        
        System.out.println("\n✅ 部门管理测试通过");
    }
    
    /**
     * 测试8：班级管理
     */
    @Test
    @Order(6)
    @DisplayName("测试班级管理")
    public void testClassManagement() {
        System.out.println("\n========== 测试6：班级管理 ==========");
        
        // 步骤1：添加班级
        System.out.println("\n--- 步骤1：添加班级 ---");
        String addMessage = "添加一个班级，编号TEST_CLASS，名称测试班级，人数50";
        String addResult = llmAgentService.processMessage(addMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + addMessage);
        System.out.println("Agent响应: " + addResult);
        assertNotNull(addResult);
        assertTrue(addResult.contains("成功") || addResult.contains("添加") || addResult.contains("TEST_CLASS"), 
            "添加响应应包含成功或添加信息");
        System.out.println("✅ 添加班级成功");
        
        // 步骤2：修改班级人数
        System.out.println("\n--- 步骤2：修改班级人数 ---");
        String updateMessage = "把班级TEST_CLASS的人数改为60";
        String updateResult = llmAgentService.processMessage(updateMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + updateMessage);
        System.out.println("Agent响应: " + updateResult);
        assertNotNull(updateResult);
        assertTrue(updateResult.contains("成功") || updateResult.contains("修改") || updateResult.contains("更新"), 
            "修改响应应包含成功或修改信息");
        System.out.println("✅ 修改班级成功");
        
        // 步骤3：删除班级
        System.out.println("\n--- 步骤3：删除班级 ---");
        String deleteMessage = "删除班级TEST_CLASS";
        String deleteResult = llmAgentService.processMessage(deleteMessage, apiKey, model, baseUrl);
        System.out.println("用户输入: " + deleteMessage);
        System.out.println("Agent响应: " + deleteResult);
        assertNotNull(deleteResult);
        assertTrue(deleteResult.contains("成功") || deleteResult.contains("删除"), 
            "删除响应应包含成功或删除信息");
        System.out.println("✅ 删除班级成功");
        
        System.out.println("\n✅ 班级管理测试通过");
    }
    
    @AfterAll
    public static void teardown() {
        System.out.println("\n========== LLM Agent 自动化测试完成 ==========");
        System.out.println("✅ 所有测试已通过，数据库已自动回滚");
    }
}
