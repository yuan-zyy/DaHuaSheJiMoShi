package com.zyy.design.pattern.dhsjms.chapter06.demo01.cash;

import com.zyy.design.pattern.dhsjms.chapter06.demo01.cash.impl.CashNormal;
import com.zyy.design.pattern.dhsjms.chapter06.demo01.cash.impl.CashRebate;
import com.zyy.design.pattern.dhsjms.chapter06.demo01.cash.impl.CashReturn;

public class CashContext {

    private ISale sale;

    // 通过构造方法，传入具体的收费策略
    public CashContext(int cashType) {
        switch (cashType) {
            case 1:
                this.sale = new CashNormal();
                break;
            case 5:
                // 先打8折，再满300返100
                CashNormal cashNormal = new CashNormal();
                CashReturn cashReturn = new CashReturn(300D, 100D);
                CashRebate cashRebate = new CashRebate(0.8);

                cashReturn.setComponent(cashNormal);    // 用满300返100算法包装基本的原价计算
                cashRebate.setComponent(cashReturn);    // 打8折算法装饰满300返100算法
                this.sale = cashRebate;                 // 将包装好的算法组合引用传给cs对象
                break;
            case 6:
                // 先满200返50，再打7折
                CashNormal cn2 = new CashNormal();
                CashRebate cr3 = new CashRebate(0.7);
                CashReturn cr4 = new CashReturn(200D, 50D);
                cr3.setComponent(cn2);
                cr4.setComponent(cr3);
                this.sale = cr4;
                break;

        }
    }

    public double getResult(double price, int num) {
        return this.sale.acceptCash(price, num);
    }

}
