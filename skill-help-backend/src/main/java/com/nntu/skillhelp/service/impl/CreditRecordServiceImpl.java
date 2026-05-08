package com.nntu.skillhelp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nntu.skillhelp.entity.CreditRecord;
import com.nntu.skillhelp.entity.User;
import com.nntu.skillhelp.mapper.CreditRecordMapper;
import com.nntu.skillhelp.service.CreditRecordService;
import com.nntu.skillhelp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditRecordServiceImpl extends ServiceImpl<CreditRecordMapper, CreditRecord> implements CreditRecordService {

    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyUserCredit(Long userId, int scoreChange, int changeType, String reason, Long orderId) {
        if (userId == null || scoreChange == 0) return;

        User user = userService.getById(userId);
        if (user == null) return;

        int currentScore = user.getCreditScore() != null ? user.getCreditScore() : 50;
        int newScore = currentScore + scoreChange;
        
        // 边界控制：最高 100，最低 0
        if (newScore > 100) newScore = 100;
        if (newScore < 0) newScore = 0;

        // 根据分数确定等级
        String level = "正常";
        if (newScore >= 90) level = "优秀";
        else if (newScore >= 75) level = "良好";
        else if (newScore >= 60) level = "正常";
        else if (newScore >= 40) level = "较低";
        else level = "风险";

        // 更新用户主表汇总数据
        user.setCreditScore(newScore);
        user.setCreditLevel(level);

        // 如果是特殊类型，累加统计数值
        if (changeType == 3) {
            user.setSuccessCount((user.getSuccessCount() == null ? 0 : user.getSuccessCount()) + 1);
        } else if (changeType == -1) {
            user.setDefaultCount((user.getDefaultCount() == null ? 0 : user.getDefaultCount()) + 1);
        } else if (changeType == -2) {
            user.setComplaintCount((user.getComplaintCount() == null ? 0 : user.getComplaintCount()) + 1);
        }

        userService.updateById(user);

        // 插入溯源流水
        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeType(changeType);
        record.setScoreChange(scoreChange);
        record.setReason(reason);
        record.setRelatedOrderId(orderId);
        this.save(record);
    }

    @Override
    public boolean checkUserFeatureLimit(Long userId) {
        User user = userService.getById(userId);
        if (user == null) return false;
        int score = user.getCreditScore() != null ? user.getCreditScore() : 50;
        // 如果 < 40 分，限制部分主动接单和发布等重要功能
        return score >= 40;
    }
}
