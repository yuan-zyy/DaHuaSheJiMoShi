## 第22章 合成/聚合复用原则 & 桥接模式 

### 22.1 合成/聚合复用原则
**合成 / 聚合复用原则（Composite/Aggregate Reuse Principle，CARP）**，这是面向对象设计五大设计原则（SOLID补充）之一，核心是**优先通过 组合(合成/聚合) 的方式复用已有代码，而非继承**

#### 一、核心定义
合成 / 聚合复用原则（Composite/Aggregate Reuse Principle，CARP）的核心思想：
- 要复用一个类的功能时，**优先将该类作为成员变量嵌入到当前类 (组合/聚合关系)**，通过调用其方法实现功能复用
- 尽量避免使用继承，因为继承会导致类之间的耦合度过高，且受限于单继承（Java中类仅支持单继承）的局限性

#### 二、核心概念区分
##### 1. 合成(Composition，强关联)
- **概念**：表示**整体与部分的强依赖关系**，部分不能脱离整体独立存在（整体生命周期控制部分生命周期）
- **通俗理解**: "有一个" 且 “同生共死”，比如 “汽车有发动机”，发动机不能脱离这两汽车独立存在（汽车销毁时，发动机随之须销毁）
- **Java体现**: 通常通过**成员变量直接初始化或构造方法强传入**，整体类负责创建/销毁部分类对象

##### 2. 聚合(Aggregation，弱关联)
- **概念**：表示**整体与部分的弱依赖关系**，部分可以脱离整体独立存在，多个整体可以共享同一个部分
- **通俗理解**: “有一个” 但 “各自独立”，比如 “团队有员工”，员工可以脱离团队加入其他团队，团队解散员工依然存在
- **Java体现**: 通常通过 **setter方法注入**和**外部注入**，部分类对象的生命周期不受整体类控制

##### 3. 合成/聚合 vs 继承的核心差异


#### 三、为什么优先合成 / 聚合，而非继承？
继承复用存在明显缺陷，而合成 / 聚合复用能规避这些问题，核心优势如下：
##### 1. 降低类之间的耦合度
继承是 “is-a” 关系，子类与父类强耦合，父类的代码修改（如方法签名变更、逻辑调整）会直接影响所有子类；而合成 / 聚合是 “has-a”/“contains-a” 关系，宿主类仅依赖被复用类的公共接口，无需关心其内部实现，耦合度更低

##### 2. 突破 Java 单继承的限制
Java 中类只能单继承，一个子类无法同时继承多个父类来复用多份代码；而合成 / 聚合可以在一个类中嵌入多个不同类型的成员变量，轻松复用多个类的功能

##### 3. 高代码灵活性和可维护性
合成 / 聚合可以通过接口编程（面向抽象编程），动态替换成员变量的实现类（如依赖注入），无需修改宿主类代码；而继承的实现是固定的，子类无法随意替换父类的功能逻辑

##### 4. 避免继承带来的 “冗余继承” 问题
为了复用某个父类的一个小功能，可能需要继承整个父类，导致子类继承了许多无关的属性和方法，造成类结构臃肿

#### 四、Java 代码示例: 对比继承复用与组合/聚合复用
##### 1. 反面案例: 继承复用的问题
假设我们需要实现一个 “微信支付订单” 类，先定义一个父类Payment
```java
// 支付父类
public class Payment {
    // 支付核心逻辑
    public void pay(double amount) {
        System.out.println("执行支付流程，支付金额：" + amount);
    }
    
    // 微信支付专属逻辑（后续新增，会影响所有子类）
    public void wechatPayCallback() {
        System.out.println("微信支付回调处理");
    }
}

// 微信支付订单（通过继承复用 Payment 的功能）
public class WechatOrder extends Payment {
    private String orderNo;
    
    public WechatOrder(String orderNo) {
        this.orderNo = orderNo;
    }
    
    // 订单支付方法
    public void orderPay(double amount) {
        System.out.println("订单号：" + orderNo);
        // 复用父类的支付逻辑
        pay(amount);
        // 复用父类的微信支付回调逻辑
        wechatPayCallback();
    }
}
```

**问题暴露**
- 若后续新增 “支付宝订单”，Payment中的wechatPayCallback方法对支付宝订单毫无意义，但子类仍会继承该方法，造成冗余。
- 若修改Payment的pay方法逻辑，所有子类（WechatOrder、后续的AlipayOrder）都会受到影响，耦合度过高

##### 2. 正面示例: 组合/聚合复用
基于接口编程，通过组合 / 聚合实现灵活复用，规避继承的缺陷：

**步骤1: 定义支付行为接口（抽象功能，不涉及具体实现）**
```java
// 支付接口（定义支付的标准行为）
public interface Payment {
    void pay(double amount); // 支付核心方法
}
```

**步骤2: 定义支付行为接口（抽象功能，不涉及具体实现）**
```java
// 微信支付实现类
public class WechatPayment implements IPayment {
    // 微信支付具体实现
    @Override
    public void pay(double amount) {
        System.out.println("微信支付：" + amount + " 元");
    }

    // 微信支付专属回调方法（仅微信支付自身拥有，不会被其他类继承）
    public void wechatPayCallback() {
        System.out.println("微信支付回调处理");
    }
}

// 支付宝支付实现类（后续扩展，无需修改原有代码）
public class AlipayPayment implements IPayment {
    // 支付宝支付具体实现
    @Override
    public void pay(double amount) {
        System.out.println("支付宝支付：" + amount + " 元");
    }
}
```

**步骤3: 订单类通过聚合复用支付类的功能**
```java
// 订单类（通过聚合IPayment实现支付功能的复用）
public class Order {
    private String orderNo;
    private IPayment payment; // 聚合：订单包含支付对象（松散关联，可动态替换）

    // 构造方法注入支付对象（聚合的体现：外部传入支付实现，生命周期独立）
    public Order(String orderNo, IPayment payment) {
        this.orderNo = orderNo;
        this.payment = payment;
    }

    // 订单支付方法（复用支付对象的功能）
    public void orderPay(double amount) {
        System.out.println("订单号：" + orderNo);
        // 调用支付对象的支付方法（黑箱复用：只关心IPayment接口，不关心具体实现）
        payment.pay(amount);

        // 若为微信支付，额外处理回调（按需复用专属功能）
        if (payment instanceof WechatPayment) {
            ((WechatPayment) payment).wechatPayCallback();
        }
    }

    // 提供setter方法，支持运行时动态替换支付方式（灵活性更高）
    public void setPayment(IPayment payment) {
        this.payment = payment;
    }
}
```

**步骤 4：测试代码（验证组合 / 聚合的灵活性）**
```java
public class Test {
    public static void main(String[] args) {
        // 1. 创建微信支付订单（聚合微信支付对象）
        IPayment wechatPay = new WechatPayment();
        Order wechatOrder = new Order("WX20260102001", wechatPay);
        wechatOrder.orderPay(100.0);

        System.out.println("------------------------");

        // 2. 创建支付宝支付订单（聚合支付宝支付对象，无需修改Order类）
        IPayment alipay = new AlipayPayment();
        Order alipayOrder = new Order("AL20260102001", alipay);
        alipayOrder.orderPay(200.0);

        System.out.println("------------------------");

        // 3. 运行时动态修改订单的支付方式
        wechatOrder.setPayment(alipay);
        wechatOrder.orderPay(150.0);
    }
}
```

运行结果：
```text
订单号：WX20260102001
微信支付：100.0 元
微信支付回调处理
------------------------
订单号：AL20260102001
支付宝支付：200.0 元
------------------------
订单号：WX20260102001
支付宝支付：150.0 元
```

**优势体现**
- 低耦合：Order类仅依赖IPayment接口，与具体的支付实现（WechatPayment、AlipayPayment）解耦，修改支付实现不会影响Order类。
- 高灵活：可通过构造方法 /setter方法动态注入不同的支付对象，甚至运行时替换支付方式，扩展方便（符合 “开闭原则”）。
- 无冗余：每个支付类仅实现自身专属功能，不会给其他类造成冗余方法，类结构清晰


**合成（组合）示例（补充）**

若要实现 “汽车与发动机” 的强关联（合成关系），代码如下：
```java
// 发动机类
public class Engine {
    public void start() {
        System.out.println("发动机启动");
    }

    public void stop() {
        System.out.println("发动机关闭");
    }
}

// 汽车类（合成：内部直接创建发动机对象，生命周期绑定）
public class Car {
    private String carNo;
    private Engine engine; // 合成：汽车包含发动机，发动机无法脱离汽车独立存在

    // 构造方法中直接创建发动机对象（无需外部传入，生命周期由Car控制）
    public Car(String carNo) {
        this.carNo = carNo;
        this.engine = new Engine(); // 合成的核心体现：内部初始化部分对象
    }

    // 汽车启动（复用发动机的功能）
    public void startCar() {
        System.out.println("汽车" + carNo + "准备启动");
        engine.start();
        System.out.println("汽车" + carNo + "启动成功");
    }

    // 汽车停止（复用发动机的功能）
    public void stopCar() {
        System.out.println("汽车" + carNo + "准备停止");
        engine.stop();
        System.out.println("汽车" + carNo + "停止成功");
    }
}
```

#### 总结
1. **合成/聚合复用原则核心**：优先组合/聚合，少用继承（能用组合/聚合解决的复用问题，绝不使用继承）。
2. **聚合与合成的关键区别**：整体与部分的生命周期是否绑定（聚合松散、合成紧密）。
3. **核心优势**：降低类间耦合、提高代码灵活性和可扩展性、符合封装思想（黑箱复用）。
4. **Java 实现技巧**：基于接口编程，通过"构造方法注入"（聚合）、"内部初始化"（合成）、setter方法注入等方式实现组合复用。
5. **与其他设计原则的关联**：合成/聚合复用原则通常与"依赖倒置原则"、"开闭原则"配合使用，共同构建高内聚、低耦合的面向对象设计


### n.2 

#### 一、

#### 二、

#### 总结
