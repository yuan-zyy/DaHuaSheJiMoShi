package com.zyy.design.pattern.dhsjms.chapter06.demo02.impl;

import com.zyy.design.pattern.dhsjms.chapter06.demo02.CashSuper;

public class CashRebate extends CashSuper {

    private double moneyRebate = 1D;

    // 打折收费。初始化时必须输入折扣率，八折就输入 0.8
    public CashRebate(double moneyRebate) {
        this.moneyRebate = moneyRebate;
    }

    // 计算收费时需要在原价基础上乘以折扣率
    @Override
    public double acceptCash(double price, int num) {
        double result = super.acceptCash(price, num);
        result = result * this.moneyRebate;
        System.out.println("打折收费，折扣率：" + this.moneyRebate + " 收费：" + result);
        return result;
    }
}
