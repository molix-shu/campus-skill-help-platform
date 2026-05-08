package com.nntu.skillhelp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("help_order")
public class HelpOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;     // 订单流水号
    private Integer type;       // 1求助者申请技能，2技能者接单需求
    private Long targetId;      // 对应的技能ID或需求ID
    private Long publisherId;   // 发布者ID
    private Long applicantId;   // 申请者ID
    private Integer status;     // 0待同意，1进行中，2待评价，3已关闭
    private String cancelReason;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String targetTitle;       // 冗余展示：技能或需求的标题
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String targetUserName;    // 冗余展示：对方的昵称
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String targetUserAvatar;  // 冗余展示：对方的头像
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String targetUserCollege; // 冗余展示：对方的学院

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
