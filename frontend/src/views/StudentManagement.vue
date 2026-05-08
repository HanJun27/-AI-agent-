<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, MinusCircle, Pencil, Search, X } from 'lucide-vue-next'
import { getStudents, addStudent, updateStudent, deleteStudent, type Student, type StudentCreateDTO } from '@/api/student'

const students = ref<Student[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 弹窗控制
const showAddModal = ref(false)
const showEditModal = ref(false)
const showViewModal = ref(false)
const currentStudent = ref<Student | null>(null)

// 表单数据
const formData = ref<StudentCreateDTO>({
  stuNo: '',
  stuName: '',
  gender: '男',
  age: 18,
  phone: '',
  email: '',
  classId: 1,
  major: '',
  enrollmentDate: new Date().toISOString().split('T')[0]
})

// 加载学生数据
const loadStudents = async () => {
  loading.value = true
  try {
    const response = await getStudents({ 
      pageNum: currentPage.value, 
      pageSize: pageSize.value 
    })
    students.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('加载学生数据失败:', error)
    alert('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗
const handleAdd = () => {
  formData.value = {
    stuNo: '',
    stuName: '',
    gender: '男',
    age: 18,
    phone: '',
    email: '',
    classId: 1,
    major: '',
    enrollmentDate: new Date().toISOString().split('T')[0]
  }
  showAddModal.value = true
}

// 打开编辑弹窗
const handleEdit = (stu: Student) => {
  currentStudent.value = stu
  formData.value = { ...stu }
  showEditModal.value = true
}

// 查看详情
const handleView = (stu: Student) => {
  currentStudent.value = stu
  showViewModal.value = true
}

// 删除学生
const handleDelete = async (id: number) => {
  if (!confirm('确定要删除该学生吗？')) return
  try {
    await deleteStudent(id)
    alert('删除成功')
    await loadStudents()
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败')
  }
}

// 关闭弹窗
const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  showViewModal.value = false
  currentStudent.value = null
}

// 提交新增
const handleSubmitAdd = async () => {
  if (!formData.value.stuNo || !formData.value.stuName) {
    alert('学号和姓名不能为空')
    return
  }
  
  try {
    await addStudent(formData.value)
    alert('添加成功')
    closeModal()
    await loadStudents()
  } catch (error: any) {
    console.error('添加学生失败:', error)
    alert(error.message || '添加失败')
  }
}

// 提交编辑
const handleSubmitEdit = async () => {
  if (!currentStudent.value) return
  
  if (!formData.value.stuNo || !formData.value.stuName) {
    alert('学号和姓名不能为空')
    return
  }
  
  try {
    await updateStudent(currentStudent.value.id, formData.value)
    alert('修改成功')
    closeModal()
    await loadStudents()
  } catch (error: any) {
    console.error('修改学生失败:', error)
    alert(error.message || '修改失败')
  }
}

// 分页改变
const handlePageChange = (page: number) => {
  if (page < 1 || page * pageSize.value > total.value) return
  currentPage.value = page
}

onMounted(() => {
  loadStudents()
})
</script>

<template>
  <div class="p-8">
    <div class="flex items-center text-xs text-gray-500 mb-6">
      <span>首页</span>
      <span class="mx-2 text-gray-300">{'>>'}</span>
      <span class="text-primary font-medium">学生管理</span>
    </div>

    <button @click="handleAdd" class="flex items-center gap-2 bg-success text-white px-5 py-2.5 rounded-md text-sm font-medium shadow-l1 hover:brightness-105 transition-all mb-8 cursor-pointer">
      <Plus :size="18" />
      添加学生
    </button>

    <div class="bg-white rounded-xl shadow-l2 border border-gray-100 overflow-hidden">
      <table class="w-full border-collapse">
        <thead>
          <tr class="bg-[#fff9e6]/80 text-gray-700 font-bold border-b border-gray-100">
            <th class="px-6 py-4 text-left text-sm font-bold">学号</th>
            <th class="px-6 py-4 text-left text-sm font-bold">姓名</th>
            <th class="px-6 py-4 text-left text-sm font-bold">性别</th>
            <th class="px-6 py-4 text-left text-sm font-bold">年龄</th>
            <th class="px-6 py-4 text-left text-sm font-bold">联系电话</th>
            <th class="px-6 py-4 text-left text-sm font-bold">专业</th>
            <th class="px-6 py-4 text-left text-sm font-bold">入学日期</th>
            <th class="px-6 py-4 text-center text-sm font-bold">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-for="stu in students" :key="stu.id" class="hover:bg-gray-50/80 transition-colors">
            <td class="px-6 py-5 text-sm text-gray-500 font-mono">{{ stu.stuNo }}</td>
            <td class="px-6 py-5 text-sm font-bold text-gray-800">{{ stu.stuName }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ stu.gender }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ stu.age }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ stu.phone }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ stu.major }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ stu.enrollmentDate }}</td>
            <td class="px-6 py-5">
              <div class="flex items-center justify-center gap-3">
                <button @click="handleDelete(stu.id)" class="btn-icon bg-danger shadow-sm cursor-pointer"><MinusCircle :size="14" /></button>
                <button @click="handleEdit(stu)" class="btn-icon bg-orange-500 shadow-sm cursor-pointer"><Pencil :size="14" /></button>
                <button @click="handleView(stu)" class="btn-icon bg-primary shadow-sm cursor-pointer"><Search :size="14" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div class="px-6 py-5 bg-gray-50/50 border-t border-gray-100 flex items-center justify-between">
        <span class="text-xs text-gray-500">
          显示 1 到 {{ students.length }} 共 {{ total }} 条记录
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

    <!-- 新增学生弹窗 -->
    <transition name="modal">
      <div v-if="showAddModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 overflow-hidden">
          <div class="bg-primary px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">添加学生</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer"><X :size="20" /></button>
          </div>
          
          <div class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">学号 <span class="text-red-500">*</span></label>
                <input v-model="formData.stuNo" type="text" placeholder="例如: S2024001" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
                <p class="text-xs text-gray-500 mt-1">建议使用格式: S + 年份 + 3位数字</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">姓名 <span class="text-red-500">*</span></label>
                <input v-model="formData.stuName" type="text" placeholder="请输入学生姓名" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">性别</label>
                <select v-model="formData.gender" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all">
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">年龄</label>
                <input v-model.number="formData.age" type="number" min="15" max="30" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
                <input v-model="formData.phone" type="tel" placeholder="例如: 13900139001" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">专业</label>
                <input v-model="formData.major" type="text" placeholder="例如: 计算机科学与技术" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">入学日期</label>
              <input v-model="formData.enrollmentDate" type="date" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
            </div>
          </div>
          
          <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end gap-3">
            <button @click="closeModal" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer">取消</button>
            <button @click="handleSubmitAdd" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors cursor-pointer">确定</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 编辑学生弹窗 -->
    <transition name="modal">
      <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 overflow-hidden">
          <div class="bg-orange-500 px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">编辑学生</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer"><X :size="20" /></button>
          </div>
          
          <div class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">学号 <span class="text-red-500">*</span></label>
                <input v-model="formData.stuNo" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
                <p class="text-xs text-gray-500 mt-1">注意：修改学号时需确保不与其他学生重复</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">姓名 <span class="text-red-500">*</span></label>
                <input v-model="formData.stuName" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">性别</label>
                <select v-model="formData.gender" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all">
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">年龄</label>
                <input v-model.number="formData.age" type="number" min="15" max="30" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
                <input v-model="formData.phone" type="tel" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">专业</label>
                <input v-model="formData.major" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">入学日期</label>
              <input v-model="formData.enrollmentDate" type="date" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
            </div>
          </div>
          
          <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end gap-3">
            <button @click="closeModal" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer">取消</button>
            <button @click="handleSubmitEdit" class="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors cursor-pointer">保存</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 查看详情弹窗 -->
    <transition name="modal">
      <div v-if="showViewModal && currentStudent" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-lg mx-4 overflow-hidden">
          <div class="bg-primary px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">学生详情</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer"><X :size="20" /></button>
          </div>
          
          <div class="p-6">
            <div class="space-y-4">
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">学号：</span>
                <span class="font-medium text-gray-800">{{ currentStudent.stuNo }}</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">姓名：</span>
                <span class="font-medium text-gray-800">{{ currentStudent.stuName }}</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">性别：</span>
                <span class="font-medium text-gray-800">{{ currentStudent.gender }}</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">年龄：</span>
                <span class="font-medium text-gray-800">{{ currentStudent.age }} 岁</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">联系电话：</span>
                <span class="font-medium text-gray-800">{{ currentStudent.phone || '未设置' }}</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">专业：</span>
                <span class="font-medium text-gray-800">{{ currentStudent.major || '未设置' }}</span>
              </div>
              <div class="flex justify-between py-2">
                <span class="text-gray-600">入学日期：</span>
                <span class="font-medium text-gray-800">{{ currentStudent.enrollmentDate }}</span>
              </div>
            </div>
          </div>
          
          <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end">
            <button @click="closeModal" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors cursor-pointer">关闭</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>
