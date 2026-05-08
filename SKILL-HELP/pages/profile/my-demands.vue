<template>
  <view class="container">
    <view class="list-container">
      <view class="demand-card" v-for="item in demandList" :key="item.id">
        <view class="demand-header">
          <view class="user-badge" v-if="item.tags && item.tags.includes('急求')">急求</view>
          <text class="demand-title">{{ item.title }}</text>
        </view>
        <text style="font-size: 11px; color: #8E8E93; margin-bottom: 8px; display: block;">发布于: {{ formatTime(item.createTime) }}</text>
        <view v-if="item.tags" style="margin-bottom: 8px;">
          <text style="font-size: 12px; color: #007AFF;">{{ formatTags(item.tags) }}</text>
        </view>
        <text class="demand-desc">{{ item.description }}</text>
        <view class="demand-footer" style="display: flex; flex-direction: column; align-items: stretch;">
          <view style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
            <view style="display: flex; flex-direction: column; flex: 1; padding-right: 12px;">
              <text style="font-size: 13px; color: #1D1D1F; font-weight: 500;">{{ item.rewardDesc ? '酬劳: ' + item.rewardDesc : '互助志愿' }}</text>
            </view>
            <text class="status-badge" :class="item.status === 1 ? 'status-active' : 'status-offline'" style="flex-shrink: 0;">
             {{ item.status === 1 ? '寻找中' : '已解决' }}
            </text>
          </view>
          
          <view style="display: flex; justify-content: flex-end; align-items: center; border-top: 1px dashed #E3E3E8; padding-top: 12px;" v-if="item.status === 1">
            <button class="action-btn" @click="handleEdit(item)" style="background-color: #007AFF; margin-left: 0; margin-right: 12px;">✎ 修改信息</button>
            <button class="action-btn" @click="handleResolve(item)">标记已解决</button>
          </view>
        </view>
      </view>

      <view v-if="demandList.length === 0" class="empty-state">
        <text class="empty-text">你还没有发布过需求帖子哦~</text>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      demandList: []
    }
  },
  onShow() {
    this.fetchDemands();
  },
  onPullDownRefresh() {
    this.fetchDemands().finally(() => {
      uni.stopPullDownRefresh();
    });
  },
  methods: {
    fetchDemands() {
      const user = uni.getStorageSync('userInfo');
      if (!user || !user.id) return Promise.resolve();
      
      return request({
        url: '/api/demand/my?userId=' + user.id,
        method: 'GET'
      }).then(res => {
        this.demandList = res || [];
      }).catch(err => {
        console.error('获取需求列表失败', err);
      });
    },
    handleEdit(item) {
      uni.navigateTo({ url: '/pages/publish/publish?id=' + item.id });
    },
    handleResolve(item) {
      const user = uni.getStorageSync('userInfo');
      uni.showModal({
        title: '解决确认',
        content: `确定已找到互助同学解决"${item.title}"了吗？标记后大厅将不再展示。`,
        confirmColor: '#00A650',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading();
            request({
              url: `/api/demand/resolve?id=${item.id}&userId=${user.id}`,
              method: 'POST'
            }).then(() => {
              uni.hideLoading();
              uni.showToast({ title: '已解决', icon: 'success' });
              this.fetchDemands();
            }).catch(() => uni.hideLoading());
          }
        }
      });
    },
    formatTime(time) {
      if (!time) return '';
      if (Array.isArray(time)) {
        const [y, m, d, h, min] = time;
        const pad = (n) => String(n).padStart(2, '0');
        return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}`;
      }
      return String(time).replace('T', ' ').substring(0, 16);
    },
    formatTags(tagsStr) {
      if (!tagsStr) return '';
      return tagsStr.split(/[,,，]/).filter(Boolean)
        .map(t => t.replace(/[#＃]/g, '').trim())
        .filter(Boolean)
        .map(t => '#' + t)
        .join(' ');
    }
  }
}
</script>

<style>
page {
  background-color: #F5F5F7;
  height: 100%;
}
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.list-container {
  flex: 1;
  padding: 20px;
  box-sizing: border-box;
  width: 100%;
}

/* 需求卡片复用 */
.demand-card {
  background-color: #FFF;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
  box-sizing: border-box;
  width: 100%;
}
.demand-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.user-badge {
  background-color: #FF3B30;
  color: #FFF;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 8px;
  font-weight: 600;
  margin-right: 10px;
}
.demand-title {
  font-size: 18px;
  font-weight: 600;
  color: #1D1D1F;
}
.demand-desc {
  font-size: 15px;
  color: #636366;
  line-height: 1.5;
  display: block;
  margin-bottom: 16px;
}
.demand-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.date {
  font-size: 13px;
  color: #AEAEB2;
}

.status-badge {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
}
.status-active {
  background-color: #FFF0E5;
  color: #FF9500;
}
.status-offline {
  background-color: #F5F5F7;
  color: #8E8E93;
}
.empty-state {
  text-align: center;
  margin-top: 100px;
}
.empty-text {
  color: #8E8E93;
  font-size: 15px;
}
.action-btn {
  background-color: #00A650;
  color: #FFF;
  font-size: 13px;
  font-weight: 600;
  border-radius: 14px;
  padding: 0 12px;
  height: 28px;
  line-height: 28px;
  margin: 0;
}
</style>
