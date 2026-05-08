package com.nntu.skillhelp.controller;

import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.Appeal;
import com.nntu.skillhelp.service.AppealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appeal")
public class AppealController {

    @Autowired
    private AppealService appealService;

    @PostMapping("/submit")
    public Result submitAppeal(@RequestBody Appeal appeal) {
        if (appeal.getUserId() == null || appeal.getContent() == null) {
            return Result.error(400, "参数不完整");
        }
        appeal.setStatus(0); // 正常情况下默认也是0
        appeal.setCreateTime(LocalDateTime.now());
        boolean res = appealService.save(appeal);
        return res ? Result.success(null) : Result.error(500, "提交失败");
    }
}
