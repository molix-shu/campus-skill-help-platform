package com.nntu.skillhelp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.ChatMessage;
import com.nntu.skillhelp.service.ChatMessageService;
import com.nntu.skillhelp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final UserService userService;

    @PostMapping("/send")
    public Result<ChatMessage> send(@RequestBody ChatMessage msg) {
        if (msg.getContentType() == null) msg.setContentType("text");
        msg.setCreateTime(LocalDateTime.now());
        chatMessageService.save(msg);
        return Result.success(msg);
    }

    @GetMapping("/list")
    public Result<List<ChatMessage>> list(@RequestParam Long orderId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        wrapper.orderByAsc("create_time");
        
        List<ChatMessage> list = chatMessageService.list(wrapper);
        for (ChatMessage m : list) {
            com.nntu.skillhelp.entity.User u = userService.getById(m.getSenderId());
            if (u != null) {
                m.setSenderAvatar(u.getAvatarUrl());
            }
        }
        return Result.success(list);
    }
}
