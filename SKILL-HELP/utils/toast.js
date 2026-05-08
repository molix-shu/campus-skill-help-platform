/**
 * 统一弹窗工具 — 带柔和动画、合理 duration
 * 替代直接调用 uni.showToast，统一控制弹窗体验
 */

/**
 * 成功提示（绿色 ✓ 图标）
 * @param {string} title - 提示文字
 * @param {number} duration - 显示时长(毫秒)，默认 2000
 */
export const showSuccess = (title, duration = 2000) => {
  uni.showToast({
    title,
    icon: 'success',
    duration,
    mask: false
  });
};

/**
 * 普通信息提示（无图标）
 * @param {string} title - 提示文字
 * @param {number} duration - 显示时长(毫秒)，默认 2500
 */
export const showInfo = (title, duration = 2500) => {
  uni.showToast({
    title,
    icon: 'none',
    duration,
    mask: false
  });
};

/**
 * 警告/错误提示（无图标，较长展示）
 * @param {string} title - 提示文字
 * @param {number} duration - 显示时长(毫秒)，默认 3000
 */
export const showError = (title, duration = 3000) => {
  uni.showToast({
    title,
    icon: 'none',
    duration,
    mask: false
  });
};
