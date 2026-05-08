<template>
  <view class="container">
    <view class="header-box">
      <text class="title">信用申诉</text>
      <text class="subtitle">如果系统判定了错误的违约或者差评，可在本版块向管理员申明原因，我们将为您的人工重审并恢复信用分。</text>
    </view>
    
    <view class="form-group">
      <view class="form-item col-item">
        <text class="label mb">相关订单/任务标题 (选填)</text>
        <picker :range="orderList" range-key="targetTitle" @change="onOrderChange" class="input-full custom-picker">
          <view v-if="form.relationTitle" class="picker-inner">{{ form.relationTitle }}</view>
          <view v-else class="picker-placeholder">点击选择您要申诉的历史订单...</view>
        </picker>
      </view>
      <view class="form-item col-item">
        <text class="label mb">申诉详细理由说明</text>
        <textarea class="textarea-full" v-model="form.content" placeholder="请详细描述具体的争端或申诉理由..." maxlength="200" auto-height></textarea>
      </view>
    </view>

    <button class="submit-btn" @click="submitAuth" :disabled="isSubmitting">提交申诉</button>
  </view>
</template>

<script>
import { request } from '../../utils/request'
export default {
  data() {
    return {
      orderList: [],
      userInfo: {},
      form: {
        userId: null,
        relationTitle: '',
        content: '',
      },
      isSubmitting: false
    }
  },
  onLoad() {
    const user = uni.getStorageSync('userInfo')
    if (user && user.id) {
      this.userInfo = user
      this.form.userId = user.id
      this.fetchOrders(user.id)
    }
  },
  methods: {
    fetchOrders(userId) {
      request({
        url: '/api/order/my?userId=' + userId,
        method: 'GET'
      }).then(res => {
        this.orderList = res || [];
      })
    },
    onOrderChange(e) {
      const idx = e.detail.value;
      const order = this.orderList[idx];
      this.form.relationTitle = (order.targetTitle || '互助任务') + ' (编号:' + (order.orderNo || order.id) + ')';
    },
    submitAuth() {
      if(!this.form.content) {
         return uni.showToast({ title: '理由不能为空', icon: 'none' })
      }
      this.isSubmitting = true
      uni.showLoading({ title: '提交中' })
      
      request({
        url: '/api/appeal/submit',
        method: 'POST',
        data: this.form
      }).then(res => {
        uni.hideLoading()
        uni.showToast({ title: '申诉已提交，管理员将尽快处理', icon: 'success' })
        setTimeout(() => { uni.navigateBack() }, 1500)
      }).catch(err => {
        uni.hideLoading()
      }).finally(() => {
        this.isSubmitting = false
      })
    }
  }
}
</script>

<style scoped>
.container {
  padding: 20px;
  background-color: #F5F5F7;
  height: 100vh;
}
.header-box { margin-bottom: 20px; }
.title { font-size: 20px; font-weight: bold; color: #1d1d1f; display: block; margin-bottom: 8px;}
.subtitle { font-size: 13px; color: #8E8E93;}
.form-group {
  background-color: #fff;
  border-radius: 12px;
  padding: 0 16px;
}
.form-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}
.form-item:last-child {
  border-bottom: none;
}
.col-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.mb { margin-bottom: 12px; }
.label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.input-full {
  width: 100%;
  height: 48px;
  line-height: normal;
  font-size: 15px;
  background-color: #f9f9f9;
  padding: 0 12px;
  box-sizing: border-box;
  border-radius: 8px;
}
.custom-picker {
  display: flex;
  align-items: center;
}
.picker-inner {
  font-size: 15px;
  color: #333;
}
.picker-placeholder {
  font-size: 15px;
  color: #999;
}
.textarea-full {
   width: 100%;
   min-height: 80px;
   font-size: 15px;
   background-color: #f9f9f9;
   padding: 10px;
   box-sizing: border-box;
   border-radius: 8px;
}
.submit-btn {
  margin-top: 30px;
  background-color: #007aff;
  color: #fff;
  border-radius: 24px;
}
</style>
