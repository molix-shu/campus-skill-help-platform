package com.nntu.skillhelp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("appeal")
public class Appeal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;          // 申诉人ID
    private String relationTitle; // 关联订单标题
    private String content;       // 申诉理由详细说明
    private Integer status;       // 审核状态：0待处理, 1已处理并恢复信用分, 2已驳回
    private LocalDateTime createTime;
}
