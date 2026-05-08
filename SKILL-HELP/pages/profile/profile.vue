<template>
  <view class="page-container">
    <!-- 极简高阶档案卡 -->
    <view class="profile-header" @click="navTo('/pages/profile/edit')">
      <image class="avatar-large" :src="userInfo.avatarUrl || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'" mode="aspectFill"></image>
      <view class="user-info-stack">
        <text class="nickname-bold">{{ userInfo.nickname || '未命名用户' }}</text>
        
        <view class="badges-row">
          <!-- 极简态信用标签 (点缀极简冷绿) -->
          <view class="pill-badge">
            <view class="dot-indicator green-dot"></view>
            <text>信用分 {{ userInfo.creditScore || '50' }}</text>
          </view>
          
          <!-- 极简态认证标签 (点缀雾蓝) -->
          <view class="pill-badge" v-if="userInfo.authStatus === 2">
            <view class="dot-indicator blue-dot"></view>
            <text>已认证</text>
          </view>
          <view class="pill-badge" v-else>
            <view class="dot-indicator gray-dot"></view>
            <text>{{ getAuthText(userInfo.authStatus) }}</text>
          </view>
        </view>
        
        <view class="edit-hint-wrap">
          <text class="edit-hint">编辑个人资料</text>
          <image class="arrow-tiny" :src="icons.chevronRight"></image>
        </view>
      </view>
    </view>

    <!-- 黑白灰流转菜单 1 -->
    <view class="menu-card">
      <view class="menu-row" @click="navTo('/pages/profile/my-skills')">
        <view class="icon-box">
          <image class="fluent-icon" :src="icons.briefcase"></image>
        </view>
        <text class="menu-label">我发布的技能</text>
        <image class="arrow-right" :src="icons.chevronRight"></image>
      </view>
      
      <view class="menu-divider"></view>
      
      <view class="menu-row" @click="navTo('/pages/profile/my-demands')">
        <view class="icon-box">
          <image class="fluent-icon" :src="icons.clipboardList"></image>
        </view>
        <text class="menu-label">我的求助订单</text>
        <image class="arrow-right" :src="icons.chevronRight"></image>
      </view>
      
      <view class="menu-divider"></view>

      <view class="menu-row" @click="viewMyProfile">
        <view class="icon-box">
          <image class="fluent-icon" :src="icons.star"></image>
        </view>
        <text class="menu-label">我收到的评价</text>
        <image class="arrow-right" :src="icons.chevronRight"></image>
      </view>
      
      <view class="menu-divider"></view>

      <view class="menu-row" @click="navTo('/pages/profile/favorites')">
        <view class="icon-box">
          <image class="fluent-icon" :src="icons.bookmark"></image>
        </view>
        <text class="menu-label">我的收藏</text>
        <image class="arrow-right" :src="icons.chevronRight"></image>
      </view>
    </view>

    <!-- 黑白灰流转菜单 2 -->
    <view class="menu-card mt-16">
      <view class="menu-row" @click="navToAuth">
        <view class="icon-box">
          <image class="fluent-icon" :src="icons.shieldCheck"></image>
        </view>
        <text class="menu-label">校园身份认证</text>
        <view class="status-wrap" v-if="userInfo.authStatus === 2">
          <text class="status-text blue-text">已认证</text>
        </view>
        <image class="arrow-right" :src="icons.chevronRight"></image>
      </view>
      
      <view class="menu-divider"></view>

      <view class="menu-row" @click="navTo('/pages/profile/appeal')">
        <view class="icon-box">
          <image class="fluent-icon" :src="icons.scale"></image>
        </view>
        <text class="menu-label">信用申诉</text>
        <image class="arrow-right" :src="icons.chevronRight"></image>
      </view>
    </view>
    
    <!-- 克制高级退出按钮 -->
    <view class="logout-wrapper">
      <button class="logout-btn-minimal" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request'

export default {
  data() {
    return {
      userInfo: {},
      // 严格要求：纯 Base64 编码 SVG，解决微信小程序真机渲染占位符/空白块的问题
      icons: {
        briefcase: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMkIyQjJCIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PHJlY3QgeD0iMiIgeT0iNyIgd2lkdGg9IjIwIiBoZWlnaHQ9IjE0IiByeD0iMiIgcnk9IjIiPjwvcmVjdD48cGF0aCBkPSJNMTYgMjFWNWEyIDIgMCAwIDAtMi0yaC00YTIgMiAwIDAgMC0yIDJ2MTYiPjwvcGF0aD48L3N2Zz4=',
        clipboardList: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMkIyQjJCIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PHJlY3QgeD0iOCIgeT0iMiIgd2lkdGg9IjgiIGhlaWdodD0iNCIgcng9IjEiIHJ5PSIxIj48L3JlY3Q+PHBhdGggZD0iTTE2IDRoMmEyIDIgMCAwIDEgMiAydjE0YTIgMiAwIDAgMS0yIDJINmEyIDIgMCAwIDEtMi0yVjZhMiAyIDAgMCAxIDItMmgyIj48L3BhdGg+PHBhdGggZD0iTTEyIDExaDQiPjwvcGF0aD48cGF0aCBkPSJNMTIgMTZoNCI+PC9wYXRoPjxwYXRoIGQ9Ik04IDExaC4wMSI+PC9wYXRoPjxwYXRoIGQ9Ik04IDE2aC4wMSI+PC9wYXRoPjwvc3ZnPg==',
        star: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMkIyQjJCIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PHBvbHlnb24gcG9pbnRzPSIxMiAyIDE1LjA5IDguMjYgMjIgOS4yNyAxNyAxNC4xNCAxOC4xOCAyMS4wMiAxMiAxNy43NyA1LjgyIDIxLjAyIDcgMTQuMTQgMiA5LjI3IDguOTEgOC4yNiAxMiAyIj48L3BvbHlnb24+PC9zdmc+',
        bookmark: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMkIyQjJCIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PHBhdGggZD0iTTE5IDIxbC03LTUtNyA1VjVhMiAyIDAgMCAxIDItMmgxMGEyIDIgMCAwIDEgMiAyeiI+PC9wYXRoPjwvc3ZnPg==',
        shieldCheck: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMkIyQjJCIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PHBhdGggZD0iTTEyIDIyczgtNCA4LTEwVjVsLTgtMy04IDN2N2MwIDYgOCAxMCA4IDEwIj48L3BhdGg+PHBhdGggZD0iTTkgMTJsMiAyIDQtNCI+PC9wYXRoPjwvc3ZnPg==',
        scale: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjMkIyQjJCIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PHBhdGggZD0iTTE2IDE2bDMtOCAzIDhjLS44Ny42NS0xLjkyIDEtMyAxcy0yLjEzLS4zNS0zLTFaIj48L3BhdGg+PHBhdGggZD0iTTIgMTZsMy04IDMgOGMtLjg3LjY1LTEuOTIgMS0zIDFzLTIuMTMtLjM1LTMtMVoiPjwvcGF0aD48cGF0aCBkPSJNNyAyMWgxMCI+PC9wYXRoPjxwYXRoIGQ9Ik0xMiAzdjE4Ij48L3BhdGg+PHBhdGggZD0iTTMgN2gyYzIgMCA1LTEgNy0yIDIgMSA1IDIgNyAyaDIiPjwvcGF0aD48L3N2Zz4=',
        chevronRight: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjQzdDN0NDIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PHBhdGggZD0ibTkgMTggNi02LTYtNiI+PC9wYXRoPjwvc3ZnPg=='
      }
    }
  },
  onShow() {
    const user = uni.getStorageSync('userInfo')
    if (user && user.id) {
       // 首次加载或 userInfo 为空时才从缓存赋值，避免每次 onShow 重置导致闪烁
       if (!this.userInfo || !this.userInfo.id) {
         this.userInfo = user
       }
       request({ url: '/api/user/info?userId=' + user.id }).then(res => {
          if (res) {
             this.userInfo = res;
             uni.setStorageSync('userInfo', res);
          }
       });
    }
    const page = getCurrentPages()[getCurrentPages().length - 1];
    if (page && typeof page.getTabBar === 'function' && page.getTabBar()) {
      page.getTabBar().setData({ selected: 4 });
    }
  },
  onTabItemTap() {
    uni.vibrateShort({ type: 'light' });
  },
  methods: {
    getAuthText(status) {
      if (status === 0 || status == null) return "未认证学号"
      if (status === 1) return "审核中"
      if (status === 2) return "已认证"
      return "非正常状态"
    },
    navTo(url) {
      if (!this.userInfo || !this.userInfo.id) {
        return uni.showToast({ title: '请先登录！', icon: 'none' });
      }
      uni.navigateTo({ url });
    },
    viewMyProfile() {
      if (!this.userInfo || !this.userInfo.id) {
        return uni.showToast({ title: '请先登录！', icon: 'none' });
      }
      uni.navigateTo({ url: `/pages/profile/user-profile?userId=${this.userInfo.id}` });
    },
    navToAuth() {
      if (this.userInfo && this.userInfo.authStatus === 2) {
        return uni.showToast({ title: '亲，您的身份已认证通过', icon: 'success' });
      }
      if (this.userInfo && this.userInfo.authStatus === 1) {
        return uni.showToast({ title: '正在审核中，请耐心等待...', icon: 'none' });
      }
      this.navTo('/pages/profile/auth');
    },
    handleLogout() {
      uni.showModal({
        title: '退出确认',
        content: '确定要安全注销当前账号吗？',
        confirmColor: '#111111',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('userInfo');
            uni.reLaunch({ url: '/pages/index/index' });
          }
        }
      });
    }
  }
}
</script>

<style>
/* 严格极简：冷基调背景色 */
page {
  background-color: #F7F7F7;
}

.page-container {
  padding: 100px 20px 40px 20px;
  min-height: 100vh;
  box-sizing: border-box;
}

/* 顶部信息卡 */
.profile-header {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  border-radius: 20px;
  padding: 24px 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02); /* 弱化阴影，保持干净 */
  margin-bottom: 20px;
  transition: transform 0.15s ease;
}
.profile-header:active {
  transform: scale(0.98);
}

.avatar-large {
  width: 72px;
  height: 72px;
  border-radius: 36px;
  background-color: #F7F7F7;
  margin-right: 20px;
  flex-shrink: 0;
  border: 1px solid rgba(0,0,0,0.04);
}

.user-info-stack {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.nickname-bold {
  font-size: 22px;
  font-weight: 600;
  color: #111111;
  margin-bottom: 10px;
  line-height: 1.2;
}

/* 绝对克制的 Badge 系统 */
.badges-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px; 
  margin-bottom: 8px;
}

.pill-badge {
  display: flex;
  align-items: center;
  background-color: #F7F7F7;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
  color: #111111;
  height: 24px;
  box-sizing: border-box;
  border: 1px solid rgba(0,0,0,0.03);
}

.dot-indicator {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  margin-right: 6px;
}
.green-dot { background-color: #34C759; } /* 强调色 A: 极简绿 */
.blue-dot { background-color: #5B8DEF; } /* 强调色 B: 雾蓝 */
.gray-dot { background-color: #C7C7CC; }

/* 编辑提示 */
.edit-hint-wrap {
  display: flex;
  align-items: center;
}
.edit-hint {
  font-size: 13px;
  color: #8E8E93;
}
.arrow-tiny {
  width: 12px;
  height: 12px;
  opacity: 0.6;
  margin-left: 2px;
}

/* 黑白灰极简卡片 */
.menu-card {
  background-color: #FFFFFF;
  border-radius: 18px;
  overflow: hidden; 
  box-shadow: 0 4px 20px rgba(0,0,0,0.015);
}

.mt-16 {
  margin-top: 16px;
}

.menu-row {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  transition: background-color 0.15s ease;
}
.menu-row:active {
  background-color: #F7F7F7; /* 纯净按压态 */
}

/* 克制容器 (去除彩色) */
.icon-box {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background-color: #F7F7F7; /* 极浅统一灰底 */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
}

.fluent-icon {
  width: 20px;
  height: 20px;
}

.menu-label {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: #111111;
}

.status-wrap {
  margin-right: 8px;
}
.status-text {
  font-size: 13px;
  font-weight: 500;
}
.blue-text {
  color: #5B8DEF;
}

.arrow-right {
  width: 18px;
  height: 18px;
  opacity: 0.6;
}

/* 细腻的分割线 */
.menu-divider {
  margin-left: 66px; /* 20 + 32 + 14 = 66 */
  height: 1px;
  background-color: rgba(0,0,0,0.04);
}

/* 退出登录 */
.logout-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.logout-btn-minimal {
  background-color: #FFFFFF;
  color: #111111; /* 黑白灰极简本质 */
  font-size: 15px;
  font-weight: 500;
  border-radius: 18px;
  width: 100%;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.015);
  transition: transform 0.15s ease, background-color 0.15s ease;
}
.logout-btn-minimal::after {
  border: none;
}
.logout-btn-minimal:active {
  background-color: #F7F7F7;
  color: #E75D5F; /* 按下时泛出克制的警示色 */
  transform: scale(0.98);
}
</style>
