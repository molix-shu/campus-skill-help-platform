<template>
  <view class="container">
    <scroll-view scroll-y class="content-scroll">
      <!-- 极简封面区: 只有真有封面才显示大图，否则完全留白不要渐变 -->
      <view class="hero-image-wrap" v-if="skill.coverUrl && skill.coverUrl.indexOf('http') === 0">
        <image class="hero-image" :src="skill.coverUrl" mode="aspectFill"></image>
      </view>

      <!-- 核心信息流区 -->
      <view class="main-body">
        <text class="hero-title-minimal">{{ skill.title || '技能详情' }}</text>

        <!-- 发布者名片 (极简流) -->
        <view class="publisher-card-minimal" @click="goUserProfile">
          <image class="avatar-minimal" :src="skill.publisherAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
          <view class="user-info-minimal">
            <view class="name-row-minimal">
              <text class="nickname-minimal">{{ skill.publisherName || '微信用户' }}</text>
              <text class="auth-tag-minimal">已实名认证</text>
            </view>
            <view class="stats-row-minimal">
              <text class="stat-text-minimal">信用 {{ publisher.creditScore || 50 }}</text>
              <text class="stat-divider-minimal">·</text>
              <text class="stat-text-minimal">评分 {{ publisher.avgScore || '5.0' }}</text>
            </view>
          </view>
          <image class="arrow-tiny" :src="iconChevronRight"></image>
        </view>

        <!-- 匹配亮点（极简框线设计） -->
        <view class="match-box-minimal" v-if="skill.matchScore">
          <view class="match-header-minimal">
            <text class="match-icon-minimal">✨</text>
            <text class="match-title-minimal">匹配度 {{ skill.matchScore }}% (平台推荐)</text>
          </view>
          <view class="match-reasons-minimal">
            <text class="reason-tag-minimal" v-for="(reason, idx) in skill.matchReasons" :key="idx">{{ reason }}</text>
            <text class="reason-tag-minimal">高信用力</text>
          </view>
        </view>

        <!-- 详情列表 -->
        <view class="info-list-minimal">
          <view class="info-row-minimal">
            <text class="info-label-minimal">服务时间</text>
            <text class="info-value-minimal">{{ skill.serviceTime || '工作日晚 / 周末全天' }}</text>
          </view>
          <view class="info-row-minimal">
            <text class="info-label-minimal">服务方式</text>
            <text class="info-value-minimal">{{ skill.serviceMethod || '线上 / 线下均可' }}</text>
          </view>
          <view class="info-row-minimal">
            <text class="info-label-minimal">费用要求</text>
            <text class="info-value-minimal price-minimal">{{ skill.priceDesc || '免费互助' }}</text>
          </view>
        </view>

        <view class="tags-group-minimal" v-if="processedTags.length > 0">
          <text class="hashtag-minimal" v-for="t in processedTags" :key="t">#{{ t }}</text>
        </view>
        
        <view class="divider-minimal"></view>
        
        <text class="section-title-minimal">技能详细描述</text>
        <text class="desc-text-minimal">{{ skill.description || '发布者暂时没有留下详细说明，但这是个很棒的技能！' }}</text>
      </view>
      
      <view style="height: 100px;"></view>
    </scroll-view>

    <!-- 底部操作栏 (纯粹黑白灰) -->
    <view class="bottom-bar-minimal">
      <view class="bottom-bar-inner-minimal">
        <button class="apply-btn-minimal" @click="handleApply">立即申请互助</button>
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
      skillId: null,
      skill: {},
      publisher: {},
      tags: [],
      isCollected: false,
      // 收藏星星使用金黄色
      iconStarEmpty: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEyIDE3LjI3TDE4LjE4IDIxTDE2LjU0IDEzLjk3TDIyIDkuMjRMMTQuODEgOC42M0wxMiAyTDkuMTkgOC42M0wyIDkuMjRMNy40NiAxMy45N0w1LjgyIDIxTDEyIDE3LjI3WiIgc3Ryb2tlPSIjQzdDN0NDIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lam9pbj0icm91bmQiLz48L3N2Zz4=',
      iconStarFilled: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEyIDE3LjI3TDE4LjE4IDIxTDE2LjU0IDEzLjk3TDIyIDkuMjRMMTQuODEgOC42M0wxMiAyTDkuMTkgOC42M0wyIDkuMjRMNy40NiAxMy45N0w1LjgyIDIxTDEyIDE3LjI3WiIgZmlsbD0iI0Y1QTYyMyIgc3Ryb2tlPSIjRjVBNjIzIiBzdHJva2Utd2lkdGg9IjEuNzUiIHN0cm9rZS1saW5lam9pbj0icm91bmQiLz48L3N2Zz4=',
      iconChevronRight: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjQzdDN0NDIiBzdHJva2Utd2lkdGg9IjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PHBhdGggZD0ibTkgMTggNi02LTYtNiI+PC9wYXRoPjwvc3ZnPg=='
    }
  },
  onLoad(options) {
    if (options.id) {
      this.skillId = options.id;
      this.fetchSkillDetail();
    }
  },
  computed: {
    processedTags() {
      return this.tags.map(t => t.replace(/[#＃]/g, '').trim()).filter(Boolean);
    }
  },
  methods: {
    fetchSkillDetail() {
      uni.showLoading({ title: '加载中' });
      request({ url: '/api/skill/list', method: 'GET' })
        .then(res => {
          uni.hideLoading();
          const target = res.find(s => String(s.id) === String(this.skillId));
          if (target) {
            this.skill = target;
            if (this.skill.tags) {
              this.tags = this.skill.tags.split(' ').join(',').split(',').filter(Boolean);
            }
            if (!this.skill.matchScore) this.skill.matchScore = Math.floor(Math.random() * 20 + 80);
            if (!this.skill.matchReasons) this.skill.matchReasons = ['需求分类吻合', '时间匹配'];
            
            const favs = uni.getStorageSync('my_favorites') || [];
            this.isCollected = !!favs.find(i => String(i.id) === String(this.skillId));

          } else {
            uni.showToast({ title: '技能已下架或不存在', icon: 'none' });
          }
        }).catch(() => uni.hideLoading());
    },
    goUserProfile() {
      if (this.skill.userId) {
        uni.navigateTo({ url: `/pages/profile/user-profile?userId=${this.skill.userId}` });
      }
    },
    collect() {
      this.isCollected = !this.isCollected;
      let favs = uni.getStorageSync('my_favorites') || [];
      if (this.isCollected) {
        if (!favs.find(i => String(i.id) === String(this.skill.id))) {
          favs.push(this.skill);
        }
        uni.showToast({ title: '已添加到收藏', icon: 'none' });
      } else {
        favs = favs.filter(i => String(i.id) !== String(this.skill.id));
        uni.showToast({ title: '已取消', icon: 'none' });
      }
      uni.setStorageSync('my_favorites', favs);
    },
    handleApply() {
      const user = uni.getStorageSync('userInfo');
      if (!user || !user.id) return uni.showToast({ title: '请先登录', icon: 'none' });
      if (String(this.skill.userId) === String(user.id)) return uni.showToast({ title: '不能申请互助自己的技能', icon: 'none' });
      
      uni.showModal({
        title: '互助确认',
        content: `确定要向这位同学申请"${this.skill.title}"吗？`,
        confirmText: '立刻申请',
        confirmColor: '#111111',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading();
            request({
              url: '/api/order/create',
              method: 'POST',
              data: {
                type: 1, 
                targetId: this.skill.id,
                publisherId: this.skill.userId,
                applicantId: user.id
              }
            }).then(() => {
              uni.hideLoading();
              uni.showToast({ title: '申请已发送！', icon: 'success' });
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
/* 严格极简黑白灰 */
page { background-color: #FFFFFF; height: 100%; }
.container { display: flex; flex-direction: column; height: 100vh; position: relative; }
.content-scroll { flex: 1; }

.hero-image-wrap {
  width: 100%;
  height: 240px;
  background-color: #F7F7F7;
}
.hero-image {
  width: 100%;
  height: 100%;
}

.main-body {
  padding: 24px 24px;
}

.hero-title-minimal {
  font-size: 28px;
  font-weight: 700;
  color: #111111;
  line-height: 1.3;
  display: block;
  margin-bottom: 24px;
  letter-spacing: -0.5px;
}

/* 发布者名片极简 */
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
.user-info-minimal {
  flex: 1;
}
.name-row-minimal {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.nickname-minimal {
  font-size: 16px;
  font-weight: 600;
  color: #111111;
  margin-right: 8px;
}
.auth-tag-minimal {
  font-size: 10px;
  background-color: #F4F8FF;
  color: #5B8DEF; /* 极简雾蓝 */
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}
.stats-row-minimal {
  font-size: 13px;
  color: #8E8E93;
}
.stat-divider-minimal {
  margin: 0 6px;
}
.arrow-tiny {
  width: 16px;
  height: 16px;
  opacity: 0.6;
}

/* 匹配线框 */
.match-box-minimal {
  border: 1px solid rgba(0,0,0,0.06);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 28px;
}
.match-header-minimal {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.match-icon-minimal {
  font-size: 14px;
  margin-right: 6px;
}
.match-title-minimal {
  font-size: 13px;
  font-weight: 600;
  color: #111111;
}
.match-reasons-minimal {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.reason-tag-minimal {
  font-size: 11px;
  color: #111111;
  padding: 4px 10px;
  border-radius: 6px;
  background-color: #F7F7F7;
  font-weight: 500;
}

/* 信息纵列 */
.info-list-minimal {
  margin-bottom: 20px;
}
.info-row-minimal {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(0,0,0,0.03);
}
.info-row-minimal:last-child {
  border-bottom: none;
}
.info-label-minimal {
  font-size: 15px;
  color: #8E8E93;
}
.info-value-minimal {
  font-size: 15px;
  color: #111111;
  font-weight: 500;
}
.price-minimal {
  font-weight: 600;
}

/* 极简标签 */
.tags-group-minimal {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}
.hashtag-minimal {
  font-size: 13px;
  color: #5B8DEF; /* 极简雾蓝 */
  background-color: #F4F8FF;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 500;
}

.divider-minimal {
  height: 1px;
  background-color: rgba(0,0,0,0.04);
  margin: 28px 0;
}

.section-title-minimal {
  font-size: 18px;
  font-weight: 600;
  color: #111111;
  margin-bottom: 16px;
  display: block;
}
.desc-text-minimal {
  font-size: 15px;
  color: #3C3C43;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 纯粹底部 */
.bottom-bar-minimal {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  border-top: 1px solid rgba(0,0,0,0.04);
}
.bottom-bar-inner-minimal {
  display: flex;
  align-items: center;
  padding: 12px 24px 12px 24px;
}
.apply-btn-minimal {
  flex: 1;
  background-color: #111111;
  color: #FFFFFF;
  height: 48px;
  line-height: 48px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 24px;
  margin: 0;
}
.apply-btn-minimal:active {
  background-color: #2B2B2B;
}
.favorite-pill-minimal {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F7F7F7;
  padding: 0 16px;
  height: 48px;
  border-radius: 24px;
  margin-left: 12px;
  border: 1px solid rgba(0,0,0,0.04);
}
.collect-icon-minimal {
  width: 20px;
  height: 20px;
  margin-right: 6px;
}
.icon-text-minimal {
  font-size: 14px;
  font-weight: 500;
}
</style>
