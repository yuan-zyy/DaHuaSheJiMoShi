## 第3章 拍摄UFO - 单一职责原则

### 3.1 单一职责原则
***单一职责原则***（Single Responsibility Principle SRP）: 就一个类而言，应该仅有一个引起它变化的原因

#### 一、单一职责原则的核心定义
单一职责原则的核心思想: 一个类（或方法、模块）应该只有一个引起它变化的原因。换句话说，一个类只负责完成一个核心功能，而不是把多个不相关的功能都堆砌在同一个类里

可以用一个通俗的比喻理解: 就像一个公司里，会计只负责算账，销售只负责卖货，如果让会计既算账又去跑去销售，一旦公司的财务规则或销售政策变动，这个会计的工作都需要修改，维护成本会大大增加

#### 二、反面示例（违反单一职责原则）
先看一个典型的反例：一个UserService类既负责用户信息的 CRUD，又负责用户登录验证，还负责发送用户通知。
```java
// 违反单一职责原则的类：承担了用户管理、登录验证、消息通知三个职责
public class UserService {
    // 职责1：用户信息管理
    public void addUser(String username, String password) {
        System.out.println("添加用户：" + username);
    }

    public void deleteUser(String username) {
        System.out.println("删除用户：" + username);
    }

    // 职责2：登录验证
    public boolean login(String username, String password) {
        System.out.println("验证用户 " + username + " 的登录信息");
        return "admin".equals(username) && "123456".equals(password);
    }

    // 职责3：发送通知
    public void sendNotification(String username, String message) {
        System.out.println("给用户 " + username + " 发送通知：" + message);
    }
}
```

***问题分析***：
- 如果登录验证规则变了（比如要加验证码），需要修改UserService；
- 如果通知方式变了（比如从短信改成邮件），也需要修改UserService；
- 如果用户信息存储规则变了，还是要修改这个类。 </br>
  这个类有 3 个不同的变化原因，违反了单一职责原则，维护性极差


#### 三、正面示例（遵循单一职责原则）
我们把上面的类拆分成 3 个单一职责的类，每个类只负责一件事：
```java
// 职责1：仅负责用户信息的CRUD
public class UserManager {
    public void addUser(String username, String password) {
        System.out.println("添加用户：" + username);
    }

    public void deleteUser(String username) {
        System.out.println("删除用户：" + username);
    }
}

// 职责2：仅负责用户登录验证
public class UserAuthenticator {
    public boolean login(String username, String password) {
        System.out.println("验证用户 " + username + " 的登录信息");
        return "admin".equals(username) && "123456".equals(password);
    }
}

// 职责3：仅负责发送用户通知
public class UserNotifier {
    public void sendNotification(String username, String message) {
        System.out.println("给用户 " + username + " 发送通知：" + message);
    }
}

// 测试类
public class SRPTest {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        userManager.addUser("zhangsan", "123456");

        UserAuthenticator authenticator = new UserAuthenticator();
        boolean loginSuccess = authenticator.login("zhangsan", "123456");
        System.out.println("登录结果：" + loginSuccess);

        UserNotifier notifier = new UserNotifier();
        notifier.sendNotification("zhangsan", "欢迎注册！");
    }
}
```


#### 四、单一职责原则的应用场景
1. ***类的设计***：这是最常见的场景，避免 “万能类”“上帝类”，比如不要把订单管理、支付、物流都写在OrderService里，拆分成OrderManager、PaymentService、LogisticsService。
2. ***方法的设计***：一个方法也应遵循单一职责，比如不要写一个handleOrder()方法既创建订单、又扣库存、又发物流，拆分成createOrder()、deductStock()、sendLogistics()。
3. ***模块的设计***：整个项目的模块划分也适用，比如用户模块、订单模块、商品模块应各自独立，不要混在一起。

#### 五、遵循单一职责原则的好处
1. ***提高代码的可读性***：每个类 / 方法的功能清晰，一眼就能看懂；
2. ***提高代码的可维护性***：修改一个功能时，只需要改动对应的类，不会影响其他不相关的功能，降低 bug 风险；
3. ***提高代码的复用性***：单一职责的类更容易被复用，比如UserAuthenticator可以在登录、找回密码等场景复用。


#### 总结
1. 单一职责原则的核心是***一个类 / 方法 / 模块只有一个变化的原因***，只负责一件核心事；
2. 违反该原则会导致类臃肿、维护成本高、修改一处影响多处；
3. 遵循该原则的关键是 “合理拆分”，但也不要过度拆分（比如把一个简单的登录功能拆成 10 个类），需平衡粒度。

需要注意的是，“职责” 的划分没有绝对标准，更多是基于业务场景和变化频率的判断 —— 只要两个功能的变化原因不同，就应该考虑拆分。



### 3.2 


#### 一、

#### 二、Java 代码实现案例
