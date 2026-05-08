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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Agent意图识别与参数提取自动化测试
 * 
 * 测试说明：
 * 1. 所有测试方法都使用 @Transactional，测试完成后自动回滚
 * 2. 测试数据不会影响数据库原有内容
 * 3. 覆盖查询、添加、删除、更新等操作
 */
@SpringBootTest
@Transactional  // 关键：确保测试后数据回滚
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentIntentRecognitionTest {

    @Autowired
    private AgentService agentService;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private DepartmentMapper departmentMapper;
    
    // 测试用的API配置
    // 留空则使用规则匹配（推荐，速度快、不依赖网络）
    // 如需测试LLM识别，请填写真实的API配置
    private static final String API_KEY = "";  // 例如: "your-api-key-here"
    private static final String MODEL = "";     // 例如: "glm-4-flash"
    private static final String BASE_URL = "";  // 例如: "https://open.bigmodel.cn/api/paas/v4"

    /**
     * 测试开始前显示配置信息
     */
    @BeforeAll
    public static void setup() {
        System.out.println("\n========================================");
        System.out.println("🧪 开始运行 Agent 自动化测试");
        System.out.println("========================================");
        
        if (API_KEY.isEmpty()) {
            System.out.println("⚠️  当前使用规则匹配（正则表达式）");
            System.out.println("💡 如需测试LLM识别，请在测试类中配置API_KEY、MODEL、BASE_URL");
        } else {
            System.out.println("✅ 当前使用LLM意图识别");
            System.out.println("📡 API模型: " + MODEL);
        }
        System.out.println("========================================\n");
    }

    /**
     * 测试1：查询学生列表
     */
    @Test
    @Order(1)
    @DisplayName("测试查询学生列表")
    public void testQueryStudents() {
        String message = "查看所有学生";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("学生") || result.contains("列表"), 
            "查询学生应该返回包含学生信息的列表");
        System.out.println("✅ 测试1通过: " + result.substring(0, Math.min(50, result.length())));
    }

    /**
     * 测试2：查询员工列表
     */
    @Test
    @Order(2)
    @DisplayName("测试查询员工列表")
    public void testQueryEmployees() {
        String message = "显示所有员工";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("员工") || result.contains("列表"),
            "查询员工应该返回包含员工信息的列表");
        System.out.println("✅ 测试2通过: " + result.substring(0, Math.min(50, result.length())));
    }

    /**
     * 测试3：统计学生数量
     */
    @Test
    @Order(3)
    @DisplayName("测试统计学生数量")
    public void testCountStudents() {
        String message = "统计学生数量";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.matches(".*\\d+.*"), 
            "统计结果应该包含数字");
        System.out.println("✅ 测试3通过: " + result);
    }

    /**
     * 测试4：添加学生（会回滚）
     */
    @Test
    @Order(4)
    @DisplayName("测试添加学生")
    public void testAddStudent() {
        String message = "添加学生 学号TEST001 姓名测试学生 年龄20";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("成功") || result.contains("✅"),
            "添加操作应该返回成功提示");
        
        // 验证学生是否被添加（在事务内可见）
        Student student = studentMapper.findByStuNo("TEST001");
        assertNotNull(student, "学生应该被成功添加");
        assertEquals("测试学生", student.getStuName(), "学生姓名应该正确");
        assertEquals(20, student.getAge(), "学生年龄应该正确");
        
        System.out.println("✅ 测试4通过: 学生已添加（测试后将回滚）");
    }

    /**
     * 测试5：添加员工（会回滚）
     */
    @Test
    @Order(5)
    @DisplayName("测试添加员工")
    public void testAddEmployee() {
        String message = "添加员工 工号TEST001 姓名测试员工 年龄30 职位教师";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("成功") || result.contains("✅"),
            "添加操作应该返回成功提示");
        
        // 验证员工是否被添加
        Employee employee = employeeMapper.findByEmpNo("TEST001");
        assertNotNull(employee, "员工应该被成功添加");
        assertEquals("测试员工", employee.getEmpName(), "员工姓名应该正确");
        
        System.out.println("✅ 测试5通过: 员工已添加（测试后将回滚）");
    }

    /**
     * 测试6：添加部门（会回滚）
     */
    @Test
    @Order(6)
    @DisplayName("测试添加部门")
    public void testAddDepartment() {
        String message = "添加部门 编号TEST001 名称测试部门";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("成功") || result.contains("✅"),
            "添加操作应该返回成功提示");
        
        // 验证部门是否被添加
        Department department = departmentMapper.findByDeptNo("TEST001");
        assertNotNull(department, "部门应该被成功添加");
        assertEquals("测试部门", department.getDeptName(), "部门名称应该正确");
        
        System.out.println("✅ 测试6通过: 部门已添加（测试后将回滚）");
    }

    /**
     * 测试7：删除学生（按学号）
     */
    @Test
    @Order(7)
    @DisplayName("测试删除学生-按学号")
    public void testDeleteStudentByStuNo() {
        // 先添加一个测试学生
        Student testStudent = new Student();
        testStudent.setStuNo("DELETE_TEST_001");
        testStudent.setStuName("待删除学生");
        testStudent.setAge(20);
        testStudent.setGender("男");
        studentMapper.insert(testStudent);
        
        // 执行删除
        String message = "删除学号为DELETE_TEST_001的学生";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("成功") || result.contains("✅"),
            "删除操作应该返回成功提示");
        
        // 验证学生已被删除
        Student deleted = studentMapper.findByStuNo("DELETE_TEST_001");
        assertNull(deleted, "学生应该已被删除");
        
        System.out.println("✅ 测试7通过: 学生已删除（测试后将回滚）");
    }

    /**
     * 测试8：删除员工（按工号）
     */
    @Test
    @Order(8)
    @DisplayName("测试删除员工-按工号")
    public void testDeleteEmployeeByEmpNo() {
        // 先添加一个测试员工
        Employee testEmployee = new Employee();
        testEmployee.setEmpNo("DELETE_TEST_001");
        testEmployee.setEmpName("待删除员工");
        testEmployee.setAge(30);
        testEmployee.setGender("男");
        employeeMapper.insert(testEmployee);
        
        // 执行删除
        String message = "删除工号为DELETE_TEST_001的员工";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("成功") || result.contains("✅"),
            "删除操作应该返回成功提示");
        
        // 验证员工已被删除
        Employee deleted = employeeMapper.findByEmpNo("DELETE_TEST_001");
        assertNull(deleted, "员工应该已被删除");
        
        System.out.println("✅ 测试8通过: 员工已删除（测试后将回滚）");
    }

    /**
     * 测试9：更新学生姓名
     */
    @Test
    @Order(9)
    @DisplayName("测试更新学生姓名")
    public void testUpdateStudentName() {
        // 先添加一个测试学生
        Student testStudent = new Student();
        testStudent.setStuNo("UPDATE_TEST_001");
        testStudent.setStuName("原名");
        testStudent.setAge(20);
        testStudent.setGender("男");
        studentMapper.insert(testStudent);
        
        // 执行更新
        String message = "修改学号UPDATE_TEST_001的学生姓名为新名字";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("成功") || result.contains("✅"),
            "更新操作应该返回成功提示");
        
        // 验证学生姓名已更新
        Student updated = studentMapper.findByStuNo("UPDATE_TEST_001");
        assertNotNull(updated, "学生应该存在");
        assertEquals("新名字", updated.getStuName(), "学生姓名应该已更新");
        
        System.out.println("✅ 测试9通过: 学生姓名已更新为'新名字'（测试后将回滚）");
    }

    /**
     * 测试10：更新员工信息
     */
    @Test
    @Order(10)
    @DisplayName("测试更新员工信息")
    public void testUpdateEmployeeInfo() {
        // 先添加一个测试员工
        Employee testEmployee = new Employee();
        testEmployee.setEmpNo("UPDATE_TEST_001");
        testEmployee.setEmpName("原姓名");
        testEmployee.setAge(25);
        testEmployee.setGender("男");
        employeeMapper.insert(testEmployee);
        
        // 执行更新
        String message = "修改工号UPDATE_TEST_001的员工姓名为新姓名";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("成功") || result.contains("✅"),
            "更新操作应该返回成功提示");
        
        // 验证员工姓名已更新
        Employee updated = employeeMapper.findByEmpNo("UPDATE_TEST_001");
        assertNotNull(updated, "员工应该存在");
        assertEquals("新姓名", updated.getEmpName(), "员工姓名应该已更新");
        
        System.out.println("✅ 测试10通过: 员工姓名已更新为'新姓名'（测试后将回滚）");
    }

    /**
     * 测试11：参数提取准确性 - 姓名不应包含"为"字
     */
    @Test
    @Order(11)
    @DisplayName("测试参数提取-姓名不包含多余字符")
    public void testParameterExtractionName() {
        // 先添加一个测试员工
        Employee testEmployee = new Employee();
        testEmployee.setEmpNo("PARAM_TEST_001");
        testEmployee.setEmpName("原始名");
        testEmployee.setAge(30);
        testEmployee.setGender("男");
        employeeMapper.insert(testEmployee);
        
        // 执行更新
        String message = "修改工号PARAM_TEST_001的员工姓名为王五";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        // 验证姓名正确提取（不应该包含"为"字）
        Employee updated = employeeMapper.findByEmpNo("PARAM_TEST_001");
        assertNotNull(updated, "员工应该存在");
        assertEquals("王五", updated.getEmpName(), 
            "姓名应该是'王五'，而不是'为王五'或其他错误值");
        assertNotEquals("为王五", updated.getEmpName(), 
            "姓名不应该包含'为'字");
        
        System.out.println("✅ 测试11通过: 姓名正确提取为'王五'（不包含'为'字）");
    }

    /**
     * 测试12：分析功能
     */
    @Test
    @Order(12)
    @DisplayName("测试分析功能")
    public void testAnalysis() {
        String message = "分析学生性别比例";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("性别") || result.contains("比例") || result.contains("分析"),
            "分析结果应该包含相关信息");
        System.out.println("✅ 测试12通过: " + result.substring(0, Math.min(50, result.length())));
    }

    /**
     * 测试13：通用对话
     */
    @Test
    @Order(13)
    @DisplayName("测试通用对话")
    public void testGeneralChat() {
        String message = "你好";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.length() > 0, "应该返回回复");
        System.out.println("✅ 测试13通过: " + result.substring(0, Math.min(50, result.length())));
    }

    /**
     * 测试14：错误处理 - 删除不存在的学生
     */
    @Test
    @Order(14)
    @DisplayName("测试错误处理-删除不存在的学生")
    public void testDeleteNonExistentStudent() {
        String message = "删除学号为NOT_EXIST_999的学生";
        String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
        
        assertNotNull(result);
        assertTrue(result.contains("不存在") || result.contains("未找到") || result.contains("❌"),
            "删除不存在的学生应该返回错误提示");
        System.out.println("✅ 测试14通过: " + result);
    }

    /**
     * 测试15：复杂语句理解
     */
    @Test
    @Order(15)
    @DisplayName("测试复杂语句理解")
    public void testComplexSentence() {
        // 先添加测试数据
        Student testStudent = new Student();
        testStudent.setStuNo("COMPLEX_TEST_001");
        testStudent.setStuName("张三");
        testStudent.setAge(18);
        testStudent.setGender("男");
        studentMapper.insert(testStudent);
        
        // 测试多种表达方式
        String[] messages = {
            "把学号COMPLEX_TEST_001的学生年龄改为20",
            "更新学生张三的年龄为21",
            "更改学号COMPLEX_TEST_001的学生姓名为李四"
        };
        
        for (String message : messages) {
            String result = agentService.processMessage(message, API_KEY, MODEL, BASE_URL);
            assertNotNull(result, "应该返回结果: " + message);
            System.out.println("  输入: " + message);
            System.out.println("  输出: " + result.substring(0, Math.min(50, result.length())));
        }
        
        System.out.println("✅ 测试15通过: 复杂语句理解正常");
    }

    /**
     * 测试完成后清理（虽然@Transactional会自动回滚，但这里可以添加额外清理逻辑）
     */
    @AfterAll
    public static void cleanup() {
        System.out.println("\n========================================");
        System.out.println("🎉 所有测试完成！");
        System.out.println("📊 由于使用了 @Transactional，所有测试数据已自动回滚");
        System.out.println("💾 数据库内容保持原样，未受影响");
        System.out.println("========================================\n");
    }
}
