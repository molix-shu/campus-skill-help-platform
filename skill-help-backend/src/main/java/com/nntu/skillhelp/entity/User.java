package com.nntu.skillhelp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    // 对应主键ID，自增
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String openid;       // 微信openid
    private String nickname;     // 用户昵称
    private String avatarUrl;    // 头像URL
    private String realName;     // 真实姓名
    private String college;      // 所属学院
    private String major;        // 所属专业
    private String studentId;    // 学号
    private String studentCardUrl; // 学生证照片
    private String authMessage;  // 审核驳回备注
    private Integer role;        // 角色：0普通用户, 1管理员
    private Integer authStatus;  // 认证状态 (0未认证, 1等)
    private Integer creditScore; // 信用分
    private String creditLevel;  // 信用等级
    private Integer successCount;// 互助成功次数
    private Integer defaultCount;// 爽约违约次数
    private Integer complaintCount;// 被投诉成立次数
    private LocalDateTime createTime; 
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
