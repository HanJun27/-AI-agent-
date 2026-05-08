<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, MinusCircle, Pencil, Search, X } from 'lucide-vue-next'
import { getEmployees, addEmployee, updateEmployee, deleteEmployee, type Employee, type EmployeeCreateDTO } from '@/api/employee'

const employees = ref<Employee[]>([])
const activeMenu = ref('人事管理')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 弹窗控制
const showAddModal = ref(false)
const showEditModal = ref(false)
const showViewModal = ref(false)
const currentEmp = ref<Employee | null>(null)

// 表单数据
const formData = ref<EmployeeCreateDTO>({
  empNo: '',
  empName: '',
  gender: '男',
  age: 25,
  phone: '',
  email: '',
  position: '',
  deptId: 1,
  hireDate: new Date().toISOString().split('T')[0]
})

// 加载员工数据
const loadEmployees = async () => {
  loading.value = true
  try {
    const response = await getEmployees({ 
      pageNum: currentPage.value, 
      pageSize: pageSize.value 
    })
    employees.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('加载员工数据失败:', error)
    alert('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗
const handleAdd = () => {
  formData.value = {
    empNo: '',
    empName: '',
    gender: '男',
    age: 25,
    phone: '',
    email: '',
    position: '',
    deptId: 1,
    hireDate: new Date().toISOString().split('T')[0]
  }
  showAddModal.value = true
}

// 打开编辑弹窗
const handleEdit = (emp: Employee) => {
  currentEmp.value = emp
  formData.value = {
    empNo: emp.empNo,
    empName: emp.empName,
    gender: emp.gender,
    age: emp.age,
    phone: emp.phone,
    email: emp.email,
    position: emp.position,
    deptId: emp.deptId,
    hireDate: emp.hireDate
  }
  showEditModal.value = true
}

// 打开详情弹窗
const handleView = (emp: Employee) => {
  currentEmp.value = emp
  showViewModal.value = true
}

// 关闭弹窗
const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  showViewModal.value = false
  currentEmp.value = null
}

// 提交新增
const handleSubmitAdd = async () => {
  if (!formData.value.empNo || !formData.value.empName) {
    alert('员工编号和姓名不能为空')
    return
  }
  
  try {
    await addEmployee(formData.value)
    alert('添加成功')
    closeModal()
    await loadEmployees()
  } catch (error: any) {
    console.error('添加员工失败:', error)
    alert(error.message || '添加失败')
  }
}

// 提交编辑
const handleSubmitEdit = async () => {
  if (!formData.value.empNo || !formData.value.empName) {
    alert('员工编号和姓名不能为空')
    return
  }
  
  if (!currentEmp.value) return
  
  try {
    await updateEmployee(currentEmp.value.id, formData.value)
    alert('修改成功')
    closeModal()
    await loadEmployees()
  } catch (error: any) {
    console.error('更新员工失败:', error)
    alert(error.message || '更新失败')
  }
}

// 删除员工
const handleDelete = async (id: number) => {
  if (!confirm('确定要删除该员工吗？')) return
  
  try {
    await deleteEmployee(id)
    alert('删除成功')
    await loadEmployees()
  } catch (error) {
    console.error('删除员工失败:', error)
    alert('删除失败')
  }
}

// 分页改变
const handlePageChange = (page: number) => {
  if (page < 1 || page * pageSize.value > total.value) return
  currentPage.value = page
  loadEmployees()
}

onMounted(() => {
  loadEmployees()
})
</script>

<template>
  <div class="p-8">
    <!-- Breadcrumb -->
    <div class="flex items-center text-xs text-gray-500 mb-6">
      <span>首页</span>
      <span class="mx-2 text-gray-300">{'>>'}</span>
      <span class="text-primary font-medium">员工管理</span>
    </div>

    <!-- Action Button -->
    <button 
      @click="handleAdd"
      class="flex items-center gap-2 bg-success text-white px-5 py-2.5 rounded-md text-sm font-medium shadow-l1 hover:brightness-105 transition-all mb-8 cursor-pointer"
    >
      <Plus :size="18" />
      添加员工
    </button>

    <!-- Table Container -->
    <div class="bg-white rounded-xl shadow-l2 border border-gray-100 overflow-hidden">
      <table class="w-full border-collapse">
        <thead>
          <tr class="bg-[#fff9e6]/80 text-gray-700 font-bold border-b border-gray-100">
            <th class="px-6 py-4 text-left text-sm font-bold w-[12%]">员工编号</th>
            <th class="px-6 py-4 text-left text-sm font-bold w-[10%]">姓名</th>
            <th class="px-6 py-4 text-left text-sm font-bold w-[8%]">性别</th>
            <th class="px-6 py-4 text-left text-sm font-bold w-[8%]">年龄</th>
            <th class="px-6 py-4 text-left text-sm font-bold w-[15%]">联系电话</th>
            <th class="px-6 py-4 text-left text-sm font-bold w-[15%]">职位</th>
            <th class="px-6 py-4 text-left text-sm font-bold w-[12%]">入职日期</th>
            <th class="px-6 py-4 text-center text-sm font-bold w-[20%]">操作列表</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr 
            v-for="emp in employees"
            :key="emp.id"
            class="hover:bg-gray-50/80 transition-colors group"
          >
            <td class="px-6 py-5 text-sm text-gray-500 font-medium font-mono">{{ emp.empNo }}</td>
            <td class="px-6 py-5 text-sm font-bold text-gray-800">{{ emp.empName }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ emp.gender }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ emp.age }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ emp.phone }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ emp.position }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ emp.hireDate }}</td>
            <td class="px-6 py-5">
              <div class="flex items-center justify-center gap-3">
                <button @click="handleDelete(emp.id)" class="btn-icon bg-danger shadow-sm cursor-pointer" title="删除">
                  <MinusCircle :size="14" />
                </button>
                <button @click="handleEdit(emp)" class="btn-icon bg-orange-500 shadow-sm cursor-pointer" title="编辑">
                  <Pencil :size="14" />
                </button>
                <button @click="handleView(emp)" class="btn-icon bg-primary shadow-sm hover:bg-primary-dark cursor-pointer" title="查看">
                  <Search :size="14" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div class="px-6 py-5 bg-gray-50/50 border-t border-gray-100 flex items-center justify-between">
        <span class="text-xs text-gray-500">
          显示 1 到 {{ employees.length }} 共 {{ total }} 条记录
        </span>
        <div class="flex gap-2">
          <button 
            @click="handlePageChange(currentPage - 1)"
            :disabled="currentPage === 1"
            class="px-4 py-1.5 border border-gray-200 text-xs font-medium text-gray-500 rounded-lg hover:bg-white transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一页
          </button>
          <button class="px-3 py-1.5 bg-primary text-white text-xs font-bold rounded-lg shadow-sm">
            {{ currentPage }}
          </button>
          <button 
            @click="handlePageChange(currentPage + 1)"
            :disabled="currentPage * pageSize >= total"
            class="px-4 py-1.5 border border-gray-200 text-xs font-medium text-gray-500 rounded-lg hover:bg-white transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 新增员工弹窗 -->
    <transition name="modal">
      <div v-if="showAddModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 overflow-hidden">
          <div class="bg-primary px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">添加员工</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer"><X :size="20" /></button>
          </div>
          
          <div class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">员工编号 <span class="text-red-500">*</span></label>
                <input v-model="formData.empNo" type="text" placeholder="例如: E006" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
                <p class="text-xs text-gray-500 mt-1">建议使用格式: E + 3位数字（如 E006）</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">姓名 <span class="text-red-500">*</span></label>
                <input v-model="formData.empName" type="text" placeholder="请输入姓名" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">性别</label>
                <select v-model="formData.gender" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none">
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">年龄</label>
                <input v-model.number="formData.age" type="number" min="18" max="65" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
                <input v-model="formData.phone" type="tel" placeholder="例如: 13800138001" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">邮箱</label>
                <input v-model="formData.email" type="email" placeholder="例如: wangxm@zijin.edu.cn" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">职位</label>
                <input v-model="formData.position" type="text" placeholder="例如: 软件工程师" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">入职日期</label>
                <input v-model="formData.hireDate" type="date" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
              </div>
            </div>
          </div>
          
          <div class="px-6 py-4 bg-gray-50 flex justify-end gap-3">
            <button @click="closeModal" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer">取消</button>
            <button @click="handleSubmitAdd" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors cursor-pointer">确定</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 编辑员工弹窗 -->
    <transition name="modal">
      <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 overflow-hidden">
          <div class="bg-orange-500 px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">编辑员工</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer"><X :size="20" /></button>
          </div>
          
          <div class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">员工编号 <span class="text-red-500">*</span></label>
                <input v-model="formData.empNo" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
                <p class="text-xs text-gray-500 mt-1">注意：修改编号时需确保不与其他员工重复</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">姓名 <span class="text-red-500">*</span></label>
                <input v-model="formData.empName" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">性别</label>
                <select v-model="formData.gender" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none">
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">年龄</label>
                <input v-model.number="formData.age" type="number" min="18" max="65" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
                <input v-model="formData.phone" type="tel" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">邮箱</label>
                <input v-model="formData.email" type="email" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">职位</label>
                <input v-model="formData.position" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">入职日期</label>
                <input v-model="formData.hireDate" type="date" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
              </div>
            </div>
          </div>
          
          <div class="px-6 py-4 bg-gray-50 flex justify-end gap-3">
            <button @click="closeModal" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer">取消</button>
            <button @click="handleSubmitEdit" class="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors cursor-pointer">确定</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 查看详情弹窗 -->
    <transition name="modal">
      <div v-if="showViewModal && currentEmp" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 overflow-hidden">
          <div class="bg-primary px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">员工详情</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer"><X :size="20" /></button>
          </div>
          
          <div class="p-6">
            <div class="bg-gray-50 p-6 rounded-lg">
              <div class="grid grid-cols-2 gap-6">
                <div><div class="text-xs text-gray-500 mb-1">员工编号</div><div class="text-sm font-semibold">{{ currentEmp.empNo }}</div></div>
                <div><div class="text-xs text-gray-500 mb-1">姓名</div><div class="text-sm font-semibold">{{ currentEmp.empName }}</div></div>
                <div><div class="text-xs text-gray-500 mb-1">性别</div><div class="text-sm font-semibold">{{ currentEmp.gender }}</div></div>
                <div><div class="text-xs text-gray-500 mb-1">年龄</div><div class="text-sm font-semibold">{{ currentEmp.age }} 岁</div></div>
                <div><div class="text-xs text-gray-500 mb-1">联系电话</div><div class="text-sm font-semibold">{{ currentEmp.phone || '未设置' }}</div></div>
                <div><div class="text-xs text-gray-500 mb-1">邮箱</div><div class="text-sm font-semibold">{{ currentEmp.email || '未设置' }}</div></div>
                <div><div class="text-xs text-gray-500 mb-1">职位</div><div class="text-sm font-semibold">{{ currentEmp.position || '未设置' }}</div></div>
                <div><div class="text-xs text-gray-500 mb-1">入职日期</div><div class="text-sm font-semibold">{{ currentEmp.hireDate }}</div></div>
                <div><div class="text-xs text-gray-500 mb-1">状态</div><div class="text-sm font-semibold"><span :class="currentEmp.status === 1 ? 'text-green-600' : 'text-red-600'">{{ currentEmp.status === 1 ? '在职' : '离职' }}</span></div></div>
              </div>
            </div>
          </div>
          
          <div class="px-6 py-4 bg-gray-50 flex justify-end">
            <button @click="closeModal" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors cursor-pointer">关闭</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>
