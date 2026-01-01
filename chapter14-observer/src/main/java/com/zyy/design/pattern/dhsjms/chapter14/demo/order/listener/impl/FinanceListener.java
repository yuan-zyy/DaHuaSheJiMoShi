package com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener.impl;

import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatus;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatusEvent;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener.OrderStatusListener;

/**
 * 具体观察者2：财务观察者 - 生成账单
 */
public class FinanceListener implements OrderStatusListener {
    @Override
    public void onOrderStatusChange(OrderStatusEvent event) {
        if (event.getOldStatus() == OrderStatus.PENDING_PAYMENT && event.getNewStatus() == OrderStatus.PAID) {
            System.out.println("💰 财务服务：订单[" + event.getOrderId() + "]已付款，生成收款账单");
            // 实际场景：调用财务系统API生成账单
        }
    }
}