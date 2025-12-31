package com.zyy.design.pattern.dhsjms.chapter02.demo02.cash;

public class CashNormal extends CashSuper {

    @Override
    public double acceptCash(double money) {
        return money;
    }

}
