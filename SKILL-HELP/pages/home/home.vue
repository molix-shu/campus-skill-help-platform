<template>
  <view class="container">
    <!-- 极简顶部导航栏 / 搜索栏 (毛玻璃悬浮) -->
    <view class="sticky-header">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input class="search-placeholder" placeholder="搜索技能：如剪辑、手绘、高数..."
               v-model="searchQuery" @input="onSearchInput" style="flex:1" />
      </view>
    </view>
 
    <!-- 瀑布流/卡片流展示区 (高级黑白灰极简名片) -->
    <view class="feed-scroll">
      
      <!-- 帖子分类筛选标签 --> 
      <view class="tag-filters">
        <view class="tag" 
              v-for="cat in categories" :key="cat.id"
              :class="{ 'active': currentTab === cat.name }"
              @click="switchTab(cat.name, cat.id)">
          {{ cat.name }}
        </view>
      </view>
      
      <!-- 动态按需渲染数据库返回的真实技能帖子 -->
      <transition-group name="fade">
        <view class="post-card" v-for="item in filteredList" :key="item.id" @click="goToDetail(item.id)">
          <view class="card-header">
            <image class="avatar" :src="item.publisherAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
            <view class="user-info">
              <text class="nickname">{{ item.publisherName || '微信用户' }}</text>
              <text class="time">{{ formatTime(item.createTime) }}发布</text>
            </view>
          </view>

          <!-- 极简重构：去除大面积渐变背景色，还原干净清爽的白底黑字呈现 -->
          <view class="card-title-minimal">
            <text class="hero-text-minimal">{{ item.title }}</text>
          </view>
          <view class="card-desc-minimal" v-if="item.description">
             <text class="desc-text-minimal">{{ item.description }}</text>
          </view>

          <view class="card-footer">
            <view class="tags-group">
              <text class="hashtag" v-if="item.tags">{{ formatTags(item.tags) }}</text>
            </view>
            <button
              v-if="userInfo && String(item.userId) === String(userInfo.id)"
              class="action-btn own-skill-btn"
              disabled
            >我的技能</button>
            <button v-else class="action-btn" @click.stop="handleApplySkill(item)">申请互助</button>
          </view>
        </view>
      </transition-group>

      <view v-if="filteredList.length === 0" class="bottom-filler">
        {{ skillList.length === 0 ? '空空如也，快去发布第一条技能吧！' : '没有找到相关技能喔~' }}
      </view>
      <view v-else class="bottom-filler">无更多数据</view>
    </view>  
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      userInfo: {},
      skillList: [],
      filteredList: [],
      currentTab: '全部推荐',
      searchQuery: '',
      categories: [
        { name: '全部推荐', id: 0 },
        { name: '辅导答疑', id: 2 },
        { name: '软件代做', id: 3 },
        { name: '运动陪练', id: 4 }
      ]
    }
  },
  onLoad() {
    const user = uni.getStorageSync('userInfo');
    if (user) {
      this.userInfo = user;
    }
  },
  onShow() {
    this.fetchSkills();
    const page = getCurrentPages()[getCurrentPages().length - 1];
    if (page && typeof page.getTabBar === 'function' && page.getTabBar()) {
      page.getTabBar().setData({ selected: 0 });
    }
  },
  onPullDownRefresh() {
    this.fetchSkills().finally(() => {
      uni.stopPullDownRefresh();
    });
  },
  onTabItemTap() {
    uni.vibrateShort({ type: 'light' });
  },
  methods: {
    goToDetail(id) {
      uni.navigateTo({ url: '/pages/home/skill-detail?id=' + id });
    },
    fetchSkills() {
      let url = '/api/skill/list';
      if (this.userInfo && this.userInfo.id) {
        url += '?userId=' + this.userInfo.id;
      }
      request({
        url: url,
        method: 'GET'
      }).then(res => {
        this.skillList = res;
        this.applyFilters();
        uni.stopPullDownRefresh();
      }).catch(err => {
        console.error("获取技能列表失败", err);
        uni.stopPullDownRefresh();
      });
    },
    switchTab(tabName, catId) {
      this.currentTab = tabName;
      this.applyFilters(catId);
    },
    onSearchInput(e) {
      this.searchQuery = e.detail.value;
      this.applyFilters();
    },
    applyFilters(catId = null) {
      let result = this.skillList;
      if (this.currentTab !== '全部推荐') {
        const activeCat = this.categories.find(c => c.name === this.currentTab);
        result = result.filter(item => {
          if (activeCat && item.categoryId === activeCat.id) return true;
          if (activeCat && (
            (item.tags && item.tags.includes(activeCat.name)) || 
            (item.title && item.title.includes(activeCat.name))
          )) return true;
          return false;
        });
      }
      if (this.searchQuery.trim() !== '') {
        const q = this.searchQuery.toLowerCase();
        result = result.filter(item => 
          (item.title && item.title.toLowerCase().includes(q)) || 
          (item.tags && item.tags.toLowerCase().includes(q))
        );
      }
      this.filteredList = result;
    },
    handleApplySkill(item) {
      if (!this.userInfo || !this.userInfo.id) {
        return uni.showToast({ title: '请先登录', icon: 'none' });
      }
      if (String(item.userId) === String(this.userInfo.id)) return uni.showToast({ title: '不能申请互助自己的技能', icon: 'none' });
      
      uni.showModal({
        title: '互助确认',
        content: `确定要向这位同学申请"${item.title}"吗？`,
        confirmText: '立刻申请',
        confirmColor: '#111111',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading();
            request({
              url: '/api/order/create',
              method: 'POST',
              data: {
                type: 1, 
                targetId: item.id,
                publisherId: item.userId,
                applicantId: this.userInfo.id
              }
            }).then(resp => {
              uni.hideLoading();
              uni.showToast({ title: '申请已发送！', icon: 'success' });
            }).catch(err => uni.hideLoading());
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

.sticky-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(247, 247, 247, 0.85);
  backdrop-filter: blur(20px);
  padding: 12px 20px 20px 20px;
}
.search-bar {
  background-color: #EFEFF0;
  height: 40px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border: 1px solid rgba(0,0,0,0.02);
}
.search-icon {
  margin-right: 8px;
  font-size: 16px;
  color: #8E8E93;
}
.search-placeholder {
  color: #8E8E93;
  font-size: 15px;
}

.tag-filters {
  display: flex;
  padding: 0 20px 20px 20px;
  overflow-x: auto;
}
.tag-filters::-webkit-scrollbar {
  display: none;
}
.tag {
  background-color: #FFFFFF;
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #8E8E93;
  margin-right: 12px;
  white-space: nowrap;
  border: 1px solid rgba(0,0,0,0.06);
  transition: all 0.2s;
}
.tag.active {
  background-color: #111111;
  color: #FFFFFF;
  border-color: #111111;
}

.feed-scroll {
  flex: 1;
}

/* 极简卡片 */
.post-card {
  background-color: #FFFFFF;
  border-radius: 20px;
  margin: 0 20px 20px 20px;
  padding: 22px 20px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.015);
  border: 1px solid rgba(0,0,0,0.03);
}
.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background-color: #F7F7F7;
  margin-right: 12px;
  border: 1px solid rgba(0,0,0,0.04);
}
.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.nickname {
  font-size: 15px;
  font-weight: 600;
  color: #111111;
}
.time {
  font-size: 12px;
  color: #8E8E93;
  margin-top: 2px;
}

/* 取消彩色大渐变，改为高级黑白灰的排版 */
.card-title-minimal {
  margin-bottom: 8px;
}
.hero-text-minimal {
  font-size: 18px;
  font-weight: 600;
  color: #111111;
  line-height: 1.4;
}
.card-desc-minimal {
  margin-bottom: 16px;
}
.desc-text-minimal {
  font-size: 14px;
  color: #8E8E93;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(0,0,0,0.04);
  padding-top: 14px;
}
.tags-group {
  display: flex;
}
.hashtag {
  font-size: 13px;
  font-weight: 500;
  color: #5B8DEF; /* 极简雾蓝点缀 */
  margin-right: 8px;
  background-color: #F4F8FF;
  padding: 4px 8px;
  border-radius: 6px;
}
.action-btn {
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
.action-btn:active {
  background-color: #2B2B2B;
}
/* 自己发布的技能 — 灰色不可点击 */
.own-skill-btn {
  background-color: #E5E5EA !important;
  color: #AEAEB2 !important;
}

.bottom-filler {
  text-align: center;
  color: #8E8E93;
  font-size: 13px;
  padding: 20px 0 100px 0;
}
</style>
