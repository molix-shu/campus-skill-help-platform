<template>
  <view class="container">
    <view class="header">
      <text class="title">{{ isEdit ? '修改我的技能' : '发布我的技能' }}</text>
      <text class="subtitle">{{ isEdit ? '及时更新技能信息，获取更多互助' : '展示特长，用你的技能去帮助别人并获得报酬' }}</text>
    </view>

    <view class="form-card">
      <view class="input-group" style="padding: 16px 0; border-bottom: 1px solid rgba(60, 60, 67, 0.08);">
        <text class="label">技能分类</text>
        <picker @change="onCategoryChange" :value="categoryIndex" :range="categoryNames">
          <view class="apple-input" style="display: flex; align-items: center; justify-content: space-between;">
            <text :class="categoryIndex === -1 ? 'ph-color' : ''">{{ categoryIndex === -1 ? '请点击选择分类' : categoryNames[categoryIndex] }}</text>
            <text style="color: #C7C7CC;">›</text>
          </view>
        </picker>
      </view>

      <view class="input-group">
        <text class="label">技能标题</text>
        <input class="apple-input" v-model="form.title" placeholder="例如: 专业修电脑 / 四六级一对一陪练" placeholder-class="ph-color" />
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">背景与经验介绍</text>
        <textarea class="apple-textarea" v-model="form.description" placeholder="你可以帮别人解决什么问题？你有多少经验？..." placeholder-class="ph-color"></textarea>
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">技能标签 (逗号分隔)</text>
        <input class="apple-input" v-model="form.tags" placeholder="例如: #电脑维修, #装系统" placeholder-class="ph-color" />
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">可服务时间</text>
        <input class="apple-input" v-model="form.serviceTime" placeholder="例如: 工作日晚 / 周末全天" placeholder-class="ph-color" />
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">服务方式 (线上/线下)</text>
        <input class="apple-input" v-model="form.serviceMethod" placeholder="例如: 线上辅导 / 线下见面" placeholder-class="ph-color" />
      </view>

      <view class="divider"></view>

      <view class="input-group">
        <text class="label">费用要求 (免费或收费标准)</text>
        <input class="apple-input" v-model="form.priceDesc" placeholder="例如: 免费互助 / 50元/次" placeholder-class="ph-color" />
      </view>

    </view>

    <view class="footer-action">
      <button class="submit-btn" :disabled="isSubmitting" @click="submitSkill">
        {{ isSubmitting ? '正在提交...' : (isEdit ? '保存修改' : '立刻发布技能') }}
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
      categoryNames: ['辅导答疑', '软件代做', '运动陪练', '生活互助'],
      form: {
        categoryId: null,
        title: '',
        description: '',
        tags: '',
        serviceTime: '',
        serviceMethod: '',
        priceDesc: ''
      }
    }
  },
  onLoad(options) {
    if (options.id) {
      this.isEdit = true;
      this.editId = options.id;
      uni.setNavigationBarTitle({ title: '修改我的技能' });
      this.fetchDetail();
    }
  },
  methods: {
    onCategoryChange(e) {
      this.categoryIndex = e.detail.value;
      // 注意：首页分类ID不连续，这里我们按照 publishing 顺序映射， 
      // 辅导答疑=2, 软件代做=3, 运动陪练=4, 生活互助=1 (为了跟大厅对齐)
      const idMap = [2, 3, 4, 1];
      this.form.categoryId = idMap[this.categoryIndex];
    },
    fetchDetail() {
      const user = uni.getStorageSync('userInfo');
      if (!user) return;
      uni.showLoading({ title: '加载中' });
      request({ url: '/api/skill/my?userId=' + user.id, method: 'GET' })
        .then(res => {
          uni.hideLoading();
          const target = res.find(s => String(s.id) === String(this.editId));
          if (target) {
            this.form.categoryId = target.categoryId;
            const idMap = [2, 3, 4, 1];
            this.categoryIndex = idMap.indexOf(target.categoryId);
            this.form.title = target.title || '';
            this.form.description = target.description || '';
            this.form.tags = target.tags || '';
            this.form.serviceTime = target.serviceTime || '';
            this.form.serviceMethod = target.serviceMethod || '';
            this.form.priceDesc = target.priceDesc || '';
          }
        })
        .catch(() => uni.hideLoading());
    },
    submitSkill() {
      if (!this.form.title || !this.form.description || !this.form.categoryId) {
        return uni.showToast({ title: '标题、介绍、分类都要填哦~', icon: 'none' });
      }

      if (this.isSubmitting) return;
      this.isSubmitting = true;
      uni.showLoading({ title: '发射中...' });

      const user = uni.getStorageSync('userInfo');
      if (!user || !user.id) {
        uni.hideLoading();
        this.isSubmitting = false;
        return uni.showToast({ title: '登录失效，请重新登录', icon: 'none' });
      }

      const payload = {
        userId: user.id,
        categoryId: this.form.categoryId,
        title: this.form.title,
        description: this.form.description,
        // 提交前清理掉所有中英文井号，交给展示层统一展示
        tags: this.form.tags ? this.form.tags.split(/[,,，]/).filter(Boolean).map(t => t.replace(/[#＃]/g, '').trim()).filter(Boolean).join(',') : '',
        serviceTime: this.form.serviceTime,
        serviceMethod: this.form.serviceMethod,
        priceDesc: this.form.priceDesc
      };
      
      if (this.isEdit) {
        payload.id = this.editId;
      }

      request({
        url: this.isEdit ? '/api/skill/update' : '/api/skill/publish',
        method: 'POST',
        data: payload
      }).then(res => {
        uni.hideLoading();
        uni.showToast({ title: this.isEdit ? '修改成功！' : '技能发布成功！', icon: 'success' });
        setTimeout(() => {
          if (this.isEdit) {
            uni.navigateBack();
          } else {
            // 发布成功后跳转到技能大厅
            uni.switchTab({ url: '/pages/home/home' });
          }
        }, 2000);
      }).catch(err => {
        uni.hideLoading();
        uni.showToast({ title: '发布失败', icon: 'none' });
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
.header { margin-bottom: 24px; }
.title { font-size: 32px; font-weight: 700; color: #1D1D1F; }
.subtitle { font-size: 15px; color: #8E8E93; margin-top: 8px; display: block; }
.form-card { background-color: #FFFFFF; border-radius: 20px; padding: 10px 20px; box-shadow: 0 4px 24px rgba(0,0,0,0.02); }
.input-group { padding: 16px 0; }
.label { font-size: 13px; font-weight: 600; color: #86868B; margin-bottom: 8px; display: block; }
.apple-input { height: 44px; font-size: 16px; color: #1D1D1F; }
.apple-textarea { width: 100%; height: 100px; font-size: 16px; color: #1D1D1F; line-height: 1.5; }
.ph-color { color: #C7C7CC; }
.divider { height: 1px; background-color: rgba(60, 60, 67, 0.08); }
.footer-action { position: fixed; bottom: 0; left: 0; right: 0; padding: 16px 24px 34px 24px; background: rgba(245, 245, 247, 0.9); backdrop-filter: blur(15px); }
.submit-btn { background-color: #1D1D1F; color: #FFF; height: 52px; border-radius: 26px; font-size: 17px; font-weight: 600; display: flex; justify-content: center; align-items: center; }
.submit-btn:disabled { background-color: #8E8E93; color: #E3E3E8; }
</style>
