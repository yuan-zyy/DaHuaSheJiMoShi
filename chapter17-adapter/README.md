## 第17章 适配器模式

### 17.1 适配器模式
适配器模式，这是一种常用的结构型设计模式，核心作用是解决***接口不兼容***的问题，让原本因接口不匹配而无法协同工作的类能够正常交互，就像生活中的电源适配器（将 220V 交流电转换为设备所需的低压直流电）一样

#### 一、核心角色
适配器模式主要包含3个核心角色（类适配器还会设计4个角色，后续说明）：
1. ***目标接口(Target)***: 客户端期望使用的统一接口，是适配器最终要转换成的接口形态，客户端只与该接口交互
2. ***适配者(Adaptee)***: 存在接口不兼容问题的现有类/接口，是需要被适配的对象（即 "待转换的原始资源" ）
3. ***适配器(Adapter)***: 核心中间件，实现了目标接口，并持有适配者的引用（或者继承适配者），负责将目标接口转换为对适配者方法的调用，完成接口适配
4. ***(类适配器专属)***: 适配者类的父类(因Java单继承特性，类适配器需要继承适配者，同时实现目标接口)

#### 二、两种常见实现方式
Java 中适配器模式分为对象适配器（更常用，推荐）和类适配器（受单继承限制，使用较少），下面分别通过代码示例说明

##### 1. 对象适配器 (推荐，组合复用原则)
对象适配器通过 ** 组合（持有适配者实例）** 的方式实现适配，无需继承适配者，灵活性更高，符合 “组合优于继承” 的设计原则

***示例代码***
```java
// 1. 目标接口(Target): 客户端期望使用的接口
public interface TargetInterface {
    // 客户端需要的业务方法
    void targetMethod();
}

// 2. 适配者(Adaptee): 接口不兼容的现有类，需要被适配
public class Adaptee {
    // 适配者的原有方法(与目标接口不兼容)
    public void adapteeMethod() {
        System.out.println("执行适配者Adaptee的原有业务逻辑");
    }
}

// 3. 适配器(Adapter): 实现目标接口，持有适配者实例（组合方法）
public class ObjectAdapter implements TargetInterface {
    // 持有适配者对象（核心: 组合复用）
    private Adaptee adaptee;
    
    // 构造方法注入适配者实例
    public ObjectAdapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }
    
    @Override
    public void targetMethod() {
        // 适配逻辑: 将目标接口调用转换为适配者方法调用
        adaptee.adapteeMethod();
    }
}

// 4. 客户端测试类
public class Client {
    public static void main(String[] args) {
        // 1. 创建适配者实例（原始不兼容对象）
        Adaptee adaptee = new Adaptee();
        
        // 2. 创建适配器实例，包装适配者实例
        ObjectAdapter adapter = new ObjectAdapter(adaptee);
        
        // 3. 客户端仅调用目标接口方法，无需感知适配者的存在
        adapter.targetMethod(); // 输出: 执行适配者Adaptee的原有业务逻辑
    }
}
```

##### 2. 类适配器 (单继承限制，灵活性低)
类适配器通过***继承适配者类*** + 实现目标接口的方式实现适配，因 Java 仅支持单继承，若适配者是接口或已有父类，该方式无法使用，灵活性较差

***示例代码***
```java
// 1. 目标接口（Target）：与对象适配器一致
public interface TargetInterface {
    void targetMethod();
}

// 2. 适配者（Adaptee）：与对象适配器一致
public class Adaptee {
    public void adapteeMethod() {
        System.out.println("执行适配者Adaptee的原有业务逻辑");
    }
}

// 3. 适配器（Adapter）：继承适配者，同时实现目标接口
public class ClassAdapter extends Adaptee implements TargetInterface {
    // 实现目标接口方法，直接复用父类（适配者）的方法
    @Override
    public void targetMethod() {
        // 适配逻辑：直接调用父类的adapteeMethod（因继承关系）
        super.adapteeMethod();
    }
}

// 4. 客户端测试类
public class Client {
    public static void main(String[] args) {
        // 直接创建适配器实例，无需手动传入适配者
        TargetInterface target = new ClassAdapter();

        // 客户端调用目标接口方法
        target.targetMethod(); // 输出：执行适配者Adaptee的原有业务逻辑
    }
}
```

#### 三、核心应用场景
适配器模式的核心使用场景是接口不兼容的整合场景，常见场景包括

1. 整合第三方组件 / 框架：当项目引入第三方库，其接口与项目内部接口不一致时，通过适配器转换，无需修改第三方代码和项目核心代码。
2. 遗留系统重构：老旧系统的接口无法满足新业务需求，直接修改遗留系统风险高，通过适配器将旧接口适配为新接口，实现新旧系统无缝对接。
3. 统一接口形态：当多个类具有相似功能但接口不一致时，通过适配器封装为统一的目标接口，方便客户端统一调用（如多数据源适配：MySQL、Oracle、Redis 的查询接口统一）。
4. 兼容旧版本 API：当项目 API 升级后，为了兼容旧版本调用，通过适配器将新版本接口适配为旧版本接口

#### 四、关键区别（对象适配器 vs 类适配器）

| 特性   | 对象适配器（组合）                | 类适配器（继承）                |
|------|--------------------------|-------------------------|
| 灵活性  | 高，可适配多个适配者（通过构造方法传入不同子类） | 低，受Java单继承限制，仅能适配一个适配者  |
| 耦合度  | 低，仅持有适配者引用，无继承关系         | 高，与适配者强耦合（继承关系）         |
| 可扩展性 | 好，便于新增适配逻辑，不影响原有代码       | 差，修改适配逻辑可能影响父类          |
| 适用场景 | 绝大多数场景（推荐优先使用）           | 适配者类稳定、无子类，且无需适配多个对象的场景 |

#### 总结

1.  适配器模式的核心是**解决接口不兼容问题**，让不兼容的类可以协同工作。
2.  核心角色：目标接口（Target）、适配者（Adaptee）、适配器（Adapter）。
3.  两种实现：对象适配器（组合，推荐）、类适配器（继承，受限多）。
4.  核心场景：整合第三方组件、遗留系统重构、统一接口形态、兼容旧API。
5.  设计原则：对象适配器遵循“组合优于继承”，耦合度更低、灵活性更高。

### n.2 

#### 一、

#### 二、

#### 总结
