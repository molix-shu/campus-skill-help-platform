package com.nntu.skillhelp.service;

import com.nntu.skillhelp.entity.Demand;
import com.nntu.skillhelp.entity.Skill;

import java.util.List;

public interface MatchingAlgorithmService {

    /**
     * 为当前用户推荐技能（基于用户的需求集合进行双向匹配）
     */
    List<Skill> recommendSkills(Long currentUserId, List<Skill> allActiveSkills);

    /**
     * 为当前用户推荐需求（基于用户的技能集合进行双向匹配）
     */
    List<Demand> recommendDemands(Long currentUserId, List<Demand> allActiveDemands);
}
