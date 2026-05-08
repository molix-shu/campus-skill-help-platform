<template>
  <div class="page-appeal">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon orange"><svg viewBox="0 0 24 24" fill="none" width="20" height="20"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></div>
        <div class="stat-body">
          <div class="stat-value">{{ pendingCount }}</div>
          <div class="stat-label">待处理申诉</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><svg viewBox="0 0 24 24" fill="none" width="20" height="20"><path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M22 4L12 14.01l-3-3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
        <div class="stat-body">
          <div class="stat-value">{{ approvedCount }}</div>
          <div class="stat-label">已通过补偿</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red"><svg viewBox="0 0 24 24" fill="none" width="20" height="20"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="m15 9-6 6M9 9l6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></div>
        <div class="stat-body">
          <div class="stat-value">{{ rejectedCount }}</div>
          <div class="stat-label">已驳回</div>
        </div>
      </div>
    </div>

    <!-- 内容卡片 -->
    <div class="content-card">
      <div class="card-header">
        <h3 class="card-title">申诉判决中心</h3>
        <div class="filter-bar">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
            clearable />
          <el-button type="primary" @click="fetchData">查询</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="编号" width="80" align="center" />
        <el-table-column prop="userId" label="申诉人" width="90" align="center" />
        <el-table-column prop="relationTitle" label="争议订单" width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="申诉理由" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-desc">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" size="small">待处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="success" size="small">已通过</el-tag>
            <el-tag v-else type="danger" size="small">已驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申诉时间" width="160">
          <template #default="{ row }">
            <span class="text-muted">{{ row.createTime?.replace('T', ' ').substring(0, 16) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="判决操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="handleAudit(row.id, 1)">补偿信用</el-button>
              <el-button size="small" type="danger" @click="handleAudit(row.id, 2)">驳回</el-button>
            </template>
            <el-button v-else size="small" @click="handleAudit(row.id, 0)">撤销判决</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dateRange = ref([])

const pendingCount = computed(() => tableData.value.filter(a => a.status === 0).length)
const approvedCount = computed(() => tableData.value.filter(a => a.status === 1).length)
const rejectedCount = computed(() => tableData.value.filter(a => a.status === 2).length)

const fetchData = () => {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (dateRange.value && dateRange.value.length === 2) {
       params.startTime = dateRange.value[0]
       params.endTime = dateRange.value[1]
    }
    request.get('/appeal/list', { params }).then(res => {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
    })
}

const handleAudit = (id, status) => {
    request.post('/appeal/handle/' + id, { status }).then(() => {
        ElMessage.success('处理成功')
        fetchData()
    })
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
.stat-icon.orange { background: #FFF7E8; color: #FF7D00; }
.stat-icon.green { background: #E8FFEA; color: #00B42A; }
.stat-icon.red { background: #FFECE8; color: #F53F3F; }
.stat-value { font-size: 28px; font-weight: 700; color: #1D2129; line-height: 1; }
.stat-label { font-size: 13px; color: #86909C; margin-top: 4px; }

/* 内容卡片 */
.content-card {
  background: #fff; border-radius: 12px; padding: 24px;
  border: 1px solid #F2F3F5;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.card-title { font-size: 16px; font-weight: 600; color: #1D2129; margin: 0; }
.filter-bar { display: flex; gap: 10px; align-items: center; }

.table-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #F2F3F5; }
.table-total { font-size: 13px; color: #86909C; }
.text-muted { color: #C9CDD4; font-size: 12px; }
.text-desc { color: #4E5969; font-size: 13px; }
</style>
