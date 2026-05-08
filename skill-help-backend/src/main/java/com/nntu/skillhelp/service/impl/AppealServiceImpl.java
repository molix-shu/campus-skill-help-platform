package com.nntu.skillhelp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nntu.skillhelp.entity.Appeal;
import com.nntu.skillhelp.mapper.AppealMapper;
import com.nntu.skillhelp.service.AppealService;
import org.springframework.stereotype.Service;

@Service
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements AppealService {
}
