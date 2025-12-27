package com.zyy.design.pattern.dhsjms.chapter06.demo02;

public class CashSuper implements ISale {

    protected ISale component;

    public void setComponent(ISale component) {
        this.component = component;
    }

    @Override
    public double acceptCash(double price, int num) {
        double result = 0D;
        if(this.component != null) {
            // 如果装饰对象存在，则执行装饰对象的算法运算
            result = this.component.acceptCash(price, num);
        }
        return result;
    }
}
