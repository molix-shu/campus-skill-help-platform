<template>
  <view class="container">
    <view class="hero">
      <text class="title">你想发布什么？</text>
      <text class="subtitle">无论是提供技能，还是寻求帮助，这里都是最好的起点。我们已通过规则引擎为您优化展示。</text>
    </view>
    
    <view class="options-container">
      <view class="card option-card" @click="navTo('/pages/publish/publish-skill')">
        <view class="icon-wrap dark">
          <text class="emoji">💡</text>
        </view>
        <view class="text-wrap">
          <text class="card-title">提供我的技能</text>
          <text class="card-desc">我有一技之长，想帮助其他同学</text>
        </view>
        <text class="arrow">›</text>
      </view>

      <view class="card option-card" @click="navTo('/pages/publish/publish')">
        <view class="icon-wrap light">
          <text class="emoji">🆘</text>
        </view>
        <view class="text-wrap">
          <text class="card-title">我需要帮助</text>
          <text class="card-desc">我在学习或生活中遇到了困难，急需外援</text>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>
    
    <!-- 占位图或插画区 -->
    <view class="illustration-placeholder">
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    navTo(url) {
      uni.navigateTo({ url });
    }
  }
}
</script>

<style>
page { background-color: #F5F5F7; height: 100%; }
.container { display: flex; flex-direction: column; height: 100vh; padding: 40px 20px;}

.hero { margin-bottom: 40px; }
.title { font-size: 32px; font-weight: 700; color: #1D1D1F; margin-bottom: 12px; display: block; }
.subtitle { font-size: 15px; color: #8E8E93; line-height: 1.5; display: block; }

.options-container { display: flex; flex-direction: column; gap: 20px; }
.card {
  background: #FFF;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
  transition: transform 0.2s;
}
.card:active { transform: scale(0.98); }

.icon-wrap { width: 60px; height: 60px; border-radius: 16px; display: flex; align-items: center; justify-content: center; margin-right: 16px; font-size: 32px; }
.icon-wrap.dark { background: #1D1D1F; }
.icon-wrap.light { background: #FF9500; }

.text-wrap { flex: 1; display: flex; flex-direction: column; }
.card-title { font-size: 18px; font-weight: 600; color: #1D1D1F; margin-bottom: 4px; }
.card-desc { font-size: 13px; color: #8E8E93; }
.arrow { font-size: 24px; color: #C7C7CC; }
</style>
