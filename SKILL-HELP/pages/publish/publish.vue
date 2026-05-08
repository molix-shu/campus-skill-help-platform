<template>
  <view class="container">
    <view class="header">
      <text class="title">{{ isEdit ? '修改我的求助' : '发布求助' }}</text>
      <text class="subtitle">{{ isEdit ? '及时修改你的需求说明以获取同学们的援助' : '详细描述你的困难，让拥有技能的同学更易匹配到你' }}</text>
    </view>

    <!-- Apple 风格圆角白色沉浸式表单框 -->
    <view class="form-card">
      <view class="input-group" style="padding: 16px 0; border-bottom: 1px solid rgba(60, 60, 67, 0.08);">
        <text class="label">需求分类</text>
        <picker @change="onCategoryChange" :value="categoryIndex" :range="categoryNames">
          <view class="apple-input" style="display: flex; align-items: center; justify-content: space-between;">
            <text :class="categoryIndex === -1 ? 'ph-color' : ''">{{ categoryIndex === -1 ? '请点击选择分类' : categoryNames[categoryIndex] }}</text>
            <text style="color: #C7C7CC;">›</text>
          </view>
        </picker>
      </view>

      <view class="input-group">
        <text class="label">需求标题</text>
        <input class="apple-input" v-model="form.title" placeholder="例如: 期末高数冲刺辅导 / PR宣传片剪辑求助" placeholder-class="ph-color" />
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">具体描述</text>
        <textarea class="apple-textarea" v-model="form.description" placeholder="请详细说明你遇到了什么麻烦，希望对方怎么帮你解决..." placeholder-class="ph-color"></textarea>
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">技能标签 (用逗号分隔以利于被搜索到)</text>
        <input class="apple-input" v-model="form.tags" placeholder="例如: #高等数学, #视频剪辑, #急求" placeholder-class="ph-color" />
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">希望的服务方式 (线上/线下)</text>
        <input class="apple-input" v-model="form.serviceMethod" placeholder="例如: 随时线上解答 / 图书馆线下见面" placeholder-class="ph-color" />
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">期望完成时间</text>
        <input class="apple-input" v-model="form.expectedTime" placeholder="例如: 越快越好 / 本周五之前" placeholder-class="ph-color" />
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">你打算如何感谢帮助你的人？(报酬说明)</text>
        <input class="apple-input" v-model="form.rewardDesc" placeholder="例如: 一杯瑞幸咖啡 / 互换技能辅导" placeholder-class="ph-color" />
      </view>
    </view>

    <!-- 吸底发布的巨大圆角按钮 -->
    <view class="footer-action">
      <button class="submit-btn" :disabled="isSubmitting" @click="submitDemand">
        {{ isSubmitting ? '正在提交...' : (isEdit ? '保存修改' : '立刻发布') }}
      </button>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      categoryIndex: -1,
      categoryNames: ['生活求助', '课业辅导', '技术支持', '出二手货'],
      form: {
        categoryId: null,
        title: '',
        description: '',
        tags: '',
        serviceMethod: '',
        expectedTime: '',
        rewardDesc: ''
      }
    }
  },
  onLoad(options) {
    if (options.id) {
      this.isEdit = true;
      this.editId = options.id;
      uni.setNavigationBarTitle({ title: '修改求助信息' });
      this.fetchDetail();
    }
  },
  methods: {
    onCategoryChange(e) {
      this.categoryIndex = e.detail.value;
      this.form.categoryId = Number(this.categoryIndex) + 1; // ID从1开始
    },
    fetchDetail() {
      const user = uni.getStorageSync('userInfo');
      if (!user) return;
      uni.showLoading({ title: '加载中' });
      request({ url: '/api/demand/my?userId=' + user.id, method: 'GET' })
        .then(res => {
          uni.hideLoading();
          const target = res.find(d => String(d.id) === String(this.editId));
          if (target) {
            this.form.categoryId = target.categoryId;
            this.categoryIndex = target.categoryId ? target.categoryId - 1 : -1;
            this.form.title = target.title || '';
            this.form.description = target.description || '';
            this.form.tags = target.tags || '';
            this.form.serviceMethod = target.serviceMethod || '';
            this.form.expectedTime = target.expectedTime || '';
            this.form.rewardDesc = target.rewardDesc || '';
          }
        })
        .catch(() => uni.hideLoading());
    },
    submitDemand() {
      // 1. 表单验证基础防御
      if (!this.form.title || !this.form.description || !this.form.categoryId) {
        return uni.showToast({ title: '标题、描述和分类都要填哦~', icon: 'none' });
      }

      // 2. 防连点拦截
      if (this.isSubmitting) return;
      this.isSubmitting = true;
      uni.showLoading({ title: '发射中...' });

      // 3. 从本地存储拿一下当前是谁在发帖？(用户ID)
      const user = uni.getStorageSync('userInfo');
      if (!user || !user.id) {
        uni.hideLoading();
        this.isSubmitting = false;
        return uni.showToast({ title: '登录信息失效，请重新登录', icon: 'none' });
      }

      // 4. 调用后端接口，真正写入 MySQL
      const payload = {
        userId: user.id,
        categoryId: this.form.categoryId,
        title: this.form.title,
        description: this.form.description,
        // 提交前清理掉所有中英文井号，交给展示层统一展示
        tags: this.form.tags ? this.form.tags.split(/[,,，]/).filter(Boolean).map(t => t.replace(/[#＃]/g, '').trim()).filter(Boolean).join(',') : '',
        serviceMethod: this.form.serviceMethod,
        expectedTime: this.form.expectedTime,
        rewardDesc: this.form.rewardDesc
      };
      
      if (this.isEdit) {
        payload.id = this.editId;
      }

      request({
        url: this.isEdit ? '/api/demand/update' : '/api/demand/publish',
        method: 'POST',
        data: payload
      }).then(res => {
        uni.hideLoading();
        uni.showToast({ title: this.isEdit ? '修改成功！' : '发布成功！', icon: 'success' });
        
        // 发布/修改成功后跳转
        setTimeout(() => {
          if (this.isEdit) {
            uni.navigateBack();
          } else {
            // 发布成功后跳转到需求大厅
            uni.switchTab({ url: '/pages/demand/demand' });
          }
        }, 2000);

      }).catch(err => {
        uni.hideLoading();
        uni.showToast({ title: '发贴失败', icon: 'none' });
      }).finally(() => {
        this.isSubmitting = false;
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
  padding: 24px 20px 100px 20px;
}
.header {
  margin-bottom: 24px;
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

/* 苹果风卡片式内阴影表单 */
.form-card {
  background-color: #FFFFFF;
  border-radius: 20px;
  padding: 10px 20px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.02);
}
.input-group {
  padding: 16px 0;
}
.label {
  font-size: 13px;
  font-weight: 600;
  color: #86868B;
  margin-bottom: 8px;
  display: block;
}
.apple-input {
  height: 44px;
  font-size: 16px;
  color: #1D1D1F;
}
.apple-textarea {
  width: 100%;
  height: 100px;
  font-size: 16px;
  color: #1D1D1F;
  line-height: 1.5;
}
.ph-color {
  color: #C7C7CC;
}

/* 极细的浅灰色分割线 */
.divider {
  height: 1px;
  background-color: rgba(60, 60, 67, 0.08); /* 极致柔和的线条 */
}

/* 底部操作区定位 */
.footer-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 24px 34px 24px; /* 苹果安全区 */
  background: rgba(245, 245, 247, 0.9);
  backdrop-filter: blur(15px); /* 毛玻璃吸底 */
}
.submit-btn {
  background-color: #1D1D1F;
  color: #FFF;
  height: 52px;
  border-radius: 26px;
  font-size: 17px;
  font-weight: 600;
  display: flex;
  justify-content: center;
  align-items: center;
}
.submit-btn:disabled {
  background-color: #8E8E93;
  color: #E3E3E8;
}
</style>
