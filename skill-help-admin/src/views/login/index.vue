<template>
  <div class="login-page">
    <div class="login-bg-pattern"></div>
    
    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-logo">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="40" height="40">
            <path d="M12 2L2 7l10 5 10-5-10-5z" fill="#fff"/>
            <path d="M2 17l10 5 10-5" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12l10 5 10-5" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.5"/>
          </svg>
        </div>
        <h1 class="brand-title">校园技能互助平台</h1>
        <p class="brand-desc">管理控制台 · Admin Console</p>
        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>用户身份审核与管理</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>帖子内容安全治理</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>信用申诉仲裁判决</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-wrap">
        <div class="form-inner">
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">请使用管理员账号登录控制台</p>

          <div class="form-field">
            <label class="field-label">管理员账号</label>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入账号"
              size="large"
              prefix-icon="User"
            />
          </div>

          <div class="form-field">
            <label class="field-label">登录密码</label>
            <el-input
              type="password"
              v-model="loginForm.password"
              placeholder="请输入密码"
              size="large"
              show-password
              prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </div>

          <el-button
            type="primary"
            size="large"
            class="login-submit"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录控制台' }}
          </el-button>

          <div class="login-footer">
            <span class="footer-text">校园技能互助平台 · 管理端 v1.0</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const router = useRouter()
const loginForm = reactive({ username: '', password: '' })
const loading = ref(false)

const handleLogin = () => {
    if(!loginForm.username || !loginForm.password) {
        return ElMessage.warning('请输入账号密码')
    }
    loading.value = true
    request.post('/user/login', loginForm).then(res => {
        ElMessage.success('登录成功')
        localStorage.setItem('admin_token', res.data.token)
        router.push('/')
    }).catch(() => {}).finally(() => {
        loading.value = false
    })
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F7F8FA;
  position: relative;
  overflow: hidden;
}
.login-bg-pattern {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 60% at 10% 90%, rgba(22,93,255,0.06), transparent),
    radial-gradient(ellipse 60% 50% at 90% 10%, rgba(22,93,255,0.04), transparent);
}

.login-container {
  display: flex;
  width: 880px;
  min-height: 520px;
  background: #FFFFFF;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.06), 0 1px 3px rgba(0,0,0,0.04);
  position: relative;
  z-index: 1;
}

/* 左侧品牌 */
.login-brand {
  width: 380px;
  background: linear-gradient(135deg, #165DFF 0%, #0E42D2 100%);
  padding: 48px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.brand-logo {
  width: 56px; height: 56px;
  background: rgba(255,255,255,0.15);
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 28px;
  backdrop-filter: blur(8px);
}
.brand-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px;
  line-height: 1.3;
}
.brand-desc {
  font-size: 14px;
  opacity: 0.7;
  margin: 0 0 40px;
}
.brand-features { display: flex; flex-direction: column; gap: 14px; }
.feature-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  opacity: 0.85;
  gap: 10px;
}
.feature-dot {
  width: 6px; height: 6px;
  border-radius: 3px;
  background: rgba(255,255,255,0.6);
  flex-shrink: 0;
}

/* 右侧表单 */
.login-form-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
}
.form-inner { width: 100%; max-width: 340px; }
.form-title {
  font-size: 22px;
  font-weight: 600;
  color: #1D2129;
  margin: 0 0 6px;
}
.form-subtitle {
  font-size: 14px;
  color: #86909C;
  margin: 0 0 32px;
}
.form-field { margin-bottom: 20px; }
.field-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #4E5969;
  margin-bottom: 6px;
}
.login-submit {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px !important;
  margin-top: 8px;
}
.login-footer {
  margin-top: 32px;
  text-align: center;
}
.footer-text {
  font-size: 12px;
  color: #C9CDD4;
}
</style>
