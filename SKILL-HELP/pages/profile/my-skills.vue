<template>
  <view class="container">
    <view class="list-container">
      <view class="post-card-minimal" v-for="item in skillList" :key="item.id">
        <!-- 移除彩色背景大方块，只保留纯粹的文本卡片 -->
        <view class="card-hero-minimal" @click="handleEdit(item)">
          <text class="hero-text-minimal">{{ item.title }}</text>
        </view>
        
        <view class="card-footer-minimal">
          <view class="footer-top">
            <view class="tags-time-wrap">
              <text class="hashtag-minimal" v-if="item.tags">{{ formatTags(item.tags) }}</text>
              <text class="time-text">{{ formatTime(item.createTime) }}发布</text>
            </view>
            <text class="status-badge-minimal" :class="item.status === 1 ? 'status-active' : 'status-offline'">
              {{ item.status === 1 ? '接单中' : '已下架' }}
            </text>
          </view>
          
          <view class="operations-wrap" v-if="item.status === 1">
            <button class="action-btn-minimal bg-dark" @click="handleEdit(item)">修改内容</button>
            <button class="action-btn-minimal outline-dark" @click="handleOffline(item)">下架互助</button>
          </view>
        </view>
      </view>

      <view v-if="skillList.length === 0" class="empty-state">
        <text class="empty-text">你还没有发布过任何技能哦~</text>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      skillList: []
    }
  },
  onShow() {
    this.fetchSkills();
  },
  onPullDownRefresh() {
    this.fetchSkills().finally(() => {
      uni.stopPullDownRefresh();
    });
  },
  methods: {
    fetchSkills() {
      const user = uni.getStorageSync('userInfo');
      if (!user || !user.id) return;
      
      return request({
        url: '/api/skill/my?userId=' + user.id,
        method: 'GET'
      }).then(res => {
        this.skillList = res || [];
      }).catch(err => {
        console.error('获取技能列表失败', err);
      });
    },
    handleEdit(item) {
      uni.navigateTo({ url: '/pages/publish/publish-skill?id=' + item.id });
    },
    handleOffline(item) {
      const user = uni.getStorageSync('userInfo');
      uni.showModal({
        title: '下架确认',
        content: `确定要下架技能"${item.title}"吗？`,
        confirmColor: '#111111',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading();
            request({
              url: `/api/skill/offline?id=${item.id}&userId=${user.id}`,
              method: 'POST'
            }).then(() => {
              uni.hideLoading();
              uni.showToast({ title: '已下架', icon: 'success' });
              this.fetchSkills();
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
/* 严格极简黑白灰 */
page {
  background-color: #F7F7F7;
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

.post-card-minimal {
  background-color: #FFFFFF;
  border-radius: 20px;
  margin-bottom: 20px;
  padding: 24px 20px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.015);
  border: 1px solid rgba(0,0,0,0.03);
  box-sizing: border-box;
  width: 100%;
}
.card-hero-minimal {
  margin-bottom: 16px;
}
.hero-text-minimal {
  font-size: 20px;
  font-weight: 700;
  color: #111111;
  line-height: 1.4;
  display: block;
}

.card-footer-minimal {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}
.footer-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.tags-time-wrap {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding-right: 12px;
}
.hashtag-minimal {
  font-size: 13px;
  font-weight: 500;
  color: #5B8DEF; /* 雾蓝 */
  background-color: #F4F8FF;
  padding: 4px 10px;
  border-radius: 6px;
  align-self: flex-start;
  margin-bottom: 8px;
}
.time-text {
  font-size: 12px;
  color: #8E8E93;
}
.status-badge-minimal {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 6px;
  flex-shrink: 0;
  border: 1px solid transparent;
}
.status-active {
  background-color: #F7F7F7;
  color: #111111;
  border-color: rgba(0,0,0,0.06);
}
.status-offline {
  background-color: #FFFFFF;
  color: #C7C7CC;
  border-color: #EFEFF0;
}

.operations-wrap {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-top: 1px solid rgba(0,0,0,0.04);
  padding-top: 16px;
}
.action-btn-minimal {
  font-size: 13px;
  font-weight: 500;
  border-radius: 16px;
  padding: 0 16px;
  height: 32px;
  line-height: 32px;
  margin: 0;
}
.action-btn-minimal::after {
  border: none;
}
.bg-dark {
  background-color: #111111;
  color: #FFFFFF;
  margin-right: 12px;
}
.bg-dark:active {
  background-color: #2B2B2B;
}
.outline-dark {
  background-color: #FFFFFF;
  color: #111111;
  border: 1px solid rgba(0,0,0,0.1);
}
.outline-dark:active {
  background-color: #F7F7F7;
}

.empty-state {
  text-align: center;
  margin-top: 100px;
}
.empty-text {
  color: #8E8E93;
  font-size: 14px;
}
</style>
