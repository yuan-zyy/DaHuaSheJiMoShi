## 第4章 开闭原则

### 4.1 开闭原则
***开闭原则***（Open/Closed Principle，OCP）

#### 一、开闭原则的核心定义
开闭原则的核心思想是: ***软件实体（类、模块、函数等）***应该对扩展开放，对修改关闭

简单来说：当你需要给软件新增功能时，应该通过***新增代码（扩展）***来实现，而不是去修改已有的、已经测试通过的稳定代码（修改）

#### 二、为什么需要开闭原则？
- 减少修改原有代码带来的风险（原有代码可能被多人适用，修改容易引入bug)
- 提高代码的可维护性和扩展性
- 符合 “单一职责” 的设计思路，让代码逻辑更清晰

#### 三、Java 代码示例
1. 反例（违反开闭原则）

    假设你有一个计算图形面积的功能，初始只能支持矩形
    ```java
    // 计算面积的工具类
    public class AreaCalculator {
        // 计算面积的方法
        public double calculateArea(Object shape) {
            double area = 0.0;
            // 如果是矩形
            if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                area = rect.getWitch() * rect.getHeight();
            }
            // 问题: 当需要新增圆形时，必须修改这个方法
            return area;
        }
    }
    
    // 矩形类
    public class Rectangle {
        private double width;
        private double height;
    
        // 构造器、getter/setter省略
        public Rectangle(double width, double height) {
            this.width = width;
            this.height = height;
        }
    
        public double getWidth() { return width; }
        public double getHeight() { return height; }
    }
    
    ```
    问题: 如果现在需要支持圆形、三角形，必须修改 calculateArea 方法的内部逻辑（加if-else），违反了 ”对修改关闭“ 的原则

2. 正例 （遵循开闭原则）

通过***抽象 + 多态***实现扩展开放、修改关闭
```java
// 1. 定义抽象的图形接口（核心：抽象出公共行为）
public interface Shape {
    // 抽象方法：计算面积
    double calculateArea();
}

// 2. 矩形实现类（扩展：新增图形只需要加实现类）
public class Rectangle implements Shape {
    private double width;
    private double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    // 实现计算面积的方法
    @Override
    public double calculateArea() {
        return width * height;
    }
}

// 3. 圆形实现类（新增功能：无需修改原有代码，直接扩展）
public class Circle implements Shape {
    private double radius;
    private static final double PI = 3.1415926;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return PI * radius * radius;
    }
}

// 4. 面积计算工具类（对修改关闭：永远不要修改这个类）
public class AreaCalculator {
    // 接收抽象的Share，而非具体实现类
    public double calculateArea(Shape shape) {
        return shape.calculateArea();
    }
}

// 测试类
public class Test {
    public static void main(String[] args) {
        AreaCalculator calculator = new AreaCalculator();

        // 计算矩形面积
        Shape rectangle = new Rectangle(5, 3);
        System.out.println("矩形面积：" + calculator.calculateArea(rectangle)); // 输出 15.0

        // 计算圆形面积（扩展功能，无修改原有代码）
        Shape circle = new Circle(2);
        System.out.println("圆形面积：" + calculator.calculateArea(circle)); // 输出 ~12.566
    }
}

```

***核心改进***：
- 抽象 Shape 接口，定义公共的 calculateArea 方法
- 每个具体图形（矩形、圆形）实现接口，负责自己的面积计算逻辑
- AreaCalculator 依赖抽象接口，而非具体实现，新增图形时只需增加实现类，无需修改原因计算逻辑


#### 四、Java中实现开闭原则的常见方式
1. ***抽象类/接口 + 多态****（最核心的方式，如上面的示例）
2. ***设计模式***：策略模式、工厂模式、装饰器模式等都是开闭原则的典型应用</br>
比如装饰器模式：给咖啡加配料（牛奶、糖），无需修改咖啡类，只需要新增配料装饰器
3. 依赖注入：通过Spring等框架注入具体实现，扩展时只需配置新的实现类

#### 总结
1. 开闭原则的核心是扩展开放、修改关闭，新增功能优先通过 “加代码” 而非 “改代码” 实现；
2. 在 Java 中，实现开闭原则的关键是面向抽象编程（依赖接口 / 抽象类，而非具体实现）；
3. 遵循开闭原则能降低代码修改风险，提升代码的可维护性和扩展性，是 SOLID 原则中最具指导意义的原则之一


### 4.2 开闭原则在实际项目中有具体的应用案例
在实际 Java 项目中，开闭原则的应用核心是***通过抽象、设计模式或框架特性***，实现新增功能不修改原有稳定代码，以下是 3 个典型且高频的应用案例：

#### 一、业务规则引擎（策略模式 + 开闭原则）
在电商订单优惠计算场景中，初始需求只有 “满减优惠”，若后续要新增 “折扣优惠”“优惠券优惠”，遵循开闭原则的设计如下：

- 定义抽象策略接口 DiscountStrategy，包含 calculateDiscount(Order order) 方法；
- 原有 FullReduceDiscount 类实现该接口，负责满减逻辑；
- 新增 DiscountRateDiscount（折扣）、CouponDiscount（优惠券）时，无需修改订单计算核心类，只需新增实现类；
- 订单计算类 OrderService 依赖 DiscountStrategy 接口，通过工厂模式或依赖注入切换不同优惠策略。</br>
***优势***：新增优惠类型时，原有订单计算逻辑零修改，避免引入 bug

#### 二、日志框架扩展（装饰器模式 + 开闭原则）
在项目日志功能迭代中，初始只有 “控制台输出日志”，后续要新增 “文件日志”“数据库日志”“脱敏日志” 功能：

- 定义抽象接口 Logger，包含 log(String message) 方法；
- 基础实现类 ConsoleLogger 负责控制台输出；
- 新增功能时，通过装饰器模式创建 FileLoggerDecorator、DbLoggerDecorator、DesensitizeLoggerDecorator，这些装饰器类实现 Logger 接口并包装原有 Logger 对象；
- 调用方只需组合不同装饰器，无需修改原有 ConsoleLogger 代码。
- 典型案例：Java 原生的 java.io 包中，BufferedReader 装饰 Reader、DataInputStream 装饰 InputStream，正是开闭原则的体现

#### 三、基于 Spring 的业务模块扩展（依赖注入 + 开闭原则）
在分布式系统的支付模块中，初始只支持 “支付宝支付”，后续要接入 “微信支付”“银联支付”：
- 定义抽象接口 PaymentService，包含 pay(PaymentDTO dto) 方法；
- 原有 AlipayService 实现该接口，注册为 Spring Bean；
- 新增 WechatPayService、UnionPayService 时，无需修改支付调度类，只需新增实现类并注册 Bean；
- 支付调度类 PaymentDispatcher 通过 @Autowired List<PaymentService> 自动注入所有实现类，根据支付类型动态选择，或通过 @Qualifier 指定。 </br>
***优势***：借助 Spring 依赖注入，扩展支付渠道时，核心调度逻辑完全无需修改，符合开闭原则

#### 四、接口版本管理（适配器模式 + 开闭原则）
在对外 API 接口迭代中，若要兼容旧版本并新增 v2 版本功能：
- 定义基础接口 UserApi，v1 版本实现 UserApiV1；
- 新增 v2 版本时，创建 UserApiV2 实现接口，新增字段和逻辑；
- 通过适配器 UserApiAdapter 统一对外暴露，根据请求版本号路由到不同实现类；
- 原有 v1 接口代码完全不修改，保证老用户系统稳定。

#### 总结
实际项目中，开闭原则的落地离不开 **“面向抽象编程”和设计模式 ** 的结合，本质是将 “易变的业务逻辑” 封装在独立实现类中，让核心稳定代码对修改关闭、对扩展开放