<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, MinusCircle, Pencil, Search } from 'lucide-vue-next'
import { getDepartments, deleteDepartment, addDepartment, updateDepartment, type Department } from '@/api/department'

interface DepartmentItem extends Department {
  id: number
}

const departments = ref<DepartmentItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 弹窗控制
const showAddModal = ref(false)
const showEditModal = ref(false)
const showViewModal = ref(false)
const currentDept = ref<DepartmentItem | null>(null)

// 表单数据
const formData = ref({
  deptNo: '',
  deptName: '',
  location: '',
  manager: ''
})

// 加载部门数据
const loadDepartments = async () => {
  loading.value = true
  try {
    const response = await getDepartments({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    })
    departments.value = response.data.list as DepartmentItem[]
    total.value = response.data.total
  } catch (error) {
    console.error('加载部门数据失败:', error)
    alert('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗
const handleAdd = () => {
  formData.value = { deptNo: '', deptName: '', location: '', manager: '' }
  showAddModal.value = true
}

// 打开编辑弹窗
const handleEdit = (dept: DepartmentItem) => {
  currentDept.value = dept
  formData.value = {
    deptNo: dept.deptNo,
    deptName: dept.deptName,
    location: dept.location || '',
    manager: dept.manager || ''
  }
  showEditModal.value = true
}

// 打开详情弹窗
const handleView = (dept: DepartmentItem) => {
  currentDept.value = dept
  showViewModal.value = true
}

// 关闭弹窗
const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  showViewModal.value = false
  currentDept.value = null
}

// 提交新增
const handleSubmitAdd = async () => {
  if (!formData.value.deptNo || !formData.value.deptName) {
    alert('部门编号和部门名称不能为空')
    return
  }
  
  const exists = departments.value.some(dept => dept.deptNo === formData.value.deptNo)
  if (exists) {
    alert('部门编号已存在，请使用其他编号')
    return
  }
  
  try {
    await addDepartment(formData.value)
    alert('添加成功')
    closeModal()
    await loadDepartments()
  } catch (error: any) {
    console.error('添加部门失败:', error)
    alert(error.message?.includes('Duplicate') ? '部门编号已存在' : '添加失败')
  }
}

// 提交编辑
const handleSubmitEdit = async () => {
  if (!formData.value.deptNo || !formData.value.deptName) {
    alert('部门编号和部门名称不能为空')
    return
  }
  
  if (!currentDept.value) return
  
  try {
    await updateDepartment(currentDept.value.id, formData.value)
    alert('更新成功')
    closeModal()
    await loadDepartments()
  } catch (error: any) {
    console.error('更新部门失败:', error)
    alert(error.message?.includes('Duplicate') ? '部门编号已存在' : '更新失败')
  }
}

// 删除部门
const handleDelete = async (id: number) => {
  if (!confirm('确定要删除该部门吗?')) return
  
  try {
    await deleteDepartment(id)
    alert('删除成功')
    await loadDepartments()
  } catch (error) {
    console.error('删除部门失败:', error)
    alert('删除失败')
  }
}

// 分页改变
const handlePageChange = (page: number) => {
  if (page < 1 || page * pageSize.value > total.value) return
  currentPage.value = page
  loadDepartments()
}

onMounted(() => {
  loadDepartments()
})
</script>

<template>
  <div class="p-8">
    <!-- Breadcrumb -->
    <div class="flex items-center text-xs text-gray-500 mb-6">
      <span>首页</span>
      <span class="mx-2 text-gray-300">{'>>'}</span>
      <span class="text-primary font-medium">部门管理</span>
    </div>

    <!-- Action Button -->
    <button 
      @click="handleAdd"
      class="flex items-center gap-2 bg-success text-white px-5 py-2.5 rounded-md text-sm font-medium shadow-l1 hover:brightness-105 transition-all mb-8 cursor-pointer"
    >
      <Plus :size="18" />
      添加部门
    </button>

    <!-- Table Container -->
    <div class="bg-white rounded-xl shadow-l2 border border-gray-100 overflow-hidden">
      <table class="w-full border-collapse">
        <thead>
          <tr class="bg-[#fff9e6]/80 text-gray-700 font-bold border-b border-gray-100">
            <th class="px-6 py-4 text-left text-sm font-bold w-[15%]">部门编号</th>
            <th class="px-6 py-4 text-left text-sm font-bold w-[25%]">部门名称</th>
            <th class="px-6 py-4 text-left text-sm font-bold w-[20%]">部门位置</th>
            <th class="px-6 py-4 text-left text-sm font-bold w-[20%]">部门负责人</th>
            <th class="px-6 py-4 text-center text-sm font-bold w-[20%]">操作列表</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-for="dept in departments" :key="dept.id" class="hover:bg-gray-50/80 transition-colors">
            <td class="px-6 py-5 text-sm text-gray-500 font-medium font-mono">{{ dept.deptNo }}</td>
            <td class="px-6 py-5 text-sm font-bold text-gray-800">{{ dept.deptName }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ dept.location }}</td>
            <td class="px-6 py-5 text-sm text-gray-600">{{ dept.manager }}</td>
            <td class="px-6 py-5">
              <div class="flex items-center justify-center gap-3">
                <button @click="handleDelete(dept.id)" class="btn-icon bg-danger shadow-sm cursor-pointer" title="删除">
                  <MinusCircle :size="14" />
                </button>
                <button @click="handleEdit(dept)" class="btn-icon bg-orange-500 shadow-sm cursor-pointer" title="编辑">
                  <Pencil :size="14" />
                </button>
                <button @click="handleView(dept)" class="btn-icon bg-primary shadow-sm hover:bg-primary-dark cursor-pointer" title="查看">
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
          显示 1 到 {{ departments.length }} 共 {{ total }} 条记录
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

    <!-- 新增部门弹窗 -->
    <transition name="modal">
      <div v-if="showAddModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-md mx-4 overflow-hidden">
          <div class="bg-primary px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">添加部门</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer">✕</button>
          </div>
          <div class="p-6 space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">部门编号 <span class="text-red-500">*</span></label>
              <input v-model="formData.deptNo" type="text" placeholder="例如: A0007" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">部门名称 <span class="text-red-500">*</span></label>
              <input v-model="formData.deptName" type="text" placeholder="请输入部门名称" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">部门位置</label>
              <input v-model="formData.location" type="text" placeholder="请输入部门位置" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">部门负责人</label>
              <input v-model="formData.manager" type="text" placeholder="请输入部门负责人" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary outline-none" />
            </div>
          </div>
          <div class="px-6 py-4 bg-gray-50 flex justify-end gap-3">
            <button @click="closeModal" class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer">取消</button>
            <button @click="handleSubmitAdd" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors cursor-pointer">确定</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 编辑部门弹窗 -->
    <transition name="modal">
      <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-md mx-4 overflow-hidden">
          <div class="bg-orange-500 px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">编辑部门</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer">✕</button>
          </div>
          <div class="p-6 space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">部门编号 <span class="text-red-500">*</span></label>
              <input v-model="formData.deptNo" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">部门名称 <span class="text-red-500">*</span></label>
              <input v-model="formData.deptName" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">部门位置</label>
              <input v-model="formData.location" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">部门负责人</label>
              <input v-model="formData.manager" type="text" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 outline-none" />
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
      <div v-if="showViewModal && currentDept" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" @click.self="closeModal">
        <div class="bg-white rounded-xl shadow-2xl w-full max-w-md mx-4 overflow-hidden">
          <div class="bg-primary px-6 py-4 flex items-center justify-between">
            <h3 class="text-white font-semibold text-lg">部门详情</h3>
            <button @click="closeModal" class="text-white/80 hover:text-white cursor-pointer">✕</button>
          </div>
          <div class="p-6">
            <div class="bg-gray-50 p-4 rounded-lg grid grid-cols-2 gap-4">
              <div><div class="text-xs text-gray-500 mb-1">部门编号</div><div class="text-sm font-semibold">{{ currentDept.deptNo }}</div></div>
              <div><div class="text-xs text-gray-500 mb-1">部门名称</div><div class="text-sm font-semibold">{{ currentDept.deptName }}</div></div>
              <div><div class="text-xs text-gray-500 mb-1">部门位置</div><div class="text-sm font-semibold">{{ currentDept.location || '未设置' }}</div></div>
              <div><div class="text-xs text-gray-500 mb-1">部门负责人</div><div class="text-sm font-semibold">{{ currentDept.manager || '未设置' }}</div></div>
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
