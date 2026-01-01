## 第16章 状态模式 

### 16.1 状态模式
***状态模式(State Pattern)***

#### 一、核心概念
***状态模式(State Pattern)***是一种行为型设计模式，它的核心思想是: ***将对象的每一种状态都封装成一个独立的类，对象的状态发生变化时，会切换对应的状态对象，而非通过大量的 if-else 或 switch 语句来控制行为***。

简单来说：就是将对象的行为随着其状态的改变而改变，状态的逻辑被隔离在各个状态类中，提高代码的可维护性和可扩展性

#### 二、核心角色
状态模式包含 4 个核心角色，职责清晰，相互配合完成状态的切换和行为调度
##### 1. 环境类(Context)
- 核心作用: 维护一个当前状态(State)对象的引用，是客户端与状态模式交互的入口
- 额外职责: 提供状态切换的接口，自身不负责状态逻辑的实现，而是委托给当前关联的状态对象

##### 2. 抽象状态类(State)
- 核心作用: 定义所有具体状态类必须实现的统一接口(或抽象方法)，这些方法对象对应在该状态下的行为
- 可以是接口（推荐，更灵活）或抽象类，规范具体状态的行为规范

##### 3. 具体状态类(Concrete State)
- 核心作用: 实现抽象状态定义的接口，封装对象在该状态下的具体行为
- 可选职责: 根据业务逻辑，负责触发状态切换（修改 Context 的当前状态引用）

##### 4. 客户端(Client)
- 核心作用: 创建环境类和具体状态对象，初始换环境类的初始状态，通过环境类调用业务方法，无需直接操作状态对象

#### 三、Java 状态模式完整示例代码
我们以「订单状态流转」为场景（待支付→已支付→已发货→已完成），实现状态模式：
##### 1. 步骤1: 定义抽象状态类(State)
这里使用接口定义，规范订单在各个状态下的核心行为:
```java
// 订单状态接口
public interface OrderState {
    // 支付操作(对应状态下的行为)
    void pay(OrderContext order);
    
    // 发货操作(对应状态下的行为)
    void deliver(OrderContext order);
    
    // 完成订单操作(对应状态下的行为)
    void finish(OrderContext order);
    
    // 获取订单状态名称
    String getStateName();
}
```

##### 2. 步骤2: 创建具体状态类(Concrete State)
分别实现 4 种订单状态，每个类封装对应状态下的行为和状态切换逻辑

###### (1)待支付状态 (UnPaidState)
```java
// 待支付状态(具体状态类1)
public class UnPaidState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        // 待支付状态下，支付操作有效，执行支付逻辑并切换状态为 [已支付]
        System.out.println("订单支付成功！");
        // 切换到环境类的当前状态
        context.setCurrentState(new PaidState());
    }
    
    @Override
    public void deliver(OrderContext context) {
        // 待支付状态下，发货操作无效，打印提示信息
        System.out.println("错误: 订单未支付，无法发货！");
    }
    
    @Override
    public void finish(OrderContext context) {
        // 待支付状态下，完成订单操作无效，打印提示信息
        System.out.println("错误: 订单未支付，无法完成！");
    }
    
    @Override
    public String getStateName() {
        return "待支付";
    }
}
```

###### (2)已支付状态 (PaidState)
```java
// 已支付状态(具体状态类2)
public class PaidState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        // 已支付状态下，支付操作无效，打印提示信息
        System.out.println("错误: 订单已支付，请勿重复支付！");
    }
    
    @Override
    public void deliver(OrderContext context) {
        // 已支付状态下，发货操作有效，执行发货逻辑并切换状态为 [已发货]
        System.out.println("订单已发货！");
        context.setCurrentState(new DeliveredState());
    }
    
    @Override
    public void finish(OrderContext context) {
        // 已支付状态下，完成订单操作无效，打印提示信息
        System.out.println("错误: 订单未发货，无法完成！");
    }
    
    @Override
    public String getStateName() {
        return "已支付";
    }
}
```

###### (3)已发货状态 (DeliveredState)
```java
// 已发货状态(具体状态类3)
public class DeliveredState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        // 已发货状态下，支付操作无效，打印提示信息
        System.out.println("错误: 订单已发货，请勿重复支付！");
    }
    
    @Override
    public void deliver(OrderContext context) {
        // 已发货状态下，发货操作无效，打印提示信息
        System.out.println("错误: 订单已发货，请勿重复发货！");
    }
    
    @Override
    public void finish(OrderContext context) {
        // 已发货状态下，完成订单操作有效，执行完成逻辑并切换状态为 [已完成]
        System.out.println("订单已完成！");
        context.setCurrentState(new FinishedState());
    }
    
    @Override
    public String getStateName() {
        return "已发货";
    }
}
```

###### (4)已完成状态 (FinishedState)
```java
// 已完成状态(具体状态类4)
public class FinishedState implements OrderState {
    @Override
    public void pay(OrderContext context) {
        // 已完成状态下，支付操作无效，打印提示信息
        System.out.println("错误: 订单已完成，无需支付！");
    }
    
    @Override
    public void deliver(OrderContext context) {
        // 已完成状态下，发货操作无效，打印提示信息
        System.out.println("错误: 订单已完成，无需发货！");
    }
    
    @Override
    public void finish(OrderContext context) {
        // 已完成状态下，完成订单操作无效，打印提示信息
        System.out.println("错误: 订单已完成，请勿重复完成！");
    }
    
    @Override
    public String getStateName() {
        return "已完成";
    }
}
```

##### 3. 步骤3: 创建环境类(Context)
维护当前状态，委托状态对象执行具体行为，并提供状态切换的入口
```java
// 环境类(Context)
public class OrderContext {
    // 维护当前状态的引用
    private OrderState currentState;
    
    // 构造方法: 初始化当前状态为 [待支付]
    public OrderContext() {
        currentState = new UnPaidState();
        System.out.println("订单创建成功: 当前订单状态: " + currentState.getStateName());
    }
    
    // 设置当前状态（状态切换的核心方法）
    public void setCurrentState(OrderState currentState) {
        this.currentState = currentState;
    }
    
    // 订单支付操作
    public void payOrder() {
        currentState.pay(this);
    }
    
    // 订单发货操作
    public void deliverOrder() {
        currentState.deliver(this);
    }
    
    // 订单完成操作
    public void finishOrder() {
        currentState.finish(this);
    }
    
    @Override
    public String getCurrentStateName() {
        return currentState.getStateName();
    }
}
```

#### 4. 步骤4: 客户端测试(Client)
```java
// 客户端测试类
public class StatePatternTest {
    public static void main(String[] args) {
        // 1. 创建订单环境类（初始状态：待支付）
        OrderContext order = new OrderContext();

        // 2. 执行各类操作，观察状态流转
        System.out.println("当前订单状态：" + order.getCurrentStateName());
        order.payOrder(); // 支付订单（待支付→已支付）

        System.out.println("当前订单状态：" + order.getCurrentStateName());
        order.deliverOrder(); // 发货订单（已支付→已发货）

        System.out.println("当前订单状态：" + order.getCurrentStateName());
        order.finishOrder(); // 完成订单（已发货→已完成）

        System.out.println("当前订单状态：" + order.getCurrentStateName());
        // 尝试重复操作
        order.payOrder(); // 已完成状态下支付，提示错误
    }
}
```

##### 5. 运行结果
```text
订单创建成功，初始状态：待支付
当前订单状态：待支付
订单支付成功！
当前订单状态：已支付
订单发货成功！
当前订单状态：已发货
订单完成成功！
当前订单状态：已完成
错误：订单已完成，无需支付！
```

#### 四、状态模式的核心优势
1. ***消除大量分支判断***: 替代 if-else/ switch-case，避免代码臃肿、可读性差、维护成本高的问题（尤其是状态较多时）
2. ***状态逻辑隔离封装***: 每个状态的行为都被封装在独立的具体状态类中，职责单一，便于单独维护、测试和扩展
3. ***状态切换清晰可控***: 状态切换逻辑要么在具体状态类中(业务驱动)，要么在环境类中（统一管理），逻辑清晰，不易出错
4. ***符合开闭原则***: 新增状态是，只需新增一个具体状态类，无需修改现有环境类和其他状态类的代码，可扩展性极强

#### 五、状态模式在Java中的实现
当对象满足以下特征时，优先使用状态模式：
1. 对象的行为依赖于其当前状态（不同状态下，同一行为有不同表现）。
2. 对象存在多种状态，且状态之间存在明确的流转关系。
3. 代码中存在大量与状态相关的分支判断，导致代码可读性和可维护性下降。

常见实际场景：
- 订单状态流转（待支付→已支付→已发货→已完成→已取消）。
- 电梯状态控制（停止→运行→开门→关门）。
- 游戏角色状态（正常→受伤→死亡→无敌）。
- 网络连接状态（断开→连接中→已连接→异常）。

#### 总结
1. 状态模式核心：***状态封装为独立类，对象行为随状态切换而改变，委托状态对象执行具体逻辑***。
2. 核心角色：环境类（Context，维护当前状态）、抽象状态类（State，规范行为）、具体状态类（Concrete State，实现状态行为）。
3. 核心价值：消除分支判断、隔离状态逻辑、提高扩展性，符合开闭原则。
4. 适用场景：对象多状态、行为依赖状态、存在大量状态相关分支判断的场景


### n.2 

#### 一、

#### 二、

#### 总结
