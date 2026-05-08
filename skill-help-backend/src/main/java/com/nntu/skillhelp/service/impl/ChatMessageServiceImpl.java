package com.nntu.skillhelp.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nntu.skillhelp.entity.ChatMessage;
import com.nntu.skillhelp.mapper.ChatMessageMapper;
import com.nntu.skillhelp.service.ChatMessageService;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {}
