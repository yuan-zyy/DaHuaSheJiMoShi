package com.zyy.design.pattern.dhsjms.chapter06.demo02.impl;

import com.zyy.design.pattern.dhsjms.chapter06.demo02.ISale;

public class CashNormal implements ISale {
    @Override
    public double acceptCash(double price, int num) {
        // 正常收费，原价返回
        double result = price * num;
        System.out.println("正常收费: " + result);
        return result;
    }
}
