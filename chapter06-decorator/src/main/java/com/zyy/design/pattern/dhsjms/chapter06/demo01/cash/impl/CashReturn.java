package com.zyy.design.pattern.dhsjms.chapter06.demo01.cash.impl;

import com.zyy.design.pattern.dhsjms.chapter06.demo01.cash.CashSuper;

public class CashReturn extends CashSuper {
    // 返利条件
    private double moneyCondition = 0D;
    // 返利值
    private double moneyRetrun = 0D;

    // 返利收费，初始化时需要输入返利条件和返利值
    // 比如 “满300返100”
    public CashReturn(double moneyCondition, double moneyReturn) {
        this.moneyCondition = moneyCondition;
        this.moneyRetrun = moneyReturn;
    }

    // 计算收费时，当达到返利条件，就按原价减去返利值
    @Override
    public double acceptCash(double price, int num) {
        double result = price * num;
        if (moneyCondition > 0 && result >= moneyCondition) {
            result = result - Math.floor(result / moneyCondition) * moneyRetrun;
        }
        System.out.println("满" + moneyCondition + "返" + moneyRetrun + "收费：" + result);
        result = super.acceptCash(result, 1);
        return result;
    }
}
