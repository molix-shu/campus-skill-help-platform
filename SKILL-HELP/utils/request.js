// 从你的 ipconfig 中获取的 WLAN IPv4 地址
export const BASE_URL = 'http://10.8.177.46:8088';

/**
 * 封装微信小程序的网络请求 (Axios-like 风格)
 */
export const request = (options) => {
    return new Promise((resolve, reject) => {
        uni.request({
            url: BASE_URL + options.url,
            method: options.method || 'GET',
            data: options.data || {},
            header: {
                'Content-Type': 'application/json',
                ...options.header
            },
            success: (res) => {
                // 判断后端自定义的业务状态码
                if (res.data.code === 200) {
                    resolve(res.data.data);
                } else {
                    // 必须先关闭 showLoading，否则 showToast 会被它覆盖
                    uni.hideLoading();
                    setTimeout(() => {
                        uni.showToast({
                            title: res.data.msg || '请求失败',
                            icon: 'none',
                            duration: 2500
                        });
                    }, 100);
                    reject(res.data);
                }
            },
            fail: (err) => {
                uni.hideLoading();
                setTimeout(() => {
                    uni.showToast({
                        title: '网络连接异常',
                        icon: 'none',
                        duration: 2500
                    });
                }, 100);
                reject(err);
            }
        });
    });
};
