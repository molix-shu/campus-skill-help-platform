<template>
  <view class="container">
    <scroll-view scroll-y class="content-scroll">
      
      <!-- 极简封面区 -->
      <view class="hero-image-wrap" v-if="demand.coverUrl && demand.coverUrl.indexOf('http') === 0">
        <image class="hero-image" :src="demand.coverUrl" mode="aspectFill"></image>
      </view>

      <view class="main-body">
        <text class="hero-title-minimal">{{ demand.title || '求助详情' }}</text>
        
        <view class="badge-row-minimal">
          <text class="urgency-badge-minimal" v-if="demand.tags && demand.tags.includes('急求')">急求帮助</text>
          <text class="type-badge-minimal">{{ demand.rewardDesc ? '有偿互助' : '志愿互助' }}</text>
        </view>

        <!-- 发布者名片极简流 -->
        <view class="publisher-card-minimal" @click="goUserProfile">
          <image class="avatar-minimal" :src="demand.publisherAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
          <view class="user-info-minimal">
            <view class="name-row-minimal">
              <text class="nickname-minimal">{{ demand.publisherName || '微信用户' }}</text>
              <text class="auth-tag-minimal">已实名认证</text>
            </view>
            <view class="stats-row-minimal">
              <text class="stat-text-minimal">发布了此求助请求</text>
            </view>
          </view>
          <image class="arrow-tiny" :src="iconChevronRight"></image>
        </view>

        <!-- 匹配线框极简设计 -->
        <view class="match-box-minimal" v-if="demand.matchScore">
          <view class="match-header-minimal">
            <text class="match-icon-minimal">🎯</text>
            <text class="match-title-minimal">你的技能适合帮忙 (匹配度 {{ demand.matchScore }}%)</text>
          </view>
          <view class="match-reasons-minimal">
            <text class="reason-tag-minimal" v-for="(reason, idx) in demand.matchReasons" :key="idx">{{ reason }}</text>
            <text class="reason-tag-minimal">时间宽裕</text>
          </view>
        </view>

        <view class="info-list-minimal">
          <view class="info-row-minimal">
            <text class="info-label-minimal">期望时间</text>
            <text class="info-value-minimal">{{ demand.expectTime || '越快越好' }}</text>
          </view>
          <view class="info-row-minimal">
            <text class="info-label-minimal">服务方式</text>
            <text class="info-value-minimal">{{ demand.serviceMethod || '线下 / 线上均可' }}</text>
          </view>
          <view class="info-row-minimal">
            <text class="info-label-minimal">预算/酬劳</text>
            <text class="info-value-minimal price-minimal">{{ demand.rewardDesc || '互助' }}</text>
          </view>
        </view>

        <view class="tags-group-minimal" v-if="processedTags.length > 0">
          <text class="hashtag-minimal" v-for="t in processedTags" :key="t">#{{ t }}</text>
        </view>
        
        <view class="divider-minimal"></view>
        
        <text class="section-title-minimal">详细说明</text>
        <text class="desc-text-minimal">{{ demand.description || 'TA遇到了困难，需要你的帮助...' }}</text>
      </view>
      
      <view style="height: 100px;"></view>
    </scroll-view>

    <!-- 纯粹黑白灰底部操作栏 -->
    <view class="bottom-bar-minimal">
      <view class="bottom-bar-inner-minimal">
        <button v-if="!isMyDemand" class="apply-btn-minimal" @click="handleAccept">我有能力接单！</button>
        <button v-else class="apply-btn-minimal disable-btn-minimal">我发布的求助</button>
        
        <view class="favorite-pill-minimal" @click="collect">
          <image class="collect-icon-minimal" :src="isCollected ? iconStarFilled : iconStarEmpty" mode="aspectFit"></image>
          <text class="icon-text-minimal" :style="isCollected ? 'color: #111111; font-weight: 600;' : 'color: #8E8E93;'">{{ isCollected ? '已收藏' : '收藏' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      demandId: null,
      demand: {},
      tags: [],
      isMyDemand: false,
      isCollected: false,
      // 收藏星星使用金黄色
      iconStarEmpty: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEyIDE3LjI3TDE4LjE4IDIxTDE2LjU0IDEzLjk3TDIyIDkuMjRMMTQuODEgOC42M0wxMiAyTDkuMTkgOC42M0wyIDkuMjRMNy40NiAxMy45N0w1LjgyIDIxTDEyIDE3LjI3WiIgc3Ryb2tlPSIjQzdDN0NDIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lam9pbj0icm91bmQiLz48L3N2Zz4=',
      iconStarFilled: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEyIDE3LjI3TDE4LjE4IDIxTDE2LjU0IDEzLjk3TDIyIDkuMjRMMTQuODEgOC42M0wxMiAyTDkuMTkgOC42M0wyIDkuMjRMNy40NiAxMy45N0w1LjgyIDIxTDEyIDE3LjI3WiIgZmlsbD0iI0Y1QTYyMyIgc3Ryb2tlPSIjRjVBNjIzIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lam9pbj0icm91bmQiLz48L3N2Zz4=',
      iconChevronRight: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjQzdDN0NDIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PHBhdGggZD0ibTkgMTggNi02LTYtNiI+PC9wYXRoPjwvc3ZnPg=='
    }
  },
  onLoad(options) {
    if (options.id) {
      this.demandId = options.id;
      this.fetchDemandDetail();
    }
  },
  computed: {
    processedTags() {
      return this.tags.map(t => t.replace(/[#＃]/g, '').trim()).filter(Boolean);
    }
  },
  methods: {
    fetchDemandDetail() {
      uni.showLoading({ title: '加载中' });
      request({ url: '/api/demand/list', method: 'GET' })
        .then(res => {
          uni.hideLoading();
          const target = res.find(d => String(d.id) === String(this.demandId));
          if (target) {
            this.demand = target;
            if (this.demand.tags) {
              this.tags = this.demand.tags.split(' ').join(',').split(',').filter(Boolean);
            }
            const user = uni.getStorageSync('userInfo');
            if (user && String(user.id) === String(this.demand.userId)) {
              this.isMyDemand = true;
            }
            
            const favs = uni.getStorageSync('my_favorites_demand') || [];
            this.isCollected = !!favs.find(i => String(i.id) === String(this.demandId));
            
          } else {
            uni.showToast({ title: '求助已解决或不存在', icon: 'none' });
          }
        }).catch(() => uni.hideLoading());
    },
    collect() {
      this.isCollected = !this.isCollected;
      let favs = uni.getStorageSync('my_favorites_demand') || [];
      if (this.isCollected) {
        if (!favs.find(i => String(i.id) === String(this.demand.id))) {
          favs.push(this.demand);
        }
        uni.showToast({ title: '已添加到收藏', icon: 'none' });
      } else {
        favs = favs.filter(i => String(i.id) !== String(this.demand.id));
        uni.showToast({ title: '已从我的收藏移除', icon: 'none' });
      }
      uni.setStorageSync('my_favorites_demand', favs);
    },
    goUserProfile() {
      if (this.demand.userId) {
        uni.navigateTo({ url: `/pages/profile/user-profile?userId=${this.demand.userId}` });
      }
    },
    handleAccept() {
      const user = uni.getStorageSync('userInfo');
      if (!user || !user.id) return uni.showToast({ title: '请先登录', icon: 'none' });

      uni.showModal({
        title: '接单确认',
        content: `你有把握帮助这位同学解决问题吗？`,
        confirmColor: '#111111',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading();
            request({
              url: '/api/order/create',
              method: 'POST',
              data: {
                type: 2, 
                targetId: this.demand.id,
                publisherId: this.demand.userId,
                applicantId: user.id
              }
            }).then(() => {
              uni.hideLoading();
              uni.showToast({ title: '接单成功！', icon: 'success' });
              setTimeout(() => uni.navigateBack(), 1500);
            }).catch(() => uni.hideLoading());
          }
        }
      });
    }
  }
}
</script>

<style>
/* 与 skill-detail 统一的极简风格 */
page { background-color: #FFFFFF; height: 100%; }
.container { display: flex; flex-direction: column; height: 100vh; position: relative; }
.content-scroll { flex: 1; }

.hero-image-wrap {
  width: 100%;
  height: 240px;
  background-color: #F7F7F7;
}
.hero-image { width: 100%; height: 100%; }

.main-body { padding: 24px; }

.badge-row-minimal {
  display: flex;
  margin-bottom: 24px;
}
.urgency-badge-minimal {
  font-size: 11px;
  background-color: #111111;
  color: #FFFFFF;
  padding: 4px 10px;
  border-radius: 6px;
  margin-right: 8px;
  font-weight: 600;
}
.type-badge-minimal {
  font-size: 11px;
  background-color: #F7F7F7;
  color: #111111;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 500;
  border: 1px solid rgba(0,0,0,0.04);
}

.hero-title-minimal {
  font-size: 28px;
  font-weight: 700;
  color: #111111;
  line-height: 1.3;
  display: block;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.publisher-card-minimal {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background-color: #F7F7F7;
  border-radius: 16px;
  margin-bottom: 28px;
  border: 1px solid rgba(0,0,0,0.03);
}
.avatar-minimal {
  width: 44px;
  height: 44px;
  border-radius: 22px;
  margin-right: 14px;
  background-color: #EFEFF0;
  border: 1px solid rgba(0,0,0,0.04);
}
.user-info-minimal { flex: 1; }
.name-row-minimal { display: flex; align-items: center; margin-bottom: 4px; }
.nickname-minimal { font-size: 16px; font-weight: 600; color: #111111; margin-right: 8px;}
.auth-tag-minimal {
  font-size: 10px;
  background-color: #F4F8FF;
  color: #5B8DEF; /* 极简雾蓝 */
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}
.stats-row-minimal { font-size: 13px; color: #8E8E93; }
.arrow-tiny { width: 16px; height: 16px; opacity: 0.6; }

.match-box-minimal {
  border: 1px solid rgba(0,0,0,0.06);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 28px;
}
.match-header-minimal { display: flex; align-items: center; margin-bottom: 12px; }
.match-icon-minimal { font-size: 14px; margin-right: 6px; }
.match-title-minimal { font-size: 13px; font-weight: 600; color: #111111; }
.match-reasons-minimal { display: flex; flex-wrap: wrap; gap: 8px; }
.reason-tag-minimal { font-size: 11px; color: #111111; padding: 4px 10px; border-radius: 6px; background-color: #F7F7F7; font-weight: 500; }

.info-list-minimal { margin-bottom: 20px; }
.info-row-minimal { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid rgba(0,0,0,0.03); }
.info-row-minimal:last-child { border-bottom: none; }
.info-label-minimal { font-size: 15px; color: #8E8E93; }
.info-value-minimal { font-size: 15px; color: #111111; font-weight: 500; }
.price-minimal { font-weight: 600; }

.tags-group-minimal { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 24px; }
.hashtag-minimal { font-size: 13px; color: #5B8DEF; background-color: #F4F8FF; padding: 4px 10px; border-radius: 6px; font-weight: 500; }

.divider-minimal { height: 1px; background-color: rgba(0,0,0,0.04); margin: 28px 0; }
.section-title-minimal { font-size: 18px; font-weight: 600; color: #111111; margin-bottom: 16px; display: block; }
.desc-text-minimal { font-size: 15px; color: #3C3C43; line-height: 1.6; white-space: pre-wrap; }

.bottom-bar-minimal {
  position: fixed; bottom: 0; left: 0; right: 0;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  border-top: 1px solid rgba(0,0,0,0.04);
}
.bottom-bar-inner-minimal { display: flex; align-items: center; padding: 12px 24px 12px 24px; }
.apply-btn-minimal {
  flex: 1; background-color: #111111; color: #FFFFFF;
  height: 48px; line-height: 48px; font-size: 15px; font-weight: 600;
  border-radius: 24px; margin: 0;
}
.apply-btn-minimal:active { background-color: #2B2B2B; }
.disable-btn-minimal { background-color: #F7F7F7; color: #8E8E93; border: 1px solid rgba(0,0,0,0.04); }
.favorite-pill-minimal {
  display: flex; align-items: center; justify-content: center;
  background-color: #F7F7F7; padding: 0 16px; height: 48px; border-radius: 24px; margin-left: 12px; border: 1px solid rgba(0,0,0,0.04);
}
.collect-icon-minimal { width: 20px; height: 20px; margin-right: 6px; }
.icon-text-minimal { font-size: 14px; font-weight: 500; }
</style>
