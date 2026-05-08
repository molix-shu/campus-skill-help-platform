<template>
  <view class="container" :style="{ paddingBottom: keyboardHeight + 'px' }">
    <!-- 固定在顶部的聊天提示框 -->
    <view class="chat-header">
      <text class="safe-tip">⚠️ 线下互助请注意人身安全并核实对方身份信息</text>
    </view>

    <scroll-view class="chat-history" scroll-y :scroll-top="scrollTop" scroll-with-animation>
      <view class="chat-msg system-msg">
        <text class="system-text">你们已经成功建立互助匹配，可以直接打招呼沟通~</text>
      </view>
      
      <view class="chat-msg" v-for="msg in messageList" :key="msg.id" :class="msg.senderId === myUserId ? 'myself' : 'received'">
        <!-- 对方头像 -->
        <image v-if="msg.senderId !== myUserId" class="msg-avatar" :src="msg.senderAvatar || targetAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
        
        <view class="msg-bubble" :class="msg.senderId === myUserId ? 'my-bubble' : ''" v-if="msg.contentType === 'text'">
           {{ msg.content }}
        </view>

        <!-- 缩略图模式显示图片 -->
        <image class="msg-image" v-if="msg.contentType === 'image'" :src="msg.content" mode="aspectFill" @click="previewImage(msg.content)"></image>

        <!-- 我的头像 -->
        <image v-if="msg.senderId === myUserId" class="msg-avatar" :src="myAvatar || 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'"></image>
      </view>

      <view style="height: 10px;"></view>
    </scroll-view>
    
    <!-- 键盘输入区 -->
    <view class="chat-input-bar" :style="{ bottom: keyboardHeight + 'px' }">
      <input class="msg-input" 
             placeholder="输入消息..." 
             v-model="inputText" 
             @confirm="sendText" 
             @focus="emojiVisible = false"
             :adjust-position="false"
             confirm-type="send" />
      <view class="action-btn-group">
        <button v-show="inputText.trim() !== ''" class="send-btn btn-active" @click="sendText">发送</button>
        <view v-show="inputText.trim() === ''" class="action-icons">
          <image class="action-icon" src="/static/emoji-icon.png" @click="toggleEmoji"></image>
          <image class="action-icon" src="/static/add-icon.png" @click="chooseImage"></image>
        </view>
      </view>
    </view>

    <!-- Emoji 选择面板 -->
    <view v-if="emojiVisible" class="emoji-panel" :style="{ bottom: keyboardHeight + 86 + 'px' }">
      <scroll-view scroll-y class="emoji-scroll">
        <view class="emoji-grid">
          <text class="emoji-item" v-for="e in emojiList" :key="e" @click="insertEmoji(e)">{{ e }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/request.js';

export default {
  data() {
    return {
      orderId: '',
      targetName: '同学',
      targetAvatar: '',
      myUserId: null,
      myAvatar: '',
      messageList: [],
      inputText: '',
      scrollTop: 99999,
      keyboardHeight: 0,
      pollTimer: null,
      emojiVisible: false,
      emojiList: [
        '😀','😂','😊','😇','😘','😜','🤣','😳','🙏','👍',
        '👋','🙌','🙋','🤝','💪','🎉','🌟','❤️','🔥','💥',
        '🎂','🍓','💦','💤','🚀','🌈','☀️','🌙','⭐','🍎',
        '📚','💡','🎧','🎮','📸','🌻','🐱','🐶','🍌','🍕'
      ]
    }
  },
  onLoad(options) {
    this.orderId = options.orderId;
    this.targetName = decodeURIComponent(options.targetName || '对方同学');
    this.targetAvatar = decodeURIComponent(options.targetAvatar || '');
    
    const user = uni.getStorageSync('userInfo');
    if (user) {
      this.myUserId = user.id;
      this.myAvatar = user.avatarUrl;
    }
    
    uni.setNavigationBarTitle({
      title: `与 ${this.targetName} 的沟通`
    });

    // 解决原生键盘把导航栏顶飞的硬核手段：拦截系统键盘弹起事件，并给容器塞底边距压缩界面
    uni.onKeyboardHeightChange(res => {
      this.keyboardHeight = res.height;
      this.scrollToBottom();
    });

    this.fetchMessages();
    this.pollTimer = setInterval(() => {
       this.fetchMessages();
    }, 2000);
  },
  onUnload() {
    if (this.pollTimer) clearInterval(this.pollTimer);
    uni.offKeyboardHeightChange();
  },
  methods: {
    fetchMessages() {
      request({
        url: '/api/chat/list?orderId=' + this.orderId,
        method: 'GET'
      }).then(res => {
        if (res.length !== this.messageList.length) {
          this.messageList = res;
          this.scrollToBottom();
        }
      }).catch(err => {});
    },
    toggleEmoji() {
      this.emojiVisible = !this.emojiVisible;
    },
    insertEmoji(e) {
      this.inputText += e;
    },
    chooseImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          const tempPath = res.tempFilePaths[0];
          uni.getFileSystemManager().readFile({
            filePath: tempPath,
            encoding: 'base64',
            success: dataRes => {
              const base64Str = 'data:image/jpeg;base64,' + dataRes.data;
              this.postMessage('image', base64Str);
            }
          });
        }
      });
    },
    sendText() {
      if (this.inputText.trim() === '') return;
      const text = this.inputText;
      this.inputText = '';
      this.postMessage('text', text);
    },
    postMessage(type, content) {
      // 乐观 UI：先把消息假装塞进屏幕列表里
      const fakeMsg = {
        id: 'temp_' + Date.now(),
        senderId: this.myUserId,
        contentType: type,
        content: content,
        senderAvatar: this.myAvatar
      };
      this.messageList.push(fakeMsg);
      this.scrollToBottom();
      uni.vibrateShort({ type: 'light' });

      // 正式告知后端引擎
      request({
        url: '/api/chat/send',
        method: 'POST',
        data: {
          orderId: this.orderId,
          senderId: this.myUserId,
          contentType: type,
          content: content
        }
      }).then(() => {
        this.fetchMessages();
      }).catch(err => {
        uni.showToast({ title: '消息发送失败', icon: 'none' });
      });
    },
    previewImage(url) {
      uni.previewImage({ urls: [url] });
    },
    scrollToBottom() {
      // 通过巧妙的 nextTick 引发 scroll-view 视图底层强制更新到底
      this.$nextTick(() => {
        setTimeout(() => {
           this.scrollTop = this.scrollTop + 999;
        }, 50);
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
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-sizing: border-box;
  transition: padding-bottom 0.1s ease-out; /* 关键：给键盘腾地方缩骨功 */
}

.chat-history {
  flex: 1;
  padding: 16px;
  box-sizing: border-box;
  overflow-y: auto;
}
.chat-header {
  padding: 10px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}
.safe-tip {
  font-size: 11px;
  color: #8E8E93;
  background-color: #E3E3E8;
  padding: 4px 12px;
  border-radius: 12px;
}
.chat-msg {
  display: flex;
  margin-bottom: 24px;
  align-items: flex-start;
}
.system-msg {
  justify-content: center;
  margin-bottom: 24px;
}
.system-text {
  font-size: 12px;
  color: #AEAEB2;
}

/* 接收方消息（左侧） */
.received {
  justify-content: flex-start;
}
.received .msg-avatar {
  margin-right: 12px;
}
.received .msg-bubble {
  background-color: #FFF;
  color: #1D1D1F;
  border-top-left-radius: 4px;
}

/* 我的消息（右侧） */
.myself {
  justify-content: flex-end;
}
.myself .msg-avatar {
  margin-left: 12px;
}
.my-bubble {
  background-color: #007AFF !important;
  color: #FFF !important;
  border-top-right-radius: 4px;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  background-color: #E3E3E8;
  flex-shrink: 0;
}
.msg-bubble {
  max-width: 65%;
  padding: 12px 16px;
  border-radius: 20px;
  font-size: 15px;
  line-height: 1.5;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  word-wrap: break-word;
}
.msg-image {
  max-width: 140px;
  height: 140px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

/* 底部输入框（固定在 flex 的底端） */
.chat-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  padding: 12px 20px 30px 20px;
  background-color: #F5F5F7;
  border-top: 1px solid #E3E3E8;
  align-items: center;
  z-index: 100;
}
.action-btn-group {
  margin-left: 12px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.action-icons {
  display: flex;
  align-items: center;
}
.action-icon {
  width: 28px;
  height: 28px;
  margin-left: 12px;
}
/* Emoji 面板 */
.emoji-panel {
  position: fixed;
  left: 0;
  right: 0;
  background: #FFF;
  border-top: 1px solid #E3E3E8;
  padding: 12px;
  z-index: 99;
}
.emoji-scroll {
  height: 150px;
}
.emoji-grid {
  display: flex;
  flex-wrap: wrap;
}
.emoji-item {
  font-size: 28px;
  width: 14.28%;
  text-align: center;
  padding: 6px 0;
}
.msg-input {
  flex: 1;
  background-color: #FFF;
  min-height: 44px;
  border-radius: 8px;
  padding: 0 16px;
  font-size: 15px;
  color: #1D1D1F;
}
.send-btn {
  width: 70px;
  height: 40px;
  border-radius: 6px;
  background-color: #E3E3E8;
  color: #8E8E93;
  font-size: 15px;
  font-weight: 600;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0;
  transition: all 0.3s;
}
.send-btn.btn-active {
  background-color: #007AFF;
  color: #FFF;
}
</style>
