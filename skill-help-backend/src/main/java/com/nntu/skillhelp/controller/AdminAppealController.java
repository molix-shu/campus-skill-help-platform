package com.nntu.skillhelp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.Appeal;
import com.nntu.skillhelp.entity.User;
import com.nntu.skillhelp.service.AppealService;
import com.nntu.skillhelp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/admin/appeal")
@CrossOrigin
public class AdminAppealController {

    @Autowired
    private AppealService appealService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result getList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<Appeal> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Appeal> queryWrapper = new QueryWrapper<>();
        if (startTime != null && !startTime.trim().isEmpty()) {
            queryWrapper.ge("create_time", startTime + " 00:00:00");
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            queryWrapper.le("create_time", endTime + " 23:59:59");
        }
        queryWrapper.orderByDesc("create_time");
        Page<Appeal> resPage = appealService.page(page, queryWrapper);
        return Result.success(resPage);
    }

    /**
     * 处理申诉
     * status: 1 = 同意并挽回信用， 2 = 驳回维持原判
     */
    @PostMapping("/handle/{id}")
    public Result handleAppeal(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Appeal appeal = appealService.getById(id);
        if (appeal == null) return Result.error(404, "找不到该申诉");

        Integer status = body.get("status");
        if (status == 1) {
            appeal.setStatus(1);
            // 补偿用户信用分，比如加回 10 分
            User u = userService.getById(appeal.getUserId());
            if (u != null && u.getCreditScore() != null) {
                u.setCreditScore(u.getCreditScore() + 10);
                userService.updateById(u);
            }
        } else if (status == 2) {
            appeal.setStatus(2);
        } else if (status == 0) {
            // 如果原本是1撤销，得扣回由于补偿加的分
            if (appeal.getStatus() == 1) {
                User u = userService.getById(appeal.getUserId());
                if (u != null && u.getCreditScore() != null) {
                    u.setCreditScore(u.getCreditScore() - 10);
                    userService.updateById(u);
                }
            }
            appeal.setStatus(0);
        }
        appealService.updateById(appeal);
        Result<String> r = Result.success(null);
        r.setMsg("处理完毕");
        return r;
    }
}
