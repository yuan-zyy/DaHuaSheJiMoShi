## 第5-1章 依赖倒转原则

### 5-1.1 依赖倒转原则
***依赖倒转原则***（Dependence Inversion Principle，DIP）是 SOLID 五大设计原则之一，由 Robert C. Martin 提出

#### 一、依赖倒转原则 (DIP) 核心定义
核心定义包含两层：
1. 抽象不应该依赖于细节，细节应该依赖抽象
2. 高层模块不应该依赖底层模块，二者都应该依赖抽象

简单来说: ***要面向接口/抽象编程，不要面向具体实现编程***，通过抽象（接口/抽象类）隔离底层的变化，降低模块的耦合

#### 二、为什么需要依赖倒换？
***反例: 未遵循 DIP 的代码*** </br>
假设开发一个消息通知功能，最初只支持短信通知:
```java
// 低层模块: 短信实现
public class SmsNotification {
    public void send(String message) {
        System.out.println("发送短信：" + message);
    }
}

// 高层模块：通知服务（直接依赖低层实现）
public class NotificationService {
    private SmsNotification smsNotification = new SmsNotification();
    
    public void notifyUser(String message) {
        smsNotification.send(message);
    }
}

// 调用层
public class Client {
    public static void main(String[] args) {
        NotificationService service = new NotificationService();
        service.notifyUser("您的订单已发货");
    }
}
```
***问题***：如果需要新增 邮件通知、微信通知，必须修改 NotificationService 的代码（违反开闭原则），高层模块和底层模块紧耦合


#### 三、通过 DIP 的改造实现
***步骤1: 定义抽象（接口）***

抽象处通知的核心行为，不依赖具体实现
```java
// 抽象层：通知接口（高层/低层都依赖此抽象）
public interface Notification {
    void send(String message);
}
```

***步骤 2：低层模块实现抽象***

短信、邮件等实现都依赖抽象接口：

```java
// 低层模块：短信实现（细节依赖抽象）
public class SmsNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("发送短信：" + message);
    }
}

// 新增：邮件实现（无需修改高层代码）
public class EmailNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("发送邮件：" + message);
    }
}
```

***步骤 3：高层模块依赖抽象***

通过***依赖注入***（构造器 / Setter / 接口注入）解耦具体实现：
```java
// 高层模块：通知服务（依赖抽象，而非具体实现）
public class NotificationService {
    // 依赖抽象接口
    private Notification notification;
    
    // 构造器注入（推荐）：由外部决定使用哪种实现
    public NotificationService(Notification notification) {
        this.notification = notification;
    }
    
    public void notifyUser(String message) {
        notification.send(message); // 面向抽象编程
    }
}
```

***步骤 4：调用端使用***
```java
public class Client {
    public static void main(String[] args) {
        // 切换实现只需修改此处，高层模块无需改动
        Notification sms = new SmsNotification();
        NotificationService service = new NotificationService(sms);
        service.notifyUser("您的订单已发货");
        
        // 新增邮件通知：完全无侵入
        Notification email = new EmailNotification();
        NotificationService emailService = new NotificationService(email);
        emailService.notifyUser("您的账号已登录");
    }
}
```

#### 四、依赖注入 （DI）: DIP 的落地手段
依赖注入原则的核心是***依赖抽象***，而***依赖注入***是实现 DIP 的关键手段，常见方式：
1. ***构造器注入***（推荐）：如上述示例，强制依赖初始化，适合必须的依赖
2. ***Setter注入***：灵活修改依赖，适合可选依赖
    ```java
    public void setNotification(Notification notification) {
        this.notification = notification;
    }
    ```
3. ***接口注入***：通过接口约定注入方式（较少用）

#### 五、DIP 的核心价值
1. ***降低耦合***：高层模块与低层模块通过抽象隔离，修改低层实现不影响高层；
2. ***提高扩展性***：新增实现只需实现抽象接口，无需修改现有代码（符合开闭原则）；
3. ***提高可测试性***：可通过 mock 抽象接口测试高层模块，无需依赖真实低层实现

#### 六、常见误区
1. 滥用抽象：为简单场景过度设计接口（如仅一个实现的接口）；
2. 依赖抽象但仍硬编码实现：如Notification notification = new SmsNotification()，未真正解耦；
3. 抽象依赖细节：接口中包含具体实现的专属方法（违反 “抽象不依赖细节”）

#### 总结
依赖倒转原则的核心是「面向抽象编程」，通过抽象让高层和低层模块都依赖于稳定的抽象层，而非不稳定的具体实现。在 Java 中，通常通过***接口 / 抽象类***定义抽象层，通过***依赖注入***解耦具体实现，最终实现代码的高内聚、低耦合和高扩展性

### 5-1.2 

#### 一、

#### 二、

#### 总结
