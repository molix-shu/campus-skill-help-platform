<template>
  <view class="container">
    <!-- 用户信息卡片 -->
    <view class="hero-card">
      <image class="avatar" :src="user.avatarUrl || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
      <view class="user-info">
        <text class="nickname">{{ user.nickname || '微信用户' }}</text>
        <text class="college">{{ user.college || '' }}{{ user.major ? ' · ' + user.major : '' }}</text>
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-num">{{ stats.completedCount }}</text>
            <text class="stat-label">完成互助</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-num">{{ stats.avgScore || '--' }}</text>
            <text class="stat-label">综合评分</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-num credit">{{ user.creditScore !== undefined ? user.creditScore : 0 }}</text>
            <text class="stat-label">信用分</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 评价列表 -->
    <view class="section-title">收到的评价 ({{ evalList.length }})</view>

    <view v-if="evalList.length === 0" class="empty-tip">
      <text>暂无评价记录</text>
    </view>

    <view class="eval-card" v-for="ev in evalList" :key="ev.id">
      <view class="eval-header">
        <image class="eval-avatar" :src="ev.fromUserAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
        <view class="eval-meta">
          <text class="eval-name">{{ ev.fromUserNickname || '匿名同学' }}</text>
          <text class="eval-time">{{ formatTime(ev.createTime) }}</text>
        </view>
        <view class="stars-small">
          <image 
            v-for="i in 5" :key="i" 
            class="s-svg" 
            :src="i <= ev.score ? icons.starFilled : icons.starEmpty"
          ></image>
        </view>
      </view>

      <!-- 三维评分小标签 -->
      <view class="dim-scores">
        <view class="dim-tag"><image class="dim-icon-svg" :src="icons.bolt"></image>{{ ev.scoreSpeed || ev.score }}分</view>
        <view class="dim-tag"><image class="dim-icon-svg" :src="icons.smile"></image>{{ ev.scoreAttitude || ev.score }}分</view>
        <view class="dim-tag"><image class="dim-icon-svg" :src="icons.target"></image>{{ ev.scoreQuality || ev.score }}分</view>
      </view>

      <!-- 追加：关联帮助信息展示 -->
      <view class="related-task" v-if="ev.targetTitle" @click="goToOrder(ev.orderId)">
        <image class="task-icon-svg" :src="icons.bookmark"></image>
        <text class="task-title">互助项目：{{ ev.targetTitle }}</text>
        <text class="task-arrow">进入凭证 ›</text>
      </view>

      <text class="eval-content">{{ ev.content }}</text>

      <image v-if="ev.imageUrl" class="eval-img" :src="ev.imageUrl" mode="aspectFill" @click="previewImg(ev.imageUrl)"></image>
    </view>
  </view>
</template>

<script>
import { request, BASE_URL } from '../../utils/request.js';

export default {
  data() {
    return {
      userId: null,
      user: {},
      evalList: [],
      stats: { completedCount: 0, avgScore: null },
      isMyProfile: false,
      icons: {
        starFilled: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEyIDE3LjI3TDE4LjE4IDIxTDE2LjU0IDEzLjk3TDIyIDkuMjRMMTQuODEgOC42M0wxMiAyTDkuMTkgOC42M0wyIDkuMjRMNy40NiAxMy45N0w1LjgyIDIxTDEyIDE3LjI3WiIgZmlsbD0iI0ZGOTUwMCIgc3Ryb2tlPSIjRkY5NTAwIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lam9pbj0icm91bmQiLz48L3N2Zz4=',
        starEmpty: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEyIDE3LjI3TDE4LjE4IDIxTDE2LjU0IDEzLjk3TDIyIDkuMjRMMTQuODEgOC42M0wxMiAyTDkuMTkgOC42M0wyIDkuMjRMNy40NiAxMy45N0w1LjgyIDIxTDEyIDE3LjI3WiIgc3Ryb2tlPSIjRTNFM0U4IiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lam9pbj0icm91bmQiLz48L3N2Zz4=',
        bolt: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEzIDJMMyAxNkgxMkwxMSAyMkwyMSA4SDEyTDEzIDJaIiBzdHJva2U9IiM2MzYzNjYiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIvPjwvc3ZnPg==',
        smile: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTAiIHN0cm9rZT0iIzYzNjM2NiIgc3Ryb2tlLXdpZHRoPSIyIi8+PHBhdGggZD0iTTggMTRTOSAxNiAxMiAxNlMxNiAxNCAxNiAxNCIgc3Ryb2tlPSIjNjM2MzY2IiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPjxwYXRoIGQ9Ik05IDlIOXYuMDEiIHN0cm9rZT0iIzYzNjM2NiIgc3Ryb2tlLXdpZHRoPSIzIiBzdHJva2UtbGluZWNhcD0icm91bmQiLz48cGF0aCBkPSJNMTUgOUgxNXYuMDEiIHN0cm9rZT0iIzYzNjM2NiIgc3Ryb2tlLXdpZHRoPSIzIiBzdHJva2UtbGluZWNhcD0icm91bmQiLz48L3N2Zz4=',
        target: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTAiIHN0cm9rZT0iIzYzNjM2NiIgc3Ryb2tlLXdpZHRoPSIyIi8+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iNiIgc3Ryb2tlPSIjNjM2MzY2IiBzdHJva2Utd2lkdGg9IjIiLz48Y2lyY2xlIGN4PSIxMiIgY3k9IjEyIiByPSIyIiBmaWxsPSIjNjM2MzY2Ii8+PC9zdmc+',
        bookmark: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTE5IDVWMjFMMTIgMTcuM0w1IDIxVjVDNSAzLjg5NSA1Ljg5NSAzIDcgM0gxN0MxOC4xMDUgMyAxOSAzLjg5NSAxOSA1WiIgc3Ryb2tlPSIjNTg1NkQ2IiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lam9pbj0icm91bmQiLz48L3N2Zz4='
      }
    }
  },
  onLoad(options) {
    this.userId = Number(options.userId);

    // 如果是查看自己的主页，先用本地缓存立即显示
    const myInfo = uni.getStorageSync('userInfo');
    if (myInfo && String(myInfo.id) === String(this.userId)) {
      this.user = myInfo;
      this.isMyProfile = true;
    }

    // 并行请求：用户信息、订单统计、评价列表同时发出，不串行等待
    this.loadUserInfo();
    this.loadOrderStats();
    this.loadEvals();
  },
  methods: {
    /**
     * 直接加载目标用户信息
     */
    loadUserInfo() {
      uni.request({
        url: BASE_URL + '/api/user/info?userId=' + this.userId,
        method: 'GET',
        success: res => {
          if (res.data && res.data.code === 200 && res.data.data) {
            this.user = res.data.data;
          }
        },
        fail: () => {}
      });
    },

    /**
     * 从订单列表统计完成互助次数
     */
    loadOrderStats() {
      const myInfo = uni.getStorageSync('userInfo');
      if (!myInfo || !myInfo.id) return;

      request({ url: '/api/order/my?userId=' + myInfo.id, method: 'GET' })
        .then(res => {
          const relatedOrders = res.filter(o =>
            String(o.publisherId) === String(this.userId) ||
            String(o.applicantId) === String(this.userId)
          );
          this.stats.completedCount = relatedOrders.filter(o => o.status >= 2).length;
        })
        .catch(() => {});
    },

    /**
     * 加载评价列表
     */
    loadEvals() {
      uni.request({
        url: BASE_URL + '/api/eval/received?userId=' + this.userId,
        method: 'GET',
        success: res => {
          if (res.data && res.data.code === 200 && res.data.data) {
            this.evalList = res.data.data;
            if (this.evalList.length > 0) {
              const avg = this.evalList.reduce((s, e) => s + (e.score || 0), 0) / this.evalList.length;
              this.stats.avgScore = avg.toFixed(1);
            }
          }
        },
        fail: () => {}
      });
    },

    formatTime(time) {
      if (!time) return '';
      if (Array.isArray(time)) {
        const [y, m, d, h, min] = time;
        return `${y}-${String(m).padStart(2,'0')}-${String(d).padStart(2,'0')} ${String(h).padStart(2,'0')}:${String(min).padStart(2,'0')}`;
      }
      return String(time).replace('T', ' ').substring(0, 16);
    },
    previewImg(url) {
      uni.previewImage({ urls: [url] });
    },
    goToOrder(orderId) {
      if (!orderId) return;
      uni.navigateTo({ url: `/pages/message/order-detail?id=${orderId}` });
    }
  }
}
</script>

<style>
page { background-color: #F5F5F7; }
.container { padding: 20px 20px 40px 20px; }

/* 头部英雄卡片 */
.hero-card {
  background: linear-gradient(135deg, #1D1D1F 0%, #3C3C43 100%);
  border-radius: 24px;
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.15);
}
.avatar {
  width: 80px;
  height: 80px;
  border-radius: 40px;
  border: 3px solid rgba(255,255,255,0.3);
  margin-bottom: 14px;
  background-color: #636366;
}
.user-info { text-align: center; width: 100%; }
.nickname { font-size: 22px; font-weight: 700; color: #FFF; display: block; margin-bottom: 4px; }
.college { font-size: 13px; color: rgba(255,255,255,0.6); display: block; margin-bottom: 20px; }

.stats-row { display: flex; justify-content: center; align-items: center; }
.stat-item { text-align: center; padding: 0 20px; }
.stat-num { font-size: 22px; font-weight: 700; color: #FFF; display: block; }
.stat-num.credit { color: #4CD964; }
.stat-label { font-size: 11px; color: rgba(255,255,255,0.5); display: block; margin-top: 2px; }
.stat-divider { width: 1px; height: 32px; background: rgba(255,255,255,0.15); }

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1D1D1F;
  margin-bottom: 16px;
}

.empty-tip {
  text-align: center;
  color: #AEAEB2;
  font-size: 14px;
  padding: 40px 0;
}

/* 评价卡片 */
.eval-card {
  background: #FFF;
  border-radius: 20px;
  padding: 18px;
  margin-bottom: 14px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
}
.eval-header { display: flex; align-items: center; margin-bottom: 12px; }
.eval-avatar { width: 38px; height: 38px; border-radius: 19px; background-color: #E3E3E8; margin-right: 12px; flex-shrink: 0; }
.eval-meta { flex: 1; }
.eval-name { font-size: 14px; font-weight: 600; color: #1D1D1F; display: block; }
.eval-time { font-size: 11px; color: #AEAEB2; display: block; margin-top: 2px; }
.stars-small { display: flex; align-items: center; }
.s-svg { width: 14px; height: 14px; margin-left: 2px; }

.dim-scores { display: flex; margin-bottom: 10px; }
.dim-tag {
  font-size: 11px;
  color: #636366;
  background: #F5F5F7;
  border-radius: 10px;
  padding: 3px 10px;
  margin-right: 8px;
  display: flex; align-items: center;
}
.dim-icon-svg { width: 12px; height: 12px; margin-right: 4px; }

.related-task {
  display: flex;
  align-items: center;
  background: #F8F8FC;
  padding: 10px 14px;
  border-radius: 12px;
  margin-bottom: 12px;
}
.task-icon-svg { width: 14px; height: 14px; margin-right: 6px; }
.task-title { font-size: 13px; color: #5856D6; flex: 1; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.task-arrow { font-size: 11px; color: #8E8E93; }

.eval-content { font-size: 14px; color: #3C3C43; line-height: 1.6; display: block; }
.eval-img { width: 100%; height: 160px; border-radius: 12px; margin-top: 12px; }
</style>
