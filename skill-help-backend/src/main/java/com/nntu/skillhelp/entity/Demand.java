package com.nntu.skillhelp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("demand")
public class Demand {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;        // 求助者用户ID
    private Long categoryId;    // 分类ID
    private String title;       // 需求标题
    private String description; // 需求描述
    private String tags;        // 标签集合(逗号分隔)
    private String expectedTime;// 期望完成时间
    private String serviceMethod;// 期望服务方式
    private String rewardDesc;  // 酬劳/感谢方式
    private Integer status;     // 状态：0已关闭/撤销，1寻找中，2对接中，3已完成
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String publisherName;

    @TableField(exist = false)
    private String publisherAvatar;
}
