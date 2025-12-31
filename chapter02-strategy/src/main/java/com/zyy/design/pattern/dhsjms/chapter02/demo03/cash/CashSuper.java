package com.zyy.design.pattern.dhsjms.chapter02.demo03.cash;

/**
 *
 */
public abstract class CashSuper {

    /**
     * 现金收取超类的抽象方法，收取现金，参数为原价，返回当前价
     * @param money
     * @return
     */
    public abstract double acceptCash(double money);

}
