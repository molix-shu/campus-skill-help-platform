package com.nntu.skillhelp.dto;

import lombok.Data;

@Data
public class WechatLoginDTO {
    // 微信小程序 wx.login 拿到的 code
    private String code;       
    private String nickname;
    private String avatarUrl;
}
