package com.nntu.skillhelp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nntu.skillhelp.entity.User;
import com.nntu.skillhelp.mapper.UserMapper;
import com.nntu.skillhelp.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
