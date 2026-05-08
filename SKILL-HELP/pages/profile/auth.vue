<template>
  <view class="container">
    <view class="alert-box" v-if="userInfo.authStatus === 0 && userInfo.authMessage">
      <text class="alert-text">审核未通过：{{ userInfo.authMessage }}</text>
      <text class="alert-subtext">请根据管理员提示的原因修改并重新提交，以免影响积分积累</text>
    </view>
    <view class="form-group">
      <view class="form-item">
        <text class="label">真实姓名</text>
        <input class="input" v-model="form.realName" placeholder="请输入你的真实姓名" />
      </view>
      <view class="form-item">
        <text class="label">学院</text>
        <input class="input" v-model="form.college" placeholder="请输入所属学院" />
      </view>
      <view class="form-item">
        <text class="label">专业(班级)</text>
        <input class="input" v-model="form.major" placeholder="请输入专业及班级" />
      </view>
      <view class="form-item">
        <text class="label">学号</text>
        <input class="input" v-model="form.studentId" placeholder="请输入学号" type="number" />
      </view>
      
      <view class="form-item col-item">
        <text class="label mb" style="width: 200px">上传学生证（扉页带照片侧）</text>
        <view class="upload-box" @click="chooseImage">
          <image v-if="form.studentCardUrl" :src="form.studentCardUrl" class="card-img" mode="aspectFit"></image>
          <view v-else class="upload-placeholder">
            <text class="plus">+</text>
            <text class="upload-txt">点击上传</text>
          </view>
        </view>
        <text class="tip">仅用于管理员核实你的校园身份</text>
      </view>
    </view>

    <button class="submit-btn" @click="submitAuth" :disabled="isSubmitting">提交审核</button>
  </view>
</template>

<script>
import { request } from '../../utils/request'
export default {
  data() {
    return {
      userInfo: {},
      form: {
        id: null,
        realName: '',
        college: '',
        major: '',
        studentId: '',
        studentCardUrl: ''
      },
      isSubmitting: false
    }
  },
  onLoad() {
    const user = uni.getStorageSync('userInfo')
    if (user && user.id) {
      this.userInfo = user
      this.form.id = user.id
      this.form.realName = user.realName || ''
      this.form.college = user.college || ''
      this.form.major = user.major || ''
      this.form.studentId = user.studentId || ''
      this.form.studentCardUrl = user.studentCardUrl || ''
      
      // Fetch latest status in case authMessage was updated remotely
      request({ url: '/api/user/info?userId=' + user.id }).then(res => {
          if(res) {
             this.userInfo = res;
             uni.setStorageSync('userInfo', res);
             if (res.authStatus === 2) {
               uni.showToast({ title: '您已认证成功', icon: 'success' })
             }
          }
      });
    }
  },
  methods: {
    chooseImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          // 为了简单演示和无需搭建文件服务器，直接读取临时文件转 Base64 传递给后端的学生证字段。
          // 真正的项目应配合后端oss文件上传然后保存url。鉴于已经放宽了Tomcat 20MB限制，可以直接存小体积的base64
          uni.getFileSystemManager().readFile({
            filePath: res.tempFilePaths[0],
            encoding: 'base64',
            success: (base64Res) => {
              this.form.studentCardUrl = 'data:image/jpeg;base64,' + base64Res.data;
            }
          })
        }
      })
    },
    submitAuth() {
      if(!this.form.realName || !this.form.studentId || !this.form.studentCardUrl) {
         return uni.showToast({ title: '请填写完整信息再提交', icon: 'none' })
      }
      this.isSubmitting = true
      uni.showLoading({ title: '提交中' })
      
      request({
        url: '/api/user/auth',
        method: 'POST',
        data: this.form
      }).then(res => {
        uni.hideLoading()
        uni.showToast({ title: '已提交，等待管理员审核', icon: 'success', duration: 2000 })
        const u = Object.assign(this.userInfo, this.form);
        u.authStatus = 1;
        uni.setStorageSync('userInfo', u);
        setTimeout(() => { uni.navigateBack() }, 2000)
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
.form-group {
  background-color: #fff;
  border-radius: 12px;
  padding: 0 16px;
}
.form-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}
.form-item:last-child {
  border-bottom: none;
}
.col-item {
  flex-direction: column;
  align-items: flex-start;
}
.mb { margin-bottom: 12px; }
.label {
  width: 90px;
  font-size: 15px;
  color: #333;
}
.input {
  flex: 1;
  font-size: 15px;
}
.tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
.upload-box {
  width: 100%;
  height: 180px;
  background-color: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.plus {
  font-size: 40px;
  color: #ccc;
  font-weight: 300;
  line-height: .8;
}
.upload-txt {
  font-size: 14px;
  color: #999;
  margin-top: 10px;
}
.card-img {
  width: 100%;
  height: 100%;
}
.submit-btn {
  margin-top: 40px;
  background-color: #007aff;
  color: #fff;
  border-radius: 24px;
}
.alert-box {
  background-color: #fff0f0;
  border: 1px solid #ffd5d5;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 20px;
}
.alert-text {
  color: #d93025;
  font-size: 15px;
  font-weight: bold;
  display: block;
  margin-bottom: 4px;
}
.alert-subtext {
  color: #e57373;
  font-size: 12px;
}
</style>
