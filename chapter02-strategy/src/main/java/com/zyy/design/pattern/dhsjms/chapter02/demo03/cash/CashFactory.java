package com.zyy.design.pattern.dhsjms.chapter02.demo03.cash;

public class CashFactory {

    public static CashSuper createCashAccept(String type) {
        return switch (type) {
            case "9折" -> new CashRebate(0.9);
            case "8折" -> new CashRebate(0.8);
            case "5折" -> new CashRebate(0.5);
            case "满200减30" -> new CashReturn(200, 30);
            case "满100减10" -> new CashReturn(100, 10);
            default -> new CashNormal();
        };
    }

}
