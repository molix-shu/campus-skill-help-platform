<template>
  <view class="container">
    <view class="header-section">
      <text class="title">需求大厅</text>
      <text class="subtitle">这些同学在这个大厅急求帮助</text>
    </view>
    
    <!-- 顶层过滤区极其克制的设计 -->
    <view class="filter-sticky-area">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input class="search-input" placeholder="搜索急求：如高数、电脑维修..." v-model="searchQuery" @input="onSearchInput" />
      </view>
      <scroll-view scroll-x class="category-scroll" show-scrollbar="false">
        <view class="category-wrapper">
          <view class="category-tag" :class="{ active: currentTab === 0 }" @click="switchTab(0)">全宇宙的求助</view>
          <view class="category-tag" 
                v-for="cat in categories" 
                :key="cat.id" 
                :class="{ active: currentTab === cat.id }" 
                @click="switchTab(cat.id)">
            {{ cat.name }}
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="list-container">
      <transition-group name="fade">
        <view class="demand-card" v-for="item in filteredList" :key="item.id" @click="goToDetail(item.id)">
          
          <!-- 极简重磅结构：移除全部上方大块渐变背景 -->
          <view class="demand-header">
            <image class="avatar-small" :src="item.publisherAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
            <view class="header-text-zone">
              <view style="display: flex; align-items: center; justify-content: space-between;">
                <text class="publisher-name">{{ item.publisherName || '微信用户' }}</text>
                <text class="publish-time">{{ formatTime(item.createTime) }}</text>
              </view>
            </view>
          </view>
          
          <view class="demand-body">
             <text class="demand-title">{{ item.title }}</text>
             <text class="demand-desc">{{ item.description }}</text>
             <view class="tags-row" v-if="item.tags">
                <text class="minimal-tag">{{ formatTags(item.tags) }}</text>
             </view>
          </view>

          <view class="demand-footer">
            <text class="date">{{ item.rewardDesc ? '酬劳: ' + item.rewardDesc : '互助' }}</text>
            <!-- 接单大按钮极简设计 -->
            <button v-if="String(item.userId) !== String(currentUserId)" class="apply-btn" @click.stop="handleAcceptDemand(item)">立刻接单</button>
            <view v-else class="owner-pill">我发布的</view>
          </view>
        </view>
      </transition-group>
      
      <view v-if="filteredList.length === 0" style="text-align: center; color: #8E8E93; padding: 40px 0; font-size: 13px;">当前类别/搜索下暂无求助~</view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      demandList: [],
      filteredList: [],
      currentTab: 0,
      searchQuery: '',
      currentUserId: null,
      categories: [
        { id: 1, name: '生活求助' },
        { id: 2, name: '课业辅导' },
        { id: 3, name: '技术支持' },
        { id: 4, name: '出二手货' }
      ]
    }
  },
  onShow() {
    const user = uni.getStorageSync('userInfo');
    if (user && user.id) this.currentUserId = user.id;
    this.fetchDemands();
    const page = getCurrentPages()[getCurrentPages().length - 1];
    if (page && typeof page.getTabBar === 'function' && page.getTabBar()) {
      page.getTabBar().setData({ selected: 1 });
    }
  },
  onPullDownRefresh() {
    this.fetchDemands().finally(() => {
      uni.stopPullDownRefresh();
    });
  },
  onTabItemTap() {
    uni.vibrateShort({ type: 'light' });
  },
  methods: {
    goToDetail(id) {
      uni.navigateTo({ url: '/pages/demand/demand-detail?id=' + id });
    },
    switchTab(id) {
      if (this.currentTab === id) return;
      this.currentTab = id;
      uni.showLoading({ title: '筛选中', mask: true });
      setTimeout(() => {
        this.applyFilters();
        uni.hideLoading();
      }, 300);
    },
    onSearchInput() {
      this.applyFilters();
    },
    applyFilters() {
      let temp = this.demandList;
      if (this.currentTab !== 0) {
        const activeCat = this.categories.find(c => c.id === this.currentTab);
        temp = temp.filter(item => {
          if (item.categoryId === this.currentTab) return true;
          if (activeCat && item.tags && item.tags.includes(activeCat.name)) return true;
          return false;
        });
      }
      if (this.searchQuery.trim() !== '') {
        const query = this.searchQuery.toLowerCase();
        temp = temp.filter(item => 
          (item.title && item.title.toLowerCase().includes(query)) ||
          (item.tags && item.tags.toLowerCase().includes(query)) ||
          (item.description && item.description.toLowerCase().includes(query))
        );
      }
      this.filteredList = temp;
    },
    handleAcceptDemand(item) {
      const user = uni.getStorageSync('userInfo');
      if (!user || !user.id) return uni.showToast({ title: '请先登录', icon: 'none' });
      if (String(item.userId) === String(user.id)) return uni.showToast({ title: '不能接单自己发布的求助', icon: 'none' });

      uni.showModal({
        title: '接单确认',
        content: `你确认要帮助这位同学解决"${item.title}"吗？`,
        confirmColor: '#111111',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading();
            request({
              url: '/api/order/create',
              method: 'POST',
              data: {
                type: 2, 
                targetId: item.id,
                publisherId: item.userId,
                applicantId: user.id
              }
            }).then(() => {
              uni.hideLoading();
              uni.showToast({ title: '接单成功！', icon: 'success' });
            }).catch(() => uni.hideLoading());
          }
        }
      });
    },
    fetchDemands() {
      let url = '/api/demand/list';
      if (this.currentUserId) {
        url += '?userId=' + this.currentUserId;
      }
      request({
        url: url,
        method: 'GET'
      }).then(res => {
        this.demandList = res;
        this.applyFilters();
        uni.stopPullDownRefresh();
      }).catch(err => {
        console.error("获取需求列表失败", err);
        uni.stopPullDownRefresh();
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
/* 黑白灰极简底色 */
page {
  background-color: #F7F7F7;
  height: 100%;
}
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: relative;
}
.header-section {
  padding: 24px 20px 10px 20px;
  box-sizing: border-box;
  width: 100%;
}
.title {
  font-size: 30px;
  font-weight: 700;
  color: #111111;
  letter-spacing: -0.5px;
}
.subtitle {
  font-size: 14px;
  color: #8E8E93;
  margin-top: 6px;
  display: block;
}

.filter-sticky-area {
  position: sticky;
  top: 0;
  z-index: 99;
  background: rgba(247, 247, 247, 0.85); 
  backdrop-filter: blur(20px);
  padding: 10px 20px 0 20px;
}
.search-bar {
  display: flex;
  align-items: center;
  background-color: #EFEFF0;
  height: 40px;
  border-radius: 20px;
  padding: 0 16px;
  margin-bottom: 16px;
  border: 1px solid rgba(0,0,0,0.02);
}
.search-icon {
  font-size: 16px;
  color: #8E8E93;
  margin-right: 8px;
}
.search-input {
  flex: 1;
  font-size: 15px;
  color: #111111;
}

.category-scroll {
  white-space: nowrap;
  margin-bottom: 10px;
}
.category-wrapper {
  display: inline-flex;
  padding-bottom: 4px;
}
.category-tag {
  display: inline-block;
  padding: 8px 18px;
  margin-right: 12px;
  background-color: #FFFFFF;
  border-radius: 20px;
  font-size: 14px;
  color: #8E8E93;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0,0,0,0.015);
  border: 1px solid rgba(0,0,0,0.06);
  transition: all 0.2s;
}
.category-tag.active {
  background-color: #111111;
  color: #FFFFFF;
  border-color: #111111;
}

.list-container {
  flex: 1;
  padding: 5px 20px 100px 20px;
  box-sizing: border-box;
  width: 100%;
}
.demand-card {
  background-color: #FFFFFF;
  border-radius: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.015);
  border: 1px solid rgba(0,0,0,0.03);
  box-sizing: border-box;
  width: 100%;
  padding: 20px;
}

.demand-header {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
}
.avatar-small {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background-color: #F7F7F7;
  margin-right: 12px;
  border: 1px solid rgba(0,0,0,0.04);
}
.header-text-zone {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.publisher-name {
  font-size: 15px;
  font-weight: 600;
  color: #111111;
}
.publish-time {
  font-size: 12px;
  color: #8E8E93;
}

.demand-body {
  margin-bottom: 16px;
}
.demand-title {
  font-size: 18px;
  font-weight: 600;
  color: #111111;
  display: block;
  margin-bottom: 8px;
  line-height: 1.4;
}
.demand-desc {
  font-size: 14px;
  color: #8E8E93;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.tags-row {
  margin-top: 10px;
}
.minimal-tag {
  font-size: 13px;
  color: #5B8DEF; /* 雾蓝强调色 */
  background-color: #F4F8FF;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: 500;
  margin-right: 8px;
}

.demand-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(0,0,0,0.04);
  padding-top: 16px;
}
.date {
  font-size: 13px;
  color: #8E8E93;
}
.apply-btn {
  background-color: #111111;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 500;
  border-radius: 16px;
  padding: 0 20px;
  height: 32px;
  line-height: 32px;
  margin: 0;
  border: none;
}
.apply-btn:active {
  background-color: #2B2B2B;
}
.owner-pill {
  font-size: 12px;
  color: #8E8E93;
  background: #F7F7F7;
  padding: 6px 12px;
  border-radius: 12px;
  border: 1px solid rgba(0,0,0,0.04);
}
</style>
