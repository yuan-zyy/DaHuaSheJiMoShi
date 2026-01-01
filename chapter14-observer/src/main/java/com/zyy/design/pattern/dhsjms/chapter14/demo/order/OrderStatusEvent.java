package com.zyy.design.pattern.dhsjms.chapter14.demo.order;

import java.util.Date;

/**
 * 订单状态事件
 */
public class OrderStatusEvent {
    private String orderId;        // 订单ID
    private OrderStatus oldStatus;  // 原状态
    private OrderStatus newStatus;  // 新状态
    private Date changeTime;       // 变更时间
    private String userId;         // 用户ID

    public OrderStatusEvent(String orderId, OrderStatus oldStatus, OrderStatus newStatus, String userId) {
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changeTime = new Date();
        this.userId = userId;
    }

    // Getter 方法
    public String getOrderId() { return orderId; }
    public OrderStatus getOldStatus() { return oldStatus; }
    public OrderStatus getNewStatus() { return newStatus; }
    public Date getChangeTime() { return changeTime; }
    public String getUserId() { return userId; }

    @Override
    public String toString() {
        return String.format("订单[%s] 状态变更：%s → %s（用户ID：%s，变更时间：%s）",
                orderId, oldStatus.getDesc(), newStatus.getDesc(), userId, changeTime);
    }
}