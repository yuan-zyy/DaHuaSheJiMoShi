package com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener.impl;

import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatus;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatusEvent;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener.OrderStatusListener;

/**
 * 具体观察者4：用户通知观察者 - 推送短信
 */
public class UserNotifyListener implements OrderStatusListener {
    @Override
    public void onOrderStatusChange(OrderStatusEvent event) {
        if (event.getOldStatus() == OrderStatus.PENDING_DELIVERY && event.getNewStatus() == OrderStatus.DELIVERED) {
            System.out.println("📱 用户通知：给用户[" + event.getUserId() + "]推送短信，告知订单[" + event.getOrderId() + "]已发货");
            // 实际场景：调用短信服务商API推送短信
        }
    }
}
