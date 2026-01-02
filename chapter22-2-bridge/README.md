## 第22章 合成/聚合复用原则 & 桥接模式

### 22.2 合成/聚合复用原则
**桥接模式（Bridge Pattern）**

#### 一、核心定义
**桥接模式（Bridge Pattern）**是一种**结构型设计模式是**，它的核心思想是: **将抽象部分与它的实现部分分立，使它们都可以独立的变化。**，这里的 “抽象” 和 “实现” 并非传统意义上的抽象类和具体实现类，而是两个独立的维度（层级），通过 “桥接” (关联关系) 替代继承关系，避免继承带来的类爆炸问题

#### 二、设计出众（解决的问题）
在不使用桥接模式的情形中，如果存在两个独立变化的维度（例如: "形状”和“颜色”、“手机”和“品牌”），适用继承会导致类数量成指数级增长（类爆炸）

举例：如果有 “圆形、方形”2 种形状，“红色、蓝色、绿色”3 种颜色，通过继承实现 “红色圆形、蓝色圆形、红色方形...”，需要 2*3=6 个类；若后续增加 “三角形” 和 “黄色”，则需要新增 1*3 + 2*1=5 个类，扩展性极差

桥接模式通过将两个维度解耦，让每个维度独立扩展，大幅减少类的数量，提高系统灵活性

#### 三、核心角色
桥接模式包含 4 个角色，它们共同构成两个独立的维度（抽象维度 + 实现维度）
##### 1. 抽象化(Abstraction)角色:
- 定义抽象类的接口，持有一个对 “实现化角色” 的引用（核心桥接点，通过组合而非继承关系实现维度）
- 该角色负责定义抽象层的业务逻辑，具体实现委托给持有的 “实现化角色”

##### 2. 扩展抽象化(Refined Abstraction)角色:
- 是抽象化角色的扩展，对抽象化角色的接口进行扩展和细化
- 实现抽象化角色中定义的抽象方法，并添加自身特有功能，仍依赖实现化角色完成具体业务

##### 3. 实现化(Implementor)角色:
- 定义实现化维度的统一接口（与抽象维度的接口相互独立），提供基本的业务能力实现
- 它不依赖抽象化角色，仅关注自身维度的功能定义，是抽象维度委托的目标

##### 4. 抽象化实现(Concrete Implementor)角色:
- 是实现化角色的子类，完全实现实现化接口中的方法，提供具体的业务实现逻辑
- 多个具体实现化角色对应实现维度的不同变体，可独立扩展

#### 四、Java 代码实现示例
##### 1. 代码示例1
我们以 “图形（抽象维度）” 和 “颜色（实现维度）” 为例，演示桥接模式的实现（图形和颜色是两个独立变化的维度，避免创建 “红色圆形、蓝色圆形、红色方形、蓝色方形” 等大量子类）
###### 步骤1 定义实现化角色(Implementor) - 颜色接口（实现维度）
```java
// 实现化角色: 颜色接口（统一实现维度的接口）
public interface Color {
    // 提供颜色填充的具体实现功能
    void fillColor();
}
```

###### 步骤2: 定义具体实现化角色(Concrete Implementor) - 具体颜色
```java
// 具体实现化角色1：红色
public class Red implements Color {
    @Override
    public void fillColor() {
        System.out.println("填充红色");
    }
}

// 具体实现化角色2：蓝色
public class Blue implements Color {
    @Override
    public void fillColor() {
        System.out.println("填充蓝色");
    }
}
```

###### 步骤3: 定义抽象化角色(Abstraction) - 图形抽象类(抽象维度)
```java
// 抽象化角色: 图形抽象类（持有实现化角色的引用，构成桥接）
public abstract class Shape {
    // 核心: 通过组合关联实现化角色(桥接点)
    protected Color color;
    
    // 构造方法注入
    public Shape(Color color) {
        this.color = color;
    }
    
    // 抽象方法: 绘制图形(抽象维度的业务逻辑)
    public abstract void draw();
}
```
###### 步骤4: 定义扩展抽象化角色(Refined Abstraction) - 具体图形
```java
// 扩展抽象化角色1：圆形
public class Circle extends Shape {
    // 继承父类构造方法，注入颜色
    public Circle(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        System.out.print("绘制圆形，");
        // 委托给实现化角色（颜色）完成具体实现
        color.fillColor();
    }
}

// 扩展抽象化角色2：方形
public class Square extends Shape {
    public Square(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        System.out.print("绘制方形，");
        color.fillColor();
    }
}
```

###### 步骤5: 客户端测试代码
```java
public class BridgePatternTest {
    public static void main(String[] args) {
        // 1. 创建实现维度对象（颜色）
        Color red = new Red();
        Color blue = new Blue();

        // 2. 创建抽象维度对象（图形），注入颜色（桥接关联）
        Shape redCircle = new Circle(red);
        Shape blueSquare = new Square(blue);
        Shape redSquare = new Square(red);

        // 3. 调用业务方法
        redCircle.draw();    // 输出：绘制圆形，填充红色
        blueSquare.draw();   // 输出：绘制方形，填充蓝色
        redSquare.draw();    // 输出：绘制方形，填充红色
    }
}
```

###### 关键特点与优势
1. **解耦抽象与实现**：通过组合（桥接）替代继承，将抽象维度和实现维度完全分离，两者可独立扩展，互不影响。
2. **避免类爆炸**：若采用继承，图形（圆形、方形）和颜色（红、蓝）的组合会产生 2*2=4 个子类，若新增绿色、三角形，会产生 3*3=9 个子类；而桥接模式仅需新增 1 个颜色类和 1 个图形类，扩展性极强。
3. **提高可扩展性**：新增抽象维度（如三角形）或实现维度（如绿色），无需修改原有代码，符合 “开闭原则”。
4. **动态切换实现**：客户端可在运行时动态替换抽象对象对应的实现对象（如将圆形的颜色从红色切换为蓝色）

##### 2. 代码示例2
以 “消息发送” 场景为例（抽象维度：消息类型（普通消息、紧急消息）；实现维度：发送方式（短信、邮件）），避免类爆炸问题

###### 步骤1: 定义实现化角色(Implementor) - 消息发送方式接口
```java
// 实现化角色: 消息发送方式统一接口（实现维度）
public interface MessageSender {
    // 发送消息的基础方法
    void send(String content);
}
```
###### 步骤2: 定义具体实现化角色(Concrete Implementor) - 具体消息发送方式
```java
// 具体实现化角色1：短信发送
public class SmsMessageSender implements MessageSender {
    @Override
    public void send(String content) {
        System.out.println("发送短信：" + content);
    }
}

// 具体实现化角色2：邮件发送
public class EmailMessageSender implements MessageSender {
    @Override
    public void send(String content) {
        System.out.println("发送邮件：" + content);
    }
}
```
###### 步骤3: 定义抽象化角色(Abstraction) - 消息抽象类(抽象维度)
```java
// 抽象化角色: 消息抽象类（抽象维度，持有实现化角色引用）
public abstract class Message {
    // 核心桥接点：组合实现化角色
    protected MessageSender messageSender;
    
    // 构造方法注入发送方式(建立桥接关联)
    public Message(MessageSender messageSender) {
        this.messageSender = messageSender;
    }
    
    // 抽象方法：发送消息（抽象维度业务逻辑）
    public abstract void sendMessage(String content);
}
```
###### 步骤4: 定义扩展抽象化角色(Refined Abstraction) - 具体消息类型
```java
// 扩展抽象化角色1：普通消息
public class NormalMessage extends Message {
    public NormalMessage(MessageSender messageSender) {
        super(messageSender);
    }
    
    @Override
    public void sendMessage(String content) {
        // 细化业务逻辑: 普通消息直接发送
        System.out.print("普通消息：");
        messageSender.send(content); // 委托给实现化角色完成具体发送
    }
}

// 扩展抽象化角色2：紧急消息
public class EmergencyMessage extends Message {
    public EmergencyMessage(MessageSender messageSender) {
        super(messageSender);
    }
    
    @Override
    public void sendMessage(String content) {
        // 细化业务逻辑: 紧急消息添加提醒标识
        String contentWithEmergency = "【紧急通知】：" + content;
        System.out.print("紧急消息：");
        messageSender.send(contentWithEmergency); // 委托给实现化角色完成具体发送
    }
}
```
###### 步骤5: 测试代码
```java
// 桥接模式客户端测试
public class BridgeClient {
    public static void main(String[] args) {
        // 1. 创建实现维度对象（发送方式）
        MessageSender smsSender = new SmsSender();
        MessageSender emailSender = new EmailSender();

        // 2. 创建抽象维度对象（消息类型），注入发送方式（桥接）
        Message normalSms = new NormalMessage(smsSender);
        Message urgentEmail = new UrgentMessage(emailSender);
        Message normalEmail = new NormalMessage(emailSender);

        // 3. 调用业务方法
        normalSms.sendMessage("您的订单已发货");
        urgentEmail.sendMessage("系统即将重启，请保存数据");
        normalEmail.sendMessage("您的会员已到期，请及时续费");
    }
}
```

###### 步骤6: 测试结果
```text
【普通消息】通过短信发送：您的订单已发货
【紧急消息】通过邮件发送：[紧急通知] 系统即将重启，请保存数据
【普通消息】通过邮件发送：您的会员已到期，请及时续费
```

###### 核心优势（对比继承方案）
1. **彻底解耦**：抽象维度（消息类型）和实现维度（发送方式）完全独立，修改其中一个维度不会影响另一个维度的代码。
2. **避免类爆炸**：
   - **继承方案**：2 种消息类型 × 2 种发送方式 = 4 个子类；若新增 “微信发送” 和 “预警消息”，则需要 3×3=9 个子类。
   - **桥接模式**：新增 1 种发送方式（仅需新增 1 个实现类），新增 1 种消息类型（仅需新增 1 个扩展抽象类），无组合冗余。
3. **动态切换实现**：运行时可灵活替换抽象对象对应的实现对象，例如
    ```java
    // 将普通消息的发送方式从短信切换为邮件
    Message normalMsg = new NormalMessage(smsSender);
    normalMsg.sendMessage("测试1");
    normalMsg = new NormalMessage(emailSender);
    normalMsg.sendMessage("测试2"); 
    ```
4. **符合开闭原则**：新增维度变体无需修改原有代码，仅需新增对应类即可完成扩展


#### 五、适用场景
1. 当一个类存在**两个或多个独立变化的维度**，且这些维度都需要独立扩展时（如示例中的 “图形” 和 “颜色”，再如 “手机” 和 “操作系统”、“日志框架” 和 “输出介质”）。
   - 实际框架 / 开发中的应用：JDBC 驱动（抽象：Connection/Statement；实现：不同数据库驱动）、GUI 组件（组件 + 皮肤）、日志框架（日志等级 + 输出方式）
2. 当使用多层继承会导致类数量急剧增加（类爆炸），难以维护时。
3. 当希望抽象部分和实现部分可以独立地进行版本升级，互不干扰时。
4. 当需要在运行时动态切换一个对象的实现方式时
   - 当希望动态切换实现逻辑，且抽象部分和实现部分需要独立复用时

#### 六、总结
1. 桥接模式是结构型模式，核心是抽象与实现分离，通过组合（桥接）实现解耦。
2. 四大核心角色：抽象化（Abstraction）、扩展抽象化（Refined Abstraction）、实现化（Implementor）、具体实现化（Concrete Implementor）。
3. 核心优势：避免类爆炸、提高扩展性、符合开闭原则，适用于多维度独立变化的场景。
4. 关键实现：抽象化角色持有实现化角色的引用，将具体业务委托给实现化角色完成



### n.2 

#### 一、

#### 二、

#### 总结
