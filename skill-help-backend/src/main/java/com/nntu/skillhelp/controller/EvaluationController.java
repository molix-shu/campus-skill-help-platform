package com.nntu.skillhelp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.Evaluation;
import com.nntu.skillhelp.entity.HelpOrder;
import com.nntu.skillhelp.service.EvaluationService;
import com.nntu.skillhelp.service.HelpOrderService;
import com.nntu.skillhelp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eval")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final HelpOrderService helpOrderService;
    private final UserService userService;
    private final com.nntu.skillhelp.service.SkillService skillService;
    private final com.nntu.skillhelp.service.DemandService demandService;

    @PostMapping("/submit")
    public Result<Evaluation> submitEval(@RequestBody Evaluation evaluation) {
        HelpOrder order = helpOrderService.getById(evaluation.getOrderId());
        if (order == null) return Result.error(404, "订单不存在");
        
        if (order.getStatus() != 2) {
            return Result.error(400, "当前订单不是待评价状态");
        }

        boolean saved = evaluationService.save(evaluation);
        if (saved) {
            order.setStatus(3); // 3=关闭/已完结
            helpOrderService.updateById(order);
            
            // 更新评价对象的信用分逻辑：5星+2分，4星+1分，3星不加不减，2星-1分，1星-2分
            com.nntu.skillhelp.entity.User toUser = userService.getById(evaluation.getToUserId());
            if (toUser != null && evaluation.getScore() != null) {
                int addScore = evaluation.getScore() - 3;
                toUser.setCreditScore(toUser.getCreditScore() + addScore);
                userService.updateById(toUser);
            }
            
            return Result.success(evaluation);
        }
        return Result.error(500, "提交评价失败");
    }

    /**
     * 获取某用户收到的所有评价（带评价人头像和昵称）
     */
    @GetMapping("/received")
    public Result<List<Evaluation>> received(@RequestParam Long userId) {
        QueryWrapper<Evaluation> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", userId);
        wrapper.orderByDesc("create_time");
        List<Evaluation> list = evaluationService.list(wrapper);

        for (Evaluation e : list) {
            if (e.getFromUserId() != null) {
                com.nntu.skillhelp.entity.User u = userService.getById(e.getFromUserId());
                if (u != null) {
                    e.setFromUserNickname(u.getNickname());
                    e.setFromUserAvatar(u.getAvatarUrl());
                }
            }
            if (e.getOrderId() != null) {
                HelpOrder order = helpOrderService.getById(e.getOrderId());
                if (order != null) {
                    if (order.getType() == 1) {
                        com.nntu.skillhelp.entity.Skill skill = skillService.getById(order.getTargetId());
                        if (skill != null) e.setTargetTitle(skill.getTitle());
                    } else if (order.getType() == 2) {
                        com.nntu.skillhelp.entity.Demand demand = demandService.getById(order.getTargetId());
                        if (demand != null) e.setTargetTitle(demand.getTitle());
                    }
                }
            }
        }
        return Result.success(list);
    }
}
