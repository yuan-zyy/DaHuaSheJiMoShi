package com.zyy.design.pattern.dhsjms.chapter02.demo03.cash;

public class CashReturn extends CashSuper {

    private double moneyCondition = 0D;

    private double moneyReturn = 0D;

    public CashReturn(double moneyCondition, double moneyReturn) {
        this.moneyCondition = moneyCondition;
        this.moneyReturn = moneyReturn;
    }

    @Override
    public double acceptCash(double money) {
        if (money >= moneyCondition) {
            money = money - Math.floor(money / moneyCondition) * moneyReturn;
        }
        return money;
    }

}
