## 第25章 中介者模式

### 25.1 中介者模式
你想了解Java中的中介者模式，我会从核心概念、角色划分、代码实现到适用场景为你全面解析。

#### 一、中介者模式核心概述

中介者模式（Mediator Pattern）是一种**行为型设计模式**，它的核心思想是：**定义一个中介对象来封装一系列对象之间的交互，使原有对象之间不再直接相互引用（解除紧耦合），而是通过中介者对象间接通信**。当其中一个对象发生变化时，只需通知中介者，再由中介者统一协调其他相关对象做出响应，简化对象间的交互复杂度。

#### 二、中介者模式的核心角色

中介者模式包含4个核心角色，各自承担明确职责，构成完整的交互体系：

1.  **抽象中介者（Mediator）**：定义统一的接口，用于规范中介者与各个同事对象之间的通信方法（通常包含注册同事对象、转发同事对象消息的抽象方法）。

2.  **具体中介者（ConcreteMediator）**：实现抽象中介者接口，维护所有同事对象的引用，负责具体的协调逻辑——接收某个同事对象的消息后，决定哪些其他同事对象需要做出响应，并完成消息转发。

3.  **抽象同事类（Colleague）**：定义同事对象的公共属性和行为，维护一个对抽象中介者的引用（所有同事对象都知道中介者的存在），提供发送消息和接收消息的抽象/通用方法。

4.  **具体同事类（ConcreteColleague）**：实现抽象同事类，是具体的业务对象。当需要与其他同事对象交互时，不直接调用其他同事，而是通过中介者间接完成；同时接收中介者转发的消息并做出具体业务响应。

#### 三、Java 代码完整实现（示例：聊天室场景）

我们以“多人聊天室”为场景实现中介者模式：聊天室（中介者）负责转发用户（同事对象）发送的消息，用户之间无需直接通信，仅通过聊天室交互。

##### 1. 抽象中介者（Mediator）

```java

/**
 * 抽象中介者：聊天室接口
 * 定义注册用户和转发消息的规范
 */
public interface ChatRoomMediator {
    // 注册用户到聊天室
    void registerUser(User colleague);
    
    // 转发消息（从发送者到其他所有用户）
    void relayMessage(User sender, String message);
}
```

##### 2. 具体中介者（ConcreteMediator）

```java

import java.util.ArrayList;
import java.util.List;

/**
 * 具体中介者：具体聊天室
 * 维护用户列表，实现消息转发的具体逻辑
 */
public class ConcreteChatRoom implements ChatRoomMediator {
    // 存储所有注册到该聊天室的用户（同事对象引用）
    private List<User> userList = new ArrayList<>();

    @Override
    public void registerUser(User user) {
        if (!userList.contains(user)) {
            userList.add(user);
            // 为用户绑定当前聊天室（中介者）
            user.setMediator(this);
            System.out.println(user.getName() + "已加入聊天室");
        }
    }

    @Override
    public void relayMessage(User sender, String message) {
        // 协调逻辑：将发送者的消息转发给其他所有用户
        for (User user : userList) {
            // 排除发送者自身
            if (user != sender) {
                user.receiveMessage(sender.getName(), message);
            }
        }
    }
}
```

##### 3. 抽象同事类（Colleague）

```java

/**
 * 抽象同事类：用户抽象类
 * 维护中介者引用，定义发送和接收消息的通用方法
 */
public abstract class User {
    // 同事对象持有中介者引用
    protected ChatRoomMediator mediator;
    private String name;

    public User(String name) {
        this.name = name;
    }

    // 设置中介者
    public void setMediator(ChatRoomMediator mediator) {
        this.mediator = mediator;
    }

    // 发送消息（委托给中介者）
    public void sendMessage(String message) {
        System.out.println(this.name + "发送消息：" + message);
        mediator.relayMessage(this, message);
    }

    // 接收消息（抽象方法，由具体同事实现）
    public abstract void receiveMessage(String senderName, String message);

    // getter
    public String getName() {
        return name;
    }
}
```

##### 4. 具体同事类（ConcreteColleague）

```java

/**
 * 具体同事类：普通用户
 * 实现接收消息的具体业务逻辑
 */
public class CommonUser extends User {

    public CommonUser(String name) {
        super(name);
    }

    @Override
    public void receiveMessage(String senderName, String message) {
        System.out.println(this.getName() + "收到" + senderName + "的消息：" + message);
    }
}
```

##### 5. 测试类（客户端调用）

```java

/**
 * 客户端测试类
 */
public class MediatorPatternTest {
    public static void main(String[] args) {
        // 1. 创建具体中介者（聊天室）
        ChatRoomMediator chatRoom = new ConcreteChatRoom();

        // 2. 创建具体同事（用户）
        User user1 = new CommonUser("张三");
        User user2 = new CommonUser("李四");
        User user3 = new CommonUser("王五");

        // 3. 用户注册到聊天室
        chatRoom.registerUser(user1);
        chatRoom.registerUser(user2);
        chatRoom.registerUser(user3);

        // 4. 用户发送消息（通过中介者转发）
        System.out.println("------------------------");
        user1.sendMessage("大家好，我是张三！");
        System.out.println("------------------------");
        user2.sendMessage("张三你好，我是李四～");
    }
}
```

##### 6. 运行结果

```text

张三已加入聊天室
李四已加入聊天室
王五已加入聊天室
------------------------
张三发送消息：大家好，我是张三！
李四收到张三的消息：大家好，我是张三！
王五收到张三的消息：大家好，我是张三！
------------------------
李四发送消息：张三你好，我是李四～
张三收到李四的消息：张三你好，我是李四～
王五收到李四的消息：张三你好，我是李四～
```

#### 四、中介者模式的适用场景

当满足以下场景时，优先考虑使用中介者模式：

1. 多个对象之间存在复杂的交互关系，且这些对象之间的依赖关系紧密（形成“网状结构”），需要简化其耦合关系。

2. 希望复用某个对象时，发现该对象与其他多个对象存在直接依赖，难以单独复用。

3. 想集中管理多个对象之间的交互逻辑，便于后续统一维护和扩展（例如：聊天室、调度中心、窗口管理器等场景）。

#### 五、中介者模式的优缺点

##### 优点

1.  **降低对象耦合度**：原有对象之间不再直接引用，解除紧耦合，提高对象的独立性和可复用性。

2.  **简化对象交互**：将多对象之间的复杂交互逻辑集中到中介者中，使对象自身的逻辑更简洁，便于理解和维护。

3.  **便于扩展和维护**：新增或修改对象交互逻辑时，只需修改中介者（或新增中介者），无需修改所有相关对象，符合“开闭原则”。

##### 缺点

1.  **中介者职责过重**：随着对象交互复杂度的增加，中介者的逻辑会变得越来越复杂，可能成为“上帝类”（God Class），难以维护和扩展。

2.  **可能造成性能瓶颈**：所有对象的交互都经过中介者转发，当交互频率极高时，中介者可能成为系统的性能瓶颈。

##### 总结

1. 中介者模式是行为型模式，核心是**用中介对象封装多对象交互，解除对象间直接耦合**。

2. 核心角色：抽象中介者、具体中介者、抽象同事类、具体同事类，四者协同完成间接通信。

3. 优势是简化耦合、便于维护，劣势是中介者易成为“上帝类”，需合理设计中介者的职责边界。

4. 典型场景：聊天室、服务调度中心、UI组件交互（如按钮、输入框、弹窗的协调）等。
