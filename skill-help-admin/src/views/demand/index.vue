<template>
  <div class="page-demand">
    <!-- 筛选工具栏 -->
    <div class="content-card">
      <div class="card-header">
        <h3 class="card-title">帖子 / 需求管理</h3>
        <div class="filter-bar">
          <el-input v-model="keyword" placeholder="搜索帖子内容..." style="width: 220px" clearable prefix-icon="Search" />
          <el-select v-model="status" placeholder="全部状态" style="width: 130px" clearable>
            <el-option label="寻找中" :value="1" />
            <el-option label="已解决" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
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
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="description" label="详情" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-desc">{{ row.description }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expectedTime" label="期望时间" width="110" show-overflow-tooltip />
        <el-table-column prop="serviceMethod" label="服务方式" width="100" show-overflow-tooltip />
        <el-table-column prop="rewardDesc" label="酬劳" width="100" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="warning" size="small">寻找中</el-tag>
            <el-tag v-else-if="row.status === 2" type="success" size="small">已解决</el-tag>
            <el-tag v-else-if="row.status === 3" type="info" size="small">已关闭</el-tag>
            <el-tag v-else size="small">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160">
          <template #default="{ row }">
            <span class="text-muted">{{ row.createTime?.replace('T', ' ').substring(0, 16) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row.id)">下架</el-button>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const keyword = ref('')
const status = ref('')
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dateRange = ref([])

const fetchData = () => {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value, status: status.value !== '' ? status.value : undefined }
    if (dateRange.value && dateRange.value.length === 2) {
       params.startTime = dateRange.value[0]
       params.endTime = dateRange.value[1]
    }
    request.get('/demand/list', { params }).then(res => {
        tableData.value = res.data.records || []
        total.value = res.data.total || 0
    })
}

const handleDelete = (id) => {
    ElMessageBox.confirm('确定下架该帖子吗？操作不可恢复。', '操作确认', {
        confirmButtonText: '确认下架',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        request.post('/demand/delete/' + id).then(() => {
            ElMessage.success('帖子已下架')
            fetchData()
        })
    }).catch(() => {})
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.content-card {
  background: #fff; border-radius: 12px; padding: 24px;
  border: 1px solid #F2F3F5;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.card-title { font-size: 16px; font-weight: 600; color: #1D2129; margin: 0; }
.filter-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }

.table-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 16px; border-top: 1px solid #F2F3F5; }
.table-total { font-size: 13px; color: #86909C; }
.text-muted { color: #C9CDD4; font-size: 12px; }
.text-desc { color: #4E5969; font-size: 13px; }
</style>
