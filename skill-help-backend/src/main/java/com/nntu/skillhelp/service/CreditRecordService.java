package com.nntu.skillhelp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nntu.skillhelp.entity.CreditRecord;

public interface CreditRecordService extends IService<CreditRecord> {
    
    /**
     * 核心风控/信用流转函数
     *
     * @param userId 操作对象用户ID
     * @param scoreChange 增加或扣除的分数 (负数为扣除)
     * @param changeType 业务类型标识 (正负枚举类别)
     * @param reason 追溯说明
     * @param orderId 关联订单记录（非必填填 null）
     */
    void modifyUserCredit(Long userId, int scoreChange, int changeType, String reason, Long orderId);

    /**
     * 门禁方法：信用分是否低到熔断接单功能？(通常 <40 分返回 false)
     */
    boolean checkUserFeatureLimit(Long userId);
}
