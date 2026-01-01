package com.zyy.design.pattern.dhsjms.chapter14.demo.order;

/**
 * 订单状态码
 */
import java.util.Date;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    PENDING_PAYMENT("待付款"),
    PAID("已付款"),
    PENDING_DELIVERY("待发货"),
    DELIVERED("已发货"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private String desc;

    OrderStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}

