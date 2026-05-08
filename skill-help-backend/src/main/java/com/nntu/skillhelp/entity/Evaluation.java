package com.nntu.skillhelp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation")
public class Evaluation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long fromUserId;
    private Long toUserId;
    private Integer score;
    private Integer scoreSpeed;
    private Integer scoreAttitude;
    private Integer scoreQuality;
    private String content;
    private String imageUrl;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String fromUserNickname;
    @TableField(exist = false)
    private String fromUserAvatar;
    @TableField(exist = false)
    private String targetTitle;
}
