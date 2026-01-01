package com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener.impl;

import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatus;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatusEvent;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener.OrderStatusListener;

/**
 * 具体观察者3：物流观察者 - 生成运单
 */
public class LogisticsListener implements OrderStatusListener {
    @Override
    public void onOrderStatusChange(OrderStatusEvent event) {
        if (event.getOldStatus() == OrderStatus.PENDING_DELIVERY && event.getNewStatus() == OrderStatus.DELIVERED) {
            System.out.println("🚚 物流服务：订单[" + event.getOrderId() + "]已发货，生成运单（运单号：YD" + System.currentTimeMillis() + "）");
            // 实际场景：调用物流系统API生成运单
        }
    }
}