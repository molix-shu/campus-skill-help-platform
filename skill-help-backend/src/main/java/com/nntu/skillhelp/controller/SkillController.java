package com.nntu.skillhelp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.Skill;
import com.nntu.skillhelp.service.SkillService;
import com.nntu.skillhelp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skill")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final UserService userService;
    private final com.nntu.skillhelp.service.MatchingAlgorithmService matchingAlgorithmService;

    @PostMapping("/publish")
    public Result<Skill> publish(@RequestBody Skill skill) {
        if (skill.getCategoryId() == null) {
            skill.setCategoryId(1L);
        }
        skill.setStatus(1); // 1 = 正常接单中

        boolean save = skillService.save(skill);
        if (save) {
            return Result.success(skill);
        }
        return Result.error(500, "技能发布失败");
    }

    @GetMapping("/list")
    public Result<List<Skill>> list(@RequestParam(required = false) Long userId) {
        QueryWrapper<Skill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        List<Skill> allActiveSkills = skillService.list(queryWrapper);
        
        // 使用基于双向约束的综合评分匹配算法进行排序推荐
        List<Skill> recommendedList = matchingAlgorithmService.recommendSkills(userId, allActiveSkills);
        
        fillPublisherInfo(recommendedList);
        return Result.success(recommendedList);
    }

    @GetMapping("/my")
    public Result<List<Skill>> mySkills(@RequestParam Long userId) {
        QueryWrapper<Skill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        List<Skill> list = skillService.list(queryWrapper);
        fillPublisherInfo(list);
        return Result.success(list);
    }

    private void fillPublisherInfo(List<Skill> list) {
        for (Skill skill : list) {
            if (skill.getUserId() != null) {
                com.nntu.skillhelp.entity.User user = userService.getById(skill.getUserId());
                if (user != null) {
                    skill.setPublisherName(user.getNickname());
                    skill.setPublisherAvatar(user.getAvatarUrl());
                }
            }
        }
    }

    @PostMapping("/offline")
    public Result<String> offlineSkill(@RequestParam Long id, @RequestParam Long userId) {
        Skill skill = skillService.getById(id);
        if (skill == null) return Result.error(404, "贴子不存在");
        if (!skill.getUserId().equals(userId)) return Result.error(403, "无权下架别人的技能");
        
        skill.setStatus(0); // 0 = 下架
        skillService.updateById(skill);
        return Result.success("下架成功");
    }

    @PostMapping("/update")
    public Result<String> updateSkill(@RequestBody Skill skill) {
        if (skill.getId() == null) {
            return Result.error(400, "缺少主键");
        }
        boolean update = skillService.updateById(skill);
        if (update) {
            return Result.success("更新成功");
        }
        return Result.error(500, "技能更新失败");
    }
}
