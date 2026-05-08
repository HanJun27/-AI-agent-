<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, MinusCircle, Pencil, Search, X } from 'lucide-vue-next'
import { getClasses, addClass, updateClass, deleteClass, type ClassInfo, type ClassCreateDTO } from '@/api/class'

const classes = ref<ClassInfo[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 弹窗控制
const showAddModal = ref(false)
const showEditModal = ref(false)
const showViewModal = ref(false)
const currentClass = ref<ClassInfo | null>(null)

// 表单数据
const formData = ref<ClassCreateDTO>({
  classNo: '',
  className: '',
  major: '',
  grade: '2024级',
  teacher: '',
  studentCount: 0,
  classroom: ''
})

// 加载班级数据
const loadClasses = async () => {
  loading.value = true
  try {
    const response = await getClasses({ 
      pageNum: currentPage.value, 
      pageSize: pageSize.value 
    })
    classes.value = response.data.list
    total.value = response.data.total
  } catch (error) {
    console.error('加载班级数据失败:', error)
    alert('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗
const handleAdd = () => {
  formData.value = {
    classNo: '',
    className: '',
    major: '',
    grade: '2024级',
    teacher: '',
    studentCount: 0,
    classroom: ''
  }
  showAddModal.value = true
}

// 打开编辑弹窗
const handleEdit = (cls: ClassInfo) => {
  currentClass.value = cls
  formData.value = { ...cls }
  showEditModal.value = true
}

// 查看详情
const handleView = (cls: ClassInfo) => {
  currentClass.value = cls
  showViewModal.value = true
}

// 删除班级
const handleDelete = async (id: number) => {
  if (!confirm('确定要删除该班级吗？')) return
  try {
    await deleteClass(id)
    alert('删除成功')
    await loadClasses()
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
  currentClass.value = null
}

// 提交新增
const handleSubmitAdd = async () => {
  if (!formData.value.classNo || !formData.value.className) {
    alert('班级编号和班级名称不能为空')
    return
  }
  
  try {
    await addClass(formData.value)
    alert('添加成功')
    closeModal()
    await loadClasses()
  } catch (error: any) {
    console.error('添加班级失败:', error)
    alert(error.message || '添加失败')
  }
}

// 提交编辑
const handleSubmitEdit = async () => {
  if (!currentClass.value) return
  
  if (!formData.value.classNo || !formData.value.className) {
    alert('班级编号和班级名称不能为空')
    return
  }
  
  try {
    await updateClass(currentClass.value.id, formData.value)
    alert('修改成功')
    closeModal()
    await loadClasses()
  } catch (error: any) {
    console.error('修改班级失败:', error)
    alert(error.message || '修改失败')
  }
}

// 分页改变
const handlePageChange = (page: number) => {
  if (page < 1 || page * pageSize.value > total.value) return
  currentPage.value = page
}

onMounted(() => {
  loadClasses()
})
</script>

<template>
  <div class="p-8">
    <!-- Breadcrumb -->
    <div class="flex items-center text-xs text-gray-500 mb-6">
      <span>首页</span>
      <span class="mx-2 text-gray-300">{'>>'}</span>
      <span class="text-primary font-medium">班级管理</span>
    </div>

    <button @click="handleAdd" class="flex items-center gap-2 bg-success text-white px-5 py-2.5 rounded-md text-sm font-medium shadow-l1 hover:brightness-105 transition-all mb-8 cursor-pointer">
      <Plus :size="18" />
      添加班级
    </button>

    <div class="bg-white rounded-xl shadow-l2 border border-gray-100 overflow-hidden">
      <table class="w-full border-collapse">
        <thead>
          <tr class="bg-[#fff9e6]/80 text-gray-700 font-bold border-b border-gray-100">
            <th class="px-6 py-4 text-left text-sm font-bold">班级编号</th>
            <th class="px-6 py-4 text-left text-sm font-bold">班级名称</th>
            <th class="px-6 py-4 text-left text-sm font-bold">专业</th>
            <th class="px-6 py-4 text-left text-sm font-bold">年级</th>
            <th class="px-6 py-4 text-left text-sm font-bold">班主任</th>
            <th class="px-6 py-4 text-left text-sm font-bold">人数</th>
            <th class="px-6 py-4 text-left text-sm font-bold">教室</th>
            <th class="px-6 py-4 text-center text-sm font-bold">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-for="cls in classes" :key="cls.id" class="hover:bg-gray-50/80 transition-colors">
            <td class="px-6 py-5 text-sm text-gray-500 font-mono">{{ cls.classNo }}</td>
            <td class="px-6 py-5 text-sm font-bold text-gray-800">{{ cls.className }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ cls.major }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ cls.grade }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ cls.teacher }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ cls.studentCount }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ cls.classroom }}</td>
            <td class="px-6 py-5">
              <div class="flex items-center justify-center gap-3">
                <button @click="handleDelete(cls.id)" class="btn-icon bg-danger shadow-sm cursor-pointer"><MinusCircle :size="14" /></button>
                <button @click="handleEdit(cls)" class="btn-icon bg-orange-500 shadow-sm cursor-pointer"><Pencil :size="14" /></button>
                <button @click="handleView(cls)" class="btn-icon bg-primary shadow-sm cursor-pointer"><Search :size="14" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div class="px-6 py-5 bg-gray-50/50 border-t border-gray-100 flex items-center justify-between">
        <span class="text-xs text-gray-500">
          显示 1 到 {{ classes.length }} 共 {{ total }} 条记录
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

    <!-- 新增班级弹窗 -->
    <transition name="modal">
      <div v-if="showAddModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 overflow-hidden">
          <div class="bg-primary px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">添加班级</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer"><X :size="20" /></button>
          </div>
          
          <div class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">班级编号 <span class="text-red-500">*</span></label>
                <input v-model="formData.classNo" type="text" placeholder="例如: C2024001" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
                <p class="text-xs text-gray-500 mt-1">建议使用格式: C + 年份 + 3位数字</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">班级名称 <span class="text-red-500">*</span></label>
                <input v-model="formData.className" type="text" placeholder="例如: 计算机科学与技术2401班" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">专业</label>
                <input v-model="formData.major" type="text" placeholder="例如: 计算机科学与技术" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">年级</label>
                <select v-model="formData.grade" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all">
                  <option value="2024级">2024级</option>
                  <option value="2023级">2023级</option>
                  <option value="2022级">2022级</option>
                  <option value="2021级">2021级</option>
                </select>
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">班主任</label>
                <input v-model="formData.teacher" type="text" placeholder="例如: 王老师" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">学生人数</label>
                <input v-model.number="formData.studentCount" type="number" min="0" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">教室</label>
              <input v-model="formData.classroom" type="text" placeholder="例如: 教学楼A101" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition-all" />
            </div>
          </div>
          
          <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex justify-end gap-3">
            <button @click="closeModal" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer">取消</button>
            <button @click="handleSubmitAdd" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors cursor-pointer">确定</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 编辑班级弹窗 -->
    <transition name="modal">
      <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-2xl mx-4 overflow-hidden">
          <div class="bg-orange-500 px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">编辑班级</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer"><X :size="20" /></button>
          </div>
          
          <div class="p-6 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">班级编号 <span class="text-red-500">*</span></label>
                <input v-model="formData.classNo" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
                <p class="text-xs text-gray-500 mt-1">注意：修改编号时需确保不与其他班级重复</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">班级名称 <span class="text-red-500">*</span></label>
                <input v-model="formData.className" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">专业</label>
                <input v-model="formData.major" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">年级</label>
                <select v-model="formData.grade" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all">
                  <option value="2024级">2024级</option>
                  <option value="2023级">2023级</option>
                  <option value="2022级">2022级</option>
                  <option value="2021级">2021级</option>
                </select>
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">班主任</label>
                <input v-model="formData.teacher" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">学生人数</label>
                <input v-model.number="formData.studentCount" type="number" min="0" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">教室</label>
              <input v-model="formData.classroom" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-all" />
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
      <div v-if="showViewModal && currentClass" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-lg mx-4 overflow-hidden">
          <div class="bg-primary px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">班级详情</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer"><X :size="20" /></button>
          </div>
          
          <div class="p-6">
            <div class="space-y-4">
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">班级编号：</span>
                <span class="font-medium text-gray-800">{{ currentClass.classNo }}</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">班级名称：</span>
                <span class="font-medium text-gray-800">{{ currentClass.className }}</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">专业：</span>
                <span class="font-medium text-gray-800">{{ currentClass.major || '未设置' }}</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">年级：</span>
                <span class="font-medium text-gray-800">{{ currentClass.grade || '未设置' }}</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">班主任：</span>
                <span class="font-medium text-gray-800">{{ currentClass.teacher || '未设置' }}</span>
              </div>
              <div class="flex justify-between py-2 border-b border-gray-100">
                <span class="text-gray-600">学生人数：</span>
                <span class="font-medium text-gray-800">{{ currentClass.studentCount }} 人</span>
              </div>
              <div class="flex justify-between py-2">
                <span class="text-gray-600">教室：</span>
                <span class="font-medium text-gray-800">{{ currentClass.classroom || '未设置' }}</span>
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
