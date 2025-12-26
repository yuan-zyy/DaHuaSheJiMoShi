# LSP 中参数协变的 Java 示例解析

你想理解“子类方法的参数类型可以比父类更宽松（协变）”这个LSP规则，并且需要具体的案例来辅助理解——首先要先澄清一个容易混淆的点：**在Java语法中，方法参数并不支持“协变”（子类参数不能比父类更宽松），但从LSP的语义层面，这个规则是对“继承契约合理性”的要求**。

简单来说，这个规则的核心是：

父类方法承诺“能处理类型A的参数”，子类方法如果要重写，必须至少能处理“类型A及A的父类（更宽松）”，而不能只处理“A的子类（更严格）”——否则子类无法完全替换父类，违反LSP。

下面我会先拆解“参数协变”的语义本质，再用「正反案例+Java适配方案」帮你彻底理解。

### 一、先理清：语法层面 vs 语义层面的“参数协变”

|维度|规则说明|Java语法支持情况|
|---|---|---|
|语法层面|方法重写要求参数类型**完全一致**，子类参数不能是父类参数的父类（无协变）|不支持（参数不一致=重载，非重写）|
|语义层面（LSP要求）|子类方法能处理的参数范围 ≥ 父类方法能处理的范围（更宽松）|需通过代码逻辑实现|
### 二、语义层面的“参数宽松”案例（LSP合规）

#### 场景背景

设计一个「消息处理器」：父类处理「文本消息（TextMessage）」，子类作为「通用消息处理器」，能处理「所有类型的消息（Message，TextMessage的父类）」——子类参数范围更宽松，符合LSP。

#### 1. 基础类定义

```java

// 父类：通用消息（最宽松的类型）
class Message {
    protected String content;
    public Message(String content) { this.content = content; }
    public String getContent() { return content; }
}

// 子类：文本消息（更具体的类型）
class TextMessage extends Message {
    public TextMessage(String content) { super(content); }
    // 文本消息专属方法
    public boolean isPlainText() { return true; }
}
```

#### 2. 父类：文本消息处理器（参数仅支持TextMessage）

```java

// 父类契约：只能处理TextMessage类型的参数
class TextMessageHandler {
    public void handle(TextMessage msg) {
        System.out.println("处理文本消息：" + msg.getContent());
    }
}
```

#### 3. 子类：通用消息处理器（参数支持Message，更宽松）

```java

// 子类契约：能处理所有Message类型（包含TextMessage），参数范围更宽松
class GeneralMessageHandler extends TextMessageHandler {
    // 注意：Java中这里不能直接重写为handle(Message msg)（参数不一致，是重载）
    // 因此需要先重写父类方法，再在内部扩展参数范围
    @Override
    public void handle(TextMessage msg) {
        // 先处理父类要求的TextMessage
        super.handle(msg);
    }

    // 新增方法：支持更宽松的Message类型（体现“参数更宽松”的语义）
    public void handle(Message msg) {
        System.out.println("处理通用消息：" + msg.getContent());
        // 若msg是TextMessage，复用父类逻辑
        if (msg instanceof TextMessage) {
            handle((TextMessage) msg);
        }
    }
}
```

#### 4. 业务代码：子类可完全替换父类

```java

public class HandlerDemo {
    public static void main(String[] args) {
        // 1. 父类只能处理TextMessage
        TextMessageHandler textHandler = new TextMessageHandler();
        textHandler.handle(new TextMessage("你好")); // 正常
        // textHandler.handle(new Message("通用消息")); // 编译报错（父类不支持）

        // 2. 子类替换父类：既能处理TextMessage（兼容父类），也能处理Message（更宽松）
        TextMessageHandler generalHandler = new GeneralMessageHandler();
        generalHandler.handle(new TextMessage("你好")); // 兼容父类逻辑，正常
        // 调用子类新增的宽松参数方法
        ((GeneralMessageHandler) generalHandler).handle(new Message("通用消息")); // 正常
    }
}
```

#### 核心说明

- 子类`GeneralMessageHandler`的参数处理范围（`Message`）比父类（`TextMessage`）更宽松；

- 子类完全能替换父类：父类能处理的`TextMessage`，子类也能处理；子类还能处理父类处理不了的`Message`；

- 这符合LSP的核心要求：**子类的能力≥父类，替换后程序行为不降级，反而更强大**。

### 三、反例：子类参数更严格（违反LSP）

如果反过来，父类处理`Message`（宽松），子类只处理`TextMessage`（严格），就会违反LSP：

```java

// 父类：通用消息处理器（参数支持Message，宽松）
class GeneralMessageHandler {
    public void handle(Message msg) {
        System.out.println("处理通用消息：" + msg.getContent());
    }
}

// 子类：文本消息处理器（参数仅支持TextMessage，严格）
class TextMessageHandler extends GeneralMessageHandler {
    // Java中这是重载，不是重写
    public void handle(TextMessage msg) {
        System.out.println("处理文本消息：" + msg.getContent());
    }

    // 若强制重写（参数必须和父类一致），则内部限制类型，违反LSP
    @Override
    public void handle(Message msg) {
        if (!(msg instanceof TextMessage)) {
            throw new IllegalArgumentException("仅支持文本消息");
        }
        handle((TextMessage) msg);
    }
}

// 业务代码：子类替换父类后失效
public class BadHandlerDemo {
    public static void process(GeneralMessageHandler handler, Message msg) {
        handler.handle(msg); // 父类能处理所有Message，子类仅处理TextMessage
    }

    public static void main(String[] args) {
        // 父类处理通用消息：正常
        process(new GeneralMessageHandler(), new Message("通用消息"));

        // 子类处理通用消息：抛异常（违反LSP）
        process(new TextMessageHandler(), new Message("通用消息"));
    }
}
```

#### 问题本质

父类承诺“能处理所有Message”，子类却把参数范围缩小为“仅TextMessage”，导致子类替换父类后，传入通用Message时程序抛异常——这就是“参数更严格”违反LSP的典型场景。

### 四、Java中实现“参数协变”的实用方案

由于Java语法不支持参数协变，实际开发中可通过以下2种方式实现“子类参数更宽松”的语义：

1. **泛型约束**（推荐）：父类用泛型定义参数范围，子类放宽泛型上限；
        `// 父类：泛型约束为TextMessage
class MessageHandler<T extends TextMessage> {
    public void handle(T msg) {
        System.out.println("处理消息：" + msg.getContent());
    }
}

// 子类：放宽泛型上限为Message（更宽松）
class GeneralHandler extends MessageHandler<Message> {
    @Override
    public void handle(Message msg) {
        System.out.println("处理通用消息：" + msg.getContent());
    }
}`

2. **重写+类型判断**：重写父类方法时，内部支持更宽松的类型；

如前面的`GeneralMessageHandler`案例，先重写父类的`handle(TextMessage)`，再新增`handle(Message)`方法扩展范围。

### 总结

1. “子类参数更宽松”的核心是：**子类能处理的参数范围 ≥ 父类**，保证子类可完全替换父类；

2. Java语法不支持参数协变（参数不一致是重载），需通过「泛型」或「重写+新增方法」实现语义上的宽松；

3. 反例警示：子类参数若比父类更严格（范围更小），会导致替换父类后程序异常，违反LSP。

这个规则的本质是保证继承体系的“兼容性”——父类能做的，子类不仅能做，还能做更多（处理更宽松的参数），而不是“做更少”（仅处理更严格的参数）。
> （注：文档部分内容可能由 AI 生成）