package com.nntu.skillhelp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nntu.skillhelp.entity.Demand;
import com.nntu.skillhelp.mapper.DemandMapper;
import com.nntu.skillhelp.service.DemandService;
import org.springframework.stereotype.Service;

@Service
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand> implements DemandService {
}
