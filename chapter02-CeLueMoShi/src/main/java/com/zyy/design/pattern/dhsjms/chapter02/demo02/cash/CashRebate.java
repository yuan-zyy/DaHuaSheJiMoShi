package com.zyy.design.pattern.dhsjms.chapter02.demo02.cash;

public class CashRebate extends CashSuper {

    private double moneyRebate = 1D;

    public CashRebate(double moneyRebate) {
        this.moneyRebate = moneyRebate;
    }

    @Override
    public double acceptCash(double money) {
        return money * moneyRebate;
    }
}
