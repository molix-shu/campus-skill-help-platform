<template>
  <view class="container">
    <view class="list-container">
      <view class="post-card-minimal" v-for="item in favoritesList" :key="item.id + '-' + item.type">
        <view class="card-hero-minimal" @click="goToDetail(item)">
          <text class="hero-text-minimal">{{ item.title }}</text>
        </view>
        
        <view class="card-footer-minimal">
          <view class="footer-top">
            <view class="tags-time-wrap">
              <text class="hashtag-minimal" v-if="item.tags">{{ formatTags(item.tags) }}</text>
              <text class="time-text">{{ formatTime(item.createTime) }}发布</text>
            </view>
            <text class="type-badge-minimal">{{ item.type === 'skill' ? '技能' : '求助' }}</text>
          </view>
          
          <view class="operations-wrap">
            <button class="action-btn-minimal bg-dark" @click="goToDetail(item)">查看详情</button>
            <button class="action-btn-minimal outline-dark" @click="unfavorite(item)">取消收藏</button>
          </view>
        </view>
      </view>

      <view v-if="favoritesList.length === 0" class="empty-state">
        <text class="empty-text">你还没有收藏任何内容哦~</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      favoritesList: []
    }
  },
  onShow() {
    this.fetchFavorites();
  },
  onPullDownRefresh() {
    this.fetchFavorites();
    setTimeout(() => {
      uni.stopPullDownRefresh();
    }, 800);
  },
  methods: {
    fetchFavorites() {
      const skills = uni.getStorageSync('my_favorites') || [];
      const demands = uni.getStorageSync('my_favorites_demand') || [];
      
      const skillsMapped = skills.map(s => ({ ...s, type: 'skill' }));
      const demandsMapped = demands.map(d => ({ ...d, type: 'demand' }));
      
      this.favoritesList = [...skillsMapped, ...demandsMapped].sort((a, b) => {
        const timeA = new Date(Array.isArray(a.createTime) ? a.createTime.join('/') : a.createTime).getTime();
        const timeB = new Date(Array.isArray(b.createTime) ? b.createTime.join('/') : b.createTime).getTime();
        return timeB - timeA;
      });
    },
    unfavorite(item) {
      if (item.type === 'skill') {
        let favs = uni.getStorageSync('my_favorites') || [];
        favs = favs.filter(i => String(i.id) !== String(item.id));
        uni.setStorageSync('my_favorites', favs);
      } else {
        let favs = uni.getStorageSync('my_favorites_demand') || [];
        favs = favs.filter(i => String(i.id) !== String(item.id));
        uni.setStorageSync('my_favorites_demand', favs);
      }
      uni.showToast({ title: '已取消收藏', icon: 'none' });
      this.fetchFavorites();
    },
    goToDetail(item) {
      if (item.type === 'skill') {
        uni.navigateTo({ url: '/pages/home/skill-detail?id=' + item.id });
      } else {
        uni.navigateTo({ url: '/pages/demand/demand-detail?id=' + item.id });
      }
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
page { background-color: #F7F7F7; height: 100%; }
.container { display: flex; flex-direction: column; height: 100vh; }
.list-container { flex: 1; padding: 20px; box-sizing: border-box; width: 100%; }

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
.card-hero-minimal { margin-bottom: 16px; }
.hero-text-minimal { font-size: 20px; font-weight: 700; color: #111111; line-height: 1.4; display: block; }

.card-footer-minimal { display: flex; flex-direction: column; align-items: stretch; }
.footer-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.tags-time-wrap { display: flex; flex-direction: column; flex: 1; padding-right: 12px; }
.hashtag-minimal {
  font-size: 13px; font-weight: 500; color: #111111;
  background-color: #F7F7F7; padding: 4px 10px; border-radius: 6px;
  align-self: flex-start; margin-bottom: 8px; border: 1px solid rgba(0,0,0,0.04);
}
.time-text { font-size: 12px; color: #8E8E93; }
.type-badge-minimal {
  font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 6px; flex-shrink: 0;
  background-color: #111111; color: #FFFFFF;
}

.operations-wrap {
  display: flex; justify-content: flex-end; align-items: center;
  border-top: 1px solid rgba(0,0,0,0.04); padding-top: 16px;
}
.action-btn-minimal {
  font-size: 13px; font-weight: 500; border-radius: 16px; padding: 0 16px; height: 32px; line-height: 32px; margin: 0;
}
.action-btn-minimal::after { border: none; }
.bg-dark { background-color: #111111; color: #FFFFFF; margin-right: 12px; }
.bg-dark:active { background-color: #2B2B2B; }
.outline-dark { background-color: #FFFFFF; color: #111111; border: 1px solid rgba(0,0,0,0.1); }
.outline-dark:active { background-color: #F7F7F7; }

.empty-state { text-align: center; margin-top: 100px; }
.empty-text { color: #8E8E93; font-size: 14px; }
</style>
