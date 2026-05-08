<template>
  <view class="container" v-if="order">
    <scroll-view scroll-y class="content-scroll">
      
      <!-- 顶部状态视图跟随页面一起滚动 -->
      <view class="status-header">
        <view class="status-circle" :class="'bg-' + order.status">
          <text class="status-icon" v-if="order.status === 0">⏳</text>
          <text class="status-icon" v-if="order.status === 1">🤝</text>
          <text class="status-icon" v-if="order.status === 2">✒️</text>
          <text class="status-icon" v-if="order.status === 3">✅</text>
        </view>
        <text class="status-text">{{ statusText }}</text>
        <text class="status-desc">{{ statusDesc }}</text>
      </view>
    
      <!-- 对方信息卡片 -->
      <view class="card user-card" @click="viewUserProfile">
        <text class="card-label">互助对象</text>
        <view class="user-row">
          <image class="avatar" :src="targetUserAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
          <view class="user-info">
            <text class="nickname">{{ targetUserName || '微信用户' }}</text>
            <text class="college">{{ targetUserCollege || '未填写学院' }}</text>
          </view>
          <text class="arrow">›</text>
        </view>
      </view>

      <!-- 订单信息卡片 -->
      <view class="card info-card">
        <text class="card-label">订单信息</text>
        <view class="info-row">
          <text class="info-key">订单编号</text>
          <text class="info-val">{{ order.orderNo }}</text>
        </view>
        <view class="info-row">
          <text class="info-key">关联内容</text>
          <text class="info-val">{{ order.targetTitle || '已折叠内容' }}</text>
        </view>
        <view class="info-row">
          <text class="info-key">订单类型</text>
          <text class="info-val">{{ order.type === 1 ? '申请的技能' : '接下的求助' }}</text>
        </view>
        <view class="info-row">
          <text class="info-key">创建时间</text>
          <text class="info-val">{{ formatTime(order.createTime) }}</text>
        </view>
      </view>
      
      <!-- 防止拉到底部被悬浮条遮挡的安全高度 -->
      <view style="height: 120px;"></view>
    </scroll-view>

    <!-- 底部操作按钮 -->
    <view class="bottom-bar">
      <view class="bottom-bar-inner">
        <!-- 根据身份和状态渲染不同的按钮 -->
        <template v-if="order.status === 0">
          <button v-if="String(order.publisherId) === String(currentUserId)" class="action-btn primary" @click="handleConfirm">同意互助</button>
          <button v-else class="action-btn" disabled>等待对方同意</button>
        </template>

        <template v-else-if="order.status === 1">
          <button class="action-btn" @click="handleCommunicate">去沟通</button>
          <button v-if="String(order.applicantId) === String(currentUserId)" class="action-btn primary" @click="handleFinish">确认完成</button>
          <button v-else class="action-btn" disabled>等待对方确认完成</button>
        </template>

        <template v-else-if="order.status === 2">
          <button v-if="String(order.publisherId) === String(currentUserId)" class="action-btn primary" @click="handleEvaluate">去评价</button>
          <button v-else class="action-btn" disabled>等待对方评价</button>
        </template>

        <template v-else-if="order.status === 3">
          <button class="action-btn" disabled>已结束</button>
        </template>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      orderId: null,
      order: null,
      currentUserId: null,
      targetUserName: '',
      targetUserAvatar: '',
      targetUserCollege: ''
    }
  },
  computed: {
    statusText() {
      if (!this.order) return '';
      if (this.order.status === 0) {
        return String(this.order.publisherId) === String(this.currentUserId) ? '待确认' : '待对方确认';
      }
      return ['--', '进行中', '待评价', '已完成'][this.order.status] || '未知状态';
    },
    statusDesc() {
      if (!this.order) return '';
      if (this.order.status === 0) return '互助还在萌芽阶段，等待双方确认中...';
      if (this.order.status === 1) return '互助正式开始，请积极与对方沟通完成任务！';
      if (this.order.status === 2) return '任务已完成！客观评价有助于维持校园良好环境';
      if (this.order.status === 3) return '互助流转结束，信用分已结算';
      return '';
    }
  },
  onLoad(options) {
    if (options.id) {
      this.orderId = options.id;
    }
    const user = uni.getStorageSync('userInfo');
    if (user && user.id) this.currentUserId = user.id;
  },
  onShow() {
    if (this.orderId) this.fetchOrderDetail();
  },
  methods: {
    fetchOrderDetail() {
      // 当前没有 detail 接口，用 my 接口过滤 mock
      request({
        url: '/api/order/my?userId=' + this.currentUserId,
        method: 'GET'
      }).then(res => {
        const o = res.find(i => String(i.id) === String(this.orderId));
        if (o) {
          this.order = o;
          this.targetUserName = o.targetUserName;
          this.targetUserAvatar = o.targetUserAvatar;
          this.targetUserCollege = o.targetUserCollege;
        } else {
          uni.showToast({ title: '无权查看该互助订单或已删除', icon: 'none' });
        }
      });
    },
    viewUserProfile() {
      const targetId = String(this.order.publisherId) === String(this.currentUserId) ? this.order.applicantId : this.order.publisherId;
      uni.navigateTo({ url: `/pages/profile/user-profile?userId=${targetId}` });
    },
    handleConfirm() {
      uni.showLoading();
      request({
        url: `/api/order/confirm?orderId=${this.order.id}&userId=${this.currentUserId}`, method: 'POST'
      }).then(() => {
        uni.hideLoading(); this.fetchOrderDetail();
      });
    },
    handleFinish() {
      uni.showLoading();
      request({
        url: `/api/order/finish?orderId=${this.order.id}&userId=${this.currentUserId}`, method: 'POST'
      }).then(() => {
        uni.hideLoading(); this.fetchOrderDetail();
      });
    },
    handleCommunicate() {
      uni.navigateTo({
        url: `/pages/message/chat?orderId=${this.order.id}&targetName=${encodeURIComponent(this.targetUserName || '同学')}`
      });
    },
    handleEvaluate() {
      uni.navigateTo({
        url: `/pages/publish/eval?orderId=${this.order.id}&toUserId=${String(this.order.publisherId) === String(this.currentUserId) ? this.order.applicantId : this.order.publisherId}`
      });
    },
    formatTime(t) {
      if (!t) return '';
      return String(t).replace('T', ' ').substring(0, 16);
    }
  }
}
</script>

<style>
page { background-color: #F5F5F7; height: 100%; }
.container { display: flex; flex-direction: column; height: 100vh; position: relative; }
.content-scroll { flex: 1; }

.status-header {
  padding: 40px 20px 30px;
  background: #FFF;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-bottom-left-radius: 20px;
  border-bottom-right-radius: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.02);
}
.status-circle {
  width: 52px; height: 52px; border-radius: 26px;
  display: flex; justify-content: center; align-items: center;
  margin-bottom: 12px; font-size: 24px;
}
.bg-0 { background: #FFF0E5; } .bg-1 { background: #E5F6EB; } .bg-2 { background: #E3E3E8; } .bg-3 { background: #F5F5F7; }
.status-text { font-size: 20px; font-weight: 700; color: #1D1D1F; margin-bottom: 6px; }
.status-desc { font-size: 13px; color: #8E8E93; }

.card {
  background: #FFF; border-radius: 20px; padding: 20px; margin: 0 20px 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
}
.card-label { font-size: 16px; font-weight: 600; color: #1D1D1F; display: block; margin-bottom: 16px; }

.user-row { display: flex; align-items: center; }
.avatar { width: 44px; height: 44px; border-radius: 22px; margin-right: 12px; background: #E3E3E8;}
.user-info { flex: 1; display: flex; flex-direction: column; }
.nickname { font-size: 15px; font-weight: 600; color: #1D1D1F; }
.college { font-size: 12px; color: #8E8E93; margin-top: 2px;}
.arrow { color: #C7C7CC; font-size: 20px; }

.info-row { display: flex; justify-content: space-between; margin-bottom: 12px; }
.info-row:last-child { margin-bottom: 0; }
.info-key { font-size: 14px; color: #8E8E93; }
.info-val { font-size: 14px; color: #1D1D1F; font-weight: 500; }

/* 底部操作区重构：内外解耦，外侧背景延伸至安全区，内侧限定高度并居中操作按钮 */
.bottom-bar {
  position: fixed; 
  bottom: 0; left: 0; right: 0;
  background: #FFF;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -4px 16px rgba(0,0,0,0.04);
}
.bottom-bar-inner {
  display: flex; 
  align-items: center; 
  justify-content: center; 
  gap: 12px;
  padding: 12px 24px;
}
.action-btn { flex: 1; height: 48px; line-height: 48px; font-size: 16px; font-weight: 600; border-radius: 24px; background: #E3E3E8; color: #1D1D1F; margin: 0; }
.action-btn.primary { background: #007AFF; color: #FFF; }
.action-btn[disabled] { background: #F5F5F7; color: #C7C7CC; }
</style>
