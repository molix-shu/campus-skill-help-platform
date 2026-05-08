<template>
  <view class="container">
    <!-- 顶部信息 -->
    <view class="header">
      <view style="display: flex; align-items: center;">
        <image class="header-icon-svg" :src="icons.edit"></image>
        <text class="title">互助评价</text>
      </view>
      <text class="subtitle">你的真实反馈让校园互助更可信~</text>
    </view>

    <!-- 三维度星级评分 -->
    <view class="form-card">
      <view class="card-title">互助综合评分</view>

      <view v-for="dim in dimensions" :key="dim.key" class="score-row">
        <view class="score-left">
          <image class="dim-icon-svg" :src="icons[dim.iconKey]"></image>
          <text class="dim-label">{{ dim.label }}</text>
        </view>
        <view class="stars">
          <image 
            class="star-svg" 
            v-for="i in 5" :key="i"
            :src="i <= form[dim.key] ? icons.starFilled : icons.starEmpty"
            @click="form[dim.key] = i"
          ></image>
        </view>
        <text class="score-tag" :class="'tag-' + form[dim.key]">{{ scoreLabels[form[dim.key]] }}</text>
      </view>
    </view>

    <!-- 快捷标签 -->
    <view class="form-card">
      <view class="card-title">你想夸他什么？（可多选）</view>
      <view class="tag-list">
        <view 
          v-for="tag in quickTags" :key="tag" 
          class="quick-tag" 
          :class="{ selected: selectedTags.includes(tag) }"
          @click="toggleTag(tag)"
        >{{ tag }}</view>
      </view>
    </view>

    <!-- 文字评价 -->
    <view class="form-card">
      <view class="card-title">留下你的评价</view>
      <textarea 
        class="review-textarea" 
        v-model="form.content" 
        placeholder="这位同学值得5星吗？写点什么夸夸他吧..." 
        placeholder-class="ph-color"
        maxlength="300"
      ></textarea>
      <text class="word-count">{{ form.content.length }}/300</text>
    </view>

    <!-- 上传示证图片 -->
    <view class="form-card">
      <view class="card-title">上传示证图片（选填）</view>
      <view class="image-upload-area" @click="chooseImage">
        <view v-if="!form.imageUrl" class="upload-placeholder">
          <image class="upload-icon-svg" :src="icons.camera"></image>
          <text class="upload-text">点击上传图片</text>
          <text class="upload-hint">如成果照、评分截图等</text>
        </view>
        <image v-else class="preview-image" :src="form.imageUrl" mode="aspectFill" @click.stop="previewImg"></image>
        <view v-if="form.imageUrl" class="remove-img" @click.stop="form.imageUrl = ''">✕</view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="footer-action">
      <button class="submit-btn" :disabled="isSubmitting" @click="submitEval">
        {{ isSubmitting ? '提交中...' : '提交评价 ✓' }}
      </button>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      orderId: null,
      toUserId: null,
      isSubmitting: false,
      form: {
        score: 5,
        scoreSpeed: 5,
        scoreAttitude: 5,
        scoreQuality: 5,
        content: '',
        imageUrl: ''
      },
      selectedTags: [],
      quickTags: ['响应超快', '专业靠谱', '耐心细致', '态度友好', '准时准点', '责任到位', '物超所值'],
      dimensions: [
        { key: 'scoreSpeed',    iconKey: 'bolt',  label: '响应速度' },
        { key: 'scoreAttitude', iconKey: 'smile', label: '服务态度' },
        { key: 'scoreQuality',  iconKey: 'target', label: '专业水平' }
      ],
      scoreLabels: ['', '很差', '较差', '一般', '不错', '完美'],
      icons: {
        starFilled: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEyIDE3LjI3TDE4LjE4IDIxTDE2LjU0IDEzLjk3TDIyIDkuMjRMMTQuODEgOC42M0wxMiAyTDkuMTkgOC42M0wyIDkuMjRMNy40NiAxMy45N0w1LjgyIDIxTDEyIDE3LjI3WiIgZmlsbD0iI0Y1QTYyMyIgc3Ryb2tlPSIjRjVBNjIzIiBzdHJva2Utd2lkdGg9IjEuNSIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIvPjwvc3ZnPg==',
        starEmpty: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEyIDE3LjI3TDE4LjE4IDIxTDE2LjU0IDEzLjk3TDIyIDkuMjRMMTQuODEgOC42M0wxMiAyTDkuMTkgOC42M0wyIDkuMjRMNy40NiAxMy45N0w1LjgyIDIxTDEyIDE3LjI3WiIgc3Ryb2tlPSIjRTNFM0U4IiBzdHJva2Utd2lkdGg9IjEuNSIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIvPjwvc3ZnPg==',
        edit: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTExIDRIM0MyLjQ0NzczIDQgMiA0LjQ0NzczIDIgNXYxOEMyIDIzLjU1MjMgMi40NDc3MyAyNCAzIDI0SDIxQzIxLjU1MjMgMjQgMjIgMjMuNTUyMyAyMiAyM1YxNSIgc3Ryb2tlPSIjMWQxZDFmIiBzdHJva2Utd2lkdGg9IjEuNSIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIi8+PHBhdGggZD0iTTE4LjUgMi41QzE5LjMzIDEuNjcgMjAuNjcgMS42NyAyMS41IDIuNUMyMi4zMyAzLjMzIDIyLjMzIDQuNjcgMjEuNSA1LjVMMTIgMTVMOCAxNkw5IDExTDE4LjUgMi41WiIgc3Ryb2tlPSIjMWQxZDFmIiBzdHJva2Utd2lkdGg9IjEuNSIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIvPjwvc3ZnPg==',
        bolt: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTEzIDJMMyAxNkgxMkwxMSAyMkwyMSA4SDEyTDEzIDJaIiBzdHJva2U9IiMxZDFkMWYiIHN0cm9rZS13aWR0aD0iMS41IiBzdHJva2UtbGluZWpvaW49InJvdW5kIi8+PC9zdmc+',
        smile: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTAiIHN0cm9rZT0iIzFkMWQxZiIgc3Ryb2tlLXdpZHRoPSIxLjUiLz48cGF0aCBkPSJNOCAxNFM5IDE2IDEyIDE2UzE2IDE0IDE2IDE0IiBzdHJva2U9IiMxZDFkMWYiIHN0cm9rZS13aWR0aD0iMS41IiBzdHJva2UtbGluZWNhcD0icm91bmQiLz48cGF0aCBkPSJNOSA5SDl2LjAxIiBzdHJva2U9IiMxZDFkMWYiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIi8+PHBhdGggZD0iTTE1IDlIMTV2LjAxIiBzdHJva2U9IiMxZDFkMWYiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIi8+PC9zdmc+',
        target: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMTAiIHN0cm9rZT0iIzFkMWQxZiIgc3Ryb2tlLXdpZHRoPSIxLjUiLz48Y2lyY2xlIGN4PSIxMiIgY3k9IjEyIiByPSI2IiBzdHJva2U9IiMxZDFkMWYiIHN0cm9rZS13aWR0aD0iMS41Ii8+PGNpcmNsZSBjeD0iMTIiIGN5PSIxMiIgcj0iMiIgZmlsbD0iIzFkMWQxZiIvPjwvc3ZnPg==',
        camera: 'data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgMjQgMjQiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTIyIDE5QzIyIDE5LjU1MjMgMjEuNTUyMyAyMCAyMSAyMEgzQzIuNDQ3NzIgMjAgMiAxOS41NTIzIDIgMTlWNUMyIDQuNDQ3NzIgMi40NDc3MiA0IDMgNEg3TDEwIDJIMTRMMTcgNEgyMUMyMS41NTIzIDQgMjIgNC40NDc3MiAyMiA1VjE5WiIgc3Ryb2tlPSIjMWQxZDFmIiBzdHJva2Utd2lkdGg9IjEuNSIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIvPjxjaXJjbGUgY3g9IjEyIiBjeT0iMTIiIHI9IjQiIHN0cm9rZT0iIzFkMWQxZiIgc3Ryb2tlLXdpZHRoPSIxLjUiLz48L3N2Zz4='
      }
    }
  },
  onLoad(options) {
    this.orderId = options.orderId;
    this.toUserId = options.toUserId;
  },
  computed: {
    // 三维平均分作为总体 score
    avgScore() {
      return Math.round((this.form.scoreSpeed + this.form.scoreAttitude + this.form.scoreQuality) / 3);
    }
  },
  methods: {
    toggleTag(tag) {
      const idx = this.selectedTags.indexOf(tag);
      if (idx >= 0) {
        this.selectedTags.splice(idx, 1);
      } else {
        this.selectedTags.push(tag);
      }
    },
    chooseImage() {
      uni.chooseImage({
        count: 1,
        success: res => {
          const tempPath = res.tempFilePaths[0];
          uni.getFileSystemManager().readFile({
            filePath: tempPath,
            encoding: 'base64',
            success: dataRes => {
              this.form.imageUrl = 'data:image/jpeg;base64,' + dataRes.data;
            }
          });
        }
      });
    },
    previewImg() {
      if (this.form.imageUrl) {
        uni.previewImage({ urls: [this.form.imageUrl] });
      }
    },
    submitEval() {
      if (!this.form.content && this.selectedTags.length === 0) {
        return uni.showToast({ title: '请选择标签或填写评价内容', icon: 'none' });
      }
      if (this.isSubmitting) return;
      this.isSubmitting = true;
      uni.showLoading({ title: '提交中...' });

      // 合并快捷标签到文字内容的前面
      let fullContent = this.form.content;
      if (this.selectedTags.length > 0) {
        fullContent = '【' + this.selectedTags.join('・') + '】' + (fullContent ? '\n' + fullContent : '');
      }

      const user = uni.getStorageSync('userInfo');
      request({
        url: '/api/eval/submit',
        method: 'POST',
        data: {
          orderId: this.orderId,
          fromUserId: user.id,
          toUserId: this.toUserId,
          score: this.avgScore,
          scoreSpeed: this.form.scoreSpeed,
          scoreAttitude: this.form.scoreAttitude,
          scoreQuality: this.form.scoreQuality,
          content: fullContent,
          imageUrl: this.form.imageUrl || null
        }
      }).then(() => {
        uni.hideLoading();
        uni.showToast({ title: '评价成功！感谢你的反馈 ✓', icon: 'success', duration: 2000 });
        // 主动刷新自己的 userInfo 缓存，确保回到"我的"页面时信用分即时显示最新值
        if (user && user.id) {
          request({ url: '/api/user/info?userId=' + user.id }).then(latestUser => {
            if (latestUser) uni.setStorageSync('userInfo', latestUser);
          }).catch(() => {});
        }
        setTimeout(() => { uni.navigateBack(); }, 2000);
      }).catch(() => {
        uni.hideLoading();
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
  padding: 24px 20px 120px 20px;
}
.header { margin-bottom: 28px; }
.title { font-size: 30px; font-weight: 700; color: #1D1D1F; display: block; }
.subtitle { font-size: 14px; color: #8E8E93; margin-top: 6px; display: block; }

.form-card {
  background-color: #FFFFFF;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid rgba(0,0,0,0.03);
  box-shadow: 0 4px 24px rgba(0,0,0,0.015);
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 16px;
}

/* 评分行 — 安卓兼容：改为两行布局，上面 label，下面 stars + tag */
.score-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F5F5F7;
}
.score-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}
.score-left {
  display: flex;
  align-items: center;
  width: 80px;
  flex-shrink: 0;
}
.header-icon-svg { width: 26px; height: 26px; margin-right: 8px; }
.dim-icon-svg { width: 18px; height: 18px; margin-right: 6px; }
.dim-label { font-size: 13px; color: #3C3C43; }
.stars { flex: 1; display: flex; align-items: center; justify-content: center; min-width: 0; }
.star-svg { width: 24px; height: 24px; margin: 0 2px; transition: transform 0.1s; flex-shrink: 0; }
.star-svg:active { transform: scale(1.15); }
.score-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  margin-left: 4px;
  flex-shrink: 0;
  white-space: nowrap;
}
.tag-1, .tag-2 { background: #F7F7F7; color: #8E8E93; border: 1px solid rgba(0,0,0,0.04); }
.tag-3 { background: #F7F7F7; color: #111111; border: 1px solid rgba(0,0,0,0.04); }
.tag-4, .tag-5 { background: #F5A623; color: #FFFFFF; }

/* 快捷标签 */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.quick-tag {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  background-color: #F5F5F7;
  color: #636366;
  border: 1.5px solid transparent;
  transition: all 0.2s;
}
.quick-tag.selected {
  background-color: #111111;
  color: #FFFFFF;
  border-color: #111111;
  font-weight: 600;
}

/* 文字评价 */
.review-textarea {
  width: 100%;
  height: 110px;
  font-size: 15px;
  color: #1D1D1F;
  line-height: 1.6;
  box-sizing: border-box;
}
.ph-color { color: #C7C7CC; }
.word-count { font-size: 11px; color: #AEAEB2; text-align: right; display: block; margin-top: 8px; }

/* 上传区 */
.image-upload-area {
  border: 2px dashed #E3E3E8;
  border-radius: 16px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.upload-icon-svg { width: 44px; height: 44px; margin-bottom: 8px; }
.upload-text { font-size: 14px; color: #636366; }
.upload-hint { font-size: 12px; color: #AEAEB2; margin-top: 2px; }
.preview-image {
  width: 100%;
  height: 120px;
}
.remove-img {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 12px;
  background: rgba(0,0,0,0.45);
  color: #FFF;
  font-size: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 底部提交 */
.footer-action {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  padding: 16px 24px 34px 24px;
  background: rgba(245,245,247,0.92);
  backdrop-filter: blur(20px);
}
.submit-btn {
  background: #111111;
  color: #FFFFFF;
  height: 54px;
  border-radius: 27px;
  font-size: 17px;
  font-weight: 600;
  display: flex;
  justify-content: center;
  align-items: center;
}
.submit-btn:disabled {
  background: #F7F7F7;
  color: #8E8E93;
  border: 1px solid rgba(0,0,0,0.04);
}
</style>
