package com.zyy.design.pattern.dhsjms.chapter06.demo02;

import com.zyy.design.pattern.dhsjms.chapter06.demo02.impl.CashNormal;
import com.zyy.design.pattern.dhsjms.chapter06.demo02.impl.CashRebate;
import com.zyy.design.pattern.dhsjms.chapter06.demo02.impl.CashReturn;

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
                CashRebate cashRebate = new CashRebate(0.8);
                CashReturn cashReturn = new CashReturn(300D, 100D);

                cashRebate.setComponent(cashNormal);
                cashReturn.setComponent(cashRebate);
                this.sale = cashReturn;                 // 将包装好的算法组合引用传给cs对象
                break;
            case 6:
                // 先满200返50，再打7折
                CashNormal cn2 = new CashNormal();
                CashReturn cr4 = new CashReturn(200D, 50D);
                CashRebate cr3 = new CashRebate(0.7);

                cr4.setComponent(cn2);
                cr3.setComponent(cr4);
                this.sale = cr3;
                break;

        }
    }

    public double getResult(double price, int num) {
        return this.sale.acceptCash(price, num);
    }

}
