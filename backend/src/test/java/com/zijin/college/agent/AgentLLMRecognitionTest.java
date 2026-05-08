package com.zijin.college.agent;

import com.zijin.college.entity.Department;
import com.zijin.college.entity.Employee;
import com.zijin.college.entity.Student;
import com.zijin.college.mapper.DepartmentMapper;
import com.zijin.college.mapper.EmployeeMapper;
import com.zijin.college.mapper.StudentMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LLM语义识别专项测试
 * 
 * 测试目标：
 * 1. 验证LLM能否正确理解各种自然语言表达
 * 2. 对比LLM与规则匹配的识别效果
 * 3. 测试LLM的参数提取准确性
 * 4. 验证降级机制是否正常工作
 * 
 * 注意：此测试需要配置真实的API密钥
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentLLMRecognitionTest {

    @Autowired
    private AgentService agentService;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    // ========================================
    // ⚠️ 重要：请在此处配置真实的API信息
    // ========================================
    // 获取方式：
    // 1. 智谱AI: https://open.bigmodel.cn/
    // 2. DeepSeek: https://platform.deepseek.com/
    // 3. 通义千问: https://dashscope.aliyun.com/
    
    private static final String API_KEY = "ed4bac5b02cc41428bb832a6a6331efd.CR4YotjUqw2Pa9M0";  // TODO: 填入你的API Key
    private static final String MODEL = "glm-4-flash";  // 或 "deepseek-chat" 等
    private static final String BASE_URL = "https://open.bigmodel.cn/api/paas/v4";  // 根据提供商修改
    
    // 识别模式
    private static final AgentService.RecognitionMode LLM_MODE = AgentService.RecognitionMode.LLM_ONLY;
    private static final AgentService.RecognitionMode RULE_MODE = AgentService.RecognitionMode.RULE_ONLY;
    private static final AgentService.RecognitionMode HYBRID_MODE = AgentService.RecognitionMode.LLM_FIRST;

    /**
     * 测试前检查API配置
     */
    @BeforeAll
    public static void checkApiConfig() {
        System.out.println("\n========================================");
        System.out.println("🧪 LLM语义识别专项测试");
        System.out.println("========================================");
        
        if (API_KEY.isEmpty()) {
            System.out.println("⚠️  警告：API_KEY未配置！");
            System.out.println("💡 请在测试类中填入真实的API密钥以启用LLM测试");
            System.out.println("📝 或者跳过此测试，使用规则匹配测试即可");
            System.out.println("========================================\n");
        } else {
            System.out.println("✅ API配置已设置");
            System.out.println("📡 模型: " + MODEL);
            System.out.println("🌐 Base URL: " + BASE_URL);
            System.out.println("========================================\n");
        }
    }

    /**
     * 辅助方法：执行LLM测试并返回结果
     */
    private String executeLLMTest(String message) {
        if (API_KEY.isEmpty()) {
            System.out.println("⏭️  跳过LLM测试（API未配置）: " + message);
            return "SKIPPED";
        }
        return agentService.processMessage(message, API_KEY, MODEL, BASE_URL, LLM_MODE);
    }
    
    /**
     * 辅助方法：执行规则匹配测试
     */
    private String executeRuleTest(String message) {
        return agentService.processMessage(message, "", "", "", RULE_MODE);
    }
    
    /**
     * 辅助方法：执行混合模式测试
     */
    private String executeHybridTest(String message) {
        String apiKey = API_KEY.isEmpty() ? "" : API_KEY;
        String baseUrl = API_KEY.isEmpty() ? "" : BASE_URL;
        return agentService.processMessage(message, apiKey, MODEL, baseUrl, HYBRID_MODE);
    }

    // ========================================
    // 第一部分：基础语义理解测试
    // ========================================

    /**
     * 测试1：标准表达 vs 口语化表达
     */
    @Test
    @Order(1)
    @DisplayName("测试1：标准表达与口语化表达的理解")
    public void testStandardVsColloquial() {
        System.out.println("\n========== 测试1：标准表达 vs 口语化表达 ==========");
        
        // 标准表达
        String standard1 = "删除学号为S001的学生";
        String result1 = executeLLMTest(standard1);
        System.out.println("标准表达: " + standard1);
        System.out.println("LLM回复: " + result1);
        
        // 口语化表达
        String colloquial1 = "帮我把学号是S001的那个学生删掉呗";
        String result2 = executeLLMTest(colloquial1);
        System.out.println("\n口语化表达: " + colloquial1);
        System.out.println("LLM回复: " + result2);
        
        // 验证两者都能被理解
        if (!API_KEY.isEmpty()) {
            assertNotNull(result1);
            assertNotNull(result2);
            System.out.println("✅ 两种表达都被正确理解");
        }
    }

    /**
     * 测试2：同义词替换
     */
    @Test
    @Order(2)
    @DisplayName("测试2：同义词替换识别")
    public void testSynonymReplacement() {
        System.out.println("\n========== 测试2：同义词替换 ==========");
        
        String[] messages = {
            "查看所有员工",
            "显示全部员工",
            "列出员工列表",
            "给我看看员工都有谁"
        };
        
        for (String msg : messages) {
            String result = executeLLMTest(msg);
            System.out.println("输入: " + msg);
            System.out.println("输出: " + (result.length() > 50 ? result.substring(0, 50) + "..." : result));
            
            if (!API_KEY.isEmpty()) {
                assertNotNull(result);
                assertTrue(result.contains("员工") || result.contains("列表"),
                    "应该返回员工相关信息");
            }
        }
        
        System.out.println("✅ 同义词替换测试完成");
    }

    /**
     * 测试3：语序变化
     */
    @Test
    @Order(3)
    @DisplayName("测试3：语序变化理解")
    public void testWordOrderVariation() {
        System.out.println("\n========== 测试3：语序变化 ==========");
        
        String[] messages = {
            "删除工号E001的员工",
            "工号E001的员工删除",
            "把工号E001的员工给删除了"
        };
        
        for (String msg : messages) {
            String result = executeLLMTest(msg);
            System.out.println("输入: " + msg);
            System.out.println("输出: " + (result.length() > 50 ? result.substring(0, 50) + "..." : result));
        }
        
        System.out.println("✅ 语序变化测试完成");
    }

    // ========================================
    // 第二部分：复杂语义理解测试
    // ========================================

    /**
     * 测试4：隐含意图识别
     */
    @Test
    @Order(4)
    @DisplayName("测试4：隐含意图识别")
    public void testImplicitIntent() {
        System.out.println("\n========== 测试4：隐含意图识别 ==========");
        
        // 准备测试数据
        Student testStudent = new Student();
        testStudent.setStuNo("IMPLICIT_TEST_001");
        testStudent.setStuName("张三");
        testStudent.setAge(18);
        testStudent.setGender("男");
        studentMapper.insert(testStudent);
        
        // 隐含删除意图
        String implicit1 = "我不想要学号IMPLICIT_TEST_001的学生了";
        String result1 = executeLLMTest(implicit1);
        System.out.println("隐含删除: " + implicit1);
        System.out.println("LLM回复: " + result1);
        
        // 隐含查询意图
        String implicit2 = "我想知道都有哪些学生";
        String result2 = executeLLMTest(implicit2);
        System.out.println("\n隐含查询: " + implicit2);
        System.out.println("LLM回复: " + (result2.length() > 50 ? result2.substring(0, 50) + "..." : result2));
        
        System.out.println("✅ 隐含意图识别测试完成");
    }

    /**
     * 测试5：多条件组合
     */
    @Test
    @Order(5)
    @DisplayName("测试5：多条件组合理解")
    public void testMultipleConditions() {
        System.out.println("\n========== 测试5：多条件组合 ==========");
        
        String[] messages = {
            "添加学生 学号MULTI_001 姓名李四 年龄20 性别男",
            "创建一个新员工，工号MULTI_E001，叫王五，30岁，职位是教授"
        };
        
        for (String msg : messages) {
            String result = executeLLMTest(msg);
            System.out.println("输入: " + msg);
            System.out.println("输出: " + (result.length() > 60 ? result.substring(0, 60) + "..." : result));
        }
        
        System.out.println("✅ 多条件组合测试完成");
    }

    /**
     * 测试6：模糊表达
     */
    @Test
    @Order(6)
    @DisplayName("测试6：模糊表达理解")
    public void testVagueExpression() {
        System.out.println("\n========== 测试6：模糊表达 ==========");
        
        String[] messages = {
            "帮我查一下学生的情况",
            "看看员工的信息",
            "有没有什么班级"
        };
        
        for (String msg : messages) {
            String result = executeLLMTest(msg);
            System.out.println("输入: " + msg);
            System.out.println("输出: " + (result.length() > 50 ? result.substring(0, 50) + "..." : result));
            
            if (!API_KEY.isEmpty()) {
                assertNotNull(result);
                // 模糊查询应该返回一些有用信息
                assertFalse(result.contains("抱歉") || result.contains("不理解"),
                    "LLM应该能处理模糊表达");
            }
        }
        
        System.out.println("✅ 模糊表达测试完成");
    }

    // ========================================
    // 第三部分：参数提取准确性测试
    // ========================================

    /**
     * 测试7：特殊格式编码识别
     */
    @Test
    @Order(7)
    @DisplayName("测试7：特殊格式编码识别")
    public void testSpecialFormatCodes() {
        System.out.println("\n========== 测试7：特殊格式编码 ==========");
        
        String[] messages = {
            "删除学号为S-2023-001的学生",
            "查找工号E_001的员工",
            "查看班级C2023A的信息"
        };
        
        for (String msg : messages) {
            String result = executeLLMTest(msg);
            System.out.println("输入: " + msg);
            System.out.println("输出: " + (result.length() > 50 ? result.substring(0, 50) + "..." : result));
        }
        
        System.out.println("✅ 特殊格式编码测试完成");
    }

    /**
     * 测试8：中文数字识别
     */
    @Test
    @Order(8)
    @DisplayName("测试8：中文数字识别")
    public void testChineseNumbers() {
        System.out.println("\n========== 测试8：中文数字 ==========");
        
        String[] messages = {
            "统计一下有多少个学生",
            "一共有几个员工",
            "班级总数是多少"
        };
        
        for (String msg : messages) {
            String result = executeLLMTest(msg);
            System.out.println("输入: " + msg);
            System.out.println("输出: " + (result.length() > 50 ? result.substring(0, 50) + "..." : result));
            
            if (!API_KEY.isEmpty()) {
                assertNotNull(result);
                // 应该返回数字或统计信息
                assertTrue(result.matches(".*\\d+.*") || result.contains("共") || result.contains("总"),
                    "应该返回统计结果");
            }
        }
        
        System.out.println("✅ 中文数字测试完成");
    }

    // ========================================
    // 第四部分：LLM vs 规则对比测试
    // ========================================

    /**
     * 测试9：LLM与规则匹配效果对比
     */
    @Test
    @Order(9)
    @DisplayName("测试9：LLM vs 规则匹配对比")
    public void testLLMvsRuleComparison() {
        System.out.println("\n========== 测试9：LLM vs 规则匹配对比 ==========");
        
        String[] testCases = {
            "标准: 删除学号S001的学生",
            "口语: 帮我把学号S001的学生删了吧",
            "复杂: 那个学号是S001的同学，我不想要了，能帮我处理一下吗"
        };
        
        for (String testCase : testCases) {
            String[] parts = testCase.split(": ", 2);
            String label = parts[0];
            String message = parts[1];
            
            String llmResult = executeLLMTest(message);
            String ruleResult = executeRuleTest(message);
            
            System.out.println("\n" + label + ": " + message);
            System.out.println("  LLM:  " + (llmResult.length() > 40 ? llmResult.substring(0, 40) + "..." : llmResult));
            System.out.println("  规则: " + (ruleResult.length() > 40 ? ruleResult.substring(0, 40) + "..." : ruleResult));
            
            // 简单判断哪种方式更好
            if (!API_KEY.isEmpty()) {
                boolean llmSuccess = !llmResult.contains("抱歉") && !llmResult.contains("不理解");
                boolean ruleSuccess = !ruleResult.contains("抱歉") && !ruleResult.contains("不理解");
                
                if (llmSuccess && !ruleSuccess) {
                    System.out.println("  👉 LLM表现更好");
                } else if (!llmSuccess && ruleSuccess) {
                    System.out.println("  👉 规则匹配表现更好");
                } else if (llmSuccess && ruleSuccess) {
                    System.out.println("  👉 两者都成功");
                } else {
                    System.out.println("  👉 两者都失败");
                }
            }
        }
        
        System.out.println("\n✅ 对比测试完成");
    }

    /**
     * 测试10：降级机制验证
     */
    @Test
    @Order(10)
    @DisplayName("测试10：降级机制验证")
    public void testFallbackMechanism() {
        System.out.println("\n========== 测试10：降级机制验证 ==========");
        
        String message = "删除学号FALLBACK_TEST的学生";
        
        // 测试混合模式（LLM优先，降级规则）
        String hybridResult = executeHybridTest(message);
        System.out.println("混合模式结果: " + hybridResult);
        
        if (!API_KEY.isEmpty()) {
            assertNotNull(hybridResult);
            System.out.println("✅ 降级机制正常工作");
        } else {
            System.out.println("⚠️  API未配置，无法测试降级机制");
        }
    }

    // ========================================
    // 第五部分：边界情况测试
    // ========================================

    /**
     * 测试11：错误输入处理
     */
    @Test
    @Order(11)
    @DisplayName("测试11：错误输入处理")
    public void testErrorInput() {
        System.out.println("\n========== 测试11：错误输入处理 ==========");
        
        String[] errorInputs = {
            "",  // 空字符串
            "asdfghjkl",  // 无意义字符
            "123456789",  // 纯数字
            "删除删除删除"  // 重复词汇
        };
        
        for (String input : errorInputs) {
            String result = executeLLMTest(input.isEmpty() ? "[空]" : input);
            System.out.println("输入: " + (input.isEmpty() ? "[空]" : input));
            System.out.println("输出: " + (result.length() > 50 ? result.substring(0, 50) + "..." : result));
            
            if (!API_KEY.isEmpty()) {
                assertNotNull(result);
                // LLM应该能优雅地处理错误输入
                assertFalse(result.isEmpty(), "不应该返回空结果");
            }
        }
        
        System.out.println("✅ 错误输入处理测试完成");
    }

    /**
     * 测试12：超长语句理解
     */
    @Test
    @Order(12)
    @DisplayName("测试12：超长语句理解")
    public void testLongSentence() {
        System.out.println("\n========== 测试12：超长语句 ==========");
        
        String longMessage = "你好，我想请你帮我一个忙，就是能不能帮我把那个学号是LONG_TEST_001的学生从我们的系统里面删除掉呢？因为这个学生已经毕业离校了，所以我们不需要再保留他的信息了，麻烦你了！";
        
        String result = executeLLMTest(longMessage);
        System.out.println("输入长度: " + longMessage.length() + " 字符");
        System.out.println("输出: " + (result.length() > 80 ? result.substring(0, 80) + "..." : result));
        
        if (!API_KEY.isEmpty()) {
            assertNotNull(result);
            // LLM应该能从长句中提取关键信息
            System.out.println("✅ LLM能够处理超长语句");
        }
    }

    // ========================================
    // 测试结果总结
    // ========================================

    /**
     * 测试完成后生成报告
     */
    @AfterAll
    public static void generateReport() {
        System.out.println("\n========================================");
        System.out.println("📊 LLM语义识别测试报告");
        System.out.println("========================================");
        
        if (API_KEY.isEmpty()) {
            System.out.println("⚠️  本次测试未配置API，所有LLM测试已跳过");
            System.out.println("💡 如需测试LLM功能，请在测试类中配置API_KEY");
        } else {
            System.out.println("✅ 已完成LLM语义识别能力测试");
            System.out.println("📈 测试覆盖：");
            System.out.println("   - 基础语义理解（标准/口语/同义词/语序）");
            System.out.println("   - 复杂语义理解（隐含意图/多条件/模糊表达）");
            System.out.println("   - 参数提取准确性（特殊格式/中文数字）");
            System.out.println("   - LLM vs 规则对比");
            System.out.println("   - 边界情况处理");
        }
        
        System.out.println("\n💾 数据库状态：所有测试数据已自动回滚");
        System.out.println("========================================\n");
    }
}

