package com.nntu.skillhelp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.User;
import com.nntu.skillhelp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@CrossOrigin // 解决跨域问题，建议后台都加上
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 【后台端】Admin 专用：仅用于演示登录
     * 实际中需要根据密码/账号判断，此处由于系统之前用openid，这里提供一个直接入口
     */
    @PostMapping("/login")
    public Result adminLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if ("admin".equals(username) && "123456".equals(password)) {
            Map<String, Object> data = new HashMap<>();
            data.put("token", "admin-demo-token-xyz"); // 假token
            data.put("username", "admin");
            Result<Map<String, Object>> res = Result.success(data);
            res.setMsg("登录成功");
            return res;
        }
        return Result.error(-1, "账号或密码错误");
    }

    /**
     * 获取所有用户列表 (带分页)
     */
    @GetMapping("/list")
    public Result getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like("nickname", keyword).or().like("real_name", keyword).or().like("student_id", keyword));
        }
        if (startTime != null && !startTime.trim().isEmpty()) {
            queryWrapper.ge("create_time", startTime + " 00:00:00");
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            queryWrapper.le("create_time", endTime + " 23:59:59");
        }
        queryWrapper.orderByDesc("create_time");
        Page<User> userPage = userService.page(page, queryWrapper);
        return Result.success(userPage);
    }

    /**
     * 禁用/解禁或删除用户操作 (演示)
     */
    @PostMapping("/delete/{id}")
    public Result deleteUser(@PathVariable Long id) {
        // 这里可以直接删除或者把 is_deleted 标为1
        boolean res = userService.removeById(id);
        if (res) {
            Result<String> r = Result.success(null);
            r.setMsg("删除成功");
            return r;
        }
        return Result.error(-1, "操作失败");
    }

    /**
     * 审核用户校园认证
     * @param body "status" 为 2 代表通过， 为 0 代表驳回重填
     */
    @PostMapping("/audit/{id}")
    public Result auditUser(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        Integer status = (Integer) body.get("status");
        String message = (String) body.get("message");
        if (status != null && (status == 2 || status == 0)) {
            user.setAuthStatus(status);
            if (status == 0 && message != null) {
                user.setAuthMessage(message);
            } else if (status == 2) {
                user.setAuthMessage("");
                // 认证通过后赋予初始信用分 50
                user.setCreditScore(50);
            }
            userService.updateById(user);
            Result<String> r = Result.success(null);
            r.setMsg(status == 2 ? "审核已通过" : "已驳回审核");
            return r;
        }
        return Result.error(400, "非法的状态参数");
    }
}
