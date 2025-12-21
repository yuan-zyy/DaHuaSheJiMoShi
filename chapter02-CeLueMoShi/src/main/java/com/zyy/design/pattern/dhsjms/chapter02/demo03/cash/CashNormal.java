package com.zyy.design.pattern.dhsjms.chapter02.demo03.cash;

public class CashNormal extends CashSuper {

    @Override
    public double acceptCash(double money) {
        return money;
    }

}
