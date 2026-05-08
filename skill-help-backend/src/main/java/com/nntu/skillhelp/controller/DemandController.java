package com.nntu.skillhelp.controller;

import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.Demand;
import com.nntu.skillhelp.service.DemandService;
import com.nntu.skillhelp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demand")
@RequiredArgsConstructor
public class DemandController {

    private final DemandService demandService;
    private final UserService userService;
    private final com.nntu.skillhelp.service.MatchingAlgorithmService matchingAlgorithmService;

    /**
     * 用户在小程序端发布一条新的互助需求贴
     */
    @PostMapping("/publish")
    public Result<Demand> publish(@RequestBody Demand demand) {
        // 容错处理：设置默认分类和状态
        if (demand.getCategoryId() == null) {
            demand.setCategoryId(1L); // 默认赋予分类 1
        }
        demand.setStatus(1); // 1 = 寻找中，刚发出默认就是寻找中

        // 保存到数据库 (MyBatis-Plus 提供的方法，自动转为 INSERT SQL)
        boolean save = demandService.save(demand);
        if (save) {
            return Result.success(demand);
        }
        return Result.error(500, "发布失败，数据库异常");
    }
    @GetMapping("/list")
    public Result<java.util.List<Demand>> list(@RequestParam(required = false) Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Demand> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("status", 1);
        java.util.List<Demand> allActiveDemands = demandService.list(queryWrapper);
        
        // 使用基于双向约束的综合评分匹配算法进行排序推荐
        java.util.List<Demand> recommendedList = matchingAlgorithmService.recommendDemands(userId, allActiveDemands);
        
        fillPublisherInfo(recommendedList);
        return Result.success(recommendedList);
    }

    @GetMapping("/my")
    public Result<java.util.List<Demand>> myDemands(@RequestParam Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Demand> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        java.util.List<Demand> list = demandService.list(wrapper);
        fillPublisherInfo(list);
        return Result.success(list);
    }

    private void fillPublisherInfo(java.util.List<Demand> list) {
        for (Demand demand : list) {
            if (demand.getUserId() != null) {
                com.nntu.skillhelp.entity.User user = userService.getById(demand.getUserId());
                if (user != null) {
                    demand.setPublisherName(user.getNickname());
                    demand.setPublisherAvatar(user.getAvatarUrl());
                }
            }
        }
    }

    @PostMapping("/resolve")
    public Result<String> resolveDemand(@RequestParam Long id, @RequestParam Long userId) {
        Demand demand = demandService.getById(id);
        if (demand == null) return Result.error(404, "互助需求不存在");
        if (!demand.getUserId().equals(userId)) return Result.error(403, "无权操作别人的求助贴");
        
        demand.setStatus(2); // 2 = 已解决
        demandService.updateById(demand);
        return Result.success("已标记为解决！");
    }

    @PostMapping("/update")
    public Result<String> updateDemand(@RequestBody Demand demand) {
        if (demand.getId() == null) {
            return Result.error(400, "缺少主键");
        }
        boolean update = demandService.updateById(demand);
        if (update) {
            return Result.success("更新成功");
        }
        return Result.error(500, "需求更新失败");
    }
}
