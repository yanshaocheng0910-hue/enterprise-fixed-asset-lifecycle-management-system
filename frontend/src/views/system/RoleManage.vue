<template>
  <div class="role-manage">
    <div class="table-toolbar">
      <el-button type="primary" v-permission="'role:create'" :icon="Plus" @click="handleAdd">新增角色</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border>
      <el-table-column prop="roleCode" label="角色编码" width="150" />
      <el-table-column prop="roleName" label="角色名称" width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="userCount" label="用户数" width="80" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link v-permission="'role:edit'" @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" link v-permission="'role:permission'" @click="handlePermission(row)">权限配置</el-button>
          <el-button type="danger" link v-permission="'role:delete'" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" :disabled="isEdit" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permDialogVisible" title="权限配置" width="600px">
      <el-tree
        ref="permTreeRef"
        :data="permTreeData"
        show-checkbox
        node-key="id"
        :props="{ children: 'permissions', label: 'permissionName' }"
        :default-checked-keys="checkedPermIds"
        default-expand-all
      >
        <template #default="{ data }">
          <span>{{ data.permissionName }} <el-tag size="small" type="info" style="margin-left: 8px">{{ data.permissionCode }}</el-tag></span>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitting" @click="handlePermSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ElTree } from 'element-plus'
import { getRolePage, getRoleDetail, createRole, updateRole, deleteRole, assignRolePermissions } from '@/api/user'
import { getPermissionTree } from '@/api/permission'
import { vPermission } from '@/directives/permission'

const tableData = ref<any[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({ roleCode: '', roleName: '', description: '' })

const formRules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const permDialogVisible = ref(false)
const permRoleId = ref<number | null>(null)
const permTreeRef = ref<InstanceType<typeof ElTree>>()
const permTreeData = ref<any[]>([])
const checkedPermIds = ref<number[]>([])
const permSubmitting = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const res = await getRolePage()
    if (res.code === 200) {
      tableData.value = res.data.records
    }
  } finally {
    loading.value = false
  }
}

async function handleAdd() {
  isEdit.value = false
  editingId.value = null
  dialogVisible.value = true
}

async function handleEdit(row: any) {
  isEdit.value = true
  editingId.value = row.id
  const res = await getRoleDetail(row.id)
  if (res.code === 200) {
    formData.roleCode = res.data.roleCode
    formData.roleName = res.data.roleName
    formData.description = res.data.description || ''
  }
  dialogVisible.value = true
}

function resetForm() {
  formData.roleCode = ''
  formData.roleName = ''
  formData.description = ''
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      const res = await updateRole(editingId.value, { roleName: formData.roleName, description: formData.description })
      if (res.code === 200) {
        ElMessage.success('编辑成功')
        dialogVisible.value = false
        fetchData()
      }
    } else {
      const res = await createRole({ roleCode: formData.roleCode, roleName: formData.roleName, description: formData.description })
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

function handleDelete(row: any) {
  ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '删除确认', { type: 'warning' }).then(async () => {
    const res = await deleteRole(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  }).catch(() => {})
}

async function handlePermission(row: any) {
  permRoleId.value = row.id
  const res = await getPermissionTree()
  if (res.code === 200) {
    permTreeData.value = res.data.map((m: any) => ({
      id: 'm-' + m.module,
      permissionName: m.module,
      permissionCode: '',
      permissions: m.permissions
    }))
  }
  const detail = await getRoleDetail(row.id)
  if (detail.code === 200) {
    checkedPermIds.value = detail.data.permissionIds || []
  }
  permDialogVisible.value = true
}

async function handlePermSubmit() {
  if (permRoleId.value === null) return
  permSubmitting.value = true
  try {
    const checked = permTreeRef.value?.getCheckedKeys() || []
    const permissionIds = checked.filter((k: any) => typeof k === 'number') as number[]
    const res = await assignRolePermissions(permRoleId.value, { permissionIds })
    if (res.code === 200) {
      ElMessage.success('权限配置成功')
      permDialogVisible.value = false
      fetchData()
    }
  } finally {
    permSubmitting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.role-manage { padding: 0; }
.table-toolbar { margin-bottom: 16px; }
</style>
