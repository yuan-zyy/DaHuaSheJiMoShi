package com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener.impl;

import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatus;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatusEvent;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener.OrderStatusListener;

/**
 * 具体观察者1：库存观察者 - 扣减库存
 */
public class StockListener implements OrderStatusListener {
    @Override
    public void onOrderStatusChange(OrderStatusEvent event) {
        // 仅处理「待付款→已付款」的状态变更
        if (event.getOldStatus() == OrderStatus.PENDING_PAYMENT && event.getNewStatus() == OrderStatus.PAID) {
            System.out.println("📦 库存服务：订单[" + event.getOrderId() + "]已付款，扣减对应商品库存");
            // 实际场景：调用库存系统API扣减库存
        }
    }
}