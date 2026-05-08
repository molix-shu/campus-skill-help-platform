<template>
  <view class="container">
    <view class="header-section">
      <text class="title">消息与订单</text>
      <text class="subtitle">跟踪互助流程</text>
    </view>
    
    <scroll-view scroll-x class="status-tabs" show-scrollbar="false">
      <view class="tabs-inner">
        <text class="tab-item" v-for="t in tabs" :key="t.id" :class="{active: currentTab===t.id}" @click="switchTab(t.id)">{{ t.name }}</text>
      </view>
    </scroll-view>
    
    <view class="list-container" v-if="filteredOrderList.length > 0">
      <view class="order-card" v-for="item in filteredOrderList" :key="item.id" @click="goToDetail(item.id)">
        <view class="order-header">
          <text class="order-no">订单号: {{ item.orderNo }}</text>
          <!-- 0待同意，1进行中，2待评价，3已关闭 -->
          <text class="status-badge" :class="'status-' + item.status">
            {{ formatStatusText(item) }}
          </text>
        </view>
        <view class="order-body">
          <text class="order-type">{{ item.type === 1 ? '申请的技能' : '接下的求助' }}: {{ item.targetTitle || '已折叠内容' }}</text>
          <view class="user-info-row" @click.stop="viewUserProfile(item)">
            <image class="avatar-sm" :src="item.targetUserAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
            <view class="user-info-text">
              <text class="target-name">对方同学: {{ item.targetUserName || '微信用户' }}</text>
              <view class="target-college" v-if="item.targetUserCollege">{{ item.targetUserCollege }}</view>
              <text style="font-size: 11px; color: #007AFF;">查看主页 ›</text>
            </view>
          </view>
        </view>
        <view class="order-footer">
          <text class="time">{{ formatTime(item.createTime) }}</text>
          <button class="action-btn" v-if="item.status === 0 && String(item.publisherId) !== String(currentUserId)">待对方同意</button>
          <button class="action-btn primary" v-if="item.status === 0 && String(item.publisherId) === String(currentUserId)" @click.stop="handleConfirm(item)">同意互助</button>
          <!-- 只有订单的接单方(帮手)才能点确认完成 -->
          <view v-if="item.status === 1">
            <button class="action-btn" style="margin-right:8px; display:inline-block" @click.stop="handleCommunicate(item)">去沟通</button>
            <button class="action-btn primary" style="display:inline-block"
              v-if="String(item.applicantId) === String(currentUserId)"
              @click.stop="handleFinish(item)">确认完成</button>
            <button class="action-btn" style="display:inline-block"
              v-else>等对方确认</button>
          </view>
          <!-- 只有求助方(发布方)才能评价接单方 -->
          <button class="action-btn primary"
            v-if="item.status === 2 && String(item.publisherId) === String(currentUserId)"
            @click.stop="handleEvaluate(item)">去评价</button>
          <button class="action-btn"
            v-if="item.status === 2 && String(item.applicantId) === String(currentUserId)">等待对方评价</button>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂时没有收到新消息或订单</text>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      orderList: [],
      filteredOrderList: [],
      currentTab: -1,
      tabs: [
        { id: -1, name: '全部' },
        { id: 0, name: '待处理' },
        { id: 1, name: '进行中' },
        { id: 2, name: '已完成' },
        { id: 4, name: '申诉中' }
      ],
      currentUserId: null
    }
  },
  onShow() {
    this.fetchOrders();
    const page = getCurrentPages()[getCurrentPages().length - 1];
    if (page && typeof page.getTabBar === 'function' && page.getTabBar()) {
      page.getTabBar().setData({ selected: 3 });
    }
  },
  onPullDownRefresh() {
    this.fetchOrders();
    setTimeout(() => {
      uni.stopPullDownRefresh();
    }, 1000);
  },
  onTabItemTap() {
    // 触发轻微震动反馈
    uni.vibrateShort({ type: 'light' });
  },
  methods: {
    fetchOrders() {
      const user = uni.getStorageSync('userInfo');
      if (!user || !user.id) return;
      this.currentUserId = user.id;
      
      request({
        url: '/api/order/my?userId=' + user.id,
        method: 'GET'
      }).then(res => {
        this.orderList = res;
        this.applyFilter();

        // --- 核心更新：计算未读/待办的数字并挂载红点 ---
        let unreadCount = 0;
        res.forEach(o => {
          // 我需要处理订单或进行中：
          // 1. 我是被申请方(publisher)，有人接单了待我确认 (status=0)
          // 2. 状态处于 1 (沟通互助中)
          if (o.status === 0 && o.publisherId === this.currentUserId) unreadCount++;
          else if (o.status === 1) unreadCount++;
        });
        
        if (unreadCount > 0) {
          uni.setTabBarBadge({ index: 2, text: String(unreadCount) });
        } else {
          uni.removeTabBarBadge({ index: 2 }).catch(e => e);
        }

      }).catch(err => {
        console.error("加载订单失败", err);
      });
    },
    switchTab(tabId) {
      if (this.currentTab === tabId) return;
      this.currentTab = tabId;
      this.applyFilter();
    },
    applyFilter() {
      if (this.currentTab === -1) {
        this.filteredOrderList = this.orderList;
      } else if (this.currentTab === 2) {
        // 已完成包含 status 2/3
        this.filteredOrderList = this.orderList.filter(o => o.status === 2 || o.status === 3);
      } else {
        this.filteredOrderList = this.orderList.filter(o => o.status === this.currentTab);
      }
    },
    goToDetail(id) {
      uni.navigateTo({ url: `/pages/message/order-detail?id=${id}` });
    },
    handleConfirm(item) {
      uni.showModal({
        title: '确认互助',
        content: `确定同意【${item.targetUserName}】的互助请求吗？`,
        confirmColor: '#007AFF',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中' });
            request({
              url: `/api/order/confirm?orderId=${item.id}&userId=${this.currentUserId}`,
              method: 'POST'
            }).then(resp => {
              uni.hideLoading();
              uni.showToast({ title: '已同意互助！', icon: 'success' });
              this.fetchOrders(); // 刷新列表，状态变更
            }).catch(err => uni.hideLoading());
          }
        }
      });
    },
    handleFinish(item) {
      uni.showModal({
        title: '完成确认',
        content: `确认互助已经圆满完成了吗？`,
        confirmColor: '#007AFF',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading();
            request({
              url: `/api/order/finish?orderId=${item.id}&userId=${this.currentUserId}`,
              method: 'POST'
            }).then(() => {
              uni.hideLoading();
              uni.showToast({ title: '互助完成！', icon: 'success' });
              this.fetchOrders();
            }).catch(() => uni.hideLoading());
          }
        }
      });
    },
    handleCommunicate(item) {
      uni.navigateTo({
        url: `/pages/message/chat?orderId=${item.id}&targetName=${encodeURIComponent(item.targetUserName || '同学')}&targetAvatar=${encodeURIComponent(item.targetUserAvatar || '')}`
      });
    },
    handleEvaluate(item) {
      uni.navigateTo({
        url: `/pages/publish/eval?orderId=${item.id}&toUserId=${item.publisherId === this.currentUserId ? item.applicantId : item.publisherId}`
      });
    },
    viewUserProfile(item) {
      // 用 String() 将两者都转为字符串再比较，避免数字 vs 字符串的类型误判
      const targetId = String(item.publisherId) === String(this.currentUserId)
        ? item.applicantId
        : item.publisherId;
      if (!targetId) return uni.showToast({ title: '无法获取用户信息', icon: 'none' });
      uni.navigateTo({ url: `/pages/profile/user-profile?userId=${targetId}` });
    },
    formatTime(t) {
      if (!t) return '刚刚下单';
      // 把 ISO 格式的 T 替换为空格，并去掉秒数
      return String(t).replace('T', ' ').substring(0, 16);
    },
    formatStatusText(item) {
      if (item.status === 0) {
        return String(item.publisherId) === String(this.currentUserId) ? '待确认' : '待对方确认';
      }
      return ['--', '进行中', '待评价', '已完成'][item.status] || '未知状态';
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
.header-section {
  padding: 24px 20px 10px 20px;
  box-sizing: border-box;
  width: 100%;
}
.title {
  font-size: 32px;
  font-weight: 700;
  color: #1D1D1F;
}
.subtitle {
  font-size: 15px;
  color: #8E8E93;
  margin-top: 8px;
  display: block;
}

.status-tabs {
  width: 100%;
  white-space: nowrap;
  margin-bottom: 6px;
}
.tabs-inner {
  display: inline-flex;
  padding: 0 20px 10px 20px;
}
.tab-item {
  font-size: 15px;
  font-weight: 500;
  color: #8E8E93;
  margin-right: 24px;
  position: relative;
  padding-bottom: 6px;
  transition: all 0.2s;
}
.tab-item.active {
  font-size: 16px;
  font-weight: 700;
  color: #1D1D1F;
}
.tab-item.active::after {
  content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%);
  width: 16px; height: 3px; background-color: #007AFF; border-radius: 2px;
}

.list-container {
  flex: 1;
  padding: 10px 20px;
  box-sizing: border-box;
  width: 100%;
}
.order-card {
  background-color: #FFF;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
  box-sizing: border-box;
  width: 100%;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #E3E3E8;
}
.order-no {
  font-size: 13px;
  color: #8E8E93;
}
.status-badge {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
}
/* 状态颜色规划 */
.status-0 { background-color: #FFF0E5; color: #FF9500; } /* 待确认-橙色 */
.status-1 { background-color: #E5F6EB; color: #00A650; } /* 进行中-绿色 */
.status-2 { background-color: #E2E2E8; color: #1D1D1F; } /* 待评价-黑色 */
.status-3 { background-color: #F5F5F7; color: #C7C7CC; } /* 已完成-灰色 */

.order-body {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}
.order-type {
  font-size: 18px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8px;
}
.user-info-row {
  display: flex;
  align-items: center;
  background-color: #F8F8FA; /* 给联系人卡片加个极淡的微底色 */
  padding: 10px;
  border-radius: 12px;
}
.avatar-sm {
  width: 44px;
  height: 44px;
  border-radius: 22px;
  background-color: #E3E3E8;
  margin-right: 12px;
}
.user-info-text {
  display: flex;
  flex-direction: column;
}
.target-name {
  font-size: 14px;
  color: #1D1D1F;
  font-weight: 600;
}
.target-college {
  font-size: 12px;
  color: #8E8E93;
  margin-top: 4px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time {
  font-size: 12px;
  color: #AEAEB2;
}
.action-btn {
  font-size: 13px;
  font-weight: 600;
  border-radius: 14px;
  padding: 0 16px;
  height: 28px;
  line-height: 28px;
  margin: 0;
  background-color: #E3E3E8;
  color: #1D1D1F;
}
.action-btn.primary {
  background-color: #007AFF;
  color: #FFF;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-top: -100px;
}
.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}
.empty-text {
  font-size: 16px;
  color: #8E8E93;
}
</style>
