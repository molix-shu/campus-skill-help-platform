package com.nntu.skillhelp.controller;

import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.dto.WechatLoginDTO;
import com.nntu.skillhelp.entity.User;
import com.nntu.skillhelp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 小程序用户：一键登录/注册接口
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody WechatLoginDTO loginDTO) {
        String mockOpenId = "wx_test_mock_" + loginDTO.getCode();
        User user = userService.query().eq("openid", mockOpenId).one();
        if (user == null) {
            user = new User();
            user.setOpenid(mockOpenId);
            user.setNickname(loginDTO.getNickname() != null ? loginDTO.getNickname() : "微信用户");
            user.setAvatarUrl(loginDTO.getAvatarUrl());
            // 设置初始默认值
            user.setAuthStatus(0);    // 0代表未学生认证
            user.setCreditScore(0);   // 未认证用户信用分为 0，认证通过后变为 50
            user.setIsDeleted(0);

            userService.save(user);
        }
        return Result.success(user);
    }

    /**
     * 更新用户资料（头像、昵称、真实姓名、学院等）
     */
    @PostMapping("/update")
    public Result<User> updateUserInfo(@RequestBody User updatedUser) {
        if (updatedUser.getId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        User existingUser = userService.getById(updatedUser.getId());
        if (existingUser == null) {
            return Result.error(404, "未找到该用户");
        }

        // 只更新允许修改的字段
        if (updatedUser.getNickname() != null) existingUser.setNickname(updatedUser.getNickname());
        if (updatedUser.getAvatarUrl() != null) existingUser.setAvatarUrl(updatedUser.getAvatarUrl());
        if (updatedUser.getRealName() != null) existingUser.setRealName(updatedUser.getRealName());
        if (updatedUser.getCollege() != null) existingUser.setCollege(updatedUser.getCollege());
        if (updatedUser.getMajor() != null) existingUser.setMajor(updatedUser.getMajor());

        boolean success = userService.updateById(existingUser);
        if (success) {
            return Result.success(existingUser);
        }
        return Result.error(500, "更新资料失败");
    }

    /**
     * 查询指定用户的公开主页信息（头像、昵称、学院、专业、信用分）
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestParam Long userId) {
        User user = userService.getById(userId);
        if (user == null) return Result.error(404, "用户不存在");
        // 隐藏敏感字段
        user.setOpenid(null);
        return Result.success(user);
    }

    /**
     * 用户提交校园身份认证信息
     */
    @PostMapping("/auth")
    public Result<User> submitAuth(@RequestBody User authInfo) {
        if (authInfo.getId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        User existingUser = userService.getById(authInfo.getId());
        if (existingUser == null) {
            return Result.error(404, "未找到该用户");
        }

        // 学号唯一性校验：同一个学号不允许被两个不同用户使用
        if (authInfo.getStudentId() != null && !authInfo.getStudentId().isEmpty()) {
            User duplicate = userService.query()
                    .eq("student_id", authInfo.getStudentId())
                    .ne("id", authInfo.getId())
                    .one();
            if (duplicate != null) {
                return Result.error(400, "该学号已被其他用户认证，请核实后重新填写");
            }
        }

        existingUser.setRealName(authInfo.getRealName());
        existingUser.setCollege(authInfo.getCollege());
        existingUser.setMajor(authInfo.getMajor());
        existingUser.setStudentId(authInfo.getStudentId());
        existingUser.setStudentCardUrl(authInfo.getStudentCardUrl());
        existingUser.setAuthStatus(1); // 1 = 审核中
        existingUser.setAuthMessage(""); // 清空之前的驳回原因

        boolean success = userService.updateById(existingUser);
        if (success) {
            return Result.success(existingUser);
        }
        return Result.error(500, "提交认证失败");
    }
}
