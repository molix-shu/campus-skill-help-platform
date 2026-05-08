<template>
  <el-container class="admin-layout">
    <!-- 左侧导航 — 白色极简侧栏 -->
    <el-aside width="220px" class="admin-sidebar">
      <div class="sidebar-header">
        <div class="brand-mark">
          <div class="brand-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" width="22" height="22">
              <path d="M12 2L2 7l10 5 10-5-10-5z" fill="#165DFF"/>
              <path d="M2 17l10 5 10-5" stroke="#165DFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12l10 5 10-5" stroke="#165DFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" opacity="0.5"/>
            </svg>
          </div>
          <div class="brand-text">
            <span class="brand-name">技能互助</span>
            <span class="brand-sub">管理控制台</span>
          </div>
        </div>
      </div>

      <div class="sidebar-nav">
        <div class="nav-section-label">工作台</div>
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: $route.path === item.path }"
        >
          <div class="nav-icon" v-html="item.icon"></div>
          <span class="nav-label">{{ item.label }}</span>
          <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
        </router-link>
      </div>

      <div class="sidebar-footer">
        <div class="env-tag">v1.0.0 · 生产环境</div>
      </div>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container class="admin-body">
      <!-- 顶部导航栏 -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <h2 class="page-title">{{ $route.meta.title || '概览' }}</h2>
        </div>
        <div class="topbar-right">
          <div class="topbar-actions">
            <div class="action-item" title="刷新当前页面" @click="refreshPage" :class="{ spinning: isRefreshing }">
              <svg viewBox="0 0 24 24" fill="none" width="18" height="18"><path d="M1 4v6h6M23 20v-6h-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M20.49 9A9 9 0 005.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 013.51 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </div>
          </div>
          <div class="topbar-divider"></div>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="admin-avatar-wrap">
              <div class="admin-avatar">A</div>
              <span class="admin-name">管理员</span>
              <svg viewBox="0 0 24 24" fill="none" width="14" height="14" style="margin-left:4px;opacity:0.5"><path d="m6 9 6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <svg viewBox="0 0 24 24" fill="none" width="14" height="14" style="margin-right:6px"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="admin-main">
        <router-view />
      </main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { reactive, ref } from 'vue'

const router = useRouter()
const route = useRoute()
const isRefreshing = ref(false)

const navItems = reactive([
  {
    path: '/user',
    label: '用户管理',
    icon: '<svg viewBox="0 0 24 24" fill="none" width="18" height="18"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/><circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.75"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  },
  {
    path: '/demand',
    label: '帖子管理',
    icon: '<svg viewBox="0 0 24 24" fill="none" width="18" height="18"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 2v6h6M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  },
  {
    path: '/appeal',
    label: '申诉判决',
    icon: '<svg viewBox="0 0 24 24" fill="none" width="18" height="18"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>'
  }
])

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('admin_token')
    router.push('/login')
  }
}

// 刷新当前页面：直接重载整个页面，重新发起网络请求
const refreshPage = () => {
  isRefreshing.value = true
  setTimeout(() => {
    window.location.reload()
  }, 300)
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.admin-layout { height: 100vh; overflow: hidden; }

/* ===== 左侧导航栏 ===== */
.admin-sidebar {
  background: #FFFFFF;
  border-right: 1px solid #F2F3F5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid #F2F3F5;
}
.brand-mark {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-icon {
  width: 36px; height: 36px;
  background: #E8F3FF;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.brand-text { display: flex; flex-direction: column; }
.brand-name { font-size: 15px; font-weight: 600; color: #1D2129; line-height: 1.2; }
.brand-sub { font-size: 11px; color: #86909C; margin-top: 2px; }

/* 导航区 */
.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  overflow-y: auto;
}
.nav-section-label {
  font-size: 11px;
  color: #C9CDD4;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 8px 12px 6px;
  margin-bottom: 4px;
}
.nav-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  color: #4E5969;
  text-decoration: none;
  font-size: 14px;
  font-weight: 450;
  margin-bottom: 2px;
  transition: all 0.15s ease;
  cursor: pointer;
  position: relative;
}
.nav-item:hover { background: #F7F8FA; color: #1D2129; }
.nav-item.active {
  background: #E8F3FF;
  color: #165DFF;
  font-weight: 500;
}
.nav-item.active .nav-icon { color: #165DFF; }
.nav-icon {
  width: 18px; height: 18px;
  margin-right: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  color: #86909C;
  transition: color 0.15s;
}
.nav-item:hover .nav-icon { color: #4E5969; }
.nav-label { flex: 1; }
.nav-badge {
  font-size: 11px;
  background: #F53F3F;
  color: #fff;
  padding: 0 6px;
  border-radius: 10px;
  line-height: 18px;
  font-weight: 600;
}

/* 底部 */
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid #F2F3F5;
}
.env-tag {
  font-size: 11px;
  color: #C9CDD4;
  text-align: center;
}

/* ===== 右侧主体 ===== */
.admin-body {
  flex-direction: column;
  overflow: hidden;
  background: #F7F8FA;
}

/* 顶部栏 */
.admin-topbar {
  height: 56px;
  background: #FFFFFF;
  border-bottom: 1px solid #F2F3F5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}
.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
  margin: 0;
}
.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.topbar-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
.action-item {
  width: 32px; height: 32px;
  border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  color: #86909C;
  cursor: pointer;
  transition: all 0.15s;
}
.action-item:hover { background: #F2F3F5; color: #1D2129; }
.topbar-divider {
  width: 1px; height: 20px;
  background: #E5E6EB;
}
.admin-avatar-wrap {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.15s;
}
.admin-avatar-wrap:hover { background: #F7F8FA; }
.admin-avatar {
  width: 28px; height: 28px;
  border-radius: 6px;
  background: #165DFF;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  margin-right: 8px;
}
.admin-name {
  font-size: 13px;
  color: #1D2129;
  font-weight: 500;
}

/* 主内容区 */
.admin-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* 刷新旋转动画 */
.action-item.spinning svg {
  animation: spin 0.6s ease;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
