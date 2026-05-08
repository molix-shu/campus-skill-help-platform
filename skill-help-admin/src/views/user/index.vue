<template>
  <div class="page-user">
    <!-- 页面顶部统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon blue"><svg viewBox="0 0 24 24" fill="none" width="20" height="20"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" stroke="currentColor" stroke-width="2"/><circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/></svg></div>
        <div class="stat-body">
          <div class="stat-value">{{ total }}</div>
          <div class="stat-label">注册用户总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange"><svg viewBox="0 0 24 24" fill="none" width="20" height="20"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M12 6v6l4 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></div>
        <div class="stat-body">
          <div class="stat-value">{{ pendingCount }}</div>
          <div class="stat-label">待审核认证</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><svg viewBox="0 0 24 24" fill="none" width="20" height="20"><path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
        <div class="stat-body">
          <div class="stat-value">{{ verifiedCount }}</div>
          <div class="stat-label">已实名认证</div>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="content-card">
      <div class="card-header">
        <h3 class="card-title">用户列表</h3>
        <div class="filter-bar">
          <el-input v-model="keyword" placeholder="搜索昵称、学号..." style="width: 220px" clearable prefix-icon="Search" />
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="注册开始"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
            clearable />
          <el-button type="primary" @click="fetchData">
            <svg viewBox="0 0 24 24" fill="none" width="14" height="14" style="margin-right:4px"><circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/><path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
            查询
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="100" />
        <el-table-column prop="studentId" label="学号" min-width="120" />
        <el-table-column prop="college" label="学院" min-width="110" />
        <el-table-column label="认证照" width="80" align="center">
          <template #default="{ row }">
            <el-image 
              v-if="row.studentCardUrl" 
              :src="row.studentCardUrl" 
              :preview-src-list="[row.studentCardUrl]" 
              preview-teleported
              style="width: 36px; height: 36px; border-radius: 6px;" 
            />
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="认证状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.authStatus === 2" type="success" size="small">已认证</el-tag>
            <el-tag v-else-if="row.authStatus === 1" type="warning" size="small">待审核</el-tag>
            <el-tag v-else type="info" size="small">未认证</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : ''" size="small">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="160">
          <template #default="{ row }">
            <span class="text-muted">{{ row.createTime?.replace('T', ' ').substring(0, 16) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.authStatus === 1" size="small" type="success" @click="handleAudit(row.id, 2)">通过</el-button>
            <el-button v-if="row.authStatus === 1" size="small" type="warning" @click="handleAudit(row.id, 0)">驳回</el-button>
            <el-button v-if="row.role !== 1" size="small" type="danger" @click="handleDelete(row.id)">封禁</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="table-footer">
        <span class="table-total">共 {{ total }} 条记录</span>
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          background
          layout="prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </div>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="填写驳回原因" width="440px" :close-on-click-modal="false">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因，将推送至用户端" />
      <div style="margin-top: 14px;">
        <div class="quick-label">快捷理由</div>
        <div class="quick-tags">
          <span class="quick-tag" @click="rejectReason = '照片反光或过于模糊'">模糊/反光</span>
          <span class="quick-tag" @click="rejectReason = '姓名与学号不匹配，请核对'">信息不匹配</span>
          <span class="quick-tag" @click="rejectReason = '需拍出带有钢印和相片的一页'">需相片钢印</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const keyword = ref('')
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dateRange = ref([])

const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentRejectId = ref(null)

const pendingCount = computed(() => tableData.value.filter(u => u.authStatus === 1).length)
const verifiedCount = computed(() => tableData.value.filter(u => u.authStatus === 2).length)

const fetchData = () => {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value }
    if (dateRange.value && dateRange.value.length === 2) {
       params.startTime = dateRange.value[0]
       params.endTime = dateRange.value[1]
    }
    request.get('/user/list', { params }).then(res => {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
    })
}

const handleAudit = (id, status) => {
    if (status === 0) {
       currentRejectId.value = id;
       rejectReason.value = '';
       rejectDialogVisible.value = true;
       return;
    }
    request.post('/user/audit/' + id, { status }).then(() => {
        ElMessage.success('审核通过')
        fetchData()
    })
}

const confirmReject = () => {
    if(!rejectReason.value) return ElMessage.warning('请填写驳回原因');
    request.post('/user/audit/' + currentRejectId.value, { status: 0, message: rejectReason.value }).then(() => {
        ElMessage.success('已驳回')
        rejectDialogVisible.value = false;
        fetchData()
    })
}

const handleDelete = (id) => {
    ElMessageBox.confirm('确定要封禁该用户吗？', '操作确认', {
        confirmButtonText: '确认封禁',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        request.post('/user/delete/' + id).then(() => {
            ElMessage.success('用户已封禁')
            fetchData()
        })
    }).catch(() => {})
}

onMounted(() => { fetchData() })
</script>

<style scoped>
/* 统计卡片 */
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card {
  background: #fff; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px;
  border: 1px solid #F2F3F5; transition: box-shadow 0.2s;
}
.stat-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.stat-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-icon.blue { background: #E8F3FF; color: #165DFF; }
.stat-icon.orange { background: #FFF7E8; color: #FF7D00; }
.stat-icon.green { background: #E8FFEA; color: #00B42A; }
.stat-value { font-size: 28px; font-weight: 700; color: #1D2129; line-height: 1; }
.stat-label { font-size: 13px; color: #86909C; margin-top: 4px; }

/* 内容卡片 */
.content-card {
  background: #fff; border-radius: 12px; padding: 24px;
  border: 1px solid #F2F3F5;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.card-title { font-size: 16px; font-weight: 600; color: #1D2129; margin: 0; white-space: nowrap; }
.filter-bar { display: flex; gap: 10px; align-items: center; }

/* 表格底部 */
.table-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #F2F3F5; }
.table-total { font-size: 13px; color: #86909C; }

.text-muted { color: #C9CDD4; font-size: 12px; }

/* 快捷标签 */
.quick-label { font-size: 12px; color: #86909C; margin-bottom: 8px; }
.quick-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.quick-tag {
  font-size: 12px; padding: 4px 12px; border-radius: 4px;
  background: #F2F3F5; color: #4E5969; cursor: pointer; transition: all 0.15s;
}
.quick-tag:hover { background: #E8F3FF; color: #165DFF; }
</style>
