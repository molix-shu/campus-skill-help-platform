package com.nntu.skillhelp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.CreditRecord;
import com.nntu.skillhelp.service.CreditRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
public class CreditRecordController {

    private final CreditRecordService creditRecordService;

    /**
     * 获取指定用户的信用积分流转记录
     */
    @GetMapping("/records")
    public Result<List<CreditRecord>> getUserCreditRecords(@RequestParam Long userId) {
        QueryWrapper<CreditRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return Result.success(creditRecordService.list(wrapper));
    }
}
