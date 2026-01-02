# Java 中合成/聚合复用原则的实现与应用

你想知道在Java中具体如何实现合成/聚合复用原则，我们可以从**核心实现思路**、**两种关联关系的具体编码方式**、**关键技巧**和**完整示例**几个方面展开，让实现过程清晰可落地。

### 一、核心实现思路

在Java中实现合成/聚合复用原则的核心是：**放弃继承的“is-a”关系，采用组合/聚合的“has-a”（聚合）或“contains-a”（合成）关系，通过持有其他类的对象引用，调用其公开方法来复用功能，而非继承其实现**。

核心步骤提炼：

1. 优先定义**接口**（抽象功能契约），解耦具体实现；

2. 实现具体功能类（专注自身业务逻辑，不依赖继承复用）；

3. 在需要复用功能的“整体类”中，**持有功能类的对象引用**（核心体现）；

4. 通过该引用调用功能类的方法，实现代码复用，同时灵活控制依赖关系。

### 二、分场景实现：聚合（Aggregation）

聚合是**松散的关联关系**（整体与部分生命周期独立，部分可脱离整体存在、可被共享），在Java中有明确的编码实现方式。

#### 1. 聚合的核心实现方式

在Java中，聚合通常通过以下两种方式实现（核心是“外部传入部分对象”，而非内部创建）：

- **构造方法注入**：通过整体类的构造方法，接收外部创建的部分对象，初始化引用；

- **Setter方法注入**：提供`setXxx()`方法，允许在运行时动态设置/替换部分对象；

这两种方式都能保证部分对象的生命周期由外部控制，与整体类解耦。

#### 2. 聚合实现代码示例

```java

// 步骤1：定义功能接口（抽象支付行为，解耦具体实现）
public interface IPayment {
    void pay(double amount); // 支付标准行为
}

// 步骤2：实现具体功能类（微信支付，专注自身逻辑）
public class WechatPayment implements IPayment {
    @Override
    public void pay(double amount) {
        System.out.println("微信支付：" + amount + " 元");
    }
}

// 步骤3：实现具体功能类（支付宝支付，扩展方便）
public class AlipayPayment implements IPayment {
    @Override
    public void pay(double amount) {
        System.out.println("支付宝支付：" + amount + " 元");
    }
}

// 步骤4：整体类（订单），通过聚合持有IPayment引用，复用支付功能
public class Order {
    private String orderNo;
    private IPayment payment; // 聚合核心：持有部分对象的引用（松散关联）

    // 方式1：构造方法注入（初始化时绑定支付对象，生命周期独立）
    public Order(String orderNo, IPayment payment) {
        this.orderNo = orderNo;
        this.payment = payment; // 外部传入部分对象，非内部创建
    }

    // 方式2：Setter方法注入（运行时动态替换支付对象，灵活性更高）
    public void setPayment(IPayment payment) {
        this.payment = payment;
    }

    // 复用支付功能：通过引用调用部分对象的方法
    public void doPay(double amount) {
        System.out.println("订单号：" + orderNo);
        payment.pay(amount); // 黑箱复用：只依赖接口，不关心具体实现
    }
}

// 测试类
public class AggregationTest {
    public static void main(String[] args) {
        // 1. 外部创建部分对象（支付对象），生命周期由外部控制
        IPayment wechatPay = new WechatPayment();
        IPayment alipay = new AlipayPayment();

        // 2. 构造方法注入：创建订单（整体），关联微信支付（部分）
        Order order = new Order("ORDER_20260102_001", wechatPay);
        order.doPay(100.0);

        // 3. Setter方法注入：运行时动态替换为支付宝支付，无需修改Order类
        order.setPayment(alipay);
        order.doPay(200.0);
    }
}
```

#### 3. 聚合实现的特点

- 部分对象（`WechatPayment`/`AlipayPayment`）可被多个订单（整体）共享；

- 订单销毁（对象回收）不会导致支付对象销毁，生命周期独立；

- 可灵活替换部分对象，符合“开闭原则”，耦合度极低。

### 三、分场景实现：合成（Composition，又称组合）

合成是**紧密的依赖关系**（整体与部分生命周期绑定，部分无法脱离整体独立存在），其Java编码实现方式与聚合有明显区别。

#### 1. 合成的核心实现方式

在Java中，合成的核心实现方式是：**在整体类的内部直接创建部分对象**（无需外部传入），部分对象的创建与销毁完全由整体类控制。

- 通常在整体类的**构造方法中初始化部分对象**；

- 部分对象仅为当前整体类服务，不对外暴露，也无法被其他整体共享；

#### 2. 合成实现代码示例

```java

// 步骤1：定义部分类（发动机，无法脱离汽车独立存在）
public class Engine {
    // 发动机自身功能
    public void start() {
        System.out.println("发动机启动，转速逐渐升高");
    }

    public void stop() {
        System.out.println("发动机关闭，转速归0");
    }
}

// 步骤2：整体类（汽车），通过合成持有Engine引用，复用发动机功能
public class Car {
    private String carNo;
    private Engine engine; // 合成核心：持有部分对象的引用（紧密关联）

    // 构造方法中直接创建部分对象（无需外部传入，生命周期由Car控制）
    public Car(String carNo) {
        this.carNo = carNo;
        this.engine = new Engine(); // 合成关键：内部初始化部分对象
    }

    // 复用发动机功能：调用其方法
    public void startCar() {
        System.out.println("汽车" + carNo + "准备启动");
        engine.start(); // 复用发动机启动功能
        System.out.println("汽车" + carNo + "启动成功");
    }

    public void stopCar() {
        System.out.println("汽车" + carNo + "准备停止");
        engine.stop(); // 复用发动机关闭功能
        System.out.println("汽车" + carNo + "停止成功");
    }
}

// 测试类
public class CompositionTest {
    public static void main(String[] args) {
        // 创建汽车（整体）时，内部自动创建发动机（部分）
        Car bmw = new Car("京A88888");
        bmw.startCar();
        System.out.println("------------------------");
        bmw.stopCar();

        // 无法单独使用发动机（逻辑上不可行，代码上也无法外部注入）
        // Engine engine = new Engine();  // 代码可创建，但业务上无独立意义
    }
}
```

#### 3. 合成实现的特点

- 部分对象（`Engine`）由整体类（`Car`）内部创建，生命周期完全绑定：汽车创建则发动机创建，汽车销毁则发动机随之回收；

- 部分对象无法被其他整体共享，仅服务于当前整体；

- 耦合度相对聚合更高，但逻辑上更紧密，符合业务场景（如汽车与发动机、电脑与主板）。

### 四、实现合成/聚合复用原则的关键技巧

1. **基于接口编程（依赖倒置原则）**：优先定义接口来抽象功能，整体类仅持有接口引用，而非具体实现类引用。这样可以灵活替换具体实现，进一步降低耦合，比如上述示例中`Order`持有`IPayment`接口，而非`WechatPayment`具体类。

2. **优先使用Setter方法注入（聚合场景）**：Setter方法相比构造方法，支持运行时动态替换部分对象，灵活性更高，更能体现聚合的松散关联特性。

3. **避免暴露部分对象的内部细节**：整体类应仅对外提供自身的业务方法，不对外暴露所持有的部分对象（如不提供`getEngine()`方法返回`Engine`对象），保证“黑箱复用”，符合封装思想。

4. **区分场景选择聚合或合成**：
        

    - 若部分可独立存在、需灵活替换/共享，选择**聚合**（如订单与支付方式、学校与教师）；

    - 若部分无法独立存在、与整体强绑定，选择**合成**（如汽车与发动机、用户与用户身份证）。

### 五、核心总结

在Java中实现合成/聚合复用原则的关键要点：

1. 核心思想：**持有对象引用，调用方法复用功能**，替代继承的“白箱复用”；

2. 聚合实现：通过「构造方法注入」或「Setter方法注入」获取部分对象，生命周期独立；

3. 合成实现：在整体类内部（通常是构造方法）直接创建部分对象，生命周期绑定；

4. 最佳实践：基于接口编程，隐藏部分对象细节，根据业务场景灵活选择聚合或合成；

5. 最终效果：降低类间耦合，提高代码灵活性和可扩展性，符合开闭原则。
> （注：文档部分内容可能由 AI 生成）