package com.nntu.skillhelp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("credit_record")
public class CreditRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;          // 被操作的用户ID
    private Integer changeType;   // 变动类型(1:注册 2:实名 3:完成互助 4:好评 -1:违约爽约 -2:投诉成立)
    private Integer scoreChange;  // 变动分值(正数加分，负数扣分)
    private String reason;        // 详情理由(中文字面)
    private Long relatedOrderId;  // 关联的互助订单ID
    
    private LocalDateTime createTime;
}
