<template>
  <view class="container">
    <!-- Apple 风格的大标题区域 -->
    <view class="header-section">
      <text class="title">欢迎使用</text>
      <text class="subtitle">南通大学 · 技能互助平台</text>
    </view>

    <!-- 现代风格首页插画 (已替换为真实的 3D/现代风格图片) -->
    <view class="illustration-box">
      <image class="illustration" src="../../static/hero.png?v=3" mode="aspectFill"></image>
    </view>

    <!-- 底部操作区域 (毛玻璃质感卡片) -->
    <view class="action-card">
      <text class="card-desc">发现校园里的隐藏技能与互助机会</text>
      
      <button class="apple-btn primary" @click="handleWechatLogin">
        <text>一键微信登录</text>
      </button>

      <view class="terms">
        <text class="terms-text">登录即代表你同意我们的《用户协议》和《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script>
// 引入刚刚写好的网络请求工具
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      isLoading: false
    }
  },
  onLoad() {
    const user = uni.getStorageSync('userInfo');
    if (user && user.id) {
      // 如果是个老用户且资料已经完整，静默直接进大厅
      if (user.avatarUrl && user.nickname && user.nickname !== '微信用户' && user.college && user.major) {
        uni.switchTab({ url: '/pages/home/home' });
      }
      // 如果不完整，就停留在这里，等待用户再次点击登录按钮
    }
  },
  methods: {
    checkAndRedirect(user) {
      // 检查必填的个人资料是否都有值且不是默认值
      if (!user.avatarUrl || !user.nickname || user.nickname === '微信用户' || !user.college || !user.major) {
        uni.showModal({
          title: '欢迎加入技能互助',
          content: '为了带来良好的使用体验以及互助的可信度，请您先完善个人的基本信息',
          confirmText: '去完善',
          confirmColor: '#007AFF',
          showCancel: false,
          success: (res) => {
            if (res.confirm) {
              uni.reLaunch({ url: '/pages/profile/edit?fromLogin=1' });
            }
          }
        });
      } else {
        // 全量信息准备完毕，直接进入大厅
        uni.switchTab({ url: '/pages/home/home' });
      }
    },
    // 处理微信登录逻辑
    handleWechatLogin() {
      if (this.isLoading) return;
      this.isLoading = true;

      uni.showLoading({ title: '登录中...' });

      // 1. 调用微信接口获取临时鉴权 code
      uni.login({
        provider: 'weixin',
        success: (loginRes) => {
          
          // ⚠️ 修复双机测试问题：因为没有真实 appid，wx.login的code每次都变，导致每次登录都被当成新用户。
          // 解决方案：在本地生成一个固定的机器码当做这台手机的专属 openId
          let stableMockOpenId = uni.getStorageSync('stableMockOpenId');
          if (!stableMockOpenId) {
            stableMockOpenId = 'wx_mock_' + new Date().getTime() + Math.floor(Math.random() * 1000);
            uni.setStorageSync('stableMockOpenId', stableMockOpenId);
          }

          const mockUserInfo = {
            nickname: "微信用户",
            avatarUrl: ""
          };

          request({
            url: '/api/user/login',
            method: 'POST',
            data: {
              code: stableMockOpenId, // 用固定的机器码骗过后端的 code 字段
              nickname: mockUserInfo.nickname,
              avatarUrl: mockUserInfo.avatarUrl
            }
          }).then(res => {
            uni.hideLoading();
            uni.showToast({ title: '登陆成功!', icon: 'success' });
            
            // 登录成功后，把后台返回的用户信息保存到手机本地缓存里
            uni.setStorageSync('userInfo', res);
            
            // 接下来走验证与分流逻辑
            this.checkAndRedirect(res);
            
            console.log("最终拿到当前登录的用户对象:", res);
          }).catch(err => {
            uni.hideLoading();
          }).finally(() => {
            this.isLoading = false;
          });
        },
        fail: (err) => {
          uni.hideLoading();
          uni.showToast({ title: '微信登录调用失败', icon: 'none' });
          this.isLoading = false;
        }
      });
    }
  }
}
</script>

<style>
/* ====================
   Apple & Instagram 设计风格的核心 CSS 
   ==================== */
page {
  /* 苹果风常用的纯净浅灰背景 */
  background-color: #F5F5F7;
  height: 100%;
}

.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-sizing: border-box;
  padding: 40px 24px;
  position: relative;
}

/* 顶部大标题无界设计 */
.header-section {
  display: flex;
  flex-direction: column;
  margin-top: 40px;
}
.title {
  font-size: 36px;
  font-weight: 700;
  color: #1D1D1F; /* 苹果深空灰 */
  letter-spacing: -1px;
}
.subtitle {
  font-size: 18px;
  font-weight: 500;
  color: #86868B;
  margin-top: 8px;
}

/* 占位图区域 */
.illustration-box {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 10px 0; /* 缩小上下间距，让图片更紧凑 */
}
.illustration {
  width: 100%;
  height: 380px; /* 从 280px 调大到 380px，让图片展示更大 */
  border-radius: 20px;
}

/* 底部操作卡片 - 毛玻璃悬浮效果 (Glassmorphism) */
.action-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px); /* 毛玻璃核心属性 */
  padding: 30px 24px;
  border-radius: 28px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.04); /* 极淡的弥散阴影 */
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.card-desc {
  display: block;
  text-align: center;
  font-size: 15px;
  color: #1D1D1F;
  margin-bottom: 24px;
  font-weight: 600;
}

/* 苹果风格的大圆角纯黑色按钮 */
.apple-btn {
  width: 100%;
  height: 54px;
  border-radius: 27px; /* 椭圆半圆角 */
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 17px;
  font-weight: 600;
  transition: transform 0.2s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.apple-btn:active {
  transform: scale(0.96); /* 按下微缩的物理反馈动效 */
}
.apple-btn.primary {
  background-color: #000000;
  color: #FFFFFF;
}

/* 底部小字 */
.terms {
  margin-top: 16px;
  text-align: center;
}
.terms-text {
  font-size: 12px;
  color: #86868B;
}
</style>
