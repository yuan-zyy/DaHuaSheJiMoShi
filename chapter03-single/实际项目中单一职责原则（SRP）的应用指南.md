# 实际项目中单一职责原则（SRP）的应用指南

本文结合真实Java项目场景，提供单一职责原则（SRP）可落地的判断标准、实施步骤、实操案例及避坑注意事项，帮助将理论转化为实际代码实践。

## 一、先掌握：判断是否违反SRP的3个实操标准

在动手拆分前，先判断现有代码是否违反SRP，这是应用的第一步：

1. **变化频率判断**：如果修改一个需求，需要改动同一个类里的多个不相关方法（比如改登录规则，却要动用户信息存储的代码），说明违反SRP；

2. **功能关联性判断**：看类里的方法是否围绕“同一个核心目标”，比如`OrderService`里既有`createOrder()`（创建订单），又有`sendSms()`（发短信），这两个功能无核心关联，违反SRP；

3. **代码注释判断**：如果给一个方法写注释时，需要用“并且”“同时”“还负责”这类词，说明方法承担了多个职责（比如注释写“该方法创建订单并且扣减库存”）。

## 二、实际项目中应用SRP的4个步骤（附案例）

以电商项目中最常见的`OrderService`为例，一步步落地SRP：

### 步骤1：识别“臃肿类”的多职责

先看一个真实项目中常见的“万能订单类”（违反SRP）：

```java

// 电商项目中常见的“臃肿订单类”——承担了4个不相关职责
public class OrderService {
    // 职责1：订单基础CRUD
    public void createOrder(Long userId, Long productId, Integer count) {
        System.out.println("创建订单：用户" + userId + "购买商品" + productId + "共" + count + "件");
    }

    // 职责2：库存扣减
    public void deductStock(Long productId, Integer count) {
        System.out.println("扣减商品" + productId + "库存：" + count + "件");
    }

    // 职责3：支付处理
    public boolean payOrder(Long orderId, BigDecimal amount) {
        System.out.println("订单" + orderId + "支付金额：" + amount);
        return true; // 模拟支付成功
    }

    // 职责4：订单消息通知
    public void sendOrderMsg(Long orderId, Long userId) {
        System.out.println("给用户" + userId + "发送订单" + orderId + "的支付成功消息");
    }
}
```

### 步骤2：按“职责边界”拆分类

核心原则：**把“变化原因不同”的职责拆分成独立类**。

比如上面的`OrderService`中：

- 订单CRUD：变化原因是“订单规则调整”（比如订单字段增加）；

- 库存扣减：变化原因是“库存规则调整”（比如预售库存逻辑）；

- 支付处理：变化原因是“支付方式调整”（比如新增微信支付）；

- 消息通知：变化原因是“通知方式调整”（比如从短信改成推送）。

拆分后的代码（符合SRP）：

```java

// 职责1：仅负责订单基础管理（核心订单CRUD）
public class OrderManager {
    public void createOrder(Long userId, Long productId, Integer count) {
        System.out.println("创建订单：用户" + userId + "购买商品" + productId + "共" + count + "件");
    }

    // 可扩展：cancelOrder、updateOrder等仅和订单本身相关的方法
}

// 职责2：仅负责库存管理
public class StockService {
    public void deductStock(Long productId, Integer count) {
        System.out.println("扣减商品" + productId + "库存：" + count + "件");
    }

    // 可扩展：checkStock（校验库存）、restoreStock（恢复库存）等
}

// 职责3：仅负责支付处理
public class PaymentService {
    public boolean payOrder(Long orderId, BigDecimal amount) {
        System.out.println("订单" + orderId + "支付金额：" + amount);
        return true;
    }

    // 可扩展：refund（退款）、supportPayType（支持的支付方式）等
}

// 职责4：仅负责订单消息通知
public class OrderNotifier {
    public void sendOrderMsg(Long orderId, Long userId) {
        System.out.println("给用户" + userId + "发送订单" + orderId + "的支付成功消息");
    }

    // 可扩展：sendCancelMsg（取消订单消息）、sendDeliveryMsg（发货消息）等
}}
```

### 步骤3：通过“上层协调类”整合职责（避免代码分散）

拆分后，业务逻辑可能需要多个类协同工作（比如创建订单需要先扣库存，支付后要发通知），这时可以创建一个“业务协调类”（也叫“门面类/Facade”），它只负责协调，不承担具体业务逻辑，依然符合SRP：

```java

// 订单业务协调类：仅负责协调各单一职责类完成完整业务流程，无具体业务逻辑
public class OrderBusinessService {
    // 依赖注入各单一职责类（实际项目中用Spring的@Autowired）
    private OrderManager orderManager = new OrderManager();
    private StockService stockService = new StockService();
    private PaymentService paymentService = new PaymentService();
    private OrderNotifier orderNotifier = new OrderNotifier();

    // 完整的“创建订单并支付”业务流程
    public void createAndPayOrder(Long userId, Long productId, Integer count, BigDecimal amount) {
        // 1. 扣减库存
        stockService.deductStock(productId, count);
        // 2. 创建订单
        orderManager.createOrder(userId, productId, count);
        // 3. 支付订单
        boolean paySuccess = paymentService.payOrder(1L, amount); // 模拟订单ID=1
        // 4. 发送通知
        if (paySuccess) {
            orderNotifier.sendOrderMsg(1L, userId);
        }
    }
}

// 测试类（模拟项目调用）
public class SRPProjectTest {
    public static void main(String[] args) {
        OrderBusinessService orderBusiness = new OrderBusinessService();
        // 调用完整业务流程
        orderBusiness.createAndPayOrder(1001L, 2001L, 2, new BigDecimal("99.8"));
    }
}
```

**输出结果**：

```Plain Text

扣减商品2001库存：2件
创建订单：用户1001购买商品2001共2件
订单1支付金额：99.8
给用户1001发送订单1的支付成功消息
```

### 步骤4：方法级别的SRP落地（更细粒度）

不仅类要遵循SRP，方法也需要——一个方法只做一件事。

比如下面的反例（一个方法做3件事）：

```java

// 违反方法级SRP：一个方法既查用户、又查订单、又统计金额
public BigDecimal getUserOrderTotal(Long userId) {
    // 1. 查询用户是否存在
    User user = userDao.getById(userId);
    if (user == null) throw new RuntimeException("用户不存在");
    
    // 2. 查询用户所有订单
    List<Order> orders = orderDao.listByUserId(userId);
    
    // 3. 统计订单总金额
    BigDecimal total = BigDecimal.ZERO;
    for (Order order : orders) {
        total = total.add(order.getAmount());
    }
    return total;
}
```

拆分后（符合方法级SRP）：

```java

// 职责1：校验用户
public void checkUserExist(Long userId) {
    User user = userDao.getById(userId);
    if (user == null) throw new RuntimeException("用户不存在");
}

// 职责2：查询用户订单
public List<Order> listUserOrders(Long userId) {
    return orderDao.listByUserId(userId);
}

// 职责3：统计订单金额
public BigDecimal calculateOrderTotal(List<Order> orders) {
    BigDecimal total = BigDecimal.ZERO;
    for (Order order : orders) {
        total = total.add(order.getAmount());
    }
    return total;
}

// 协调方法（仅整合，无具体逻辑）
public BigDecimal getUserOrderTotal(Long userId) {
    checkUserExist(userId);
    List<Order> orders = listUserOrders(userId);
    return calculateOrderTotal(orders);
}
```

## 三、项目中应用SRP的注意事项（避坑）

1. **不要过度拆分**：SRP不是“拆得越细越好”。比如把“创建订单”拆成“创建订单头”“创建订单项”两个类，反而增加复杂度——拆分的粒度以“变化原因”为边界，而非“功能步骤”。

2. **结合项目规模调整**：小型项目/简单功能可以适度“合并职责”（比如个人练手项目的`UserService`可以兼顾登录和CRUD），但中大型项目必须严格拆分（否则维护成本爆炸）。

3. **和其他SOLID原则配合**：SRP常和“依赖倒置原则”“接口隔离原则”配合使用——拆分后的类通过接口交互，降低耦合。

4. **利用框架辅助**：Spring项目中，通过`@Service`/`@Component`将拆分后的类交给容器管理，通过依赖注入（`@Autowired`）整合，既符合SRP，又保证代码整洁。

## 总结

1. 实际项目中应用SRP的核心是**按“变化原因”拆分职责**：先识别类/方法的多职责，再拆分成独立的单一职责类，最后通过协调类整合业务流程；

2. 判断是否违反SRP的关键：看是否有“多个变化原因”，而非“功能数量多少”；

3. 落地时要平衡“拆分粒度”，避免过度拆分，同时结合项目规模和框架特性（如Spring依赖注入）让拆分后的代码更易维护。
> （注：文档部分内容可能由 AI 生成）