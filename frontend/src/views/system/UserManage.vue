<template>
  <div class="user-manage">
    <div class="search-bar">
      <el-input v-model="searchParams.username" placeholder="用户名" clearable style="width: 180px" @clear="handleSearch" />
      <el-input v-model="searchParams.realName" placeholder="真实姓名" clearable style="width: 180px; margin-left: 12px" @clear="handleSearch" />
      <el-select v-model="searchParams.status" placeholder="状态" clearable style="width: 120px; margin-left: 12px" @clear="handleSearch">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" style="margin-left: 12px" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-toolbar">
      <el-button type="primary" v-permission="'user:create'" :icon="Plus" @click="handleAdd">新增用户</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="department" label="部门" width="120" />
      <el-table-column label="角色" min-width="200">
        <template #default="{ row }">
          <el-tag v-for="name in row.roleNames" :key="name" size="small" style="margin-right: 4px">{{ name }}</el-tag>
          <span v-if="!row.roleNames || row.roleNames.length === 0" style="color: #999">未分配</span>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="(val: boolean) => handleStatusChange(row, val)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link v-permission="'user:edit'" @click="handleEdit(row)">编辑</el-button>
          <el-button type="warning" link v-permission="'user:role'" @click="handleAssignRole(row)">角色</el-button>
          <el-button type="danger" link v-permission="'user:delete'" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="searchParams.pageNum"
        v-model:page-size="searchParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="fetchData"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="formData.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="角色分配" width="450px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id" :value="role.id" style="margin-right: 20px; margin-bottom: 8px">
          {{ role.roleName }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitting" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getUserPage, createUser, updateUser, updateUserStatus, deleteUser, assignUserRoles, getAllRoles, getUserDetail } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { vPermission } from '@/directives/permission'
import type { SysUser } from '@/types/user'

const authStore = useAuthStore()

const tableData = ref<SysUser[]>([])
const total = ref(0)
const loading = ref(false)

const searchParams = reactive({ pageNum: 1, pageSize: 10, username: '', realName: '', status: undefined as number | undefined })

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({ username: '', password: '', realName: '', department: '', phone: '', email: '' })

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 20, message: '2-20个字符', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 64, message: '6-64个字符', trigger: 'blur' }]
}

const roleDialogVisible = ref(false)
const roleUserId = ref<number | null>(null)
const selectedRoleIds = ref<number[]>([])
const allRoles = ref<{ id: number; roleCode: string; roleName: string }[]>([])
const roleSubmitting = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserPage({ ...searchParams })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

async function fetchAllRoles() {
  const res = await getAllRoles()
  if (res.code === 200) {
    allRoles.value = res.data
  }
}

function handleSearch() {
  searchParams.pageNum = 1
  fetchData()
}

function handleReset() {
  searchParams.username = ''
  searchParams.realName = ''
  searchParams.status = undefined
  searchParams.pageNum = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  editingId.value = null
  dialogVisible.value = true
}

async function handleEdit(row: SysUser) {
  isEdit.value = true
  editingId.value = row.id
  const res = await getUserDetail(row.id)
  if (res.code === 200) {
    formData.username = res.data.username
    formData.realName = res.data.realName
    formData.department = res.data.department || ''
    formData.phone = res.data.phone || ''
    formData.email = res.data.email || ''
    formData.password = ''
  }
  dialogVisible.value = true
}

function resetForm() {
  formData.username = ''
  formData.password = ''
  formData.realName = ''
  formData.department = ''
  formData.phone = ''
  formData.email = ''
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      const res = await updateUser(editingId.value, {
        realName: formData.realName,
        department: formData.department,
        phone: formData.phone,
        email: formData.email
      })
      if (res.code === 200) {
        ElMessage.success('编辑成功')
        dialogVisible.value = false
        fetchData()
      }
    } else {
      const res = await createUser({
        username: formData.username,
        password: formData.password,
        realName: formData.realName,
        department: formData.department,
        phone: formData.phone,
        email: formData.email
      })
      if (res.code === 200) {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        fetchData()
      }
    }
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(row: SysUser, val: boolean) {
  const res = await updateUserStatus(row.id, { status: val ? 1 : 0 })
  if (res.code === 200) {
    ElMessage.success(val ? '已启用' : '已禁用')
    fetchData()
  }
}

function handleDelete(row: SysUser) {
  ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？`, '删除确认', { type: 'warning' }).then(async () => {
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  }).catch(() => {})
}

async function handleAssignRole(row: SysUser) {
  roleUserId.value = row.id
  await fetchAllRoles()
  selectedRoleIds.value = row.roleIds || []
  roleDialogVisible.value = true
}

async function handleRoleSubmit() {
  if (roleUserId.value === null) return
  roleSubmitting.value = true
  try {
    const res = await assignUserRoles(roleUserId.value, { roleIds: selectedRoleIds.value })
    if (res.code === 200) {
      ElMessage.success('角色分配成功')
      roleDialogVisible.value = false
      authStore.refreshPermissions()
      fetchData()
    }
  } finally {
    roleSubmitting.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchAllRoles()
})
</script>

<style scoped>
.user-manage { padding: 0; }
.search-bar { display: flex; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 8px; }
.table-toolbar { margin-bottom: 16px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
