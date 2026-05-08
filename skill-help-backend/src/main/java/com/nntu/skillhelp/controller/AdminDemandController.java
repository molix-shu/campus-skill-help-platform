package com.nntu.skillhelp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.Demand;
import com.nntu.skillhelp.service.DemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/demand")
@CrossOrigin 
public class AdminDemandController {

    @Autowired
    private DemandService demandService;

    /**
     * 获取需求列表 (Admin 分页查找)
     */
    @GetMapping("/list")
    public Result getDemandList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        Page<Demand> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Demand> queryWrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("description", keyword));
        }
        if (status != null) {
            queryWrapper.eq("status", status); // 假设 status 用于区分是否完成等
        }
        if (startTime != null && !startTime.trim().isEmpty()) {
            queryWrapper.ge("create_time", startTime + " 00:00:00");
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            queryWrapper.le("create_time", endTime + " 23:59:59");
        }
        
        queryWrapper.orderByDesc("create_time");
        Page<Demand> demandPage = demandService.page(page, queryWrapper);
        
        return Result.success(demandPage);
    }

    /**
     * 封禁违规需求 (Admin)
     */
    @PostMapping("/delete/{id}")
    public Result deleteDemand(@PathVariable Long id) {
        // 由于是毕设，最简单的伪删除或者是真删除：
        boolean res = demandService.removeById(id);
        if (res) {
            Result<String> r = Result.success(null);
            r.setMsg("操作成功");
            return r;
        }
        return Result.error(-1, "操作失败");
    }
}
