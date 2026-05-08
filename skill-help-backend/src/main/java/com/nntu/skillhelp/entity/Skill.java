package com.nntu.skillhelp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("skill")
public class Skill {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;        // 技能发布者ID
    private Long categoryId;    // 分类ID
    private String title;       // 技能标题
    private String description; // 技能详细描述
    private String tags;        // 标签集合(逗号分隔)
    
    // 这三个字段是刚刚前端表单新加的，现在加入实体类
    private String serviceTime;
    private String serviceMethod;
    private String priceDesc;

    private Integer status;     // 状态：0下架/关闭，1正常接单中
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String publisherName;

    @TableField(exist = false)
    private String publisherAvatar;
}
