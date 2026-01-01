# Java 对象适配器模式的详细使用指南

你想了解Java中对象适配器模式的具体使用方法，我会从核心原理、步骤拆解、完整代码示例到使用说明，为你详细讲解：

### 一、对象适配器模式的核心原理

对象适配器模式是适配器模式的推荐实现方式，它基于**组合（Has-A）关系**实现（而非类适配器的继承关系），核心是让适配器类持有适配者的实例，通过包装适配者对象来完成接口转换，最终让客户端可以通过统一的目标接口使用适配者的功能，同时遵循“合成复用原则”，耦合度更低、灵活性更强。

### 二、使用对象适配器模式的4个核心步骤

1. **定义目标接口（Target）**：客户端期望依赖和使用的接口，明确客户端需要的功能规范。

2. **实现适配者类（Adaptee）**：现有存在的、接口不符合客户端需求的类（被适配的类），包含客户端需要的核心业务逻辑，但无法直接被客户端使用。

3. **创建对象适配器类（Adapter）**：
        

    - 实现目标接口（Target），保证客户端能以统一方式调用；

    - 在适配器内部持有适配者（Adaptee）的实例（通过成员变量存储）；

    - 通常通过构造方法注入适配者对象，提高灵活性；

    - 重写目标接口的方法，在方法内部调用适配者的对应方法，完成接口转换。

4. **客户端调用**：客户端只依赖目标接口，无需感知适配者的存在，通过适配器间接使用适配者的功能。

### 三、完整可运行代码示例

下面通过一个实际场景（客户端需要`Payment`接口的`pay()`方法，现有`Alipay`类只有`aliPay()`方法，接口不兼容，通过对象适配器解决）演示完整使用：

```java

// 步骤1：定义目标接口（Target）- 客户端期望的接口
public interface Payment {
    // 客户端需要的支付方法
    void pay(String orderId, double amount);
}

// 步骤2：实现适配者类（Adaptee）- 现有接口不兼容的类（包含核心业务逻辑）
public class Alipay {
    // 适配者原有方法，客户端需要但接口不匹配
    public void aliPay(String orderNumber, BigDecimal money) {
        System.out.println("使用支付宝支付，订单号：" + orderNumber 
                + "，支付金额：" + money + "元，支付成功！");
    }
}

// 步骤3：创建对象适配器类（Adapter）- 组合适配者 + 实现目标接口
public class AlipayAdapter implements Payment {
    // 核心：持有适配者实例（组合关系）
    private Alipay alipay;

    // 构造方法注入适配者对象，灵活指定适配者实例
    public AlipayAdapter(Alipay alipay) {
        this.alipay = alipay;
    }

    // 重写目标接口方法，完成接口转换
    @Override
    public void pay(String orderId, double amount) {
        // 1. 对参数进行适配（客户端传入double，适配者需要BigDecimal）
        BigDecimal money = new BigDecimal(amount);
        // 2. 调用适配者的原有方法，实现核心功能
        alipay.aliPay(orderId, money);
    }
}

// 步骤4：客户端测试 - 只依赖目标接口，无需感知适配者
public class Client {
    public static void main(String[] args) {
        // 1. 创建适配者实例（现有支付宝类）
        Alipay alipay = new Alipay();

        // 2. 创建对象适配器，注入适配者
        Payment paymentAdapter = new AlipayAdapter(alipay);

        // 3. 客户端调用目标接口方法，间接使用适配者功能
        // 客户端无需关心Alipay的存在，也无需知道参数转换逻辑
        paymentAdapter.pay("ORDER_20260101_001", 99.99);
    }
}
```

### 四、运行结果

```text

使用支付宝支付，订单号：ORDER_20260101_001，支付金额：99.99元，支付成功！
```

### 五、扩展说明（多适配者兼容）

对象适配器的灵活性还体现在可兼容多个适配者（只需在适配器中持有多个适配者实例），例如新增`WeChatPay`适配者，无需修改客户端代码，只需新增适配器或扩展现有适配器：

```java

// 新增适配者：微信支付（接口不兼容）
public class WeChatPay {
    public void weChatPay(String tradeNo, double totalFee) {
        System.out.println("使用微信支付，交易号：" + tradeNo 
                + "，支付金额：" + totalFee + "元，支付成功！");
    }
}

// 扩展适配器：支持支付宝+微信支付
public class MultiPayAdapter implements Payment {
    // 持有多个适配者实例
    private Alipay alipay;
    private WeChatPay weChatPay;

    // 构造方法注入（可灵活选择适配者）
    public MultiPayAdapter(Alipay alipay, WeChatPay weChatPay) {
        this.alipay = alipay;
        this.weChatPay = weChatPay;
    }

    @Override
    public void pay(String orderId, double amount) {
        // 可根据业务逻辑选择调用哪个适配者的方法
        if (orderId.startsWith("ALI")) {
            alipay.aliPay(orderId, new BigDecimal(amount));
        } else if (orderId.startsWith("WX")) {
            weChatPay.weChatPay(orderId, amount);
        } else {
            System.out.println("不支持的支付方式，订单号：" + orderId);
        }
    }
}

// 客户端扩展调用
public class ClientExtension {
    public static void main(String[] args) {
        // 创建多个适配者实例
        Alipay alipay = new Alipay();
        WeChatPay weChatPay = new WeChatPay();

        // 创建多适配者适配器
        Payment multiAdapter = new MultiPayAdapter(alipay, weChatPay);

        // 调用统一接口，自动适配不同支付方式
        multiAdapter.pay("ALI_20260101_002", 199.99);
        multiAdapter.pay("WX_20260101_003", 299.99);
    }
}
```

### 总结

1. 对象适配器的核心是**组合关系**（持有适配者实例），而非继承，这是它与类适配器的核心区别；

2. 核心步骤：定义Target接口 → 实现Adaptee类 → 创建Adapter（组合Adaptee+实现Target） → 客户端调用；

3. 关键特性：灵活性高、耦合度低，支持多适配者兼容，完全遵循“合成复用原则”和“开闭原则”；

4. 客户端只需依赖Target接口，无需感知Adaptee的细节，降低了使用成本和系统耦合。
> （注：文档部分内容可能由 AI 生成）