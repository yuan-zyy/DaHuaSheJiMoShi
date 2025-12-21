## 第2章 商场促销 - 策略模式

### 2.1 商场促销系统 - 简单工厂实现



### 2.2 商场促销系统 - 策略模式
策略模式是一种**行为型设计模式**，核心思想是**将算法（策略）封装为独立的类**，让他们可以相互替换，使算法的变化独立于使用算法的客户端

#### 一、核心角色
策略模式包含3个关键角色，职责划分清晰：
1. 抽象策略（Strategy）
   - 定义所有支持的算法的公共接口（通常是Java接口或者抽象类）
   - 约束具体策略类必须实现的方法
2. 具体策略（ConcreteStrategy）
   - 实现抽象策略接口，封装具体的算法逻辑
   - 多个具体策略类对应不同的算法实现
3. 上下文（Context）
    - 持有抽象策略的引用，提供给客户端调用
    - 不直接实现算法，而是**委托给策略对象**执行
    - 可提供方法动态切换策略
#### 二、Java 代码实现案例
以**支付方式选择**场景为例，用于可在支付宝、微信、银行卡三种支付方式中动态切换
1. 定义抽象策略（支付策略接口）
    ```java
    // 抽象策略: 支付策略的公共接口
    public interface PaymentStrategy {
        /**
         * 支付方法
         * @param amount 支付金额
         */
        void pay(double amount);
    }
    ```
2. 实现具体策略（不同支付方式）
    ```java
    // 具体策略1: 支付宝支付
    public class AlipayStrategy implements PaymentStrategy {
        @Override
        public void pay(double amount) {
            System.out.println("使用支付宝支付: " + amount + "元");
            // 可扩展真实支付逻辑: 接口调用、签名验证登
        }
    }
    
    // 具体策略2: 微信支付
    public class WechatPayStrategy implements PaymentStrategy {
        @Override
        public void pay(double amount) {
            System.out.println("使用微信支付: " + amount + "元");
        }
    }
    
    // 具体策略2: 银行卡支付
    public class BankCardStrategy implements PaymentStrategy {
        @Override
        public void pay(double amount) {
            System.out.println("使用银行卡支付: " + amount + "元");
        }
    }
    ```
3. 定义上下文
    ```java
    import java.util.Map;
    
    // 上下文: 订单，持有支付策略并提供调用入口
    public class Order {
        private double amount;  // 订单金额
        private PaymentStrategy paymentStrategy; // 支付策略引用
    
        // 构造方法: 初始化金额和策略
        public Order(double amount, PaymentStrategy paymentStrategy) {
            this.amount = amount;
            this.paymentStrategy = paymentStrategy;
        }
    
        /**
         * 动态切换策略
         * @param paymentStrategy
         */
        public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
            this.paymentStrategy = paymentStrategy;
        }
        
        // 执行支付（委托给策略对象）
        public void payOrder() {
            paymentStrategy.pay(amount);
        }
    }
    ```
4. 客户端测试代码
```java
// 客户端: 调用上下文执行策略
public class StrategyPatternDemo {
    public static void main(String[] args) {
        // 1. 创建订单对象
        Order order = new Order(100.0, new AlipayStrategy());
        order.payOrder();   // 输出: 使用支付宝支付: 100.0 元
        
        // 2. 动态切换为微信支付
        order.setPaymentStrategy(new WechatPayStrategy());
        order.payOrder(); // 输出: 使用微信支付: 100.0 元

        // 3. 再切换为银行卡支付
        order.setPaymentStrategy(new BankCardStrategy());
        order.payOrder(); // 输出: 使用银行卡支付: 100.0 元
    }
}
```
#### 三、适用场景

| 场景特征                        | 示例                                            |
|-----------------------------|-----------------------------------------------|
| 系统需要动态选择多种算法中的一种            | 支付方式切换、排序算法切换                                 |
| 多个类的核心逻辑相同，近算法或者行为不同        | 不同的日志存储方式（本地文件/数据局/云存储）                       |
| 避免适用大量的 if-else/switch 判断算法 | 替代 if(支付类型 == 支付宝){...}else if(支付类型==微信){...} |

#### 四、优缺点对比

| 优点                                    | 缺点                        |
|---------------------------------------|---------------------------|
| 符合***开闭原则***: 新增策略无需修改原有代码，只需要新增具体策略类 | 客户端必须了解所有策略，才能选择适合的实现     |
| 算法解耦: 策略与客户端分立，便于维护和引用                | 策略数量可能膨胀: 每新增一种算法就需要新增一个类 |
| 支持运行时***动态切换策略***                     | 上下文和抽象策略之间存在依赖            |

#### 五、核心总结
1. 策略模式的本质是 ***封装数据***，将异变的算法逻辑独立出来
2. 核心价值是***消除分支判断***，提升代码灵活性和可维护性
3. 与工厂模式搭配使用: 可通过工厂模式创建策略对象，降低客户端对具体策略的感知

### 2.3 扩展 - 策略模式理解
#### 2.3.1 策略类
##### 一、抽象策略类 （Strategy）的核心作用
抽象策略类 （通常是接口或者抽象类）是所有具体策略的的 “契约”，它的作用是***定义规则、统一接口、约束行为***，具体体现这三点
1. 统一接口调度接口
定义所有具体策略必须实现的核心方法，让上下文（Context）无需关心具体策略的实现细节，只需调用这个统一接口
   - 示例: PaymentStrategy 定义接口 pay(double amount) 方法，不管是支付宝还是微信支付，上下文都只调用 pay()，无需区分具体类型
   - 价值: 消除上下文与具体策略的耦合，新增策略时上下文代码无需修改（符合开闭原则）
2. 抽离公共逻辑 （抽象类场景）
如果多个具体策略由公共逻辑（如支付前的参数校验、日志记录），可把这些逻辑抽离到抽象策略类中，避免代码重复
    - 示例: AbstractPaymentStrategy 中实现 checkParams() 公共校验方法，所有支付策略都能复用
    - 价值: 复用 “复用原则”，减少冗余代码，便于统一维护公共逻辑
3. 约束策略的行为边界
明确策略必须实现的方法和输入输出规范，避免具体策略 “随心所欲” 实现，保证所有策略行为的一致性
    - 示例: 规定 pay() 方法必须接受 double 类型金额，返回无值（或统一的结果枚举），避免有的策略用 int、有的用 String 导致上下文调用出错

##### 二、具体策略类 （ConcreteStrategy）的核心作用
具体策略类是 算法/行为 的***实际载体***，它的作用是***封装差异化逻辑、实现独立可替换的算法***，具体体现在这4点:
###### 1. 封装单一且独立的算法/行为
每个具体策略只实现一种 算法/行为，且逻辑内聚（单一职责原则），避免一个策略混杂多种逻辑
- 示例: AlipayStrategy 只封装支付宝支付的逻辑，WechatPayStrategy 只封装微信支付的逻辑，彼此独立
- 价值: 代码可读性、可维护性提升，定位问题时只需关注对应策略类

###### 2. 实现算法的可替换性
所有具体策略类都遵循抽象策略的接口规范，因此可以在上下文中动态替换，不影响整体逻辑
- 示例: 订单对象可随时把支付策略从 AlipayStrategy 切换为 WechatPayStrategy，只需调用 setPaymentStrategy()，无需修改订单的支付逻辑
- 价值: 运行时灵活调整行为，适配不同场景（比如用户选择不同支付方式、系统根据时段切换促销策略）

###### 3. 隔离算法的变化影响
算法的修改只影响对应的具体策略，不会扩展到上下文或其他策略
- 示例: 修改支付宝的支付的签名算法，只需修改 AlipayStrategy 中的 generateSign() 方法，微信支付、银行卡支付的代码完全不受影响
- 价值: 降低修改风险，避免 "牵一发而动全身"

###### 4. 支持算法的扩展
新增一种 算法/行为 时，只需新增一个具体策略类，无需修改原有代码
- 示例: 要支持 “数字人民币支付”，只需新增 DigitalRMBStrategy 实现 PaymentStrategy，上下文和其他策略类无需任何修改
- 价值: 完美契合 “开闭原则”，时策略模式最核心的价值体现


#### 三、策略类的 “隐形作用”: 消除分支判断
这是策略类带来的核心业务价值，也是适用策略模式的核心动机

- 无策略类时: 需用大量 if-else 判断选择算法，比如:
```java
// 糟糕的写法
if (payType.equals("alipay")) {
    // 支付宝逻辑
} else if (payType.equals("wechat")) {
    // 微信支付    
}
```
- 有策略类时: 通过策略对象直接调用，无需分支判断，比如:
```java
// 优雅的写法
paymentStrategy.pay(amount); // 策略类已封装好逻辑，上下文无需判断
```
- 价值: 代码更简洁，新增策略时无需加新的 else if，避免分支嵌套过深导致的可读性差、维护难

#### 四、策略类的使用边界（避免滥用）
理解作用的同时，也要清楚策略类的使用场景，避免为了用模式而用
1. 当 算法/行为 是 ”易变的“ ”多版本的“，适合封装为策略类
2. 当 算法逻辑简单且固定，无需封装为策略类（比如只有一种支付方式，没必要拆策略）
3. 当 策略数量过多（比如几十种），需要结合 工厂模式、缓存机制 管理，避免类膨胀

#### 总结
1. 抽象策略类: 核心是***定规则、抽公共、约束边界***，是策略模式的骨架，保证所有策略的一致性和复用性
2. 具体策略类: 核心是***封逻辑、可替换、易扩展***，是策略模式的 “血肉”，实现算法的独立封装和动态切换
3. 策略类的终极价值: 让 算法/行为 与使用方解耦，消除分支判断，提升代码的灵活性、可维护性和扩展性

#### 2.3.2 上下文（Context）
上下文是策略的 “管理者 + 调度者 + 封装者”，它不做具体算法实现，而是负责把策略和业务场景结合起来，对客户端屏蔽复杂的策略细节，是连接客户端和策略类的核心桥梁

##### 一、上下文的核心作用（6个核心，按重要性排序）
1. 持有策略引用，统一策略调用入口（最核心）
上下文内部维护***抽象策略***的引用（而非策略），对外提供统一的调用方法，让客户端完全不用接触策略类，只需调用上下文的方法即可
    ```java
    public class Order {    // 上下文
        private PaymentStrategy strategy; // 持有抽象策略引用
        
        // 对外统一入口: 客户端只调用这个方法
        public void payOrder() {
            strategy.pay(amount);   // 委托执行策略
        }
    }
    
    // 客户端调用: 完全看不到策略类的细节
    Order order = new Order(100, new AlipayStrategy());
    order.payOrder(); // 只需调用上下文的方法
    ```
    - 核心价值: 对客户端屏蔽策略的存在，降低客户端的使用成本；同时因为依赖抽象策略，新增具体策略时上下文无需修改（符合开闭原则）

2. 封装业务场景逻辑，补充策略的 “上下文信息”
策略类只负责纯算法（比如 “怎么支付”），而上下文会封装与业务场景相关的逻辑和数据（比如订单金额、订单状态、用户信息），并把这些信息传递给策略
   - 实战案例
    ```java
    public class Order {
        private double amount; // 业务数据: 订单金额
        private OrderStatus status; // 业务数据: 订单状态
        
        public void payOrder() {
            // 上下文的业务逻辑: 校验订单状态（策略类不关心这个）
            if (status != OrderStatus.UNPAID) {
                throw new RuntimeException("订单未待支付");
            }
            // 把业务数据传递给策略
            strategy.pay(amount);
            // 上下文的业务逻辑: 支付后更新订单状态（策略类不处理这个）
            this.status = OrderStatus.PAID;
        }
    }
    ```
    - 核心价值: 策略类专注 "算法本身"，上下文转出 “业务场景”，职责分离（单一职责原则）；避免策略类混杂业务逻辑，导致策略无法复用

3. 动态管理策略（切换/替换）
上下文提供方法让策略可以在运行时动态切换，无需重新创建上下文对象，这是策略模式 "灵活替换算法" 的关键支撑
   - 实战案例:
    ```java
    public class Order {
        // 提供切换策略的方法
        public void setPaymentStrategy(PaymentStrategy strategy) {
            this.strategy = strategy;
        }
    }
    
    // 客户端动态切换策略
    order.setPaymentStrategy(new WechatPayStrategy());
    order.payOrder(); // 此时用微信支付
    ```
   - 核心价值: 支持业务场景的动态调整（比如用户支付临时切换支付方式、系统根据时段切换促销模式策略），提升代码的灵活性

4. 屏蔽策略执行的复杂度，简化客户端操作
如果策略的执行需要多步 前置/后置 操作，上下文可以把这些操作封装成一个 “一站式” 方法，客户端只需调用一次即可完成全流程
   - 实战案例
    ```java
    public class Order {
        public void payOrder() {
            // 前置操作: 校验订单、记录支付日志
            checkOrder();
            recordPayLog("开始支付");
            // 核心: 执行策略
            strategy.pay(amount);
            // 后置操作: 更新状态、发送支付成功通知
            updateStatus();
            sendNotify();
        }
    }
    
    // 客户端: 只需要调用一次 payOrder()，无需关心内部多步操作
    order.payOrder();
    ```
   - 核心价值: 客户端无需了解策略执行的完整流程，降低使用门槛；同时把多步操作封装在上下文，便于统一维护

5. 负责策略的创建/缓存（进阶）
在复杂场景下，上下文（或配合工厂类）可负责策略对象的创建、缓存，避免重复创建开销大的策略对象（比如需要初始化资源的策略）
   - 实战案例
    ```java
    import java.util.HashMap;
    
    public class Order {
        // 缓存常用策略，避免重复创建
        private static final Map<String, PaymentStrategy> STRATEGY_CACHE = new HashMap<>();
        
        static {
            STRATEGY_CACHE.put("ALIPAY", new AlipayStrategy());
            STRATEGY_CACHE.put("WECHAT", new WechatStrategy());
        }
        
        // 上下文内部 创建/获取 策略
        public void initStrategy(String type) {
            this.strategy = STRATEGY_CACHE.get(type);
        }
    }
    ```
   - 核心价值: 优化性能，减少策略对象的创建开销；同时集中管理策略的生命周期，避免内存泄漏

6. 处理策略执行的结果，适配业务输出
策略执行后可能返回原始结果（比如接口返回JSON字符串），上下文可对结果进行解析、抓换，适配业务层的输出要求
- 实战案例

```java

public class Order {
    public String payOrder() {
        // 策略的原始返回结果
        String rawResult = strategy.pay(amount);
        // 上下文解析结果，返回业务层需要的格式
        if (rawResult.contains("success")) {
            this.status = OrderStatus.PAID;
            return "支付成功，订单号: " + this.orderId;
        } else {
            return "支付失败: " + rawResult;
        }
    }
}
```
- 核心价值: 策略类只需要返回结果算法，无需适配业务输出；上下文统一处理结果格式，保证业务输出的一致性

##### 二、上下文的 “不可替代” 性 （为什么不能没有上下文）
很多新手会问: "能不能让客户端直接调用策略类? 为什么要加上下文?" --- 答案是: 可以，但会丧失策略模式的核心价值:
1. 客户端需直接接触所有具体策略类，耦合度极高;
2. 业务逻辑（如订单状态更新）会混杂在客户端和策略类中，代码混乱;
3. 无法动态切换策略（客户端需要重新创建策略对象，无法复用上下文）;
4. 客户端需要处理策略执行的全流程（校验、日志、结果解析）、代码冗余

##### 总结
1. 上下文的核心是 ***策略的管理者***: 持有策略引用、统一调用入口、动态切换策略，是策略模式 “可替换” 的核心支撑
2. 上下文的价值是 ***业务与算法的解耦者***: 封装业务场景逻辑，让策略专注算法，客户端专注调用上下文，三层职责清晰
3. 上下文的定位是***客户端的屏蔽层***: 对客户端隐藏策略细节、执行流程，简化调用，提升代码的可维护性和灵活性


### 2.4 扩展 - 策略模式的应用场景
“通用场景 + 行业场景 + JDK/框架内置场景”分类

#### 一、通用核心应用场景（所有项目都可能遇到）
这是策略模式最基础、最常用的场景，覆盖 80% 的使用需求:
1. 业务规则/算法 的动态切换

    ***场景特征***: 同一业务有多种规则/算法，需要根据场景（用户选择、系统配置、运行时条件）动态切换

    ***问题痛点***: 用 if-else 判断规则类型，新增规则时需修改原有代码，违反开闭原则

    ***实战案例***:
   - 支付方式选择（支付宝/微信/银行卡/数字人民币）
   - 排序算法切换（升序/降序/自定义排序）
   - 日志输出方式切换（控制台/文件/数据库/ELK）

    ***策略模式解法***:
   - 抽象策略: 定义 PaymentStrategy/SorStrategy/LogStrategy 接口;
   - 具体策略: 每种 支付/排序/日志 方式对应一个具体策略类
   - 上下文: 订单/排序工具/日志工具 持有策略引用，提供切换方法

2. 消除大量分支判断（if-else/switch地狱）

   ***场景特征***：业务逻辑中存在大量分支判断，每个分支对应一种独立逻辑，且分支可能持续新增。

    ***问题痛点***：分支嵌套过深，代码可读性差，修改一个分支可能影响其他分支，维护成本高

   ***实战案例***:
    - 促销规则计算（满减 / 折扣 / 优惠券 / 拼团，不同规则对应不同折扣逻辑）；
    - 数据校验规则（手机号 / 邮箱 / 身份证，不同类型对应不同校验逻辑）；
    - 消息发送渠道（短信 / 邮件 / 推送，不同渠道对应不同发送逻辑

   ***策略模式解法***:
   - 把每个分支的逻辑封装为一个具体策略类；
   - 用策略工厂替代分支判断（根据类型获取对应策略）；
   - 上下文调用策略执行逻辑，无需任何分支
   
    ***对比案例***
    ```java
    // 优化前：if-else 地狱
    public double calculatePrice(double price, String promotionType) {
        if ("full_reduce".equals(promotionType)) {
            return price - 20;
        } else if ("discount".equals(promotionType)) {
            return price * 0.9;
        } else if ("coupon".equals(promotionType)) {
            return price - 10;
        }
        return price;
    }
    
    // 优化后：策略模式（无分支）
    public double calculatePrice(double price, PromotionStrategy strategy) {
        return strategy.calculate(price);
    }
    ```
3. 多版本算法 / 接口适配

   ***场景特征***：同一功能有多个版本（如接口 V1/V2、老版 / 新版算法），需要兼容或按需切换。

   ***问题痛点***：版本逻辑混杂在一个类中，代码冗余，切换版本需修改大量代码。

   ***实战案例***:
   - 接口适配（第三方支付接口 V1/V2，不同版本参数 / 签名规则不同）；
   - 风控规则版本（V1 按金额风控，V2 按用户等级 + 金额风控）；
   - 数据解析规则（XML/JSON/CSV，不同格式对应不同解析逻辑）。
   
   ***策略模式解法***:
   - 每个版本对应一个具体策略类；
   - 上下文根据版本号动态加载对应策略；
   - 新增版本只需新增策略类，无需修改原有逻辑

#### 二、行业专属应用场景（按领域划分）
1. 电商领域
   - 订单拆分策略（按仓库 / 物流 / 品类拆分订单）；
   - 物流配送策略（同城配送 / 普通快递 / 冷链配送）；
   - 退款规则策略（七天无理由 / 质量问题 / 缺货退款）。
   
2. 金融领域
   - 风控策略（实时风控 / 离线风控 / 大额风控）；
   - 费率计算策略（不同用户等级 / 交易金额对应不同费率）；
   - 撮合交易策略（限价撮合 / 市价撮合 / 最优价撮合）。
   
3. 游戏领域
   - 角色技能策略（物理攻击 / 魔法攻击 / 控制技能）；
   - 怪物 AI 策略（主动攻击 / 逃跑 / 伏击）；
   - 奖励发放策略（新手奖励 / 日常奖励 / 节日奖励）。
   
4. 大数据 / AI 领域
   - 数据清洗策略（去重 / 脱敏 / 格式转换）；
   - 模型推理策略（不同场景加载不同 AI 模型）；
   - 任务调度策略（定时执行 / 触发式执行 / 批量执行）。


#### 三、JDK/主流框架中的内置应用场景
策略模式在 Java 生态中被广泛使用，这些场景可以作为学习参考

| 场景                   | 抽象策略类                    | 具体策略实现                                          | 上下文/调用方               |
|----------------------|--------------------------|-------------------------------------------------|-----------------------|
| 集合排序                 | java.util.Comparator<T>  | 自定义 Comparator 实现类                              | Collections.sort()    |
| 线程池拒绝策略              | RejectedExecutionHandler | AbortPolicy/CallerRunsPolicy 等                  | ThreadPoolExecutor    |
| Spring 资源加载	         | ResourceLoader	          | DefaultResourceLoader/FileSystemResourceLoader	 | ApplicationContext    | 
| MyBatis 分页插件	        | PaginationDialect        | MySQLDialect/OracleDialect 等	                   | PageHelper            |
| Spring Security 认证 	 | AuthenticationProvider	  | DaoAuthenticationProvider 等	                    | AuthenticationManager | 

***示例: ThreadPoolExecutor 的拒绝策略***
```java
// 抽象策略：RejectedExecutionHandler
// 具体策略：AbortPolicy（直接抛异常）、CallerRunsPolicy（调用者线程执行）
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5, 10, 60, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100),
    new ThreadPoolExecutor.CallerRunsPolicy() // 动态指定拒绝策略
);

// 切换策略只需替换最后一个参数，无需修改线程池核心逻辑
executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
```

#### 四、策略模式的 “反场景”（不适合用的情况）
避免滥用策略模式，以下场景建议不用：
1. 算法 / 行为固定不变，且逻辑简单（比如只有一种支付方式，没必要拆策略）；
2. 策略逻辑极少（只有几行代码），用 Lambda 即可替代（无需创建大量策略类）；
3. 客户端必须感知所有策略，且策略数量极少（2-3 个），用简单分支更简洁

#### 总结
1. 策略模式的核心适用场景：多可替换算法 / 行为、需动态切换、消除分支判断，这三类场景用策略模式能显著提升代码灵活性和可维护性；
2. 通用场景覆盖支付、促销、排序等基础业务，行业场景可根据领域特性适配，JDK / 框架的内置场景可参考其设计思路；
3. 关键判断标准：如果你的代码中出现 “大量 if-else 处理不同规则”“需要频繁新增 / 修改业务规则”，就是引入策略模式的最佳时机。
