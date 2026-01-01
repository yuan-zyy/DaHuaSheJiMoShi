package com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener;

import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatusEvent;

/**
 * 订单状态监听器（抽象观察者）
 */
public interface OrderStatusListener {

    void onOrderStatusChange(OrderStatusEvent event);

}
