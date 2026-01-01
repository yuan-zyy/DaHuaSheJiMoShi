package com.zyy.design.pattern.dhsjms.chapter14.demo.order.service;

import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatus;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.OrderStatusEvent;
import com.zyy.design.pattern.dhsjms.chapter14.demo.order.listener.OrderStatusListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 订单服务（具体被观察者）
 */
public class LocalOrderService {
    // 观察者列表
    private List<OrderStatusListener> listenerList = new ArrayList<>();
    // 异步线程池：避免阻塞订单主流程
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    // 注册观察者
    public void registerListener(OrderStatusListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    // 移除观察者
    public void removeListener(OrderStatusListener listener) {
        listenerList.remove(listener);
    }

    // 异步发布订单状态事件
    public void publishOrderStatusEvent(OrderStatusEvent event) {
        System.out.println("📢 订单服务：发布订单状态变更事件 - " + event);
        // 异步分发事件，不阻塞订单主流程
        executor.execute(() -> {
            for (OrderStatusListener listener : listenerList) {
                listener.onOrderStatusChange(event);
            }
        });
    }

    // 变更订单状态
    public void changeOrderStatus(String orderId, OrderStatus oldStatus, OrderStatus newStatus, String userId) {
        // 1. 业务逻辑：更新订单状态（实际场景：操作数据库）
        System.out.println("🔧 订单服务：更新订单[" + orderId + "]状态为" + newStatus.getDesc());
        // 2. 发布状态变更事件
        publishOrderStatusEvent(new OrderStatusEvent(orderId, oldStatus, newStatus, userId));
    }

    // 关闭线程池
    public void shutdown() {
        executor.shutdown();
    }
}