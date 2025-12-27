## 第7章 代理模式

### 7.1 代理模式

#### 一、核心概述
##### 1. 核心定义
***代理模式(Proxy Pattern)***是一种***结构性设计模式***，它的核心思想是：***为其他对象提供一个代理（中介），以控制对这个对象的访问***。代理对象作为客户端和目标对象之间的中间层，既能转发客户端的请求到目标对象，也能在转发前后添加额外的逻辑（如日志记录、权限校验、性能监控等），而客户端无需感知目标对象的存在，只需与代理对象交互
##### 2. 核心角色
代理模式包含3个关键角色，缺一不可：

| 角色名称               | 核心职责                                            |
|--------------------|-------------------------------------------------|
| 抽象主体(Subject)      | 定义目标对象和代理对象的共同接口/抽象类，规范两者的核心业务方法，保证客户端对两者的使用一致性 |
| 真实主题(Real Subject) | 即目标对象，是业务逻辑的实际执行者，代理对象最总会委托完成核心业务操作             |
| 代理主体(Proxy)        | 即代理对象，持有真实主题的引用，实现抽象主体接口，在调用真实主题方法的前后可以添加额外增强逻辑 |

#### 二、代理模式的主要分类
Java 中的代理模式主要分为两大类，其中动态代理又包含两种常见示例
1. ***静态代理（Static Proxy）***：代理类在***编译期就已确定并生成***（由开发者手动编写或工具生成），一对一绑定目标对象，灵活性较低。

2. ***动态代理（Dynamic Proxy）***：代理类在***程序运行时动态生成***（无需手动编写代理类代码），可动态绑定多个目标对象，灵活性更高，分为：
   - JDK 动态代理：基于 Java 反射机制实现，***要求目标对象必须实现一个或多个接口***。
   - CGLIB 动态代理：基于字节码增强技术实现，***无需目标对象实现接口***，通过继承目标类生成代理子类。

#### 三、代码实现示例
##### 1. 静态代理实现
***步骤1: 定义抽象主体(共同接口)***
```java
// 抽象主体: 订单服务接口（规范核心业务方法）
public interface OrderService {
    // 核心业务：创建订单
    void createOrder(String orderNo);
}
```
***步骤2: 实现真实主题(目标对象)***
```java
// 真实主题：订单服务的实际实现类（业务逻辑执行者）
public class OrderServiceImpl implements OrderService {
    @Override
    public void createOrder(String orderNo) {
        System.out.println("真实业务：创建订单，订单号：" + orderNo);
        // 模拟订单的核心逻辑
    }
}
```
***步骤3: 编写静态代理类***
```java
// 代理主题：订单服务的静态代理类
public class OrderServiceStaticProxy implements OrderService {
    // 持有真实主题的引用（核心：委托目标对象执行业务）
    private final OrderService realOrderService;

    // 构造方法注入真实主题对象
    public OrderServiceStaticProxy(OrderService realOrderService) {
        this.realOrderService = realOrderService;
    }

    @Override
    public void createOrder(String orderNo) {
        // 前置增强逻辑：调用目标方法前执行
        beforeCreateOrder(orderNo);

        // 委托真实主题执行核心业务方法
        realOrderService.createOrder(orderNo);

        // 后置增强逻辑：调用目标方法后执行
        afterCreateOrder(orderNo);
    }

    // 前置增强：日志记录 + 权限校验
    private void beforeCreateOrder(String orderNo) {
        System.out.println("静态代理前置：记录订单创建请求日志，订单号：" + orderNo);
        System.out.println("静态代理前置：校验用户权限，权限通过");
    }

    // 后置增强：订单创建完成后的通知
    private void afterCreateOrder(String orderNo) {
        System.out.println("静态代理后置：订单创建成功，发送通知给用户，订单号：" + orderNo);
    }
}
```
***步骤4: 客户端测试***
```java
public class StaticProxyTest {
    public static void main(String[] args) {
        // 1. 创建真实主题（目标对象）
        OrderService realOrderService = new OrderServiceImpl();

        // 2. 创建静态代理对象，注入目标对象
        OrderService proxy = new OrderServiceStaticProxy(realOrderService);

        // 3. 客户端仅与代理对象交互，无需感知目标对象
        proxy.createOrder("ORDER_20251227_001");
    }
}
```
运行结果
```text
静态代理前置：记录订单创建请求日志，订单号：ORDER_20251227_001
静态代理前置：校验用户权限，权限通过
真实业务：创建订单，订单号：ORDER_20251227_001
静态代理后置：订单创建成功，发送通知给用户，订单号：ORDER_20251227_001
```

##### 2. 动态代理实现 (JDK动态代理)
***步骤1: 复用抽象主体和真实主题（同静态代理的 OrderService、OrderServiceImpl）***

***步骤2: 编写 InvocationHandler 实现类（增强逻辑处理器）

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

// JDk 动态代理的核心: 增强逻辑处理器（处理代理对象的方法调用）
public class OrderServiceInvocationHandler implements InvocationHandler {
   // 持有真实主题的引用（可适配任意接口的目标对象，灵活性更高）
   private final Object realObject;

   // 构造方法注入目标对象
   public OrderServiceInvocationHandler(Object realObject) {
      this.realObject = realObject;
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // 前置增强逻辑
      beforeInvoke(method, args);
      
      // 反射目标调用目标对象的核心方法
      Object result = method.invoke(realObject, args);

      // 后置增强逻辑
      afterInvoke(method, args);
      
      return result;
   }

   // 前置增强逻辑
   private void beforeInvoke(Method method, Object[] args) {
      String methodName = method.getName();
      String param = args != null && args.length > 0 ? args[0].toString() : "无参数";
      System.out.println("JDK动态代理前置：调用方法[" + methodName + "]，参数：" + param);
      System.out.println("JDK动态代理前置：执行权限校验和日志记录");
   }

   // 后置增强逻辑
   private void afterInvoke(Method method, Object[] args) {
      String methodName = method.getName();
      String param = args != null && args.length > 0 ? args[0].toString() : "无参数";
      System.out.println("JDK动态代理后置：方法[" + methodName + "]执行完成，参数：" + param);
      System.out.println("JDK动态代理后置：发送业务通知");
   }
}
```

##### 3. 客户端测试（动态生成代理对象）

```java
import com.zyy.design.pattern.dhsjms.chapter07.demojdk.user.UserServiceInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JdkDynamicProxyTest {
   public static void main(String[] args) {
      // 1. 创建真实主题（目标对象）
      OrderService orderService = new OrderServiceImpl();
      // 2. 创建 InvocationHandler 实例，注入目标对象
      InvocationHandler handler = new UserServiceInvocationHandler(orderService);
      // 3. 动态生成代理对象（核心：Proxy.newProxyInstance()）
      orderService proxy = (OrderService) Proxy.newProxyInstance(
              orderService.getClass().getClassLoader(), // 目标对象的类加载器
              orderService.getClass().getInterfaces(),  // 目标对象实现的接口（JDK 动态代理的关键要求）
              handler); // 增强逻辑处理器
      // 4. 客户端调用代理对象方法
      proxy.createOrder("ORDER_20251227_002");
   }
}
```
##### 运行结果
```text
JDK动态代理前置：调用方法[createOrder]，参数：ORDER_20251227_002
JDK动态代理前置：执行权限校验和日志记录
真实业务：创建订单，订单号：ORDER_20251227_002
JDK动态代理后置：方法[createOrder]执行完成，参数：ORDER_20251227_002
JDK动态代理后置：发送业务通知
```

#### 四、代理模式的核心应用场景
代理模式在实际开发中应用广泛，核心场景包括：
1. 日志记录: 在方法调用前后自动记录请求参数、返回值、执行时间等日志，无需侵入业务代码
2. 权限校验: 在调用核心业务方法前，校验用户是否具备对应的操作权限，拒绝非法访问
3. 性能监控: 统计方法的执行耗时、调用次数，用于系统性能调优（如接口响应时间监控）
4. 远程代理: 为远程服务器上的对象提供本地代理，客户端通过代理对象间接调用远程对象（如 RPC 框架的底层实现）
5. 懒加载(虚拟代理): 对于创建成本较高的对象，先通过代理对象占位，只有当真正需要使用时才创建目标对象（如大型图片的延迟加载）
6. 缓存代理: 将方法调用的结果缓存起来，当后续相同参数的请求到来时，直接返回缓存结果，提高系统响应速度

#### 五、静态代理 vs 动态代理 核心区别

|对比维度|静态代理|动态代理|
|---|---|---|
|代理类生成时机|编译期（手动编写/工具生成）|运行期（反射/字节码技术动态生成）|
|灵活性|低，一个代理类仅能代理一个目标对象（或一组同接口的固定对象）|高，一个处理器可代理任意目标对象（实现接口的对象/JDK代理；任意类/CGLIB代理）|
|代码冗余度|高，每增加一个目标对象，需手动编写对应的代理类|低，无需编写代理类，仅需实现增强逻辑处理器|
|适用场景|目标对象固定、增强逻辑简单且不常变更的场景|目标对象不固定、增强逻辑通用、需要批量代理的场景（如框架开发）|
|性能|略高（编译期确定，无反射开销）|略低（运行期反射/字节码生成有一定开销，可通过缓存优化）|
### 总结

1. 代理模式的核心是**通过代理对象控制对目标对象的访问，同时添加额外增强逻辑**，核心角色为抽象主题、真实主题、代理主题。

2. 按代理类生成时机分为静态代理（编译期）和动态代理（运行期），动态代理又分为JDK（需接口）和CGLIB（无需接口）两种实现。

3. 核心应用包括日志、权限、性能监控等，动态代理因灵活性更高，在Spring AOP等框架中被广泛使用。

4. 静态代理适用于简单固定场景，动态代理适用于通用化、批量代理的复杂场景。
> （注：文档部分内容可能由 AI 生成）

#### 总结


### n.2 

#### 一、

#### 二、

#### 总结
