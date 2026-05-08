package com.nntu.skillhelp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nntu.skillhelp.common.Result;
import com.nntu.skillhelp.entity.HelpOrder;
import com.nntu.skillhelp.service.HelpOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class HelpOrderController {

    private final HelpOrderService helpOrderService;
    private final com.nntu.skillhelp.service.SkillService skillService;
    private final com.nntu.skillhelp.service.DemandService demandService;
    private final com.nntu.skillhelp.service.UserService userService;

    /**
     * 创建订单（接单）
     */
    @PostMapping("/create")
    public Result<HelpOrder> createOrder(@RequestBody HelpOrder order) {
        // 后端双保险：防止自己接自己的单
        if (order.getPublisherId() != null && order.getPublisherId().equals(order.getApplicantId())) {
            return Result.error(400, "不能接自己发布的需求");
        }

        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        order.setOrderNo(orderNo);
        order.setStatus(0); // 0 待发布者同意

        boolean save = helpOrderService.save(order);
        if (save) {
            // 接单后将需求状态改为 对接中(status=2)，从需求大厅移除
            if (order.getType() == 2 && order.getTargetId() != null) {
                com.nntu.skillhelp.entity.Demand demand = demandService.getById(order.getTargetId());
                if (demand != null) {
                    demand.setStatus(2);
                    demandService.updateById(demand);
                }
            }
            return Result.success(order);
        }
        return Result.error(500, "订单创建失败");
    }

    /**
     * 查询我的订单列表（带对方用户信息和标题）
     */
    @GetMapping("/my")
    public Result<List<HelpOrder>> getMyOrders(@RequestParam Long userId) {
        QueryWrapper<HelpOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("publisher_id", userId).or().eq("applicant_id", userId);
        wrapper.orderByDesc("create_time");

        List<HelpOrder> list = helpOrderService.list(wrapper);
        for (HelpOrder order : list) {
            // 用 String 比较避免 Long 自动装箱范围外失效的类型不匹配
            Long targetUserId = String.valueOf(order.getPublisherId()).equals(String.valueOf(userId))
                    ? order.getApplicantId()
                    : order.getPublisherId();

            if (targetUserId != null) {
                com.nntu.skillhelp.entity.User user = userService.getById(targetUserId);
                if (user != null) {
                    order.setTargetUserName(user.getNickname());
                    order.setTargetUserAvatar(user.getAvatarUrl());
                    order.setTargetUserCollege(user.getCollege());
                }
            }

            // 获取具体的技能或需求标题
            if (order.getType() == 1) {
                com.nntu.skillhelp.entity.Skill skill = skillService.getById(order.getTargetId());
                if (skill != null) order.setTargetTitle(skill.getTitle());
            } else if (order.getType() == 2) {
                com.nntu.skillhelp.entity.Demand demand = demandService.getById(order.getTargetId());
                if (demand != null) order.setTargetTitle(demand.getTitle());
            }
        }
        return Result.success(list);
    }

    /**
     * 发布者（求助方）同意接单方加入互助
     */
    @PostMapping("/confirm")
    public Result<String> confirmOrder(@RequestParam Long orderId, @RequestParam Long userId) {
        HelpOrder order = helpOrderService.getById(orderId);
        if (order == null) return Result.error(404, "订单不存在");

        if (!String.valueOf(order.getPublisherId()).equals(String.valueOf(userId))) {
            return Result.error(403, "只有求助方才能同意接单");
        }

        if (order.getStatus() != 0) {
            return Result.error(400, "订单状态错误，无法同意");
        }

        order.setStatus(1);
        helpOrderService.updateById(order);
        return Result.success("互助已开始！");
    }

    /**
     * 接单方（帮手）确认互助已完成 → 触发评价流程
     */
    @PostMapping("/finish")
    public Result<String> finishOrder(@RequestParam Long orderId, @RequestParam Long userId) {
        HelpOrder order = helpOrderService.getById(orderId);
        if (order == null) return Result.error(404, "订单不存在");

        if (!String.valueOf(order.getApplicantId()).equals(String.valueOf(userId))) {
            return Result.error(403, "只有接单方才能确认完成");
        }

        if (order.getStatus() != 1) {
            return Result.error(400, "订单不在进行中，无法完成");
        }

        order.setStatus(2);
        helpOrderService.updateById(order);

        // 同步把关联需求标为已解决
        if (order.getType() == 2 && order.getTargetId() != null) {
            com.nntu.skillhelp.entity.Demand demand = demandService.getById(order.getTargetId());
            if (demand != null) {
                demand.setStatus(3);
                demandService.updateById(demand);
            }
        }

        return Result.success("互助已完成！请求助方进行评价");
    }
}
