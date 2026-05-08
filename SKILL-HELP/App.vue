<script>
import { request } from './utils/request.js';

// 全局劫持 uni.showToast，统一延长弹窗展示时间
const _originalShowToast = uni.showToast.bind(uni);
uni.showToast = function(options = {}) {
	// 如果调用方没有显式指定 duration，则根据 icon 类型自动设置合理时长
	if (options.duration === undefined) {
		if (options.icon === 'success') {
			options.duration = 2000; // 成功提示 2 秒
		} else {
			options.duration = 2500; // 错误/信息提示 2.5 秒，让用户有时间阅读
		}
	}
	return _originalShowToast(options);
};

export default {
	onLaunch: function() {
		console.log('App Launch');
		
		// 开启全局轮询，每 5 秒刷新一次消息池（用于解决消息红点全局提醒功能）
		setInterval(() => {
			const user = uni.getStorageSync('userInfo');
			if (user && user.id) {
				request({
					url: '/api/order/my?userId=' + user.id,
					method: 'GET'
				}).then(res => {
					let unreadCount = 0;
					res.forEach(o => {
						if (o.status === 0 && o.publisherId === user.id) unreadCount++;
						else if (o.status === 1) unreadCount++;
					});
					if (unreadCount > 0) {
						uni.setTabBarBadge({ index: 2, text: String(unreadCount) }).catch(e=>{});
					} else {
						uni.removeTabBarBadge({ index: 2 }).catch(e=>{});
					}
				}).catch(e=>{});
			}
		}, 5000);
	},
	onShow: function() {
		console.log('App Show')
	},
	onHide: function() {
		console.log('App Hide')
	}
}
</script>

<style>
	/*每个页面公共css */
	
	/* 解决全局过渡动画的基础底色 */
	page {
		background-color: #F5F5F7;
	}

	/* 解决小程序 button 默认带伪元素 border 导致边缘样式问题 */
	button::after {
		border: none;
	}
</style>
