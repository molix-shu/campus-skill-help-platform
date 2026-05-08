package com.nntu.skillhelp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nntu.skillhelp.entity.HelpOrder;
import com.nntu.skillhelp.mapper.HelpOrderMapper;
import com.nntu.skillhelp.service.HelpOrderService;
import org.springframework.stereotype.Service;

@Service
public class HelpOrderServiceImpl extends ServiceImpl<HelpOrderMapper, HelpOrder> implements HelpOrderService {
}
