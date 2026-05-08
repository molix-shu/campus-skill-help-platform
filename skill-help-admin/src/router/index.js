import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    // 刷新中转路由：从 /redirect/xxx 立刻跳回 /xxx，触发组件重新挂载
    path: '/redirect/:path(.*)',
    component: { render: () => null },
    beforeEnter: (to, from, next) => {
      const target = '/' + to.params.path
      next(target)
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue')
  },
  {
    path: '/',
    name: 'Layout',
    redirect: '/user',
    component: () => import('../layout/index.vue'),
    children: [
      {
        path: '/user',
        name: 'UserManage',
        meta: { title: '用户管理' },
        component: () => import('../views/user/index.vue')
      },
      {
        path: '/demand',
        name: 'DemandManage',
        meta: { title: '帖子/需求管理' },
        component: () => import('../views/demand/index.vue')
      },
      {
        path: '/appeal',
        name: 'AppealManage',
        meta: { title: '信用申诉判决' },
        component: () => import('../views/appeal/index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：简单判断是否有 token
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
