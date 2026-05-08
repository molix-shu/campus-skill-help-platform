package com.nntu.skillhelp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nntu.skillhelp.entity.Skill;
import com.nntu.skillhelp.mapper.SkillMapper;
import com.nntu.skillhelp.service.SkillService;
import org.springframework.stereotype.Service;

@Service
public class SkillServiceImpl extends ServiceImpl<SkillMapper, Skill> implements SkillService {
}
