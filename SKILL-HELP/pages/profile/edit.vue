<template>
  <view class="container">
    <view class="edit-card">
      <view class="avatar-section">
        <!-- 小程序专用的头像选择功能 -->
        <button class="avatar-wrapper" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
          <image class="avatar" :src="formData.avatarUrl || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
          <view class="camera-icon">📷</view>
        </button>
        <text class="hint-text">点击修改头像</text>
      </view>

      <view class="form-group">
        <text class="label">常用昵称</text>
        <!-- 小程序专用的昵称填写 (会弹出键盘上方快捷使用微信昵称的功能) -->
        <input type="nickname" class="input-field" placeholder="请输入或使用微信昵称" 
               v-model="formData.nickname" @blur="onNicknameBlur" />
      </view>

      <view class="form-group">
        <text class="label">真实姓名 (选填)</text>
        <input class="input-field" placeholder="便于线下互助联系" v-model="formData.realName" />
      </view>

      <view class="form-group">
        <text class="label">所属学院 (必填)</text>
        <input class="input-field" placeholder="例如：软件工程学院" v-model="formData.college" />
      </view>

      <view class="form-group">
        <text class="label">专业方向 (必填)</text>
        <input class="input-field" placeholder="例如：计科2020级" v-model="formData.major" />
      </view>
    </view>

    <button class="save-btn" @click="handleSave">保存修改</button>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      formData: {
        avatarUrl: '',
        nickname: '',
        realName: '',
        college: '',
        major: ''
      },
      fromLogin: false
    }
  },
  onLoad(options) {
    if (options.fromLogin) {
      this.fromLogin = true;
    }
    // 读取现有资料填进去
    const user = uni.getStorageSync('userInfo');
    if (user) {
      this.formData = {
        avatarUrl: user.avatarUrl || '',
        nickname: user.nickname || '',
        realName: user.realName || '',
        college: user.college || '',
        major: user.major || ''
      };
    }
  },
  methods: {
    // 微信小程序特性：获取用户选定的头像 (支持拍照或相册选最新的)
    onChooseAvatar(e) {
      const tempPath = e.detail.avatarUrl;
      uni.showLoading({ title: '处理头像中...' });
      // 将本地临时路径转为 Base64 以便永久存入数据库，跨设备可正确显示
      uni.getFileSystemManager().readFile({
        filePath: tempPath,
        encoding: 'base64',
        success: res => {
          uni.hideLoading();
          this.formData.avatarUrl = 'data:image/jpeg;base64,' + res.data;
          uni.showToast({ title: '头像已选择 ✓', icon: 'success', duration: 1000 });
        },
        fail: err => {
          uni.hideLoading();
          // 读取失败时退而求其次用临时路径（仅本机本次有效），起码不阻止填资料
          this.formData.avatarUrl = tempPath;
          uni.showToast({ title: '头像选择成功（本机显示）', icon: 'none' });
        }
      });
    },
    // 微信小程序特性：输入框失去焦点时，捕捉真实填写的昵称
    onNicknameBlur(e) {
      this.formData.nickname = e.detail.value;
    },
    handleSave() {
      // 只在头像真正为空时才拦截（不阻止已有 wxfile 路径或 base64 通过）
      if (!this.formData.avatarUrl) return uni.showToast({ title: '请先点击头像选择图片', icon: 'none' });
      if (!this.formData.nickname || this.formData.nickname.trim() === '') return uni.showToast({ title: '请输入常用昵称', icon: 'none' });
      if (!this.formData.college || this.formData.college.trim() === '') return uni.showToast({ title: '请输入所属学院', icon: 'none' });
      if (!this.formData.major || this.formData.major.trim() === '') return uni.showToast({ title: '请输入专业方向', icon: 'none' });

      uni.showLoading({ title: '保存中' });
      const user = uni.getStorageSync('userInfo');

      request({
        url: '/api/user/update',
        method: 'POST',
        data: {
          id: user.id,
          nickname: this.formData.nickname,
          avatarUrl: this.formData.avatarUrl,
          realName: this.formData.realName,
          college: this.formData.college,
          major: this.formData.major
        }
      }).then(res => {
        uni.hideLoading();
        // 用后端返回的最新的 user 数据覆盖本地缓存
        uni.setStorageSync('userInfo', res);
        uni.showToast({ title: '资料更新成功', icon: 'success' });
        setTimeout(() => {
          if (this.fromLogin) {
             uni.switchTab({ url: '/pages/home/home' });
          } else {
             const pages = getCurrentPages();
             if (pages.length > 1) {
                uni.navigateBack();
             } else {
                uni.switchTab({ url: '/pages/home/home' });
             }
          }
        }, 800);
      }).catch(err => {
        uni.hideLoading();
        uni.showToast({ title: '更新失败', icon: 'none' });
      });
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
  padding: 20px;
}
.edit-card {
  background-color: #FFF;
  border-radius: 20px;
  padding: 30px 20px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
}
.avatar-wrapper {
  padding: 0;
  width: 90px;
  height: 90px;
  border-radius: 45px;
  background-color: #E3E3E8;
  border: none;
  position: relative;
  overflow: visible;
}
.avatar-wrapper::after {
  border: none;
}
.avatar {
  width: 90px;
  height: 90px;
  border-radius: 45px;
}
.camera-icon {
  position: absolute;
  right: -5px;
  bottom: 0;
  background-color: #FFF;
  border-radius: 15px;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.hint-text {
  margin-top: 12px;
  font-size: 13px;
  color: #8E8E93;
}

.form-group {
  margin-bottom: 24px;
}
.form-group:last-child {
  margin-bottom: 0;
}
.label {
  font-size: 14px;
  color: #636366;
  margin-bottom: 8px;
  display: block;
}
.input-field {
  background-color: #F5F5F7;
  height: 50px;
  border-radius: 12px;
  padding: 0 16px;
  font-size: 16px;
  color: #1D1D1F;
}

.save-btn {
  margin-top: 40px;
  background-color: #000;
  color: #FFF;
  height: 56px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  justify-content: center;
  align-items: center;
}
.save-btn:active {
  opacity: 0.8;
}
</style>
