## 第15章 抽象工厂设计模式

### 15.1 抽象工厂设计模式
***抽象工厂模式（Abstract Factory Pattern）***

#### 一、核心定义
***抽象工厂模式(Abstract Factory Pattern)***是一种***创建型设计模式***，它的核心作用是: ***提供一个统一的接口(抽象工厂)，用于创建一系列想关或相互依赖的产品对象，而无需指定他们的具体类***

***与简单工厂、工厂方法模式不同，抽象工厂模式关注的是 “产品族“ 的创建，而非单产品***

#### 二、核心组成部分
抽象工厂模式包含 4 个关键角色，它们各司其职，层层依赖：
1. **抽象工厂（Abstract Factory）**：核心接口/抽象类，定义了创建一系列相关产品的抽象方法（每个方法对应一个产品类型），是具体工厂的规范。
2. **具体工厂（Concrete Factory）**：实现/继承抽象工厂的类，负责创建某一特定产品族的所有具体产品实例。
3. **抽象产品（Abstract Product）**：定义某一类产品的公共接口/抽象类，是具体产品的规范，通常按产品类型划分（如"手机""电脑"为两个抽象产品）。
4. **具体产品（Concrete Product）**：实现/继承抽象产品的类，是抽象工厂模式最终创建的对象，与具体工厂一一对应（某一具体工厂创建对应产品族的具体产品）。

#### 三、代码实战示例
我们以 “电子设备产品族” 为例：定义两个产品族（华为产品族、苹果产品族），每个产品族包含两个相关产品（手机、电脑），通过抽象工厂模式实现创建
##### 步骤 1：定义抽象产品（两个产品类型）
1.1 手机抽象产品(Abstract Product1)
```java
// 手机抽象产品：定义手机的公共行为
public interface Phone {
    // 开机方法
    void powerOn();
    // 获取手机品牌
    String getBrand();
}
```
1.2 电脑抽象产品(Abstract Product2)
```java
// 电脑抽象产品：定义电脑的公共行为
public interface Computer {
    // 启动系统方法
    void bootSystem();
    // 获取电脑品牌
    String getBrand();
}
```

##### 步骤 2：定义具体产品（对应两个产品族）
2.1 华为产品组的具体产品
```java
// 华为手机（具体产品：属于华为产品族、手机产品类型）
public class HuaweiPhone implements Phone {
    @Override
    public void powerOn() {
        System.out.println("华为手机开机，显示鸿蒙系统启动界面");
    }

    @Override
    public String getBrand() {
        return "华为";
    }
}

// 华为电脑（具体产品：属于华为产品族、电脑产品类型）
public class HuaweiComputer implements Computer {
    @Override
    public void bootSystem() {
        System.out.println("华为电脑开机，启动Windows系统（或鸿蒙PC版）");
    }

    @Override
    public String getBrand() {
        return "华为";
    }
}
```
2.2 苹果产品组具体的产品
```java
// 苹果手机（具体产品：属于苹果产品族、手机产品类型）
public class IPhone implements Phone {
    @Override
    public void powerOn() {
        System.out.println("苹果手机开机，显示iOS系统启动界面");
    }

    @Override
    public String getBrand() {
        return "苹果";
    }
}

// 苹果电脑（具体产品：属于苹果产品族、电脑产品类型）
public class MacComputer implements Computer {
    @Override
    public void bootSystem() {
        System.out.println("苹果电脑开机，启动macOS系统");
    }

    @Override
    public String getBrand() {
        return "苹果";
    }
}
```

##### 步骤 3：定义抽象工厂（规范产品族的创建）
```java
// 电子设备抽象工厂：定义创建手机和电脑（相关产品）的抽象方法
public interface ElectronicFactory {
    // 创建手机产品
    Phone createPhone();
    // 创建电脑产品
    Computer createComputer();
}
```

##### 步骤 4：定义具体工厂（对应具体产品族）
4.1 华为工厂（创建华为产品族）
```java
public class HuaweiFactory implements ElectronicFactory {
    @Override
    public Phone createPhone() {
        return new HuaweiPhone(); // 返回华为手机实例
    }

    @Override
    public Computer createComputer() {
        return new HuaweiComputer(); // 返回华为电脑实例
    }
}
```

4.2 苹果工厂（创建苹果产品族）
```java
public class AppleFactory implements ElectronicFactory {
    @Override
    public Phone createPhone() {
        return new IPhone(); // 返回苹果手机实例
    }

    @Override
    public Computer createComputer() {
        return new MacComputer(); // 返回苹果电脑实例
    }
}
```

##### 步骤 5：客户端测试使用
```java
public class Client {
    public static void main(String[] args) {
        // 1. 创建华为工厂，生产华为产品族
        ElectronicFactory huaweiFactory = new HuaweiFactory();
        Phone huaweiPhone = huaweiFactory.createPhone();
        Computer huaweiComputer = huaweiFactory.createComputer();
        System.out.println("=== 华为产品族 ===");
        huaweiPhone.powerOn();
        huaweiComputer.bootSystem();
        System.out.println("手机品牌：" + huaweiPhone.getBrand());
        System.out.println("电脑品牌：" + huaweiComputer.getBrand());

        System.out.println("------------------------");

        // 2. 创建苹果工厂，生产苹果产品族
        ElectronicFactory appleFactory = new AppleFactory();
        Phone iphone = appleFactory.createPhone();
        Computer mac = appleFactory.createComputer();
        System.out.println("=== 苹果产品族 ===");
        iphone.powerOn();
        mac.bootSystem();
        System.out.println("手机品牌：" + iphone.getBrand());
        System.out.println("电脑品牌：" + mac.getBrand());
    }
}
```

#### 运行结果
```text
=== 华为产品族 ===
华为手机开机，显示鸿蒙系统启动界面
华为电脑开机，启动Windows系统（或鸿蒙PC版）
手机品牌：华为
电脑品牌：华为
------------------------
=== 苹果产品族 ===
苹果手机开机，显示iOS系统启动界面
苹果电脑开机，启动macOS系统
手机品牌：苹果
电脑品牌：苹果
```

#### 关键特点与优缺点
***优点***</br>
1. **封装性强**：客户端无需关心产品的具体创建细节，只需通过抽象工厂和抽象产品交互，降低了代码耦合度。
2. **产品族一致性**：确保创建的一系列产品（如华为手机 + 华为电脑）是相互匹配、兼容的，避免出现 "华为手机 + 苹果电脑" 这种不配套的组合。
3. **易于切换产品族**：只需更换具体工厂（如从HuaweiFactory切换到AppleFactory），即可快速切换整个产品族，符合 "开闭原则"

***缺点***</br>
***缺点***</br>
1. **扩展新产品类型困难**：如果需要新增一个产品类型（如平板），不仅要新增平板的抽象产品和具体产品，还需要修改所有抽象工厂和具体工厂的代码，违反 "开闭原则"。
2. **系统复杂度提升**：随着产品族和产品类型的增加，会产生大量的工厂类和产品类，增加系统的维护成本和理解难度。

#### 五、适用场景
1. **产品族创建场景**：当需要创建一系列相关或相互依赖的产品对象（产品族），且需要保证产品族的一致性时。例如：家电产品族（冰箱、洗衣机、空调）、汽车配件产品族（发动机、轮胎、座椅）等。
2. **解耦产品创建与使用**：当客户端不关心产品的具体实现细节，只需要依赖抽象接口创建产品，且需要灵活切换产品族时。
3. **系统架构优化**：当系统需要隔离具体产品的创建与使用，提高代码可维护性和可扩展性时。



#### 总结
1. 抽象工厂模式是创建型模式，核心是创建产品族，而非单个产品。
2. 四大核心角色：抽象工厂、具体工厂、抽象产品、具体产品，缺一不可。
3. 优势是封装性强、产品族兼容、易于切换；劣势是扩展新商品类型困难、系统复杂度高。
4. 典型适用场景：需要配套产品创建、需灵活切换产品族的业务场景


### n.2 

#### 一、

#### 二、

#### 总结
