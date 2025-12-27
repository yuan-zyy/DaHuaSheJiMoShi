package com.zyy.design.pattern.dhsjms.chapter06.demo01.cash;

public class Main {

    public static void main(String[] args) {
        CashContext cashContext = new CashContext(5);
        double result = cashContext.getResult(1000, 1);
        System.out.println("最终收费：" + result);
    }

}
